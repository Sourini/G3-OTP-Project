package otpproju.screens;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.config.I18n;
import otpproju.model.Flashcard;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardService;
import otpproju.service.FlashcardSetService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CreateCardTest {

    private static final int USER_ID = 1;
    private static final int SET_ID = 10;

    private User user;
    private FakeFlashcardService cardService;
    private FakeFlashcardSetService setService;
    private TestCreateCard createCard;
    private Stage stage;
    private Locale originalLocale;

    @BeforeAll
    static void initToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);

        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
        } catch (IllegalStateException alreadyRunning) {
            Platform.runLater(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
        }

        assertTrue(
                started.await(10, TimeUnit.SECONDS),
                "JavaFX toolkit did not start"
        );
    }

    private static <T> T onFx(Callable<T> action)
            throws Exception {

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(action.call());
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                done.countDown();
            }
        });

        assertTrue(
                done.await(10, TimeUnit.SECONDS),
                "FX action timed out"
        );

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    private static void onFx(Runnable action) throws Exception {
        onFx(() -> {
            action.run();
            return null;
        });
    }

    @BeforeEach
    void setUp() {
        originalLocale = I18n.getLocale();

        user = new User(
                USER_ID,
                "Olivia",
                "olivia@example.com",
                "test-hash",
                User.UserType.STUDENT,
                LocalDateTime.now()
        );

        cardService = new FakeFlashcardService();
        setService = new FakeFlashcardSetService();

        createCard = new TestCreateCard(
                user,
                cardService,
                setService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null) {
            onFx(stage::close);
            stage = null;
        }

        I18n.setLocale(originalLocale);
    }

    private Scene showCreateCard() throws Exception {
        return onFx(() -> {
            stage = new Stage();
            createCard.show(stage);
            return stage.getScene();
        });
    }

    private static List<Node> allNodes(Parent root) {
        List<Node> nodes = new ArrayList<>();
        collect(root, nodes);
        return nodes;
    }

    private static void collect(Node node, List<Node> nodes) {
        nodes.add(node);

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, nodes);
            }
        }
    }

    private static TextArea textArea(Scene scene, String prompt) {
        return allNodes(scene.getRoot()).stream()
                .filter(TextArea.class::isInstance)
                .map(TextArea.class::cast)
                .filter(area -> prompt.equals(area.getPromptText()))
                .findFirst()
                .orElseThrow();
    }

    private static ComboBox<?> setComboBox(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(node -> node instanceof ComboBox<?>)
                .map(node -> (ComboBox<?>) node)
                .findFirst()
                .orElseThrow();
    }

    private static Button createButton(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button ->
                        I18n.t("createCard.createButton")
                                .equals(button.getText()))
                .findFirst()
                .orElseThrow();
    }

    private static FlashcardSet testSet() {
        return new FlashcardSet(
                SET_ID,
                USER_ID,
                "Biology",
                "Biology flashcards",
                null,
                null
        );
    }

    private void selectTestSet(Scene scene) {
        ComboBox<?> comboBox = setComboBox(scene);
        comboBox.getSelectionModel().selectFirst();
    }

    private void enterValidForm(Scene scene) {
        selectTestSet(scene);

        textArea(scene, I18n.t("createCard.questionField"))
                .setText("  What is a cell?  ");

        textArea(scene, I18n.t("createCard.answerField"))
                .setText("  The basic unit of life.  ");
    }

    @Test
    void show_displaysCreationForm() throws Exception {
        setService.sets = List.of(testSet());

        Scene scene = showCreateCard();

        assertNotNull(scene);

        assertEquals(
                I18n.t("createCard.title"),
                onFx(() -> stage.getTitle())
        );

        assertNotNull(setComboBox(scene));

        assertNotNull(
                textArea(scene, I18n.t("createCard.questionField"))
        );

        assertNotNull(
                textArea(scene, I18n.t("createCard.answerField"))
        );

        assertNotNull(createButton(scene));

        assertFalse(scene.getStylesheets().isEmpty());

        assertTrue(
                scene.getStylesheets().get(0).endsWith("style.css")
        );
    }

    @Test
    void show_loadsFlashcardSetsForCurrentUser() throws Exception {
        setService.sets = List.of(
                testSet(),
                new FlashcardSet(
                        11,
                        USER_ID,
                        "History",
                        "History flashcards",
                        null,
                        null
                )
        );

        Scene scene = showCreateCard();

        ComboBox<?> comboBox = setComboBox(scene);

        assertEquals(1, setService.callCount);
        assertEquals(USER_ID, setService.requestedUserId);

        assertEquals(2, onFx(() -> comboBox.getItems().size()));

        assertEquals("Biology", onFx(() ->
                ((FlashcardSet) comboBox.getItems().get(0)).getTitle()));
    }

    @Test
    void show_whenLoadingSetsFails_displaysError() throws Exception {
        setService.failure =
                new RuntimeException("Could not load sets");

        Scene scene = showCreateCard();

        assertNotNull(scene);
        assertEquals(1, createCard.alerts.size());

        assertEquals(
                Alert.AlertType.ERROR,
                createCard.alerts.get(0).type()
        );

        assertEquals(
                I18n.t("createCard.exceptionError"),
                createCard.alerts.get(0).title()
        );
    }

    @Test
    void createButton_withoutSelectedSet_displaysValidationError()
            throws Exception {

        setService.sets = List.of(testSet());

        Scene scene = showCreateCard();

        onFx(() -> createButton(scene).fire());

        assertEquals(0, cardService.callCount);
        assertEquals(1, createCard.alerts.size());

        assertEquals(
                Alert.AlertType.ERROR,
                createCard.alerts.get(0).type()
        );

        assertEquals(
                I18n.t("createCard.setSelectError.title"),
                createCard.alerts.get(0).title()
        );
    }

    @Test
    void createButton_withEmptyFields_displaysValidationError()
            throws Exception {

        setService.sets = List.of(testSet());

        Scene scene = showCreateCard();

        onFx(() -> {
            selectTestSet(scene);
            createButton(scene).fire();
        });

        assertEquals(0, cardService.callCount);
        assertEquals(1, createCard.alerts.size());

        assertEquals(
                I18n.t("createCard.setEmptyField.title"),
                createCard.alerts.get(0).title()
        );
    }

    @Test
    void createButton_passesTrimmedInputToService()
            throws Exception {

        setService.sets = List.of(testSet());

        Scene scene = showCreateCard();

        onFx(() -> {
            enterValidForm(scene);
            createButton(scene).fire();
        });

        assertEquals(1, cardService.callCount);

        assertEquals(USER_ID, cardService.receivedUserId);
        assertEquals(SET_ID, cardService.receivedSetId);

        assertEquals(
                "What is a cell?",
                cardService.receivedQuestion
        );

        assertEquals(
                "The basic unit of life.",
                cardService.receivedAnswer
        );

        assertEquals(
                Alert.AlertType.INFORMATION,
                createCard.alerts.get(0).type()
        );

        assertEquals(
                I18n.t("createCard.created.title"),
                createCard.alerts.get(0).title()
        );

        assertEquals(
                "",
                onFx(() -> textArea(
                        scene,
                        I18n.t("createCard.questionField")
                ).getText())
        );

        assertEquals(
                "",
                onFx(() -> textArea(
                        scene,
                        I18n.t("createCard.answerField")
                ).getText())
        );
    }

    @Test
    void createButton_whenServiceFails_displaysError()
            throws Exception {

        setService.sets = List.of(testSet());

        cardService.failure =
                new RuntimeException("Could not save card");

        Scene scene = showCreateCard();

        onFx(() -> {
            enterValidForm(scene);
            createButton(scene).fire();
        });

        Button button = allNodes(scene.getRoot()).stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(b ->
                        I18n.t("createCard.exception").equals(b.getText()))
                .findFirst()
                .orElseThrow();

        assertFalse(button.isDisabled());

        assertEquals(1, cardService.callCount);

        assertEquals(
                Alert.AlertType.ERROR,
                createCard.alerts.get(0).type()
        );

        assertEquals(
                I18n.t("createCard.createButton.error"),
                createCard.alerts.get(0).title()
        );
    }

    // Fake service records calls instead of accessing the database.
    private static class FakeFlashcardService extends FlashcardService {

        private int callCount;
        private int receivedUserId;
        private int receivedSetId;
        private String receivedQuestion;
        private String receivedAnswer;
        private RuntimeException failure;

        FakeFlashcardService() {
            super(
                    new FlashcardRepository(),
                    new FlashcardSetRepository()
            );
        }

        @Override
        public Flashcard createFlashcard(
                int requestingUserId,
                int setId,
                String question,
                String answer
        ) {
            callCount++;

            receivedUserId = requestingUserId;
            receivedSetId = setId;
            receivedQuestion = question;
            receivedAnswer = answer;

            if (failure != null) {
                throw failure;
            }

            return new Flashcard(
                    setId,
                    question,
                    answer
            );
        }
    }

    // Fake service returns test sets or simulates a loading failure.
    private static class FakeFlashcardSetService
            extends FlashcardSetService {

        private List<FlashcardSet> sets = List.of();
        private RuntimeException failure;
        private int callCount;
        private int requestedUserId;

        FakeFlashcardSetService() {
            super(new FlashcardSetRepository());
        }

        @Override
        public List<FlashcardSet> getFlashcardSetsForUser(int userId) {
            callCount++;
            requestedUserId = userId;

            if (failure != null) {
                throw failure;
            }

            return sets;
        }
    }

    // Captures alerts instead of displaying modal dialogs during tests.
    private static class TestCreateCard extends CreateCard {

        private final List<AlertRecord> alerts = new ArrayList<>();

        TestCreateCard(
                User user,
                FlashcardService cardService,
                FlashcardSetService setService
        ) {
            super(user, cardService, setService);
        }

        @Override
        void showAlert(
                Alert.AlertType type,
                String title,
                String message
        ) {
            alerts.add(new AlertRecord(type, title, message));
        }
    }

    private record AlertRecord(
            Alert.AlertType type,
            String title,
            String message
    ) {}
}