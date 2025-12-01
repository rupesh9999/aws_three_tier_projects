package com.streamflix.billing.controller;

import com.streamflix.billing.dto.*;
import com.streamflix.billing.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    @GetMapping("/plans")
    @Operation(summary = "Get available subscription plans")
    public ResponseEntity<List<PlanResponse>> getAvailablePlans() {
        return ResponseEntity.ok(subscriptionService.getAvailablePlans());
    }
    
    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get subscription plan by ID")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable UUID planId) {
        return ResponseEntity.ok(subscriptionService.getPlanById(planId));
    }
    
    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Creating subscription for user: {}", userId);
        return ResponseEntity.ok(subscriptionService.createSubscription(userId, request));
    }
    
    @GetMapping("/current")
    @Operation(summary = "Get current user's subscription")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription(
            @RequestHeader("X-User-Id") UUID userId) {
        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription(userId);
        if (subscription == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(subscription);
    }
    
    @GetMapping("/{subscriptionId}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get user's subscription history")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionHistory(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptionHistory(userId));
    }
    
    @PutMapping("/upgrade")
    @Operation(summary = "Upgrade subscription to a new plan")
    public ResponseEntity<SubscriptionResponse> upgradeSubscription(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpgradeSubscriptionRequest request) {
        log.info("Upgrading subscription for user: {}", userId);
        return ResponseEntity.ok(subscriptionService.upgradeSubscription(userId, request));
    }
    
    @PostMapping("/cancel")
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CancelSubscriptionRequest request) {
        log.info("Cancelling subscription for user: {}", userId);
        return ResponseEntity.ok(subscriptionService.cancelSubscription(userId, request));
    }
    
    @PostMapping("/resume")
    @Operation(summary = "Resume a pending cancellation subscription")
    public ResponseEntity<SubscriptionResponse> resumeSubscription(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Resuming subscription for user: {}", userId);
        return ResponseEntity.ok(subscriptionService.resumeSubscription(userId));
    }
}
