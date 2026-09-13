package com.anshubank.service;

import com.anshubank.enums.AccountStatus;
import com.anshubank.enums.AlertStatus;
import com.anshubank.model.Account;
import com.anshubank.repository.AccountRepository;
import com.anshubank.repository.CustomerRepository;
import com.anshubank.repository.FraudAlertRepository;
import com.anshubank.repository.TransactionRepository;

import java.sql.SQLException;
import java.util.List;

public class AdminService {

    public final CustomerRepository customers =
            new CustomerRepository();

    public final AccountRepository accounts =
            new AccountRepository();

    public final TransactionRepository transactions =
            new TransactionRepository();

    public final FraudAlertRepository alerts =
            new FraudAlertRepository();

    /**
     * Updates the review status of a fraud alert.
     */
    public void review(String alertId, AlertStatus status)
            throws SQLException {

        alerts.updateStatus(alertId, status);
    }

    /**
     * Generates a system statistics summary for the administrator.
     */
    public String statistics() throws SQLException {

        List<Account> allAccounts = accounts.findAll();

        long activeAccounts = allAccounts.stream()
                .filter(account ->
                        account.getStatus() == AccountStatus.ACTIVE)
                .count();

        long suspendedAccounts = allAccounts.stream()
                .filter(account ->
                        account.getStatus() == AccountStatus.SUSPENDED)
                .count();

        long closedAccounts = allAccounts.stream()
                .filter(account ->
                        account.getStatus() == AccountStatus.CLOSED)
                .count();

        long totalCustomers = customers.findAll().size();

        long totalAccounts = allAccounts.size();

        long totalTransactions = transactions.count();

        long successfulTransactions =
                transactions.countWhere("status = 'COMPLETED'");

        long failedTransactions =
                transactions.countWhere("status = 'FAILED'");

        long suspiciousTransactions =
                transactions.suspicious().size();

        return """
                ========================================
                       ANSHUBANK SYSTEM STATISTICS
                ========================================

                Total Customers          : %d
                Total Accounts           : %d

                Active Accounts          : %d
                Suspended Accounts       : %d
                Closed Accounts          : %d

                Total Transactions       : %d
                Successful Transactions  : %d
                Failed Transactions      : %d
                Suspicious Transactions  : %d

                Total Deposit Amount     : Rs.%s
                Total Withdrawal Amount  : Rs.%s
                Total Transfer Amount    : Rs.%s

                ========================================
                """.formatted(
                totalCustomers,
                totalAccounts,
                activeAccounts,
                suspendedAccounts,
                closedAccounts,
                totalTransactions,
                successfulTransactions,
                failedTransactions,
                suspiciousTransactions,
                transactions.sum("DEPOSIT"),
                transactions.sum("WITHDRAWAL"),
                transactions.sum("TRANSFER")
        );
    }
}