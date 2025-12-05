package com.instagram.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AiDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CaptionRequest {
        private String imageUrl;
        private String context; // Optional context for the caption
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerationResponse {
        private String content;
    }
}
