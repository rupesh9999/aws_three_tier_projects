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
public class CancelSubscriptionRequest {
    
    @NotNull(message = "Subscription ID is required")
    private UUID subscriptionId;
    
    private String reason;
    
    @Builder.Default
    private Boolean cancelImmediately = false;
    
    private String feedback;
}
