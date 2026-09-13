package com.anshubank.service;

import com.anshubank.database.DatabaseManager;
import com.anshubank.enums.AlertStatus;
import com.anshubank.enums.TransactionStatus;
import com.anshubank.enums.TransactionType;
import com.anshubank.exception.AccountNotFoundException;
import com.anshubank.exception.InvalidAccountException;
import com.anshubank.model.Account;
import com.anshubank.model.FraudAlert;
import com.anshubank.model.Transaction;
import com.anshubank.repository.AccountRepository;
import com.anshubank.repository.FraudAlertRepository;
import com.anshubank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionService {

    private final DatabaseManager db =
            DatabaseManager.getInstance();

    private final AccountRepository accounts =
            new AccountRepository();

    private final TransactionRepository txns =
            new TransactionRepository();

    private final FraudAlertRepository alerts =
            new FraudAlertRepository();

    private final FraudDetectionService fraud =
            new FraudDetectionService(txns);

    /*
     * Account-level locks provide thread safety.
     */
    private static final Map<String, Object> LOCKS =
            new ConcurrentHashMap<>();

    private Object lock(String accountId) {

        return LOCKS.computeIfAbsent(
                accountId,
                key -> new Object()
        );
    }

    public Transaction deposit(
            String accountId,
            BigDecimal amount,
            String description
    ) throws Exception {

        Account account =
                accounts.findById(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        synchronized (lock(accountId)) {

            account.deposit(
                    amount,
                    description
            );

            TransferData data =
                    make(
                            "DEP",
                            accountId,
                            null,
                            TransactionType.DEPOSIT,
                            amount
                    );

            db.withTransaction(connection -> {

                accounts.updateBalance(
                        connection,
                        accountId,
                        account.getBalance()
                );

                txns.insert(
                        connection,
                        data.tx
                );

                if (data.alert != null) {

                    alerts.insert(
                            connection,
                            data.alert
                    );
                }
            });

            showSecurityResult(data.tx);

            return data.tx;
        }
    }

    public Transaction withdraw(
            String accountId,
            BigDecimal amount
    ) throws Exception {

        Account account =
                accounts.findById(accountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                )
                        );

        synchronized (lock(accountId)) {

            account.withdraw(amount);

            TransferData data =
                    make(
                            "WDR",
                            accountId,
                            null,
                            TransactionType.WITHDRAWAL,
                            amount
                    );

            db.withTransaction(connection -> {

                accounts.updateBalance(
                        connection,
                        accountId,
                        account.getBalance()
                );

                txns.insert(
                        connection,
                        data.tx
                );

                if (data.alert != null) {

                    alerts.insert(
                            connection,
                            data.alert
                    );
                }
            });

            showSecurityResult(data.tx);

            return data.tx;
        }
    }

    public List<Transaction> history(
            String accountId
    ) throws SQLException {

        return txns.forAccount(accountId);
    }

    public List<FraudAlert> alertsFor(
        String accountId
) throws SQLException {

    List<FraudAlert> result = new ArrayList<>();

    if (accountId == null || accountId.isBlank()) {
        return result;
    }

    /*
     * Get all transactions related to this account.
     */
    List<Transaction> accountTransactions =
            txns.forAccount(accountId);

    /*
     * Store transaction IDs belonging to this account.
     */
    java.util.Set<String> transactionIds =
            new java.util.HashSet<>();

    for (Transaction transaction : accountTransactions) {

        if (transaction.transactionId() != null) {
            transactionIds.add(
                    transaction.transactionId()
            );
        }
    }

    /*
     * Find fraud alerts belonging to those transactions.
     */
    for (FraudAlert alert : alerts.all()) {

        if (alert.transactionId() != null
                && transactionIds.contains(
                        alert.transactionId()
                )) {

            result.add(alert);
        }
    }

    return result;
}

    public Transaction transfer(
            String from,
            String to,
            BigDecimal amount
    ) throws Exception {

        if (from.equals(to)) {

            throw new InvalidAccountException(
                    "Sender and receiver cannot be the same"
            );
        }

        Account sender =
                accounts.findById(from)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Sender not found"
                                )
                        );

        Account receiver =
                accounts.findById(to)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Receiver not found"
                                )
                        );

        String first =
                from.compareTo(to) < 0
                        ? from
                        : to;

        String second =
                from.compareTo(to) < 0
                        ? to
                        : from;

        synchronized (lock(first)) {

            synchronized (lock(second)) {

                sender.withdraw(amount);

                receiver.deposit(amount);

                TransferData data =
                        make(
                                "TRF",
                                from,
                                to,
                                TransactionType.TRANSFER,
                                amount
                        );

                db.withTransaction(connection -> {

                    accounts.updateBalance(
                            connection,
                            from,
                            sender.getBalance()
                    );

                    accounts.updateBalance(
                            connection,
                            to,
                            receiver.getBalance()
                    );

                    txns.insert(
                            connection,
                            data.tx
                    );

                    if (data.alert != null) {

                        alerts.insert(
                                connection,
                                data.alert
                        );
                    }
                });

                showSecurityResult(data.tx);

                return data.tx;
            }
        }
    }

    /**
     * Performs fraud analysis and creates
     * a fraud alert whenever the risk is HIGH
     * or CRITICAL.
     */
    private TransferData make(
            String prefix,
            String from,
            String to,
            TransactionType type,
            BigDecimal amount
    ) throws Exception {

        FraudDetectionService.RuleResult result =
                fraud.analyze(
                        from,
                        amount
                );

        TransactionStatus status =
                result.score() >= 40
                        ? TransactionStatus.FLAGGED
                        : TransactionStatus.COMPLETED;

        String transactionId =
                prefix
                        + System.currentTimeMillis()
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 4)
                        .toUpperCase();

        Transaction transaction =
                new Transaction(
                        transactionId,
                        from,
                        to,
                        type,
                        amount,
                        LocalDateTime.now(),
                        status,
                        result.score(),
                        result.level(),
                        result.reason()
                );

        FraudAlert alert =
                result.score() >= 40
                        ? new FraudAlert(
                                "FA"
                                        + transactionId
                                        .substring(3),
                                transactionId,
                                result.reason(),
                                result.score(),
                                result.level(),
                                AlertStatus.OPEN,
                                LocalDateTime.now(),
                                null
                        )
                        : null;

        return new TransferData(
                transaction,
                alert
        );
    }

    /**
     * Displays security information after
     * every transaction.
     */
    private void showSecurityResult(
            Transaction transaction
    ) {

        System.out.println();
        System.out.println(
                "========== SECURITY MONITOR =========="
        );

        System.out.println(
                "Transaction ID : "
                        + transaction.transactionId()
        );

        System.out.println(
                "Amount         : Rs."
                        + transaction.amount()
        );

        System.out.println(
                "Risk Score     : "
                        + transaction.riskScore()
                        + "/100"
        );

        System.out.println(
                "Risk Level     : "
                        + transaction.riskLevel()
        );

        System.out.println(
                "Status         : "
                        + transaction.status()
        );

        System.out.println(
                "Security Check : "
                        + transaction.description()
        );

        if (transaction.riskScore() >= 40) {

            System.out.println();
            System.out.println(
                    "!!! FRAUD ALERT GENERATED !!!"
            );

            System.out.println(
                    "Reason: "
                            + transaction.description()
            );

            System.out.println(
                    "Action: Transaction flagged for admin review."
            );

        } else if (transaction.riskScore() >= 31) {

            System.out.println();
            System.out.println(
                    "WARNING: Medium-risk transaction detected."
            );

        } else {

            System.out.println(
                    "Security Check : PASSED"
            );
        }

        System.out.println(
                "======================================"
        );

        System.out.println();
    }

    private record TransferData(
            Transaction tx,
            FraudAlert alert
    ) {
    }
}