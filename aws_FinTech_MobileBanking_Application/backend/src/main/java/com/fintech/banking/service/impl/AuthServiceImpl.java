package com.fintech.banking.service.impl;

import com.fintech.banking.dto.AuthDto;
import com.fintech.banking.exception.AuthenticationException;
import com.fintech.banking.exception.ResourceConflictException;
import com.fintech.banking.model.Role;
import com.fintech.banking.model.User;
import com.fintech.banking.repository.UserRepository;
import com.fintech.banking.security.JwtService;
import com.fintech.banking.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceConflictException("Phone number already registered");
        }

        User user = User.builder()
                .customerId(generateCustomerId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .panNumber(request.getPanNumber())
                .aadhaarNumber(request.getAadhaarNumber())
                .status(User.UserStatus.ACTIVE)
                .kycStatus(User.KycStatus.PENDING)
                .mfaEnabled(true)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User registered successfully: {}", user.getCustomerId());

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .user(mapToUserInfo(user))
                .mfaRequired(false)
                .build();
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }

        User user = userRepository.findByEmail(request.getUsername())
                .or(() -> userRepository.findByPhoneNumber(request.getUsername()))
                .orElseThrow(() -> new AuthenticationException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginAttempts(0);
        userRepository.save(user);

        if (user.getMfaEnabled()) {
            // Generate MFA token and send OTP
            String mfaToken = UUID.randomUUID().toString();
            // TODO: Send OTP via SMS/Email
            log.info("MFA required for user: {}", user.getCustomerId());

            return AuthDto.AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaToken(mfaToken)
                    .build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Login successful for user: {}", user.getCustomerId());

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .user(mapToUserInfo(user))
                .mfaRequired(false)
                .build();
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse verifyMfa(AuthDto.MfaVerifyRequest request) {
        log.info("MFA verification attempt");
        
        // TODO: Verify OTP from cache/database
        // For now, placeholder implementation
        
        // After verification, generate tokens
        throw new UnsupportedOperationException("MFA verification not yet implemented");
    }

    @Override
    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        log.info("Token refresh request");

        String username = jwtService.extractUsername(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String accessToken = jwtService.generateToken(userDetails);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(900)
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("Logout request");
        // TODO: Add token to blacklist/invalidate in cache
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void forgotPassword(AuthDto.ForgotPasswordRequest request) {
        log.info("Password reset request for: {}", request.getIdentifier());

        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByPhoneNumber(request.getIdentifier()))
                .orElse(null);

        if (user != null) {
            // Generate reset token and send via email/SMS
            // TODO: Implement password reset flow
            log.info("Password reset initiated for user: {}", user.getCustomerId());
        }
        
        // Always return success to prevent user enumeration
    }

    @Override
    @Transactional
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        log.info("Password reset completion");
        // TODO: Verify reset token and update password
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }

    @Override
    @Transactional
    public void changePassword(AuthDto.ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getCustomerId());
    }

    private String generateCustomerId() {
        return "CUST" + String.format("%08d", System.currentTimeMillis() % 100000000);
    }

    private AuthDto.UserInfo mapToUserInfo(User user) {
        return AuthDto.UserInfo.builder()
                .customerId(user.getCustomerId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .kycStatus(user.getKycStatus().name())
                .build();
    }
}
