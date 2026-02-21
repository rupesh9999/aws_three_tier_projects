package com.payment.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_tenant_status", columnList = "tenantId, status"),
    @Index(name = "idx_payments_correlation_id", columnList = "correlationId"),
    @Index(name = "idx_payments_created_at", columnList = "createdAt")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_idempotency_tenant", columnNames = {"idempotencyKey", "tenantId"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 36)
    private String idempotencyKey;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String debtorAccount;

    @Column(nullable = false, length = 50)
    private String creditorAccount;

    @Column(nullable = false, length = 100)
    private String debtorName;

    @Column(nullable = false, length = 100)
    private String creditorName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(length = 36)
    private String correlationId;

    @Column(length = 20)
    private String region;

    @Column(length = 500)
    private String statusReason;

    private BigDecimal feeAmount;

    private Double riskScore;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        if (idempotencyKey == null) {
            idempotencyKey = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
