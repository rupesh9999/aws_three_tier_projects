package com.streamflix.playback.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProgressUpdateRequest(
    @NotNull(message = "Position is required")
    @PositiveOrZero(message = "Position must be positive")
    Integer positionSeconds,
    
    String quality,
    
    Integer bitrate,
    
    Double bufferHealth
) {}
