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
import otpproju.config.I18n;
import java.util.function.Consumer;

// Provides the flashcard study screen, allowing users to select a set,
// view questions and answers, and track their study progress.
public class StudyCards {

    private final User user;

    private final FlashcardService flashcardService;
    private final FlashcardSetService flashcardSetService;
    private final Consumer<Stage> goToDashboard;

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
        this(user, null);
    }

    public StudyCards(
            User user,
            FlashcardSet selectedSet
    ) {
        this(
                user,
                selectedSet,
                new FlashcardService(
                        new FlashcardRepository(),
                        new FlashcardSetRepository()
                ),
                new FlashcardSetService(new FlashcardSetRepository()),
                stage -> new UserPage(user).show(stage)
        );
    }

    // visible for testing
    StudyCards(
            User user,
            FlashcardSet selectedSet,
            FlashcardService flashcardService,
            FlashcardSetService flashcardSetService,
            Consumer<Stage> goToDashboard
    ) {
        this.user = user;
        this.initialSet = selectedSet;
        this.flashcardService = flashcardService;
        this.flashcardSetService = flashcardSetService;
        this.goToDashboard = goToDashboard;
    }

    // Builds and displays the study interface and loads an initially
    // selected flashcard set when one has been provided.
    public void show(Stage stage) {

        setTitle =
                UIComponents.pageTitle(
                        I18n.t("studyCards.title")
                );

        setTitle.textProperty().addListener(
                (observable, oldText, newText) ->
                        setTitle.setNodeOrientation(
                                I18n.getTextOrientation(newText)
                        )
        );

        setTitle.setNodeOrientation(
                I18n.getTextOrientation(setTitle.getText())
        );

        setTitle.getStyleClass().add(
                "page-title"
        );

        ComboBox<FlashcardSet> setComboBox =
                new ComboBox<>();

        setComboBox.setPromptText(
                I18n.t("studyCards.choose")
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
                    I18n.t("studyCards.loadErrorSet"),
                    ex.getMessage()
            );
        }


        Button loadButton =
                UIComponents.dashboardButton(
                        I18n.t("studyCards.loadButton")
                );

        loadButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loadButton.setPrefHeight(42);


        progressText =
                new Label(
                        I18n.t("studyCards.chooseSet")
                );

        progressText.getStyleClass().add(
                "progress-label"
        );


        cardText =
                new Label(
                        I18n.t("studyCards.cardLabel1") +
                                I18n.t("studyCards.cardLabel2")
                );

        cardText.textProperty().addListener(
                (observable, oldText, newText) ->
                        cardText.setNodeOrientation(
                                I18n.getTextOrientation(newText)
                        )
        );

        cardText.setNodeOrientation(
                I18n.getTextOrientation(cardText.getText())
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
                UIComponents.primaryButton(
                        I18n.t("studyCards.answerButton")
                );

        answerButton.setDisable(true);

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
                UIComponents.primaryButton(
                        I18n.t("studyCards.next")
                );

        nextButton.setDisable(true);

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
                new Button(I18n.t("app.backButton"));

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
                        I18n.t("studyCards.select.title"),
                        I18n.t("studyCards.select.message")
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
                goToDashboard.accept(stage)
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
                I18n.t("studyCards.title")
        );

        scene.getRoot().setNodeOrientation(I18n.getOrientation());
        stage.setScene(scene);
        stage.show();
    }

    // Retrieves the flashcards belonging to the selected set
    // and prepares the first card for studying.
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
                        I18n.t("studyCards.noCards1") +
                                I18n.t("studyCards.noCards2")
                );

                progressText.setText(
                        I18n.t("studyCards.0cards")
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
                    I18n.t("studyCards.loadError"),
                    ex.getMessage()
            );
        }
    }

    // Displays the current question and updates the study progress.
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
                I18n.format(
                        "studyCards.progress",
                        currentCardIndex + 1,
                        cards.size()
                )
        );

        answerButton.setText(
                I18n.t("studyCards.answerButton")

        );

        answerButton.setDisable(
                false
        );

        nextButton.setDisable(
                true
        );
    }

    // Reveals the answer to the current flashcard.
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

    // Advances to the next flashcard or displays the completion
    // message when the user has reached the end of the set.
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
                    I18n.format(
                            "studyCards.completed",
                            cards.size()
                    )
            );

            cardText.setText(
                    I18n.t("studyCards.congrats")

            );

            answerButton.setDisable(
                    true
            );

            nextButton.setDisable(
                    true
            );
        }
    }

    // Displays an alert using the current application's text direction.
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
        alert.getDialogPane().setNodeOrientation(
                I18n.getOrientation()
        );

        alert.showAndWait();
    }
}