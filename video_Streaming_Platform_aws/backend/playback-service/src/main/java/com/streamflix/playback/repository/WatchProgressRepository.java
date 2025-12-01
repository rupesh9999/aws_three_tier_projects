package com.streamflix.playback.repository;

import com.streamflix.playback.entity.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {

    Optional<WatchProgress> findByUserIdAndContentIdAndEpisodeId(
            Long userId, Long contentId, Long episodeId
    );

    @Query("SELECT w FROM WatchProgress w WHERE w.userId = :userId AND w.contentId = :contentId " +
           "AND w.episodeId IS NULL")
    Optional<WatchProgress> findByUserIdAndContentId(
            @Param("userId") Long userId, 
            @Param("contentId") Long contentId
    );

    List<WatchProgress> findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(Long userId);

    List<WatchProgress> findByUserIdOrderByLastWatchedAtDesc(Long userId);

    @Query("SELECT w FROM WatchProgress w WHERE w.userId = :userId AND w.contentId = :contentId " +
           "ORDER BY w.episodeId ASC")
    List<WatchProgress> findByUserIdAndContentIdOrderByEpisode(
            @Param("userId") Long userId, 
            @Param("contentId") Long contentId
    );

    @Query("SELECT COUNT(w) FROM WatchProgress w WHERE w.userId = :userId AND w.completed = true")
    long countCompletedByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndContentId(Long userId, Long contentId);
}
