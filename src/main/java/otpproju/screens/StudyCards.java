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
import java.util.ArrayList; 
import java.util.List;

public class StudyCards {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final List<String> cards = new ArrayList<>();
    private int currentCardIndex = 0;
    private Label cardText;
    private Button answerButton;
    private Button nextButton;

    public void show(Stage stage) {
        // Title
        Label title = new Label("Study Cards");

        // Card set ID
        Label cardSetLabel = new Label("Card Set ID:");
        TextField cardSetField = new TextField();
        cardSetField.setPromptText("Enter the card set ID");

        // Load cards button
        Button loadCardsButton = new Button("Load Cards");
        loadCardsButton.setMaxWidth(Double.MAX_VALUE);

        // Card display
        cardText = new Label(
            "Enter a card set ID and load the cards to start studying."
        );
        cardText.setWrapText(true);
        cardText.setAlignment(Pos.CENTER);

        // Answer button
        answerButton = new Button("Show Answer");
        answerButton.setDisable(true);
        answerButton.setMaxWidth(Double.MAX_VALUE);

        // Next button
        nextButton = new Button("Next Card");
        nextButton.setDisable(true);
        nextButton.setMaxWidth(Double.MAX_VALUE);

        // Back button
        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);

        // Load cards action
        loadCardsButton.setOnAction(e -> {
            String cardSetId = cardSetField.getText().trim();
            if (cardSetId.isEmpty()) {
                showAlert(
                    Alert.AlertType.ERROR, 
                    "Error", 
                    "Missing fields", 
                    "Please enter a card set ID."
                );
                return;
            }
            loadCardsButton.setDisable(true);
            loadCardsButton.setText("Loading...");
            loadCards(cardSetId, stage, loadCardsButton);
        });

        // Show answer action
        answerButton.setOnAction(e -> {
            if (!cards.isEmpty()) {
                Card currentCard = cards.get(currentCardIndex);
                cardText.setText(currentCard.getBack());
                answerButton.setDisable(true);
                nextButton.setDisable(false);
            }
        });

        // Next card action
        nextButton.setOnAction(e -> {
            if (cards.isEmpty()) {
                return;
            }

            currentCardIndex++;
            if (currentCardIndex < cards.size()) {
                showCurrentCard();
            } else {
                currentCardIndex = cards.size() - 1;
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "End of Cards",
                    null,
                    "You have reached the end of the card set."
                );
                answerButton.setDisable(true);
                nextButton.setDisable(true);
            }
        });

        // Back button action
        backButton.setOnAction(e -> {
            new UserPage().show(stage);
        });

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(400);

        layout.getChildren().addAll(
            title,
            new Label(""),
            cardSetLabel,
            cardSetField,
            loadCardsButton,
            new Label(""),
            cardText,
            answerButton,
            nextButton,
            new Label(""),
            backButton
        );

        VBox root = new VBox(layout);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Study Cards");
        stage.setScene(scene);
        stage.show();
    }

    private void loadCards(
        String cardSetId,
        Stage stage,
        Button loadCardsButton
    ) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/cardsets/" + cardSetId + "/cards"))
            .GET()
            .build();

        Thread thread = new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    loadCardsButton.setDisable(false);
                    loadCardsButton.setText("Load Cards");

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        System.out.println("Cards loaded successfully.");
                        // Parse the response and populate the cards list
                        showAlert(
                            Alert.AlertType.INFORMATION, 
                            "Success", 
                            null, 
                            "Cards loaded successfully."
                        );
                    } else {
                        showAlert(
                            Alert.AlertType.ERROR, 
                            "Error", 
                            "Failed to load cards", 
                            "Please check the card set ID and try again."
                        );
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadCardsButton.setDisable(false);
                    loadCardsButton.setText("Load Cards");
                    showAlert(
                        Alert.AlertType.ERROR, 
                        "Error", 
                        "Failed to load cards", 
                        "An error occurred while loading the cards. Please try again."
                    );
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void showCurrentCard() {
        if (!cards.isEmpty()) {
            Card currentCard = cards.get(currentCardIndex);
            cardText.setText(currentCard.getFront());
            answerButton.setDisable(false);
            nextButton.setDisable(true);
        }
    }

    private void showAlert(
        Alert.AlertType alertType,
        String title,
        String header,
        String message
    ) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Card class to represent a flashcard
    private static class Card {
        private final String front;
        private final String back;

        public Card(String front, String back) {
            this.front = front;
            this.back = back;
        }

        public String getFront() {
            return front;
        }

        public String getBack() {
            return back;
        }
    }
}