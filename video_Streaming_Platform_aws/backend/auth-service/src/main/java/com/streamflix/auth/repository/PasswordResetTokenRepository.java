package com.streamflix.auth.repository;

import com.streamflix.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.tokenHash = :tokenHash AND prt.used = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.used = false AND prt.expiresAt > :now ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findLatestValidTokenByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.usedAt = :usedAt WHERE prt.user.id = :userId AND prt.used = false")
    void invalidateAllUserTokens(@Param("userId") UUID userId, @Param("usedAt") LocalDateTime usedAt);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :cutoff OR prt.used = true")
    int deleteExpiredOrUsedTokens(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.createdAt > :since")
    long countRecentRequestsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
