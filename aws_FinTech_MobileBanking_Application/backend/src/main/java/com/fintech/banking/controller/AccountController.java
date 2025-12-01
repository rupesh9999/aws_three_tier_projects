package com.fintech.banking.controller;

import com.fintech.banking.dto.AccountDto;
import com.fintech.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CUSTOMER')")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieve all accounts for authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccountDto.AccountListResponse> getAllAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAllAccounts(userDetails.getUsername()));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details", description = "Retrieve details of a specific account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account details retrieved"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AccountDto.AccountResponse> getAccountDetails(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccountDetails(accountId, userDetails.getUsername()));
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Retrieve current balance of an account")
    public ResponseEntity<AccountDto.AccountSummary> getAccountBalance(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccountBalance(accountId, userDetails.getUsername()));
    }

    @PostMapping("/statement")
    @Operation(summary = "Generate account statement", 
               description = "Generate and download account statement for a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statement generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDto.AccountStatementResponse> generateStatement(
            @RequestBody AccountDto.AccountStatementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.generateStatement(request, userDetails.getUsername()));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by number", description = "Retrieve account details by account number")
    public ResponseEntity<AccountDto.AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber, userDetails.getUsername()));
    }
}
