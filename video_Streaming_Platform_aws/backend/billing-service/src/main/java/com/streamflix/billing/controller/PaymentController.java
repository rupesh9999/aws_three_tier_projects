package com.streamflix.billing.controller;

import com.streamflix.billing.dto.*;
import com.streamflix.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // ==================== Payment Methods ====================
    
    @PostMapping("/methods")
    @Operation(summary = "Add a new payment method")
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PaymentMethodRequest request) {
        log.info("Adding payment method for user: {}", userId);
        return ResponseEntity.ok(paymentService.addPaymentMethod(userId, request));
    }
    
    @GetMapping("/methods")
    @Operation(summary = "Get user's payment methods")
    public ResponseEntity<List<PaymentMethodResponse>> getUserPaymentMethods(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(paymentService.getUserPaymentMethods(userId));
    }
    
    @GetMapping("/methods/{paymentMethodId}")
    @Operation(summary = "Get payment method by ID")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethod(@PathVariable UUID paymentMethodId) {
        return ResponseEntity.ok(paymentService.getPaymentMethod(paymentMethodId));
    }
    
    @PutMapping("/methods/{paymentMethodId}/default")
    @Operation(summary = "Set payment method as default")
    public ResponseEntity<PaymentMethodResponse> setDefaultPaymentMethod(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID paymentMethodId) {
        log.info("Setting default payment method: {} for user: {}", paymentMethodId, userId);
        return ResponseEntity.ok(paymentService.setDefaultPaymentMethod(userId, paymentMethodId));
    }
    
    @DeleteMapping("/methods/{paymentMethodId}")
    @Operation(summary = "Delete a payment method")
    public ResponseEntity<Void> deletePaymentMethod(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID paymentMethodId) {
        log.info("Deleting payment method: {} for user: {}", paymentMethodId, userId);
        paymentService.deletePaymentMethod(userId, paymentMethodId);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Invoices ====================
    
    @GetMapping("/invoices")
    @Operation(summary = "Get user's invoices")
    public ResponseEntity<Page<InvoiceResponse>> getUserInvoices(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUserInvoices(userId, pageable));
    }
    
    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(paymentService.getInvoice(invoiceId));
    }
    
    @GetMapping("/invoices/number/{invoiceNumber}")
    @Operation(summary = "Get invoice by invoice number")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(paymentService.getInvoiceByNumber(invoiceNumber));
    }
    
    // ==================== Payments ====================
    
    @GetMapping
    @Operation(summary = "Get user's payment history")
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId, pageable));
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
    
    @PostMapping("/process")
    @Operation(summary = "Process a payment for an invoice")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam UUID invoiceId,
            @RequestParam UUID paymentMethodId) {
        log.info("Processing payment for invoice: {} with method: {}", invoiceId, paymentMethodId);
        return ResponseEntity.ok(paymentService.processPayment(userId, invoiceId, paymentMethodId));
    }
    
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID paymentId,
            @RequestParam(required = false) BigDecimal amount) {
        log.info("Processing refund for payment: {}", paymentId);
        return ResponseEntity.ok(paymentService.refundPayment(userId, paymentId, amount));
    }
}
