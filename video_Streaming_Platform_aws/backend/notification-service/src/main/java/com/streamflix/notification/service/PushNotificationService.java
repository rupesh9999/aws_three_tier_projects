package com.streamflix.notification.service;

import com.streamflix.notification.entity.PushToken;
import com.streamflix.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Push notification service.
 * In production, this would integrate with Firebase Cloud Messaging (FCM) and Apple Push Notification Service (APNS).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {
    
    private final PushTokenRepository pushTokenRepository;
    
    @Value("${fcm.server-key:mock-fcm-key}")
    private String fcmServerKey;
    
    @Value("${apns.key-id:mock-apns-key}")
    private String apnsKeyId;
    
    /**
     * Send push notification to all user's devices
     */
    public void sendPushNotification(UUID userId, String title, String message, String actionUrl, String imageUrl) {
        log.info("Sending push notification to user: {}", userId);
        
        List<PushToken> tokens = pushTokenRepository.findByUserIdAndIsActiveTrue(userId);
        
        if (tokens.isEmpty()) {
            log.debug("No active push tokens for user: {}", userId);
            return;
        }
        
        for (PushToken token : tokens) {
            try {
                sendToDevice(token, title, message, actionUrl, imageUrl);
            } catch (Exception e) {
                log.error("Failed to send push notification to device: {}", token.getDeviceId(), e);
                // Mark token as inactive if it fails consistently
                handlePushError(token, e);
            }
        }
    }
    
    /**
     * Send push notification to specific device
     */
    public void sendToDevice(PushToken token, String title, String message, String actionUrl, String imageUrl) {
        log.debug("Sending push notification to device: {} on platform: {}", token.getDeviceId(), token.getDeviceType());
        
        Map<String, Object> payload = buildPayload(title, message, actionUrl, imageUrl);
        
        switch (token.getDeviceType().toUpperCase()) {
            case "IOS" -> sendApnsPush(token.getToken(), payload);
            case "ANDROID" -> sendFcmPush(token.getToken(), payload);
            case "WEB" -> sendWebPush(token.getToken(), payload);
            default -> log.warn("Unknown device type: {}", token.getDeviceType());
        }
    }
    
    /**
     * Send push notification to multiple users
     */
    public void sendBulkPushNotification(List<UUID> userIds, String title, String message, String actionUrl, String imageUrl) {
        log.info("Sending bulk push notification to {} users", userIds.size());
        
        for (UUID userId : userIds) {
            try {
                sendPushNotification(userId, title, message, actionUrl, imageUrl);
            } catch (Exception e) {
                log.error("Failed to send push notification to user: {}", userId, e);
            }
        }
    }
    
    /**
     * Send silent/data push notification
     */
    public void sendSilentPush(UUID userId, Map<String, Object> data) {
        log.debug("Sending silent push to user: {}", userId);
        
        List<PushToken> tokens = pushTokenRepository.findByUserIdAndIsActiveTrue(userId);
        
        for (PushToken token : tokens) {
            try {
                sendSilentToDevice(token, data);
            } catch (Exception e) {
                log.error("Failed to send silent push to device: {}", token.getDeviceId(), e);
            }
        }
    }
    
    private void sendApnsPush(String token, Map<String, Object> payload) {
        log.debug("Sending APNS push notification");
        // Mock implementation - would use APNS SDK
        log.info("APNS push sent to token: {}...", token.substring(0, Math.min(10, token.length())));
    }
    
    private void sendFcmPush(String token, Map<String, Object> payload) {
        log.debug("Sending FCM push notification");
        // Mock implementation - would use Firebase Admin SDK
        log.info("FCM push sent to token: {}...", token.substring(0, Math.min(10, token.length())));
    }
    
    private void sendWebPush(String token, Map<String, Object> payload) {
        log.debug("Sending Web push notification");
        // Mock implementation - would use web-push library
        log.info("Web push sent to token: {}...", token.substring(0, Math.min(10, token.length())));
    }
    
    private void sendSilentToDevice(PushToken token, Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>(data);
        payload.put("content-available", 1);
        payload.put("silent", true);
        
        switch (token.getDeviceType().toUpperCase()) {
            case "IOS" -> sendApnsPush(token.getToken(), payload);
            case "ANDROID" -> sendFcmPush(token.getToken(), payload);
            case "WEB" -> sendWebPush(token.getToken(), payload);
        }
    }
    
    private Map<String, Object> buildPayload(String title, String message, String actionUrl, String imageUrl) {
        Map<String, Object> payload = new HashMap<>();
        
        Map<String, String> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", message);
        if (imageUrl != null) {
            notification.put("image", imageUrl);
        }
        
        Map<String, String> data = new HashMap<>();
        if (actionUrl != null) {
            data.put("action_url", actionUrl);
        }
        data.put("click_action", "OPEN_APP");
        
        payload.put("notification", notification);
        payload.put("data", data);
        
        return payload;
    }
    
    private void handlePushError(PushToken token, Exception error) {
        String errorMessage = error.getMessage();
        
        // Check for common errors that indicate token is invalid
        if (errorMessage != null && (
                errorMessage.contains("NotRegistered") ||
                errorMessage.contains("InvalidRegistration") ||
                errorMessage.contains("Unregistered") ||
                errorMessage.contains("BadDeviceToken"))) {
            
            log.warn("Push token is invalid, deactivating: {}", token.getId());
            token.setIsActive(false);
            pushTokenRepository.save(token);
        }
    }
}
