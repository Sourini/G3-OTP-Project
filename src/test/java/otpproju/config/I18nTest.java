package otpproju.config;

import javafx.beans.binding.StringBinding;
import javafx.geometry.NodeOrientation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class I18nTest {

    private static final String EN_BUNDLE = "Message_bundle_eng";
    private static final Map<String, String> OTHER_BUNDLES = Map.of(
            "ko", "Message_bundle_kor", "ru", "Message_bundle_rus",
            "ar", "Message_bundle_ara", "fi", "Message_bundle_fin");

    @BeforeEach
    @AfterEach
    void resetLocale() {
        I18n.setLocale(Locale.ENGLISH);
    }

    @Test
    void localeHandling() {
        assertEquals(5, I18n.getSupportedLocales().size());
        assertEquals(Locale.ENGLISH, I18n.getLocale());

        I18n.setLocale(Locale.forLanguageTag("fi-FI"));
        assertEquals(Locale.forLanguageTag("fi"), I18n.getLocale());
        assertEquals(I18n.getLocale(), I18n.localeProperty().get());

        assertThrows(NullPointerException.class, () -> I18n.setLocale(null));
        assertThrows(IllegalArgumentException.class, () -> I18n.setLocale(Locale.FRENCH));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> I18n.t(null));
        assertThrows(NullPointerException.class, () -> I18n.formatNumber(null));
        assertThrows(NullPointerException.class, () -> I18n.formatDate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "ko", "ru", "ar", "fi"})
    void perLanguageBehaviour(String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        LocalDate date = LocalDate.of(2024, 3, 15);
        I18n.setLocale(locale);

        assertEquals("missing.key", I18n.t("missing.key"));
        assertEquals(NumberFormat.getNumberInstance(locale).format(1234567.891),
                I18n.formatNumber(1234567.891));
        assertEquals(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)),
                I18n.formatDate(date));

        boolean rtl = "ar".equals(lang);
        assertEquals(rtl, I18n.isRightToLeft());
        assertEquals(rtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT,
                I18n.getOrientation());
    }

    @Test
    void translationAndBinding() {
        ResourceBundle en = ResourceBundle.getBundle(EN_BUNDLE, Locale.ROOT);
        String key = en.keySet().stream().filter(k -> !en.getString(k).isBlank()).findFirst().orElse(null);
        assumeTrue(key != null, "English bundle has no entries");

        StringBinding binding = I18n.bind(key);
        assertEquals(en.getString(key), I18n.t(key));
        assertEquals(en.getString(key), binding.get());

        I18n.setLocale(Locale.KOREAN);
        assertEquals(I18n.t(key), binding.get());
    }

    @Test
    void fallsBackToEnglishWhenMissingOrBlank() {
        ResourceBundle en = ResourceBundle.getBundle(EN_BUNDLE, Locale.ROOT);
        boolean checked = false;

        for (Map.Entry<String, String> entry : OTHER_BUNDLES.entrySet()) {
            ResourceBundle other = ResourceBundle.getBundle(entry.getValue(), Locale.ROOT);
            String key = en.keySet().stream()
                    .filter(k -> !en.getString(k).isBlank())
                    .filter(k -> !other.containsKey(k) || other.getString(k).isBlank())
                    .findFirst().orElse(null);
            if (key != null) {
                I18n.setLocale(Locale.forLanguageTag(entry.getKey()));
                assertEquals(en.getString(key), I18n.t(key));
                checked = true;
            }
        }
        assumeTrue(checked, "All English keys are translated in every language");
    }

    @Test
    void formatUsesKeyAsPatternWhenNoTranslation() {
        assertEquals("Hello World", I18n.format("Hello {0}", "World"));
        assertEquals("1,234", I18n.format("{0}", 1234));
    }

    @Test
    void textOrientation() {
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, I18n.getTextOrientation(null));
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, I18n.getTextOrientation("Hello"));
        assertEquals(NodeOrientation.RIGHT_TO_LEFT,
                I18n.getTextOrientation("\u0645\u0631\u062D\u0628\u0627"));
    }
}