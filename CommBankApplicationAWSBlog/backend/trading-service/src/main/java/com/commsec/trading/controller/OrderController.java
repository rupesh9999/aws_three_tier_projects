package com.commsec.trading.controller;

import com.commsec.trading.dto.OrderRequest;
import com.commsec.trading.dto.OrderResponse;
import com.commsec.trading.model.OrderStatus;
import com.commsec.trading.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Trading", description = "Order management endpoints")
public class OrderController {

    private final TradingService tradingService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-Account-Id") String accountId,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = tradingService.placeOrder(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get orders for an account")
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @RequestHeader("X-Account-Id") String accountId,
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(tradingService.getOrders(accountId, status, pageable));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get a specific order")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-Account-Id") String accountId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(tradingService.getOrder(accountId, orderId));
    }

    @GetMapping("/open")
    @Operation(summary = "Get all open orders")
    public ResponseEntity<List<OrderResponse>> getOpenOrders(
            @RequestHeader("X-Account-Id") String accountId) {
        return ResponseEntity.ok(tradingService.getOpenOrders(accountId));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-Account-Id") String accountId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(tradingService.cancelOrder(accountId, orderId));
    }
}
