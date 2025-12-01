package com.fintech.banking.service;

import com.fintech.banking.dto.TransactionDto;
import com.fintech.banking.exception.InsufficientBalanceException;
import com.fintech.banking.exception.ResourceNotFoundException;
import com.fintech.banking.model.Account;
import com.fintech.banking.model.Transaction;
import com.fintech.banking.model.User;
import com.fintech.banking.repository.AccountRepository;
import com.fintech.banking.repository.TransactionRepository;
import com.fintech.banking.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction testTransaction;
    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");

        sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setAccountNumber("1234567890");
        sourceAccount.setAccountType(Account.AccountType.CHECKING);
        sourceAccount.setBalance(new BigDecimal("5000.00"));
        sourceAccount.setCurrency("USD");
        sourceAccount.setStatus(Account.AccountStatus.ACTIVE);
        sourceAccount.setUser(testUser);

        destinationAccount = new Account();
        destinationAccount.setId(destinationAccountId);
        destinationAccount.setAccountNumber("0987654321");
        destinationAccount.setAccountType(Account.AccountType.SAVINGS);
        destinationAccount.setBalance(new BigDecimal("2000.00"));
        destinationAccount.setCurrency("USD");
        destinationAccount.setStatus(Account.AccountStatus.ACTIVE);
        destinationAccount.setUser(testUser);

        testTransaction = new Transaction();
        testTransaction.setId(transactionId);
        testTransaction.setSourceAccount(sourceAccount);
        testTransaction.setDestinationAccount(destinationAccount);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(Transaction.TransactionType.TRANSFER);
        testTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        testTransaction.setDescription("Test transfer");
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("transfer tests")
    class TransferTests {

        @Test
        @DisplayName("Should transfer money successfully")
        void shouldTransferSuccessfully() {
            TransactionDto.TransferRequest request = TransactionDto.TransferRequest.builder()
                    .sourceAccountId(sourceAccountId)
                    .destinationAccountId(destinationAccountId)
                    .amount(new BigDecimal("100.00"))
                    .description("Test transfer")
                    .build();

            when(accountRepository.findById(sourceAccountId))
                    .thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(testTransaction);

            TransactionDto result = transactionService.transfer(request);

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            verify(accountRepository, times(2)).save(any(Account.class));
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when source account not found")
        void shouldThrowExceptionWhenSourceAccountNotFound() {
            TransactionDto.TransferRequest request = TransactionDto.TransferRequest.builder()
                    .sourceAccountId(sourceAccountId)
                    .destinationAccountId(destinationAccountId)
                    .amount(new BigDecimal("100.00"))
                    .build();

            when(accountRepository.findById(sourceAccountId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Source account not found");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            sourceAccount.setBalance(new BigDecimal("50.00"));

            TransactionDto.TransferRequest request = TransactionDto.TransferRequest.builder()
                    .sourceAccountId(sourceAccountId)
                    .destinationAccountId(destinationAccountId)
                    .amount(new BigDecimal("100.00"))
                    .build();

            when(accountRepository.findById(sourceAccountId))
                    .thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));

            assertThatThrownBy(() -> transactionService.transfer(request))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance");

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTransactionsByAccount tests")
    class GetTransactionsByAccountTests {

        @Test
        @DisplayName("Should return paginated transactions")
        void shouldReturnPaginatedTransactions() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> transactionPage = new PageImpl<>(
                    Arrays.asList(testTransaction),
                    pageable,
                    1
            );

            when(accountRepository.findById(sourceAccountId))
                    .thenReturn(Optional.of(sourceAccount));
            when(transactionRepository.findBySourceAccountOrDestinationAccount(
                    eq(sourceAccount), eq(sourceAccount), any(Pageable.class)))
                    .thenReturn(transactionPage);

            Page<TransactionDto> result = transactionService.getTransactionsByAccount(
                    sourceAccountId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(transactionRepository, times(1))
                    .findBySourceAccountOrDestinationAccount(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("getTransactionById tests")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should return transaction when exists")
        void shouldReturnTransactionWhenExists() {
            when(transactionRepository.findById(transactionId))
                    .thenReturn(Optional.of(testTransaction));

            TransactionDto result = transactionService.getTransactionById(transactionId);

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            verify(transactionRepository, times(1)).findById(transactionId);
        }

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            when(transactionRepository.findById(transactionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction not found");
        }
    }
}
