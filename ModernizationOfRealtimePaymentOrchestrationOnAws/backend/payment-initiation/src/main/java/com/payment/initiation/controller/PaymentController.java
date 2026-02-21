package com.payment.initiation.controller;

import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.common.model.PaymentStatus;
import com.payment.initiation.service.PaymentInitiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Initiation", description = "APIs for initiating and querying payments")
public class PaymentController {

    private final PaymentInitiationService paymentService;

    @PostMapping
    @Operation(summary = "Initiate a new payment", description = "Creates a new payment and publishes event to Kafka")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by transaction ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "List payments by tenant", description = "Paginated list of payments for a tenant")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByTenant(
            @PathVariable String tenantId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByTenantAndStatus(tenantId, status, pageable));
        }
        return ResponseEntity.ok(paymentService.getPaymentsByTenant(tenantId, pageable));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Initiation Service - Healthy");
    }
}
