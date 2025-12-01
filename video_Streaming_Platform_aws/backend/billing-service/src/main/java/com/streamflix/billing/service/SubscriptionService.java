package com.streamflix.billing.service;

import com.streamflix.billing.dto.*;
import com.streamflix.billing.entity.Subscription;
import com.streamflix.billing.entity.SubscriptionPlan;
import com.streamflix.billing.repository.SubscriptionPlanRepository;
import com.streamflix.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final StripeService stripeService;
    
    public List<PlanResponse> getAvailablePlans() {
        return planRepository.findByIsActiveTrue()
                .stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public PlanResponse getPlanById(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        return PlanResponse.fromEntity(plan);
    }
    
    public SubscriptionResponse createSubscription(UUID userId, CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {} with plan: {}", userId, request.getPlanId());
        
        // Check if user already has an active subscription
        subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new IllegalStateException("User already has an active subscription");
                });
        
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + request.getPlanId()));
        
        if (!plan.getIsActive()) {
            throw new IllegalArgumentException("Plan is not available: " + plan.getName());
        }
        
        // Create Stripe subscription
        String stripeSubscriptionId = stripeService.createSubscription(
                userId.toString(),
                plan.getStripePriceId(),
                request.getPaymentMethodId()
        );
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(now, plan.getBillingInterval());
        LocalDateTime trialEndDate = null;
        
        if (Boolean.TRUE.equals(request.getStartTrial()) && plan.getTrialDays() != null && plan.getTrialDays() > 0) {
            trialEndDate = now.plusDays(plan.getTrialDays());
        }
        
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .plan(plan)
                .status(trialEndDate != null ? Subscription.SubscriptionStatus.TRIALING : Subscription.SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .trialEndDate(trialEndDate)
                .nextBillingDate(trialEndDate != null ? trialEndDate : endDate)
                .autoRenew(true)
                .stripeSubscriptionId(stripeSubscriptionId)
                .currentPeriodAmount(plan.getPrice())
                .currency(plan.getCurrency())
                .build();
        
        subscription = subscriptionRepository.save(subscription);
        log.info("Created subscription: {} for user: {}", subscription.getId(), userId);
        
        return SubscriptionResponse.fromEntity(subscription);
    }
    
    public SubscriptionResponse getCurrentSubscription(UUID userId) {
        return subscriptionRepository.findActiveOrTrialingByUserId(userId)
                .map(SubscriptionResponse::fromEntity)
                .orElse(null);
    }
    
    public SubscriptionResponse getSubscriptionById(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
        return SubscriptionResponse.fromEntity(subscription);
    }
    
    public List<SubscriptionResponse> getUserSubscriptionHistory(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public SubscriptionResponse upgradeSubscription(UUID userId, UpgradeSubscriptionRequest request) {
        log.info("Upgrading subscription for user: {} to plan: {}", userId, request.getNewPlanId());
        
        Subscription currentSubscription = subscriptionRepository.findActiveOrTrialingByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active subscription found for user: " + userId));
        
        SubscriptionPlan newPlan = planRepository.findById(request.getNewPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + request.getNewPlanId()));
        
        if (!newPlan.getIsActive()) {
            throw new IllegalArgumentException("Plan is not available: " + newPlan.getName());
        }
        
        // Update Stripe subscription
        stripeService.updateSubscription(
                currentSubscription.getStripeSubscriptionId(),
                newPlan.getStripePriceId(),
                request.getProrated()
        );
        
        // Calculate prorated amount if applicable
        if (Boolean.TRUE.equals(request.getProrated())) {
            BigDecimal proratedAmount = calculateProratedAmount(
                    currentSubscription.getCurrentPeriodAmount(),
                    newPlan.getPrice(),
                    currentSubscription.getNextBillingDate()
            );
            currentSubscription.setCurrentPeriodAmount(proratedAmount);
        } else {
            currentSubscription.setCurrentPeriodAmount(newPlan.getPrice());
        }
        
        currentSubscription.setPlan(newPlan);
        currentSubscription = subscriptionRepository.save(currentSubscription);
        
        log.info("Upgraded subscription: {} to plan: {}", currentSubscription.getId(), newPlan.getName());
        
        return SubscriptionResponse.fromEntity(currentSubscription);
    }
    
    public SubscriptionResponse cancelSubscription(UUID userId, CancelSubscriptionRequest request) {
        log.info("Cancelling subscription for user: {}", userId);
        
        Subscription subscription = subscriptionRepository.findActiveOrTrialingByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No active subscription found for user: " + userId));
        
        // Cancel in Stripe
        stripeService.cancelSubscription(
                subscription.getStripeSubscriptionId(),
                request.getCancelImmediately()
        );
        
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(request.getReason());
        subscription.setAutoRenew(false);
        
        if (Boolean.TRUE.equals(request.getCancelImmediately())) {
            subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            subscription.setEndDate(LocalDateTime.now());
        } else {
            subscription.setStatus(Subscription.SubscriptionStatus.PENDING_CANCELLATION);
        }
        
        subscription = subscriptionRepository.save(subscription);
        log.info("Cancelled subscription: {} for user: {}", subscription.getId(), userId);
        
        return SubscriptionResponse.fromEntity(subscription);
    }
    
    public SubscriptionResponse resumeSubscription(UUID userId) {
        log.info("Resuming subscription for user: {}", userId);
        
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.PENDING_CANCELLATION)
                .orElseThrow(() -> new IllegalStateException("No pending cancellation subscription found for user: " + userId));
        
        // Resume in Stripe
        stripeService.resumeSubscription(subscription.getStripeSubscriptionId());
        
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setCancelledAt(null);
        subscription.setCancellationReason(null);
        subscription.setAutoRenew(true);
        
        subscription = subscriptionRepository.save(subscription);
        log.info("Resumed subscription: {} for user: {}", subscription.getId(), userId);
        
        return SubscriptionResponse.fromEntity(subscription);
    }
    
    public void processSubscriptionRenewal(String stripeSubscriptionId) {
        log.info("Processing renewal for Stripe subscription: {}", stripeSubscriptionId);
        
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for Stripe ID: " + stripeSubscriptionId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newEndDate = calculateEndDate(now, subscription.getPlan().getBillingInterval());
        
        subscription.setStartDate(now);
        subscription.setEndDate(newEndDate);
        subscription.setNextBillingDate(newEndDate);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        
        subscriptionRepository.save(subscription);
        log.info("Renewed subscription: {}", subscription.getId());
    }
    
    public void handlePaymentFailed(String stripeSubscriptionId) {
        log.warn("Payment failed for Stripe subscription: {}", stripeSubscriptionId);
        
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for Stripe ID: " + stripeSubscriptionId));
        
        subscription.setStatus(Subscription.SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
        
        log.warn("Marked subscription {} as PAST_DUE", subscription.getId());
    }
    
    public void expireSubscription(String stripeSubscriptionId) {
        log.info("Expiring Stripe subscription: {}", stripeSubscriptionId);
        
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for Stripe ID: " + stripeSubscriptionId));
        
        subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        log.info("Expired subscription: {}", subscription.getId());
    }
    
    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubscriptionPlan.BillingInterval interval) {
        return switch (interval) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }
    
    private BigDecimal calculateProratedAmount(BigDecimal currentAmount, BigDecimal newAmount, LocalDateTime nextBillingDate) {
        // Simplified proration calculation
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), nextBillingDate);
        if (daysRemaining <= 0) {
            return newAmount;
        }
        
        BigDecimal dailyDifference = newAmount.subtract(currentAmount).divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
        return dailyDifference.multiply(BigDecimal.valueOf(daysRemaining));
    }
}
