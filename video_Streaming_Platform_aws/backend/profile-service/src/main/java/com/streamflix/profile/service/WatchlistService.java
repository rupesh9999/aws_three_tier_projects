package com.streamflix.profile.service;

import com.streamflix.common.exception.ConflictException;
import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.profile.dto.WatchlistAddRequest;
import com.streamflix.profile.dto.WatchlistItemResponse;
import com.streamflix.profile.entity.ProfileWatchlistItem;
import com.streamflix.profile.entity.UserProfile;
import com.streamflix.profile.repository.ProfileWatchlistRepository;
import com.streamflix.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WatchlistService {

    private final ProfileWatchlistRepository watchlistRepository;
    private final UserProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Page<WatchlistItemResponse> getWatchlist(UUID profileId, UUID userId, Pageable pageable) {
        log.debug("Getting watchlist for profile: {}", profileId);
        verifyProfileOwnership(profileId, userId);
        
        return watchlistRepository.findByProfileIdOrderByPositionAsc(profileId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<UUID> getWatchlistContentIds(UUID profileId, UUID userId) {
        verifyProfileOwnership(profileId, userId);
        return watchlistRepository.findContentIdsByProfileId(profileId);
    }

    @Transactional(readOnly = true)
    public boolean isInWatchlist(UUID profileId, UUID userId, UUID contentId) {
        verifyProfileOwnership(profileId, userId);
        return watchlistRepository.existsByProfileIdAndContentId(profileId, contentId);
    }

    public WatchlistItemResponse addToWatchlist(UUID profileId, UUID userId, WatchlistAddRequest request) {
        log.info("Adding content {} to watchlist for profile: {}", request.getContentId(), profileId);
        
        UserProfile profile = verifyProfileOwnership(profileId, userId);

        // Check if already in watchlist
        if (watchlistRepository.existsByProfileIdAndContentId(profileId, request.getContentId())) {
            throw new ConflictException("WatchlistItem", "contentId", request.getContentId().toString());
        }

        // Calculate position
        int position = request.getPosition() != null 
                ? request.getPosition() 
                : watchlistRepository.findMaxPositionByProfileId(profileId).orElse(-1) + 1;

        ProfileWatchlistItem item = ProfileWatchlistItem.builder()
                .profile(profile)
                .contentId(request.getContentId())
                .position(position)
                .build();

        item = watchlistRepository.save(item);
        log.info("Added content {} to watchlist for profile: {}", request.getContentId(), profileId);
        return mapToResponse(item);
    }

    public void removeFromWatchlist(UUID profileId, UUID userId, UUID contentId) {
        log.info("Removing content {} from watchlist for profile: {}", contentId, profileId);
        
        verifyProfileOwnership(profileId, userId);

        if (!watchlistRepository.existsByProfileIdAndContentId(profileId, contentId)) {
            throw new ResourceNotFoundException("WatchlistItem", contentId.toString());
        }

        watchlistRepository.deleteByProfileIdAndContentId(profileId, contentId);
        log.info("Removed content {} from watchlist for profile: {}", contentId, profileId);
    }

    public void reorderWatchlist(UUID profileId, UUID userId, List<UUID> contentIds) {
        log.info("Reordering watchlist for profile: {}", profileId);
        
        verifyProfileOwnership(profileId, userId);

        List<ProfileWatchlistItem> items = watchlistRepository.findByProfileIdOrderByPositionAsc(profileId);
        
        for (int i = 0; i < contentIds.size(); i++) {
            UUID contentId = contentIds.get(i);
            final int position = i;
            items.stream()
                    .filter(item -> item.getContentId().equals(contentId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setPosition(position);
                        watchlistRepository.save(item);
                    });
        }
        
        log.info("Reordered watchlist for profile: {}", profileId);
    }

    private UserProfile verifyProfileOwnership(UUID profileId, UUID userId) {
        return profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));
    }

    private WatchlistItemResponse mapToResponse(ProfileWatchlistItem item) {
        return WatchlistItemResponse.builder()
                .id(item.getId())
                .contentId(item.getContentId())
                .position(item.getPosition())
                .addedAt(item.getAddedAt())
                .build();
    }
}
