package com.fintech.banking.dto;

import com.fintech.banking.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AccountDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountResponse {
        private UUID id;
        private String accountNumber;
        private String accountNumberMasked;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private String currency;
        private Account.AccountStatus status;
        private String ifscCode;
        private String branchName;
        private BigDecimal dailyLimit;
        private BigDecimal perTransactionLimit;
        private BigDecimal interestRate;
        private BigDecimal minimumBalance;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private UUID id;
        private String accountNumber;
        private String accountNumberMasked;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private String currency;
        private Account.AccountStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountListResponse {
        private List<AccountSummary> accounts;
        private BigDecimal totalBalance;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountStatementRequest {
        private String accountNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String format; // PDF, CSV, XLSX
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountStatementResponse {
        private String accountNumber;
        private String accountHolderName;
        private LocalDateTime statementPeriodStart;
        private LocalDateTime statementPeriodEnd;
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private List<TransactionDto.TransactionResponse> transactions;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private String downloadUrl;
    }
}
