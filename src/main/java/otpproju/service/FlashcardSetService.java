package otpproju.service;

import otpproju.model.FlashcardSet;
import otpproju.repository.FlashcardSetRepository;
import otpproju.repository.RepositoryException;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import otpproju.model.SetText;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FlashcardSetService {

    private final FlashcardSetRepository repository;

    public FlashcardSetService(
            FlashcardSetRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "Flashcard set repository must not be null"
        );
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

        FlashcardSet set = repository
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

    public FlashcardSet createFlashcardSet(
            int userId,
            String title,
            String description
    ) {
        validatePositiveId(userId, "User ID");

        FlashcardSet set = new FlashcardSet(
                userId,
                validateTitle(title),
                cleanDescription(description)
        );

        return repository.create(set);
    }

    public FlashcardSet createMultilingualSet(int userId, String defaultLanguage, Map<String, SetText> texts) {
        validatePositiveId(userId, "User ID");
        if (defaultLanguage == null || texts.isEmpty() || !texts.containsKey(defaultLanguage)
                || !Set.of("en", "fi", "ko", "ar", "ru").containsAll(texts.keySet())) {
            throw new IllegalArgumentException("Choose at least one supported language and a default.");
        }
        Map<String, SetText> validated = new LinkedHashMap<>();
        texts.forEach((language, text) -> {
            if (text.description() == null || text.description().isBlank()) {
                throw new IllegalArgumentException("Enter a description for every selected language.");
            }
            validated.put(language, new SetText(validateTitle(text.title()), cleanDescription(text.description())));
        });
        return repository.createMultilingual(userId, defaultLanguage, validated);
    }

    public SetText getStudyText(FlashcardSet set, String language) {
        if (set.hasConfiguredLanguages() && language != null && !language.equals(set.getDefaultLanguage())
                && set.getSupportedLanguages().contains(language)) {
            return repository.findTranslation(set.getSetId(), language)
                    .orElseGet(() -> new SetText(set.getTitle(), set.getDescription()));
        }
        return new SetText(set.getTitle(), set.getDescription());
    }

    public FlashcardSet getFlashcardSet(int setId) {
        validatePositiveId(setId, "Set ID");

        return repository.findById(setId).orElseThrow(
                () -> new NoSuchElementException(
                        "Flashcard set was not found"
                )
        );
    }

    public List<FlashcardSet> getFlashcardSetsForUser(
            int userId
    ) {
        validatePositiveId(userId, "User ID");

        return repository.findByUserId(userId);
    }

    public List<FlashcardSet> getAllFlashcardSets() {
        return repository.findAll();
    }

    public FlashcardSet updateFlashcardSet(
            int requestingUserId,
            int setId,
            String title,
            String description
    ) {
        FlashcardSet set = requireOwnedSet(
                requestingUserId,
                setId
        );

        set.setTitle(validateTitle(title));
        set.setDescription(cleanDescription(description));

        if (!repository.update(set)) {
            throw new RepositoryException(
                    "Flashcard set could not be updated"
            );
        }

        return repository
                .findById(setId)
                .orElseThrow(
                        () -> new RepositoryException(
                                "Updated flashcard set could " +
                                        "not be retrieved"
                        )
                );
    }

    public void deleteFlashcardSet(
            int requestingUserId,
            int setId
    ) {
        requireOwnedSet(requestingUserId, setId);

        if (!repository.deleteById(setId)) {
            throw new RepositoryException(
                    "Flashcard set could not be deleted"
            );
        }
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Title must not be empty"
            );
        }

        String cleanedTitle = title.trim();

        if (cleanedTitle.length() > 100) {
            throw new IllegalArgumentException(
                    "Title must not exceed 100 characters"
            );
        }

        return cleanedTitle;
    }

    private String cleanDescription(String description) {
        return description == null
                ? null
                : description.trim();
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
}