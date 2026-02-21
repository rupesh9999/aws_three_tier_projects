package com.payment.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_EXECUTED = "payment-executed";
    public static final String PAYMENT_SETTLED = "payment-settled";
    public static final String PAYMENT_TRACKING = "payment-tracking";
    public static final String PAYMENT_BILLING = "payment-billing";
    public static final String RISK_ASSESSED = "risk-assessed";
    public static final String PAYMENT_NOTIFICATION = "payment-notification";
    public static final String PAYMENT_EXECUTION_DLQ = "payment-execution-dlq";

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replicas:1}")
    private int replicas;

    @Bean
    public NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(PAYMENT_INITIATED).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentExecutedTopic() {
        return TopicBuilder.name(PAYMENT_EXECUTED).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentSettledTopic() {
        return TopicBuilder.name(PAYMENT_SETTLED).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentTrackingTopic() {
        return TopicBuilder.name(PAYMENT_TRACKING).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentBillingTopic() {
        return TopicBuilder.name(PAYMENT_BILLING).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic riskAssessedTopic() {
        return TopicBuilder.name(RISK_ASSESSED).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentNotificationTopic() {
        return TopicBuilder.name(PAYMENT_NOTIFICATION).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic paymentExecutionDlqTopic() {
        return TopicBuilder.name(PAYMENT_EXECUTION_DLQ).partitions(partitions).replicas(replicas).build();
    }
}

