package com.payment.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.payment.reconciliation", "com.payment.common"})
@EntityScan("com.payment.common.model")
public class PaymentReconciliationApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentReconciliationApplication.class, args);
    }
}
