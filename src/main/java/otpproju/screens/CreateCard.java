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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardService;
import otpproju.service.FlashcardSetService;

import otpproju.config.I18n;


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
                new Label(I18n.t("createCard.title"));

        title.getStyleClass().add(
                "page-title"
        );

        Label description =
                new Label(I18n.t("createCard.desc")
                );

        ComboBox<FlashcardSet> setComboBox =
                new ComboBox<>();

        setComboBox.setPromptText(
                I18n.t("createCard.chooseSet")
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
                    I18n.t("createCard.exceptionError"),
                    ex.getMessage()
            );
        }

        TextArea questionField =
                new TextArea();

        questionField.setPromptText(
                I18n.t("createCard.questionField")
        );

        questionField.setPrefRowCount(4);

        TextArea answerField =
                new TextArea();

        answerField.setPromptText(
                I18n.t("createCard.answerField")
        );

        answerField.setPrefRowCount(4);

        Button createButton =
                new Button(I18n.t("createCard.createButton"));

        createButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createButton.setPrefHeight(45);

        Button backButton =
                new Button(I18n.t("createCard.backButton"));

        backButton.setMaxWidth(
                Double.MAX_VALUE
        );

        backButton.setPrefHeight(40);

        createButton.setOnAction(e -> {

            FlashcardSet selectedSet =
                    setComboBox.getValue();

            String question =
                    questionField
                            .getText()
                            .trim();

            String answer =
                    answerField
                            .getText()
                            .trim();

            if (selectedSet == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("createCard.setSelectError.title"),
                        I18n.t("createCard.setSelectError.message")
                );

                return;
            }

            if (question.isEmpty()
                    || answer.isEmpty()) {

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("createCard.setEmptyField.title"),
                        I18n.t("createCard.setEmptyField.message")
                );

                return;
            }

            createButton.setDisable(true);
            createButton.setText(
                    I18n.t("createCard.creating")
            );

            try {

                flashcardService.createFlashcard(
                        user.getUserId(),
                        selectedSet.getSetId(),
                        question,
                        answer
                );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        I18n.t("createCard.created.title"),
                        I18n.t("createCard.created.message")
                );

                questionField.clear();
                answerField.clear();

                createButton.setDisable(false);
                createButton.setText(
                        I18n.t("createCard.createButton")
                );

            } catch (Exception ex) {

                createButton.setDisable(false);
                createButton.setText(
                        I18n.t("createCard.exception")
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("createCard.createButton.error"),
                        ex.getMessage()
                );
            }
        });

        backButton.setOnAction(e ->
                new UserPage(user).show(stage)
        );

        // CONTENT

        VBox content =
                new VBox(
                        15,
                        title,
                        description,
                        setComboBox,
                        questionField,
                        answerField,
                        createButton,
                        backButton
                );

        content.setAlignment(
                Pos.TOP_CENTER
        );

        content.setPadding(
                new Insets(30)
        );

        content.setMaxWidth(550);
        content.setPrefWidth(550);

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

        stage.setTitle(
                I18n.t("createCard.title")

        );

        scene.getRoot().setNodeOrientation(I18n.getOrientation());
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
        alert.getDialogPane().setNodeOrientation(
                I18n.getOrientation()
        );

        alert.showAndWait();
    }
}