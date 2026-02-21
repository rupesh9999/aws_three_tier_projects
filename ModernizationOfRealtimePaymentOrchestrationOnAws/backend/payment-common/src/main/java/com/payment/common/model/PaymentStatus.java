package com.payment.common.model;

public enum PaymentStatus {
    INITIATED,
    RISK_SCREENING,
    RISK_APPROVED,
    RISK_REJECTED,
    EXECUTING,
    EXECUTED,
    SETTLED,
    RECONCILED,
    BILLED,
    COMPLETED,
    FAILED,
    REVERSED
}
