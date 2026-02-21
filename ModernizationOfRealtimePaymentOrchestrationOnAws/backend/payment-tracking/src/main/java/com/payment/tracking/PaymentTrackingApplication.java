package com.payment.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.payment.tracking", "com.payment.common"})
@EntityScan("com.payment.common.model")
public class PaymentTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentTrackingApplication.class, args);
    }
}
