package com.instagram.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatePostRequest {
        private Long userId;
        private String caption;
        // In a real app, this would be a multipart file or a presigned URL flow
        // For simplicity here, we might accept a URL or handle file upload in controller
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostResponse {
        private Long id;
        private Long userId;
        private String imageUrl;
        private String caption;
        private long likesCount;
        private long commentsCount;
        private String createdAt;
    }
}
