package com.streamflix.billing.repository;

import com.streamflix.billing.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUserId(UUID userId);
    
    Optional<Subscription> findByUserIdAndStatus(UUID userId, Subscription.SubscriptionStatus status);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    
    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status IN ('ACTIVE', 'TRIALING')")
    Optional<Subscription> findActiveOrTrialingByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate < :date")
    List<Subscription> findExpiredSubscriptions(
            @Param("status") Subscription.SubscriptionStatus status,
            @Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIALING' AND s.trialEndDate < :date")
    List<Subscription> findExpiredTrials(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'PENDING_CANCELLATION' AND s.endDate < :date")
    List<Subscription> findSubscriptionsToCancel(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.nextBillingDate BETWEEN :start AND :end")
    List<Subscription> findSubscriptionsDueForRenewal(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    Page<Subscription> findByStatusIn(List<Subscription.SubscriptionStatus> statuses, Pageable pageable);

    boolean existsByUserIdAndStatusIn(UUID userId, List<Subscription.SubscriptionStatus> statuses);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status IN :statuses")
    long countByStatuses(@Param("statuses") List<Subscription.SubscriptionStatus> statuses);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.userId = :userId AND s.status IN ('ACTIVE', 'TRIALING')")
    long countActiveSubscriptions(@Param("userId") UUID userId);
}
