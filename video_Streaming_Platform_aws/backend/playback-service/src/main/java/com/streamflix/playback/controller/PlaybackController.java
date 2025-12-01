package com.streamflix.playback.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.playback.dto.*;
import com.streamflix.playback.service.PlaybackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playback")
@RequiredArgsConstructor
@Tag(name = "Playback", description = "Video playback and streaming APIs")
@SecurityRequirement(name = "bearerAuth")
public class PlaybackController {

    private final PlaybackService playbackService;

    @PostMapping("/start")
    @Operation(summary = "Initialize playback", description = "Start a new playback session and get streaming URL")
    public ResponseEntity<ApiResponse<PlaybackSessionResponse>> startPlayback(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("X-Subscription-Tier") String subscriptionTier,
            @Valid @RequestBody PlaybackInitRequest request,
            HttpServletRequest httpRequest) {
        
        // Enrich request with IP and user agent
        PlaybackInitRequest enrichedRequest = new PlaybackInitRequest(
                request.contentId(),
                request.episodeId(),
                request.deviceId(),
                request.deviceType(),
                request.quality(),
                request.durationSeconds(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        PlaybackSessionResponse session = playbackService.initializePlayback(userId, subscriptionTier, enrichedRequest);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/sessions/{sessionId}/progress")
    @Operation(summary = "Update progress", description = "Update playback progress and send heartbeat")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable String sessionId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        
        playbackService.updateProgress(userId, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Progress updated"));
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End playback", description = "End the playback session")
    public ResponseEntity<ApiResponse<Void>> endPlayback(
            @AuthenticationPrincipal Long userId,
            @PathVariable String sessionId) {
        
        playbackService.endPlayback(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Playback ended"));
    }

    @GetMapping("/continue-watching")
    @Operation(summary = "Get continue watching", description = "Get list of content to continue watching")
    public ResponseEntity<ApiResponse<List<WatchProgressResponse>>> getContinueWatching(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<WatchProgressResponse> progress = playbackService.getContinueWatching(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get watch progress", description = "Get watch progress for specific content")
    public ResponseEntity<ApiResponse<WatchProgressResponse>> getWatchProgress(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long contentId,
            @RequestParam(required = false) Long episodeId) {
        
        WatchProgressResponse progress = playbackService.getWatchProgress(userId, contentId, episodeId);
        if (progress == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No progress found"));
        }
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @PostMapping("/progress/complete")
    @Operation(summary = "Mark as completed", description = "Mark content as completed")
    public ResponseEntity<ApiResponse<Void>> markAsCompleted(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long contentId,
            @RequestParam(required = false) Long episodeId) {
        
        playbackService.markAsCompleted(userId, contentId, episodeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as completed"));
    }

    @DeleteMapping("/progress")
    @Operation(summary = "Reset progress", description = "Reset watch progress for content")
    public ResponseEntity<ApiResponse<Void>> resetProgress(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long contentId,
            @RequestParam(required = false) Long episodeId) {
        
        playbackService.resetProgress(userId, contentId, episodeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Progress reset"));
    }
}
