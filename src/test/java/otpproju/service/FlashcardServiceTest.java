package otpproju.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import otpproju.model.Flashcard;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlashcardServiceTest {

    private FlashcardService service;

    private UserRepository userRepository;
    private FlashcardSetRepository setRepository;

    private int testUserId;
    private int testSetId;

    @BeforeAll
    void setUpDatabaseData() {
        userRepository = new UserRepository();
        setRepository = new FlashcardSetRepository();

        FlashcardRepository cardRepository =
                new FlashcardRepository();

        service = new FlashcardService(cardRepository);

        String uniqueValue = UUID.randomUUID().toString();

        User user = userRepository.create(
                new User(
                        "service_" + uniqueValue,
                        "service_" + uniqueValue +
                                "@example.com",
                        "password-hash",
                        User.UserType.STUDENT
                )
        );

        testUserId = user.getUserId();

        FlashcardSet set = setRepository.create(
                new FlashcardSet(
                        testUserId,
                        "Service test set",
                        "Temporary test data"
                )
        );

        testSetId = set.getSetId();
    }

    @AfterAll
    void removeDatabaseData() {
        // Deleting the user cascades to its sets and cards.
        userRepository.deleteById(testUserId);
    }

    @Test
    void createFlashcardPersistsValidCard() {
        Flashcard card = service.createFlashcard(
                testSetId,
                "What is Java?",
                "A programming language"
        );

        assertNotNull(card.getCardId());
        assertEquals(testSetId, card.getSetId());
        assertEquals("What is Java?", card.getQuestion());
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
        assertNotNull(card.getCreatedAt());
    }

    @Test
    void createFlashcardTrimsQuestionAndAnswer() {
        Flashcard card = service.createFlashcard(
                testSetId,
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
    void createdFlashcardCanBeRetrieved() {
        Flashcard created = service.createFlashcard(
                testSetId,
                "Retrieval question",
                "Retrieval answer"
        );

        Flashcard retrieved =
                service.getFlashcard(created.getCardId());

        assertEquals(created, retrieved);
        assertEquals(
                "Retrieval question",
                retrieved.getQuestion()
        );
    }

    @Test
    void getsFlashcardsForSet() {
        Flashcard created = service.createFlashcard(
                testSetId,
                "List question",
                "List answer"
        );

        List<Flashcard> cards =
                service.getFlashcardsForSet(testSetId);

        assertTrue(
                cards.stream().anyMatch(
                        card -> card.equals(created)
                )
        );
    }

    @Test
    void updatesExistingFlashcard() {
        Flashcard created = service.createFlashcard(
                testSetId,
                "Original question",
                "Original answer"
        );

        Flashcard updated = service.updateFlashcard(
                created.getCardId(),
                "  Updated question  ",
                "  Updated answer  "
        );

        assertEquals(
                "Updated question",
                updated.getQuestion()
        );
        assertEquals(
                "Updated answer",
                updated.getAnswer()
        );
    }

    @Test
    void deletesExistingFlashcard() {
        Flashcard created = service.createFlashcard(
                testSetId,
                "Delete question",
                "Delete answer"
        );

        service.deleteFlashcard(created.getCardId());

        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcard(
                        created.getCardId()
                )
        );
    }

    @Test
    void getFlashcardRejectsMissingCard() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcard(
                        Integer.MAX_VALUE
                )
        );
    }

    @Test
    void deleteFlashcardRejectsMissingCard() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.deleteFlashcard(
                        Integer.MAX_VALUE
                )
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
                        testSetId,
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
                        testSetId,
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
                        testSetId,
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
                        testSetId,
                        "Question",
                        "   "
                )
        );
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new FlashcardService(null)
        );
    }
}