package com.streamflix.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRatingResponse {
    private UUID id;
    private UUID contentId;
    private BigDecimal rating;
    private Boolean thumbsUp;
    private Instant createdAt;
    private Instant updatedAt;
}
