package com.streamflix.playback.dto;

import java.time.Instant;

public record WatchProgressResponse(
    Long contentId,
    Long episodeId,
    Integer positionSeconds,
    Integer durationSeconds,
    Double progressPercentage,
    Boolean completed,
    Instant lastWatchedAt
) {}
