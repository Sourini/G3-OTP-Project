package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.User;
import otpproju.repository.UserRepository;
import otpproju.service.UserService;

public class Register {

    private final UserService userService;

    public Register() {

        UserRepository userRepository =
                new UserRepository();

        userService =
                new UserService(userRepository);
    }

    public void show(Stage stage) {

        Label appTitle =
                new Label("Simply Flashcard");
        appTitle.getStyleClass().add("app-title");

        Label title =
                new Label("Create Account");
        title.getStyleClass().add("page-title");

        Label description =
                new Label(
                        "Create an account to start studying."
                );

        TextField usernameField =
                new TextField();

        usernameField.setPromptText(
                "Username"
        );

        usernameField.setPrefHeight(45);

        TextField emailField =
                new TextField();

        emailField.setPromptText(
                "Email"
        );

        emailField.setPrefHeight(45);

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                "Password"
        );

        passwordField.setPrefHeight(45);

        Button registerButton =
                new Button("Create Account");

        registerButton.setMaxWidth(
                Double.MAX_VALUE
        );

        registerButton.setPrefHeight(45);

        Label loginText =
                new Label(
                        "Already have an account?"
                );

        Hyperlink loginLink =
                new Hyperlink("Login");

        registerButton.setOnAction(e -> {

            String username =
                    usernameField.getText().trim();

            String email =
                    emailField.getText().trim();

            String password =
                    passwordField.getText();

            if (username.isEmpty()
                    || email.isEmpty()
                    || password.isEmpty()) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Missing fields",
                        "Please fill in all fields."
                );

                return;
            }

            if (password.length() < 6) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Password too short",
                        "Password must contain at least 6 characters."
                );

                return;
            }

            registerButton.setDisable(true);
            registerButton.setText(
                    "Creating account..."
            );

            try {

                User user =
                        userService.registerUser(
                                username,
                                email,
                                password
                        );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Account created",
                        "Your account has been created successfully."
                );

                new Login().show(stage);

            } catch (Exception ex) {

                registerButton.setDisable(false);
                registerButton.setText(
                        "Create Account"
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration failed",
                        ex.getMessage()
                );
            }
        });

        loginLink.setOnAction(e ->
                new Login().show(stage)
        );

        VBox box =
                new VBox(
                        12
                );

        box.setAlignment(
                Pos.CENTER
        );

        box.setPadding(
                new Insets(30)
        );

        box.setMaxWidth(330);

        box.getChildren().addAll(
                appTitle,
                new Label(""),
                title,
                description,
                new Label(""),
                usernameField,
                emailField,
                passwordField,
                registerButton,
                new Label(""),
                loginText,
                loginLink
        );

        VBox root =
                new VBox(box);

        root.setAlignment(
                Pos.CENTER
        );

        Scene scene =
                new Scene(
                        root,
                        390,
                        750
                );

        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setTitle("Create Account");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}