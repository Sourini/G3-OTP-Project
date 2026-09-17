package otpproju.repository;

import otpproju.config.DatabaseConnection;
import otpproju.model.Flashcard;

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

public class FlashcardRepository {

    public Flashcard create(Flashcard flashcard) {
        Objects.requireNonNull(
                flashcard,
                "Flashcard must not be null"
        );

        String sql = """
                INSERT INTO flashcards (
                    set_id,
                    question,
                    answer
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
                    flashcard.getSetId()
            );

            statement.setString(
                    2,
                    flashcard.getQuestion()
            );

            statement.setString(
                    3,
                    flashcard.getAnswer()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "Creating flashcard affected an " +
                                "unexpected number of rows"
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new RepositoryException(
                            "Database did not return a card ID"
                    );
                }

                int cardId = generatedKeys.getInt(1);

                return findById(cardId).orElseThrow(
                        () -> new RepositoryException(
                                "Created flashcard could " +
                                        "not be retrieved"
                        )
                );
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not create flashcard",
                    exception
            );
        }
    }

    public Optional<Flashcard> findById(int cardId) {
        String sql = """
                SELECT
                    card_id,
                    set_id,
                    question,
                    answer,
                    updated_at,
                    created_at
                FROM flashcards
                WHERE card_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, cardId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapFlashcard(resultSet)
                );
            }

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find flashcard by ID",
                    exception
            );
        }
    }

    public List<Flashcard> findBySetId(int setId) {
        String sql = """
                SELECT
                    card_id,
                    set_id,
                    question,
                    answer,
                    updated_at,
                    created_at
                FROM flashcards
                WHERE set_id = ?
                ORDER BY created_at, card_id
                """;

        List<Flashcard> flashcards = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, setId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    flashcards.add(
                            mapFlashcard(resultSet)
                    );
                }
            }

            return flashcards;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not find flashcards by set ID",
                    exception
            );
        }
    }

    public List<Flashcard> findAll() {
        String sql = """
                SELECT
                    card_id,
                    set_id,
                    question,
                    answer,
                    updated_at,
                    created_at
                FROM flashcards
                ORDER BY created_at, card_id
                """;

        List<Flashcard> flashcards = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                flashcards.add(mapFlashcard(resultSet));
            }

            return flashcards;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not retrieve flashcards",
                    exception
            );
        }
    }

    public boolean update(Flashcard flashcard) {
        Objects.requireNonNull(
                flashcard,
                "Flashcard must not be null"
        );

        if (flashcard.getCardId() == null) {
            throw new IllegalArgumentException(
                    "Card ID must not be null"
            );
        }

        String sql = """
                UPDATE flashcards
                SET
                    question = ?,
                    answer = ?
                WHERE card_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    flashcard.getQuestion()
            );

            statement.setString(
                    2,
                    flashcard.getAnswer()
            );

            statement.setInt(
                    3,
                    flashcard.getCardId()
            );

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not update flashcard",
                    exception
            );
        }
    }

    public boolean deleteById(int cardId) {
        String sql = """
                DELETE FROM flashcards
                WHERE card_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, cardId);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            throw new RepositoryException(
                    "Could not delete flashcard",
                    exception
            );
        }
    }

    private Flashcard mapFlashcard(
            ResultSet resultSet
    ) throws SQLException {

        Timestamp updatedAt =
                resultSet.getTimestamp("updated_at");

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        return new Flashcard(
                resultSet.getInt("card_id"),
                resultSet.getInt("set_id"),
                resultSet.getString("question"),
                resultSet.getString("answer"),
                updatedAt == null
                        ? null
                        : updatedAt.toLocalDateTime(),
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime()
        );
    }
}