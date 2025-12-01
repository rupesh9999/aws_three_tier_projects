package com.streamflix.content.repository;

import com.streamflix.content.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {
    
    List<Season> findByContentIdOrderBySeasonNumberAsc(UUID contentId);
    
    Optional<Season> findByContentIdAndSeasonNumber(UUID contentId, Integer seasonNumber);
    
    @Query("SELECT s FROM Season s LEFT JOIN FETCH s.episodes WHERE s.id = :id")
    Optional<Season> findByIdWithEpisodes(@Param("id") UUID id);
    
    @Query("SELECT s FROM Season s LEFT JOIN FETCH s.episodes WHERE s.content.id = :contentId ORDER BY s.seasonNumber ASC")
    List<Season> findByContentIdWithEpisodes(@Param("contentId") UUID contentId);
    
    @Query("SELECT COUNT(s) FROM Season s WHERE s.content.id = :contentId")
    int countByContentId(@Param("contentId") UUID contentId);
}
