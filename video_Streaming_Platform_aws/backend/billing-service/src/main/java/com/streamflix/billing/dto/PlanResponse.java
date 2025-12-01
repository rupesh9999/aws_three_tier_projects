package com.streamflix.billing.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.billing.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {
    private UUID id;
    private String name;
    private String type;
    private String description;
    private BigDecimal price;
    private String currency;
    private String billingInterval;
    private Integer trialDays;
    private Integer maxScreens;
    private String maxQuality;
    private List<String> features;
    private Boolean isActive;
    private String stripePriceId;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PlanResponse fromEntity(SubscriptionPlan plan) {
        List<String> features = null;
        if (plan.getFeatures() != null) {
            try {
                features = objectMapper.readValue(plan.getFeatures(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                features = List.of();
            }
        }

        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .type(plan.getType() != null ? plan.getType().name() : null)
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingInterval(plan.getBillingInterval() != null ? plan.getBillingInterval().name() : null)
                .trialDays(plan.getTrialDays())
                .maxScreens(plan.getMaxScreens())
                .maxQuality(plan.getMaxQuality())
                .features(features)
                .isActive(plan.getIsActive())
                .stripePriceId(plan.getStripePriceId())
                .build();
    }
}
