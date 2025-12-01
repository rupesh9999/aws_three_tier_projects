package com.streamflix.playback.service;

import com.streamflix.common.exception.BusinessException;
import com.streamflix.common.exception.ForbiddenException;
import com.streamflix.playback.dto.*;
import com.streamflix.playback.entity.PlaybackSession;
import com.streamflix.playback.entity.WatchProgress;
import com.streamflix.playback.repository.PlaybackSessionRepository;
import com.streamflix.playback.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaybackService {

    private final PlaybackSessionRepository sessionRepository;
    private final WatchProgressRepository progressRepository;
    private final CloudFrontSigningService signingService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${playback.max-concurrent-streams.basic:1}")
    private int maxStreamsBasic;

    @Value("${playback.max-concurrent-streams.standard:2}")
    private int maxStreamsStandard;

    @Value("${playback.max-concurrent-streams.premium:4}")
    private int maxStreamsPremium;

    @Value("${playback.session.timeout:30}")
    private int sessionTimeoutMinutes;

    private static final String ACTIVE_SESSIONS_KEY = "active_sessions:";
    private static final String STREAM_COUNT_KEY = "stream_count:";

    @Transactional
    public PlaybackSessionResponse initializePlayback(Long userId, String subscriptionTier, PlaybackInitRequest request) {
        log.info("Initializing playback for user {} content {}", userId, request.contentId());

        // Check concurrent stream limits
        int maxStreams = getMaxStreams(subscriptionTier);
        int activeStreams = getActiveStreamCount(userId);
        
        if (activeStreams >= maxStreams) {
            log.warn("User {} has reached max concurrent streams: {}", userId, maxStreams);
            throw new ForbiddenException(
                String.format("Maximum concurrent streams (%d) reached. Please stop another stream to continue.", maxStreams)
            );
        }

        // Get or create watch progress
        WatchProgress progress = progressRepository
                .findByUserIdAndContentIdAndEpisodeId(userId, request.contentId(), request.episodeId())
                .orElse(null);

        int startPosition = 0;
        if (progress != null && !progress.getCompleted()) {
            startPosition = progress.getPositionSeconds();
        }

        // Generate signed streaming URL
        String streamPath = buildStreamPath(request.contentId(), request.episodeId(), request.quality());
        String signedUrl = signingService.generateSignedUrl(streamPath);

        // Create playback session
        PlaybackSession session = PlaybackSession.builder()
                .userId(userId)
                .contentId(request.contentId())
                .episodeId(request.episodeId())
                .deviceId(request.deviceId())
                .deviceType(PlaybackSession.DeviceType.valueOf(request.deviceType()))
                .quality(request.quality() != null ? request.quality() : "AUTO")
                .startedAt(Instant.now())
                .lastHeartbeat(Instant.now())
                .positionSeconds(startPosition)
                .durationSeconds(request.durationSeconds())
                .isActive(true)
                .ipAddress(request.ipAddress())
                .userAgent(request.userAgent())
                .streamUrl(signedUrl)
                .build();

        session = sessionRepository.save(session);

        // Update Redis for active session tracking
        trackActiveSession(userId, session.getId().toString());

        log.info("Playback session created: {}", session.getId());

        return new PlaybackSessionResponse(
                session.getId().toString(),
                signedUrl,
                startPosition,
                request.durationSeconds(),
                session.getQuality(),
                getAvailableQualities(subscriptionTier),
                sessionTimeoutMinutes * 60
        );
    }

    @Transactional
    public void updateProgress(Long userId, String sessionId, ProgressUpdateRequest request) {
        UUID sessionUuid = UUID.fromString(sessionId);
        
        PlaybackSession session = sessionRepository.findById(sessionUuid)
                .orElseThrow(() -> new BusinessException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException("Session does not belong to user");
        }

        // Update session
        session.setPositionSeconds(request.positionSeconds());
        session.setLastHeartbeat(Instant.now());
        if (request.quality() != null) {
            session.setQuality(request.quality());
        }
        sessionRepository.save(session);

        // Update watch progress
        WatchProgress progress = progressRepository
                .findByUserIdAndContentIdAndEpisodeId(userId, session.getContentId(), session.getEpisodeId())
                .orElseGet(() -> WatchProgress.builder()
                        .userId(userId)
                        .contentId(session.getContentId())
                        .episodeId(session.getEpisodeId())
                        .durationSeconds(session.getDurationSeconds())
                        .build());

        progress.setPositionSeconds(request.positionSeconds());
        progress.setDurationSeconds(session.getDurationSeconds());
        progress.setLastWatchedAt(Instant.now());
        progressRepository.save(progress);

        // Refresh Redis TTL
        refreshActiveSession(userId, sessionId);

        log.debug("Updated progress for session {}: {}s", sessionId, request.positionSeconds());
    }

    @Transactional
    public void endPlayback(Long userId, String sessionId) {
        UUID sessionUuid = UUID.fromString(sessionId);
        
        PlaybackSession session = sessionRepository.findById(sessionUuid)
                .orElseThrow(() -> new BusinessException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException("Session does not belong to user");
        }

        session.setIsActive(false);
        session.setEndedAt(Instant.now());
        sessionRepository.save(session);

        // Remove from active sessions
        removeActiveSession(userId, sessionId);

        log.info("Playback session ended: {}", sessionId);
    }

    @Transactional(readOnly = true)
    public List<WatchProgressResponse> getContinueWatching(Long userId, int limit) {
        return progressRepository.findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(userId)
                .stream()
                .limit(limit)
                .map(this::toWatchProgressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WatchProgressResponse getWatchProgress(Long userId, Long contentId, Long episodeId) {
        return progressRepository.findByUserIdAndContentIdAndEpisodeId(userId, contentId, episodeId)
                .map(this::toWatchProgressResponse)
                .orElse(null);
    }

    @Transactional
    public void markAsCompleted(Long userId, Long contentId, Long episodeId) {
        WatchProgress progress = progressRepository
                .findByUserIdAndContentIdAndEpisodeId(userId, contentId, episodeId)
                .orElseThrow(() -> new BusinessException("Watch progress not found"));

        progress.setCompleted(true);
        progress.setProgressPercentage(BigDecimal.valueOf(100));
        progressRepository.save(progress);
    }

    @Transactional
    public void resetProgress(Long userId, Long contentId, Long episodeId) {
        progressRepository.findByUserIdAndContentIdAndEpisodeId(userId, contentId, episodeId)
                .ifPresent(progressRepository::delete);
    }

    // Scheduled task to clean up inactive sessions
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void cleanupInactiveSessions() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(sessionTimeoutMinutes));
        int cleaned = sessionRepository.deactivateInactiveSessions(threshold);
        if (cleaned > 0) {
            log.info("Cleaned up {} inactive playback sessions", cleaned);
        }
    }

    private int getMaxStreams(String subscriptionTier) {
        return switch (subscriptionTier.toUpperCase()) {
            case "PREMIUM" -> maxStreamsPremium;
            case "STANDARD" -> maxStreamsStandard;
            default -> maxStreamsBasic;
        };
    }

    private int getActiveStreamCount(Long userId) {
        String countKey = STREAM_COUNT_KEY + userId;
        Long count = redisTemplate.opsForValue().increment(countKey, 0);
        return count != null ? count.intValue() : 0;
    }

    private void trackActiveSession(Long userId, String sessionId) {
        String sessionsKey = ACTIVE_SESSIONS_KEY + userId;
        String countKey = STREAM_COUNT_KEY + userId;
        
        redisTemplate.opsForSet().add(sessionsKey, sessionId);
        redisTemplate.expire(sessionsKey, sessionTimeoutMinutes + 5, TimeUnit.MINUTES);
        
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, sessionTimeoutMinutes + 5, TimeUnit.MINUTES);
    }

    private void refreshActiveSession(Long userId, String sessionId) {
        String sessionsKey = ACTIVE_SESSIONS_KEY + userId;
        redisTemplate.expire(sessionsKey, sessionTimeoutMinutes + 5, TimeUnit.MINUTES);
    }

    private void removeActiveSession(Long userId, String sessionId) {
        String sessionsKey = ACTIVE_SESSIONS_KEY + userId;
        String countKey = STREAM_COUNT_KEY + userId;
        
        redisTemplate.opsForSet().remove(sessionsKey, sessionId);
        redisTemplate.opsForValue().decrement(countKey);
    }

    private String buildStreamPath(Long contentId, Long episodeId, String quality) {
        if (episodeId != null) {
            return String.format("/content/%d/episodes/%d/manifest.m3u8", contentId, episodeId);
        }
        return String.format("/content/%d/manifest.m3u8", contentId);
    }

    private List<String> getAvailableQualities(String subscriptionTier) {
        List<String> qualities = new ArrayList<>();
        qualities.add("AUTO");
        qualities.add("SD");
        qualities.add("HD");
        
        if ("STANDARD".equalsIgnoreCase(subscriptionTier) || "PREMIUM".equalsIgnoreCase(subscriptionTier)) {
            qualities.add("FHD");
        }
        if ("PREMIUM".equalsIgnoreCase(subscriptionTier)) {
            qualities.add("4K");
            qualities.add("4K_HDR");
        }
        return qualities;
    }

    private WatchProgressResponse toWatchProgressResponse(WatchProgress progress) {
        return new WatchProgressResponse(
                progress.getContentId(),
                progress.getEpisodeId(),
                progress.getPositionSeconds(),
                progress.getDurationSeconds(),
                progress.getProgressPercentage().doubleValue(),
                progress.getCompleted(),
                progress.getLastWatchedAt()
        );
    }
}
