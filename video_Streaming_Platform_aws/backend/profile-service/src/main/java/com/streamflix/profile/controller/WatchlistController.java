package com.streamflix.profile.controller;

import com.streamflix.profile.dto.WatchlistAddRequest;
import com.streamflix.profile.dto.WatchlistItemResponse;
import com.streamflix.profile.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/{profileId}/watchlist")
@RequiredArgsConstructor
@Slf4j
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<Page<WatchlistItemResponse>> getWatchlist(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Getting watchlist for profile: {}", profileId);
        return ResponseEntity.ok(watchlistService.getWatchlist(profileId, userId, pageable));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> getWatchlistContentIds(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(watchlistService.getWatchlistContentIds(profileId, userId));
    }

    @GetMapping("/check/{contentId}")
    public ResponseEntity<Map<String, Boolean>> checkInWatchlist(
            @PathVariable UUID profileId,
            @PathVariable UUID contentId,
            @RequestHeader("X-User-Id") UUID userId) {
        boolean inWatchlist = watchlistService.isInWatchlist(profileId, userId, contentId);
        return ResponseEntity.ok(Map.of("inWatchlist", inWatchlist));
    }

    @PostMapping
    public ResponseEntity<WatchlistItemResponse> addToWatchlist(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody WatchlistAddRequest request) {
        log.info("Adding to watchlist for profile: {}", profileId);
        WatchlistItemResponse response = watchlistService.addToWatchlist(profileId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable UUID profileId,
            @PathVariable UUID contentId,
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Removing from watchlist for profile: {}", profileId);
        watchlistService.removeFromWatchlist(profileId, userId, contentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderWatchlist(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody List<UUID> contentIds) {
        log.info("Reordering watchlist for profile: {}", profileId);
        watchlistService.reorderWatchlist(profileId, userId, contentIds);
        return ResponseEntity.ok().build();
    }
}
