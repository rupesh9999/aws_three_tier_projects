package com.streamflix.notification.repository;

import com.streamflix.notification.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {
    
    List<PushToken> findByUserIdAndIsActiveTrue(UUID userId);
    
    Optional<PushToken> findByToken(String token);
    
    Optional<PushToken> findByUserIdAndDeviceId(UUID userId, String deviceId);
    
    @Modifying
    @Query("UPDATE PushToken p SET p.isActive = false WHERE p.userId = :userId AND p.deviceId = :deviceId")
    int deactivateToken(@Param("userId") UUID userId, @Param("deviceId") String deviceId);
    
    @Modifying
    @Query("UPDATE PushToken p SET p.isActive = false WHERE p.userId = :userId")
    int deactivateAllUserTokens(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM PushToken p WHERE p.isActive = false AND p.updatedAt < :before")
    int deleteInactiveTokens(@Param("before") Instant before);
    
    long countByUserIdAndIsActiveTrue(UUID userId);
}
