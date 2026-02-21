package com.payment.execution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.payment.execution", "com.payment.common"})
@EntityScan("com.payment.common.model")
public class PaymentExecutionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentExecutionApplication.class, args);
    }
}
