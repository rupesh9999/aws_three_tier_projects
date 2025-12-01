package com.streamflix.notification.dto;

import com.streamflix.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {
    
    private UUID userId;
    
    private List<UUID> userIds;
    
    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;
    
    @Builder.Default
    private Notification.NotificationPriority priority = Notification.NotificationPriority.NORMAL;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String data;
    
    private String category;
    
    private String actionUrl;
    
    private String imageUrl;
    
    private Instant expiresAt;
}
