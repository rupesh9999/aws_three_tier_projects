package com.payment.tracking.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTrackingService {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final Map<UUID, PaymentEvent> trackingStore = new ConcurrentHashMap<>();

    @KafkaListener(topics = {
        KafkaTopicConfig.PAYMENT_INITIATED,
        KafkaTopicConfig.PAYMENT_EXECUTED,
        KafkaTopicConfig.RISK_ASSESSED,
        KafkaTopicConfig.PAYMENT_SETTLED
    }, groupId = "payment-tracking-group")
    public void trackPayment(PaymentEvent event) {
        log.info("Tracking event: txnId={}, status={}, eventType={}, correlationId={}",
            event.getTransactionId(), event.getStatus(), event.getEventType(), event.getCorrelationId());

        trackingStore.put(event.getTransactionId(), event);

        // Forward to tracking topic for downstream consumers
        event.setEventType("PAYMENT_TRACKED");
        event.setTimestamp(Instant.now());
        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_TRACKING, event.getTenantId(), event);
    }

    public PaymentEvent getTrackingInfo(UUID transactionId) {
        return trackingStore.get(transactionId);
    }

    public Map<UUID, PaymentEvent> getAllTracking() {
        return trackingStore;
    }
}
