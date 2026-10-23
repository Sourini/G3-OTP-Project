package otpproju.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardSetService;

import java.util.List;
import otpproju.config.I18n;

public class UserPage {

    private final User user;
    private final FlashcardSetService flashcardSetService;

    public UserPage(User user) {
        this.user = user;

        FlashcardSetRepository repository =
                new FlashcardSetRepository();

        this.flashcardSetService =
                new FlashcardSetService(repository);
    }

    public void show(Stage stage) {

        // TOP BAR

        Label appName =
                new Label(I18n.t("app.title"));

        appName.getStyleClass().add("app-title");

        Button logoutButton =
                new Button(I18n.t("userPage.logout"));

        logoutButton.getStyleClass().add("logout-button");

        logoutButton.setOnAction(e ->
                new Login().show(stage)
        );

        BorderPane topBar = new BorderPane();

        topBar.setLeft(appName);

        // LANGUAGE DROPDOWN MENU
        ComboBox<String> languageSelector = new ComboBox<>();

        languageSelector.getItems().addAll(
                "English",
                "한국어",
                "Русский"
        );

        languageSelector.setValue("English");
        languageSelector.setPrefWidth(140);
        languageSelector.getStyleClass().add("language-selector");

        // LANGUAGE SELECTOR AND LOGOUT
        HBox topRight = new HBox(
                12,
                languageSelector,
                logoutButton
        );

        topRight.setAlignment(Pos.CENTER_RIGHT);
        topBar.setRight(topRight);

        topBar.setPadding(
                new Insets(15, 20, 15, 20)
        );

        topBar.getStyleClass().add("top-bar");

        // HEADER

        Label greeting =
                new Label(
                        I18n.t("userPage.greet") + (", ") + user.getUsername()
                );

        greeting.getStyleClass().add(
                "page-title"
        );

        Label subtitle =
                new Label(
                        I18n.t("userPage.subs")
                );

        subtitle.getStyleClass().add(
                "subtitle"
        );

        VBox header =
                new VBox(
                        5,
                        greeting,
                        subtitle
                );

        header.setAlignment(
                Pos.CENTER
        );

        // CREATE SET CARD

        VBox createSetCard =
                new VBox(8);

        Label createSetTitle =
                new Label(
                        I18n.t("userPage.createSet")
                );

        createSetTitle.getStyleClass().add(
                "card-title"
        );

        Label createSetDescription =
                new Label(
                        I18n.t("userPage.createSetDesc1") +
                                I18n.t("userPage.createSetDesc2")
                );

        createSetDescription.setWrapText(
                true
        );

        Button createSetButton =
                new Button(I18n.t("userPage.createButton"));

        createSetButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createSetButton.setPrefHeight(
                42
        );

        createSetButton.setOnAction(e ->
                new CreateCardSet(user)
                        .show(stage)
        );

        createSetCard.getChildren().addAll(
                createSetTitle,
                createSetDescription,
                createSetButton
        );

        createSetCard.getStyleClass().add(
                "dashboard-card"
        );

        createSetCard.setMaxWidth(500);
        createSetCard.setPrefWidth(500);

        // CREATE CARD

        VBox createCardCard =
                new VBox(8);

        Label createCardTitle =
                new Label(
                        I18n.t("userPage.createButton")
                );

        createCardTitle.getStyleClass().add(
                "card-title"
        );

        Label createCardDescription =
                new Label(
                        I18n.t("userPage.createDesc1") +
                                I18n.t("userPage.createDesc2")
                );

        createCardDescription.setWrapText(
                true
        );

        Button createCardButton =
                new Button(I18n.t("userPage.createButton"));

        createCardButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createCardButton.setPrefHeight(
                42
        );

        createCardButton.setOnAction(e ->
                new CreateCard(user)
                        .show(stage)
        );

        createCardCard.getChildren().addAll(
                createCardTitle,
                createCardDescription,
                createCardButton
        );

        createCardCard.getStyleClass().add(
                "dashboard-card"
        );

        createCardCard.setMaxWidth(500);
        createCardCard.setPrefWidth(500);

        // STUDY CARD

        VBox studyCard =
                new VBox(8);

        Label studyTitle =
                new Label(
                        I18n.t("userPage.studyTitle")
                );

        studyTitle.getStyleClass().add(
                "card-title"
        );

        Label studyDescription =
                new Label(
                        I18n.t("userPage.studyDesc")
                );

        studyDescription.setWrapText(
                true
        );

        Button studyButton =
                new Button(I18n.t("userPage.studyButton"));

        studyButton.setMaxWidth(
                Double.MAX_VALUE
        );

        studyButton.setPrefHeight(
                42
        );

        studyButton.setOnAction(e ->
                new StudyCards(user)
                        .show(stage)
        );

        studyCard.getChildren().addAll(
                studyTitle,
                studyDescription,
                studyButton
        );

        studyCard.getStyleClass().add(
                "dashboard-card"
        );

        studyCard.setMaxWidth(500);
        studyCard.setPrefWidth(500);

        // MY FLASHCARD SETS

        Label recentTitle =
                new Label(
                        I18n.t("userPage.mySets")
                );

        recentTitle.getStyleClass().add(
                "section-title"
        );

        VBox setsBox =
                new VBox(8);

        setsBox.setMaxWidth(500);
        setsBox.setPrefWidth(500);

        loadSets(setsBox);

        // DASHBOARD CONTENT

        VBox content =
                new VBox(
                        18,
                        header,
                        createSetCard,
                        createCardCard,
                        studyCard,
                        recentTitle,
                        setsBox
                );

        content.setAlignment(
                Pos.TOP_CENTER
        );

        content.setPadding(
                new Insets(25)
        );

        content.setMaxWidth(550);
        content.setPrefWidth(550);

        // CENTER CONTENT

        StackPane centeredContent =
                new StackPane(content);

        centeredContent.setAlignment(
                Pos.TOP_CENTER
        );

        // SCROLL PANE

        ScrollPane scrollPane =
                new ScrollPane(
                        centeredContent
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setFitToHeight(
                false
        );

        scrollPane.getStyleClass().add(
                "dashboard-scroll"
        );

        // MAIN ROOT

        BorderPane root =
                new BorderPane();

        root.setTop(topBar);
        root.setCenter(scrollPane);

        root.getStyleClass().add(
                "dashboard-root"
        );

        // SCENE

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
                I18n.t("app.title")
        );

        stage.setScene(scene);
        stage.show();
    }

