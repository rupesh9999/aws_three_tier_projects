package com.streamflix.auth.service;

import com.streamflix.auth.dto.*;
import com.streamflix.auth.entity.*;
import com.streamflix.auth.repository.*;
import com.streamflix.common.exception.BusinessException;
import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.common.exception.UnauthorizedException;
import com.streamflix.common.security.JwtUtils;
import com.streamflix.common.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    
    @Value("${app.auth.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${app.auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;
    
    @Value("${app.auth.password-reset-token-validity-hours:24}")
    private int passwordResetTokenValidityHours;
    
    @Value("${app.auth.email-verification-token-validity-hours:48}")
    private int emailVerificationTokenValidityHours;
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        log.info("Processing registration for email: {}", CommonUtils.maskEmail(request.getEmail()));
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BusinessException("Email already registered", "EMAIL_EXISTS");
        }
        
        // Get default user role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BusinessException("Default role not found", "ROLE_NOT_FOUND"));
        
        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .passwordChangedAt(LocalDateTime.now())
                .build();
        
        user.addRole(userRole);
        user = userRepository.save(user);
        
        // Generate verification token
        String verificationToken = generateEmailVerificationToken(user);
        
        // Send verification email asynchronously
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        
        // Generate tokens
        AuthResponse response = generateAuthResponse(user, ipAddress, userAgent);
        
        // Log audit
        logAudit(user.getId(), AuditLog.ACTION_REGISTER, ipAddress, userAgent, null, true, null);
        
        log.info("User registered successfully: {}", user.getId());
        return response;
    }
    
    /**
     * Authenticate user and return tokens
     */
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing login for email: {}", CommonUtils.maskEmail(request.getEmail()));
        
        User user = userRepository.findByEmailWithRoles(request.getEmail().toLowerCase())
                .orElseThrow(() -> {
                    logAudit(null, AuditLog.ACTION_LOGIN, ipAddress, userAgent, 
                            Map.of("email", request.getEmail()), false, "User not found");
                    return new UnauthorizedException("Invalid email or password");
                });
        
        // Check if account is deleted
        if (user.isDeleted()) {
            throw new UnauthorizedException("Account has been deleted");
        }
        
        // Check if account is locked
        if (user.getAccountLocked() && !user.isLockExpired()) {
            throw new BusinessException("Account is locked. Please try again later.", "ACCOUNT_LOCKED");
        }
        
        // Unlock if lock has expired
        if (user.getAccountLocked() && user.isLockExpired()) {
            user.unlock();
            userRepository.save(user);
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress, userAgent);
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Successful login
        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);
        
        // Generate tokens
        AuthResponse response = generateAuthResponse(user, ipAddress, userAgent);
        
        // Log audit
        logAudit(user.getId(), AuditLog.ACTION_LOGIN, ipAddress, userAgent, null, true, null);
        
        log.info("User logged in successfully: {}", user.getId());
        return response;
    }
    
    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(refreshToken);
        
        RefreshToken storedToken = refreshTokenRepository.findValidToken(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));
        
        User user = storedToken.getUser();
        
        if (user.getAccountLocked() || user.isDeleted()) {
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Account is not accessible");
        }
        
        // Revoke old refresh token
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        
        // Generate new tokens
        AuthResponse response = generateAuthResponse(user, ipAddress, userAgent);
        
        // Log audit
        logAudit(user.getId(), AuditLog.ACTION_TOKEN_REFRESH, ipAddress, userAgent, null, true, null);
        
        return response;
    }
    
    /**
     * Logout user and revoke refresh token
     */
    public void logout(String refreshToken, UUID userId, String ipAddress, String userAgent) {
        if (refreshToken != null) {
            String tokenHash = hashToken(refreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
        }
        
        logAudit(userId, AuditLog.ACTION_LOGOUT, ipAddress, userAgent, null, true, null);
        log.info("User logged out: {}", userId);
    }
    
    /**
     * Logout from all devices
     */
    public void logoutAll(UUID userId, String ipAddress, String userAgent) {
        refreshTokenRepository.revokeAllUserTokens(userId, LocalDateTime.now());
        
        logAudit(userId, AuditLog.ACTION_SESSION_REVOKED, ipAddress, userAgent, 
                Map.of("scope", "all_devices"), true, null);
        log.info("User logged out from all devices: {}", userId);
    }
    
    /**
     * Verify email address
     */
    public void verifyEmail(String token) {
        String tokenHash = hashToken(token);
        
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findValidToken(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Invalid or expired verification token", "INVALID_TOKEN"));
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationToken.markAsUsed();
        emailVerificationTokenRepository.save(verificationToken);
        
        logAudit(user.getId(), AuditLog.ACTION_EMAIL_VERIFICATION, null, null, null, true, null);
        log.info("Email verified for user: {}", user.getId());
    }
    
    /**
     * Resend verification email
     */
    public void resendVerificationEmail(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (user.getEmailVerified()) {
            throw new BusinessException("Email is already verified", "ALREADY_VERIFIED");
        }
        
        // Invalidate existing tokens
        emailVerificationTokenRepository.invalidateAllUserTokens(userId, LocalDateTime.now());
        
        // Generate new token
        String verificationToken = generateEmailVerificationToken(user);
        
        // Send email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        
        log.info("Verification email resent to user: {}", userId);
    }
    
    /**
     * Request password reset
     */
    public void requestPasswordReset(PasswordResetRequest request, String ipAddress) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().toLowerCase())
                .orElse(null);
        
        // Don't reveal if user exists or not for security
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", CommonUtils.maskEmail(request.getEmail()));
            return;
        }
        
        // Rate limiting: max 3 requests per hour
        long recentRequests = passwordResetTokenRepository.countRecentRequestsByUserId(
                user.getId(), LocalDateTime.now().minusHours(1));
        if (recentRequests >= 3) {
            throw new BusinessException("Too many password reset requests. Please try again later.", "RATE_LIMITED");
        }
        
        // Invalidate existing tokens
        passwordResetTokenRepository.invalidateAllUserTokens(user.getId(), LocalDateTime.now());
        
        // Generate new token
        String token = CommonUtils.generateSecureToken(32);
        String tokenHash = hashToken(token);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(passwordResetTokenValidityHours))
                .ipAddress(ipAddress)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
        
        logAudit(user.getId(), AuditLog.ACTION_PASSWORD_RESET_REQUEST, ipAddress, null, null, true, null);
        log.info("Password reset requested for user: {}", user.getId());
    }
    
    /**
     * Confirm password reset
     */
    public void confirmPasswordReset(PasswordResetConfirmRequest request, String ipAddress, String userAgent) {
        String tokenHash = hashToken(request.getToken());
        
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token", "INVALID_TOKEN"));
        
        User user = resetToken.getUser();
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Mark token as used
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());
        
        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
        
        logAudit(user.getId(), AuditLog.ACTION_PASSWORD_RESET, ipAddress, userAgent, null, true, null);
        log.info("Password reset completed for user: {}", user.getId());
    }
    
    /**
     * Change password for authenticated user
     */
    public void changePassword(UUID userId, ChangePasswordRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }
        
        // Check if new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password", "SAME_PASSWORD");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Optionally revoke all refresh tokens except current
        // For now, keep sessions active
        
        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
        
        logAudit(userId, AuditLog.ACTION_PASSWORD_CHANGE, ipAddress, userAgent, null, true, null);
        log.info("Password changed for user: {}", userId);
    }
    
    // Private helper methods
    
    private void handleFailedLogin(User user, String ipAddress, String userAgent) {
        user.incrementFailedAttempts();
        
        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.lock("Too many failed login attempts", lockoutDurationMinutes);
            logAudit(user.getId(), AuditLog.ACTION_ACCOUNT_LOCKED, ipAddress, userAgent, 
                    Map.of("reason", "max_attempts_exceeded"), true, null);
            log.warn("Account locked due to failed attempts: {}", user.getId());
        }
        
        userRepository.save(user);
        logAudit(user.getId(), AuditLog.ACTION_LOGIN, ipAddress, userAgent, null, false, "Invalid password");
    }
    
    private AuthResponse generateAuthResponse(User user, String ipAddress, String userAgent) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        claims.put("plan", user.getSubscriptionPlan());
        claims.put("verified", user.getEmailVerified());
        
        String accessToken = jwtUtils.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                claims
        );
        
        String refreshToken = jwtUtils.generateRefreshToken(user.getId().toString());
        
        // Store refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusNanos(jwtUtils.getRefreshTokenExpiration() * 1_000_000L))
                .build();
        
        refreshTokenRepository.save(refreshTokenEntity);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpiration() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .emailVerified(user.getEmailVerified())
                        .subscriptionPlan(user.getSubscriptionPlan())
                        .subscriptionStatus(user.getSubscriptionStatus())
                        .subscriptionExpiresAt(user.getSubscriptionExpiresAt())
                        .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
    
    private String generateEmailVerificationToken(User user) {
        String token = CommonUtils.generateSecureToken(32);
        String tokenHash = hashToken(token);
        
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(emailVerificationTokenValidityHours))
                .build();
        
        emailVerificationTokenRepository.save(verificationToken);
        return token;
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private void logAudit(UUID userId, String action, String ipAddress, String userAgent, 
                         Map<String, Object> details, boolean success, String failureReason) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(details != null ? details.toString() : null)
                    .success(success)
                    .failureReason(failureReason)
                    .build();
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
