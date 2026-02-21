package com.payment.initiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.payment.initiation", "com.payment.common"})
@EntityScan("com.payment.common.model")
@EnableJpaRepositories
public class PaymentInitiationApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentInitiationApplication.class, args);
    }
}
