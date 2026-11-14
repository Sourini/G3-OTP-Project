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
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class LoginTest {

    // A fake "database" that keeps users in memory, so no real database is needed.
    static class FakeUserRepository extends UserRepository {
        private final List<User> users = new ArrayList<>();
        private int nextId = 1;

        @Override
        public User create(User user) {
            User saved = new User(
                    nextId++,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getUserType(),
                    LocalDateTime.now());
            users.add(saved);
            return saved;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }
    }

    // Remembers which user was logged in (instead of opening the real UserPage).
    private AtomicReference<User> loggedInUser;

    @Start
    private void start(Stage stage) {
        loggedInUser = new AtomicReference<>();

        UserService service = new UserService(new FakeUserRepository());
        service.registerUser("alice", "alice@example.com", "secret123");

        new Login(service, (user, st) -> loggedInUser.set(user)).show(stage);
    }

    private void login(FxRobot robot, String email, String password) {
        robot.clickOn(".text-field").write(email);
        robot.clickOn(".password-field").write(password);
        robot.clickOn(I18n.t("login.button"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private DialogPane alert(FxRobot robot) {
        return robot.lookup(".dialog-pane")
                .tryQueryAs(DialogPane.class)
                .orElseGet(() -> fail("Expected an error alert"));
    }

    private void closeAlert(FxRobot robot) {
        robot.clickOn(".dialog-pane .button");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void correctLogin_succeeds(FxRobot robot) {
        login(robot, "alice@example.com", "secret123");

        assertNotNull(loggedInUser.get());
        assertEquals("alice", loggedInUser.get().getUsername());
    }

    @Test
    void emailWithSpacesAndCapitals_stillWorks(FxRobot robot) {
        login(robot, "   ALICE@Example.com  ", "secret123");

        assertNotNull(loggedInUser.get());
    }

    @Test
    void wrongPassword_showsError(FxRobot robot) {
        login(robot, "alice@example.com", "wrong-password");

        assertEquals("Invalid email or password", alert(robot).getContentText());
        closeAlert(robot);

        assertNull(loggedInUser.get());
        Button button = robot.lookup(".button").queryAs(Button.class);
        assertFalse(button.isDisabled());   // button works again after the error
    }

    @Test
    void unknownEmail_showsSameError(FxRobot robot) {
        login(robot, "nobody@example.com", "secret123");

        assertEquals("Invalid email or password", alert(robot).getContentText());
        closeAlert(robot);

        assertNull(loggedInUser.get());
    }

    @Test
    void badEmailFormat_showsError(FxRobot robot) {
        login(robot, "not-an-email", "secret123");

        assertEquals("Email address is invalid", alert(robot).getContentText());
        closeAlert(robot);

        assertNull(loggedInUser.get());
    }

    @Test
    void emptyFields_showMissingFieldsAlert(FxRobot robot) {
        robot.clickOn(I18n.t("login.button"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(I18n.t("login.missingFields.message"), alert(robot).getContentText());
        closeAlert(robot);

        assertNull(loggedInUser.get());
    }
}