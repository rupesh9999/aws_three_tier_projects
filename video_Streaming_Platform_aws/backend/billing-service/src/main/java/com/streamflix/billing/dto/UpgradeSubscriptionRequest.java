package com.streamflix.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpgradeSubscriptionRequest {
    
    @NotNull(message = "New plan ID is required")
    private UUID newPlanId;
    
    @Builder.Default
    private Boolean prorated = true;
    
    private String couponCode;
}
