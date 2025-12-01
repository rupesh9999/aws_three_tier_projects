package com.streamflix.notification.dto;

import com.streamflix.notification.entity.NotificationPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesResponse {
    
    private UUID id;
    private UUID userId;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
    private Boolean smsEnabled;
    private Boolean marketingEmails;
    private Boolean newContentAlerts;
    private Boolean watchlistUpdates;
    private Boolean accountAlerts;
    private Boolean weeklyDigest;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
    
    public static NotificationPreferencesResponse fromEntity(NotificationPreferences prefs) {
        return NotificationPreferencesResponse.builder()
                .id(prefs.getId())
                .userId(prefs.getUserId())
                .emailEnabled(prefs.getEmailEnabled())
                .pushEnabled(prefs.getPushEnabled())
                .inAppEnabled(prefs.getInAppEnabled())
                .smsEnabled(prefs.getSmsEnabled())
                .marketingEmails(prefs.getMarketingEmails())
                .newContentAlerts(prefs.getNewContentAlerts())
                .watchlistUpdates(prefs.getWatchlistUpdates())
                .accountAlerts(prefs.getAccountAlerts())
                .weeklyDigest(prefs.getWeeklyDigest())
                .quietHoursStart(prefs.getQuietHoursStart())
                .quietHoursEnd(prefs.getQuietHoursEnd())
                .timezone(prefs.getTimezone())
                .createdAt(prefs.getCreatedAt())
                .updatedAt(prefs.getUpdatedAt())
                .build();
    }
}
