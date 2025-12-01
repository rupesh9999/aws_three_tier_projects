package com.streamflix.profile.controller;

import com.streamflix.profile.dto.*;
import com.streamflix.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getProfiles(
            @RequestHeader("X-User-Id") UUID userId) {
        log.debug("Getting profiles for user: {}", userId);
        return ResponseEntity.ok(profileService.getProfilesByUserId(userId));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId) {
        log.debug("Getting profile: {} for user: {}", profileId, userId);
        return ResponseEntity.ok(profileService.getProfile(profileId, userId));
    }

    @GetMapping("/default")
    public ResponseEntity<ProfileResponse> getDefaultProfile(
            @RequestHeader("X-User-Id") UUID userId) {
        log.debug("Getting default profile for user: {}", userId);
        return ResponseEntity.ok(profileService.getDefaultProfile(userId));
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateProfileRequest request) {
        log.info("Creating profile for user: {}", userId);
        ProfileResponse response = profileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating profile: {} for user: {}", profileId, userId);
        return ResponseEntity.ok(profileService.updateProfile(profileId, userId, request));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Deleting profile: {} for user: {}", profileId, userId);
        profileService.deleteProfile(profileId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{profileId}/preferences")
    public ResponseEntity<ProfilePreferencesResponse> updatePreferences(
            @PathVariable UUID profileId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        log.info("Updating preferences for profile: {}", profileId);
        return ResponseEntity.ok(profileService.updatePreferences(profileId, userId, request));
    }
}
