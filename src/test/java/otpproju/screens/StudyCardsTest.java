package otpproju.screens;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class StudyCardsTest {

    // fake services, never touch the database
    static class FakeFlashcardSetService extends FlashcardSetService {
        volatile List<FlashcardSet> sets = new ArrayList<>();
        volatile RuntimeException error;

        FakeFlashcardSetService() {
            super(new FlashcardSetRepository());   // never used
        }

        @Override
        public otpproju.model.SetText getStudyText(FlashcardSet set, String language) {
            return new otpproju.model.SetText(
                    "ar".equals(language) ? "علم الأحياء" : set.getTitle(), set.getDescription());
        }

        @Override
        public List<FlashcardSet> getFlashcardSetsForUser(int userId) {
            if (error != null) {
                throw error;
            }
            return sets;
        }
    }

    static class FakeFlashcardService extends FlashcardService {
        final Map<Integer, List<Flashcard>> cardsBySet = new HashMap<>();
        volatile RuntimeException error;
        volatile int calls = 0;
        final Map<String, List<Flashcard>> translations = new HashMap<>();

        FakeFlashcardService() {
            super(new FlashcardRepository(), new FlashcardSetRepository());   // never used
        }

        @Override
        public List<Flashcard> getFlashcardsForSet(FlashcardSet set, String languageCode) {
            List<Flashcard> originals = getFlashcardsForSet(set.getSetId());
            return translations.getOrDefault(languageCode, originals);
        }

        @Override
        public List<Flashcard> getFlashcardsForSet(int setId) {
            calls++;
            if (error != null) {
                throw error;
            }
            return cardsBySet.getOrDefault(setId, List.of());
        }
    }

    private Stage stage;
    private User user;
    private FakeFlashcardSetService setService;
    private FakeFlashcardService cardService;
    private AtomicBoolean navigatedToDashboard;
    private FlashcardSet biology;
    private FlashcardSet chemistry;

    private static Flashcard card(int id, int setId, String question, String answer) {
        LocalDateTime now = LocalDateTime.now();
        return new Flashcard(id, setId, question, answer, now, now);
    }

    @Start
    private void start(Stage stage) {
        this.stage = stage;

        LocalDateTime now = LocalDateTime.now();
        user = new User(7, "testuser", "test@example.com", "hash", User.UserType.STUDENT, now);
        biology = new FlashcardSet(1, 7, "Biology", "Bio set", now, now);
        chemistry = new FlashcardSet(2, 7, "Chemistry", "Chem set", now, now);

        setService = new FakeFlashcardSetService();
        setService.sets.add(biology);
        setService.sets.add(chemistry);

        cardService = new FakeFlashcardService();
        cardService.cardsBySet.put(1, List.of(
                card(1, 1, "Q1", "A1"),
                card(2, 1, "Q2", "A2"),
                card(3, 1, "Q3", "A3")));
        cardService.cardsBySet.put(2, List.of());   // an empty set

        navigatedToDashboard = new AtomicBoolean(false);
    }

    private void open(FlashcardSet initialSet) {
        Platform.runLater(() ->
                new StudyCards(user, initialSet, cardService, setService,
                        st -> navigatedToDashboard.set(true)).show(stage));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private ComboBox<FlashcardSet> combo(FxRobot robot) {
        return robot.lookup("#study-set-selector").queryAs(ComboBox.class);
    }

    private void selectSet(FxRobot robot, FlashcardSet set) {
        ComboBox<FlashcardSet> box = combo(robot);
        robot.interact(() -> box.setValue(set));
    }

    private Button button(FxRobot robot, String text) {
        return robot.lookup(".button").queryAllAs(Button.class).stream()
                .filter(b -> text.equals(b.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No button with text: " + text));
    }

    private Button loadButton(FxRobot robot)   { return button(robot, I18n.t("studyCards.loadButton")); }
    private Button answerButton(FxRobot robot) { return button(robot, I18n.t("studyCards.answerButton")); }
    private Button nextButton(FxRobot robot)   { return button(robot, I18n.t("studyCards.next")); }
    private Button backButton(FxRobot robot)   { return button(robot, I18n.t("app.backButton")); }

    private void click(FxRobot robot, Button button) {
        robot.clickOn(button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String cardText(FxRobot robot)     { return robot.lookup(".flashcard").queryLabeled().getText(); }
    private String progressText(FxRobot robot) { return robot.lookup(".progress-label").queryLabeled().getText(); }
    private String titleText(FxRobot robot)    { return robot.lookup(".page-title").queryLabeled().getText(); }

    private DialogPane alert(FxRobot robot) {
        return robot.lookup(".dialog-pane")
                .tryQueryAs(DialogPane.class)
                .orElseGet(() -> fail("Expected an alert"));
    }

    private void closeAlert(FxRobot robot) {
        robot.clickOn(".dialog-pane .button");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void loadBiology(FxRobot robot) {
        selectSet(robot, biology);
        click(robot, loadButton(robot));
    }

    @Test
    @SuppressWarnings("unchecked")
    void switchingStudyLanguageChangesContentWithoutChangingInterface(FxRobot robot) {
        Locale interfaceLocale = I18n.getLocale();
        cardService.translations.put("ar", List.of(
                card(1, 1, "ما هي جافا؟", "لغة برمجة")));
        biology.setLanguages("en", List.of("en", "ar"));
        open(biology);

        ComboBox<String> selector = robot.lookup("#study-language-selector").queryAs(ComboBox.class);
        assertEquals(List.of("en", "ar"), selector.getItems());
        robot.interact(() -> selector.setValue("ar"));

        assertEquals("ما هي جافا؟", cardText(robot));
        assertEquals("علم الأحياء", titleText(robot));
        assertEquals(interfaceLocale, I18n.getLocale());
        assertEquals(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT,
                robot.lookup(".flashcard").queryLabeled().getEffectiveNodeOrientation());
        robot.interact(() -> selector.setValue("en"));
        assertEquals("Q1", cardText(robot));
    }

    @Test
    void dropdown_listsTheUsersSets(FxRobot robot) {
        open(null);

        assertEquals(List.of(biology, chemistry), new ArrayList<>(combo(robot).getItems()));
    }

    @Test
    void initialState_asksUserToChooseASet_andButtonsAreDisabled(FxRobot robot) {
        open(null);

        assertEquals(I18n.t("studyCards.chooseSet"), progressText(robot));
        assertTrue(answerButton(robot).isDisabled());
        assertTrue(nextButton(robot).isDisabled());
    }

    @Test
    void setPassedToConstructor_isLoadedImmediately(FxRobot robot) {
        open(biology);

        assertEquals("Biology", titleText(robot));
        assertEquals("Q1", cardText(robot));
        assertEquals(I18n.format("studyCards.progress", 1, 3), progressText(robot));
    }

    @Test
    void setLoadFailure_showsAlertWithMessage(FxRobot robot) {
        setService.error = new IllegalStateException("db down");

        open(null);

        assertEquals("db down", alert(robot).getContentText());
        closeAlert(robot);
    }

    @Test
    void loadWithoutSelection_showsAlert_andDoesNotLoadCards(FxRobot robot) {
        open(null);

        click(robot, loadButton(robot));

        assertEquals(I18n.t("studyCards.select.message"), alert(robot).getContentText());
        closeAlert(robot);
        assertEquals(0, cardService.calls);
    }

    @Test
    void loadingASet_showsFirstQuestionAndProgress(FxRobot robot) {
        open(null);

        loadBiology(robot);

        assertEquals("Biology", titleText(robot));
        assertEquals("Q1", cardText(robot));
        assertEquals(I18n.format("studyCards.progress", 1, 3), progressText(robot));
        assertFalse(answerButton(robot).isDisabled());
        assertTrue(nextButton(robot).isDisabled());
    }

    @Test
    void loadingAnEmptySet_showsNoCardsMessage_andDisablesButtons(FxRobot robot) {
        open(null);

        selectSet(robot, chemistry);
        click(robot, loadButton(robot));

        assertEquals(I18n.t("studyCards.noCards1") + I18n.t("studyCards.noCards2"), cardText(robot));
        assertEquals(I18n.t("studyCards.0cards"), progressText(robot));
        assertTrue(answerButton(robot).isDisabled());
        assertTrue(nextButton(robot).isDisabled());
    }

    @Test
    void cardLoadFailure_showsAlertWithMessage(FxRobot robot) {
        cardService.error = new RuntimeException("boom");
        open(null);

        loadBiology(robot);

        assertEquals("boom", alert(robot).getContentText());
        closeAlert(robot);
    }

    @Test
    void answerButton_revealsAnswer_andEnablesNext(FxRobot robot) {
        open(null);
        loadBiology(robot);

        click(robot, answerButton(robot));

        assertEquals("A1", cardText(robot));
        assertTrue(answerButton(robot).isDisabled());
        assertFalse(nextButton(robot).isDisabled());
    }

    @Test
    void nextButton_movesToTheNextQuestion(FxRobot robot) {
        open(null);
        loadBiology(robot);

        click(robot, answerButton(robot));
        click(robot, nextButton(robot));

        assertEquals("Q2", cardText(robot));
        assertEquals(I18n.format("studyCards.progress", 2, 3), progressText(robot));
        assertFalse(answerButton(robot).isDisabled());
        assertTrue(nextButton(robot).isDisabled());
    }

    @Test
    void finishingAllCards_showsCompletionMessage(FxRobot robot) {
        open(null);
        loadBiology(robot);

        for (int i = 0; i < 3; i++) {
            click(robot, answerButton(robot));
            click(robot, nextButton(robot));
        }

        assertEquals(I18n.format("studyCards.completed", 3), progressText(robot));
        assertEquals(I18n.t("studyCards.congrats"), cardText(robot));
        assertTrue(answerButton(robot).isDisabled());
        assertTrue(nextButton(robot).isDisabled());
    }

    @Test
    void loadingAnotherSet_startsFromTheFirstCardAgain(FxRobot robot) {
        open(null);
        loadBiology(robot);
        click(robot, answerButton(robot));
        click(robot, nextButton(robot));

        click(robot, loadButton(robot));

        assertEquals("Q1", cardText(robot));
        assertEquals(I18n.format("studyCards.progress", 1, 3), progressText(robot));
    }

    @Test
    void backButton_navigatesToDashboard(FxRobot robot) {
        open(null);

        click(robot, backButton(robot));

        assertTrue(navigatedToDashboard.get());
        assertEquals(0, cardService.calls);
    }
}