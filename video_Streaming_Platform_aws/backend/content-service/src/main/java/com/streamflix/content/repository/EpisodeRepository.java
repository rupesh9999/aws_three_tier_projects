package com.streamflix.content.repository;

import com.streamflix.content.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, UUID> {
    
    List<Episode> findBySeasonIdOrderByEpisodeNumberAsc(UUID seasonId);
    
    List<Episode> findByContentIdOrderBySeasonSeasonNumberAscEpisodeNumberAsc(UUID contentId);
    
    Optional<Episode> findBySeasonIdAndEpisodeNumber(UUID seasonId, Integer episodeNumber);
    
    @Query("SELECT e FROM Episode e WHERE e.content.id = :contentId AND e.season.seasonNumber = :seasonNumber ORDER BY e.episodeNumber ASC")
    List<Episode> findByContentIdAndSeasonNumber(@Param("contentId") UUID contentId, @Param("seasonNumber") Integer seasonNumber);
    
    @Query("SELECT e FROM Episode e WHERE e.content.id = :contentId AND e.season.seasonNumber = :seasonNumber AND e.episodeNumber = :episodeNumber")
    Optional<Episode> findByContentIdAndSeasonAndEpisode(@Param("contentId") UUID contentId, 
                                                          @Param("seasonNumber") Integer seasonNumber,
                                                          @Param("episodeNumber") Integer episodeNumber);
    
    @Modifying
    @Query("UPDATE Episode e SET e.viewCount = e.viewCount + 1 WHERE e.id = :id")
    void incrementViewCount(@Param("id") UUID id);
    
    @Query("SELECT COUNT(e) FROM Episode e WHERE e.season.id = :seasonId")
    int countBySeasonId(@Param("seasonId") UUID seasonId);
    
    @Query("SELECT COUNT(e) FROM Episode e WHERE e.content.id = :contentId")
    int countByContentId(@Param("contentId") UUID contentId);
}
