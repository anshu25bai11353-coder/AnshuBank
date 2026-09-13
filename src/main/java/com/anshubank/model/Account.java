package com.anshubank.model;

import com.anshubank.enums.AccountStatus;
import com.anshubank.enums.AccountType;
import com.anshubank.exception.AccountSuspendedException;
import com.anshubank.exception.InsufficientBalanceException;
import com.anshubank.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Account {

    private final String accountId;
    private final String customerId;
    private BigDecimal balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;

    protected Account(
            String accountId,
            String customerId,
            BigDecimal balance,
            AccountStatus status,
            LocalDateTime createdAt
    ) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public synchronized void deposit(BigDecimal amount)
            throws InvalidAmountException, AccountSuspendedException {

        check(amount);
        checkActive();

        balance = balance.add(amount);
    }

    public synchronized void deposit(
            BigDecimal amount,
            String description
    ) throws InvalidAmountException, AccountSuspendedException {

        deposit(amount);
    }

    public synchronized void withdraw(BigDecimal amount)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   AccountSuspendedException {

        check(amount);
        checkActive();

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in " + accountId
            );
        }

        balance = balance.subtract(amount);
    }

    private void check(BigDecimal amount)
            throws InvalidAmountException {

        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(
                    "Amount must be positive"
            );
        }
    }

    private void checkActive()
            throws AccountSuspendedException {

        if (status != AccountStatus.ACTIVE) {
            throw new AccountSuspendedException(
                    "Account " + accountId + " is " + status
            );
        }
    }

    public abstract AccountType getAccountType();

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
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