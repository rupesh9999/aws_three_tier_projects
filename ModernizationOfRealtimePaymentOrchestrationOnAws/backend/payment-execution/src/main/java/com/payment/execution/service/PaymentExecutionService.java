package com.payment.execution.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.event.PaymentEvent;
import com.payment.common.model.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExecutionService {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_INITIATED, groupId = "payment-execution-group")
    @CircuitBreaker(name = "payment-execution", fallbackMethod = "executeFallback")
    public void processPayment(PaymentEvent event) {
        log.info("Processing payment execution: txnId={}, correlationId={}, amount={} {}",
            event.getTransactionId(), event.getCorrelationId(), event.getAmount(), event.getCurrency());

        try {
            // Simulate payment execution via external payment rails
            Thread.sleep((long) (Math.random() * 100 + 50));

            // Update event status
            event.setStatus(PaymentStatus.EXECUTED);
            event.setEventType("PAYMENT_EXECUTED");
            event.setTimestamp(Instant.now());

            kafkaTemplate.send(KafkaTopicConfig.PAYMENT_EXECUTED, event.getTenantId(), event);
            log.info("Payment executed successfully: txnId={}", event.getTransactionId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment execution interrupted: txnId={}", event.getTransactionId());
        }
    }

    private void executeFallback(PaymentEvent event, Throwable throwable) {
        log.error("Payment execution failed, sending to DLQ: txnId={}", event.getTransactionId(), throwable);
        event.setStatus(PaymentStatus.FAILED);
        event.setStatusReason("Execution engine unavailable: " + throwable.getMessage());
        event.setTimestamp(Instant.now());
        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_EXECUTION_DLQ, event.getTenantId(), event);
    }
}
