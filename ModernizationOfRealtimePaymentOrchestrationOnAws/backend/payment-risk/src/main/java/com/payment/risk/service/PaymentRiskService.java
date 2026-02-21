package com.payment.risk.service;

import com.payment.common.config.KafkaTopicConfig;
import com.payment.common.event.PaymentEvent;
import com.payment.common.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRiskService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000.00");
    private static final double RISK_THRESHOLD = 0.75;

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_INITIATED, groupId = "payment-risk-group")
    public void assessRisk(PaymentEvent event) {
        log.info("Risk assessment: txnId={}, amount={} {}, paymentType={}", 
            event.getTransactionId(), event.getAmount(), event.getCurrency(), event.getPaymentType());

        double riskScore = calculateRiskScore(event);
        event.setRiskScore(riskScore);
        event.setTimestamp(Instant.now());

        if (riskScore >= RISK_THRESHOLD) {
            log.warn("HIGH RISK payment detected: txnId={}, score={}", event.getTransactionId(), riskScore);
            event.setStatus(PaymentStatus.RISK_REJECTED);
            event.setEventType("RISK_REJECTED");
            event.setStatusReason(String.format("Risk score %.2f exceeds threshold %.2f", riskScore, RISK_THRESHOLD));
        } else {
            event.setStatus(PaymentStatus.RISK_APPROVED);
            event.setEventType("RISK_APPROVED");
            log.info("Risk approved: txnId={}, score={}", event.getTransactionId(), riskScore);
        }

        kafkaTemplate.send(KafkaTopicConfig.RISK_ASSESSED, event.getTenantId(), event);
    }

    private double calculateRiskScore(PaymentEvent event) {
        double score = 0.0;

        // High-value transaction scoring
        if (event.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            score += 0.3;
        } else if (event.getAmount().compareTo(new BigDecimal("10000.00")) > 0) {
            score += 0.15;
        }

        // Cross-border scoring
        switch (event.getPaymentType()) {
            case CROSS_BORDER -> score += 0.2;
            case WIRE_TRANSFER -> score += 0.15;
            case PEER_TO_PEER -> score += 0.1;
            default -> score += 0.05;
        }

        // Add random variance to simulate ML model behavior
        score += Math.random() * 0.2;

        return Math.min(score, 1.0);
    }
}
