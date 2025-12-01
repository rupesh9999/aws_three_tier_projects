package com.fintech.banking.service;

import com.fintech.banking.dto.AccountDto;
import com.fintech.banking.exception.ResourceNotFoundException;
import com.fintech.banking.model.Account;
import com.fintech.banking.model.User;
import com.fintech.banking.repository.AccountRepository;
import com.fintech.banking.repository.UserRepository;
import com.fintech.banking.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private Account testAccount;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testAccount = new Account();
        testAccount.setId(accountId);
        testAccount.setAccountNumber("1234567890");
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrency("USD");
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
        testAccount.setUser(testUser);
        testAccount.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getAccountsByUser tests")
    class GetAccountsByUserTests {

        @Test
        @DisplayName("Should return list of accounts for valid user")
        void shouldReturnAccountsForValidUser() {
            Account secondAccount = new Account();
            secondAccount.setId(UUID.randomUUID());
            secondAccount.setAccountNumber("0987654321");
            secondAccount.setAccountType(Account.AccountType.SAVINGS);
            secondAccount.setBalance(new BigDecimal("5000.00"));
            secondAccount.setCurrency("USD");
            secondAccount.setStatus(Account.AccountStatus.ACTIVE);
            secondAccount.setUser(testUser);

            when(accountRepository.findByUserId(userId))
                    .thenReturn(Arrays.asList(testAccount, secondAccount));

            List<AccountDto> result = accountService.getAccountsByUser(userId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAccountNumber()).isEqualTo("1234567890");
            assertThat(result.get(1).getAccountNumber()).isEqualTo("0987654321");
            verify(accountRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no accounts")
        void shouldReturnEmptyListWhenNoAccounts() {
            when(accountRepository.findByUserId(userId))
                    .thenReturn(List.of());

            List<AccountDto> result = accountService.getAccountsByUser(userId);

            assertThat(result).isEmpty();
            verify(accountRepository, times(1)).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("getAccountById tests")
    class GetAccountByIdTests {

        @Test
        @DisplayName("Should return account when exists")
        void shouldReturnAccountWhenExists() {
            when(accountRepository.findById(accountId))
                    .thenReturn(Optional.of(testAccount));

            AccountDto result = accountService.getAccountById(accountId);

            assertThat(result).isNotNull();
            assertThat(result.getAccountNumber()).isEqualTo("1234567890");
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            verify(accountRepository, times(1)).findById(accountId);
        }

        @Test
        @DisplayName("Should throw exception when account not found")
        void shouldThrowExceptionWhenAccountNotFound() {
            when(accountRepository.findById(accountId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById(accountId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found");

            verify(accountRepository, times(1)).findById(accountId);
        }
    }

    @Nested
    @DisplayName("createAccount tests")
    class CreateAccountTests {

        @Test
        @DisplayName("Should create account successfully")
        void shouldCreateAccountSuccessfully() {
            AccountDto.CreateAccountRequest request = AccountDto.CreateAccountRequest.builder()
                    .accountType("CHECKING")
                    .currency("USD")
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(testUser));
            when(accountRepository.save(any(Account.class)))
                    .thenReturn(testAccount);

            AccountDto result = accountService.createAccount(userId, request);

            assertThat(result).isNotNull();
            verify(userRepository, times(1)).findById(userId);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            AccountDto.CreateAccountRequest request = AccountDto.CreateAccountRequest.builder()
                    .accountType("CHECKING")
                    .currency("USD")
                    .build();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAccountBalance tests")
    class GetAccountBalanceTests {

        @Test
        @DisplayName("Should return account balance")
        void shouldReturnAccountBalance() {
            when(accountRepository.findById(accountId))
                    .thenReturn(Optional.of(testAccount));

            BigDecimal balance = accountService.getAccountBalance(accountId);

            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
            verify(accountRepository, times(1)).findById(accountId);
        }
    }
}
