package com.anshubank.repository;

import com.anshubank.database.DatabaseManager;
import com.anshubank.enums.AccountStatus;
import com.anshubank.enums.AccountType;
import com.anshubank.model.Account;
import com.anshubank.model.CurrentAccount;
import com.anshubank.model.SavingsAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /**
     * Converts a database ResultSet row into the appropriate Account object.
     */
    private Account map(ResultSet resultSet) throws SQLException {

        AccountType accountType =
                AccountType.valueOf(resultSet.getString("account_type"));

        String accountId = resultSet.getString("account_id");
        String customerId = resultSet.getString("customer_id");
        BigDecimal balance = resultSet.getBigDecimal("balance");

        AccountStatus status =
                AccountStatus.valueOf(resultSet.getString("status"));

        Timestamp createdTimestamp =
                resultSet.getTimestamp("created_at");

        if (accountType == AccountType.SAVINGS) {

            return new SavingsAccount(
                    accountId,
                    customerId,
                    balance,
                    status,
                    createdTimestamp.toLocalDateTime()
            );

        } else {

            return new CurrentAccount(
                    accountId,
                    customerId,
                    balance,
                    status,
                    createdTimestamp.toLocalDateTime()
            );
        }
    }

    /**
     * Finds an account using its account ID.
     */
    public Optional<Account> findById(String id) throws SQLException {

        String sql =
                "SELECT * FROM accounts WHERE account_id = ?";

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    /**
     * Returns all accounts.
     */
    public List<Account> findAll() throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts ORDER BY account_id";

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                accounts.add(map(resultSet));
            }
        }

        return accounts;
    }

    /**
     * Finds all accounts belonging to a customer.
     */
    public List<Account> findByCustomer(String customerId)
            throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts WHERE customer_id = ?";

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    accounts.add(map(resultSet));
                }
            }
        }

        return accounts;
    }

    /**
     * Creates a new account.
     */
    public void create(Account account) throws SQLException {

        String sql = """
                INSERT INTO accounts
                (account_id, customer_id, account_type, balance, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, account.getAccountId());
            statement.setString(2, account.getCustomerId());
            statement.setString(3, account.getAccountType().name());
            statement.setBigDecimal(4, account.getBalance());
            statement.setString(5, account.getStatus().name());
            statement.setTimestamp(
                    6,
                    Timestamp.valueOf(account.getCreatedAt())
            );

            statement.executeUpdate();
        }
    }

    /**
     * Updates an account balance using an existing database connection.
     * This is important for atomic banking transactions.
     */
    public void updateBalance(
            Connection connection,
            String accountId,
            BigDecimal newBalance
    ) throws SQLException {

        String sql =
                "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, newBalance);
            statement.setString(2, accountId);

            statement.executeUpdate();
        }
    }

    /**
     * Updates the status of an account.
     */
    public void updateStatus(
            String accountId,
            AccountStatus status
    ) throws SQLException {

        String sql =
                "UPDATE accounts SET status = ? WHERE account_id = ?";

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, status.name());
            statement.setString(2, accountId);

            statement.executeUpdate();
        }
    }

    /**
     * Searches accounts by account ID, customer ID,
     * account type, or account status.
     */
    public List<Account> search(String term) throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String sql = """
                SELECT *
                FROM accounts
                WHERE UPPER(account_id) LIKE ?
                   OR UPPER(customer_id) LIKE ?
                   OR UPPER(account_type) LIKE ?
                   OR UPPER(status) LIKE ?
                ORDER BY account_id
                """;

        String searchTerm = "%" + term.toUpperCase() + "%";

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            for (int i = 1; i <= 4; i++) {
                statement.setString(i, searchTerm);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    accounts.add(map(resultSet));
                }
            }
        }

        return accounts;
    }
}