package com.fintech.banking.exception;

public class TransactionLimitException extends RuntimeException {
    public TransactionLimitException(String message) {
        super(message);
    }
}
