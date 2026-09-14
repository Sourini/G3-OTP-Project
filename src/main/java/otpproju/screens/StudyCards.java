package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.Flashcard;

import java.util.ArrayList;
import java.util.List;

public class StudyCards {

    private final List<Flashcard> cards = new ArrayList<>();

    private int currentCardIndex;
    private Label cardText;
    private Label progressText;
    private Button answerButton;
    private Button nextButton;

    public void show(Stage stage) {
        Label title = new Label("Study Cards");

        Label cardSetLabel = new Label("Card Set ID:");

        TextField cardSetField = new TextField();
        cardSetField.setPromptText("Enter the card set ID");
        cardSetField.setMaxWidth(300);

        Button loadCardsButton = new Button("Load Cards");
        loadCardsButton.setMaxWidth(300);

        cardText = new Label(
                "Enter a card set ID and load the cards " +
                        "to start studying."
        );
        cardText.setWrapText(true);
        cardText.setAlignment(Pos.CENTER);
        cardText.setMinHeight(100);
        cardText.setMaxWidth(400);

        progressText = new Label();

        answerButton = new Button("Show Answer");
        answerButton.setDisable(true);
        answerButton.setMaxWidth(300);

        nextButton = new Button("Next Card");
        nextButton.setDisable(true);
        nextButton.setMaxWidth(300);

        Button backButton = new Button("Back");
        backButton.setMaxWidth(300);

        loadCardsButton.setOnAction(event ->
                loadSampleCards(cardSetField.getText())
        );

        answerButton.setOnAction(event -> showAnswer());

        nextButton.setOnAction(event -> showNextCard());

        backButton.setOnAction(event ->
                new UserPage().show(stage)
        );

        VBox layout = new VBox(
                10,
                title,
                cardSetLabel,
                cardSetField,
                loadCardsButton,
                progressText,
                cardText,
                answerButton,
                nextButton,
                backButton
        );

        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 600, 500);

        stage.setTitle("Study Cards");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Temporary frontend-only implementation.
     *
     * Later this method can call FlashcardService or FlashcardDao
     * to retrieve cards from MariaDB.
     */
    private void loadSampleCards(String cardSetIdText) {
        if (cardSetIdText == null || cardSetIdText.isBlank()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid card set",
                    "Missing card set ID",
                    "Please enter a card set ID."
            );
            return;
        }

        int setId;

        try {
            setId = Integer.parseInt(cardSetIdText.trim());
        } catch (NumberFormatException exception) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid card set",
                    "Invalid card set ID",
                    "The card set ID must be a number."
            );
            return;
        }

        if (setId <= 0) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid card set",
                    "Invalid card set ID",
                    "The card set ID must be positive."
            );
            return;
        }

        cards.clear();

        cards.add(new Flashcard(
                setId,
                "What does JVM stand for?",
                "Java Virtual Machine"
        ));

        cards.add(new Flashcard(
                setId,
                "Which keyword is used to create a Java class?",
                "class"
        ));

        cards.add(new Flashcard(
                setId,
                "What does SQL stand for?",
                "Structured Query Language"
        ));

        currentCardIndex = 0;
        showCurrentCard();

        showAlert(
                Alert.AlertType.INFORMATION,
                "Cards loaded",
                null,
                cards.size() + " sample cards were loaded."
        );
    }

    private void showAnswer() {
        if (cards.isEmpty()) {
            return;
        }

        Flashcard currentCard = cards.get(currentCardIndex);

        cardText.setText(currentCard.getAnswer());
        answerButton.setDisable(true);
        nextButton.setDisable(false);
    }

    private void showNextCard() {
        if (cards.isEmpty()) {
            return;
        }

        if (currentCardIndex < cards.size() - 1) {
            currentCardIndex++;
            showCurrentCard();
            return;
        }

        progressText.setText(
                "Completed " + cards.size() +
                        " of " + cards.size() + " cards"
        );

        answerButton.setDisable(true);
        nextButton.setDisable(true);

        showAlert(
                Alert.AlertType.INFORMATION,
                "End of cards",
                null,
                "You have reached the end of the card set."
        );
    }

    private void showCurrentCard() {
        if (cards.isEmpty()) {
            cardText.setText("No cards are available.");
            progressText.setText("");
            answerButton.setDisable(true);
            nextButton.setDisable(true);
            return;
        }

        Flashcard currentCard = cards.get(currentCardIndex);

        cardText.setText(currentCard.getQuestion());

        progressText.setText(
                "Card " + (currentCardIndex + 1) +
                        " of " + cards.size()
        );

        answerButton.setDisable(false);
        nextButton.setDisable(true);
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
}