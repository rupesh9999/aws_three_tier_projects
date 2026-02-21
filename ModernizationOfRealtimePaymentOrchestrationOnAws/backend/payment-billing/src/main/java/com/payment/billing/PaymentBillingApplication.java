package com.payment.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.payment.billing", "com.payment.common"})
@EntityScan("com.payment.common.model")
public class PaymentBillingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentBillingApplication.class, args);
    }
}
