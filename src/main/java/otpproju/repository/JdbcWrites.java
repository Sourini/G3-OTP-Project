package otpproju.repository;

import otpproju.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

final class JdbcWrites {
    private JdbcWrites() {}

    @FunctionalInterface
    interface Work {
        int run(Connection connection) throws SQLException;
    }

    static int inTransaction(Work work) {
        try (var connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id = work.run(connection);
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    ex.addSuppressed(rollback);
                }
                throw ex;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save multilingual content", ex);
        }
    }

    static int insert(Connection connection, String sql, Object... values) throws SQLException {
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert returned no ID");
            }
        }
    }
}
