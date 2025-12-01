package com.streamflix.playback.repository;

import com.streamflix.playback.entity.PlaybackSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlaybackSessionRepository extends JpaRepository<PlaybackSession, UUID> {

    List<PlaybackSession> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT COUNT(s) FROM PlaybackSession s WHERE s.userId = :userId AND s.isActive = true")
    int countActiveSessionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PlaybackSession s SET s.isActive = false, s.endedAt = CURRENT_TIMESTAMP " +
           "WHERE s.isActive = true AND s.lastHeartbeat < :threshold")
    int deactivateInactiveSessions(@Param("threshold") Instant threshold);

    @Query("SELECT s FROM PlaybackSession s WHERE s.userId = :userId AND s.contentId = :contentId " +
           "AND s.isActive = true ORDER BY s.startedAt DESC")
    List<PlaybackSession> findActiveSessionsForContent(
            @Param("userId") Long userId, 
            @Param("contentId") Long contentId
    );
}
