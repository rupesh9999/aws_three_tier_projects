package com.commsec.trading.dto;

import com.commsec.trading.model.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private String accountId;
    private String symbol;
    private String companyName;
    private OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private BigDecimal quantity;
    private BigDecimal filledQuantity;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;
    private BigDecimal avgFillPrice;
    private BigDecimal totalValue;
    private BigDecimal brokerage;
    private TimeInForce timeInForce;
    private Instant expireAt;
    private Instant submittedAt;
    private Instant filledAt;
    private Instant cancelledAt;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .accountId(order.getAccountId())
            .symbol(order.getSymbol())
            .companyName(order.getCompanyName())
            .side(order.getSide())
            .type(order.getType())
            .status(order.getStatus())
            .quantity(order.getQuantity())
            .filledQuantity(order.getFilledQuantity())
            .limitPrice(order.getLimitPrice())
            .stopPrice(order.getStopPrice())
            .avgFillPrice(order.getAvgFillPrice())
            .totalValue(order.getTotalValue())
            .brokerage(order.getBrokerage())
            .timeInForce(order.getTimeInForce())
            .expireAt(order.getExpireAt())
            .submittedAt(order.getSubmittedAt())
            .filledAt(order.getFilledAt())
            .cancelledAt(order.getCancelledAt())
            .rejectionReason(order.getRejectionReason())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
