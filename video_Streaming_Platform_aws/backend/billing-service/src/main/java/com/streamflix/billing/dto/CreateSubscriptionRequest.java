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
public class CreateSubscriptionRequest {
    
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    private String paymentMethodId;
    
    private String couponCode;
    
    @Builder.Default
    private Boolean startTrial = false;
}
