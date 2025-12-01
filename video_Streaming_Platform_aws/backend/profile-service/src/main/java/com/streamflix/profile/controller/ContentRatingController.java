package com.streamflix.profile.controller;

import com.streamflix.profile.dto.ContentRatingRequest;
import com.streamflix.profile.dto.ContentRatingResponse;
import com.streamflix.profile.service.ContentRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/{profileId}/ratings")
@RequiredArgsConstructor
@Slf4j
public class ContentRatingController {

    private final ContentRatingService ratingService;

    @GetMapping
    public ResponseEntity<Page<ContentRatingResponse>> getRatings(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Getting ratings for profile: {}", profileId);
        return ResponseEntity.ok(ratingService.getRatings(profileId, userId, pageable));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentRatingResponse> getRating(
            @PathVariable UUID profileId,
            @PathVariable UUID contentId,
            @RequestHeader("X-User-Id") UUID userId) {
        ContentRatingResponse rating = ratingService.getRating(profileId, userId, contentId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/liked")
    public ResponseEntity<Page<ContentRatingResponse>> getLikedContent(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ratingService.getLikedContent(profileId, userId, pageable));
    }

    @GetMapping("/liked/ids")
    public ResponseEntity<List<UUID>> getLikedContentIds(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ratingService.getLikedContentIds(profileId, userId));
    }

    @PostMapping
    public ResponseEntity<ContentRatingResponse> rateContent(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ContentRatingRequest request) {
        log.info("Rating content for profile: {}", profileId);
        return ResponseEntity.ok(ratingService.rateContent(profileId, userId, request));
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> removeRating(
            @PathVariable UUID profileId,
            @PathVariable UUID contentId,
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Removing rating for profile: {}", profileId);
        ratingService.removeRating(profileId, userId, contentId);
        return ResponseEntity.noContent().build();
    }
}
