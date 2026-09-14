package otpproju.service;

import otpproju.model.Flashcard;

public class FlashcardService {

    public Flashcard createFlashcard(
            int setId,
            String question,
            String answer
    ) {
        if (setId <= 0) {
            throw new IllegalArgumentException(
                    "Set ID must be positive"
            );
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty"
            );
        }

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException(
                    "Answer must not be empty"
            );
        }

        return new Flashcard(
                setId,
                question.trim(),
                answer.trim()
        );
    }
}