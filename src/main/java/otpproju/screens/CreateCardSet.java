package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.User;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardSetService;

public class CreateCardSet {

    private final User user;
    private final FlashcardSetService flashcardSetService;

    public CreateCardSet(User user) {

        this.user = user;

        FlashcardSetRepository repository =
                new FlashcardSetRepository();

        flashcardSetService =
                new FlashcardSetService(repository);
    }

    public void show(Stage stage) {

        Label title =
                new Label(
                        "Create Flashcard Set"
                );

        title.getStyleClass().add(
                "page-title"
        );

        Label description =
                new Label(
                        "Create a set to organize " +
                                "your flashcards."
                );

        TextField titleField =
                new TextField();

        titleField.setPromptText(
                "Set name"
        );

        titleField.setPrefHeight(45);

        TextArea descriptionField =
                new TextArea();

        descriptionField.setPromptText(
                "Description (optional)"
        );

        descriptionField.setPrefRowCount(4);

        Button createButton =
                new Button("Create Set");

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

        // CREATE SET

        createButton.setOnAction(e -> {

            String titleText =
                    titleField
                            .getText()
                            .trim();

            String descriptionText =
                    descriptionField
                            .getText()
                            .trim();

            if (titleText.isEmpty()) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Missing title",
                        "Please enter a name for " +
                                "your card set."
                );

                return;
            }

            createButton.setDisable(true);
            createButton.setText(
                    "Creating..."
            );

            try {

                flashcardSetService
                        .createFlashcardSet(
                                user.getUserId(),
                                titleText,
                                descriptionText
                        );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Your flashcard set was created."
                );

                new UserPage(user)
                        .show(stage);

            } catch (Exception ex) {

                createButton.setDisable(false);
                createButton.setText(
                        "Create Set"
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        "Could not create set",
                        ex.getMessage()
                );
            }
        });

        // BACK

        backButton.setOnAction(e ->
                new UserPage(user).show(stage)
        );

        // CONTENT

        VBox content =
                new VBox(
                        15,
                        title,
                        description,
                        titleField,
                        descriptionField,
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
                "Create Set"
        );

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