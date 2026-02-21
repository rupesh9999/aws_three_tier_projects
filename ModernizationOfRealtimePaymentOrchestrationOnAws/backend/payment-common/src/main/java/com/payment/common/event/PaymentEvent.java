package com.payment.common.event;

import com.payment.common.model.PaymentStatus;
import com.payment.common.model.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEvent {
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
    private String eventType;
    private String region;
    private Double riskScore;
    private BigDecimal feeAmount;
    private String statusReason;
    private Instant timestamp;

    @Builder.Default
    private String schemaVersion = "1.0";
}
