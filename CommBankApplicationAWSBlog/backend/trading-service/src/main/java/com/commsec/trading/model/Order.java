package com.commsec.trading.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_account_id", columnList = "account_id"),
    @Index(name = "idx_orders_symbol", columnList = "symbol"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "filled_quantity", precision = 15, scale = 2)
    private BigDecimal filledQuantity;

    @Column(name = "limit_price", precision = 15, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "stop_price", precision = 15, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "avg_fill_price", precision = 15, scale = 4)
    private BigDecimal avgFillPrice;

    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal brokerage;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force")
    private TimeInForce timeInForce;

    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "filled_at")
    private Instant filledAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;
}
