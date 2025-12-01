package com.streamflix.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {
    
    @NotBlank(message = "Payment method token is required")
    private String paymentMethodToken;
    
    @NotBlank(message = "Card type is required")
    private String type;
    
    @Builder.Default
    private Boolean setAsDefault = false;
    
    private String billingName;
    private String billingEmail;
    
    // Billing address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
