package otpproju.screens;

import javafx.geometry.Insets; 
import javafx.geometry.Pos; 
import javafx.scene.Scene; 
import javafx.scene.control.*; 
import javafx.scene.layout.VBox; 
import javafx.stage.Stage; 

import java.net.URI; 
import java.net.http.HttpClient; 
import java.net.http.HttpRequest; 
import java.net.http.HttpResponse;

public class Register {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void show(Stage stage) {
        // Title
        Label title = new Label("Create Account");

        // Desc
        Label desc = new Label(
            "Create an account to start using your flashcards."
        );

        // Username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        // Password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");

        // Register button
        Button registerButton = new Button("Create Account");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        // Login link
        Label loginText = new Label("Already have an account?");
        Hyperlink loginLink = new Hyperlink("Login here.");

        // Register action
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Alert alerts = new Alert(Alert.AlertType.ERROR);
                alerts.setTitle("Error");
                alerts.setHeaderText("Missing fields");
                alerts.setContentText("Please fill in all fields.");
                alerts.showAndWait();
                return;
            }

            if (password.length() < 6) {
                Alert alerts = new Alert(Alert.AlertType.ERROR);
                alerts.setTitle("Error");
                alerts.setHeaderText("Weak password");
                alerts.setContentText("Password must be at least 6 characters long.");
                alerts.showAndWait();
                return;
            }

            registerButton.setDisable(true);
            registerButton.setText("Creating account...");
            registerUser(username, email, password, stage, registerButton);
        });

        // Login nav
        loginLink.setOnAction(e -> {
            new Login().show(stage);
        });

        // Layout
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(30));
        container.setMaxWidth(400);

        container.getChildren().addAll(
                title,
                desc,
                new Label(""),
                usernameLabel,
                usernameField,
                emailLabel,
                emailField,
                passwordLabel,
                passwordField,
                registerButton,
                new Label(""),
                loginText,
                loginLink
        );

        VBox root = new VBox(container);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }

    private void registerUser(
            String username,
            String email,
            String password,
            Stage stage,
            Button registerButton
    ) {
        String json = String.format(
                "{\"username\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}",
                escapeJson(username), escapeJson(email), escapeJson(password)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        Thread thread = new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Create Account");

                    if (response.statusCode() == 201) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Account created");
                        alert.setContentText("Your account has been created successfully. You can now log in.");
                        alert.showAndWait();
                        new Login().show(stage);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Registration failed");
                        alert.setContentText("An error occurred while creating your account. Please try again.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Create Account");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Registration failed");
                    alert.setContentText("An error occurred while creating your account. Please try again.");
                    alert.showAndWait();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"");
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}