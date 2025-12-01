package com.streamflix.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;

/**
 * Email notification service.
 * In production, this would integrate with AWS SES or similar email service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.from:noreply@streamflix.com}")
    private String fromEmail;
    
    @Value("${app.base-url:https://streamflix.com}")
    private String baseUrl;
    
    /**
     * Send a simple notification email
     */
    public void sendNotificationEmail(UUID userId, String subject, String message, String actionUrl) {
        log.info("Sending notification email to user: {}", userId);
        
        // In production, would look up user email from user service
        String userEmail = getUserEmail(userId);
        
        if (userEmail == null) {
            log.warn("No email found for user: {}", userId);
            return;
        }
        
        try {
            Context context = new Context();
            context.setVariable("title", subject);
            context.setVariable("message", message);
            context.setVariable("actionUrl", actionUrl != null ? actionUrl : baseUrl);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("notification-email", context);
            
            sendHtmlEmail(userEmail, subject, htmlContent);
            log.info("Sent notification email to: {}", userEmail);
            
        } catch (Exception e) {
            log.error("Failed to send notification email to user: {}", userId, e);
            // Fallback to simple email
            sendSimpleEmail(userEmail, subject, message);
        }
    }
    
    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(String email, String name) {
        log.info("Sending welcome email to: {}", email);
        
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("welcome-email", context);
            sendHtmlEmail(email, "Welcome to StreamFlix!", htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        log.info("Sending password reset email to: {}", email);
        
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("password-reset-email", context);
            sendHtmlEmail(email, "Reset Your StreamFlix Password", htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
        }
    }
    
    /**
     * Send subscription confirmation email
     */
    public void sendSubscriptionEmail(String email, String planName, String nextBillingDate) {
        log.info("Sending subscription email to: {}", email);
        
        try {
            Context context = new Context();
            context.setVariable("planName", planName);
            context.setVariable("nextBillingDate", nextBillingDate);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("subscription-email", context);
            sendHtmlEmail(email, "Your StreamFlix Subscription is Active", htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to send subscription email", e);
        }
    }
    
    /**
     * Send payment receipt email
     */
    public void sendPaymentReceiptEmail(String email, Map<String, Object> paymentDetails) {
        log.info("Sending payment receipt to: {}", email);
        
        try {
            Context context = new Context();
            context.setVariables(paymentDetails);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("payment-receipt-email", context);
            sendHtmlEmail(email, "StreamFlix Payment Receipt", htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to send payment receipt email", e);
        }
    }
    
    /**
     * Send weekly digest email
     */
    public void sendWeeklyDigestEmail(String email, Map<String, Object> digestData) {
        log.info("Sending weekly digest to: {}", email);
        
        try {
            Context context = new Context();
            context.setVariables(digestData);
            context.setVariable("baseUrl", baseUrl);
            
            String htmlContent = templateEngine.process("weekly-digest-email", context);
            sendHtmlEmail(email, "Your Weekly StreamFlix Digest", htmlContent);
            
        } catch (Exception e) {
            log.error("Failed to send weekly digest email", e);
        }
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    private void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send simple email", e);
        }
    }
    
    private String getUserEmail(UUID userId) {
        // Mock implementation - in production, would call user service
        // For now, return a mock email based on userId
        return "user_" + userId.toString().substring(0, 8) + "@example.com";
    }
}
