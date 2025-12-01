package com.streamflix.notification.controller;

import com.streamflix.notification.dto.*;
import com.streamflix.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // ==================== Notifications ====================
    
    @PostMapping("/send")
    @Operation(summary = "Send a notification (internal use)")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("Sending notification: {}", request.getType());
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }
    
    @GetMapping
    @Operation(summary = "Get user's notifications")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Get user's unread notifications")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, pageable));
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
    
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/old")
    @Operation(summary = "Delete old notifications")
    public ResponseEntity<Void> deleteOldNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "30") int daysOld) {
        notificationService.deleteOldNotifications(userId, daysOld);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Preferences ====================
    
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.getPreferences(userId));
    }
    
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(notificationService.updatePreferences(userId, request));
    }
    
    // ==================== Push Tokens ====================
    
    @PostMapping("/push-token")
    @Operation(summary = "Register a push notification token")
    public ResponseEntity<Void> registerPushToken(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody RegisterPushTokenRequest request) {
        notificationService.registerPushToken(userId, request);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/push-token/{deviceId}")
    @Operation(summary = "Unregister a push token for a device")
    public ResponseEntity<Void> unregisterPushToken(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String deviceId) {
        notificationService.unregisterPushToken(userId, deviceId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/push-token")
    @Operation(summary = "Unregister all push tokens")
    public ResponseEntity<Void> unregisterAllPushTokens(
            @RequestHeader("X-User-Id") UUID userId) {
        notificationService.unregisterAllPushTokens(userId);
        return ResponseEntity.noContent().build();
    }
}
