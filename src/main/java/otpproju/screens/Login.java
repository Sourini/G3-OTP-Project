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

public class Login {
    private final UserService userService;

    public Login() {
        UserRepository userRepository = new UserRepository();
        userService = new UserService(userRepository);
    }

    public void show(Stage stage) {

        Label appTitle = new Label("G3 OTP FLASHCARDS");
        appTitle.getStyleClass().add("app-title");

        Label title = new Label("Welcome back");
        title.getStyleClass().add("page-title");

        Label description = new Label(
                "Login to continue studying."
        );

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(45);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(45);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(45);

        Label registerText = new Label(
                "Don't have an account?"
        );

        Hyperlink registerLink = new Hyperlink(
                "Create an account"
        );

        loginButton.setOnAction(e -> {

            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Missing fields",
                        "Please enter your email and password."
                );
                return;
            }

            loginButton.setDisable(true);
            loginButton.setText("Logging in...");

            try {
                User user = userService.login(
                        email,
                        password
                );
                loginButton.setDisable(false);
                loginButton.setText("Login");
                new UserPage(user).show(stage);
            } catch (Exception ex) {

                loginButton.setDisable(false);
                loginButton.setText("Login");
                showAlert(
                        Alert.AlertType.ERROR,
                        "Login failed",
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

        stage.setTitle("G3 OTP Flashcards");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}