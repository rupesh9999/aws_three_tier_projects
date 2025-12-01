package com.fintech.banking.controller;

import com.fintech.banking.dto.TransactionDto;
import com.fintech.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction and transfer endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Initiate fund transfer", description = "Transfer funds between accounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "402", description = "Insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Transfer limit exceeded")
    })
    public ResponseEntity<TransactionDto.TransferResponse> initiateTransfer(
            @Valid @RequestBody TransactionDto.TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.initiateTransfer(request, userDetails.getUsername()));
    }

    @PostMapping("/transfer/verify")
    @Operation(summary = "Verify transfer OTP", description = "Complete transfer with OTP verification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid OTP"),
            @ApiResponse(responseCode = "410", description = "OTP expired")
    })
    public ResponseEntity<TransactionDto.TransferResponse> verifyTransfer(
            @Valid @RequestBody TransactionDto.OtpVerification request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.verifyAndCompleteTransfer(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Get transaction history", description = "Retrieve paginated transaction history")
    public ResponseEntity<Page<TransactionDto.TransactionResponse>> getTransactionHistory(
            @ModelAttribute TransactionDto.TransactionFilter filter,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(filter, userDetails.getUsername()));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction details", description = "Retrieve details of a specific transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction details retrieved"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionDto.TransactionResponse> getTransactionDetails(
            @PathVariable UUID transactionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getTransactionDetails(transactionId, userDetails.getUsername()));
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference", 
               description = "Retrieve transaction details by reference number")
    public ResponseEntity<TransactionDto.TransactionResponse> getTransactionByReference(
            @PathVariable String referenceNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getTransactionByReference(referenceNumber, userDetails.getUsername()));
    }

    @GetMapping("/account/{accountId}/recent")
    @Operation(summary = "Get recent transactions", 
               description = "Retrieve last 10 transactions for an account")
    public ResponseEntity<java.util.List<TransactionDto.TransactionResponse>> getRecentTransactions(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getRecentTransactions(accountId, userDetails.getUsername()));
    }
}
