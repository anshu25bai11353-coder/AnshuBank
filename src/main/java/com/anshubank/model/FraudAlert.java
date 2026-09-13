package com.anshubank.model;

import com.anshubank.enums.AlertStatus;
import com.anshubank.enums.RiskLevel;

import java.time.LocalDateTime;

public record FraudAlert(
        String alertId,
        String transactionId,
        String ruleTriggered,
        int riskScore,
        RiskLevel riskLevel,
        AlertStatus reviewStatus,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}