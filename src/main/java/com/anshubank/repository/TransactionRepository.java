package com.anshubank.repository;

import com.anshubank.database.DatabaseManager;
import com.anshubank.enums.RiskLevel;
import com.anshubank.enums.TransactionStatus;
import com.anshubank.enums.TransactionType;
import com.anshubank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /**
     * Converts a database row into a Transaction object.
     */
    private Transaction map(ResultSet resultSet) throws SQLException {

        return new Transaction(
                resultSet.getString("transaction_id"),
                resultSet.getString("source_account_id"),
                resultSet.getString("destination_account_id"),
                TransactionType.valueOf(
                        resultSet.getString("transaction_type")
                ),
                resultSet.getBigDecimal("amount"),
                resultSet.getTimestamp("timestamp")
                        .toLocalDateTime(),
                TransactionStatus.valueOf(
                        resultSet.getString("status")
                ),
                resultSet.getInt("risk_score"),
                RiskLevel.valueOf(
                        resultSet.getString("risk_level")
                ),
                resultSet.getString("description")
        );
    }

    /**
     * Inserts a transaction using an existing database connection.
     *
     * This method is used when the transaction must be part
     * of a larger JDBC database transaction.
     */
    public void insert(
            Connection connection,
            Transaction transaction
    ) throws SQLException {

        String sql = """
                INSERT INTO transactions
                (
                    transaction_id,
                    source_account_id,
                    destination_account_id,
                    transaction_type,
                    amount,
                    timestamp,
                    status,
                    risk_score,
                    risk_level,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    transaction.transactionId()
            );

            statement.setString(
                    2,
                    transaction.sourceAccountId()
            );

            statement.setString(
                    3,
                    transaction.destinationAccountId()
            );

            statement.setString(
                    4,
                    transaction.type().name()
            );

            statement.setBigDecimal(
                    5,
                    transaction.amount()
            );

            statement.setTimestamp(
                    6,
                    Timestamp.valueOf(transaction.timestamp())
            );

            statement.setString(
                    7,
                    transaction.status().name()
            );

            statement.setInt(
                    8,
                    transaction.riskScore()
            );

            statement.setString(
                    9,
                    transaction.riskLevel().name()
            );

            statement.setString(
                    10,
                    transaction.description()
            );

            statement.executeUpdate();
        }
    }

    /**
     * Inserts a transaction using a new database connection.
     */
    public void insert(Transaction transaction) throws SQLException {

        try (Connection connection = db.getConnection()) {
            insert(connection, transaction);
        }
    }

    /**
     * Returns all transactions, newest first.
     */
    public List<Transaction> all() throws SQLException {

        return query(
                "SELECT * FROM transactions ORDER BY timestamp DESC"
        );
    }

    /**
     * Returns all transactions involving a particular account.
     */
    public List<Transaction> forAccount(String accountId)
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String sql = """
                SELECT *
                FROM transactions
                WHERE source_account_id = ?
                   OR destination_account_id = ?
                ORDER BY timestamp DESC
                """;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, accountId);
            statement.setString(2, accountId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    transactions.add(map(resultSet));
                }
            }
        }

        return transactions;
    }

    /**
     * Returns transactions considered suspicious.
     */
    public List<Transaction> suspicious()
            throws SQLException {

        String sql = """
                SELECT *
                FROM transactions
                WHERE risk_level IN ('HIGH', 'CRITICAL')
                   OR status = 'FLAGGED'
                ORDER BY timestamp DESC
                """;

        return query(sql);
    }

    /**
     * Executes a transaction query and maps every row.
     */
    private List<Transaction> query(String sql)
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                transactions.add(map(resultSet));
            }
        }

        return transactions;
    }

    /**
     * Returns the total number of transactions.
     */
    public long count() throws SQLException {

        return scalar(
                "SELECT COUNT(*) FROM transactions"
        );
    }

    /**
     * Counts transactions satisfying a predefined condition.
     *
     * The caller should provide a trusted SQL condition rather
     * than raw user input.
     */
    public long countWhere(String where)
            throws SQLException {

        return scalar(
                "SELECT COUNT(*) FROM transactions WHERE " + where
        );
    }

    /**
     * Executes a scalar COUNT query.
     */
    private long scalar(String sql)
            throws SQLException {

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

            return 0;
        }
    }

    /**
     * Calculates the total amount for a transaction type.
     */
    public BigDecimal sum(String transactionType)
            throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE transaction_type = ?
                  AND status IN
                      ('COMPLETED', 'APPROVED', 'FLAGGED')
                """;

        try (
                Connection connection = db.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, transactionType);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getBigDecimal(1);
                }

                return BigDecimal.ZERO;
            }
        }
    }
}