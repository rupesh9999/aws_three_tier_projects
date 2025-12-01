package com.streamflix.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePreferencesRequest {
    
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
}
