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

public class CreateCard {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void show(Stage stage) {
        // Title
        Label title = new Label("Create Card");

        // Desc
        Label desc = new Label(
            "Create a new flashcard to add to your card sets."
        );

        // Card set ID
        Label cardSetIdLabel = new Label("Card Set ID:");
        TextField cardSetIdField = new TextField();
        cardSetIdField.setPromptText("Enter the card set ID");

        // Question
        Label questionLabel = new Label("Question:");
        TextField questionField = new TextField();
        questionField.setPromptText("Enter the question");

        // Answer
        Label answerLabel = new Label("Answer:");
        TextField answerField = new TextField();
        answerField.setPromptText("Enter the answer");

        // Create card button
        Button createCardButton = new Button("Create Card");
        createCardButton.setMaxWidth(Double.MAX_VALUE);

        // Back button
        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);

        // Create card action
        createCardButton.setOnAction(e -> {
            String cardSetId = cardSetIdField.getText();
            String question = questionField.getText();
            String answer = answerField.getText();

            if (cardSetId.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                Alert alerts = new Alert(Alert.AlertType.ERROR);
                alerts.setTitle("Error");
                alerts.setHeaderText("Missing fields");
                alerts.setContentText("Please fill in all fields.");
                alerts.showAndWait();
                return;
            }

            createCardButton.setDisable(true);
            createCardButton.setText("Creating...");
            createCard(cardSetId, question, answer, stage, createCardButton);
        });

        // Back button action
        backButton.setOnAction(e -> {
            stage.close();
        });

        // Layout
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(30));
        container.setMaxWidth(500);

        container.getChildren().addAll(
                title,
                desc,
                new Label(""),
                cardSetIdLabel,
                cardSetIdField,
                questionLabel,
                questionField,
                answerLabel,
                answerField,
                createCardButton,
                new Label(""),
                backButton
        );

        VBox root = new VBox(container);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Create Card");
        stage.setScene(scene);
        stage.show();
    }

    private void createCard(
            String cardSetId,
            String question,
            String answer,
            Stage stage,
            Button createCardButton
    ) {
        String json = String.format(
                "{\"cardSetId\":\"%s\", \"question\":\"%s\", \"answer\":\"%s\"}",
                escapeJson(cardSetId), escapeJson(question), escapeJson(answer)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/cards"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        Thread thread = new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    createCardButton.setDisable(false);
                    createCardButton.setText("Create Card");

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Card Created");
                        alert.setHeaderText(null);
                        alert.setContentText("Your flashcard has been created successfully!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to create card");
                        alert.setContentText("Please try again.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    createCardButton.setDisable(false);
                    createCardButton.setText("Create Card");
                    
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Error",
                            "An error occurred while creating the card. Please try again."
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
        return value.replace("\"", "\\\"");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}