package otpproju.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.model.FlashcardSet;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardSetServiceTest {

    private FlashcardSetService service;

    @BeforeEach
    void setUp() {
        service = new FlashcardSetService();
    }

    @Test
    void createFlashcardSetCreatesSetWithValidInput() {
        FlashcardSet set = service.createFlashcardSet(
                1,
                "Java Basics",
                "Introductory Java questions"
        );

        assertEquals(1, set.getUserId());
        assertEquals("Java Basics", set.getTitle());
        assertEquals(
                "Introductory Java questions",
                set.getDescription()
        );
    }

    @Test
    void createFlashcardSetTrimsTitleAndDescription() {
        FlashcardSet set = service.createFlashcardSet(
                1,
                "  Java Basics  ",
                "  Introductory Java questions  "
        );

        assertEquals("Java Basics", set.getTitle());
        assertEquals(
                "Introductory Java questions",
                set.getDescription()
        );
    }

    @Test
    void createFlashcardSetAllowsNullDescription() {
        FlashcardSet set = service.createFlashcardSet(
                1,
                "Java Basics",
                null
        );

        assertEquals("Java Basics", set.getTitle());
        assertNull(set.getDescription());
    }

    @Test
    void createFlashcardSetAllowsBlankDescription() {
        FlashcardSet set = service.createFlashcardSet(
                1,
                "Java Basics",
                "   "
        );

        assertEquals("", set.getDescription());
    }

    @Test
    void createFlashcardSetRejectsZeroUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        0,
                        "Java Basics",
                        "Description"
                )
        );
    }

    @Test
    void createFlashcardSetRejectsNegativeUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        -1,
                        "Java Basics",
                        "Description"
                )
        );
    }

    @Test
    void createFlashcardSetRejectsNullTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        1,
                        null,
                        "Description"
                )
        );
    }

    @Test
    void createFlashcardSetRejectsBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        1,
                        "   ",
                        "Description"
                )
        );
    }

    @Test
    void createFlashcardSetAcceptsTitleWithExactly100Characters() {
        String title = "a".repeat(100);

        FlashcardSet set = service.createFlashcardSet(
                1,
                title,
                "Description"
        );

        assertEquals(100, set.getTitle().length());
    }

    @Test
    void createFlashcardSetRejectsTitleLongerThan100Characters() {
        String title = "a".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        1,
                        title,
                        "Description"
                )
        );
    }
}