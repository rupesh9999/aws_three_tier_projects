package com.commsec.trading.service;

import com.commsec.trading.dto.OrderRequest;
import com.commsec.trading.dto.OrderResponse;
import com.commsec.trading.exception.OrderNotFoundException;
import com.commsec.trading.exception.OrderValidationException;
import com.commsec.trading.model.*;
import com.commsec.trading.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TradingService {

    private final OrderRepository orderRepository;
    private final Counter ordersPlaced;
    private final Counter ordersCancelled;
    private final Timer orderProcessingTime;

    private static final BigDecimal BROKERAGE_FEE = new BigDecimal("9.95");
    private static final BigDecimal BROKERAGE_PERCENTAGE = new BigDecimal("0.0011"); // 0.11%

    public TradingService(OrderRepository orderRepository, MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        
        this.ordersPlaced = Counter.builder("trading.orders.placed")
            .description("Number of orders placed")
            .register(meterRegistry);
        
        this.ordersCancelled = Counter.builder("trading.orders.cancelled")
            .description("Number of orders cancelled")
            .register(meterRegistry);
        
        this.orderProcessingTime = Timer.builder("trading.order.processing.time")
            .description("Time to process orders")
            .register(meterRegistry);
    }

    @Transactional
    public OrderResponse placeOrder(String accountId, OrderRequest request) {
        return orderProcessingTime.record(() -> {
            log.info("Placing order for account: {}, symbol: {}, side: {}", 
                accountId, request.getSymbol(), request.getSide());

            validateOrderRequest(request);

            Order order = Order.builder()
                .accountId(accountId)
                .symbol(request.getSymbol().toUpperCase())
                .side(request.getSide())
                .type(request.getType())
                .status(OrderStatus.PENDING)
                .quantity(request.getQuantity())
                .filledQuantity(BigDecimal.ZERO)
                .limitPrice(request.getLimitPrice())
                .stopPrice(request.getStopPrice())
                .timeInForce(request.getTimeInForce())
                .expireAt(request.getExpireAt())
                .submittedAt(Instant.now())
                .brokerage(calculateBrokerage(request))
                .build();

            Order savedOrder = orderRepository.save(order);
            ordersPlaced.increment();

            log.info("Order placed successfully: {}", savedOrder.getId());
            return OrderResponse.fromEntity(savedOrder);
        });
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(String accountId, OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByAccountIdAndStatus(accountId, status, pageable);
        } else {
            orders = orderRepository.findByAccountId(accountId, pageable);
        }
        return orders.map(OrderResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String accountId, UUID orderId) {
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String accountId, UUID orderId) {
        log.info("Cancelling order: {} for account: {}", orderId, accountId);

        Order order = orderRepository.findByIdAndAccountId(orderId, accountId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!canCancel(order)) {
            throw new OrderValidationException("Order cannot be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());

        Order savedOrder = orderRepository.save(order);
        ordersCancelled.increment();

        log.info("Order cancelled successfully: {}", orderId);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOpenOrders(String accountId) {
        List<OrderStatus> openStatuses = List.of(
            OrderStatus.PENDING, 
            OrderStatus.OPEN, 
            OrderStatus.PARTIALLY_FILLED
        );
        return orderRepository.findByAccountIdAndStatusIn(accountId, openStatuses)
            .stream()
            .map(OrderResponse::fromEntity)
            .toList();
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request.getType() == OrderType.LIMIT || request.getType() == OrderType.STOP_LIMIT) {
            if (request.getLimitPrice() == null || request.getLimitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Limit price is required for LIMIT and STOP_LIMIT orders");
            }
        }

        if (request.getType() == OrderType.STOP || request.getType() == OrderType.STOP_LIMIT) {
            if (request.getStopPrice() == null || request.getStopPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Stop price is required for STOP and STOP_LIMIT orders");
            }
        }
    }

    private BigDecimal calculateBrokerage(OrderRequest request) {
        BigDecimal price = request.getLimitPrice() != null ? request.getLimitPrice() : new BigDecimal("100");
        BigDecimal tradeValue = price.multiply(request.getQuantity());
        BigDecimal percentageFee = tradeValue.multiply(BROKERAGE_PERCENTAGE);
        return percentageFee.max(BROKERAGE_FEE);
    }

    private boolean canCancel(Order order) {
        return order.getStatus() == OrderStatus.PENDING || 
               order.getStatus() == OrderStatus.OPEN;
    }
}
