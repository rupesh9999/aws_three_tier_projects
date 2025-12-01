package com.streamflix.notification.dto;

import com.streamflix.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    
    private UUID id;
    private UUID userId;
    private Notification.NotificationType type;
    private Notification.NotificationStatus status;
    private Notification.NotificationPriority priority;
    private String title;
    private String message;
    private String data;
    private String category;
    private String actionUrl;
    private String imageUrl;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant readAt;
    private Instant expiresAt;
    private Instant createdAt;
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .category(notification.getCategory())
                .actionUrl(notification.getActionUrl())
                .imageUrl(notification.getImageUrl())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
