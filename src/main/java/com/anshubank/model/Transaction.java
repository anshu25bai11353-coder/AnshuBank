package com.anshubank.model;

import com.anshubank.enums.RiskLevel;
import com.anshubank.enums.TransactionStatus;
import com.anshubank.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        String transactionId,
        String sourceAccountId,
        String destinationAccountId,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime timestamp,
        TransactionStatus status,
        int riskScore,
        RiskLevel riskLevel,
        String description
) {
}