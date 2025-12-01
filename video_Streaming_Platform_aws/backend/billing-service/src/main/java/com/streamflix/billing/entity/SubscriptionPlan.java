package com.streamflix.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval")
    @Builder.Default
    private BillingInterval billingInterval = BillingInterval.MONTHLY;

    @Column(name = "trial_days")
    @Builder.Default
    private Integer trialDays = 0;

    @Column(name = "max_screens")
    @Builder.Default
    private Integer maxScreens = 1;

    @Column(name = "max_quality", length = 10)
    @Builder.Default
    private String maxQuality = "SD";

    @Column(columnDefinition = "jsonb")
    private String features;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PlanType {
        BASIC, STANDARD, PREMIUM
    }

    public enum BillingInterval {
        MONTHLY, QUARTERLY, YEARLY
    }
}
