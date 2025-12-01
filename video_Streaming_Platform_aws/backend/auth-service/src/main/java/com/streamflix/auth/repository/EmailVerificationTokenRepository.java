package com.streamflix.auth.repository;

import com.streamflix.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    
    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.tokenHash = :tokenHash AND evt.used = false AND evt.expiresAt > :now")
    Optional<EmailVerificationToken> findValidToken(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);
    
    @Query("SELECT evt FROM EmailVerificationToken evt WHERE evt.user.id = :userId AND evt.used = false AND evt.expiresAt > :now ORDER BY evt.createdAt DESC")
    Optional<EmailVerificationToken> findLatestValidTokenByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE EmailVerificationToken evt SET evt.used = true, evt.usedAt = :usedAt WHERE evt.user.id = :userId AND evt.used = false")
    void invalidateAllUserTokens(@Param("userId") UUID userId, @Param("usedAt") LocalDateTime usedAt);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.expiresAt < :cutoff OR evt.used = true")
    int deleteExpiredOrUsedTokens(@Param("cutoff") LocalDateTime cutoff);
}
