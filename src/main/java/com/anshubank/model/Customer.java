package com.anshubank.model;

import com.anshubank.enums.AccountStatus;

import java.time.LocalDateTime;

public class Customer {

    private final String customerId;
    private final String name;
    private final String email;
    private final String phone;
    private final String passwordHash;

    private AccountStatus status = AccountStatus.ACTIVE;

    private final LocalDateTime createdAt;

    public Customer(
            String id,
            String name,
            String email,
            String phone,
            String passwordHash
    ) {
        this.customerId = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}