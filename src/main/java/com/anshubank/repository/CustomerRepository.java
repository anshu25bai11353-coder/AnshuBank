package com.anshubank.repository;

import com.anshubank.database.DatabaseManager;
import com.anshubank.enums.AccountStatus;
import com.anshubank.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Customer> findById(String id) throws SQLException {
        return find("customer_id", id);
    }

    public Optional<Customer> findByEmail(String email) throws SQLException {
        return find("email", email);
    }

    private Optional<Customer> find(
            String column,
            String value
    ) throws SQLException {

        String sql = "SELECT * FROM customers WHERE " + column + "=?";

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(1, value);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }

                return Optional.empty();
            }
        }
    }

    private Customer map(ResultSet resultSet) throws SQLException {

        Customer customer = new Customer(
                resultSet.getString("customer_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("password_hash")
        );

        customer.setStatus(
                AccountStatus.valueOf(
                        resultSet.getString("status")
                )
        );

        return customer;
    }

    public List<Customer> findAll() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "SELECT * FROM customers " +
                                "ORDER BY customer_id"
                        );
                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {
                customers.add(map(resultSet));
            }
        }

        return customers;
    }

    public List<Customer> search(String term) throws SQLException {

        List<Customer> customers = new ArrayList<>();

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "SELECT * FROM customers " +
                                "WHERE customer_id LIKE ? " +
                                "OR name LIKE ? " +
                                "OR email LIKE ? " +
                                "ORDER BY customer_id"
                        )
        ) {

            String searchTerm = "%" + term + "%";

            preparedStatement.setString(1, searchTerm);
            preparedStatement.setString(2, searchTerm);
            preparedStatement.setString(3, searchTerm);

            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    customers.add(map(resultSet));
                }
            }
        }

        return customers;
    }

    public void create(Customer customer) throws SQLException {

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "INSERT INTO customers " +
                                "VALUES(?,?,?,?,?,?,?)"
                        )
        ) {

            preparedStatement.setString(
                    1,
                    customer.getCustomerId()
            );

            preparedStatement.setString(
                    2,
                    customer.getName()
            );

            preparedStatement.setString(
                    3,
                    customer.getEmail()
            );

            preparedStatement.setString(
                    4,
                    customer.getPhone()
            );

            preparedStatement.setString(
                    5,
                    customer.getPasswordHash()
            );

            preparedStatement.setString(
                    6,
                    customer.getStatus().name()
            );

            preparedStatement.setTimestamp(
                    7,
                    Timestamp.valueOf(customer.getCreatedAt())
            );

            preparedStatement.executeUpdate();
        }
    }

    public void updatePassword(
            String id,
            String hash
    ) throws SQLException {

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "UPDATE customers " +
                                "SET password_hash=? " +
                                "WHERE customer_id=?"
                        )
        ) {

            preparedStatement.setString(1, hash);
            preparedStatement.setString(2, id);

            preparedStatement.executeUpdate();
        }
    }
}