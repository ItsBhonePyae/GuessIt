package Testing_for_bascially_everything;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import Testing_for_bascially_everything.assets.AssetCatalog;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongConsumer;

import static Testing_for_bascially_everything.SlidingSidebar_test.*;

/**
 * Demo movie-playing stage, modelled on Movie_Playing_Page.
 *
 * <p>Opened from the SlidingSidebar singleplayer page. It mimics the game
 * layout (round header, clip stage, segmented timeline, controls, answer grid)
 * without needing the database or VLC, and adds a CHOOSE FILE button so a local
 * video file can be picked for the round. The tutorial entry point reuses the
 * same page and overlays guided popovers on each control.</p>
 */
final class Movie_Playing_Demo_Page {

    private static final String[] TITLES = {
        "Blade Runner 2049", "Dune: Part Two", "Arrival", "Whiplash",
        "Parasite", "Interstellar", "La La Land", "Nightcrawler",
        "Her", "Knives Out", "Moonlight", "Mad Max: Fury Road"
    };

    private static final String[] SCENE_GRADIENTS = {
        "linear-gradient(to bottom right, #342066, #0d0a14)",
        "linear-gradient(to bottom right, #553199, #17141f)",
        "linear-gradient(to bottom right, #2b1a4d, #0d0a14)"
    };

    /** Result colours: green for a correct answer, red for a wrong one. */
    private static final String GREEN = "#3ecf6a";
    private static final String RED = "#e5484d";

    /** Points for the 3s, 5s and full-clip windows; a perfect round is 5,000. */
    private static final int[] STAGE_POINTS = {5_000, 3_500, 2_000};
    private static final int SCENE_PENALTY = 150;
    private static final int MIN_CORRECT_POINTS = 50;
    private static final int SCENES = 3;

    private Movie_Playing_Demo_Page() {
    }

    // --------------------------------------------------- segmented timeline

    /**
     * Three-part timeline (0-3s, 3-5s, 5s-end of clip) drawn on a canvas.
     *
     * <p>Mirrors the timeline in {@code Movie_Playing_Page}: unlocked parts are
     * tinted, the part already played fills with the accent colour, locked
     * parts stay dark, and a round knob shows the playhead. Click-drag inside
     * the unlocked window seeks; the locked tail is not reachable.</p>
     */
    static final class DemoTimeline extends Pane {
        private static final double PAD = 12;
        private static final long[] STAGE_MS = {3_000, 5_000};
        private static final String[] LABELS = {"3s", "5s", "FULL CLIP"};
        private static final Color LOCKED = Color.web("#2a2633");
        private static final Color LOCKED_TEXT = Color.web("#6f6b7a");
        private static final Color LIT = Color.web("#46521f");
        private static final Color PLAYED = Color.web(LIME);
        private static final Color KNOB = Color.web("#ece9e1");

        private final Canvas canvas = new Canvas();
        private long totalMs = 10_000;
        private int unlocked = 1;
        private long positionMs;
        private long lastSeekNanos;

        Runnable onSeekStart = () -> { };
        LongConsumer onSeek = ms -> { };
        Runnable onSeekEnd = () -> { };

        DemoTimeline() {
            setId("timeline");
            setMinHeight(52);
            setPrefHeight(52);
            setMaxHeight(52);
            setCursor(Cursor.HAND);
            getChildren().add(canvas);
            setOnMousePressed(event -> {
                onSeekStart.run();
                seekTo(event, true);
            });
            setOnMouseDragged(event -> seekTo(event, false));
            setOnMouseReleased(event -> {
                seekTo(event, true);
                onSeekEnd.run();
            });
        }

        void setTotal(long ms) {
            totalMs = Math.max(ms, 1_000);
            positionMs = Math.min(positionMs, windowEnd());
            redraw();
        }

        void setUnlocked(int stage) {
            unlocked = Math.max(1, Math.min(3, stage));
            redraw();
        }

        void setPosition(long ms) {
            positionMs = Math.max(0, Math.min(ms, totalMs));
            redraw();
        }

        int unlocked() {
            return unlocked;
        }

        long positionMs() {
            return positionMs;
        }

        long totalMs() {
            return totalMs;
        }

        long stageEnd(int index) {
            return index >= 2 ? totalMs : Math.min(STAGE_MS[index], totalMs);
        }

        /** Last playable millisecond of the part the player has unlocked. */
        long windowEnd() {
            return unlocked >= 3 ? totalMs : stageEnd(unlocked - 1);
        }

        private double x(long ms) {
            return PAD + (double) ms / totalMs * Math.max(1, getWidth() - 2 * PAD);
        }

        private void seekTo(javafx.scene.input.MouseEvent event, boolean force) {
            double frac = (event.getX() - PAD) / Math.max(1, getWidth() - 2 * PAD);
            long ms = Math.max(0, Math.min((long) (frac * totalMs), windowEnd()));
            positionMs = ms;
            redraw();
            long now = System.nanoTime();
            if (force || now - lastSeekNanos >= 60_000_000L) {
                lastSeekNanos = now;
                onSeek.accept(ms);
            }
        }

        @Override
        protected void layoutChildren() {
            canvas.setWidth(getWidth());
            canvas.setHeight(getHeight());
            redraw();
        }

        private void redraw() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, w, h);

            double barY = 10;
            double barH = 12;
            g.setFont(Font.font(11));
            g.setTextAlign(TextAlignment.RIGHT);

            long start = 0;
            for (int i = 0; i < 3; i++) {
                long end = stageEnd(i);
                double x0 = x(start) + 2;
                double x1 = x(end) - 2;
                boolean lit = i < unlocked;

                g.setFill(lit ? LIT : LOCKED);
                g.fillRoundRect(x0, barY, Math.max(0, x1 - x0), barH, 6, 6);

                if (lit && positionMs > start) {
                    double px = Math.min(x(positionMs), x1);
                    g.setFill(PLAYED);
                    g.fillRoundRect(x0, barY, Math.max(0, px - x0), barH, 6, 6);
                }

                g.setFill(lit ? PLAYED : LOCKED_TEXT);
                g.fillText(LABELS[i], x1, barY + barH + 16);
                start = end;
            }

