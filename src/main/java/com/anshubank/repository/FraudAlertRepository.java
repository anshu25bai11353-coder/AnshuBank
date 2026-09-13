package com.anshubank.repository;

import com.anshubank.database.DatabaseManager;
import com.anshubank.enums.AlertStatus;
import com.anshubank.enums.RiskLevel;
import com.anshubank.model.FraudAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FraudAlertRepository {

    private final DatabaseManager db = DatabaseManager.getInstance();

    private FraudAlert map(ResultSet resultSet) throws SQLException {

        Timestamp reviewedTimestamp =
                resultSet.getTimestamp("reviewed_at");

        return new FraudAlert(
                resultSet.getString("alert_id"),
                resultSet.getString("transaction_id"),
                resultSet.getString("rule_triggered"),
                resultSet.getInt("risk_score"),
                RiskLevel.valueOf(
                        resultSet.getString("risk_level")
                ),
                AlertStatus.valueOf(
                        resultSet.getString("review_status")
                ),
                resultSet.getTimestamp("created_at")
                        .toLocalDateTime(),
                reviewedTimestamp == null
                        ? null
                        : reviewedTimestamp.toLocalDateTime()
        );
    }

    public void insert(
            Connection connection,
            FraudAlert alert
    ) throws SQLException {

        try (
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "INSERT INTO fraud_alerts " +
                                "VALUES(?,?,?,?,?,?,?,?)"
                        )
        ) {

            preparedStatement.setString(
                    1,
                    alert.alertId()
            );

            preparedStatement.setString(
                    2,
                    alert.transactionId()
            );

            preparedStatement.setString(
                    3,
                    alert.ruleTriggered()
            );

            preparedStatement.setInt(
                    4,
                    alert.riskScore()
            );

            preparedStatement.setString(
                    5,
                    alert.riskLevel().name()
            );

            preparedStatement.setString(
                    6,
                    alert.reviewStatus().name()
            );

            preparedStatement.setTimestamp(
                    7,
                    Timestamp.valueOf(alert.createdAt())
            );

            if (alert.reviewedAt() == null) {

                preparedStatement.setNull(
                        8,
                        Types.TIMESTAMP
                );

            } else {

                preparedStatement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                alert.reviewedAt()
                        )
                );
            }

            preparedStatement.executeUpdate();
        }
    }

    public List<FraudAlert> all() throws SQLException {

        List<FraudAlert> alerts = new ArrayList<>();

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "SELECT * FROM fraud_alerts " +
                                "ORDER BY created_at DESC"
                        );
                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {
                alerts.add(map(resultSet));
            }
        }

        return alerts;
    }

    public void updateStatus(
            String id,
            AlertStatus status
    ) throws SQLException {

        try (
                Connection connection = db.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                "UPDATE fraud_alerts " +
                                "SET review_status=?, reviewed_at=? " +
                                "WHERE alert_id=?"
                        )
        ) {

            preparedStatement.setString(
                    1,
                    status.name()
            );

            preparedStatement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            LocalDateTime.now()
                    )
            );

            preparedStatement.setString(
                    3,
                    id
            );

            preparedStatement.executeUpdate();
        }
    }
}