package com.anshubank.service;

import com.anshubank.enums.RiskLevel;
import com.anshubank.model.Account;
import com.anshubank.model.Transaction;
import com.anshubank.repository.AccountRepository;
import com.anshubank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public FraudDetectionService(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = new AccountRepository();
    }

    /**
     * Performs account-balance-based fraud analysis.
     *
     * The transaction is compared with the customer's
     * own account balance instead of using only fixed
     * transaction limits.
     */
    public RuleResult analyze(
            String accountId,
            BigDecimal amount
    ) throws Exception {

        int score = 0;

        List<String> reasons = new ArrayList<>();

        /*
         * Find the actual account of this user.
         */
        Account account = accountRepository
                .findById(accountId)
                .orElse(null);

        if (account == null) {

            return new RuleResult(
                    0,
                    RiskLevel.LOW,
                    "Account balance could not be checked"
            );
        }

        BigDecimal balance = account.getBalance();

        /*
         * Avoid division by zero.
         */
        if (balance.compareTo(BigDecimal.ZERO) > 0) {

            /*
             * Calculate transaction amount as a
             * percentage of the user's own balance.
             */
            BigDecimal percentage =
                    amount
                            .multiply(new BigDecimal("100"))
                            .divide(
                                    balance,
                                    2,
                                    java.math.RoundingMode.HALF_UP
                            );

            /*
             * More than 80% of current balance.
             * Very unusual transaction.
             */
            if (percentage.compareTo(
                    new BigDecimal("80")
            ) >= 0) {

                score += 60;

                reasons.add(
                        "Transaction is " + percentage
                                + "% of account balance"
                );

            /*
             * Between 50% and 80%.
             */
            } else if (percentage.compareTo(
                    new BigDecimal("50")
            ) >= 0) {

                score += 40;

                reasons.add(
                        "Large transaction relative to account balance ("
                                + percentage + "%)"
                );

            /*
             * Between 30% and 50%.
             */
            } else if (percentage.compareTo(
                    new BigDecimal("30")
            ) >= 0) {

                score += 20;

                reasons.add(
                        "Transaction represents "
                                + percentage
                                + "% of account balance"
                );
            }
        }

        /*
         * Check recent transaction frequency.
         */
        LocalDateTime fiveMinutesAgo =
                LocalDateTime.now().minusMinutes(5);

        int recentTransactions = 0;

        for (Transaction transaction :
                transactionRepository.forAccount(accountId)) {

            if (transaction.timestamp()
                    .isAfter(fiveMinutesAgo)) {

                recentTransactions++;
            }
        }

        /*
         * Three or more transactions in five minutes.
         */
        if (recentTransactions >= 3) {

            score += 25;

            reasons.add(
                    "High transaction frequency"
            );
        }

        /*
         * Five or more transactions in five minutes.
         */
        if (recentTransactions >= 5) {

            score += 20;

            reasons.add(
                    "Repeated suspicious transaction pattern"
            );
        }

        /*
         * Limit score to 100.
         */
        score = Math.min(score, 100);

        RiskLevel level =
                determineRiskLevel(score);

        if (reasons.isEmpty()) {

            reasons.add(
                    "Transaction appears normal for this account"
            );
        }

        String reason =
                "Balance: Rs." + balance
                        + " | "
                        + String.join(" + ", reasons);

        return new RuleResult(
                score,
                level,
                reason
        );
    }

    private RiskLevel determineRiskLevel(
            int score
    ) {

        if (score >= 81) {
            return RiskLevel.CRITICAL;
        }

        if (score >= 61) {
            return RiskLevel.HIGH;
        }

        if (score >= 31) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    public record RuleResult(
            int score,
            RiskLevel level,
            String reason
    ) {
    }
}