package com.streamflix.profile.dto;

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
public class WatchlistItemResponse {
    private UUID id;
    private UUID contentId;
    private Integer position;
    private Instant addedAt;
}
