package com.travelease.payment.controller;

import com.travelease.common.dto.ApiResponse;
import com.travelease.common.dto.PageResponse;
import com.travelease.common.security.UserPrincipal;
import com.travelease.payment.dto.PaymentResponse;
import com.travelease.payment.dto.ProcessPaymentRequest;
import com.travelease.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment processed"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        PaymentResponse response = paymentService.getPaymentById(id, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String bookingId) {
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getUserPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PaymentResponse> response = paymentService.getUserPayments(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        PaymentResponse response = paymentService.refundPayment(id, amount);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }
}
