package com.fintech.banking.dto;

import com.fintech.banking.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotBlank(message = "From account is required")
        private String fromAccount;

        @NotBlank(message = "To account is required")
        private String toAccount;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Transfer mode is required")
        private Transaction.TransactionMode mode;

        @Size(max = 200, message = "Remarks cannot exceed 200 characters")
        private String remarks;

        private String beneficiaryName;
        private String beneficiaryIfsc;
        private String beneficiaryBank;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferResponse {
        private String referenceNumber;
        private Transaction.TransactionStatus status;
        private BigDecimal amount;
        private String currency;
        private String fromAccount;
        private String toAccount;
        private Transaction.TransactionMode mode;
        private BigDecimal balanceAfter;
        private LocalDateTime timestamp;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private UUID id;
        private String referenceNumber;
        private Transaction.TransactionType transactionType;
        private Transaction.TransactionMode transactionMode;
        private BigDecimal amount;
        private String currency;
        private String description;
        private String remarks;
        private Transaction.TransactionStatus status;
        private String fromAccountMasked;
        private String toAccountMasked;
        private String beneficiaryName;
        private BigDecimal balanceAfter;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionFilter {
        private UUID accountId;
        private Transaction.TransactionType type;
        private Transaction.TransactionMode mode;
        private Transaction.TransactionStatus status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private int page;
        private int size;
        private String sortBy;
        private String sortDirection;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpVerification {
        @NotBlank(message = "Transaction ID is required")
        private String transactionId;

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
        private String otp;
    }
}
