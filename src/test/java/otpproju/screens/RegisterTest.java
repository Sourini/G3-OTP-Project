package otpproju.screens;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.config.I18n;
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    private FakeUserService service;
    private TestRegister register;
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

    private static <T> T onFx(Callable<T> action) throws Exception {
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

        assertTrue(done.await(10, TimeUnit.SECONDS), "FX action timed out");

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
        service = new FakeUserService();
        register = new TestRegister(service);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null) {
            onFx(stage::close);
            stage = null;
        }

        I18n.setLocale(originalLocale);
    }

    private Scene showRegister() throws Exception {
        return onFx(() -> {
            stage = new Stage();
            register.show(stage);
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

    private static TextField textField(Scene scene, String prompt) {
        return allNodes(scene.getRoot()).stream()
                .filter(TextField.class::isInstance)
                .map(TextField.class::cast)
                .filter(field -> prompt.equals(field.getPromptText()))
                .findFirst()
                .orElseThrow();
    }

    private static PasswordField passwordField(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(PasswordField.class::isInstance)
                .map(PasswordField.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static Button registerButton(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button ->
                        I18n.t("register.createButton").equals(button.getText()))
                .findFirst()
                .orElseThrow();
    }

    private void enterValidForm(Scene scene) {
        textField(scene, I18n.t("register.username"))
                .setText("Olivia");

        textField(scene, I18n.t("register.email"))
                .setText("olivia@example.com");

        passwordField(scene).setText("secret123");
    }

    @Test
    void show_displaysRegistrationForm() throws Exception {
        Scene scene = showRegister();

        assertNotNull(scene);
        assertEquals(I18n.t("register.title"), stage.getTitle());

        assertNotNull(textField(scene, I18n.t("register.username")));
        assertNotNull(textField(scene, I18n.t("register.email")));
        assertNotNull(passwordField(scene));
        assertNotNull(registerButton(scene));

        assertFalse(scene.getStylesheets().isEmpty());
        assertTrue(scene.getStylesheets().get(0).endsWith("style.css"));
    }

    @Test
    void show_appliesCurrentTextOrientation() throws Exception {
        Scene scene = showRegister();

        assertEquals(
                I18n.getOrientation(),
                scene.getRoot().getNodeOrientation()
        );
    }

    @Test
    void registerButton_rejectsEmptyFields() throws Exception {
        Scene scene = showRegister();

        onFx(() -> registerButton(scene).fire());

        assertEquals(0, service.callCount);
        assertEquals(1, register.alerts.size());
        assertEquals(Alert.AlertType.ERROR, register.alerts.get(0).type());
        assertEquals(
                I18n.t("register.emptyField.title"),
                register.alerts.get(0).title()
        );
    }

    @Test
    void registerButton_rejectsPasswordShorterThanSixCharacters()
            throws Exception {
        Scene scene = showRegister();

        onFx(() -> {
            textField(scene, I18n.t("register.username"))
                    .setText("Olivia");

            textField(scene, I18n.t("register.email"))
                    .setText("olivia@example.com");

            passwordField(scene).setText("12345");
            registerButton(scene).fire();
        });

        assertEquals(0, service.callCount);
        assertEquals(
                I18n.t("register.passwordShortError.title"),
                register.alerts.get(0).title()
        );
    }

    @Test
    void registerButton_passesTrimmedInputToUserService()
            throws Exception {
        Scene scene = showRegister();

        onFx(() -> {
            textField(scene, I18n.t("register.username"))
                    .setText("  Olivia  ");

            textField(scene, I18n.t("register.email"))
                    .setText("  olivia@example.com  ");

            passwordField(scene).setText("secret123");
            registerButton(scene).fire();
        });

        assertEquals(1, service.callCount);
        assertEquals("Olivia", service.receivedUsername);
        assertEquals("olivia@example.com", service.receivedEmail);
        assertEquals("secret123", service.receivedPassword);

        assertEquals(
                Alert.AlertType.INFORMATION,
                register.alerts.get(0).type()
        );

        // Successful registration navigates to the login screen.
        assertEquals(I18n.t("app.title"), stage.getTitle());
    }

    @Test
    void registerButton_restoresButtonWhenRegistrationFails()
            throws Exception {
        Scene scene = showRegister();
        service.failure = new RuntimeException("Registration failed");

        onFx(() -> {
            enterValidForm(scene);
            registerButton(scene).fire();
        });

        Button button = registerButton(scene);

        assertFalse(button.isDisabled());
        assertEquals(I18n.t("register.createButton"), button.getText());
        assertEquals(
                I18n.t("register.createError"),
                register.alerts.get(0).title()
        );
    }

    @Test
    void loginLink_navigatesToLoginScreen() throws Exception {
        Scene scene = showRegister();

        Hyperlink loginLink = allNodes(scene.getRoot()).stream()
                .filter(Hyperlink.class::isInstance)
                .map(Hyperlink.class::cast)
                .filter(link ->
                        I18n.t("register.loginButton").equals(link.getText()))
                .findFirst()
                .orElseThrow();

        onFx(loginLink::fire);

        assertEquals(I18n.t("app.title"), stage.getTitle());
    }

    private static class FakeUserService extends UserService {

        private int callCount;
        private String receivedUsername;
        private String receivedEmail;
        private String receivedPassword;
        private RuntimeException failure;

        FakeUserService() {
            super(new UserRepository());
        }

        @Override
        public User registerUser(
                String username,
                String email,
                String password
        ) {
            callCount++;
            receivedUsername = username;
            receivedEmail = email;
            receivedPassword = password;

            if (failure != null) {
                throw failure;
            }

            return new User(
                    username,
                    email,
                    "test-hash",
                    User.UserType.STUDENT
            );
        }
    }

    private static class TestRegister extends Register {

        private final List<AlertRecord> alerts = new ArrayList<>();

        TestRegister(UserService service) {
            super(service);
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