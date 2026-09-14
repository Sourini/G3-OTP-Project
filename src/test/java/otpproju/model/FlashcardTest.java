package otpproju.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardTest {

    @Test
    void newFlashcardConstructorStoresProvidedValues() {
        Flashcard card = new Flashcard(
                5,
                "What is Java?",
                "A programming language"
        );

        assertEquals(5, card.getSetId());
        assertEquals("What is Java?", card.getQuestion());
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
        assertNull(card.getCardId());
        assertNull(card.getCreatedAt());
        assertNull(card.getUpdatedAt());
    }

    @Test
    void completeConstructorStoresDatabaseValues() {
        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 9, 14, 13, 0);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 14, 12, 0);

        Flashcard card = new Flashcard(
                10,
                5,
                "What is Java?",
                "A programming language",
                updatedAt,
                createdAt
        );

        assertEquals(10, card.getCardId());
        assertEquals(5, card.getSetId());
        assertEquals(updatedAt, card.getUpdatedAt());
        assertEquals(createdAt, card.getCreatedAt());
    }

    @Test
    void flashcardsWithSameDatabaseIdAreEqual() {
        Flashcard first = new Flashcard();
        first.setCardId(10);

        Flashcard second = new Flashcard();
        second.setCardId(10);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void flashcardsWithDifferentDatabaseIdsAreNotEqual() {
        Flashcard first = new Flashcard();
        first.setCardId(10);

        Flashcard second = new Flashcard();
        second.setCardId(20);

        assertNotEquals(first, second);
    }

    @Test
    void unsavedFlashcardsAreNotEqual() {
        assertNotEquals(
                new Flashcard(),
                new Flashcard()
        );
    }

    @Test
    void toStringContainsQuestionAndAnswer() {
        Flashcard card = new Flashcard(
                5,
                "What is Java?",
                "A programming language"
        );

        String result = card.toString();

        assertTrue(result.contains("What is Java?"));
        assertTrue(result.contains("A programming language"));
    }
}