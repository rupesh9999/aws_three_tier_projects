package com.streamflix.profile.repository;

import com.streamflix.profile.entity.ProfileWatchlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileWatchlistRepository extends JpaRepository<ProfileWatchlistItem, UUID> {

    Page<ProfileWatchlistItem> findByProfileIdOrderByPositionAsc(UUID profileId, Pageable pageable);

    List<ProfileWatchlistItem> findByProfileIdOrderByPositionAsc(UUID profileId);

    Optional<ProfileWatchlistItem> findByProfileIdAndContentId(UUID profileId, UUID contentId);

    boolean existsByProfileIdAndContentId(UUID profileId, UUID contentId);

    void deleteByProfileIdAndContentId(UUID profileId, UUID contentId);

    @Query("SELECT COUNT(w) FROM ProfileWatchlistItem w WHERE w.profile.id = :profileId")
    long countByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT MAX(w.position) FROM ProfileWatchlistItem w WHERE w.profile.id = :profileId")
    Optional<Integer> findMaxPositionByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT w.contentId FROM ProfileWatchlistItem w WHERE w.profile.id = :profileId")
    List<UUID> findContentIdsByProfileId(@Param("profileId") UUID profileId);
}
