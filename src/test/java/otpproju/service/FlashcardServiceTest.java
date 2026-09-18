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

        service = new FlashcardService(
                cardRepository,
                setRepository
        );

        String uniqueValue = UUID.randomUUID().toString();

        User user = userRepository.create(
                new User(
                        "service_" + uniqueValue,
                        "service_" + uniqueValue
                                + "@example.com",
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
        /*
         * Deleting the user also deletes its sets and
         * flashcards because of ON DELETE CASCADE.
         */
        userRepository.deleteById(testUserId);
    }

    @Test
    void createsAndPersistsValidFlashcard() {
        Flashcard card = service.createFlashcard(
                testUserId,
                testSetId,
                "What is Java?",
                "A programming language"
        );

        assertNotNull(card.getCardId());
        assertEquals(testSetId, card.getSetId());
        assertEquals(
                "What is Java?",
                card.getQuestion()
        );
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
        assertNotNull(card.getCreatedAt());
        assertNotNull(card.getUpdatedAt());
    }

    @Test
    void trimsQuestionAndAnswer() {
        Flashcard card = service.createFlashcard(
                testUserId,
                testSetId,
                "  What is Java?  ",
                "  A programming language  "
        );

        assertEquals(
                "What is Java?",
                card.getQuestion()
        );
        assertEquals(
                "A programming language",
                card.getAnswer()
        );
    }

    @Test
    void retrievesFlashcardById() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "Retrieval question",
                "Retrieval answer"
        );

        Flashcard retrieved =
                service.getFlashcard(
                        created.getCardId()
                );

        assertEquals(created, retrieved);
        assertEquals(
                "Retrieval question",
                retrieved.getQuestion()
        );
    }

    @Test
    void retrievesFlashcardsForSet() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "List question",
                "List answer"
        );

        List<Flashcard> cards =
                service.getFlashcardsForSet(testSetId);

        assertTrue(
                cards.stream().anyMatch(created::equals)
        );
    }

    @Test
    void updatesExistingFlashcard() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "Original question",
                "Original answer"
        );

        Flashcard updated = service.updateFlashcard(
                testUserId,
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

        Flashcard retrieved =
                service.getFlashcard(
                        created.getCardId()
                );

        assertEquals(
                "Updated question",
                retrieved.getQuestion()
        );
        assertEquals(
                "Updated answer",
                retrieved.getAnswer()
        );
    }

    @Test
    void deletesExistingFlashcard() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "Delete question",
                "Delete answer"
        );

        service.deleteFlashcard(
                testUserId,
                created.getCardId()
        );

        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcard(
                        created.getCardId()
                )
        );
    }

    @Test
    void rejectsMissingFlashcard() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcard(
                        Integer.MAX_VALUE
                )
        );
    }

    @Test
    void rejectsDeletingMissingFlashcard() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.deleteFlashcard(
                        testUserId,
                        Integer.MAX_VALUE
                )
        );
    }

    @Test
    void rejectsZeroSetId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        0,
                        "Question",
                        "Answer"
                )
        );
    }

    @Test
    void rejectsNegativeSetId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        -1,
                        "Question",
                        "Answer"
                )
        );
    }

    @Test
    void rejectsZeroRequestingUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        0,
                        testSetId,
                        "Question",
                        "Answer"
                )
        );
    }

    @Test
    void rejectsNullQuestion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        testSetId,
                        null,
                        "Answer"
                )
        );
    }

    @Test
    void rejectsBlankQuestion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        testSetId,
                        "   ",
                        "Answer"
                )
        );
    }

    @Test
    void rejectsNullAnswer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        testSetId,
                        "Question",
                        null
                )
        );
    }

    @Test
    void rejectsBlankAnswer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcard(
                        testUserId,
                        testSetId,
                        "Question",
                        "   "
                )
        );
    }

    @Test
    void preventsAnotherUserFromCreatingCardInSet() {
        assertThrows(
                AuthorizationException.class,
                () -> service.createFlashcard(
                        Integer.MAX_VALUE,
                        testSetId,
                        "Unauthorized question",
                        "Unauthorized answer"
                )
        );
    }

    @Test
    void preventsAnotherUserFromUpdatingCard() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "Protected question",
                "Protected answer"
        );

        assertThrows(
                AuthorizationException.class,
                () -> service.updateFlashcard(
                        Integer.MAX_VALUE,
                        created.getCardId(),
                        "Unauthorized update",
                        "Unauthorized update"
                )
        );

        Flashcard unchanged =
                service.getFlashcard(
                        created.getCardId()
                );

        assertEquals(
                "Protected question",
                unchanged.getQuestion()
        );
        assertEquals(
                "Protected answer",
                unchanged.getAnswer()
        );
    }

    @Test
    void preventsAnotherUserFromDeletingCard() {
        Flashcard created = service.createFlashcard(
                testUserId,
                testSetId,
                "Protected question",
                "Protected answer"
        );

        assertThrows(
                AuthorizationException.class,
                () -> service.deleteFlashcard(
                        Integer.MAX_VALUE,
                        created.getCardId()
                )
        );

        Flashcard unchanged =
                service.getFlashcard(
                        created.getCardId()
                );

        assertEquals(created, unchanged);
    }

    @Test
    void constructorRejectsNullCardRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new FlashcardService(
                        null,
                        new FlashcardSetRepository()
                )
        );
    }

    @Test
    void constructorRejectsNullSetRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new FlashcardService(
                        new FlashcardRepository(),
                        null
                )
        );
    }
}