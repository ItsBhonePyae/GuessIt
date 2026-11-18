package Testing_for_bascially_everything;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import Testing_for_bascially_everything.assets.AssetCatalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for {@code SidebarTest.fxml}.
 *
 * <p>Settings is not a page: {@link #settingsButton} opens
 * {@code #settingsOverlay}, which sits above the whole frame and can be
 * dismissed with the close button, the DONE button, a click on the dim
 * backdrop, or Escape.</p>
 *
 * <p>The FXML intentionally has no {@code fx:controller} attribute so Scene
 * Builder can still open it without this class on its classpath. Load it with
 * {@link #load(SidebarTestModel)} instead of a bare {@code FXMLLoader}.</p>
 *
 * <p>Motion is part of the view's behaviour: an intro reveal, a sliding sidebar
 * rail, page cross-fades, a scaled settings modal, a slow poster drift and
 * press feedback on every button. {@link #setMotionEnabled(boolean)} turns it
 * all off for headless probes or reduced-motion preferences.</p>
 */
public final class SidebarTestController {

    private static final double SIDEBAR_OPEN_WIDTH = 260.0;
    private static final double SIDEBAR_CLOSED_WIDTH = 76.0;

    /** Wall cards are 180x270; artwork is decoded at 2x that in AssetCatalog. */
    private static final double POSTER_WIDTH = 180.0;
    private static final double POSTER_HEIGHT = 270.0;

    /** Card style class to bundled file name, same slugs the old test used. */
    private static final Map<String, String> POSTER_SLUGS = Map.ofEntries(
        Map.entry("poster-blade", "blade-runner-2049"),
        Map.entry("poster-moonlight", "moonlight"),
        Map.entry("poster-knives", "knives-out"),
        Map.entry("poster-dune", "dune-part-two"),
        Map.entry("poster-night", "nightcrawler"),
        Map.entry("poster-her", "her"),
        Map.entry("poster-arrival", "arrival"),
        Map.entry("poster-parasite", "parasite"),
        Map.entry("poster-madmax", "mad-max"),
        Map.entry("poster-whiplash", "whiplash"),
        Map.entry("poster-interstellar", "interstellar"),
        Map.entry("poster-lalaland", "la-la-land"),
        Map.entry("poster-batman", "the-batman"));

    private static final Duration COLLAPSE_TIME = Duration.millis(240);
    private static final Duration PAGE_TIME = Duration.millis(240);
    private static final Duration MODAL_IN_TIME = Duration.millis(200);
    private static final Duration MODAL_OUT_TIME = Duration.millis(130);

    /** Static so a probe can disable motion before the FXML is loaded. */
    private static boolean motionEnabled = true;

    private final SidebarTestModel model;

    private Runnable searchAction = () -> { };
    private Runnable notificationsAction = () -> { };
    private Runnable playAction = () -> { };
    private Runnable tutorialAction = () -> { };

    /** Labels that disappear when the rail collapses to icons only. */
    private List<Label> collapsibleLabels = List.of();
    /** Everything that should fade away with the rail text. */
    private List<Node> fadingRails = List.of();

    private Timeline settingsAnimation;
    private Boolean collapsedState;
    private boolean introPlayed;

    /** One clock drives every poster column; it idles while the wall is hidden. */
    private AnimationTimer posterDrift;
    private final List<Node> posterStrips = new ArrayList<>();
    /** Survives timer restarts so pausing does not make the wall jump. */
    private double driftElapsed;

    /** Pauses the wall and drops its caches while the movie page owns the stage. */
    private volatile boolean suspended;

    @FXML private StackPane shell;
    @FXML private VBox sidebar;
    @FXML private VBox brandText;
    @FXML private Button navSingleplayer;
    @FXML private Button navHistory;
    @FXML private Label navSingleplayerLabel;
    @FXML private Label navHistoryLabel;
    @FXML private Button sidebarToggle;
    @FXML private HBox header;
    @FXML private Label pageTitle;
    @FXML private Button searchButton;
    @FXML private Button bellButton;
    @FXML private Button settingsButton;
    @FXML private StackPane singleplayerPage;
    @FXML private StackPane historyPage;
    @FXML private StackPane pageHost;
    @FXML private VBox heroCopy;
    @FXML private VBox promoCard;
    @FXML private VBox profileText;
    @FXML private Label playerName;
    @FXML private Label playerLevel;
    @FXML private Label version;
    @FXML private Label streakValue;
    @FXML private Button filterAll;
    @FXML private Button filterWins;
    @FXML private Button filterLosses;
    @FXML private HBox historyRowDune;
    @FXML private HBox historyRowWhiplash;
    @FXML private Label historyEmpty;
    @FXML private Label historyCount;
    @FXML private Button playNow;
    @FXML private Button howToPlay;
    @FXML private StackPane settingsOverlay;
    @FXML private Region settingsBackdrop;
    @FXML private VBox settingsModal;
    @FXML private Button settingsClose;
    @FXML private Button settingsDone;
    @FXML private Button difficultyEasy;
    @FXML private Button difficultyMedium;
    @FXML private Button difficultyHard;
    @FXML private Button accentLime;
    @FXML private Button accentViolet;
    @FXML private Label settingsStatus;

    public SidebarTestController() {
        this(new SidebarTestModel());
    }

    public SidebarTestController(SidebarTestModel model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    /** Disables every transition, for probes and reduced-motion setups. */
    public static void setMotionEnabled(boolean enabled) {
        motionEnabled = enabled;
    }

    /** Loads the layout and wires it to {@code model}. */
    public static Parent load(SidebarTestModel model) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            SidebarTestController.class.getResource("SidebarTest.fxml"));
        loader.setController(new SidebarTestController(model));
        return loader.load();
    }

    /** Loads the layout for an already-configured controller. */
    public static Parent load(SidebarTestController controller) throws IOException {
        Objects.requireNonNull(controller, "controller");
        FXMLLoader loader = new FXMLLoader(
            SidebarTestController.class.getResource("SidebarTest.fxml"));
        loader.setController(controller);
        return loader.load();
    }

    public SidebarTestModel getModel() {
        return model;
    }

    public void setSearchAction(Runnable action) {
        searchAction = action == null ? () -> { } : action;
    }

    public void setNotificationsAction(Runnable action) {
        notificationsAction = action == null ? () -> { } : action;
    }

    /** PLAY NOW on the home page. */
    public void setPlayAction(Runnable action) {
        playAction = action == null ? () -> { } : action;
    }

    /** HOW TO PLAY on the home page. */
    public void setTutorialAction(Runnable action) {
        tutorialAction = action == null ? () -> { } : action;
    }

    /** Pauses the wall and drops its caches while the movie page owns the stage. */
    public void setSuspended(boolean suspended) {
        if (this.suspended == suspended) {
            return;
        }
        this.suspended = suspended;
        if (suspended) {
            stopPosterDrift();
            for (Node strip : posterStrips) {
                strip.setCache(false);
            }
        } else {
            for (Node strip : posterStrips) {
                strip.setCache(true);
                strip.setCacheHint(CacheHint.QUALITY);
            }
            startPosterDrift();
        }
    }

    @FXML
    private void initialize() {
        navSingleplayer.setOnAction(event -> model.showPage(SidebarTestModel.SINGLEPLAYER));
        navHistory.setOnAction(event -> model.showPage(SidebarTestModel.HISTORY));

        sidebarToggle.setOnAction(event ->
            model.setSidebarCollapsed(!model.isSidebarCollapsed()));
        searchButton.setOnAction(event -> searchAction.run());
        bellButton.setOnAction(event -> notificationsAction.run());
        settingsButton.setOnAction(event -> model.openSettings());
        playNow.setOnAction(event -> playAction.run());
        howToPlay.setOnAction(event -> tutorialAction.run());

        settingsClose.setOnAction(event -> model.closeSettings());
        settingsDone.setOnAction(event -> model.closeSettings());
        settingsBackdrop.setOnMouseClicked(event -> model.closeSettings());
        settingsOverlay.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && model.isSettingsOpen()) {
                model.closeSettings();
                event.consume();
            }
        });

        difficultyEasy.setOnAction(event -> chooseDifficulty(SidebarTestModel.Difficulty.EASY));
        difficultyMedium.setOnAction(event -> chooseDifficulty(SidebarTestModel.Difficulty.MEDIUM));
        difficultyHard.setOnAction(event -> chooseDifficulty(SidebarTestModel.Difficulty.HARD));
        accentLime.setOnAction(event -> chooseAccent(SidebarTestModel.Accent.LIME));
        accentViolet.setOnAction(event -> chooseAccent(SidebarTestModel.Accent.VIOLET));

        filterAll.setOnAction(event -> model.setHistoryFilter(SidebarTestModel.ALL));
        filterWins.setOnAction(event -> model.setHistoryFilter(SidebarTestModel.WINS));
        filterLosses.setOnAction(event -> model.setHistoryFilter(SidebarTestModel.LOSSES));

        collapsibleLabels = List.of(
            navSingleplayerLabel, navHistoryLabel, version);
        fadingRails = List.of(
            brandText, promoCard, profileText, version,
            navSingleplayerLabel, navHistoryLabel);

        bind();
        refreshNav();
        refreshSelections();
        refreshHistory();

        collapsedState = false;
        applyCollapsed(model.isSidebarCollapsed());
        clipPages();
        installPosterArtwork();
        installMotion();
        installPressFeedback();
        attachWindowListeners();
        playIntroWhenShown();
    }

    private void bind() {
        pageTitle.textProperty().bind(Bindings.createStringBinding(
            model::pageTitle, model.pageProperty()));
        settingsStatus.textProperty().bind(model.statusProperty());
        playerName.textProperty().bind(model.playerNameProperty());
        playerLevel.textProperty().bind(model.playerLevelProperty());
        streakValue.textProperty().bind(Bindings.createStringBinding(
            model::streakText, model.streakDaysProperty()));

        singleplayerPage.visibleProperty().bind(
            model.pageProperty().isEqualTo(SidebarTestModel.SINGLEPLAYER));
        singleplayerPage.managedProperty().bind(singleplayerPage.visibleProperty());
        historyPage.visibleProperty().bind(
            model.pageProperty().isEqualTo(SidebarTestModel.HISTORY));
        historyPage.managedProperty().bind(historyPage.visibleProperty());

        model.pageProperty().addListener((observable, old, value) -> {
            refreshNav();
            animatePage(SidebarTestModel.HISTORY.equals(value) ? historyPage : singleplayerPage);
            if (SidebarTestModel.SINGLEPLAYER.equals(value)) {
                startPosterDrift();
            } else {
                stopPosterDrift();
            }
        });
        model.sidebarCollapsedProperty().addListener(
            (observable, old, value) -> applyCollapsed(value));
        model.difficultyProperty().addListener(
            (observable, old, value) -> refreshSelections());
        model.accentProperty().addListener(
            (observable, old, value) -> refreshSelections());
        model.historyFilterProperty().addListener(
            (observable, old, value) -> refreshHistory());
        model.settingsOpenProperty().addListener((observable, old, open) -> {
            if (open) {
                openSettings();
            } else {
                closeSettings();
            }
        });
    }

    // =============================================================== motion

    private void installMotion() {
        settingsOverlay.setVisible(false);
        settingsOverlay.setManaged(false);
    }

    private void playIntroWhenShown() {
        if (shell.getScene() != null) {
            Platform.runLater(this::playIntro);
        } else {
            shell.sceneProperty().addListener((observable, old, scene) -> {
                if (scene != null) {
                    Platform.runLater(this::playIntro);
                }
            });
        }
    }

    /** Sidebar slides in, header fades, hero copy staggers up and the wall blooms. */
    private void playIntro() {
        if (introPlayed) {
            return;
        }
        introPlayed = true;
        if (!motionEnabled) {
            return;
        }

        sidebar.setOpacity(0);
        sidebar.setTranslateX(-18);
        Timeline rail = new Timeline(new KeyFrame(Duration.millis(380),
            new KeyValue(sidebar.opacityProperty(), 1, Interpolator.EASE_OUT),
            new KeyValue(sidebar.translateXProperty(), 0, Interpolator.EASE_OUT)));
        rail.play();

        header.setOpacity(0);
        Timeline top = new Timeline(new KeyFrame(Duration.millis(320),
            new KeyValue(header.opacityProperty(), 1, Interpolator.EASE_OUT)));
        top.setDelay(Duration.millis(110));
        top.play();

        Node wall = shell.lookup("#posterBackdrop");
        if (wall != null) {
            wall.setOpacity(0);
            FadeTransition bloom = new FadeTransition(Duration.millis(700), wall);
            bloom.setToValue(1);
            bloom.play();
            startPosterDrift();
        }

        int index = 0;
        for (Node child : heroCopy.getChildren()) {
            child.setOpacity(0);
            child.setTranslateY(18);
            Timeline rise = new Timeline(new KeyFrame(Duration.millis(360),
                new KeyValue(child.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(child.translateYProperty(), 0, Interpolator.EASE_OUT)));
            rise.setDelay(Duration.millis(140 + index * 70L));
            rise.play();
            index++;
        }
    }

    /**
     * Very slow drift keeps the wall from reading as a screenshot.
     *
     * <p>One {@link AnimationTimer} drives every column, as in the code-built
     * sidebar test, instead of ten independent timelines. It ticks at ~20 fps
     * (the drift period is seconds long, so 60 fps is wasted work), pauses when
     * the wall is off screen, the window is not focused, or the movie page owns
     * the scene, and clamps its delta so a stalled pulse cannot make it jump.
     * Each strip is cached, so a pulse translates a cached bitmap rather than
     * re-rasterising the cards.</p>
     */
    private void startPosterDrift() {
        if (posterDrift != null || !motionEnabled || suspended) {
            return;
        }
        posterStrips.clear();
        posterStrips.addAll(shell.lookupAll(".poster-strip"));
        if (posterStrips.isEmpty()) {
            return;
        }
        for (Node strip : posterStrips) {
            strip.setCache(true);
            strip.setCacheHint(CacheHint.QUALITY);
        }
        posterDrift = new AnimationTimer() {
            private long last;
            private long lastPaint;

            @Override
            public void handle(long now) {
                if (!wallIsOnScreen()) {
                    stopPosterDrift();
                    return;
                }
                if (last == 0) {
                    last = now;
                    lastPaint = now;
                    return;
                }
                double delta = Math.min(0.05, (now - last) / 1_000_000_000.0);
                last = now;
                driftElapsed += delta;

                // ~20 fps is plenty for a drift whose period is 9-12 seconds.
                if (now - lastPaint < 50_000_000L) {
                    return;
                }
                lastPaint = now;
                for (int i = 0; i < posterStrips.size(); i++) {
                    double amplitude = 10 + (i % 3) * 4;
                    double period = 9 + i % 4;
                    posterStrips.get(i).setTranslateY(
                        amplitude * Math.sin(Math.PI * 2 * (driftElapsed / period) + i * 0.7));
                }
            }
        };
        posterDrift.start();
    }

    /** Stops the clock without forgetting how far the drift has progressed. */
    private void stopPosterDrift() {
        if (posterDrift != null) {
            posterDrift.stop();
            posterDrift = null;
        }
    }

    /** The wall is the expensive part of the view: idle the clock when unseen. */
    private boolean wallIsOnScreen() {
        if (!motionEnabled || suspended || !singleplayerPage.isVisible()) {
            return false;
        }
        if (singleplayerPage.getScene() == null) {
            return false;
        }
        javafx.stage.Window window = singleplayerPage.getScene().getWindow();
        if (window == null) {
            return true;
        }
        return window.isShowing() && window.isFocused();
    }

    /** Hook the window so the clock pauses when the app loses focus. */
    private void attachWindowListeners() {
        if (shell.getScene() != null) {
            bindWindow(shell.getScene().getWindow());
        } else {
            shell.sceneProperty().addListener((observable, old, scene) -> {
                if (scene != null) {
                    Platform.runLater(() -> bindWindow(scene.getWindow()));
                }
            });
        }
    }

    private void bindWindow(javafx.stage.Window window) {
        if (window == null) {
            return;
        }
        window.focusedProperty().addListener((observable, old, focused) -> {
            if (focused) {
                startPosterDrift();
            } else {
                stopPosterDrift();
            }
        });
        window.showingProperty().addListener((observable, old, showing) -> {
            if (showing) {
                startPosterDrift();
            } else {
                stopPosterDrift();
            }
        });
    }

    private void animatePage(Node page) {
        if (!motionEnabled || page == null) {
            return;
        }
        page.setOpacity(0);
        page.setTranslateX(14);
        Timeline fade = new Timeline(new KeyFrame(PAGE_TIME,
            new KeyValue(page.opacityProperty(), 1, Interpolator.EASE_OUT),
            new KeyValue(page.translateXProperty(), 0, Interpolator.EASE_OUT)));
        fade.play();
    }

    private void openSettings() {
        settingsOverlay.setManaged(true);
        settingsOverlay.setVisible(true);
        settingsOverlay.toFront();
        refreshSelections();
        settingsClose.requestFocus();

        stopSettingsAnimation();
        if (!motionEnabled) {
            settingsOverlay.setOpacity(1);
            settingsModal.setOpacity(1);
            settingsModal.setScaleX(1);
            settingsModal.setScaleY(1);
            return;
        }
        settingsOverlay.setOpacity(0);
        settingsModal.setOpacity(0);
        settingsModal.setScaleX(0.96);
        settingsModal.setScaleY(0.96);
        settingsAnimation = new Timeline(new KeyFrame(MODAL_IN_TIME,
            new KeyValue(settingsOverlay.opacityProperty(), 1, Interpolator.EASE_OUT),
            new KeyValue(settingsModal.opacityProperty(), 1, Interpolator.EASE_OUT),
            new KeyValue(settingsModal.scaleXProperty(), 1, Interpolator.EASE_OUT),
            new KeyValue(settingsModal.scaleYProperty(), 1, Interpolator.EASE_OUT)));
        settingsAnimation.play();
    }

    private void closeSettings() {
        stopSettingsAnimation();
        if (!motionEnabled) {
            settingsOverlay.setVisible(false);
            settingsOverlay.setManaged(false);
            settingsOverlay.setOpacity(1);
            return;
        }
        Timeline fade = new Timeline(new KeyFrame(MODAL_OUT_TIME,
            new KeyValue(settingsOverlay.opacityProperty(), 0, Interpolator.EASE_IN)));
        fade.setOnFinished(event -> {
            settingsOverlay.setVisible(false);
            settingsOverlay.setManaged(false);
            settingsOverlay.setOpacity(1);
        });
        settingsAnimation = fade;
        fade.play();
    }

    private void stopSettingsAnimation() {
        if (settingsAnimation != null) {
            settingsAnimation.stop();
            settingsAnimation = null;
        }
    }

    /** 0.96 squish on press, springy return, slight lift on hover for CTAs. */
    private void installPressFeedback() {
        for (Node node : shell.lookupAll(".button")) {
            if (!(node instanceof ButtonBase button)) {
                continue;
            }
            button.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> scaleTo(button, 0.96, 70));
            button.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> scaleTo(button, 1.0, 150));
            if (button.getStyleClass().contains("primary-button")
                || button.getStyleClass().contains("secondary-button")
                || button.getStyleClass().contains("modal-done")) {
                button.addEventHandler(MouseEvent.MOUSE_ENTERED,
                    event -> scaleTo(button, 1.02, 130));
                button.addEventHandler(MouseEvent.MOUSE_EXITED,
                    event -> scaleTo(button, 1.0, 130));
            }
        }
    }

    private static void scaleTo(Node node, double value, long millis) {
        if (!motionEnabled) {
            return;
        }
        Timeline pulse = new Timeline(new KeyFrame(Duration.millis(millis),
            new KeyValue(node.scaleXProperty(), value, Interpolator.EASE_OUT),
            new KeyValue(node.scaleYProperty(), value, Interpolator.EASE_OUT)));
        pulse.play();
    }

    // ============================================================== layout

    /** Clips the page host so the oversized wall cannot paint over the sidebar. */
    private void clipPages() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pageHost.widthProperty());
        clip.heightProperty().bind(pageHost.heightProperty());
        pageHost.setClip(clip);
    }

    /**
     * Collapses the rail to icons only. The width tweens rather than snapping,
     * and the text fades in step so the icons re-centre while it moves.
     */
    private void applyCollapsed(boolean collapsed) {
        boolean open = !collapsed;
        boolean changed = collapsedState == null || collapsedState != collapsed;
        collapsedState = collapsed;
        setFlag(sidebar, "collapsed", collapsed);

        if (!motionEnabled) {
            sidebar.setPrefWidth(collapsed ? SIDEBAR_CLOSED_WIDTH : SIDEBAR_OPEN_WIDTH);
            setRailContentVisible(open);
            for (Node node : fadingRails) {
                node.setOpacity(1);
            }
            return;
        }
        if (!changed) {
            return;
        }

        if (open) {
            setRailContentVisible(true);
            for (Node node : fadingRails) {
                node.setOpacity(0);
            }
        }

        Timeline width = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth())),
            new KeyFrame(COLLAPSE_TIME,
                new KeyValue(sidebar.prefWidthProperty(),
                    collapsed ? SIDEBAR_CLOSED_WIDTH : SIDEBAR_OPEN_WIDTH,
                    Interpolator.EASE_BOTH)));

        ParallelTransition transition = new ParallelTransition(width);
        for (Node node : fadingRails) {
            FadeTransition fade = new FadeTransition(
                open ? Duration.millis(170) : Duration.millis(110), node);
            fade.setToValue(open ? 1 : 0);
            if (open) {
                fade.setDelay(Duration.millis(90));
            }
            transition.getChildren().add(fade);
        }
        if (!open) {
            transition.setOnFinished(event -> setRailContentVisible(false));
        }
        transition.play();
    }

    private void setRailContentVisible(boolean open) {
        for (Label label : collapsibleLabels) {
            label.setVisible(open);
            label.setManaged(open);
        }
        brandText.setVisible(open);
        brandText.setManaged(open);
        promoCard.setVisible(open);
        promoCard.setManaged(open);
        profileText.setVisible(open);
        profileText.setManaged(open);
    }

    private void chooseDifficulty(SidebarTestModel.Difficulty value) {
        model.setDifficulty(value);
        model.setStatus("Difficulty set to " + value.name() + ".");
    }

    private void chooseAccent(SidebarTestModel.Accent value) {
        model.setAccent(value);
        model.setStatus("Accent preview set to " + value.label() + ".");
    }

    private void refreshNav() {
        boolean singleplayer = SidebarTestModel.SINGLEPLAYER.equals(model.getPage());
        setFlag(navSingleplayer, "active", singleplayer);
        setFlag(navHistory, "active", !singleplayer);
    }

    /** Both demo rows are losses, so the wins filter shows the empty state. */
    private void refreshHistory() {
        String filter = model.getHistoryFilter();
        setFlag(filterAll, "active", SidebarTestModel.ALL.equals(filter));
        setFlag(filterWins, "active", SidebarTestModel.WINS.equals(filter));
        setFlag(filterLosses, "active", SidebarTestModel.LOSSES.equals(filter));

        boolean lossVisible = !SidebarTestModel.WINS.equals(filter);
        historyRowDune.setVisible(lossVisible);
        historyRowDune.setManaged(lossVisible);
        historyRowWhiplash.setVisible(lossVisible);
        historyRowWhiplash.setManaged(lossVisible);

        int shown = lossVisible ? 2 : 0;
        historyCount.setText(shown + " / 2 ROUNDS");
        historyEmpty.setVisible(shown == 0);
        historyEmpty.setManaged(shown == 0);
    }

    private void refreshSelections() {
        setFlag(difficultyEasy, "active",
            model.getDifficulty() == SidebarTestModel.Difficulty.EASY);
        setFlag(difficultyMedium, "active",
            model.getDifficulty() == SidebarTestModel.Difficulty.MEDIUM);
        setFlag(difficultyHard, "active",
            model.getDifficulty() == SidebarTestModel.Difficulty.HARD);
        setFlag(accentLime, "active", model.getAccent() == SidebarTestModel.Accent.LIME);
        setFlag(accentViolet, "active", model.getAccent() == SidebarTestModel.Accent.VIOLET);
    }

    /** Style classes are additive, so guard against duplicates. */
    private static void setFlag(Node node, String styleClass, boolean on) {
        if (on) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    // =============================================================== artwork

    /**
     * Applies the bundled artwork from {@link AssetCatalog}, which decodes each
     * file once at 2x card size and shares that image between every card that
     * uses it. The CSS gradient stays underneath as the fallback when a poster
     * file is missing.
     */
    private void installPosterArtwork() {
        for (Node card : shell.lookupAll(".poster-card")) {
            if (!(card instanceof Pane pane)) {
                continue;
            }
            String slug = posterSlug(card);
            if (slug == null) {
                continue;
            }
            Image artwork = AssetCatalog.poster(slug);
            if (artwork == null || artwork.isError()) {
                continue;
            }
            ImageView view = new ImageView(artwork);
            view.setSmooth(true);
            view.setMouseTransparent(true);
            view.setFitWidth(POSTER_WIDTH);
            view.setFitHeight(POSTER_HEIGHT);
            view.setViewport(coverViewport(artwork, POSTER_WIDTH, POSTER_HEIGHT));
            pane.getChildren().add(0, view);

            // A background radius cannot round a child node, so clip the card.
            Rectangle clip = new Rectangle(POSTER_WIDTH, POSTER_HEIGHT);
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            card.setClip(clip);
        }
    }

    private static String posterSlug(Node card) {
        for (String styleClass : card.getStyleClass()) {
            String slug = POSTER_SLUGS.get(styleClass);
            if (slug != null) {
                return slug;
            }
        }
        return null;
    }

    /** Centre-crops a poster so it covers the card instead of stretching. */
    private static Rectangle2D coverViewport(Image image, double width, double height) {
        double targetRatio = width / height;
        double imageRatio = image.getWidth() / image.getHeight();
        if (imageRatio > targetRatio) {
            double cropWidth = image.getHeight() * targetRatio;
            return new Rectangle2D((image.getWidth() - cropWidth) / 2, 0,
                cropWidth, image.getHeight());
        }
        double cropHeight = image.getWidth() / targetRatio;
        return new Rectangle2D(0, (image.getHeight() - cropHeight) / 2,
            image.getWidth(), cropHeight);
    }
}
