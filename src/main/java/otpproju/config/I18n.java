package otpproju.config;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public final class I18n {

    private static final List<Locale> SUPPORTED_LOCALES =
            List.of(
                    Locale.ENGLISH,
                    Locale.KOREAN,
                    Locale.forLanguageTag("ru")
            );

    private static final Map<String, String> BUNDLE_NAMES =
            Map.of(
                    "en", "Message_bundle_eng",
                    "ko", "Message_bundle_kor",
                    "ru", "Message_bundle_rus"
            );

    private static final ReadOnlyObjectWrapper<Locale> LOCALE =
            new ReadOnlyObjectWrapper<>(Locale.ENGLISH);

    private I18n() {
    }

    public static List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public static Locale getLocale() {
        return LOCALE.get();
    }

    public static ReadOnlyObjectProperty<Locale> localeProperty() {
        return LOCALE.getReadOnlyProperty();
    }

    public static void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null");

        Locale supported = SUPPORTED_LOCALES.stream()
                .filter(candidate ->
                        candidate.getLanguage().equals(locale.getLanguage()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported language: " + locale.toLanguageTag()
                ));

        LOCALE.set(supported);
    }

    public static String t(String key) {
        Objects.requireNonNull(key, "Translation key must not be null");

        String translated = findTranslation(getLocale(), key);

        if (translated != null) {
            return translated;
        }

        String fallback = findTranslation(Locale.ENGLISH, key);

        return fallback != null ? fallback : key;
    }

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(
                () -> t(key),
                localeProperty()
        );
    }

    public static String formatNumber(Number number) {
        Objects.requireNonNull(number, "Number must not be null");

        return NumberFormat.getNumberInstance(getLocale())
                .format(number);
    }

    public static String formatDate(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");

        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(getLocale())
        );
    }

    private static String findTranslation(Locale locale, String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(
                BUNDLE_NAMES.get(locale.getLanguage()),
                Locale.ROOT
        );

        if (!bundle.containsKey(key)) {
            return null;
        }

        String value = bundle.getString(key);

        return value.isBlank() ? null : value;
    }
}