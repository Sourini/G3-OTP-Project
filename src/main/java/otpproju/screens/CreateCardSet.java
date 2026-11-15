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
import java.util.function.Consumer;
import otpproju.config.I18n;

// Displays the flashcard set creation form and
// handles saving a new set for the current user.
public class CreateCardSet {

    private final User user;
    private final FlashcardSetService flashcardSetService;
    private final Consumer<Stage> goToDashboard;

    // Initializes the screen with the current user and flashcard set service.
    public CreateCardSet(User user) {
        this(
                user,
                new FlashcardSetService(new FlashcardSetRepository()),
                stage -> new UserPage(user).show(stage)
        );
    }

    // visible for testing
    CreateCardSet(User user, FlashcardSetService flashcardSetService, Consumer<Stage> goToDashboard) {
        this.user = user;
        this.flashcardSetService = flashcardSetService;
        this.goToDashboard = goToDashboard;
    }

    // Builds and displays the flashcard set form, including input validation,
    // creation actions, and navigation back to the dashboard.
    public void show(Stage stage) {

        Label title =
                UIComponents.pageTitle(
                        I18n.t("createCardSet.title")
                );

        title.getStyleClass().add(
                "page-title"
        );

        Label description =
                new Label(
                        I18n.t("createCardSet.desc")
                );

        TextField titleField =
                new TextField();

        titleField.setPromptText(
                I18n.t("createCardSet.setName")
        );

        titleField.setPrefHeight(45);

        TextArea descriptionField =
                new TextArea();

        descriptionField.setPromptText(
                I18n.t("createCardSet.setDesc")
        );

        descriptionField.setPrefRowCount(4);

        Button createButton =
                UIComponents.primaryButton(
                        I18n.t("createCardSet.createButton")
                );

        createButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createButton.setPrefHeight(45);

        Button backButton =
                new Button(I18n.t("createCardSet.backButton"));

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
                        I18n.t("createCardSet.emptyTitle.title"),
                        I18n.t("createCardSet.emptyTitle.message")
                );

                return;
            }

            createButton.setDisable(true);
            createButton.setText(
                    I18n.t("createCardSet.creating")
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
                        I18n.t("createCardSet.created.title"),
                        I18n.t("createCardSet.created.message")
                );

                goToDashboard.accept(stage);

            } catch (Exception ex) {

                createButton.setDisable(false);
                createButton.setText(
                        I18n.t("createCardSet.createButton")
                );

                showAlert(
                        Alert.AlertType.ERROR,
                        I18n.t("createCardSet.createError"),
                        ex.getMessage()
                );
            }
        });

        // BACK

        backButton.setOnAction(e ->
                goToDashboard.accept(stage)
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
                I18n.t("createCardSet.title")

        );

        scene.getRoot().setNodeOrientation(I18n.getOrientation());
        stage.setScene(scene);
        stage.show();
    }

    // Displays a localized alert using the current text direction.
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