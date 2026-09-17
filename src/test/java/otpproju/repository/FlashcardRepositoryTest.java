package otpproju.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import otpproju.model.Flashcard;
import otpproju.model.FlashcardSet;
import otpproju.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class FlashcardRepositoryTest {

    private UserRepository userRepository;
    private FlashcardSetRepository setRepository;
    private FlashcardRepository cardRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        setRepository = new FlashcardSetRepository();
        cardRepository = new FlashcardRepository();
    }

    @Test
    void performsCompleteFlashcardCrudLifecycle() {
        TestData testData = createTestUserAndSet();

        Integer cardId = null;

        try {
            // CREATE
            Flashcard newCard = new Flashcard(
                    testData.setId(),
                    "What does JVM stand for?",
                    "Java Virtual Machine"
            );

            Flashcard createdCard =
                    cardRepository.create(newCard);

            cardId = createdCard.getCardId();

            assertNotNull(cardId);
            assertEquals(
                    testData.setId(),
                    createdCard.getSetId()
            );
            assertEquals(
                    "What does JVM stand for?",
                    createdCard.getQuestion()
            );
            assertEquals(
                    "Java Virtual Machine",
                    createdCard.getAnswer()
            );
            assertNotNull(createdCard.getCreatedAt());
            assertNotNull(createdCard.getUpdatedAt());

            // FIND BY ID
            Optional<Flashcard> foundById =
                    cardRepository.findById(cardId);

            assertTrue(foundById.isPresent());
            assertEquals(createdCard, foundById.get());

            // FIND BY SET
            List<Flashcard> setCards =
                    cardRepository.findBySetId(
                            testData.setId()
                    );

            int expectedCardId = cardId;

            assertTrue(
                    setCards.stream().anyMatch(
                            card ->
                                    card.getCardId() != null
                                            && card.getCardId()
                                            == expectedCardId
                    )
            );

            // FIND ALL
            List<Flashcard> allCards =
                    cardRepository.findAll();

            assertTrue(
                    allCards.stream().anyMatch(
                            card ->
                                    card.getCardId() != null
                                            && card.getCardId()
                                            == expectedCardId
                    )
            );

            // UPDATE
            createdCard.setQuestion("What is the JVM?");
            createdCard.setAnswer(
                    "The Java Virtual Machine"
            );

            assertEquals(
                    "What is the JVM?",
                    createdCard.getQuestion()
            );

            assertTrue(cardRepository.update(createdCard));

            Flashcard updatedCard = cardRepository
                    .findById(cardId)
                    .orElseThrow();

            assertEquals(
                    "What is the JVM?",
                    updatedCard.getQuestion()
            );
            assertEquals(
                    "The Java Virtual Machine",
                    updatedCard.getAnswer()
            );

            // DELETE
            assertTrue(cardRepository.deleteById(cardId));
            assertTrue(cardRepository.findById(cardId).isEmpty());

            cardId = null;

        } finally {
            if (cardId != null) {
                cardRepository.deleteById(cardId);
            }

            deleteTestData(testData);
        }
    }

    @Test
    void returnsEmptyCollectionsForMissingRecords() {
        assertTrue(
                cardRepository
                        .findById(Integer.MAX_VALUE)
                        .isEmpty()
        );

        assertTrue(
                cardRepository
                        .findBySetId(Integer.MAX_VALUE)
                        .isEmpty()
        );
    }

    @Test
    void returnsFalseWhenUpdatingOrDeletingMissingCard() {
        Flashcard missingCard = new Flashcard(
                Integer.MAX_VALUE,
                1,
                "Missing question",
                "Missing answer",
                null,
                null
        );

        assertFalse(cardRepository.update(missingCard));

        assertFalse(
                cardRepository.deleteById(Integer.MAX_VALUE)
        );
    }

    @Test
    void rejectsCardWithMissingSet() {
        Flashcard card = new Flashcard(
                Integer.MAX_VALUE,
                "Invalid set",
                "The set does not exist"
        );

        assertThrows(
                RepositoryException.class,
                () -> cardRepository.create(card)
        );
    }

    @Test
    void deletingSetAlsoDeletesItsCards() {
        TestData testData = createTestUserAndSet();

        Integer cardId = null;
        boolean setDeleted = false;

        try {
            Flashcard card = cardRepository.create(
                    new Flashcard(
                            testData.setId(),
                            "Cascade question",
                            "Cascade answer"
                    )
            );

            cardId = card.getCardId();

            assertNotNull(cardId);
            assertTrue(
                    cardRepository.findById(cardId).isPresent()
            );

            assertTrue(
                    setRepository.deleteById(
                            testData.setId()
                    )
            );

            setDeleted = true;

            assertTrue(
                    cardRepository.findById(cardId).isEmpty()
            );

            cardId = null;

        } finally {
            if (cardId != null) {
                cardRepository.deleteById(cardId);
            }

            if (!setDeleted) {
                setRepository.deleteById(testData.setId());
            }

            userRepository.deleteById(testData.userId());
        }
    }

    private TestData createTestUserAndSet() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = userRepository.create(
                new User(
                        "card_owner_" + uniqueValue,
                        "card_owner_" + uniqueValue +
                                "@example.com",
                        "password-hash",
                        User.UserType.STUDENT
                )
        );

        FlashcardSet set = setRepository.create(
                new FlashcardSet(
                        user.getUserId(),
                        "Integration test set",
                        "Temporary integration-test data"
                )
        );

        return new TestData(
                user.getUserId(),
                set.getSetId()
        );
    }

    private void deleteTestData(TestData testData) {
        setRepository.deleteById(testData.setId());
        userRepository.deleteById(testData.userId());
    }

    private record TestData(
            int userId,
            int setId
    ) {
    }
}