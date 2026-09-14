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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Login {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void show(Stage stage) {
        // Title
        Label title = new Label("Login");

        // Desc
        Label desc = new Label(
            "Login to access your flashcards."
        );

        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        // Password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        // Register link
        Label registerText = new Label("Don't have an account?");
        Hyperlink registerLink = new Hyperlink("Register here.");

        // Login action
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                Alert alerts = new Alert(Alert.AlertType.ERROR);
                alerts.setTitle("Error");
                alerts.setHeaderText("Missing fields");
                alerts.setContentText("Please fill in all fields.");
                alerts.showAndWait();
            } else {
                loginButton.setDisable(true);
                loginButton.setText("Logging in...");
                login(email, password, loginButton);
            }
        });

        // Register nav
        registerLink.setOnAction(e -> {
            new Register().show(stage);
        });

        // Layout
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setMaxWidth(300);

        container.getChildren().addAll(
                title,
                desc,
                new Label(""),
                emailLabel,
                emailField,
                passwordLabel,
                passwordField,
                loginButton,
                new Label(""),
                registerText,
                registerLink
        );

        VBox root = new VBox(container);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    private void login(
            String email,
            String password,
            Button loginButton
    ) {
        String json = String.format(
                "{\"email\":\"%s\", \"password\":\"%s\"}",
                escapeJson(email), escapeJson(password)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Run HTTP request in background so JavaFX doesn't freeze
        Thread thread = new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false); // Re-enable the button
                    loginButton.setText("Login"); // Reset text

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        System.out.println(
                            "LOGIN RESPONSE: " + response.body()
                        );

                        // Something that backend returns here (user, token..)

                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Login Successful",
                                "You have successfully logged in."
                        );

                        // TODO: save user/token, nav to main screen, etc.

                    } else {
                        showAlert(
                                Alert.AlertType.ERROR,
                                "Login Failed",
                                "Invalid username, email, or password."
                        );
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Login");

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Connection Error",
                            "Could not connect to the server."
                    );
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}