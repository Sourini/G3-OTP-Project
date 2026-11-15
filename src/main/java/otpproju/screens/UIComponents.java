package otpproju.screens;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import otpproju.model.FlashcardSet;

// Provides reusable JavaFX components to maintain consistent
// button sizing and page title styling across application screens.
public final class UIComponents {

    private UIComponents() {}

    public static ComboBox<FlashcardSet> flashcardSetSelector() {
        ComboBox<FlashcardSet> selector = new ComboBox<>();
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(FlashcardSet set) {
                return set == null ? "" : set.getTitle();
            }

            @Override
            public FlashcardSet fromString(String text) {
                throw new UnsupportedOperationException("The set selector is not editable");
            }
        });
        return selector;
    }

    // Creates a full-width button with the specified preferred
    // height and shared primary button styling.
    private static Button createButton(String text, double height) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(height);
        button.getStyleClass().add("primary-button");
        return button;
    }

    // Creates a standard primary button with a preferred height of 45 pixels.
    public static Button primaryButton(String text) {
        return createButton(text, 45);
    }

    // Creates a dashboard action button with a preferred height of 42 pixels.
    public static Button dashboardButton(String text) {
        Button button = createButton(text, 42);
        button.getStyleClass().add("dashboard-button");
        return button;
    }

    // Creates a page title label with the shared page title style.
    public static Label pageTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }
}