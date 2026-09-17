package otpproju.service;

import otpproju.model.Flashcard;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.RepositoryException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FlashcardService {

    private final FlashcardRepository repository;

    public FlashcardService(
            FlashcardRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "Flashcard repository must not be null"
        );
    }

    public Flashcard createFlashcard(
            int setId,
            String question,
            String answer
    ) {
        validatePositiveId(setId, "Set ID");

        String cleanedQuestion =
                validateText(question, "Question");

        String cleanedAnswer =
                validateText(answer, "Answer");

        Flashcard flashcard = new Flashcard(
                setId,
                cleanedQuestion,
                cleanedAnswer
        );

        return repository.create(flashcard);
    }

    public Flashcard getFlashcard(int cardId) {
        validatePositiveId(cardId, "Card ID");

        return repository.findById(cardId).orElseThrow(
                () -> new NoSuchElementException(
                        "Flashcard was not found"
                )
        );
    }

    public List<Flashcard> getFlashcardsForSet(int setId) {
        validatePositiveId(setId, "Set ID");

        return repository.findBySetId(setId);
    }

    public Flashcard updateFlashcard(
            int cardId,
            String question,
            String answer
    ) {
        validatePositiveId(cardId, "Card ID");

        String cleanedQuestion =
                validateText(question, "Question");

        String cleanedAnswer =
                validateText(answer, "Answer");

        Flashcard flashcard = repository
                .findById(cardId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Flashcard was not found"
                        )
                );

        flashcard.setQuestion(cleanedQuestion);
        flashcard.setAnswer(cleanedAnswer);

        if (!repository.update(flashcard)) {
            throw new RepositoryException(
                    "Flashcard could not be updated"
            );
        }

        return repository
                .findById(cardId)
                .orElseThrow(
                        () -> new RepositoryException(
                                "Updated flashcard could " +
                                        "not be retrieved"
                        )
                );
    }

    public void deleteFlashcard(int cardId) {
        validatePositiveId(cardId, "Card ID");

        if (!repository.deleteById(cardId)) {
            throw new NoSuchElementException(
                    "Flashcard was not found"
            );
        }
    }

    private void validatePositiveId(
            int id,
            String fieldName
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }

    private String validateText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be empty"
            );
        }

        return value.trim();
    }
}