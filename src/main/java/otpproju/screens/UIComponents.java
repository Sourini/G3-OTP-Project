package otpproju.screens;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public final class UIComponents {

    private UIComponents() {}

    private static Button createButton(String text, double height) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(height);
        button.getStyleClass().add("primary-button");
        return button;
    }

    public static Button primaryButton(String text) {
        return createButton(text, 45);
    }

    public static Button dashboardButton(String text) {
        Button button = createButton(text, 42);
        button.getStyleClass().add("dashboard-button");
        return button;
    }

    public static Label pageTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }
}