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
import java.util.Map;
import otpproju.model.SetText;
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

    public FlashcardSet createMultilingual(int userId, String defaultLanguage, Map<String, SetText> texts) {
        int id = JdbcWrites.inTransaction(connection -> {
            SetText original = texts.get(defaultLanguage);
            int setId = JdbcWrites.insert(connection, """
                    INSERT INTO flashcard_sets (user_id, title, description, default_language_code)
                    VALUES (?, ?, ?, ?)
                    """, userId, original.title(), original.description(), defaultLanguage);
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO flashcard_set_translations (set_id, language_code, title, description)
                    VALUES (?, ?, ?, ?)
                    """)) {
                for (var entry : texts.entrySet()) {
                    statement.setInt(1, setId);
                    statement.setString(2, entry.getKey());
                    statement.setString(3, entry.getValue().title());
                    statement.setString(4, entry.getValue().description());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return setId;
        });
        return findById(id).orElseThrow();
    }

    public Optional<SetText> findTranslation(int setId, String language) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT title, description FROM flashcard_set_translations
                     WHERE set_id = ? AND language_code = ?
                     """)) {
            statement.setInt(1, setId);
            statement.setString(2, language);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next()
                        ? Optional.of(new SetText(rows.getString("title"), rows.getString("description")))
                        : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load set translation", ex);
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
                    created_at, default_language_code,
                    (SELECT GROUP_CONCAT(t.language_code ORDER BY t.language_code)
                     FROM flashcard_set_translations t
                     WHERE t.set_id = flashcard_sets.set_id) AS supported_languages
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
                    created_at, default_language_code,
                    (SELECT GROUP_CONCAT(t.language_code ORDER BY t.language_code)
                     FROM flashcard_set_translations t
                     WHERE t.set_id = flashcard_sets.set_id) AS supported_languages
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
                    created_at, default_language_code,
                    (SELECT GROUP_CONCAT(t.language_code ORDER BY t.language_code)
                     FROM flashcard_set_translations t
                     WHERE t.set_id = flashcard_sets.set_id) AS supported_languages
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

        FlashcardSet set = new FlashcardSet(
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
        String languages = resultSet.getString("supported_languages");
        set.setLanguages(resultSet.getString("default_language_code"),
                languages == null ? List.of() : List.of(languages.split(",")));
        return set;
    }
}