package otpproju.screens;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardService;
import otpproju.service.FlashcardSetService;

public class CreateCard {

    private final User user;

    private final FlashcardService flashcardService;
    private final FlashcardSetService flashcardSetService;

    public CreateCard(User user) {

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

    public void show(Stage stage) {

        Label title =
                new Label("Create Flashcard");

        title.setStyle(
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Add a question and answer to a set."
                );

        ComboBox<FlashcardSet> setComboBox =
                new ComboBox<>();

        setComboBox.setPromptText(
                "Choose a flashcard set"
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

        TextArea questionField =
                new TextArea();

        questionField.setPromptText(
                "Question"
        );

        questionField.setPrefRowCount(4);

        TextArea answerField =
                new TextArea();

        answerField.setPromptText(
                "Answer"
        );

        answerField.setPrefRowCount(4);

        Button createButton =
                new Button("Create Card");

        createButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createButton.setPrefHeight(45);

        Button backButton =
                new Button("Back");

        backButton.setMaxWidth(
                Double.MAX_VALUE
        );

        backButton.setPrefHeight(40);

        createButton.setOnAction(e -> {

            FlashcardSet selectedSet =
                    setComboBox.getValue();

            String question =
                    questionField.getText().trim();

            String answer =
                    answerField.getText().trim();

            if (selectedSet == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "No set selected",
                        "Please choose a flashcard set."
                );

                return;
            }

            if (question.isEmpty()
                    || answer.isEmpty()) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Missing fields",
                        "Please enter both a question and answer."
                );

                return;
            }

            createButton.setDisable(true);
            createButton.setText("Creating...");

            try {

                flashcardService.createFlashcard(
                        user.getUserId(),
                        selectedSet.getSetId(),
                        question,
                        answer
                );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Your flashcard was created."
                );

                questionField.clear();
                answerField.clear();

                createButton.setDisable(false);
                createButton.setText("Create Card");

            } catch (Exception ex) {

                createButton.setDisable(false);
                createButton.setText("Create Card");

                showAlert(
                        Alert.AlertType.ERROR,
                        "Could not create card",
                        ex.getMessage()
                );
            }
        });

        backButton.setOnAction(e ->
                new UserPage(user).show(stage)
        );

        VBox box = new VBox(
                15,
                title,
                description,
                setComboBox,
                questionField,
                answerField,
                createButton,
                backButton
        );

        box.setPadding(
                new Insets(30)
        );

        box.setAlignment(
                Pos.TOP_CENTER
        );

        box.setMaxWidth(360);

        VBox root =
                new VBox(box);

        root.setAlignment(
                Pos.TOP_CENTER
        );

        root.setStyle(
                "-fx-background-color: #f5f7fb;"
        );

        Scene scene =
                new Scene(
                        root,
                        390,
                        750
                );

        stage.setTitle("Create Card");
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