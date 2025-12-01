package com.streamflix.profile.service;

import com.streamflix.common.exception.BadRequestException;
import com.streamflix.common.exception.ConflictException;
import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.profile.dto.*;
import com.streamflix.profile.entity.ProfilePreferences;
import com.streamflix.profile.entity.UserProfile;
import com.streamflix.profile.repository.ProfilePreferencesRepository;
import com.streamflix.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final ProfilePreferencesRepository preferencesRepository;

    @Value("${profile.max-profiles-per-account:5}")
    private int maxProfilesPerAccount;

    @Value("${profile.avatar.default-avatar-url}")
    private String defaultAvatarUrl;

    @Transactional(readOnly = true)
    @Cacheable(value = "profiles", key = "#userId")
    public List<ProfileResponse> getProfilesByUserId(UUID userId) {
        log.debug("Getting profiles for user: {}", userId);
        return profileRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID profileId, UUID userId) {
        log.debug("Getting profile: {} for user: {}", profileId, userId);
        UserProfile profile = profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getDefaultProfile(UUID userId) {
        log.debug("Getting default profile for user: {}", userId);
        return profileRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    List<UserProfile> profiles = profileRepository.findByUserIdOrderByCreatedAtAsc(userId);
                    if (profiles.isEmpty()) {
                        throw new ResourceNotFoundException("Profile", "default for user " + userId);
                    }
                    return mapToResponse(profiles.get(0));
                });
    }

    @CacheEvict(value = "profiles", key = "#userId")
    public ProfileResponse createProfile(UUID userId, CreateProfileRequest request) {
        log.info("Creating new profile for user: {}", userId);

        // Check profile limit
        long existingCount = profileRepository.countByUserId(userId);
        if (existingCount >= maxProfilesPerAccount) {
            throw new BadRequestException(
                    "Maximum number of profiles (" + maxProfilesPerAccount + ") reached for this account");
        }

        // Check for duplicate name
        if (profileRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new ConflictException("Profile", "name", request.getName());
        }

        // Create profile
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name(request.getName())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : defaultAvatarUrl)
                .isKidsProfile(request.getIsKidsProfile())
                .maturityLevel(request.getMaturityLevel())
                .language(request.getLanguage())
                .autoplayEnabled(request.getAutoplayEnabled())
                .autoplayPreviews(request.getAutoplayPreviews())
                .isDefault(request.getIsDefault())
                .build();

        // Handle default profile
        if (Boolean.TRUE.equals(request.getIsDefault()) || existingCount == 0) {
            profile.setIsDefault(true);
            if (existingCount > 0) {
                profileRepository.clearDefaultForUser(userId, profile.getId());
            }
        }

        profile = profileRepository.save(profile);

        // Create default preferences
        ProfilePreferences preferences = ProfilePreferences.builder()
                .profile(profile)
                .audioLanguage(request.getLanguage())
                .build();
        preferencesRepository.save(preferences);

        log.info("Created profile: {} for user: {}", profile.getId(), userId);
        return mapToResponse(profile);
    }

    @CacheEvict(value = "profiles", key = "#userId")
    public ProfileResponse updateProfile(UUID profileId, UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile: {} for user: {}", profileId, userId);

        UserProfile profile = profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));

        // Check for duplicate name if name is being changed
        if (request.getName() != null && !request.getName().equals(profile.getName())) {
            if (profileRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new ConflictException("Profile", "name", request.getName());
            }
            profile.setName(request.getName());
        }

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getIsKidsProfile() != null) {
            profile.setIsKidsProfile(request.getIsKidsProfile());
        }
        if (request.getMaturityLevel() != null) {
            profile.setMaturityLevel(request.getMaturityLevel());
        }
        if (request.getLanguage() != null) {
            profile.setLanguage(request.getLanguage());
        }
        if (request.getAutoplayEnabled() != null) {
            profile.setAutoplayEnabled(request.getAutoplayEnabled());
        }
        if (request.getAutoplayPreviews() != null) {
            profile.setAutoplayPreviews(request.getAutoplayPreviews());
        }

        // Handle default profile change
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(profile.getIsDefault())) {
            profileRepository.clearDefaultForUser(userId, profileId);
            profile.setIsDefault(true);
        }

        profile = profileRepository.save(profile);
        log.info("Updated profile: {} for user: {}", profileId, userId);
        return mapToResponse(profile);
    }

    @CacheEvict(value = "profiles", key = "#userId")
    public void deleteProfile(UUID profileId, UUID userId) {
        log.info("Deleting profile: {} for user: {}", profileId, userId);

        UserProfile profile = profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));

        // Don't allow deleting the last profile
        if (profileRepository.countByUserId(userId) <= 1) {
            throw new BadRequestException("Cannot delete the last profile");
        }

        // If deleting default profile, make another one default
        if (Boolean.TRUE.equals(profile.getIsDefault())) {
            List<UserProfile> otherProfiles = profileRepository.findByUserIdOrderByCreatedAtAsc(userId);
            otherProfiles.stream()
                    .filter(p -> !p.getId().equals(profileId))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setIsDefault(true);
                        profileRepository.save(p);
                    });
        }

        profileRepository.delete(profile);
        log.info("Deleted profile: {} for user: {}", profileId, userId);
    }

    public ProfilePreferencesResponse updatePreferences(UUID profileId, UUID userId, UpdatePreferencesRequest request) {
        log.info("Updating preferences for profile: {}", profileId);

        // Verify profile belongs to user
        profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));

        ProfilePreferences preferences = preferencesRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfilePreferences", profileId.toString()));

        if (request.getSubtitleLanguage() != null) {
            preferences.setSubtitleLanguage(request.getSubtitleLanguage());
        }
        if (request.getSubtitleEnabled() != null) {
            preferences.setSubtitleEnabled(request.getSubtitleEnabled());
        }
        if (request.getAudioLanguage() != null) {
            preferences.setAudioLanguage(request.getAudioLanguage());
        }
        if (request.getPlaybackQuality() != null) {
            preferences.setPlaybackQuality(request.getPlaybackQuality());
        }
        if (request.getDataSaverEnabled() != null) {
            preferences.setDataSaverEnabled(request.getDataSaverEnabled());
        }
        if (request.getNotificationsEnabled() != null) {
            preferences.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getEmailNotifications() != null) {
            preferences.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            preferences.setPushNotifications(request.getPushNotifications());
        }

        preferences = preferencesRepository.save(preferences);
        log.info("Updated preferences for profile: {}", profileId);
        return mapPreferencesToResponse(preferences);
    }

    private ProfileResponse mapToResponse(UserProfile profile) {
        ProfilePreferencesResponse prefsResponse = null;
        if (profile.getPreferences() != null) {
            prefsResponse = mapPreferencesToResponse(profile.getPreferences());
        } else {
            preferencesRepository.findByProfileId(profile.getId())
                    .ifPresent(prefs -> mapPreferencesToResponse(prefs));
        }

        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .name(profile.getName())
                .avatarUrl(profile.getAvatarUrl())
                .isKidsProfile(profile.getIsKidsProfile())
                .maturityLevel(profile.getMaturityLevel())
                .language(profile.getLanguage())
                .autoplayEnabled(profile.getAutoplayEnabled())
                .autoplayPreviews(profile.getAutoplayPreviews())
                .isDefault(profile.getIsDefault())
                .preferences(prefsResponse)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private ProfilePreferencesResponse mapPreferencesToResponse(ProfilePreferences prefs) {
        return ProfilePreferencesResponse.builder()
                .id(prefs.getId())
                .subtitleLanguage(prefs.getSubtitleLanguage())
                .subtitleEnabled(prefs.getSubtitleEnabled())
                .audioLanguage(prefs.getAudioLanguage())
                .playbackQuality(prefs.getPlaybackQuality())
                .dataSaverEnabled(prefs.getDataSaverEnabled())
                .notificationsEnabled(prefs.getNotificationsEnabled())
                .emailNotifications(prefs.getEmailNotifications())
                .pushNotifications(prefs.getPushNotifications())
                .build();
    }
}
