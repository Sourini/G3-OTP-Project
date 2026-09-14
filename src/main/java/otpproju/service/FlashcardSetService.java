package otpproju.service;

import otpproju.model.FlashcardSet;

public class FlashcardSetService {

    public FlashcardSet createFlashcardSet(
            int userId,
            String title,
            String description
    ) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be positive"
            );
        }

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

        String cleanedDescription =
                description == null
                        ? null
                        : description.trim();

        return new FlashcardSet(
                userId,
                cleanedTitle,
                cleanedDescription
        );
    }
}