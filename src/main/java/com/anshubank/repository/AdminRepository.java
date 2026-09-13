package com.anshubank.repository;

import com.anshubank.database.DatabaseManager;
import com.anshubank.security.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminRepository {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public boolean authenticate(
            String id,
            String password
    ) throws SQLException {

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT password_hash " +
                        "FROM admins " +
                        "WHERE admin_id = ? " +
                        "AND status = 'ACTIVE'"
                )
        ) {

            preparedStatement.setString(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                return resultSet.next()
                        && SecurityUtil.matches(
                                password,
                                resultSet.getString(1)
                        );
            }
        }
    }
}