    // LOAD FLASHCARD SETS

    private void loadSets(
            VBox setsBox
    ) {

        try {

            List<FlashcardSet> sets =
                    flashcardSetService
                            .getFlashcardSetsForUser(
                                    user.getUserId()
                            );

            if (sets.isEmpty()) {

                Label emptyLabel =
                        new Label(
                                I18n.t("userPage.noSetError1") +
                                        I18n.t("userPage.noSetError2")
                        );

                setsBox.getChildren().add(
                        emptyLabel
                );

                return;
            }

            for (FlashcardSet set : sets) {

                Button setButton =
                        new Button(
                                set.getTitle()
                        );

                setButton.setMaxWidth(
                        Double.MAX_VALUE
                );

                setButton.setPrefHeight(
                        45
                );

                setButton.setOnAction(e -> {

                    Stage currentStage =
                            (Stage) setButton
                                    .getScene()
                                    .getWindow();

                    new StudyCards(
                            user,
                            set
                    ).show(currentStage);
                });

                setsBox.getChildren().add(
                        setButton
                );
            }

        } catch (Exception ex) {

            Label error =
                    new Label(
                            I18n.t("userPage.loadError1") +
                                    I18n.t("userPage.loadError2")
                    );

            setsBox.getChildren().add(
                    error
            );
        }
    }
}