package com.payment.common.dto;

import com.payment.common.model.PaymentType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Debtor account is required")
    private String debtorAccount;

    @NotBlank(message = "Creditor account is required")
    private String creditorAccount;

    @NotBlank(message = "Debtor name is required")
    private String debtorName;

    @NotBlank(message = "Creditor name is required")
    private String creditorName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "10000000.00", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private String idempotencyKey;
    private String region;
}
