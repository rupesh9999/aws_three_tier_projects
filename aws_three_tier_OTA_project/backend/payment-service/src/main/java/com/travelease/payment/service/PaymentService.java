package com.travelease.payment.service;

import com.travelease.common.dto.PageResponse;
import com.travelease.common.exception.BusinessException;
import com.travelease.payment.dto.PaymentResponse;
import com.travelease.payment.dto.ProcessPaymentRequest;
import com.travelease.payment.entity.Payment;
import com.travelease.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public PaymentResponse processPayment(String userId, ProcessPaymentRequest request) {
        log.info("Processing payment for booking: {}", request.getBookingId());

        String transactionId = generateTransactionId();
        while (paymentRepository.existsByTransactionId(transactionId)) {
            transactionId = generateTransactionId();
        }

        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.valueOf(request.getPaymentMethod());

        Payment payment = Payment.builder()
                .userId(UUID.fromString(userId))
                .bookingId(UUID.fromString(request.getBookingId()))
                .transactionId(transactionId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Payment.PaymentStatus.PROCESSING)
                .paymentMethod(paymentMethod)
                .build();

        // Handle card details
        if (paymentMethod == Payment.PaymentMethod.CREDIT_CARD || paymentMethod == Payment.PaymentMethod.DEBIT_CARD) {
            if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
                payment.setCardLastFour(request.getCardNumber().substring(request.getCardNumber().length() - 4));
                payment.setCardBrand(detectCardBrand(request.getCardNumber()));
            }
        }

        // Simulate payment processing
        boolean paymentSuccess = simulatePaymentProcessing(request);

        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setGatewayResponse("Payment processed successfully");
            log.info("Payment {} completed successfully", transactionId);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by issuer");
            log.warn("Payment {} failed", transactionId);
        }

        payment = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId, String userId) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new BusinessException("Payment not found"));

        if (!payment.getUserId().toString().equals(userId)) {
            throw new BusinessException("Payment does not belong to user");
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(String bookingId, String userId) {
        Payment payment = paymentRepository.findByBookingId(UUID.fromString(bookingId))
                .orElseThrow(() -> new BusinessException("Payment not found for booking"));

        if (!payment.getUserId().toString().equals(userId)) {
            throw new BusinessException("Payment does not belong to user");
        }

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getUserPayments(String userId, int page, int size) {
        Page<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(
                UUID.fromString(userId), PageRequest.of(page, size));

        List<PaymentResponse> content = payments.getContent().stream()
                .map(PaymentResponse::fromEntity)
                .toList();

        return PageResponse.of(content, payments.getTotalElements(), payments.getTotalPages(), page, size);
    }

    @Transactional
    public PaymentResponse refundPayment(String paymentId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new BusinessException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new BusinessException("Only completed payments can be refunded");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("Refund amount cannot exceed payment amount");
        }

        payment.setRefundAmount(refundAmount);
        payment.setRefundedAt(LocalDateTime.now());

        if (refundAmount.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
        }

        payment = paymentRepository.save(payment);
        log.info("Payment {} refunded with amount {}", payment.getTransactionId(), refundAmount);

        return PaymentResponse.fromEntity(payment);
    }

    private String generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = random.nextInt(100000);
        return String.format("TXN%d%05d", timestamp, randomNum);
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "VISA";
        } else if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) {
            return "MASTERCARD";
        } else if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "AMEX";
        } else if (cardNumber.startsWith("6")) {
            return "DISCOVER";
        }
        return "UNKNOWN";
    }

    private boolean simulatePaymentProcessing(ProcessPaymentRequest request) {
        // Simulate 95% success rate
        return random.nextDouble() < 0.95;
    }
}
