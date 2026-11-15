package otpproju.screens;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.util.StringConverter;
import otpproju.config.I18n;

import java.util.List;
import java.util.Locale;

final class ContentLanguages {
    private ContentLanguages() {}

    static String displayName(String code) {
        Locale locale = Locale.forLanguageTag(code);
        return locale.getDisplayLanguage(locale);
    }

    static ComboBox<String> selector(List<String> codes) {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().setAll(codes);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(String code) {
                return code == null ? "" : displayName(code);
            }

            @Override
            public String fromString(String text) {
                throw new UnsupportedOperationException("Language selector is not editable");
            }
        });
        return selector;
    }

    static void followTextDirection(TextInputControl field) {
        field.textProperty().addListener((observable, oldText, text) ->
                field.setNodeOrientation(text.isBlank()
                        ? I18n.getOrientation() : I18n.getTextOrientation(text)));
    }
}
