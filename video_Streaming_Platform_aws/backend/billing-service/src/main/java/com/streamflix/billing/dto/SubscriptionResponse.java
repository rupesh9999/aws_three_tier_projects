package com.streamflix.billing.dto;

import com.streamflix.billing.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    
    private UUID id;
    private UUID userId;
    private PlanResponse plan;
    private Subscription.SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime nextBillingDate;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Boolean autoRenew;
    private String stripeSubscriptionId;
    private BigDecimal currentPeriodAmount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static SubscriptionResponse fromEntity(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .plan(subscription.getPlan() != null ? PlanResponse.fromEntity(subscription.getPlan()) : null)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .trialEndDate(subscription.getTrialEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .cancelledAt(subscription.getCancelledAt())
                .cancellationReason(subscription.getCancellationReason())
                .autoRenew(subscription.getAutoRenew())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .currentPeriodAmount(subscription.getCurrentPeriodAmount())
                .currency(subscription.getCurrency())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
