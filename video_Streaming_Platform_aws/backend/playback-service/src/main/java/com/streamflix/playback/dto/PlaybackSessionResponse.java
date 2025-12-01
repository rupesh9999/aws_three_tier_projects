package com.streamflix.playback.dto;

import java.util.List;

public record PlaybackSessionResponse(
    String sessionId,
    String streamUrl,
    int startPosition,
    int duration,
    String quality,
    List<String> availableQualities,
    int sessionTimeoutSeconds
) {}
