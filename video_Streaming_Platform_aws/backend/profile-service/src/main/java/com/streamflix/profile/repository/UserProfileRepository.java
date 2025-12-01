package com.streamflix.profile.repository;

import com.streamflix.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    List<UserProfile> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<UserProfile> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserProfile> findByUserIdAndIsDefaultTrue(UUID userId);

    @Query("SELECT COUNT(p) FROM UserProfile p WHERE p.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserProfile p SET p.isDefault = false WHERE p.userId = :userId AND p.id != :profileId")
    void clearDefaultForUser(@Param("userId") UUID userId, @Param("profileId") UUID profileId);

    boolean existsByUserIdAndName(UUID userId, String name);

    @Query("SELECT p FROM UserProfile p LEFT JOIN FETCH p.preferences WHERE p.id = :id")
    Optional<UserProfile> findByIdWithPreferences(@Param("id") UUID id);
}
