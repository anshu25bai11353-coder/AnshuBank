package com.anshubank.service;

import com.anshubank.repository.*;
import com.anshubank.model.*;
import com.anshubank.enums.*;
import com.anshubank.security.SecurityUtil;

import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class AccountService {

    private final AccountRepository accounts = new AccountRepository();
    private final CustomerRepository customers = new CustomerRepository();

    public String createCustomerAndAccount(
            String name,
            String email,
            String phone,
            String password,
            AccountType type,
            BigDecimal initial
    ) throws Exception {

        String id = "C" + String.format(
                "%05d",
                Math.abs(UUID.randomUUID().hashCode() % 100000)
        );

        while (customers.findById(id).isPresent()) {
            id = "C" + String.format(
                    "%05d",
                    new Random().nextInt(100000)
            );
        }

        Customer c = new Customer(
                id,
                name,
                email,
                phone,
                SecurityUtil.hash(password)
        );

        customers.create(c);

        String aid = (type == AccountType.SAVINGS ? "SB" : "CA")
                + String.format(
                        "%08d",
                        Math.abs(UUID.randomUUID().hashCode() % 100000000)
                );

        while (accounts.findById(aid).isPresent()) {
            aid = (type == AccountType.SAVINGS ? "SB" : "CA")
                    + String.format(
                            "%08d",
                            new Random().nextInt(100000000)
                    );
        }

        Account a = type == AccountType.SAVINGS
                ? new SavingsAccount(
                        aid,
                        id,
                        initial,
                        AccountStatus.ACTIVE,
                        LocalDateTime.now()
                )
                : new CurrentAccount(
                        aid,
                        id,
                        initial,
                        AccountStatus.ACTIVE,
                        LocalDateTime.now()
                );

        accounts.create(a);

        return id + "|" + aid;
    }

    public List<Account> customerAccounts(String id) throws SQLException {
        return accounts.findByCustomer(id);
    }

    public List<Account> all() throws SQLException {
        return accounts.findAll();
    }

    public List<Account> search(String q) throws SQLException {
        return accounts.search(q);
    }

    public void status(String id, AccountStatus s) throws SQLException {
        Account a = accounts.findById(id).orElseThrow();

        if (a.getStatus() == AccountStatus.CLOSED
                && s != AccountStatus.CLOSED) {
            throw new SQLException(
                    "Closed account cannot be reactivated"
            );
        }

        accounts.updateStatus(id, s);
    }
}