package otpproju.service;

import otpproju.model.Flashcard;
import otpproju.model.FlashcardSet;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.repository.RepositoryException;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import otpproju.model.CardText;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FlashcardService {

    private final FlashcardRepository cardRepository;
    private final FlashcardSetRepository setRepository;

    public FlashcardService(
            FlashcardRepository cardRepository,
            FlashcardSetRepository setRepository
    ) {
        this.cardRepository = Objects.requireNonNull(
                cardRepository,
                "Flashcard repository must not be null"
        );

        this.setRepository = Objects.requireNonNull(
                setRepository,
                "Flashcard set repository must not be null"
        );
    }

    public Flashcard createFlashcard(
            int requestingUserId,
            int setId,
            String question,
            String answer
    ) {
        if (requireOwnedSet(requestingUserId, setId).hasConfiguredLanguages()) {
            throw new IllegalArgumentException("Enter both sides in every supported language.");
        }

        Flashcard flashcard = new Flashcard(
                setId,
                validateText(question, "Question"),
                validateText(answer, "Answer")
        );

        return cardRepository.create(flashcard);
    }

    public Flashcard getFlashcard(int cardId) {
        validatePositiveId(cardId, "Card ID");

        return cardRepository.findById(cardId).orElseThrow(
                () -> new NoSuchElementException(
                        "Flashcard was not found"
                )
        );
    }

    public List<Flashcard> getFlashcardsForSet(int setId) {
        validatePositiveId(setId, "Set ID");

        return cardRepository.findBySetId(setId);
    }

    public List<Flashcard> getFlashcardsForSet(FlashcardSet set, String language) {
        validatePositiveId(set.getSetId(), "Set ID");
        if (!set.hasConfiguredLanguages() || language == null || language.equals(set.getDefaultLanguage())
                || !set.getSupportedLanguages().contains(language)) {
            return getFlashcardsForSet(set.getSetId());
        }
        return cardRepository.findBySetId(set.getSetId(), language);
    }

    public Flashcard createMultilingualCard(int userId, int setId, Map<String, CardText> texts) {
        FlashcardSet set = requireOwnedSet(userId, setId);
        if (!set.hasConfiguredLanguages() || !texts.keySet().equals(new HashSet<>(set.getSupportedLanguages()))) {
            throw new IllegalArgumentException("Enter both sides in every supported language.");
        }
        Map<String, CardText> validated = new LinkedHashMap<>();
        texts.forEach((language, text) -> validated.put(language,
                new CardText(validateText(text.question(), "Question"), validateText(text.answer(), "Answer"))));
        return cardRepository.createMultilingual(setId, set.getDefaultLanguage(), validated);
    }

    public Flashcard updateFlashcard(
            int requestingUserId,
            int cardId,
            String question,
            String answer
    ) {
        validatePositiveId(cardId, "Card ID");

        Flashcard flashcard = cardRepository
                .findById(cardId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Flashcard was not found"
                        )
                );

        requireOwnedSet(
                requestingUserId,
                flashcard.getSetId()
        );

        flashcard.setQuestion(
                validateText(question, "Question")
        );

        flashcard.setAnswer(
                validateText(answer, "Answer")
        );

        if (!cardRepository.update(flashcard)) {
            throw new RepositoryException(
                    "Flashcard could not be updated"
            );
        }

        return cardRepository
                .findById(cardId)
                .orElseThrow(
                        () -> new RepositoryException(
                                "Updated flashcard could " +
                                        "not be retrieved"
                        )
                );
    }

    public void deleteFlashcard(
            int requestingUserId,
            int cardId
    ) {
        validatePositiveId(cardId, "Card ID");

        Flashcard flashcard = cardRepository
                .findById(cardId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Flashcard was not found"
                        )
                );

        requireOwnedSet(
                requestingUserId,
                flashcard.getSetId()
        );

        if (!cardRepository.deleteById(cardId)) {
            throw new RepositoryException(
                    "Flashcard could not be deleted"
            );
        }
    }

    private FlashcardSet requireOwnedSet(
            int requestingUserId,
            int setId
    ) {
        validatePositiveId(
                requestingUserId,
                "Requesting user ID"
        );

        validatePositiveId(setId, "Set ID");

        FlashcardSet set = setRepository
                .findById(setId)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Flashcard set was not found"
                        )
                );

        if (set.getUserId() != requestingUserId) {
            throw new AuthorizationException(
                    "User does not own this flashcard set"
            );
        }

        return set;
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