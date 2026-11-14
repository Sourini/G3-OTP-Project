package otpproju.screens;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otpproju.config.I18n;
import otpproju.model.FlashcardSet;
import otpproju.model.User;
import otpproju.repository.FlashcardSetRepository;
import otpproju.service.FlashcardSetService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserPageTest {

    private static final int USER_ID = 1;
    private static final String USERNAME = "test";

    private User user;
    private FakeFlashcardSetService service;
    private Locale originalLocale;

    @BeforeAll
    static void initToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyRunning) {
            started.countDown();
        }
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(action.call());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(10, TimeUnit.SECONDS), "FX action timed out");
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return result.get();
    }

    private static void onFx(Runnable action) throws Exception {
        onFx(() -> {
            action.run();
            return null;
        });
    }

    private Scene showPage() throws Exception {
        return onFx(() -> {
            Stage stage = new Stage();
            new UserPage(user, service).show(stage);
            return stage.getScene();
        });
    }

    private static List<Node> allNodes(Parent root) {
        List<Node> nodes = new ArrayList<>();
        collect(root, nodes);
        return nodes;
    }

    private static void collect(Node node, List<Node> out) {
        out.add(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, out);
            }
        }
    }

    private static List<Label> labels(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(n -> n instanceof Label)
                .map(n -> (Label) n)
                .toList();
    }

    private static List<Button> buttons(Scene scene) {
        return allNodes(scene.getRoot()).stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .toList();
    }

    private static boolean hasLabel(Scene scene, String text) {
        return labels(scene).stream().anyMatch(l -> text.equals(l.getText()));
    }

    private static boolean hasButton(Scene scene, String text) {
        return buttons(scene).stream().anyMatch(b -> text.equals(b.getText()));
    }

    private static FlashcardSet setWithTitle(String title) {
        return new FlashcardSet(USER_ID, title, "A test set");
    }

    // Hand-written fake that returns canned sets or throws a canned failure.
    private static class FakeFlashcardSetService extends FlashcardSetService {
        List<FlashcardSet> sets = List.of();
        RuntimeException failure;
        final List<Integer> requestedUserIds = new ArrayList<>();

        FakeFlashcardSetService() {
            super(new FlashcardSetRepository());
        }

        @Override
        public List<FlashcardSet> getFlashcardSetsForUser(int userId) {
            requestedUserIds.add(userId);
            if (failure != null) {
                throw failure;
            }
            return sets;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        originalLocale = I18n.getLocale();

        user = new User(
                USER_ID,
                USERNAME,
                "test@test.com",
                "hash",
                User.UserType.STUDENT,
                LocalDateTime.now());

        service = new FakeFlashcardSetService();
    }

    @AfterEach
    void tearDown() {
        I18n.setLocale(originalLocale);
    }

    @Test
    void show_setsSceneWithExpectedSizeAndTitle() throws Exception {
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        Scene scene = onFx(() -> {
            Stage stage = new Stage();
            stageRef.set(stage);
            new UserPage(user, service).show(stage);
            return stage.getScene();
        });

        assertNotNull(scene);
        assertEquals(1000, scene.getWidth(), 0.5);
        assertEquals(750, scene.getHeight(), 0.5);
        assertEquals(I18n.t("app.title"), stageRef.get().getTitle());
    }

    @Test
    void show_attachesStylesheet() throws Exception {
        Scene scene = showPage();
        assertFalse(scene.getStylesheets().isEmpty());
        assertTrue(scene.getStylesheets().get(0).endsWith("style.css"));
    }

    @Test
    void show_appliesNodeOrientationFromI18n() throws Exception {
        Scene scene = showPage();
        NodeOrientation expected = I18n.getOrientation();
        assertEquals(expected, scene.getRoot().getNodeOrientation());
    }

    @Test
    void topBar_showsAppTitleAndLogoutButton() throws Exception {
        Scene scene = showPage();

        assertTrue(hasLabel(scene, I18n.t("app.title")));
        assertTrue(hasButton(scene, I18n.t("userPage.logout")));
    }

    @Test
    void logoutButton_hasLogoutStyleClass() throws Exception {
        Scene scene = showPage();

        Button logout = buttons(scene).stream()
                .filter(b -> I18n.t("userPage.logout").equals(b.getText()))
                .findFirst()
                .orElseThrow();

        assertTrue(logout.getStyleClass().contains("logout-button"));
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Locale> languageSelector(Scene scene) {
        return (ComboBox<Locale>) scene.getRoot().lookup(".language-selector");
    }

    @Test
    void languageSelector_listsSupportedLocalesAndSelectsCurrent() throws Exception {
        Scene scene = showPage();
        ComboBox<Locale> selector = languageSelector(scene);

        assertNotNull(selector);
        assertEquals(List.copyOf(I18n.getSupportedLocales()), selector.getItems());
        assertEquals(I18n.getLocale(), selector.getValue());
        assertFalse(selector.isEditable());
    }

    @Test
    void languageSelector_displaysLanguageNameInItsOwnLanguage() throws Exception {
        Scene scene = showPage();
        ComboBox<Locale> selector = languageSelector(scene);

        for (Locale locale : selector.getItems()) {
            assertEquals(locale.getDisplayLanguage(locale),
                    selector.getConverter().toString(locale));
        }
        assertEquals("", selector.getConverter().toString(null));
    }

    @Test
    void languageSelector_fromStringIsUnsupported() throws Exception {
        Scene scene = showPage();
        ComboBox<Locale> selector = languageSelector(scene);

        assertThrows(UnsupportedOperationException.class,
                () -> selector.getConverter().fromString("English"));
    }

    @Test
    void languageSelector_changingLocaleUpdatesI18nAndRebuildsPage() throws Exception {
        List<Locale> supported = List.copyOf(I18n.getSupportedLocales());
        Locale other = supported.stream()
                .filter(l -> !l.equals(I18n.getLocale()))
                .findFirst()
                .orElse(null);
        org.junit.jupiter.api.Assumptions.assumeTrue(other != null,
                "Need at least two supported locales");

        AtomicReference<Stage> stageRef = new AtomicReference<>();
        onFx(() -> {
            Stage stage = new Stage();
            stageRef.set(stage);
            new UserPage(user, service).show(stage);
        });

        Scene before = stageRef.get().getScene();
        onFx(() -> languageSelector(before).setValue(other));

        assertEquals(other, I18n.getLocale());
        Scene after = onFx(() -> stageRef.get().getScene());
        assertNotSame(before, after, "show(stage) should be called again with a new scene");
        assertTrue(hasLabel(after, I18n.t("app.title")));
    }

    @Test
    void languageSelector_selectingCurrentLocaleDoesNothing() throws Exception {
        AtomicReference<Stage> stageRef = new AtomicReference<>();
        onFx(() -> {
            Stage stage = new Stage();
            stageRef.set(stage);
            new UserPage(user, service).show(stage);
        });

        Scene before = stageRef.get().getScene();
        Locale current = I18n.getLocale();
        onFx(() -> languageSelector(before).fireEvent(new javafx.event.ActionEvent()));

        assertEquals(current, I18n.getLocale());
        assertSame(before, onFx(() -> stageRef.get().getScene()));
    }

    @Test
    void header_greetsUserByName() throws Exception {
        Scene scene = showPage();

        String expected = I18n.t("userPage.greet") + ", " + USERNAME;
        assertTrue(hasLabel(scene, expected));
        assertTrue(hasLabel(scene, I18n.t("userPage.subs")));
    }

    @Test
    void dashboard_hasThreeActionCards() throws Exception {
        Scene scene = showPage();

        long cards = allNodes(scene.getRoot()).stream()
                .filter(n -> n.getStyleClass().contains("dashboard-card"))
                .count();

        assertEquals(3, cards);
    }

    @Test
    void dashboard_showsCardTitlesAndButtons() throws Exception {
        Scene scene = showPage();

        assertTrue(hasLabel(scene, I18n.t("userPage.createSet")));
        assertTrue(hasLabel(scene, I18n.t("userPage.studyTitle")));
        assertTrue(hasLabel(scene, I18n.t("userPage.mySets")));

        assertTrue(hasButton(scene, I18n.t("createCardSet.createButton")));
        assertTrue(hasButton(scene, I18n.t("userPage.createButton")));
        assertTrue(hasButton(scene, I18n.t("userPage.studyButton")));
    }

    @Test
    void dashboard_cardButtonsAreFullWidthWithFixedHeight() throws Exception {
        Scene scene = showPage();

        Button studyButton = buttons(scene).stream()
                .filter(b -> I18n.t("userPage.studyButton").equals(b.getText()))
                .findFirst()
                .orElseThrow();

        assertEquals(Double.MAX_VALUE, studyButton.getMaxWidth());
        assertEquals(42, studyButton.getPrefHeight());
    }

    @Test
    void loadSets_queriesServiceWithUsersId() throws Exception {
        showPage();
        assertEquals(List.of(USER_ID), service.requestedUserIds);
    }

    @Test
    void loadSets_whenNoSets_showsEmptyMessage() throws Exception {
        Scene scene = showPage();

        String expected = I18n.t("userPage.noSetError1") + I18n.t("userPage.noSetError2");
        assertTrue(hasLabel(scene, expected));
    }

    @Test
    void loadSets_whenSetsExist_showsOneButtonPerSet() throws Exception {
        service.sets = List.of(
                setWithTitle("Biology"),
                setWithTitle("Spanish"),
                setWithTitle("History"));

        Scene scene = showPage();

        assertTrue(hasButton(scene, "Biology"));
        assertTrue(hasButton(scene, "Spanish"));
        assertTrue(hasButton(scene, "History"));

        String emptyMsg = I18n.t("userPage.noSetError1") + I18n.t("userPage.noSetError2");
        assertFalse(hasLabel(scene, emptyMsg), "Empty message must not appear when sets exist");
    }

    @Test
    void loadSets_setButtonsHaveExpectedSizing() throws Exception {
        service.sets = List.of(setWithTitle("Biology"));

        Scene scene = showPage();

        Button setButton = buttons(scene).stream()
                .filter(b -> "Biology".equals(b.getText()))
                .findFirst()
                .orElseThrow();

        assertEquals(Double.MAX_VALUE, setButton.getMaxWidth());
        assertEquals(45, setButton.getPrefHeight());
    }

    @Test
    void loadSets_whenServiceThrows_showsLoadError() throws Exception {
        service.failure = new RuntimeException("db down");

        Scene scene = showPage();

        String expected = I18n.t("userPage.loadError1") + I18n.t("userPage.loadError2");
        assertTrue(hasLabel(scene, expected));
    }

    @Test
    void loadSets_whenServiceThrows_pageStillRenders() throws Exception {
        service.failure = new RuntimeException("db down");

        Scene scene = showPage();

        assertTrue(hasLabel(scene, I18n.t("userPage.greet") + ", " + USERNAME));
        assertTrue(hasButton(scene, I18n.t("userPage.logout")));
    }
}