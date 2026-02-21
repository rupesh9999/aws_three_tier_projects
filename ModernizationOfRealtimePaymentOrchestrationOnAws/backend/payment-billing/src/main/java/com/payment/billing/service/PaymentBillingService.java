package com.payment.billing.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.event.PaymentEvent;
import com.payment.common.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentBillingService {

    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.0025"); // 0.25% fee

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_BILLING, groupId = "payment-billing-group")
    public void processPaymentBilling(PaymentEvent event) {
        log.info("Processing billing: txnId={}, amount={} {}, correlationId={}",
            event.getTransactionId(), event.getAmount(), event.getCurrency(), event.getCorrelationId());

        // Calculate fee
        BigDecimal fee = event.getAmount().multiply(FEE_PERCENTAGE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal minFee = new BigDecimal("0.50");
        BigDecimal maxFee = new BigDecimal("500.00");
        fee = fee.max(minFee).min(maxFee);

        event.setFeeAmount(fee);
        event.setStatus(PaymentStatus.BILLED);
        event.setEventType("PAYMENT_BILLED");
        event.setTimestamp(Instant.now());

        // Publish notification for billing completion
        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_NOTIFICATION, event.getTenantId(), event);

        log.info("Payment billed: txnId={}, fee={} {}", 
            event.getTransactionId(), fee, event.getCurrency());
    }
}
