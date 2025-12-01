package com.fintech.banking.service;

import com.fintech.banking.dto.AuthDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthDto.AuthResponse register(AuthDto.RegisterRequest request, HttpServletRequest httpRequest);
    AuthDto.AuthResponse login(AuthDto.LoginRequest request, HttpServletRequest httpRequest);
    AuthDto.AuthResponse verifyMfa(AuthDto.MfaVerifyRequest request);
    AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request);
    void logout(String token);
    void forgotPassword(AuthDto.ForgotPasswordRequest request);
    void resetPassword(AuthDto.ResetPasswordRequest request);
    void changePassword(AuthDto.ChangePasswordRequest request);
}
