package com.fintech.banking.service;

import com.fintech.banking.dto.AccountDto;

import java.util.UUID;

public interface AccountService {
    AccountDto.AccountListResponse getAllAccounts(String username);
    AccountDto.AccountResponse getAccountDetails(UUID accountId, String username);
    AccountDto.AccountSummary getAccountBalance(UUID accountId, String username);
    AccountDto.AccountStatementResponse generateStatement(AccountDto.AccountStatementRequest request, String username);
    AccountDto.AccountResponse getAccountByNumber(String accountNumber, String username);
}
