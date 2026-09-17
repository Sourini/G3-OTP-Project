package otpproju.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getRequiredValue("DB_URL"),
                getRequiredValue("DB_USER"),
                getRequiredValue("DB_PASSWORD")
        );
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException | IllegalStateException exception) {
            return false;
        }
    }

    private static String getRequiredValue(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            value = DOTENV.get(name);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }

        return value;
    }
}