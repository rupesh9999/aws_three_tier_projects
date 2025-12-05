package com.instagram.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

public class FeedDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostSummary implements Serializable {
        private Long id;
        private Long userId;
        private String username;
        private String userProfilePicture;
        private String imageUrl;
        private String caption;
        private long likesCount;
        private long commentsCount;
        private String createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedResponse {
        private List<PostSummary> posts;
    }
}
