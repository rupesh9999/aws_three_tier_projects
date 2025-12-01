package com.streamflix.profile.repository;

import com.streamflix.profile.entity.ProfilePreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilePreferencesRepository extends JpaRepository<ProfilePreferences, UUID> {

    Optional<ProfilePreferences> findByProfileId(UUID profileId);

    boolean existsByProfileId(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
