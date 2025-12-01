package com.streamflix.profile.repository;

import com.streamflix.profile.entity.ProfileContentRating;
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
public interface ProfileContentRatingRepository extends JpaRepository<ProfileContentRating, UUID> {

    Optional<ProfileContentRating> findByProfileIdAndContentId(UUID profileId, UUID contentId);

    Page<ProfileContentRating> findByProfileIdOrderByUpdatedAtDesc(UUID profileId, Pageable pageable);

    @Query("SELECT r FROM ProfileContentRating r WHERE r.profile.id = :profileId AND r.thumbsUp = true ORDER BY r.updatedAt DESC")
    Page<ProfileContentRating> findLikedByProfileId(@Param("profileId") UUID profileId, Pageable pageable);

    @Query("SELECT r FROM ProfileContentRating r WHERE r.profile.id = :profileId AND r.rating IS NOT NULL ORDER BY r.rating DESC")
    Page<ProfileContentRating> findRatedByProfileId(@Param("profileId") UUID profileId, Pageable pageable);

    @Query("SELECT r.contentId FROM ProfileContentRating r WHERE r.profile.id = :profileId AND r.thumbsUp = true")
    List<UUID> findLikedContentIdsByProfileId(@Param("profileId") UUID profileId);

    boolean existsByProfileIdAndContentId(UUID profileId, UUID contentId);

    void deleteByProfileIdAndContentId(UUID profileId, UUID contentId);
}
