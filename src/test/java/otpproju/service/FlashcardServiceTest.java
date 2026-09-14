package otpproju.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.model.Flashcard;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardServiceTest {

    private FlashcardService service;

    @BeforeEach
    void setUp() {
        service = new FlashcardService();
    }

    @Test
    void createFlashcardCreatesCardWithValidInput() {
        Flashcard card = service.createFlashcard(
                1,
                "What is Java?",
                "A programming language"
        );

        assertEquals(1, card.getSetId());
        assertEquals("What is Java?", card.getQuestion());
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
    }

    @Test
    void createFlashcardTrimsQuestionAndAnswer() {
        Flashcard card = service.createFlashcard(
                1,
                "  What is Java?  ",
                "  A programming language  "
        );

        assertEquals("What is Java?", card.getQuestion());
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
    }

    @Test
    void createFlashcardRejectsZeroSetId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        0,
                        "Question",
                        "Answer"
                )
        );
    }

    @Test
    void createFlashcardRejectsNegativeSetId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        -1,
                        "Question",
                        "Answer"
                )
        );
    }

    @Test
    void createFlashcardRejectsNullQuestion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        1,
                        null,
                        "Answer"
                )
        );
    }

    @Test
    void createFlashcardRejectsBlankQuestion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        1,
                        "   ",
                        "Answer"
                )
        );
    }

    @Test
    void createFlashcardRejectsNullAnswer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        1,
                        "Question",
                        null
                )
        );
    }

    @Test
    void createFlashcardRejectsBlankAnswer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        1,
                        "Question",
                        "   "
                )
        );
    }
}