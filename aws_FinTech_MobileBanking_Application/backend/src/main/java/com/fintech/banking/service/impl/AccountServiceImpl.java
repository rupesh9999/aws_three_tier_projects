package com.fintech.banking.service.impl;

import com.fintech.banking.dto.AccountDto.*;
import com.fintech.banking.exception.InsufficientBalanceException;
import com.fintech.banking.exception.ResourceNotFoundException;
import com.fintech.banking.model.Account;
import com.fintech.banking.model.Transaction;
import com.fintech.banking.model.User;
import com.fintech.banking.repository.AccountRepository;
import com.fintech.banking.repository.TransactionRepository;
import com.fintech.banking.repository.UserRepository;
import com.fintech.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        log.debug("Fetching accounts for user: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId) {
        log.debug("Fetching account by ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.debug("Fetching account by number: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        log.info("Creating new account for user: {} with type: {}", userId, request.accountType());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.accountType())
                .currency(request.currency() != null ? request.currency() : "USD")
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .user(user)
                .active(true)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountNumber());
        
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID accountId, UpdateAccountRequest request) {
        log.info("Updating account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        if (request.nickname() != null) {
            account.setNickname(request.nickname());
        }
        
        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully: {}", accountId);
        
        return mapToResponse(updatedAccount);
    }

    @Override
    @Transactional
    public void deactivateAccount(UUID accountId) {
        log.info("Deactivating account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot deactivate account with positive balance");
        }
        
        account.setActive(false);
        accountRepository.save(account);
        log.info("Account deactivated successfully: {}", accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getBalance(UUID accountId) {
        log.debug("Fetching balance for account: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        return new AccountBalanceResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getCurrency(),
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {
        log.info("Processing deposit of {} to account: {}", amount, accountId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        account.setBalance(account.getBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .amount(amount)
                .currency(account.getCurrency())
                .toAccount(account)
                .toAccountNumber(account.getAccountNumber())
                .description("Deposit")
                .completedAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        
        log.info("Deposit completed successfully for account: {}", accountId);
    }

    @Override
    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {
        log.info("Processing withdrawal of {} from account: {}", amount, accountId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + account.getAvailableBalance()
            );
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        accountRepository.save(account);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.COMPLETED)
                .amount(amount)
                .currency(account.getCurrency())
                .fromAccount(account)
                .fromAccountNumber(account.getAccountNumber())
                .description("Withdrawal")
                .completedAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        
        log.info("Withdrawal completed successfully from account: {}", accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistory(UUID accountId, Pageable pageable) {
        log.debug("Fetching transaction history for account: {}", accountId);
        
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId);
        }
        
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(UUID userId) {
        log.debug("Generating account summary for user: {}", userId);
        
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAvailable = accounts.stream()
                .map(Account::getAvailableBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new AccountSummaryResponse(
                accounts.size(),
                totalBalance,
                totalAvailable,
                "USD", // Primary currency
                accounts.stream().map(this::mapToResponse).collect(Collectors.toList())
        );
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getNickname(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.getCurrency(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String generateAccountNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis() + 
               String.format("%04d", secureRandom.nextInt(10000));
    }
}
