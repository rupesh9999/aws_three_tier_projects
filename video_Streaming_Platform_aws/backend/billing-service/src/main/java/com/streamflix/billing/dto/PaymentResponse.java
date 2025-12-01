package com.streamflix.billing.dto;

import com.streamflix.billing.entity.Payment;
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
public class PaymentResponse {
    
    private UUID id;
    private UUID userId;
    private UUID invoiceId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private String paymentIntentId;
    private String chargeId;
    private String receiptUrl;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;
    private BigDecimal refundedAmount;
    private String metadata;
    private LocalDateTime createdAt;
    
    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null)
                .paymentMethodId(payment.getPaymentMethod() != null ? payment.getPaymentMethod().getId() : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentIntentId(payment.getPaymentIntentId())
                .chargeId(payment.getChargeId())
                .receiptUrl(payment.getReceiptUrl())
                .failureCode(payment.getFailureCode())
                .failureMessage(payment.getFailureMessage())
                .processedAt(payment.getProcessedAt())
                .refundedAt(payment.getRefundedAt())
                .refundedAmount(payment.getRefundedAmount())
                .metadata(payment.getMetadata())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
