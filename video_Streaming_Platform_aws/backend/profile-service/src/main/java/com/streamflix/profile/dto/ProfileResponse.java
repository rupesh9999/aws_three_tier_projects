package com.streamflix.profile.dto;

import com.streamflix.profile.entity.UserProfile.MaturityLevel;
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
public class ProfileResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String avatarUrl;
    private Boolean isKidsProfile;
    private MaturityLevel maturityLevel;
    private String language;
    private Boolean autoplayEnabled;
    private Boolean autoplayPreviews;
    private Boolean isDefault;
    private ProfilePreferencesResponse preferences;
    private Instant createdAt;
    private Instant updatedAt;
}
