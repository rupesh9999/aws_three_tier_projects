package com.streamflix.playback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaybackInitRequest(
    @NotNull(message = "Content ID is required")
    Long contentId,
    
    Long episodeId,
    
    @NotBlank(message = "Device ID is required")
    String deviceId,
    
    @NotBlank(message = "Device type is required")
    String deviceType,
    
    String quality,
    
    Integer durationSeconds,
    
    String ipAddress,
    
    String userAgent
) {}
