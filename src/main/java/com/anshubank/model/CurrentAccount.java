package com.anshubank.model;

import com.anshubank.enums.AccountStatus;
import com.anshubank.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CurrentAccount extends Account {

    public CurrentAccount(
            String id,
            String customerId,
            BigDecimal balance,
            AccountStatus status,
            LocalDateTime createdAt
    ) {
        super(
                id,
                customerId,
                balance,
                status,
                createdAt
        );
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CURRENT;
    }
}