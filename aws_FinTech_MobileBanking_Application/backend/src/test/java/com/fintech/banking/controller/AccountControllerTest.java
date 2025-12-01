package com.fintech.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.banking.dto.AccountDto;
import com.fintech.banking.security.JwtService;
import com.fintech.banking.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtService jwtService;

    private AccountDto testAccountDto;
    private UUID accountId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testAccountDto = AccountDto.builder()
                .id(accountId)
                .accountNumber("1234567890")
                .accountType("CHECKING")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/accounts")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    class GetAccountsTests {

        @Test
        @DisplayName("Should return list of accounts")
        void shouldReturnListOfAccounts() throws Exception {
            when(accountService.getAccountsByUser(any(UUID.class)))
                    .thenReturn(Arrays.asList(testAccountDto));

            mockMvc.perform(get("/api/v1/accounts")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                    .andExpect(jsonPath("$[0].balance").value(1000.00));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{id}")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    class GetAccountByIdTests {

        @Test
        @DisplayName("Should return account when exists")
        void shouldReturnAccountWhenExists() throws Exception {
            when(accountService.getAccountById(accountId))
                    .thenReturn(testAccountDto);

            mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                    .andExpect(jsonPath("$.accountType").value("CHECKING"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    class CreateAccountTests {

        @Test
        @DisplayName("Should create account successfully")
        void shouldCreateAccountSuccessfully() throws Exception {
            AccountDto.CreateAccountRequest request = AccountDto.CreateAccountRequest.builder()
                    .accountType("CHECKING")
                    .currency("USD")
                    .build();

            when(accountService.createAccount(any(UUID.class), any(AccountDto.CreateAccountRequest.class)))
                    .thenReturn(testAccountDto);

            mockMvc.perform(post("/api/v1/accounts")
                            .with(csrf())
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountNumber").value("1234567890"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{id}/balance")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    class GetBalanceTests {

        @Test
        @DisplayName("Should return account balance")
        void shouldReturnAccountBalance() throws Exception {
            when(accountService.getAccountBalance(accountId))
                    .thenReturn(new BigDecimal("1000.00"));

            mockMvc.perform(get("/api/v1/accounts/{id}/balance", accountId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1000.00"));
        }
    }
}
