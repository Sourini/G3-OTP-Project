package otpproju.screens;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.Flashcard;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardService;
import otpproju.service.FlashcardSetService;

public class StudyCards {

    private final User user;

    private final FlashcardService flashcardService;
    private final FlashcardSetService flashcardSetService;

    private final List<Flashcard> cards =
            new ArrayList<>();

    private int currentCardIndex = 0;

    private Label cardText;
    private Label progressText;
    private Label setTitle;

    private Button answerButton;
    private Button nextButton;

    private FlashcardSet initialSet;

    public StudyCards(User user) {

        this.user = user;

        FlashcardRepository cardRepository =
                new FlashcardRepository();

        FlashcardSetRepository setRepository =
                new FlashcardSetRepository();

        flashcardService =
                new FlashcardService(
                        cardRepository,
                        setRepository
                );

        flashcardSetService =
                new FlashcardSetService(
                        setRepository
                );
    }

    public StudyCards(
            User user,
            FlashcardSet selectedSet
    ) {
        this(user);
        this.initialSet = selectedSet;
    }

    public void show(Stage stage) {

        setTitle =
                new Label("Study Cards");

        setTitle.getStyleClass().add(
                "page-title"
        );

        ComboBox<FlashcardSet> setComboBox =
                new ComboBox<>();

        setComboBox.setPromptText(
                "Choose a set to study"
        );

        setComboBox.setMaxWidth(
                Double.MAX_VALUE
        );

        setComboBox.setPrefHeight(45);

        try {

            List<FlashcardSet> sets =
                    flashcardSetService
                            .getFlashcardSetsForUser(
                                    user.getUserId()
                            );

            setComboBox
                    .getItems()
                    .addAll(sets);

        } catch (Exception ex) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Could not load sets",
                    ex.getMessage()
            );
        }


        Button loadButton =
                new Button(
                        "Start Studying"
                );

        loadButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loadButton.setPrefHeight(42);


        progressText =
                new Label(
                        "Choose a set to begin."
                );

        progressText.getStyleClass().add(
                "progress-label"
        );


        cardText =
                new Label(
                        "Your flashcard will " +
                                "appear here."
                );

        cardText.setWrapText(
                true
        );

        cardText.setAlignment(
                Pos.CENTER
        );

        cardText.setMaxWidth(
                450
        );

        cardText.setMinHeight(
                220
        );

        cardText.getStyleClass().add(
                "flashcard"
        );


        answerButton =
                new Button(
                        "Show Answer"
                );

        answerButton.setMaxWidth(
                Double.MAX_VALUE
        );

        answerButton.setPrefHeight(
                45
        );

        answerButton.setDisable(
                true
        );


        nextButton =
                new Button(
                        "Next Card"
                );

        nextButton.setMaxWidth(
                Double.MAX_VALUE
        );

        nextButton.setPrefHeight(
                45
        );

        nextButton.setDisable(
                true
        );


        Button backButton =
                new Button("Back");

        backButton.setMaxWidth(
                Double.MAX_VALUE
        );

        backButton.setPrefHeight(
                40
        );

        // BUTTON ACTIONS

        loadButton.setOnAction(e -> {

            FlashcardSet selected =
                    setComboBox.getValue();

            if (selected == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "No set selected",
                        "Please choose a flashcard set."
                );

                return;
            }

            loadCards(selected);
        });


        answerButton.setOnAction(e ->
                showAnswer()
        );


        nextButton.setOnAction(e ->
                showNextCard()
        );


        backButton.setOnAction(e ->
                new UserPage(user)
                        .show(stage)
        );

        // CONTENT

        VBox content =
                new VBox(
                        15,
                        setTitle,
                        setComboBox,
                        loadButton,
                        progressText,
                        cardText,
                        answerButton,
                        nextButton,
                        backButton
                );

        content.setAlignment(
                Pos.TOP_CENTER
        );

        content.setPadding(
                new Insets(30)
        );

        content.setMaxWidth(600);
        content.setPrefWidth(600);

        // CENTER CONTENT

        StackPane centered =
                new StackPane(content);

        centered.setAlignment(
                Pos.TOP_CENTER
        );

        // ROOT

        BorderPane root =
                new BorderPane();

        root.setCenter(centered);

        root.getStyleClass().add(
                "dashboard-root"
        );


        Scene scene =
                new Scene(
                        root,
                        1000,
                        750
                );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/style.css")
                        .toExternalForm()
        );


        if (initialSet != null) {
            loadCards(initialSet);
        }


        stage.setTitle(
                "Study Cards"
        );

        stage.setScene(scene);
        stage.show();
    }

    // LOAD CARDS

    private void loadCards(
            FlashcardSet selectedSet
    ) {

        try {

            cards.clear();

            cards.addAll(
                    flashcardService
                            .getFlashcardsForSet(
                                    selectedSet.getSetId()
                            )
            );

            currentCardIndex = 0;

            setTitle.setText(
                    selectedSet.getTitle()
            );

            if (cards.isEmpty()) {

                cardText.setText(
                        "This set does not have " +
                                "any cards yet."
                );

                progressText.setText(
                        "0 cards"
                );

                answerButton.setDisable(
                        true
                );

                nextButton.setDisable(
                        true
                );

                return;
            }

            showCurrentCard();

        } catch (Exception ex) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Could not load cards",
                    ex.getMessage()
            );
        }
    }

    // CURRENT CARD

    private void showCurrentCard() {

        if (cards.isEmpty()) {
            return;
        }

        Flashcard currentCard =
                cards.get(currentCardIndex);

        cardText.setText(
                currentCard.getQuestion()
        );

        progressText.setText(
                "Card " +
                        (currentCardIndex + 1) +
                        " of " +
                        cards.size()
        );

        answerButton.setText(
                "Show Answer"
        );

        answerButton.setDisable(
                false
        );

        nextButton.setDisable(
                true
        );
    }

    // SHOW ANSWER

    private void showAnswer() {

        if (cards.isEmpty()) {
            return;
        }

        Flashcard currentCard =
                cards.get(currentCardIndex);

        cardText.setText(
                currentCard.getAnswer()
        );

        answerButton.setDisable(
                true
        );

        nextButton.setDisable(
                false
        );
    }

    // NEXT CARD

    private void showNextCard() {

        if (cards.isEmpty()) {
            return;
        }

        if (currentCardIndex <
                cards.size() - 1) {

            currentCardIndex++;

            showCurrentCard();

        } else {

            progressText.setText(
                    "Finished " +
                            cards.size() +
                            " cards!"
            );

            cardText.setText(
                    "Great job!"
            );

            answerButton.setDisable(
                    true
            );

            nextButton.setDisable(
                    true
            );
        }
    }

    // ALERT

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