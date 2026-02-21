package com.payment.initiation.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.common.event.PaymentEvent;
import com.payment.common.exception.DuplicatePaymentException;
import com.payment.common.exception.PaymentNotFoundException;
import com.payment.common.model.Payment;
import com.payment.common.model.PaymentStatus;
import com.payment.initiation.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiationService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Transactional
    @CircuitBreaker(name = "payment-initiation", fallbackMethod = "initiatePaymentFallback")
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for tenant={}, amount={} {}", 
            request.getTenantId(), request.getAmount(), request.getCurrency());

        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<Payment> existing = paymentRepository
                .findByIdempotencyKeyAndTenantId(request.getIdempotencyKey(), request.getTenantId());
            if (existing.isPresent()) {
                log.info("Duplicate payment detected: idempotencyKey={}", request.getIdempotencyKey());
                throw new DuplicatePaymentException(
                    "Payment already exists with idempotency key: " + request.getIdempotencyKey());
            }
        }

        // Create payment entity
        Payment payment = Payment.builder()
            .idempotencyKey(request.getIdempotencyKey())
            .tenantId(request.getTenantId())
            .debtorAccount(request.getDebtorAccount())
            .creditorAccount(request.getCreditorAccount())
            .debtorName(request.getDebtorName())
            .creditorName(request.getCreditorName())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .paymentType(request.getPaymentType())
            .status(PaymentStatus.INITIATED)
            .region(request.getRegion())
            .build();

        payment = paymentRepository.save(payment);
        log.info("Payment saved: id={}, correlationId={}", payment.getId(), payment.getCorrelationId());

        // Publish event to Kafka
        PaymentEvent event = PaymentEvent.builder()
            .transactionId(payment.getId())
            .correlationId(payment.getCorrelationId())
            .tenantId(payment.getTenantId())
            .debtorAccount(payment.getDebtorAccount())
            .creditorAccount(payment.getCreditorAccount())
            .debtorName(payment.getDebtorName())
            .creditorName(payment.getCreditorName())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentType(payment.getPaymentType())
            .status(PaymentStatus.INITIATED)
            .eventType("PAYMENT_INITIATED")
            .region(payment.getRegion())
            .timestamp(Instant.now())
            .build();

        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_INITIATED, 
            payment.getTenantId(), event);
        log.info("Payment event published to {}: txnId={}", 
            KafkaTopicConfig.PAYMENT_INITIATED, payment.getId());

        return toResponse(payment);
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
        return toResponse(payment);
    }

    public Page<PaymentResponse> getPaymentsByTenant(String tenantId, Pageable pageable) {
        return paymentRepository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    public Page<PaymentResponse> getPaymentsByTenantAndStatus(String tenantId, PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByTenantIdAndStatus(tenantId, status, pageable).map(this::toResponse);
    }

    private PaymentResponse initiatePaymentFallback(PaymentRequest request, Throwable throwable) {
        log.error("Circuit breaker fallback for payment initiation: tenant={}", 
            request.getTenantId(), throwable);
        return PaymentResponse.builder()
            .status(PaymentStatus.FAILED)
            .statusReason("Service temporarily unavailable. Please try again.")
            .build();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
            .transactionId(payment.getId())
            .correlationId(payment.getCorrelationId())
            .tenantId(payment.getTenantId())
            .debtorAccount(payment.getDebtorAccount())
            .creditorAccount(payment.getCreditorAccount())
            .debtorName(payment.getDebtorName())
            .creditorName(payment.getCreditorName())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .paymentType(payment.getPaymentType())
            .status(payment.getStatus())
            .statusReason(payment.getStatusReason())
            .feeAmount(payment.getFeeAmount())
            .riskScore(payment.getRiskScore())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .completedAt(payment.getCompletedAt())
            .build();
    }
}
