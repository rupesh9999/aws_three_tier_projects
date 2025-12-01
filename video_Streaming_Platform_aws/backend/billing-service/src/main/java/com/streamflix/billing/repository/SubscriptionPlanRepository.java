package com.streamflix.billing.repository;

import com.streamflix.billing.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByType(SubscriptionPlan.PlanType type);

    List<SubscriptionPlan> findByIsActiveTrue();

    List<SubscriptionPlan> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<SubscriptionPlan> findByStripePriceId(String stripePriceId);

    boolean existsByType(SubscriptionPlan.PlanType type);
}
