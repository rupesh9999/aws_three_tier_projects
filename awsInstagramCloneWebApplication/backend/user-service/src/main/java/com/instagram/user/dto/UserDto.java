package com.instagram.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserProfileResponse {
        private Long userId;
        private String username;
        private String fullName;
        private String bio;
        private String profilePictureUrl;
        private long followersCount;
        private long followingCount;
        private long postsCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProfileRequest {
        private String fullName;
        private String bio;
        private String profilePictureUrl;
        private String website;
    }
}
