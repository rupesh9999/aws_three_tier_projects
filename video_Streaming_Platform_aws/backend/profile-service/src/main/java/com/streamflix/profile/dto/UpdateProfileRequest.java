package com.streamflix.profile.dto;

import com.streamflix.profile.entity.UserProfile.MaturityLevel;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "Profile name must be between 1 and 100 characters")
    private String name;

    private String avatarUrl;

    private Boolean isKidsProfile;

    private MaturityLevel maturityLevel;

    private String language;

    private Boolean autoplayEnabled;

    private Boolean autoplayPreviews;

    private Boolean isDefault;
}
