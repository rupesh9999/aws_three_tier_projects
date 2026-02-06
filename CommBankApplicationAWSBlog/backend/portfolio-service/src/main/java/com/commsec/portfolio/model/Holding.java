package com.commsec.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "company_name")
    private String companyName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal averageCost;

    @Column(name = "total_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "current_value", precision = 18, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "unrealized_pnl", precision = 18, scale = 2)
    private BigDecimal unrealizedPnl;

    @Column(name = "unrealized_pnl_percent", precision = 8, scale = 4)
    private BigDecimal unrealizedPnlPercent;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
