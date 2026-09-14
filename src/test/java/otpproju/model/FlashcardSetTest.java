package otpproju.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetTest {

    @Test
    void newSetConstructorStoresProvidedValues() {
        FlashcardSet set = new FlashcardSet(
                5,
                "Java Basics",
                "Introductory questions"
        );

        assertEquals(5, set.getUserId());
        assertEquals("Java Basics", set.getTitle());
        assertEquals(
                "Introductory questions",
                set.getDescription()
        );
        assertNull(set.getSetId());
        assertNull(set.getCreatedAt());
        assertNull(set.getUpdatedAt());
    }

    @Test
    void completeConstructorStoresDatabaseValues() {
        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 9, 14, 13, 0);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 14, 12, 0);

        FlashcardSet set = new FlashcardSet(
                10,
                5,
                "Java Basics",
                "Introductory questions",
                updatedAt,
                createdAt
        );

        assertEquals(10, set.getSetId());
        assertEquals(5, set.getUserId());
        assertEquals(updatedAt, set.getUpdatedAt());
        assertEquals(createdAt, set.getCreatedAt());
    }

    @Test
    void setsWithSameDatabaseIdAreEqual() {
        FlashcardSet first = new FlashcardSet();
        first.setSetId(10);

        FlashcardSet second = new FlashcardSet();
        second.setSetId(10);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void setsWithDifferentDatabaseIdsAreNotEqual() {
        FlashcardSet first = new FlashcardSet();
        first.setSetId(10);

        FlashcardSet second = new FlashcardSet();
        second.setSetId(20);

        assertNotEquals(first, second);
    }

    @Test
    void unsavedSetsAreNotEqual() {
        assertNotEquals(
                new FlashcardSet(),
                new FlashcardSet()
        );
    }

    @Test
    void toStringContainsUsefulSetInformation() {
        FlashcardSet set = new FlashcardSet(
                5,
                "Java Basics",
                "Introductory questions"
        );

        String result = set.toString();

        assertTrue(result.contains("Java Basics"));
        assertTrue(result.contains("Introductory questions"));
    }
}