            double px = x(Math.min(positionMs, totalMs));
            g.setFill(KNOB);
            g.fillOval(px - 7, barY + barH / 2 - 7, 14, 14);
        }
    }

    static void openPlay(Stage owner) {
        show(owner, false);
    }

    static void openTutorial(Stage owner) {
        show(owner, true);
    }

    /**
     * Swaps the main stage over to the play page instead of opening a second
     * window. The original root is kept so the play header's BACK button can
     * restore the sidebar exactly as it was.
     */
    private static void show(Stage owner, boolean tutorial) {
        if (owner == null || owner.getScene() == null) {
            return;
        }
        Scene scene = owner.getScene();
        Parent home = scene.getRoot();
        String homeTitle = owner.getTitle();

        PlayWindow page = new PlayWindow();
        page.setBackAction(() -> {
            page.dispose();
            scene.setRoot(home);
            owner.setTitle(homeTitle);
        });

        scene.setRoot(page);
        owner.setTitle(tutorial ? "GuessIt - How to play" : "GuessIt - Play");

        if (tutorial) {
            TutorialOverlay overlay = new TutorialOverlay(tour(page), null);
            page.getChildren().add(overlay);
            Platform.runLater(overlay::refresh);
        }
    }

    private static List<TourStep> tour(PlayWindow page) {
        return Arrays.asList(
            new TourStep(page.roundLabel.getParent(), "Rounds and score",
                "A match is five rounds. The round number and your running score stay "
                    + "pinned here, so you always know where you stand."),
            new TourStep(page.videoPane, "The scene",
                "Each round plays a short clip from a movie. The first three seconds are "
                    + "free, so watch closely before the timer bites."),
            new TourStep(page.playBtn, "Play or pause",
                "Replay the clip as often as you like. The sixty-second round timer keeps "
                    + "running while you watch."),
            new TourStep(page.moreBtn, "Unlock more footage",
                "Buy a longer window: 3 seconds, then 5, then the full clip. The timeline "
                    + "lights up as it unlocks, and more footage means fewer points."),
            new TourStep(page.sceneBtn, "Another scene",
                "Switch to a different scene from the same movie. It costs 150 points, "
                    + "but a new angle can be exactly the clue you need."),
            new TourStep(page.fileBtn, "Choose a clip",
                "Pick one of the Lord of the Rings scenes bundled with the project, or load a "
                    + "local video file from your computer."),
            new TourStep(page.searchBtn, "Search for the movie",
                "Type a title and pick it from the matches, then lock it in. The earlier "
                    + "you answer, the more of the 5,000 points you keep."),
            new TourStep(page.timerLabel, "Beat the clock",
                "You get sixty seconds per round. At zero the round ends automatically "
                    + "and the correct title is revealed.")
        );
    }

    // ---------------------------------------------------------- play window

    static final class PlayWindow extends StackPane {

        private final Label roundLabel;
        private final Label scoreLabel;
        private final Label worthLabel;
        private final Label timerLabel;
        private final StackPane videoPane;
        private final Label sceneBadge;
        private final Label fileLabel;
        private final Label statusLabel;
        private final Label clipTime;
        private final Button playBtn;
        private final ToggleButton loopBtn;
        private final Button moreBtn;
        private final Button sceneBtn;
        private final MenuButton fileBtn;
        private final Button nextBtn;
        private final Button backBtn;
        private final DemoTimeline timeline = new DemoTimeline();
        private final Button searchBtn;
        private final HBox answerRow;
        private final Label answerValue;
        private final Button confirmBtn;
        private StackPane searchLayer;
        private TextField searchField;
        private ListView<String> searchResults;
        private Label searchInfo;
        private Label searchPlaceholder;

        private final MediaView mediaView = new MediaView();
        private final VBox emptyState;
        private final Slider volume;
        private final Label resultTitle;
        private final HBox resultBanner;

        private MediaPlayer player;
        private Runnable backAction;
        private final Random random = new Random();
        private Timeline ticker;
        private int remaining = 60;
        private int round = 1;
        private int score;
        private final int[] stage = {1, 1, 1};
        private final boolean[] seen = new boolean[SCENES];
        private int sceneIdx = 1;
        private String correctTitle;
        private String pickedTitle;
        private boolean answered;
        private boolean playing;
        private boolean seeking;
        private boolean resumeAfterSeek;

        PlayWindow() {
            setId("playRoot");
            setStyle("-fx-background-color: #0f0d14;");

            roundLabel = label("ROUND 1 OF 5", 15, FontWeight.BLACK, WHITE);
            roundLabel.setId("roundLabel");
            scoreLabel = label("SCORE 0", 9, FontWeight.BOLD, MUTED);
            scoreLabel.setId("scoreLabel");
            worthLabel = label("WORTH 5,000 PTS", 10, FontWeight.BOLD, LIME);
            worthLabel.setId("worthLabel");

            timerLabel = label("1:00", 26, FontWeight.BOLD, WHITE);
            timerLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
            timerLabel.setId("timerLabel");

            videoPane = new StackPane();
            videoPane.setId("videoPane");
            videoPane.setMinHeight(240);

            mediaView.setVisible(false);
            mediaView.setSmooth(true);
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(videoPane.widthProperty().subtract(28));
            mediaView.fitHeightProperty().bind(videoPane.heightProperty().subtract(28));

            sceneBadge = label("SCENE 1 OF 3", 9, FontWeight.BOLD, WHITE);
            sceneBadge.setPadding(new Insets(5, 10, 5, 10));
            sceneBadge.setStyle("-fx-background-color: rgba(0,0,0,0.62); -fx-background-radius: 999;");
            StackPane.setAlignment(sceneBadge, Pos.TOP_LEFT);
            StackPane.setMargin(sceneBadge, new Insets(14));

            fileLabel = label("DATABASE CLIP", 9, FontWeight.BOLD, Color.web(WHITE, 0.62));
            fileLabel.setId("fileLabel");

            Polygon triangle = new Polygon(0, 0, 46, 27, 0, 54);
            triangle.setFill(Color.web(LIME, 0.92));
            triangle.setEffect(new DropShadow(20, Color.web(LIME, 0.45)));

            emptyState = new VBox(10, triangle, fileLabel);
            emptyState.setAlignment(Pos.CENTER);

            Label preview = label("PREVIEW", 8, FontWeight.BOLD, Color.web(WHITE, 0.38));
            StackPane.setAlignment(preview, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(preview, new Insets(14));

            videoPane.getChildren().addAll(mediaView, emptyState, sceneBadge, preview);

            playBtn = new Button("PLAY");
            playBtn.setId("playBtn");
            playBtn.setStyle(limeButtonStyle());
            FontIcon playIcon = new FontIcon(Feather.PLAY);
            playIcon.setIconSize(12);
            playIcon.setIconColor(Color.web(BLACK));
            playBtn.setGraphic(playIcon);
            addHover(playBtn, limeButtonStyle(), limeHoverStyle());

            loopBtn = new ToggleButton("LOOP");
            loopBtn.setId("loopToggle");
            loopBtn.setStyle(ghostButtonStyle());
            loopBtn.selectedProperty().addListener((obs, old, on) ->
                loopBtn.setStyle(on ? ghostHoverStyle() : ghostButtonStyle()));

            moreBtn = new Button("MORE TIMELINE");
            moreBtn.setId("moreTimeline");
            moreBtn.setStyle(ghostButtonStyle());
            addHover(moreBtn, ghostButtonStyle(), ghostHoverStyle());

            sceneBtn = new Button("ANOTHER SCENE");
            sceneBtn.setId("anotherScene");
            sceneBtn.setStyle(ghostButtonStyle());
            addHover(sceneBtn, ghostButtonStyle(), ghostHoverStyle());

            FontIcon fileIcon = new FontIcon(Feather.FOLDER);
            fileIcon.setIconSize(12);
            fileIcon.setIconColor(Color.web(WHITE));
            fileBtn = new MenuButton("CHOOSE CLIP", fileIcon);
            fileBtn.setId("fileChoose");
            fileBtn.setStyle(violetButtonStyle());
            addHover(fileBtn, violetButtonStyle(), violetHoverStyle());

            for (AssetCatalog.Clip clip : AssetCatalog.bundledClips()) {
                MenuItem item = new MenuItem(clip.label());
                item.setOnAction(event -> loadBundledClip(clip));
                fileBtn.getItems().add(item);
            }
            fileBtn.getItems().add(new SeparatorMenuItem());
            MenuItem localFile = new MenuItem("From this computer...");
            localFile.setOnAction(event -> chooseFile());
            fileBtn.getItems().add(localFile);

            clipTime = label("0:00 / 0:03", 10, FontWeight.NORMAL, MUTED);
            clipTime.setId("clipTime");

            volume = new Slider(0, 100, 70);
            volume.setPrefWidth(80);
            volume.setId("volumeSlider");

            Region controlSpacer = new Region();
            HBox.setHgrow(controlSpacer, Priority.ALWAYS);

            HBox controls = new HBox(9, playBtn, loopBtn, moreBtn, sceneBtn, fileBtn,
                controlSpacer, clipTime, label("VOL", 8, FontWeight.BOLD, MUTED), volume);
            controls.setAlignment(Pos.CENTER_LEFT);

            FontIcon searchIcon = new FontIcon(Feather.SEARCH);
            searchIcon.setIconSize(13);
            searchIcon.setIconColor(Color.web(LIME));
            searchBtn = new Button("SEARCH FOR A MOVIE", searchIcon);
            searchBtn.setId("searchAnswer");
            searchBtn.setGraphicTextGap(9);
            searchBtn.setPrefWidth(340);
            searchBtn.setMaxWidth(Region.USE_PREF_SIZE);
            searchBtn.setStyle(searchButtonStyle());
            addHover(searchBtn, searchButtonStyle(), searchButtonHoverStyle());

            answerValue = label("-", 13, FontWeight.BOLD, WHITE);
            answerValue.setId("answerValue");

            FontIcon confirmIcon = new FontIcon(Feather.CHECK);
            confirmIcon.setIconSize(12);
            confirmIcon.setIconColor(Color.web(BLACK));
            confirmBtn = new Button("LOCK IN ANSWER", confirmIcon);
            confirmBtn.setId("confirmAnswer");
            confirmBtn.setStyle(limeButtonStyle());
            addHover(confirmBtn, limeButtonStyle(), limeHoverStyle());

            Region answerSpacer = new Region();
            HBox.setHgrow(answerSpacer, Priority.ALWAYS);
            answerRow = new HBox(10, label("YOUR ANSWER:", 9, FontWeight.BOLD, MUTED),
                answerValue, answerSpacer, confirmBtn);
            answerRow.setId("answerRow");
            answerRow.setAlignment(Pos.CENTER_LEFT);
            answerRow.setVisible(false);
            answerRow.setManaged(false);

            resultTitle = label("WATCH THE CLIP", 10, FontWeight.BLACK, SOFT_TEXT);
            resultTitle.setId("resultTitle");
            statusLabel = label("", 11, FontWeight.NORMAL, SOFT_TEXT);
            statusLabel.setId("statusLabel");
            statusLabel.setWrapText(true);

            nextBtn = new Button("NEXT ROUND");
            nextBtn.setId("nextRound");
            nextBtn.setStyle(limeButtonStyle());
            addHover(nextBtn, limeButtonStyle(), limeHoverStyle());
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);

            Region statusSpacer = new Region();
            HBox.setHgrow(statusSpacer, Priority.ALWAYS);
            VBox resultText = new VBox(2, resultTitle, statusLabel);
            resultBanner = new HBox(12, resultText, statusSpacer, nextBtn);
            resultBanner.setId("resultBanner");
            resultBanner.setAlignment(Pos.CENTER_LEFT);
            resultBanner.setPadding(new Insets(8, 14, 8, 14));
            resultBanner.setStyle(neutralBannerStyle());

            VBox answerPane = new VBox(6, searchBtn, answerRow);
            VBox body = new VBox(8, videoPane, timeline, controls, answerPane, resultBanner);
            body.setPadding(new Insets(0, 28, 14, 28));
            VBox.setVgrow(videoPane, Priority.ALWAYS);

            FontIcon backIcon = new FontIcon(Feather.ARROW_LEFT);
            backIcon.setIconSize(12);
            backIcon.setIconColor(Color.web(SOFT_TEXT));
            backBtn = new Button("BACK", backIcon);
            backBtn.setId("playBack");
            backBtn.setStyle(ghostButtonStyle());
            addHover(backBtn, ghostButtonStyle(), ghostHoverStyle());
            backBtn.setOnAction(event -> {
                if (backAction != null) {
                    backAction.run();
                }
            });

            VBox headerLeft = new VBox(1, roundLabel, scoreLabel);
            Region headSpacer1 = new Region();
            HBox.setHgrow(headSpacer1, Priority.ALWAYS);
            Region headSpacer2 = new Region();
            HBox.setHgrow(headSpacer2, Priority.ALWAYS);
            HBox header = new HBox(16, backBtn, headerLeft, headSpacer1, worthLabel,
                headSpacer2, timerLabel);
            header.setAlignment(Pos.CENTER);
            header.setPadding(new Insets(12, 28, 8, 28));

            BorderPane root = new BorderPane();
            root.setTop(header);
            root.setCenter(body);
            getChildren().addAll(root, buildSearchLayer());

            wireActions();
            startRound();
            startTicker();
        }

        void setBackAction(Runnable action) {
            backAction = action;
        }

        private StackPane buildSearchLayer() {
            FontIcon headerIcon = new FontIcon(Feather.SEARCH);
            headerIcon.setIconSize(12);
            headerIcon.setIconColor(Color.web(LIME));
            Label panelTitle = label("SEARCH MOVIES", 9, FontWeight.BOLD, SOFT_TEXT);
            Region headerGap = new Region();
            HBox.setHgrow(headerGap, Priority.ALWAYS);

            FontIcon closeIcon = new FontIcon(Feather.X);
            closeIcon.setIconSize(12);
            closeIcon.setIconColor(Color.web(SOFT_TEXT));
            Button searchClose = new Button("", closeIcon);
            searchClose.setId("searchClose");
            searchClose.setStyle(iconButtonStyle());
            addHover(searchClose, iconButtonStyle(), iconButtonHoverStyle());
            searchClose.setOnAction(event -> closeSearch());

            HBox panelHeader = new HBox(8, headerIcon, panelTitle, headerGap, searchClose);
            panelHeader.setAlignment(Pos.CENTER_LEFT);

            FontIcon fieldIcon = new FontIcon(Feather.SEARCH);
            fieldIcon.setIconSize(14);
            fieldIcon.setIconColor(Color.web(MUTED));

            searchField = new TextField();
            searchField.setId("searchField");
            searchField.setPromptText("Search movies by title...");
            searchField.setStyle(searchInputStyle());
            HBox.setHgrow(searchField, Priority.ALWAYS);

            FontIcon clearIcon = new FontIcon(Feather.X);
            clearIcon.setIconSize(11);
            clearIcon.setIconColor(Color.web(MUTED));
            Button searchClear = new Button("", clearIcon);
            searchClear.setId("searchClear");
            searchClear.setStyle(iconButtonStyle());
            addHover(searchClear, iconButtonStyle(), iconButtonHoverStyle());
            searchClear.setOnAction(event -> {
                searchField.clear();
                searchField.requestFocus();
            });
            searchClear.visibleProperty().bind(searchField.textProperty().isNotEmpty());
            searchClear.managedProperty().bind(searchClear.visibleProperty());

            HBox searchBox = new HBox(8, fieldIcon, searchField, searchClear);
            searchBox.setId("searchBox");
            searchBox.setAlignment(Pos.CENTER_LEFT);
            searchBox.setStyle(searchBoxStyle());

            searchInfo = label("TYPE A TITLE TO SEARCH", 8, FontWeight.BOLD, MUTED);
            searchInfo.setId("searchInfo");

            searchPlaceholder = label("Start typing to search " + TITLES.length + " movies",
                11, FontWeight.NORMAL, MUTED);
            FontIcon emptyIcon = new FontIcon(Feather.SEARCH);
            emptyIcon.setIconSize(20);
            emptyIcon.setIconColor(Color.web(MUTED));
            VBox emptyState = new VBox(8, emptyIcon, searchPlaceholder);
            emptyState.setAlignment(Pos.CENTER);

            searchResults = new ListView<>();
            searchResults.setId("searchResults");
            searchResults.setPlaceholder(emptyState);
            searchResults.setPrefHeight(80);
            searchResults.setMaxHeight(360);
            searchResults.setStyle("-fx-control-inner-background: #0f0d14;"
                + " -fx-background-color: #0f0d14; -fx-border-color: " + BORDER + ";"
                + " -fx-border-radius: 10; -fx-background-radius: 10;"
                + " -fx-padding: 2;");
            searchResults.setCellFactory(view -> new ListCell<>() {
                private final FontIcon filmIcon = new FontIcon(Feather.FILM);
                private final Label title = new Label();
                private final HBox row = new HBox(9, filmIcon, title);

                {
                    filmIcon.setIconSize(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8, 11, 8, 11));
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        return;
                    }
                    boolean lit = isSelected();
                    filmIcon.setIconColor(Color.web(lit ? LIME : MUTED));
                    title.setText(item);
                    title.setTextFill(Color.web(lit ? LIME : WHITE));
                    title.setStyle("-fx-font-family: '" + FONT + "'; -fx-font-size: 12px;"
                        + " -fx-font-weight: " + (lit ? "bold" : "normal") + ";");
                    row.setStyle("-fx-background-color: "
                        + (lit ? "#241d33" : "transparent")
                        + "; -fx-background-radius: 8;");
                    setGraphic(row);
                }

                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    updateItem(getItem(), getItem() == null);
                }
            });

            HBox hints = new HBox(14, searchHint("ENTER", "select"),
                searchHint("UP / DOWN", "browse"), searchHint("ESC", "close"));
            hints.setId("searchHints");
            hints.setAlignment(Pos.CENTER_LEFT);

            VBox panel = new VBox(9, panelHeader, searchBox, searchInfo, searchResults, hints);
            panel.setId("searchPanel");
            panel.setPadding(new Insets(14));
            panel.setPrefWidth(500);
            panel.setMaxWidth(500);
            panel.setMaxHeight(Region.USE_PREF_SIZE);
            panel.setStyle("-fx-background-color: #17141f; -fx-background-radius: 16;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 16;"
                + " -fx-border-width: 1;");
            StackPane.setAlignment(panel, Pos.TOP_CENTER);
            StackPane.setMargin(panel, new Insets(40, 0, 0, 0));

            searchLayer = new StackPane(panel);
            searchLayer.setId("searchLayer");
            searchLayer.setStyle("-fx-background-color: rgba(10,11,16,0.72);");
            searchLayer.setVisible(false);
            searchLayer.setOnMouseClicked(event -> {
                if (event.getTarget() == searchLayer) {
                    closeSearch();
                }
            });

            searchField.textProperty().addListener((obs, old, value) -> refreshSearchResults());
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    closeSearch();
                } else if (event.getCode() == KeyCode.ENTER) {
                    String pick = searchResults.getSelectionModel().getSelectedItem();
                    if (pick == null && !searchResults.getItems().isEmpty()) {
                        pick = searchResults.getItems().get(0);
                    }
                    if (pick != null) {
                        pickAnswer(pick);
                    }
                } else if ((event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP)
                        && !searchResults.getItems().isEmpty()) {
                    searchResults.requestFocus();
                    searchResults.getSelectionModel().select(0);
                }
            });
            searchResults.setOnKeyPressed(event -> {
                String pick = searchResults.getSelectionModel().getSelectedItem();
                if (event.getCode() == KeyCode.ENTER && pick != null) {
                    pickAnswer(pick);
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    closeSearch();
                }
            });
            searchResults.setOnMouseClicked(event -> {
                String pick = searchResults.getSelectionModel().getSelectedItem();
                if (pick != null) {
                    pickAnswer(pick);
                }
            });
            return searchLayer;
        }

        private void openSearch() {
            if (answered) {
                return;
            }
            searchField.clear();
            refreshSearchResults();
            searchLayer.setVisible(true);
            searchField.requestFocus();
        }

        private void closeSearch() {
            searchLayer.setVisible(false);
        }

        private void refreshSearchResults() {
            String raw = searchField.getText() == null ? "" : searchField.getText().trim();
            String query = raw.toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            if (!query.isEmpty()) {
                for (String title : TITLES) {
                    if (title.toLowerCase(Locale.ROOT).contains(query)) {
                        matches.add(title);
                    }
                    if (matches.size() >= 8) {
                        break;
                    }
                }
            }

            if (query.isEmpty()) {
                searchInfo.setText("TYPE A TITLE TO SEARCH");
                searchPlaceholder.setText("Start typing to search " + TITLES.length + " movies");
            } else if (matches.isEmpty()) {
                searchInfo.setText("NO MATCHES");
                searchPlaceholder.setText("No movie matches \"" + raw + "\"");
            } else {
                searchInfo.setText(matches.size() + (matches.size() == 1 ? " MATCH" : " MATCHES"));
            }

            searchResults.getItems().setAll(matches);
            if (matches.isEmpty()) {
                searchResults.getSelectionModel().clearSelection();
            } else {
                // The first match is pre-selected, so ENTER always does something logical.
                searchResults.getSelectionModel().select(0);
                searchResults.scrollTo(0);
            }
            searchResults.setPrefHeight(matches.isEmpty()
                ? 80 : Math.min(8, matches.size()) * 42 + 10);
        }

        /** Search mode: remember the pick; the round only ends on lock-in. */
        private void pickAnswer(String title) {
            if (answered) {
                return;
            }
            pickedTitle = title;
            answerValue.setText(title);
            answerRow.setVisible(true);
            answerRow.setManaged(true);
            searchBtn.setText("CHANGE ANSWER");
            closeSearch();
        }

        private void wireActions() {
            playBtn.setOnAction(event -> togglePlayback());
            searchBtn.setOnAction(event -> openSearch());
            confirmBtn.setOnAction(event -> {
                if (pickedTitle != null) {
                    answer(pickedTitle);
                }
            });
            loopBtn.setOnAction(event -> {
                if (player != null) {
                    player.setCycleCount(loopBtn.isSelected()
                        ? MediaPlayer.INDEFINITE : 1);
                }
                statusLabel.setTextFill(Color.web(SOFT_TEXT));
                statusLabel.setText(loopBtn.isSelected()
                    ? "Loop is on - the clip repeats." : "Loop is off.");
            });
            volume.valueProperty().addListener((obs, old, value) -> {
                if (player != null) {
                    player.setVolume(value.doubleValue() / 100.0);
                }
            });
            timeline.onSeekStart = () -> {
                if (player == null) {
                    return;
                }
                seeking = true;
                resumeAfterSeek = playing;
                if (playing) {
                    player.pause();
                }
            };
            timeline.onSeek = ms -> {
                if (player == null) {
                    return;
                }
                player.seek(Duration.millis(ms));
                playing = false;
                playBtn.setText("PLAY");
                clipTime.setText(clockDuration(Duration.millis(ms)) + " / "
                    + clockDuration(windowDuration(player)));
            };
            timeline.onSeekEnd = () -> {
                seeking = false;
                if (player != null && resumeAfterSeek) {
                    player.play();
                    playing = true;
                    playBtn.setText("PAUSE");
                    statusLabel.setTextFill(Color.web(SOFT_TEXT));
                    statusLabel.setText("Clip playing - watch the scene and name the movie.");
                }
                resumeAfterSeek = false;
            };
            moreBtn.setOnAction(event -> moreTimeline());
            sceneBtn.setOnAction(event -> anotherScene());
            nextBtn.setOnAction(event -> nextRound());
        }

        private void togglePlayback() {
            if (answered) {
                return;
            }
            if (player == null) {
                loadBundledClip(AssetCatalog.bundledClips().get(0));
                return;
            }
            if (playing) {
                player.pause();
                playing = false;
                playBtn.setText("PLAY");
                statusLabel.setTextFill(Color.web(SOFT_TEXT));
                statusLabel.setText("Clip paused - replay it as often as you need.");
            } else {
                Duration now = player.getCurrentTime();
                if (now != null && now.toMillis() >= timeline.windowEnd() - 80) {
                    player.seek(Duration.ZERO);
                }
                player.play();
                playing = true;
                playBtn.setText("PAUSE");
                statusLabel.setTextFill(Color.web(SOFT_TEXT));
                statusLabel.setText("Clip playing - watch the scene and name the movie.");
            }
        }

        private void startTicker() {
            ticker = new Timeline(new KeyFrame(Duration.seconds(1), event -> tick()));
            ticker.setCycleCount(Animation.INDEFINITE);
            ticker.play();
        }

        private void tick() {
            if (answered) {
                return;
            }
            if (remaining <= 0) {
                answer(null);
                return;
            }
            remaining--;
            timerLabel.setText(clock(remaining));
            if (remaining <= 10) {
                timerLabel.setTextFill(Color.web("#ff8a7a"));
            }
            if (remaining <= 0) {
                answer(null);
            }
        }

        private void startRound() {
            stopPlayback();
            answered = false;
            playing = false;
            Arrays.fill(stage, 1);
            Arrays.fill(seen, false);
            seen[0] = true;
            sceneIdx = 1;
            remaining = 60;

            timeline.setTotal(10_000);
            timeline.setPosition(0);

            roundLabel.setText("ROUND " + round + " OF 5");
            scoreLabel.setText("SCORE " + String.format("%,d", score));
            timerLabel.setText(clock(remaining));
            timerLabel.setTextFill(Color.web(WHITE));
            nextBtn.setVisible(false);
            nextBtn.setManaged(false);
            nextBtn.setText(round >= 5 ? "PLAY AGAIN" : "NEXT ROUND");
            resultTitle.setText("WATCH THE CLIP");
            resultTitle.setTextFill(Color.web(SOFT_TEXT));
            resultBanner.setStyle(neutralBannerStyle());
            statusLabel.setTextFill(Color.web(SOFT_TEXT));
            statusLabel.setText("Press PLAY for the bundled demo scene, or choose another clip.");
            clipTime.setText("0:00 / 0:00");
            fileLabel.setText("NO CLIP LOADED");

            pickedTitle = null;
            answerValue.setText("-");
            answerRow.setVisible(false);
            answerRow.setManaged(false);
            searchBtn.setText("SEARCH FOR A MOVIE");
            searchBtn.setDisable(false);
            confirmBtn.setDisable(false);
            searchField.clear();
            searchResults.getItems().clear();
            searchLayer.setVisible(false);

            refreshScene();
            refreshAnswer();
            refreshTimeline();
        }

        private void refreshAnswer() {
            correctTitle = TITLES[random.nextInt(TITLES.length)];
        }

        private void refreshScene() {
            sceneBadge.setText("SCENE " + sceneIdx + " OF " + SCENES);
            videoPane.setStyle("-fx-background-color: "
                + SCENE_GRADIENTS[(sceneIdx - 1) % SCENE_GRADIENTS.length] + ";"
                + " -fx-background-radius: 18; -fx-border-color: #3a2b66;"
                + " -fx-border-radius: 18; -fx-border-width: 1;");
        }

        private void refreshTimeline() {
            int level = stageLevel();
            timeline.setUnlocked(level);
            moreBtn.setText(level >= 3
                ? "FULL TIMELINE" : "MORE TIMELINE (" + (level == 1 ? "5S" : "FULL") + ")");
            moreBtn.setDisable(answered || level >= 3);
            worthLabel.setText(answered ? "ROUND OVER"
                : "WORTH " + String.format("%,d", points()) + " PTS");
            boolean freshScene = false;
            for (boolean wasSeen : seen) {
                if (!wasSeen) {
                    freshScene = true;
                    break;
                }
            }
            sceneBtn.setText(freshScene
                ? "ANOTHER SCENE (-" + SCENE_PENALTY + ")" : "NEXT SCENE");
        }

        /** Footage stage of the scene currently on screen. */
        private int stageLevel() {
            return stage[sceneIdx - 1];
        }

        /** Best footage unlocked across all scenes this round. */
        private int maxStage() {
            int max = 1;
            for (int value : stage) {
                max = Math.max(max, value);
            }
            return max;
        }

        /** A correct answer keeps the 5,000-point base minus unlocked footage and new scenes. */
        private int points() {
            int base = STAGE_POINTS[maxStage() - 1];
            int opened = 0;
            for (boolean wasSeen : seen) {
                if (wasSeen) {
                    opened++;
                }
            }
            int penalty = SCENE_PENALTY * Math.max(0, opened - 1);
            return Math.max(MIN_CORRECT_POINTS, base - penalty);
        }

        /** Length the clock panel reports: the current window, not the whole file. */
        private Duration windowDuration(MediaPlayer source) {
            if (stageLevel() >= 3) {
                Duration total = source.getTotalDuration();
                return total == null ? Duration.ZERO : total;
            }
            return Duration.millis(timeline.windowEnd());
        }

        /** Applies the selected 3s / 5s / full-clip window to the live player. */
        private void applyStageLimit() {
            if (player == null) {
                return;
            }
            if (stageLevel() >= 3) {
                player.setStopTime(Duration.INDEFINITE);
                return;
            }
            Duration total = player.getTotalDuration();
            if (total == null || total.isUnknown()) {
                return; // applied again when the media reports READY
            }
            player.setStopTime(Duration.millis(timeline.windowEnd()));
        }

        private void moreTimeline() {
            if (answered || stageLevel() >= 3) {
                return;
            }
            long oldEnd = timeline.windowEnd();
            boolean parkedAtEnd = player != null && !playing
                && player.getCurrentTime().toMillis() >= oldEnd - 120;

            stage[sceneIdx - 1]++;
            refreshTimeline();
            applyStageLimit();

            if (player != null) {
                if (parkedAtEnd) {
                    // Carry on into the newly lit part instead of starting over.
                    player.seek(Duration.millis(oldEnd));
                    player.play();
                    playing = true;
                    playBtn.setText("PAUSE");
                }
                clipTime.setText(clockDuration(player.getCurrentTime()) + " / "
                    + clockDuration(windowDuration(player)));
            }

            statusLabel.setTextFill(Color.web(SOFT_TEXT));
            statusLabel.setText((stageLevel() == 2
                ? "Five-second window unlocked" : "Full clip unlocked")
                + " - a correct answer is now worth "
                + String.format("%,d", points()) + " points.");
        }

        private void anotherScene() {
            if (answered) {
                return;
            }
            sceneIdx = sceneIdx % SCENES + 1;
            seen[sceneIdx - 1] = true;
            stopPlayback();
            refreshScene();
            timeline.setTotal(10_000);
            timeline.setPosition(0);
            clipTime.setText("0:00 / 0:00");
            fileLabel.setText("NO CLIP LOADED");
            refreshTimeline();
            statusLabel.setTextFill(Color.web(SOFT_TEXT));
            if (stageLevel() == 1) {
                statusLabel.setText("Scene " + sceneIdx + " of " + SCENES
                    + " - every scene starts with its own 3-second timeline. "
                    + "Each new scene costs " + SCENE_PENALTY + " points.");
            } else {
                statusLabel.setText("Scene " + sceneIdx + " of " + SCENES
                    + " - this scene's timeline is still unlocked.");
            }
        }

        private void chooseFile() {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a movie clip");
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video files",
                    "*.mp4", "*.mkv", "*.avi", "*.mov", "*.webm", "*.m4v"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
            File start = AssetCatalog.clipsDirectory();
            if (start == null) {
                start = new File(System.getProperty("user.home", "."));
            }
            if (start.isDirectory()) {
                chooser.setInitialDirectory(start);
            }
            File picked = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
            if (picked != null) {
                fileLabel.setText(picked.getName().toUpperCase(Locale.ROOT));
                sceneBadge.setText("LOCAL FILE");
                playClip(picked.toURI().toString(), picked.getName());
            }
        }

        /** Loads and plays one of the scenes that ships in the project's asset package. */
        private void loadBundledClip(AssetCatalog.Clip clip) {
            URL url = AssetCatalog.clip(clip.relativePath());
            if (url == null) {
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("Bundled clip missing: " + clip.relativePath() + ".");
                return;
            }
            fileLabel.setText(clip.label().toUpperCase(Locale.ROOT));
            sceneBadge.setText("BUNDLED CLIP");
            playClip(url.toExternalForm(), clip.label());
        }

        /** Starts a real JavaFX media playback in the clip stage. */
        private void playClip(String source, String label) {
            stopPlayback();
            try {
                Media media = new Media(source);
                MediaPlayer created = new MediaPlayer(media);
                player = created;
                created.setVolume(volume.getValue() / 100.0);
                created.setCycleCount(loopBtn.isSelected() ? MediaPlayer.INDEFINITE : 1);
                created.setAutoPlay(true);

                mediaView.setMediaPlayer(created);
                mediaView.setVisible(true);
                emptyState.setVisible(false);
                emptyState.setManaged(false);
                playBtn.setText("PAUSE");
                playing = true;
                clipTime.setText("0:00 / 0:00");
                statusLabel.setTextFill(Color.web(SOFT_TEXT));
                statusLabel.setText("Loading " + label + "...");

                created.setOnReady(() -> {
                    timeline.setTotal(Math.round(created.getTotalDuration().toMillis()));
                    timeline.setPosition(0);
                    applyStageLimit();
                    clipTime.setText("0:00 / " + clockDuration(windowDuration(created)));
                    statusLabel.setTextFill(Color.web(SOFT_TEXT));
                    statusLabel.setText("Playing " + label + " - name the movie.");
                });
                created.currentTimeProperty().addListener((obs, old, time) -> {
                    if (!seeking && time != null) {
                        timeline.setPosition(Math.min((long) time.toMillis(), timeline.windowEnd()));
                    }
                    clipTime.setText(clockDuration(time) + " / "
                        + clockDuration(windowDuration(created)));
                });
                created.setOnEndOfMedia(() -> {
                    if (loopBtn.isSelected()) {
                        return;
                    }
                    playing = false;
                    playBtn.setText("PLAY");
                    created.pause();
                    timeline.setPosition(timeline.windowEnd());
                    statusLabel.setTextFill(Color.web(SOFT_TEXT));
                    if (stageLevel() < 3) {
                        long seconds = Math.max(1, Math.round(timeline.windowEnd() / 1000.0));
                        statusLabel.setText("That is the whole " + seconds
                            + "-second window - press MORE TIMELINE for more footage.");
                    } else {
                        statusLabel.setText("Clip finished - replay it or lock in your answer.");
                    }
                });
                created.setOnError(() -> showPlaybackError(created.getError()));
                media.setOnError(() -> showPlaybackError(media.getError()));
            } catch (RuntimeException error) {
                showPlaybackError(error);
            }
        }

        private void stopPlayback() {
            if (player != null) {
                player.stop();
                player.dispose();
                player = null;
            }
            mediaView.setMediaPlayer(null);
            mediaView.setVisible(false);
            timeline.setPosition(0);
            seeking = false;
            resumeAfterSeek = false;
            if (emptyState != null) {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
            }
            playing = false;
            if (playBtn != null) {
                playBtn.setText("PLAY");
            }
        }

        private void showPlaybackError(Throwable error) {
            stopPlayback();
            fileLabel.setText("CLIP UNAVAILABLE");
            resultTitle.setText("CLIP ERROR");
            resultTitle.setTextFill(Color.web(RED));
            statusLabel.setTextFill(Color.web(RED));
            String detail = error == null || error.getMessage() == null
                ? "" : " " + error.getMessage();
            statusLabel.setText("This clip could not be played." + detail);
        }

        private static String clockDuration(Duration duration) {
            if (duration == null || duration.isUnknown()) {
                return "0:00";
            }
            int seconds = (int) Math.round(duration.toSeconds());
            return String.format("%d:%02d", seconds / 60, seconds % 60);
        }

        private void answer(String picked) {
            if (answered) {
                return;
            }
            answered = true;
            playing = false;
            if (player != null) {
                player.pause();
            }
            searchBtn.setDisable(true);
            confirmBtn.setDisable(true);
            closeSearch();

            boolean correct = picked != null && picked.equals(correctTitle);

            String result;
            if (picked == null) {
                result = "TIME'S UP";
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("Time's up - it was " + correctTitle + ".");
            } else if (correct) {
                score += points();
                scoreLabel.setText("SCORE " + String.format("%,d", score));
                result = "CORRECT";
                statusLabel.setTextFill(Color.web(GREEN));
                statusLabel.setText("Correct - +" + String.format("%,d", points())
                    + " points. Nice one.");
            } else {
                result = "WRONG";
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("Not quite - it was " + correctTitle + ".");
            }

            if (round >= 5) {
                result = result + " - MATCH OVER";
                statusLabel.setText(statusLabel.getText() + " Final score "
                    + String.format("%,d", score) + ".");
            }

            resultTitle.setText(result);
            resultTitle.setTextFill(Color.web(correct ? GREEN : RED));
            resultBanner.setStyle(correct ? correctBannerStyle() : wrongBannerStyle());
            nextBtn.setText(round >= 5 ? "PLAY AGAIN" : "NEXT ROUND");
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);
            refreshTimeline();
            worthLabel.setText("ROUND OVER");
        }

        private void nextRound() {
            round++;
            if (round > 5) {
                round = 1;
                score = 0;
            }
            startRound();
        }

        void dispose() {
            if (ticker != null) {
                ticker.stop();
            }
            stopPlayback();
        }

        private static String clock(int seconds) {
            return String.format("%d:%02d", Math.max(0, seconds) / 60, Math.max(0, seconds) % 60);
        }

        private static String searchButtonStyle() {
            return "-fx-background-color: #17141f; -fx-text-fill: " + LIME + ";"
                + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px;"
                + " -fx-font-weight: bold; -fx-alignment: center-left;"
                + " -fx-background-radius: 12; -fx-border-color: " + BORDER + ";"
                + " -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 11 16 11 16;"
                + " -fx-cursor: hand;";
        }

        private static String searchButtonHoverStyle() {
            return "-fx-background-color: #211c2d; -fx-text-fill: #c9f26a;"
                + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px;"
                + " -fx-font-weight: bold; -fx-alignment: center-left;"
                + " -fx-background-radius: 12; -fx-border-color: rgba(183,227,78,0.5);"
                + " -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 11 16 11 16;"
                + " -fx-cursor: hand;";
        }

        private static String searchInputStyle() {
            return "-fx-background-color: transparent; -fx-text-fill: " + WHITE + ";"
                + " -fx-prompt-text-fill: " + MUTED + ";"
                + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px;"
                + " -fx-border-color: transparent; -fx-padding: 0;";
        }

        private static String searchBoxStyle() {
            return "-fx-background-color: #0f0d14; -fx-background-radius: 10;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 10;"
                + " -fx-border-width: 1; -fx-padding: 9 12 9 12;";
        }

        private static String iconButtonStyle() {
            return "-fx-background-color: transparent; -fx-background-radius: 6;"
                + " -fx-padding: 3 4 3 4; -fx-cursor: hand;";
        }

        private static String iconButtonHoverStyle() {
            return "-fx-background-color: #2a2436; -fx-background-radius: 6;"
                + " -fx-padding: 3 4 3 4; -fx-cursor: hand;";
        }

        private static HBox searchHint(String key, String action) {
            Label cap = label(key, 8, FontWeight.BOLD, SOFT_TEXT);
            cap.setStyle("-fx-background-color: #211c2d; -fx-background-radius: 5;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 5;"
                + " -fx-padding: 2 6 2 6;");
            HBox hint = new HBox(5, cap, label(action, 8, FontWeight.NORMAL, MUTED));
            hint.setAlignment(Pos.CENTER_LEFT);
            return hint;
        }

        private static String neutralBannerStyle() {
            return bannerStyle("#17141f", BORDER);
        }

        private static String correctBannerStyle() {
            return bannerStyle("rgba(62,207,106,0.12)", "rgba(62,207,106,0.45)");
        }

        private static String wrongBannerStyle() {
            return bannerStyle("rgba(229,72,77,0.12)", "rgba(229,72,77,0.45)");
        }

        private static String bannerStyle(String background, String border) {
            return "-fx-background-color: " + background + "; -fx-background-radius: 12;"
                + " -fx-border-color: " + border + "; -fx-border-radius: 12;"
                + " -fx-border-width: 1;";
        }
    }

    /** One tutorial stop: a target control plus the explanation card text. */
    static record TourStep(Node target, String title, String body) { }

    // ------------------------------------------------------ tutorial overlay

    static final class TutorialOverlay extends StackPane {

        private final List<TourStep> steps;
        private final Runnable onFinish;

        private final VBox card;
        private final Label stepLabel;
        private final Label titleLabel;
        private final Label bodyLabel;
        private final Button backBtn;
        private final Button nextBtn;
        private final HBox dots;
        private final Rectangle ring = new Rectangle();
        private final Polygon caret = new Polygon();

        private Node highlighted;
        private Effect previousEffect;
        private FadeTransition pulse;
        private int index = -1;

        TutorialOverlay(List<TourStep> steps, Runnable onFinish) {
            this.steps = steps;
            this.onFinish = onFinish;

            setId("tutorialRoot");
            setPickOnBounds(true);

            Region scrim = new Region();
            scrim.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            scrim.setStyle("-fx-background-color: rgba(0,0,0,0.60);");

            ring.setFill(Color.TRANSPARENT);
            ring.setId("tutRing");
            ring.setStroke(Color.web(LIME));
            ring.setStrokeWidth(2.5);
            ring.setArcWidth(16);
            ring.setArcHeight(16);
            ring.setEffect(new DropShadow(22, Color.web(LIME, 0.55)));
            ring.setManaged(false);
            ring.setMouseTransparent(true);

            caret.setFill(Color.web(GRAPHITE));
            caret.setId("tutCaret");
            caret.setStroke(Color.web(LIME));
            caret.setStrokeWidth(1.2);
            caret.setManaged(false);
            caret.setMouseTransparent(true);

            stepLabel = label("STEP 1 OF " + steps.size(), 9, FontWeight.BOLD, LIME);
            stepLabel.setId("tutStepLabel");

            Button skip = new Button("SKIP TOUR");
            skip.setId("tutSkip");
            skip.setStyle(ghostButtonStyle());
            skip.setOnAction(event -> finish());

            Region topSpacer = new Region();
            HBox.setHgrow(topSpacer, Priority.ALWAYS);
            HBox top = new HBox(10, stepLabel, topSpacer, skip);
            top.setAlignment(Pos.CENTER_LEFT);

            titleLabel = label("", 17, FontWeight.BLACK, WHITE);
            titleLabel.setId("tutTitle");
            titleLabel.setWrapText(true);

            bodyLabel = label("", 12, FontWeight.NORMAL, SOFT_TEXT);
            bodyLabel.setId("tutBody");
            bodyLabel.setWrapText(true);

            dots = new HBox(5);
            for (int i = 0; i < steps.size(); i++) {
                Circle dot = new Circle(3, Color.web("#3a3547"));
                dots.getChildren().add(dot);
            }

            backBtn = new Button("BACK");
            backBtn.setId("tutBack");
            backBtn.setStyle(ghostButtonStyle());
            backBtn.setOnAction(event -> {
                if (index > 0) {
                    showStep(index - 1);
                }
            });

            nextBtn = new Button("NEXT");
            nextBtn.setId("tutNext");
            nextBtn.setStyle(limeButtonStyle());
            addHover(nextBtn, limeButtonStyle(), limeHoverStyle());
            nextBtn.setOnAction(event -> {
                if (index >= steps.size() - 1) {
                    finish();
                } else {
                    showStep(index + 1);
                }
            });

            Region navSpacer = new Region();
            HBox.setHgrow(navSpacer, Priority.ALWAYS);
            HBox nav = new HBox(8, backBtn, navSpacer, nextBtn);
            nav.setAlignment(Pos.CENTER_LEFT);

            card = new VBox(10, top, titleLabel, bodyLabel, dots, nav);
            card.setId("tutCard");
            card.setManaged(false);
            card.setPrefWidth(340);
            card.setMaxWidth(340);
            card.setPadding(new Insets(16, 18, 16, 18));
            card.setStyle("-fx-background-color: " + GRAPHITE + ";"
                + " -fx-background-radius: 16; -fx-border-color: #4a2f8a;"
                + " -fx-border-radius: 16; -fx-border-width: 1;");
            card.setEffect(new DropShadow(30, Color.web(BLACK, 0.65)));

            getChildren().addAll(scrim, ring, caret, card);

            widthProperty().addListener((obs, old, value) -> refresh());
            heightProperty().addListener((obs, old, value) -> refresh());

            showStep(0);
        }

        private void showStep(int next) {
            index = Math.max(0, Math.min(next, steps.size() - 1));
            TourStep step = steps.get(index);

            stepLabel.setText("STEP " + (index + 1) + " OF " + steps.size());
            titleLabel.setText(step.title());
            bodyLabel.setText(step.body());
            backBtn.setDisable(index == 0);
            nextBtn.setText(index == steps.size() - 1 ? "DONE" : "NEXT");

            for (int i = 0; i < dots.getChildren().size(); i++) {
                Circle dot = (Circle) dots.getChildren().get(i);
                dot.setFill(Color.web(i == index ? LIME : (i < index ? "#6a42b8" : "#3a3547")));
            }

            if (highlighted != null) {
                highlighted.setEffect(previousEffect);
            }
            highlighted = step.target();
            previousEffect = highlighted.getEffect();
            highlighted.setEffect(new DropShadow(22, Color.web(LIME, 0.6)));

            card.applyCss();
            autosizeCard();

            refresh();

            card.setOpacity(0);
            card.setTranslateY(8);
            Timeline entry = new Timeline(new KeyFrame(Duration.millis(170),
                new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_OUT)));
            entry.play();
        }

        private void autosizeCard() {
            card.autosize();
            if (card.getWidth() <= 0) {
                card.resize(card.prefWidth(-1), card.prefHeight(card.prefWidth(-1)));
                card.layout();
            }
        }

        /** Positions the highlight ring, the card and its arrow for the current step. */
        void refresh() {
            if (index < 0 || index >= steps.size() || getScene() == null) {
                return;
            }
            Node target = steps.get(index).target();
            if (target == null || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            Bounds bounds = sceneToLocal(target.localToScene(target.getLayoutBounds()));
            if (bounds.getWidth() <= 0) {
                Platform.runLater(this::refresh);
                return;
            }

            double pad = 7;
            ring.setX(bounds.getMinX() - pad);
            ring.setY(bounds.getMinY() - pad);
            ring.setWidth(bounds.getWidth() + pad * 2);
            ring.setHeight(bounds.getHeight() + pad * 2);

            autosizeCard();
            double cardWidth = card.getWidth();
            double cardHeight = card.getHeight();
            if (cardWidth <= 0 || cardHeight <= 0) {
                return;
            }

            double gap = 26;
            boolean cardOnRight = bounds.getCenterX() < getWidth() / 2
                && bounds.getMaxX() + gap + cardWidth < getWidth() - 18;
            double x = cardOnRight ? bounds.getMaxX() + gap
                : bounds.getMinX() - gap - cardWidth;
            if (x < 18) {
                x = bounds.getMaxX() + gap;
            }
            x = Math.max(18, Math.min(x, Math.max(18, getWidth() - cardWidth - 18)));

            double y = bounds.getCenterY() - cardHeight / 2;
            y = Math.max(18, Math.min(y, Math.max(18, getHeight() - cardHeight - 18)));
            card.relocate(x, y);

            double arrowY = Math.max(y + 16,
                Math.min(bounds.getCenterY() - 9, y + cardHeight - 34));
            boolean cardRightOfTarget = x > bounds.getCenterX();
            if (cardRightOfTarget) {
                caret.getPoints().setAll(12.0, 0.0, 0.0, 9.0, 12.0, 18.0);
                caret.relocate(x - 12, arrowY);
            } else {
                caret.getPoints().setAll(0.0, 0.0, 12.0, 9.0, 0.0, 18.0);
                caret.relocate(x + cardWidth - 1, arrowY);
            }

            if (pulse != null) {
                pulse.stop();
            }
            pulse = new FadeTransition(Duration.millis(820), ring);
            pulse.setFromValue(0.45);
            pulse.setToValue(1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
        }

        private void finish() {
            if (pulse != null) {
                pulse.stop();
            }
            if (highlighted != null) {
                highlighted.setEffect(previousEffect);
            }
            if (getParent() instanceof Pane parent) {
                parent.getChildren().remove(this);
            }
            if (onFinish != null) {
                onFinish.run();
            }
        }
    }
}
