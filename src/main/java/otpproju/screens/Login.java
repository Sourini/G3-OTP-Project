package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.service.UserService;
import otpproju.config.I18n;
import javafx.geometry.NodeOrientation;
import java.util.function.BiConsumer;

// Displays the login screen and handles user authentication.
// Successful login redirects the user to their dashboard.
public class Login {
    private final UserService userService;
    private final BiConsumer<User, Stage> onLoginSuccess;

    public Login() {
        this(
                new UserService(new UserRepository()),
                (user, stage) -> new UserPage(user).show(stage)
        );
    }

    // visible for testing
    Login(UserService userService, BiConsumer<User, Stage> onLoginSuccess) {
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;
    }

    // Builds and displays the login form, including input fields,
    // login validation, and navigation to registration.
    public void show(Stage stage) {

        Label appTitle = new Label(I18n.t("app.title"));
        appTitle.getStyleClass().add("app-title");

        Label title = UIComponents.pageTitle(
                I18n.t("login.title")
        );

        Label description = new Label(
                I18n.t("login.description")
        );

        // EMAIL
        TextField emailField = new TextField();
        emailField.setNodeOrientation(
                NodeOrientation.LEFT_TO_RIGHT
        );
        emailField.setPromptText(I18n.t("login.email"));
        emailField.setPrefHeight(45);

        // PASSWORD
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(I18n.t("login.password"));
        passwordField.setPrefHeight(45);

        // REGISTER HYPERLINK / LOGIN
        Button loginButton = UIComponents.primaryButton(
                I18n.t("login.button")
        );

        Label registerText = new Label(
                I18n.t("login.noAccount")
        );

        Hyperlink registerLink = new Hyperlink(
                I18n.t("login.registerAccount")
        );

        // CHECK EMAIL+PASSWORD VALIDITY
        loginButton.setOnAction(e -> {

            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("login.missingFields.title"),
                        I18n.t("login.missingFields.message")
                );
                return;
            }

            loginButton.setDisable(true);
            loginButton.setText(I18n.t("login.buttonLoading"));

            try {
                User user = userService.login(
                        email,
                        password
                );
                loginButton.setDisable(false);
                loginButton.setText(I18n.t("login.button"));
                onLoginSuccess.accept(user, stage);
            } catch (Exception ex) {

                loginButton.setDisable(false);
                loginButton.setText(I18n.t("login.button"));
                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("login.failed.title"),
                        ex.getMessage()
                );
            }
        });

        registerLink.setOnAction(e ->
                new Register().show(stage)
        );

        VBox box = new VBox(12);

        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setMaxWidth(330);

        box.getChildren().addAll(
                appTitle,
                new Label(""),
                title,
                description,
                new Label(""),
                emailField,
                passwordField,
                loginButton,
                new Label(""),
                registerText,
                registerLink
        );

        VBox root = new VBox(box);

        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(
                root,
                390,
                750
        );

        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setTitle(I18n.t("app.title"));
        scene.getRoot().setNodeOrientation(I18n.getOrientation());
        stage.setScene(scene);
        stage.show();
    }

    // Displays a localized alert using the current text direction.
    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(
                I18n.getOrientation()
        );
        alert.showAndWait();
    }
}