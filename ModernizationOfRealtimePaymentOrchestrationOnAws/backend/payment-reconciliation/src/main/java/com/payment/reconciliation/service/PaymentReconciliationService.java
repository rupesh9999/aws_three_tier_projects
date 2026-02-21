package com.payment.reconciliation.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.event.PaymentEvent;
import com.payment.common.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationService {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_SETTLED, groupId = "payment-reconciliation-group")
    public void reconcilePayment(PaymentEvent event) {
        log.info("Reconciling payment: txnId={}, amount={} {}, correlationId={}",
            event.getTransactionId(), event.getAmount(), event.getCurrency(), event.getCorrelationId());

        try {
            // Simulate reconciliation against external ledger
            Thread.sleep((long) (Math.random() * 80 + 20));

            event.setStatus(PaymentStatus.RECONCILED);
            event.setEventType("PAYMENT_RECONCILED");
            event.setTimestamp(Instant.now());

            // Forward to billing
            kafkaTemplate.send(KafkaTopicConfig.PAYMENT_BILLING, event.getTenantId(), event);
            log.info("Payment reconciled, forwarded to billing: txnId={}", event.getTransactionId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Reconciliation interrupted: txnId={}", event.getTransactionId());
        }
    }
}
