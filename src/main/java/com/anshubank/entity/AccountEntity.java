package com.anshubank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Column(name = "account_type")
    private String accountType;

    private BigDecimal balance;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "sourceAccount")
    private List<TransactionEntity> outgoing = new ArrayList<>();

    public AccountEntity() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String value) {
        accountId = value;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity value) {
        customer = value;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String value) {
        accountType = value;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal value) {
        balance = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime value) {
        createdAt = value;
    }
}