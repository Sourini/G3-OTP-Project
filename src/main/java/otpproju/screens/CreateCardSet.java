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

public class CreateCardSet {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void show(Stage stage) {
        // Title
        Label title = new Label("Create Card Set");

        // Desc
        Label desc = new Label(
            "Create a new card set to organize your flashcards."
        );

        // Card set name
        Label cardSetNameLabel = new Label("Card Set Name:");
        TextField cardSetNameField = new TextField();
        cardSetNameField.setPromptText("Enter the card set name");

        // Create card set button
        Button createCardSetButton = new Button("Create Card Set");
        createCardSetButton.setMaxWidth(Double.MAX_VALUE);

        // Back button
        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);

        // Create card set action
        createCardSetButton.setOnAction(e -> {
            String cardSetName = cardSetNameField.getText();

            if (cardSetName.isEmpty()) {
                Alert alerts = new Alert(Alert.AlertType.ERROR);
                alerts.setTitle("Error");
                alerts.setHeaderText("Missing fields");
                alerts.setContentText("Please fill in all fields.");
                alerts.showAndWait();
            } else {
                createCardSet(cardSetName, stage, createCardSetButton);
            }
        });

        // Back action
        backButton.setOnAction(e -> {
            new UserPage().show(stage);
        });

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(300);
        layout.getChildren().addAll(title, desc, cardSetNameLabel, cardSetNameField, createCardSetButton, backButton);

        VBox root = new VBox(layout);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Create Card Set");
        stage.show();
    }

    private void createCardSet(
        String cardSetName,
        Stage stage,
        Button createCardSetButton
    ) {
        // Create the request body
        String json = String.format("{\"name\": \"%s\"}", cardSetName);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/cardsets"))
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
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Card Set Created");
                        alert.setHeaderText(null);
                        alert.setContentText("Your card set has been created successfully!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to create card set");
                        alert.setContentText("An error occurred while creating the card set. Please try again.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    createCardSetButton.setDisable(false);
                    createCardSetButton.setText("Create Card Set");

                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create card set");
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

    private void showAlert(
        Alert.AlertType alertType,
        String title,
        String message
    ) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}