package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import otpproju.model.User;
import otpproju.model.SetText;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        List<String> languages = I18n.getSupportedLocales().stream().map(Locale::getLanguage).toList();
        Map<String, SetFields> fields = new LinkedHashMap<>();
        Map<String, CheckBox> choices = new LinkedHashMap<>();
        VBox languageChoices = new VBox(4);
        VBox localizedFields = new VBox(10);
        ComboBox<String> defaultLanguage = ContentLanguages.selector(List.of());
        Label defaultLabel = new Label(I18n.t("content.defaultLanguage"));
        defaultLabel.setLabelFor(defaultLanguage);
        Runnable updateLanguages = () -> {
            String previous = defaultLanguage.getValue();
            List<String> selected = choices.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected()).map(Map.Entry::getKey).toList();
            defaultLanguage.getItems().setAll(selected);
            defaultLanguage.setValue(previous != null && selected.contains(previous) ? previous
                    : selected.stream().findFirst().orElse(null));
            defaultLanguage.setDisable(selected.size() < 2);
            localizedFields.getChildren().setAll(selected.stream()
                    .map(code -> fields.get(code).panel()).toList());
        };

        for (String code : languages) {
            TextField name = new TextField();
            name.setPromptText(I18n.t("createCardSet.setName"));
            TextArea details = new TextArea();
            details.setPromptText(I18n.t("createCardSet.setDesc"));
            details.setPrefRowCount(2);
            details.setWrapText(true);
            ContentLanguages.followTextDirection(name);
            ContentLanguages.followTextDirection(details);
            fields.put(code, new SetFields(name, details,
                    new VBox(5, new Label(ContentLanguages.displayName(code)), name, details)));
            CheckBox choice = new CheckBox(ContentLanguages.displayName(code));
            choice.setSelected(code.equals(I18n.getLocale().getLanguage()));
            choices.put(code, choice);
            choice.selectedProperty().addListener((observable, oldValue, newValue) -> updateLanguages.run());
            languageChoices.getChildren().add(choice);
        }
        updateLanguages.run();

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

            Map<String, SetText> texts = new LinkedHashMap<>();
            for (String code : defaultLanguage.getItems()) {
                SetFields entry = fields.get(code);
                texts.put(code, new SetText(entry.title().getText(), entry.description().getText()));
            }
            if (texts.isEmpty() || texts.values().stream()
                    .anyMatch(text -> text.title().isBlank() || text.description().isBlank())) {
                showAlert(Alert.AlertType.ERROR, I18n.t("createCardSet.emptyTitle.title"),
                        I18n.t("content.setFieldsRequired"));
                return;
            }

            createButton.setDisable(true);
            createButton.setText(
                    I18n.t("createCardSet.creating")
            );

            try {

                flashcardSetService.createMultilingualSet(user.getUserId(), defaultLanguage.getValue(), texts);

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
                        new Label(I18n.t("content.supportedLanguages")),
                        languageChoices,
                        defaultLabel,
                        defaultLanguage,
                        localizedFields,
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

        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("dashboard-scroll");
        root.setCenter(scroll);

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

    private record SetFields(TextField title, TextArea description, VBox panel) {}

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