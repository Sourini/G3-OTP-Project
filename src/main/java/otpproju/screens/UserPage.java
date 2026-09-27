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

        Label greeting = new Label(
                "Hello, " + user.getUsername() + " 👋"
        );

        greeting.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Ready to study?"
        );

        VBox header = new VBox(
                5,
                greeting,
                subtitle
        );

        // Create Set Card
        VBox createSetCard = new VBox(8);

        Label createSetTitle =
                new Label("Create a flashcard set");

        createSetTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        Label createSetDescription =
                new Label(
                        "Organize your flashcards into a new set."
                );

        Button createSetButton =
                new Button("＋ Create Set");

        createSetButton.setMaxWidth(
                Double.MAX_VALUE
        );

        createSetButton.setPrefHeight(42);

        createSetButton.setOnAction(e ->
                new CreateCardSet(user).show(stage)
        );

        createSetCard.getChildren().addAll(
                createSetTitle,
                createSetDescription,
                createSetButton
        );

        createSetCard.setPadding(
                new Insets(18)
        );

        createSetCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-color: #dddddd;"
        );

        // Study Card
        VBox studyCard = new VBox(8);

        Label studyTitle =
                new Label("Study your cards");

        studyTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        Label studyDescription =
                new Label(
                        "Review your flashcard sets."
                );

        Button studyButton =
                new Button("▶ Study Cards");

        studyButton.setMaxWidth(
                Double.MAX_VALUE
        );

        studyButton.setPrefHeight(42);

        studyButton.setOnAction(e ->
                new StudyCards(user).show(stage)
        );

        studyCard.getChildren().addAll(
                studyTitle,
                studyDescription,
                studyButton
        );

        studyCard.setPadding(
                new Insets(18)
        );

        studyCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-color: #dddddd;"
        );

        // Recent Sets
        Label recentTitle =
                new Label("My Flashcard Sets");

        recentTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        VBox setsBox = new VBox(8);

        loadSets(setsBox);

        // Logout
        Button logoutButton =
                new Button("Logout");

        logoutButton.setMaxWidth(
                Double.MAX_VALUE
        );

        logoutButton.setPrefHeight(42);

        logoutButton.setOnAction(e -> {
            new Login().show(stage);
        });

        VBox content = new VBox(
                18,
                header,
                createSetCard,
                studyCard,
                recentTitle,
                setsBox,
                logoutButton
        );

        content.setPadding(
                new Insets(25)
        );

        content.setMaxWidth(360);

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background-color: #f5f7fb;"
        );

        VBox root = new VBox(scrollPane);

        root.setStyle(
                "-fx-background-color: #f5f7fb;"
        );

        Scene scene = new Scene(
                root,
                390,
                750
        );

        stage.setTitle("Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void loadSets(VBox setsBox) {

        try {

            List<FlashcardSet> sets =
                    flashcardSetService
                            .getFlashcardSetsForUser(
                                    user.getUserId()
                            );

            if (sets.isEmpty()) {

                Label emptyLabel =
                        new Label(
                                "You don't have any sets yet."
                        );

                setsBox.getChildren().add(
                        emptyLabel
                );

                return;
            }

            for (FlashcardSet set : sets) {

                Button setButton =
                        new Button(set.getTitle());

                setButton.setMaxWidth(
                        Double.MAX_VALUE
                );

                setButton.setPrefHeight(45);

                setButton.setOnAction(e ->
                        new StudyCards(user, set)
                                .show(
                                        (Stage) setButton
                                                .getScene()
                                                .getWindow()
                                )
                );

                setsBox.getChildren().add(
                        setButton
                );
            }

        } catch (Exception ex) {

            Label error =
                    new Label(
                            "Could not load flashcard sets."
                    );

            setsBox.getChildren().add(error);
        }
    }
}