package otpproju.repository;

import otpproju.config.DatabaseConnection;
import otpproju.model.FlashcardSet;

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

public class FlashcardSetRepository {

    public FlashcardSet create(FlashcardSet flashcardSet) {
        Objects.requireNonNull(
                flashcardSet,
                "Flashcard set must not be null"
        );

        String sql = """
                INSERT INTO flashcard_sets (
                    user_id,
                    title,
                    description
                )
                VALUES (?, ?, ?)
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
            statement.setInt(
                    1,
                    flashcardSet.getUserId()
            );

            statement.setString(
                    2,
                    flashcardSet.getTitle()
            );

            statement.setString(
                    3,
                    flashcardSet.getDescription()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "Creating flashcard set affected an " +
                                "unexpected number of rows"
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new RepositoryException(
                            "Database did not return a set ID"
                    );
                }

                int setId = generatedKeys.getInt(1);

                return findById(setId).orElseThrow(
                        () -> new RepositoryException(
                                "Created flashcard set could " +
                                        "not be retrieved"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not create flashcard set",
                    exception
            );
        }
    }

    public Optional<FlashcardSet> findById(int setId) {
        String sql = """
                SELECT
                    set_id,
                    user_id,
                    title,
                    description,
                    updated_at,
                    created_at
                FROM flashcard_sets
                WHERE set_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, setId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapFlashcardSet(resultSet)
                );
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find flashcard set by ID",
                    exception
            );
        }
    }

    public List<FlashcardSet> findByUserId(int userId) {
        String sql = """
                SELECT
                    set_id,
                    user_id,
                    title,
                    description,
                    updated_at,
                    created_at
                FROM flashcard_sets
                WHERE user_id = ?
                ORDER BY created_at, set_id
                """;

        List<FlashcardSet> sets = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    sets.add(mapFlashcardSet(resultSet));
                }
            }

            return sets;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find flashcard sets by user ID",
                    exception
            );
        }
    }

    public List<FlashcardSet> findAll() {
        String sql = """
                SELECT
                    set_id,
                    user_id,
                    title,
                    description,
                    updated_at,
                    created_at
                FROM flashcard_sets
                ORDER BY created_at, set_id
                """;

        List<FlashcardSet> sets = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                sets.add(mapFlashcardSet(resultSet));
            }

            return sets;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not retrieve flashcard sets",
                    exception
            );
        }
    }

    public boolean update(FlashcardSet flashcardSet) {
        Objects.requireNonNull(
                flashcardSet,
                "Flashcard set must not be null"
        );

        if (flashcardSet.getSetId() == null) {
            throw new IllegalArgumentException(
                    "Set ID must not be null"
            );
        }

        String sql = """
                UPDATE flashcard_sets
                SET
                    title = ?,
                    description = ?
                WHERE set_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    flashcardSet.getTitle()
            );

            statement.setString(
                    2,
                    flashcardSet.getDescription()
            );

            statement.setInt(
                    3,
                    flashcardSet.getSetId()
            );

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not update flashcard set",
                    exception
            );
        }
    }

    public boolean deleteById(int setId) {
        String sql = """
                DELETE FROM flashcard_sets
                WHERE set_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, setId);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not delete flashcard set",
                    exception
            );
        }
    }

    private FlashcardSet mapFlashcardSet(
            ResultSet resultSet
    ) throws SQLException {

        Timestamp updatedAt =
                resultSet.getTimestamp("updated_at");

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        return new FlashcardSet(
                resultSet.getInt("set_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                updatedAt == null
                        ? null
                        : updatedAt.toLocalDateTime(),
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime()
        );
    }
}