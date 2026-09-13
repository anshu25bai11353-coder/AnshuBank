package com.anshubank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlertEntity {

    @Id
    @Column(name = "alert_id")
    private String alertId;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "rule_triggered")
    private String ruleTriggered;

    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public FraudAlertEntity() {
    }
}