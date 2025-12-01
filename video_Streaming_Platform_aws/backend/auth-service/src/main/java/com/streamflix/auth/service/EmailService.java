package com.streamflix.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@streamflix.com}")
    private String fromEmail;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.name:StreamFlix}")
    private String appName;
    
    @Async
    public void sendVerificationEmail(String to, String firstName, String token) {
        String subject = "Verify your email - " + appName;
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        
        String htmlContent = buildEmailTemplate(
            firstName != null ? firstName : "there",
            "Welcome to StreamFlix!",
            "Please verify your email address to complete your registration and start streaming.",
            "Verify Email",
            verificationUrl,
            "This link will expire in 48 hours."
        );
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    @Async
    public void sendPasswordResetEmail(String to, String firstName, String token) {
        String subject = "Reset your password - " + appName;
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        String htmlContent = buildEmailTemplate(
            firstName != null ? firstName : "there",
            "Password Reset Request",
            "We received a request to reset your password. Click the button below to create a new password.",
            "Reset Password",
            resetUrl,
            "This link will expire in 24 hours. If you didn't request this, please ignore this email."
        );
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    @Async
    public void sendPasswordChangedEmail(String to, String firstName) {
        String subject = "Password changed - " + appName;
        
        String htmlContent = buildEmailTemplate(
            firstName != null ? firstName : "there",
            "Password Changed Successfully",
            "Your password has been changed successfully. If you didn't make this change, please contact our support team immediately.",
            "Contact Support",
            frontendUrl + "/help",
            "For security reasons, you may need to sign in again on your devices."
        );
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    @Async
    public void sendAccountLockedEmail(String to, String firstName, int lockDurationMinutes) {
        String subject = "Account locked - " + appName;
        
        String htmlContent = buildEmailTemplate(
            firstName != null ? firstName : "there",
            "Account Temporarily Locked",
            String.format("Your account has been temporarily locked due to multiple failed login attempts. "
                    + "The lock will automatically expire in %d minutes.", lockDurationMinutes),
            "Contact Support",
            frontendUrl + "/help",
            "If you didn't attempt to log in, please reset your password immediately."
        );
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to " + appName + "!";
        
        String htmlContent = buildEmailTemplate(
            firstName != null ? firstName : "there",
            "Welcome to StreamFlix!",
            "Thank you for joining StreamFlix. Start exploring thousands of movies and TV shows right now!",
            "Start Watching",
            frontendUrl + "/browse",
            "Need help? Visit our Help Center for guides and FAQs."
        );
        
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", maskEmail(to));
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", maskEmail(to), e);
        }
    }
    
    private String buildEmailTemplate(String name, String title, String message, 
                                       String buttonText, String buttonUrl, String footer) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif; background-color: #141414;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #1f1f1f; border-radius: 8px;">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 40px 20px; text-align: center;">
                                        <h1 style="margin: 0; color: #e50914; font-size: 32px; font-weight: bold;">STREAMFLIX</h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 20px 40px;">
                                        <h2 style="color: #ffffff; font-size: 24px; margin: 0 0 20px;">Hi %s,</h2>
                                        <h3 style="color: #ffffff; font-size: 20px; margin: 0 0 15px;">%s</h3>
                                        <p style="color: #b3b3b3; font-size: 16px; line-height: 24px; margin: 0 0 30px;">%s</p>
                                        
                                        <table role="presentation" style="width: 100%%;">
                                            <tr>
                                                <td align="center">
                                                    <a href="%s" style="display: inline-block; background-color: #e50914; color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 4px; font-size: 16px; font-weight: bold;">%s</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 30px 40px 40px;">
                                        <p style="color: #737373; font-size: 14px; line-height: 20px; margin: 0;">%s</p>
                                        <hr style="border: none; border-top: 1px solid #333; margin: 30px 0;">
                                        <p style="color: #737373; font-size: 12px; line-height: 18px; margin: 0;">
                                            This email was sent by %s. Please do not reply to this email.
                                            <br><br>
                                            © 2024 StreamFlix. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(title, name, title, message, buttonUrl, buttonText, footer, appName);
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email.charAt(0) + "***" + email.substring(atIndex);
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
