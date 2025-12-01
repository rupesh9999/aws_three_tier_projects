package com.streamflix.billing.dto;

import com.streamflix.billing.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {
    
    private UUID id;
    private UUID userId;
    private PaymentMethod.PaymentMethodType type;
    private String cardBrand;
    private String last4;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private Boolean isActive;
    private String billingName;
    private String billingEmail;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PaymentMethodResponse fromEntity(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .userId(paymentMethod.getUserId())
                .type(paymentMethod.getType())
                .cardBrand(paymentMethod.getCardBrand())
                .last4(paymentMethod.getLast4())
                .expiryMonth(paymentMethod.getExpiryMonth())
                .expiryYear(paymentMethod.getExpiryYear())
                .isDefault(paymentMethod.getIsDefault())
                .isActive(paymentMethod.getIsActive())
                .billingName(paymentMethod.getBillingName())
                .billingEmail(paymentMethod.getBillingEmail())
                .country(paymentMethod.getCountry())
                .createdAt(paymentMethod.getCreatedAt())
                .updatedAt(paymentMethod.getUpdatedAt())
                .build();
    }
}
