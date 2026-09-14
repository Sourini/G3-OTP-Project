package otpproju.screens;

import javafx.application.Platform;
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

public class UserPage {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void show(Stage stage) {
        Label title = new Label("Welcome to OTP Flashcards");

        Label desc = new Label("Manage your flashcards and study your card sets.");

        Label userLabel = new Label("User dashboard");

        // Create card button
        Button createCardButton = new Button("Create Card");
        createCardButton.setMaxWidth(Double.MAX_VALUE);

        // Create card set button
        Button createCardSetButton = new Button("Create Card Set");
        createCardSetButton.setMaxWidth(Double.MAX_VALUE);

        // Study card sets button
        Button studyCardSetsButton = new Button("Study Card Sets");
        studyCardSetsButton.setMaxWidth(Double.MAX_VALUE);

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        // Create card nav
        createCardButton.setOnAction(e -> {
            new CreateCard().show(stage);
        });

        // Create card set nav
        createCardSetButton.setOnAction(e -> {
            new CreateCardSet().show(stage);
        });

        // Study card sets nav
        studyCardSetsButton.setOnAction(e -> {
            new StudyCards().show(stage);
        });

        // Logout action
        logoutButton.setOnAction(e -> {
            // Handle logout logic here
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout");
            alert.setHeaderText(null);
            alert.setContentText("You have been logged out.");
            alert.showAndWait();
            stage.close();
        });

        // Layout
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);
        layout.getChildren().addAll(title, desc, new Label(""), userLabel, new Label(""), createCardButton, createCardSetButton, studyCardSetsButton, new Label(""), logoutButton);

        VBox root = new VBox(layout);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("User Page");
        stage.show();
    }

    private void createCardSet(
        String cardSetName,
        Stage stage,
        Button createCardSetButton
    ) {
        // Create the request body
        String json = String.format(
            "{\"cardSetName\":\"%s\"}",
            escapeJson(cardSetName)
        );

        // Send the request to the server (this is a placeholder, implement actual server communication)
        // For demonstration, we'll just show an alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Card Set");
        alert.setHeaderText(null);
        alert.setContentText("Card set '" + cardSetName + "' created successfully!");
        alert.showAndWait();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://yourserver.com/api/createCardSet"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        Thread thread = new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    createCardSetButton.setDisable(false);
                    createCardSetButton.setText("Create Card Set");

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        showAlert(
                            Alert.AlertType.INFORMATION,
                            "Card Set Created",
                            null,
                            "Your card set has been created successfully!"
                        );
                        new UserPage().show(stage);
                    } else {
                        showAlert(
                            Alert.AlertType.ERROR,
                            "Error",
                            "Failed to create card set",
                            "An error occurred while creating the card set. Please try again."
                        );
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    createCardSetButton.setDisable(false);
                    createCardSetButton.setText("Create Card Set");

                    showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "Failed to create card set",
                        "An error occurred while creating the card set. Please try again."
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
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}