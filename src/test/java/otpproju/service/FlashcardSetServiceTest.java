package otpproju.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardSetRepository;
import otpproju.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlashcardSetServiceTest {

    private FlashcardSetService service;
    private UserRepository userRepository;

    private int testUserId;

    @BeforeAll
    void setUpDatabaseData() {
        userRepository = new UserRepository();

        service = new FlashcardSetService(
                new FlashcardSetRepository()
        );

        String uniqueValue = UUID.randomUUID().toString();

        User user = userRepository.create(
                new User(
                        "set_service_" + uniqueValue,
                        "set_service_" + uniqueValue +
                                "@example.com",
                        "password-hash",
                        User.UserType.STUDENT
                )
        );

        testUserId = user.getUserId();
    }

    @AfterAll
    void removeDatabaseData() {
        userRepository.deleteById(testUserId);
    }

    @Test
    void createsAndPersistsFlashcardSet() {
        FlashcardSet set = service.createFlashcardSet(
                testUserId,
                "Java Basics",
                "Introductory Java questions"
        );

        assertNotNull(set.getSetId());
        assertEquals(testUserId, set.getUserId());
        assertEquals("Java Basics", set.getTitle());
        assertEquals(
                "Introductory Java questions",
                set.getDescription()
        );
        assertNotNull(set.getCreatedAt());
    }

    @Test
    void trimsTitleAndDescription() {
        FlashcardSet set = service.createFlashcardSet(
                testUserId,
                "  Java Basics  ",
                "  Introductory questions  "
        );

        assertEquals("Java Basics", set.getTitle());
        assertEquals(
                "Introductory questions",
                set.getDescription()
        );
    }

    @Test
    void allowsNullDescription() {
        FlashcardSet set = service.createFlashcardSet(
                testUserId,
                "Java Basics",
                null
        );

        assertNull(set.getDescription());
    }

    @Test
    void allowsBlankDescription() {
        FlashcardSet set = service.createFlashcardSet(
                testUserId,
                "Java Basics",
                "   "
        );

        assertEquals("", set.getDescription());
    }

    @Test
    void retrievesSetById() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "Retrieval set",
                "Retrieval test"
        );

        FlashcardSet retrieved =
                service.getFlashcardSet(created.getSetId());

        assertEquals(created, retrieved);
    }

    @Test
    void retrievesSetsForUser() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "List set",
                "List test"
        );

        List<FlashcardSet> sets =
                service.getFlashcardSetsForUser(testUserId);

        assertTrue(
                sets.stream().anyMatch(created::equals)
        );
    }

    @Test
    void updatesExistingSet() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "Original title",
                "Original description"
        );

        FlashcardSet updated =
                service.updateFlashcardSet(
                        testUserId,
                        created.getSetId(),
                        "  Updated title  ",
                        "  Updated description  "
                );

        assertEquals(
                "Updated title",
                updated.getTitle()
        );
        assertEquals(
                "Updated description",
                updated.getDescription()
        );
    }

    @Test
    void deletesExistingSet() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "Delete set",
                "Delete test"
        );

        service.deleteFlashcardSet(
                testUserId,
                created.getSetId()
        );

        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcardSet(
                        created.getSetId()
                )
        );
    }

    @Test
    void rejectsZeroUserId() {
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
    void rejectsNullTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        testUserId,
                        null,
                        "Description"
                )
        );
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        testUserId,
                        "   ",
                        "Description"
                )
        );
    }

    @Test
    void acceptsTitleWithExactly100Characters() {
        FlashcardSet set = service.createFlashcardSet(
                testUserId,
                "a".repeat(100),
                "Description"
        );

        assertEquals(100, set.getTitle().length());
    }

    @Test
    void rejectsTitleLongerThan100Characters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createFlashcardSet(
                        testUserId,
                        "a".repeat(101),
                        "Description"
                )
        );
    }

    @Test
    void deletingMissingSetReportsNotFound() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.deleteFlashcardSet(
                        testUserId,
                        Integer.MAX_VALUE
                )
        );
    }

    @Test
    void rejectsMissingSet() {
        assertThrows(
                NoSuchElementException.class,
                () -> service.getFlashcardSet(
                        Integer.MAX_VALUE
                )
        );
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new FlashcardSetService(null)
        );
    }

    @Test
    void preventsAnotherUserFromUpdatingSet() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "Protected set",
                "Only its owner can update it"
        );

        assertThrows(
                AuthorizationException.class,
                () -> service.updateFlashcardSet(
                        Integer.MAX_VALUE,
                        created.getSetId(),
                        "Unauthorized update",
                        "This must not be saved"
                )
        );

        FlashcardSet unchanged =
                service.getFlashcardSet(created.getSetId());

        assertEquals(
                "Protected set",
                unchanged.getTitle()
        );
    }

    @Test
    void preventsAnotherUserFromDeletingSet() {
        FlashcardSet created = service.createFlashcardSet(
                testUserId,
                "Protected set",
                "Only its owner can delete it"
        );

        assertThrows(
                AuthorizationException.class,
                () -> service.deleteFlashcardSet(
                        Integer.MAX_VALUE,
                        created.getSetId()
                )
        );

        assertEquals(
                created,
                service.getFlashcardSet(created.getSetId())
        );
    }
}