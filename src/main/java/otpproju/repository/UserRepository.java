package otpproju.repository;

import otpproju.config.DatabaseConnection;
import otpproju.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserRepository {

    public User create(User user) {
        Objects.requireNonNull(user, "User must not be null");

        String sql = """
                INSERT INTO users (
                    username,
                    email,
                    password,
                    usertype
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(
                    4,
                    user.getUserType().toDatabaseValue()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "Creating user affected an unexpected number of rows"
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new RepositoryException(
                            "Database did not return a user ID"
                    );
                }

                int userId = generatedKeys.getInt(1);

                return findById(userId).orElseThrow(
                        () -> new RepositoryException(
                                "Created user could not be retrieved"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not create user",
                    exception
            );
        }
    }

    public Optional<User> findById(int userId) {
        String sql = """
                SELECT
                    user_id,
                    username,
                    email,
                    password,
                    usertype,
                    created_at
                FROM users
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(resultSet));
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find user by ID",
                    exception
            );
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT
                    user_id,
                    username,
                    email,
                    password,
                    usertype,
                    created_at
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(resultSet));
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find user by username",
                    exception
            );
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT
                    user_id,
                    username,
                    email,
                    password,
                    usertype,
                    created_at
                FROM users
                WHERE email = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(resultSet));
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find user by email",
                    exception
            );
        }
    }

    public List<User> findAll() {
        String sql = """
                SELECT
                    user_id,
                    username,
                    email,
                    password,
                    usertype,
                    created_at
                FROM users
                ORDER BY user_id
                """;

        List<User> users = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            return users;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not retrieve users",
                    exception
            );
        }
    }

    public boolean update(User user) {
        Objects.requireNonNull(user, "User must not be null");

        if (user.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID must not be null"
            );
        }

        String sql = """
                UPDATE users
                SET
                    username = ?,
                    email = ?,
                    usertype = ?
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(
                    3,
                    user.getUserType().toDatabaseValue()
            );
            statement.setInt(4, user.getUserId());

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not update user",
                    exception
            );
        }
    }

    public boolean updatePasswordHash(
            int userId,
            String passwordHash
    ) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not update password",
                    exception
            );
        }
    }

    public boolean deleteById(int userId) {
        String sql = """
                DELETE FROM users
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not delete user",
                    exception
            );
        }
    }

    private User mapUser(ResultSet resultSet)
            throws SQLException {

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                User.UserType.fromDatabaseValue(
                        resultSet.getString("usertype")
                ),
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime()
        );
    }
}