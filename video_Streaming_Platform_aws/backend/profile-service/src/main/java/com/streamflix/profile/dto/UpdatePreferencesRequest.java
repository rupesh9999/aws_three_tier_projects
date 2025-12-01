package com.streamflix.profile.dto;

import com.streamflix.profile.entity.ProfilePreferences.PlaybackQuality;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePreferencesRequest {

    @Size(max = 10, message = "Subtitle language code must not exceed 10 characters")
    private String subtitleLanguage;

    private Boolean subtitleEnabled;

    @Size(max = 10, message = "Audio language code must not exceed 10 characters")
    private String audioLanguage;

    private PlaybackQuality playbackQuality;

    private Boolean dataSaverEnabled;

    private Boolean notificationsEnabled;

    private Boolean emailNotifications;

    private Boolean pushNotifications;
}
