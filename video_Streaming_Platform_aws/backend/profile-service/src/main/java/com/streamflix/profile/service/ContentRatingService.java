package com.streamflix.profile.service;

import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.profile.dto.ContentRatingRequest;
import com.streamflix.profile.dto.ContentRatingResponse;
import com.streamflix.profile.entity.ProfileContentRating;
import com.streamflix.profile.entity.UserProfile;
import com.streamflix.profile.repository.ProfileContentRatingRepository;
import com.streamflix.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContentRatingService {

    private final ProfileContentRatingRepository ratingRepository;
    private final UserProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Page<ContentRatingResponse> getRatings(UUID profileId, UUID userId, Pageable pageable) {
        log.debug("Getting ratings for profile: {}", profileId);
        verifyProfileOwnership(profileId, userId);
        
        return ratingRepository.findByProfileIdOrderByUpdatedAtDesc(profileId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ContentRatingResponse getRating(UUID profileId, UUID userId, UUID contentId) {
        verifyProfileOwnership(profileId, userId);
        
        return ratingRepository.findByProfileIdAndContentId(profileId, contentId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<ContentRatingResponse> getLikedContent(UUID profileId, UUID userId, Pageable pageable) {
        verifyProfileOwnership(profileId, userId);
        return ratingRepository.findLikedByProfileId(profileId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<UUID> getLikedContentIds(UUID profileId, UUID userId) {
        verifyProfileOwnership(profileId, userId);
        return ratingRepository.findLikedContentIdsByProfileId(profileId);
    }

    public ContentRatingResponse rateContent(UUID profileId, UUID userId, ContentRatingRequest request) {
        log.info("Rating content {} for profile: {}", request.getContentId(), profileId);
        
        UserProfile profile = verifyProfileOwnership(profileId, userId);

        ProfileContentRating rating = ratingRepository
                .findByProfileIdAndContentId(profileId, request.getContentId())
                .orElse(ProfileContentRating.builder()
                        .profile(profile)
                        .contentId(request.getContentId())
                        .build());

        if (request.getRating() != null) {
            rating.setRating(request.getRating());
        }
        if (request.getThumbsUp() != null) {
            rating.setThumbsUp(request.getThumbsUp());
        }

        rating = ratingRepository.save(rating);
        log.info("Rated content {} for profile: {}", request.getContentId(), profileId);
        return mapToResponse(rating);
    }

    public void removeRating(UUID profileId, UUID userId, UUID contentId) {
        log.info("Removing rating for content {} from profile: {}", contentId, profileId);
        
        verifyProfileOwnership(profileId, userId);

        if (!ratingRepository.existsByProfileIdAndContentId(profileId, contentId)) {
            throw new ResourceNotFoundException("ContentRating", contentId.toString());
        }

        ratingRepository.deleteByProfileIdAndContentId(profileId, contentId);
        log.info("Removed rating for content {} from profile: {}", contentId, profileId);
    }

    private UserProfile verifyProfileOwnership(UUID profileId, UUID userId) {
        return profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));
    }

    private ContentRatingResponse mapToResponse(ProfileContentRating rating) {
        return ContentRatingResponse.builder()
                .id(rating.getId())
                .contentId(rating.getContentId())
                .rating(rating.getRating())
                .thumbsUp(rating.getThumbsUp())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
