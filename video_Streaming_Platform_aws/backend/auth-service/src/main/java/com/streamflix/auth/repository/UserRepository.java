package com.streamflix.auth.repository;

import com.streamflix.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") UUID userId, @Param("attempts") int attempts);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.lastLoginIp = :ip, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLoginSuccess(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime, @Param("ip") String ip);
    
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true, u.lockReason = :reason, u.lockedAt = :lockedAt, u.lockExpiresAt = :expiresAt WHERE u.id = :userId")
    void lockAccount(@Param("userId") UUID userId, @Param("reason") String reason, 
                     @Param("lockedAt") LocalDateTime lockedAt, @Param("expiresAt") LocalDateTime expiresAt);
    
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = false, u.lockReason = null, u.lockedAt = null, u.lockExpiresAt = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void verifyEmail(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash, u.passwordChangedAt = :changedAt WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash, 
                       @Param("changedAt") LocalDateTime changedAt);
    
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt WHERE u.id = :userId")
    void softDelete(@Param("userId") UUID userId, @Param("deletedAt") LocalDateTime deletedAt);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.subscriptionPlan = :plan AND u.deletedAt IS NULL")
    long countBySubscriptionPlan(@Param("plan") String plan);
}
