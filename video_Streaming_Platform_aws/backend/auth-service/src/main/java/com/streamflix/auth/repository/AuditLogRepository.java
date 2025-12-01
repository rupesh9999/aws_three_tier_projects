package com.streamflix.auth.repository;

import com.streamflix.auth.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentByAction(@Param("action") String action, @Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ip AND al.action = :action AND al.createdAt >= :since")
    List<AuditLog> findByIpAndActionSince(@Param("ip") String ip, @Param("action") String action, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.userId = :userId AND al.action = :action AND al.success = false AND al.createdAt >= :since")
    long countFailedAttempts(@Param("userId") UUID userId, @Param("action") String action, @Param("since") LocalDateTime since);
}
