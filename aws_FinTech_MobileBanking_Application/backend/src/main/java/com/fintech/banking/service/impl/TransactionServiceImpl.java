package com.fintech.banking.service.impl;

import com.fintech.banking.dto.TransactionDto.*;
import com.fintech.banking.exception.InsufficientBalanceException;
import com.fintech.banking.exception.ResourceNotFoundException;
import com.fintech.banking.exception.TransactionLimitException;
import com.fintech.banking.model.Account;
import com.fintech.banking.model.Beneficiary;
import com.fintech.banking.model.Transaction;
import com.fintech.banking.model.Transaction.TransactionStatus;
import com.fintech.banking.model.Transaction.TransactionType;
import com.fintech.banking.repository.AccountRepository;
import com.fintech.banking.repository.BeneficiaryRepository;
import com.fintech.banking.repository.TransactionRepository;
import com.fintech.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${banking.transaction.daily-limit:50000}")
    private BigDecimal dailyTransactionLimit;

    @Value("${banking.transaction.single-limit:10000}")
    private BigDecimal singleTransactionLimit;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse initiateTransfer(UUID userId, TransferRequest request) {
        log.info("Initiating transfer from account {} to {}, amount: {}", 
                request.fromAccountNumber(), request.toAccountNumber(), request.amount());

        // Validate amount
        validateTransactionAmount(request.amount());

        // Get source account with pessimistic lock
        Account fromAccount = accountRepository.findByAccountNumberForUpdate(request.fromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "number", request.fromAccountNumber()));

        // Validate ownership
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to account");
        }

        // Get destination account
        Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "number", request.toAccountNumber()));

        // Check balance
        if (fromAccount.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + fromAccount.getAvailableBalance()
            );
        }

        // Check daily limit
        validateDailyLimit(fromAccount.getId(), request.amount());

        // Create transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .amount(request.amount())
                .currency(fromAccount.getCurrency())
                .fromAccount(fromAccount)
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccount(toAccount)
                .toAccountNumber(toAccount.getAccountNumber())
                .description(request.description())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Process transfer
        processTransfer(fromAccount, toAccount, savedTransaction);

        log.info("Transfer completed successfully. Reference: {}", savedTransaction.getReferenceNumber());

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse initiatePayment(UUID userId, PaymentRequest request) {
        log.info("Initiating payment from account {} to beneficiary {}, amount: {}", 
                request.fromAccountNumber(), request.beneficiaryId(), request.amount());

        // Validate amount
        validateTransactionAmount(request.amount());

        // Get source account with lock
        Account fromAccount = accountRepository.findByAccountNumberForUpdate(request.fromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "number", request.fromAccountNumber()));

        // Validate ownership
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to account");
        }

        // Get beneficiary
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndUserId(request.beneficiaryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "id", request.beneficiaryId()));

        // Check balance
        if (fromAccount.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + fromAccount.getAvailableBalance()
            );
        }

        // Check daily limit
        validateDailyLimit(fromAccount.getId(), request.amount());

        // Create transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.PENDING)
                .amount(request.amount())
                .currency(fromAccount.getCurrency())
                .fromAccount(fromAccount)
                .fromAccountNumber(fromAccount.getAccountNumber())
                .toAccountNumber(beneficiary.getAccountNumber())
                .beneficiary(beneficiary)
                .description(request.description())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Debit source account
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(request.amount()));
        accountRepository.save(fromAccount);

        // Mark as completed (in real scenario, would integrate with payment gateway)
        savedTransaction.setStatus(TransactionStatus.COMPLETED);
        savedTransaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(savedTransaction);

        log.info("Payment completed successfully. Reference: {}", savedTransaction.getReferenceNumber());

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId) {
        log.debug("Fetching transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        log.debug("Fetching transaction by reference: {}", referenceNumber);

        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", referenceNumber));

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            UUID accountId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable) {
        
        log.debug("Fetching transaction history for account: {} from {} to {}", 
                accountId, startDate, endDate);

        Page<Transaction> transactions = transactionRepository
                .findByAccountIdAndDateRange(accountId, startDate, endDate, pageable);

        return transactions.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(UUID userId, int limit) {
        log.debug("Fetching recent {} transactions for user: {}", limit, userId);

        List<Transaction> transactions = transactionRepository.findRecentByUserId(userId, limit);

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse cancelTransaction(UUID transactionId, UUID userId) {
        log.info("Cancelling transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        // Validate ownership
        if (!transaction.getFromAccount().getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to transaction");
        }

        // Can only cancel pending transactions
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel transaction with status: " + transaction.getStatus());
        }

        // Refund if amount was debited
        Account fromAccount = transaction.getFromAccount();
        fromAccount.setBalance(fromAccount.getBalance().add(transaction.getAmount()));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().add(transaction.getAmount()));
        accountRepository.save(fromAccount);

        // Update transaction status
        transaction.setStatus(TransactionStatus.CANCELLED);
        Transaction cancelledTransaction = transactionRepository.save(transaction);

        log.info("Transaction cancelled successfully: {}", transactionId);

        return mapToResponse(cancelledTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getTransactionSummary(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Generating transaction summary for user: {} from {} to {}", userId, startDate, endDate);

        List<Account> userAccounts = accountRepository.findByUserIdAndActiveTrue(userId);
        List<UUID> accountIds = userAccounts.stream().map(Account::getId).collect(Collectors.toList());

        BigDecimal totalIncoming = transactionRepository.sumIncomingTransactions(accountIds, startDate, endDate);
        BigDecimal totalOutgoing = transactionRepository.sumOutgoingTransactions(accountIds, startDate, endDate);
        long transactionCount = transactionRepository.countByAccountIdsAndDateRange(accountIds, startDate, endDate);

        return new TransactionSummaryResponse(
                totalIncoming != null ? totalIncoming : BigDecimal.ZERO,
                totalOutgoing != null ? totalOutgoing : BigDecimal.ZERO,
                transactionCount,
                startDate,
                endDate
        );
    }

    @Async
    @Override
    public void processScheduledTransfer(UUID transactionId) {
        log.info("Processing scheduled transfer: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction {} is not in PENDING status, skipping", transactionId);
            return;
        }

        try {
            Account fromAccount = accountRepository.findByIdForUpdate(transaction.getFromAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", 
                            transaction.getFromAccount().getId()));

            Account toAccount = accountRepository.findById(transaction.getToAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", 
                            transaction.getToAccount().getId()));

            processTransfer(fromAccount, toAccount, transaction);

            log.info("Scheduled transfer processed successfully: {}", transactionId);

        } catch (Exception e) {
            log.error("Failed to process scheduled transfer: {}", transactionId, e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
        }
    }

    private void processTransfer(Account fromAccount, Account toAccount, Transaction transaction) {
        // Debit source account
        fromAccount.setBalance(fromAccount.getBalance().subtract(transaction.getAmount()));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(transaction.getAmount()));
        accountRepository.save(fromAccount);

        // Credit destination account
        toAccount.setBalance(toAccount.getBalance().add(transaction.getAmount()));
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(transaction.getAmount()));
        accountRepository.save(toAccount);

        // Update transaction status
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void validateTransactionAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        if (amount.compareTo(singleTransactionLimit) > 0) {
            throw new TransactionLimitException(
                    "Amount exceeds single transaction limit of " + singleTransactionLimit
            );
        }
    }

    private void validateDailyLimit(UUID accountId, BigDecimal newAmount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        BigDecimal dailyTotal = transactionRepository.sumOutgoingByAccountAndDateRange(
                accountId, startOfDay, endOfDay);

        if (dailyTotal == null) {
            dailyTotal = BigDecimal.ZERO;
        }

        if (dailyTotal.add(newAmount).compareTo(dailyTransactionLimit) > 0) {
            throw new TransactionLimitException(
                    "Transaction would exceed daily limit of " + dailyTransactionLimit + 
                    ". Current daily total: " + dailyTotal
            );
        }
    }

    private String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis() + 
               String.format("%04d", secureRandom.nextInt(10000));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getFromAccountNumber(),
                transaction.getToAccountNumber(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt(),
                transaction.getFailureReason()
        );
    }
}
