package otpproju.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import otpproju.model.FlashcardSet;
import otpproju.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class FlashcardSetRepositoryTest {

    private UserRepository userRepository;
    private FlashcardSetRepository setRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        setRepository = new FlashcardSetRepository();
    }

    @Test
    void performsCompleteFlashcardSetCrudLifecycle() {
        String uniqueValue = UUID.randomUUID().toString();

        User owner = new User(
                "set_owner_" + uniqueValue,
                "set_owner_" + uniqueValue + "@example.com",
                "password-hash",
                User.UserType.STUDENT
        );

        Integer ownerId = null;
        Integer setId = null;

        try {
            // Create a user for the foreign key.
            User createdOwner =
                    userRepository.create(owner);

            ownerId = createdOwner.getUserId();

            assertNotNull(ownerId);

            // CREATE SET
            FlashcardSet newSet = new FlashcardSet(
                    ownerId,
                    "Java Basics",
                    "Introductory Java questions"
            );

            FlashcardSet createdSet =
                    setRepository.create(newSet);

            setId = createdSet.getSetId();

            assertNotNull(setId);
            assertEquals(
                    ownerId.intValue(),
                    createdSet.getUserId()
            );
            assertEquals(
                    "Java Basics",
                    createdSet.getTitle()
            );
            assertEquals(
                    "Introductory Java questions",
                    createdSet.getDescription()
            );
            assertNotNull(createdSet.getCreatedAt());
            assertNotNull(createdSet.getUpdatedAt());

            // FIND BY ID
            Optional<FlashcardSet> foundById =
                    setRepository.findById(setId);

            assertTrue(foundById.isPresent());
            assertEquals(createdSet, foundById.get());

            // FIND BY OWNER
            List<FlashcardSet> ownerSets =
                    setRepository.findByUserId(ownerId);

            int expectedSetId = setId;

            assertTrue(
                    ownerSets.stream().anyMatch(
                            set ->
                                    set.getSetId() != null
                                            && set.getSetId()
                                            == expectedSetId
                    )
            );

            // FIND ALL
            List<FlashcardSet> allSets =
                    setRepository.findAll();

            assertTrue(
                    allSets.stream().anyMatch(
                            set ->
                                    set.getSetId() != null
                                            && set.getSetId()
                                            == expectedSetId
                    )
            );

            // UPDATE
            createdSet.setTitle("Updated Java Basics");
            createdSet.setDescription(
                    "Updated introductory questions"
            );

            assertEquals(
                    "Updated Java Basics",
                    createdSet.getTitle()
            );

            assertTrue(setRepository.update(createdSet));

            FlashcardSet updatedSet = setRepository
                    .findById(setId)
                    .orElseThrow();

            assertEquals(
                    "Updated Java Basics",
                    updatedSet.getTitle()
            );
            assertEquals(
                    "Updated introductory questions",
                    updatedSet.getDescription()
            );

            // DELETE
            assertTrue(setRepository.deleteById(setId));
            assertTrue(setRepository.findById(setId).isEmpty());

            setId = null;

        } finally {
            if (setId != null) {
                setRepository.deleteById(setId);
            }

            if (ownerId != null) {
                userRepository.deleteById(ownerId);
            }
        }
    }

    @Test
    void supportsNullDescription() {
        String uniqueValue = UUID.randomUUID().toString();

        User owner = new User(
                "null_desc_" + uniqueValue,
                "null_desc_" + uniqueValue + "@example.com",
                "password-hash",
                User.UserType.STUDENT
        );

        Integer ownerId = null;
        Integer setId = null;

        try {
            User createdOwner =
                    userRepository.create(owner);

            ownerId = createdOwner.getUserId();

            FlashcardSet newSet = new FlashcardSet(
                    ownerId,
                    "Set without description",
                    null
            );

            FlashcardSet createdSet =
                    setRepository.create(newSet);

            setId = createdSet.getSetId();

            assertNull(createdSet.getDescription());

            FlashcardSet retrievedSet = setRepository
                    .findById(setId)
                    .orElseThrow();

            assertNull(retrievedSet.getDescription());

        } finally {
            if (setId != null) {
                setRepository.deleteById(setId);
            }

            if (ownerId != null) {
                userRepository.deleteById(ownerId);
            }
        }
    }

    @Test
    void returnsEmptyCollectionsForMissingRecords() {
        assertTrue(
                setRepository
                        .findById(Integer.MAX_VALUE)
                        .isEmpty()
        );

        assertTrue(
                setRepository
                        .findByUserId(Integer.MAX_VALUE)
                        .isEmpty()
        );
    }

    @Test
    void returnsFalseWhenUpdatingOrDeletingMissingSet() {
        FlashcardSet missingSet = new FlashcardSet(
                Integer.MAX_VALUE,
                1,
                "Missing set",
                "This set does not exist",
                null,
                null
        );

        assertFalse(setRepository.update(missingSet));

        assertFalse(
                setRepository.deleteById(Integer.MAX_VALUE)
        );
    }

    @Test
    void rejectsSetWithMissingOwner() {
        FlashcardSet set = new FlashcardSet(
                Integer.MAX_VALUE,
                "Invalid owner",
                "The user does not exist"
        );

        assertThrows(
                RepositoryException.class,
                () -> setRepository.create(set)
        );
    }
}