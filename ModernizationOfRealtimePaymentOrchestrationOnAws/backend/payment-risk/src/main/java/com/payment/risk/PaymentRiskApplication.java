package com.payment.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.payment.risk", "com.payment.common"})
@EntityScan("com.payment.common.model")
public class PaymentRiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentRiskApplication.class, args);
    }
}
