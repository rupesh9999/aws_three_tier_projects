package com.streamflix.notification.service;

import com.streamflix.notification.dto.*;
import com.streamflix.notification.entity.Notification;
import com.streamflix.notification.entity.NotificationPreferences;
import com.streamflix.notification.entity.PushToken;
import com.streamflix.notification.repository.NotificationPreferencesRepository;
import com.streamflix.notification.repository.NotificationRepository;
import com.streamflix.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final PushTokenRepository pushTokenRepository;
    private final EmailService emailService;
    private final PushNotificationService pushService;
    
    // ==================== Notifications ====================
    
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending notification: {} to user: {}", request.getType(), request.getUserId());
        
        if (request.getUserId() != null) {
            return sendToUser(request.getUserId(), request);
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            List<NotificationResponse> responses = new ArrayList<>();
            for (UUID userId : request.getUserIds()) {
                responses.add(sendToUser(userId, request));
            }
            return responses.isEmpty() ? null : responses.get(0);
        } else {
            throw new IllegalArgumentException("Either userId or userIds must be provided");
        }
    }
    
    private NotificationResponse sendToUser(UUID userId, SendNotificationRequest request) {
        // Create notification record
        Notification notification = Notification.builder()
                .userId(userId)
                .type(request.getType())
                .priority(request.getPriority())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData())
                .category(request.getCategory())
                .actionUrl(request.getActionUrl())
                .imageUrl(request.getImageUrl())
                .expiresAt(request.getExpiresAt())
                .status(Notification.NotificationStatus.PENDING)
                .build();
        
        notification = notificationRepository.save(notification);
        
        boolean sent = false;
        String errorMessage = null;
        
        try {
            switch (request.getType()) {
                case EMAIL -> {
                    emailService.sendNotificationEmail(userId, request.getTitle(), request.getMessage(), request.getActionUrl());
                    sent = true;
                }
                case PUSH -> {
                    pushService.sendPushNotification(userId, request.getTitle(), request.getMessage(), request.getActionUrl(), request.getImageUrl());
                    sent = true;
                }
                case IN_APP -> {
                    // In-app notifications are just stored, no external delivery needed
                    sent = true;
                }
                case SMS -> {
                    // SMS would be handled by a separate SMS service
                    log.warn("SMS notifications not yet implemented");
                    sent = false;
                }
            }
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}", userId, e);
            errorMessage = e.getMessage();
        }
        
        notification.setStatus(sent ? Notification.NotificationStatus.SENT : Notification.NotificationStatus.FAILED);
        notification.setSentAt(sent ? Instant.now() : null);
        notification.setErrorMessage(errorMessage);
        notification = notificationRepository.save(notification);
        
        log.info("Notification {} status: {}", notification.getId(), notification.getStatus());
        return NotificationResponse.fromEntity(notification);
    }
    
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }
    
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }
    
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }
    
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        notification.setReadAt(Instant.now());
        notification.setStatus(Notification.NotificationStatus.READ);
        notification = notificationRepository.save(notification);
        
        return NotificationResponse.fromEntity(notification);
    }
    
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId, Instant.now());
    }
    
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    public int deleteOldNotifications(UUID userId, int daysOld) {
        Instant cutoff = Instant.now().minusSeconds(daysOld * 24L * 60 * 60);
        return notificationRepository.deleteOldNotifications(userId, cutoff);
    }
    
    // ==================== Preferences ====================
    
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return NotificationPreferencesResponse.fromEntity(prefs);
    }
    
    public NotificationPreferencesResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        log.info("Updating notification preferences for user: {}", userId);
        
        NotificationPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        
        if (request.getEmailEnabled() != null) prefs.setEmailEnabled(request.getEmailEnabled());
        if (request.getPushEnabled() != null) prefs.setPushEnabled(request.getPushEnabled());
        if (request.getInAppEnabled() != null) prefs.setInAppEnabled(request.getInAppEnabled());
        if (request.getSmsEnabled() != null) prefs.setSmsEnabled(request.getSmsEnabled());
        if (request.getMarketingEmails() != null) prefs.setMarketingEmails(request.getMarketingEmails());
        if (request.getNewContentAlerts() != null) prefs.setNewContentAlerts(request.getNewContentAlerts());
        if (request.getWatchlistUpdates() != null) prefs.setWatchlistUpdates(request.getWatchlistUpdates());
        if (request.getAccountAlerts() != null) prefs.setAccountAlerts(request.getAccountAlerts());
        if (request.getWeeklyDigest() != null) prefs.setWeeklyDigest(request.getWeeklyDigest());
        if (request.getQuietHoursStart() != null) prefs.setQuietHoursStart(request.getQuietHoursStart());
        if (request.getQuietHoursEnd() != null) prefs.setQuietHoursEnd(request.getQuietHoursEnd());
        if (request.getTimezone() != null) prefs.setTimezone(request.getTimezone());
        
        prefs = preferencesRepository.save(prefs);
        return NotificationPreferencesResponse.fromEntity(prefs);
    }
    
    // ==================== Push Tokens ====================
    
    public void registerPushToken(UUID userId, RegisterPushTokenRequest request) {
        log.info("Registering push token for user: {} on device: {}", userId, request.getDeviceId());
        
        // Check if token already exists for this device
        pushTokenRepository.findByUserIdAndDeviceId(userId, request.getDeviceId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setToken(request.getToken());
                            existing.setIsActive(true);
                            existing.setAppVersion(request.getAppVersion());
                            pushTokenRepository.save(existing);
                        },
                        () -> {
                            PushToken pushToken = PushToken.builder()
                                    .userId(userId)
                                    .token(request.getToken())
                                    .deviceType(request.getDeviceType())
                                    .deviceId(request.getDeviceId())
                                    .deviceName(request.getDeviceName())
                                    .appVersion(request.getAppVersion())
                                    .isActive(true)
                                    .build();
                            pushTokenRepository.save(pushToken);
                        }
                );
        
        log.info("Registered push token for user: {}", userId);
    }
    
    public void unregisterPushToken(UUID userId, String deviceId) {
        log.info("Unregistering push token for user: {} on device: {}", userId, deviceId);
        pushTokenRepository.deactivateToken(userId, deviceId);
    }
    
    public void unregisterAllPushTokens(UUID userId) {
        log.info("Unregistering all push tokens for user: {}", userId);
        pushTokenRepository.deactivateAllUserTokens(userId);
    }
    
    // ==================== Helper Methods ====================
    
    private NotificationPreferences createDefaultPreferences(UUID userId) {
        NotificationPreferences prefs = NotificationPreferences.builder()
                .userId(userId)
                .emailEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(false)
                .marketingEmails(false)
                .newContentAlerts(true)
                .watchlistUpdates(true)
                .accountAlerts(true)
                .weeklyDigest(true)
                .timezone("UTC")
                .build();
        
        return preferencesRepository.save(prefs);
    }
}
