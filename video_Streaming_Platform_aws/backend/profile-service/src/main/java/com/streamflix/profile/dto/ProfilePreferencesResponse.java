package com.streamflix.profile.dto;

import com.streamflix.profile.entity.ProfilePreferences.PlaybackQuality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePreferencesResponse {
    private UUID id;
    private String subtitleLanguage;
    private Boolean subtitleEnabled;
    private String audioLanguage;
    private PlaybackQuality playbackQuality;
    private Boolean dataSaverEnabled;
    private Boolean notificationsEnabled;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
}
