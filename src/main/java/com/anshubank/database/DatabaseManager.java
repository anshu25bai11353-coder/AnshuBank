package com.anshubank.database;

import com.anshubank.security.SecurityUtil;
import com.anshubank.util.LoggerUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class DatabaseManager {

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private static final String URL =
            "jdbc:h2:./data/anshubank;AUTO_SERVER=TRUE";

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {

        new File("data").mkdirs();

        return DriverManager.getConnection(
                URL,
                "sa",
                ""
        );
    }

    public void initialize() {

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {

            try (
                    InputStream inputStream =
                            DatabaseManager.class.getResourceAsStream("/schema.sql")
            ) {

                if (inputStream == null) {
                    throw new IOException(
                            "Classpath schema.sql not found"
                    );
                }

                String sql = new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                for (String part : sql.split(";")) {

                    if (!part.isBlank()) {
                        statement.execute(part);
                    }
                }
            }

            seed(connection);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Database initialization failed",
                    e
            );
        }
    }

    private void seed(Connection connection) throws SQLException {

        // Seed Admin
        try (
                PreparedStatement query = connection.prepareStatement(
                        "MERGE INTO admins " +
                        "(admin_id, password_hash, status, created_at) " +
                        "KEY(admin_id) " +
                        "VALUES(?,?,?,?)"
                )
        ) {

            query.setString(1, "admin");

            query.setString(
                    2,
                    SecurityUtil.hash("AnshuBank@123")
            );

            query.setString(3, "ACTIVE");

            query.setTimestamp(
                    4,
                    Timestamp.valueOf(LocalDateTime.now())
            );

            query.executeUpdate();
        }

        // Seed Demo Customer
        try (
                PreparedStatement query = connection.prepareStatement(
                        "MERGE INTO customers " +
                        "(customer_id, name, email, phone, password_hash, status, created_at) " +
                        "KEY(customer_id) " +
                        "VALUES(?,?,?,?,?,?,?)"
                )
        ) {

            query.setString(1, "C1001");
            query.setString(2, "Demo Customer");
            query.setString(3, "demo@anshubank.local");
            query.setString(4, "9876543210");

            query.setString(
                    5,
                    SecurityUtil.hash("Customer@123")
            );

            query.setString(6, "ACTIVE");

            query.setTimestamp(
                    7,
                    Timestamp.valueOf(LocalDateTime.now())
            );

            query.executeUpdate();
        }

        // Seed Demo Account
        try (
                PreparedStatement query = connection.prepareStatement(
                        "MERGE INTO accounts " +
                        "(account_id, customer_id, account_type, balance, status, created_at) " +
                        "KEY(account_id) " +
                        "VALUES(?,?,?,?,?,?)"
                )
        ) {

            query.setString(1, "SB10001");
            query.setString(2, "C1001");
            query.setString(3, "SAVINGS");

            query.setBigDecimal(
                    4,
                    new java.math.BigDecimal("50000.00")
            );

            query.setString(5, "ACTIVE");

            query.setTimestamp(
                    6,
                    Timestamp.valueOf(LocalDateTime.now())
            );

            query.executeUpdate();
        }

        LoggerUtil.info("Database initialized");
    }

    public void withTransaction(SQLConsumer consumer)
            throws SQLException {

        try (Connection connection = getConnection()) {

            boolean oldAutoCommit = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {

                consumer.accept(connection);

                connection.commit();

            } catch (Exception e) {

                connection.rollback();

                throw e instanceof SQLException sqlException
                        ? sqlException
                        : new SQLException(e);

            } finally {

                connection.setAutoCommit(oldAutoCommit);
            }
        }
    }

    @FunctionalInterface
    public interface SQLConsumer {

        void accept(Connection connection)
                throws Exception;
    }
}