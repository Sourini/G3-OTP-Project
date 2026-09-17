package otpproju.config;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DatabaseConnectionTest {

    @Test
    void connectsToMariaDbSuccessfully() {
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            assertFalse(
                    connection.isClosed(),
                    "The database connection should be open"
            );

            assertTrue(
                    connection.isValid(2),
                    "MariaDB should report a valid connection"
            );

            System.out.println(
                    "MariaDB connection successful."
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            fail(
                    "MariaDB connection failed: " +
                            exception.getMessage(),
                    exception
            );
        }
    }
}