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
import otpproju.config.I18n;

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
                new Label(I18n.t("app.title"));
        appTitle.getStyleClass().add("app-title");

        Label title =
                new Label(I18n.t("register.title"));
        title.getStyleClass().add("page-title");

        Label description =
                new Label(
                        I18n.t("register.desc")
                );

        TextField usernameField =
                new TextField();

        usernameField.setPromptText(
                I18n.t("register.username")
        );

        usernameField.setPrefHeight(45);

        TextField emailField =
                new TextField();

        emailField.setPromptText(
                I18n.t("register.email")
        );

        emailField.setPrefHeight(45);

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                I18n.t("register.password")
        );

        passwordField.setPrefHeight(45);

        Button registerButton =
                new Button(I18n.t("register.createButton"));

        registerButton.setMaxWidth(
                Double.MAX_VALUE
        );

        registerButton.setPrefHeight(45);

        Label loginText =
                new Label(
                        I18n.t("register.loginInstead")
                );

        Hyperlink loginLink =
                new Hyperlink(I18n.t("register.loginButton"));

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
                        I18n.t("register.emptyField.title"),
                        I18n.t("register.emptyField.message")
                );

                return;
            }

            if (password.length() < 6) {

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("register.passwordShortError.title"),
                        I18n.t("register.passwordShortError.message")
                );

                return;
            }

            registerButton.setDisable(true);
            registerButton.setText(
                    I18n.t("register.creating")
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
                        I18n.t("register.created.title"),
                        I18n.t("register.created.message")
                );

                new Login().show(stage);

            } catch (Exception ex) {

                registerButton.setDisable(false);
                registerButton.setText(
                        I18n.t("register.createButton")
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("register.createError"),
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

        stage.setTitle(I18n.t("register.title"));
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