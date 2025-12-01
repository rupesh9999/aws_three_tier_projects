package com.fintech.banking.service;

import com.fintech.banking.dto.TransactionDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionDto.TransferResponse initiateTransfer(TransactionDto.TransferRequest request, String username);
    TransactionDto.TransferResponse verifyAndCompleteTransfer(TransactionDto.OtpVerification request, String username);
    Page<TransactionDto.TransactionResponse> getTransactionHistory(TransactionDto.TransactionFilter filter, String username);
    TransactionDto.TransactionResponse getTransactionDetails(UUID transactionId, String username);
    TransactionDto.TransactionResponse getTransactionByReference(String referenceNumber, String username);
    List<TransactionDto.TransactionResponse> getRecentTransactions(UUID accountId, String username);
}
