package com.payment.common.dto;

import com.payment.common.model.PaymentStatus;
import com.payment.common.model.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private UUID transactionId;
    private String correlationId;
    private String tenantId;
    private String debtorAccount;
    private String creditorAccount;
    private String debtorName;
    private String creditorName;
    private BigDecimal amount;
    private String currency;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String statusReason;
    private BigDecimal feeAmount;
    private Double riskScore;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
}
