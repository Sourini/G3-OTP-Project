package otpproju.screens;

import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import otpproju.config.I18n;
import otpproju.model.FlashcardSet;
import otpproju.model.SetText;
import java.util.Map;
import otpproju.model.User;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardSetService;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class CreateCardSetTest {

    // A fake service: it records what the screen asked for, and never touches a database.
    static class FakeFlashcardSetService extends FlashcardSetService {
        volatile int calls = 0;
        volatile int lastUserId;
        volatile String lastTitle;
        volatile String lastDescription;
        volatile RuntimeException errorToThrow;

        FakeFlashcardSetService() {
            super(new FlashcardSetRepository());   // the repository is never used
        }

        @Override
        public FlashcardSet createMultilingualSet(int userId, String language, Map<String, SetText> texts) {
            SetText text = texts.get(language);
            return createFlashcardSet(userId, text.title().trim(), text.description().trim());
        }

        @Override
        public FlashcardSet createFlashcardSet(int userId, String title, String description) {
            calls++;
            lastUserId = userId;
            lastTitle = title;
            lastDescription = description;
            if (errorToThrow != null) {
                throw errorToThrow;
            }
            return null;
        }
    }

    private FakeFlashcardSetService service;
    private AtomicBoolean navigatedToDashboard;

    @Start
    private void start(Stage stage) {
        service = new FakeFlashcardSetService();
        navigatedToDashboard = new AtomicBoolean(false);

        User user = new User(
                7, "alice", "alice@example.com", "hash",
                User.UserType.STUDENT, LocalDateTime.now());

        // The callback replaces "open the real UserPage", so no database is touched.
        new CreateCardSet(user, service, st -> navigatedToDashboard.set(true)).show(stage);
    }

    private void fillForm(FxRobot robot, String title, String description) {
        robot.clickOn(".text-field").write(title);
        robot.clickOn(".text-area").write(description);
    }

    private Button buttonWithText(FxRobot robot, String text) {
        return robot.lookup(".button").queryAllAs(Button.class).stream()
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No button with text: " + text));
    }

    private void clickCreate(FxRobot robot) {
        robot.clickOn(buttonWithText(robot, I18n.t("createCardSet.createButton")));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private DialogPane alert(FxRobot robot) {
        return robot.lookup(".dialog-pane")
                .tryQueryAs(DialogPane.class)
                .orElseGet(() -> fail("Expected an alert"));
    }

    private void closeAlert(FxRobot robot) {
        robot.clickOn(".dialog-pane .button");
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void waitUntil(FxRobot robot, BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 3000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            robot.sleep(50);
        }
    }

    @Test
    void emptyTitle_showsError_andDoesNotCallService(FxRobot robot) {
        clickCreate(robot);

        assertEquals(I18n.t("content.setFieldsRequired"), alert(robot).getContentText());
        closeAlert(robot);

        assertEquals(0, service.calls);
        assertFalse(navigatedToDashboard.get());
    }

    @Test
    void whitespaceOnlyTitle_isTreatedAsEmpty(FxRobot robot) {
        fillForm(robot, "     ", "some description");
        clickCreate(robot);

        assertEquals(I18n.t("content.setFieldsRequired"), alert(robot).getContentText());
        closeAlert(robot);

        assertEquals(0, service.calls);
    }

    @Test
    void validForm_savesTrimmedValues_showsSuccess_thenNavigates(FxRobot robot) {
        fillForm(robot, "  Biology  ", "  Chapter 1  ");
        clickCreate(robot);

        assertEquals(I18n.t("createCardSet.created.message"), alert(robot).getContentText());
        assertFalse(navigatedToDashboard.get(), "Should not navigate before the alert is closed");

        closeAlert(robot);
        waitUntil(robot, navigatedToDashboard::get);

        assertEquals(1, service.calls);
        assertEquals(7, service.lastUserId);
        assertEquals("Biology", service.lastTitle);
        assertEquals("Chapter 1", service.lastDescription);
        assertTrue(navigatedToDashboard.get());
    }

    @Test
    void emptyDescription_isRejected(FxRobot robot) {
        fillForm(robot, "Biology", "");
        clickCreate(robot);
        assertEquals(I18n.t("content.setFieldsRequired"), alert(robot).getContentText());
        closeAlert(robot);
        assertEquals(0, service.calls);
        assertFalse(navigatedToDashboard.get());
    }

    @Test
    void serviceFailure_showsMessage_reEnablesButton_andStaysOnPage(FxRobot robot) {
        service.errorToThrow = new IllegalArgumentException("Title must not exceed 100 characters");

        fillForm(robot, "Biology", "Chapter 1");
        clickCreate(robot);

        assertEquals("Title must not exceed 100 characters", alert(robot).getContentText());
        closeAlert(robot);

        assertFalse(navigatedToDashboard.get());

        Button create = buttonWithText(robot, I18n.t("createCardSet.createButton"));
        assertFalse(create.isDisabled());
        assertEquals(I18n.t("createCardSet.createButton"), create.getText());
    }

    @Test
    void backButton_navigatesWithoutSaving(FxRobot robot) {
        robot.clickOn(buttonWithText(robot, I18n.t("createCardSet.backButton")));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(navigatedToDashboard.get());
        assertEquals(0, service.calls);
    }
}