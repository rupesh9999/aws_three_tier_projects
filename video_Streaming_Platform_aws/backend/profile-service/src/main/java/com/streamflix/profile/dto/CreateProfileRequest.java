package com.streamflix.profile.dto;

import com.streamflix.profile.entity.UserProfile.MaturityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfileRequest {

    @NotBlank(message = "Profile name is required")
    @Size(min = 1, max = 100, message = "Profile name must be between 1 and 100 characters")
    private String name;

    private String avatarUrl;

    @Builder.Default
    private Boolean isKidsProfile = false;

    @Builder.Default
    private MaturityLevel maturityLevel = MaturityLevel.ALL;

    @Builder.Default
    private String language = "en";

    @Builder.Default
    private Boolean autoplayEnabled = true;

    @Builder.Default
    private Boolean autoplayPreviews = true;

    @Builder.Default
    private Boolean isDefault = false;
}
