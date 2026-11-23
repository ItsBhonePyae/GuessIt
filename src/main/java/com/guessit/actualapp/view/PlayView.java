package com.guessit.actualapp.view;

import com.guessit.actualapp.model.AppSettings;
import com.guessit.actualapp.model.GameHistory;
import com.guessit.actualapp.model.Movie;
import com.guessit.actualapp.util.MovieCatalog;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The playing page: a closed clapperboard over a matte slate holding the clip,
 * the segmented film-strip timeline, playback controls and the answer search.
 * Layout follows the Movie_Playing_Demo_Page reference; colours are muted.
 */
public class PlayView extends StackPane {

    private static final int ROUNDS = 5;
    private static final int ROUND_SECONDS = 60;
    private static final int SCENES = 3;
    private static final int[] STAGE_POINTS = {5_000, 3_500, 2_000};
    private static final int SCENE_PENALTY = 150;
    private static final int MIN_CORRECT_POINTS = 50;

    private static final String GREEN = "#8fae7d";
    private static final String WARN = "#c46a62";
    private static final String LIME = Theme.LIME;
    private static final String WHITE = Theme.WHITE;
    private static final String MUTED = Theme.MUTED;
    private static final String SOFT_TEXT = Theme.SOFT_TEXT;
    private static final String PAPER = "#252a30";

    private static final String[] SCENE_PLATES = {
        "#221e19",
        "#242018",
        "#1d1b19"
    };

    private final List<Movie> movies = MovieCatalog.getAllMovies();
    private final List<String> titles = new ArrayList<>();

    /* ------------------------------------------------------------- controls */
    private final Label roundLabel = Theme.italic("TAKE 1 OF 5", 15, WHITE);
    private final Label scoreLabel = Theme.mono("ROLL 0", 9, FontWeight.BOLD, MUTED);
    private final Label worthLabel = Theme.label("WORTH 5,000 PTS", 10, FontWeight.BOLD, LIME);
    private final Label timerLabel = Theme.mono("1:00", 26, FontWeight.BOLD, WHITE);
    private final StackPane videoPane = new StackPane();
    private final ProgressIndicator loadingIndicator = new ProgressIndicator();
    private final Label sceneBadge = Theme.mono("SCENE 1 OF 3", 9, FontWeight.BOLD, WHITE);
    private final Label fileLabel = Theme.label("NO CLIP LOADED", 9, FontWeight.BOLD,
        "#a39a8b");
    private final Label statusLabel = Theme.label("", 11, FontWeight.NORMAL, SOFT_TEXT);
    private final Label clipTime = Theme.label("0:00 / 0:00", 10, FontWeight.NORMAL, MUTED);
    private final Button playBtn = new Button("PLAY");
    private final ToggleButton loopBtn = new ToggleButton("LOOP");
    private final Button moreBtn = new Button("MORE TIMELINE");
    private final Button sceneBtn = new Button("ANOTHER SCENE");
    private final MenuButton fileBtn = new MenuButton("CHOOSE CLIP");
    private final Button nextBtn = new Button("NEXT ROUND");
    private final Button searchBtn = new Button("SEARCH FOR A MOVIE");
    private final Label answerValue = Theme.label("-", 13, FontWeight.BOLD, WHITE);
    private final Button confirmBtn = new Button("LOCK IN ANSWER");
    private final HBox answerRow;
    private final Label resultTitle = Theme.label("WATCH THE CLIP", 10, FontWeight.BLACK, SOFT_TEXT);
    private final HBox resultBanner;
    private final Slider volume = new Slider(0, 100, AppSettings.getVideoVolume() * 100);
    private final DemoTimeline timeline = new DemoTimeline();
    private final MediaView mediaView = new MediaView();
    private final VBox emptyState;

    private StackPane searchLayer;
    private TextField searchField;
    private ListView<String> searchResults;
    private Label searchInfo;
    private Label searchPlaceholder;

    /* -------------------------------------------------------------- playback */
    private final Map<String, MediaPlayer> mediaCache = new LinkedHashMap<>();
    private static final int MAX_CACHED_PLAYERS = 1;
    private static final ScheduledExecutorService MEDIA_CLEANER =
        Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "guessit-media-cleaner");
            thread.setDaemon(true);
            return thread;
        });
    private static ScheduledFuture<?> pendingMediaCleanup;
    private MediaPlayer player;
    private Timeline ticker;
    private final Random random = new Random();
    private int remaining = ROUND_SECONDS;
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
    private boolean tutorialActive;
    private long appliedStopMs = Long.MIN_VALUE;

    public PlayView() {
        this(false);
    }

    public PlayView(boolean showTutorial) {
        tutorialActive = showTutorial;
        setStyle("-fx-background-color: " + Theme.INK + ";");

        for (Movie movie : movies) {
            titles.add(movie.getTitle());
        }

        roundLabel.setId("roundLabel");
        scoreLabel.setId("scoreLabel");
        worthLabel.setId("worthLabel");
        timerLabel.setId("timerLabel");

        videoPane.setId("videoPane");
        videoPane.setMinHeight(240);
        videoPane.setStyle(screenStyle(SCENE_PLATES[0]));

        mediaView.setVisible(false);
        mediaView.setSmooth(true);
        mediaView.setPreserveRatio(true);
        mediaView.fitWidthProperty().bind(videoPane.widthProperty().subtract(24));
        mediaView.fitHeightProperty().bind(videoPane.heightProperty().subtract(24));

        sceneBadge.setPadding(new Insets(4, 9, 4, 8));
        sceneBadge.setStyle("-fx-background-color: rgba(18,16,15,0.82);"
            + " -fx-background-radius: 2;"
            + " -fx-border-color: transparent transparent transparent " + Theme.RED + ";"
            + " -fx-border-width: 0 0 0 3;");
        StackPane.setAlignment(sceneBadge, Pos.TOP_LEFT);
        StackPane.setMargin(sceneBadge, new Insets(12));

        fileLabel.setId("fileLabel");

        Polygon triangle = new Polygon(0, 0, 44, 26, 0, 52);
        triangle.setFill(Color.web(LIME, 0.18));
        triangle.setStroke(Color.web(PAPER, 0.70));
        triangle.setStrokeWidth(1.5);

        emptyState = new VBox(12, triangle, fileLabel);
        emptyState.setAlignment(Pos.CENTER);

        loadingIndicator.setPrefSize(38, 38);
        loadingIndicator.setMaxSize(38, 38);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        loadingIndicator.setStyle("-fx-progress-color: " + LIME + ";");

        Label preview = Theme.mono("16:9  \u00b7  PREVIEW", 8, FontWeight.BOLD, "#6f685d");
        StackPane.setAlignment(preview, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(preview, new Insets(12));

        videoPane.getChildren().addAll(mediaView, emptyState, loadingIndicator, sceneBadge, preview);

        FontIcon playIcon = new FontIcon(Feather.PLAY);
        playIcon.setIconSize(12);
        playIcon.setIconColor(Color.web("#1c160c"));
        playBtn.setGraphic(playIcon);
        playBtn.setStyle(brassButton());
        hover(playBtn, brassButton(), brassHover());

        loopBtn.setStyle(slateButton());
        loopBtn.selectedProperty().addListener((obs, old, on) ->
            loopBtn.setStyle(on ? slateHover() : slateButton()));

        moreBtn.setStyle(slateButton());
        hover(moreBtn, slateButton(), slateHover());

        sceneBtn.setStyle(slateButton());
        hover(sceneBtn, slateButton(), slateHover());

        FontIcon fileIcon = new FontIcon(Feather.FOLDER);
        fileIcon.setIconSize(12);
        fileIcon.setIconColor(Color.web("#f1e8dc"));
        fileBtn.setGraphic(fileIcon);
        fileBtn.setStyle(brickButton());
        hover(fileBtn, brickButton(), brickHover());

        for (Movie movie : movies) {
            MenuItem item = new MenuItem(clipLabel(movie));
            item.setOnAction(event -> loadBundledClip(movie));
            fileBtn.getItems().add(item);
        }
        fileBtn.getItems().add(new SeparatorMenuItem());
        MenuItem localFile = new MenuItem("From this computer...");
        localFile.setOnAction(event -> chooseFile());
        fileBtn.getItems().add(localFile);

        volume.setPrefWidth(80);
        volume.setId("volumeSlider");

        Region controlSpacer = new Region();
        HBox.setHgrow(controlSpacer, Priority.ALWAYS);

        HBox controls = new HBox(8, playBtn, loopBtn, moreBtn, sceneBtn, fileBtn,
            controlSpacer, clipTime,
            Theme.label("VOL", 8, FontWeight.BOLD, MUTED), volume);
        controls.setAlignment(Pos.CENTER_LEFT);

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(13);
        searchIcon.setIconColor(Color.web(LIME));
        searchBtn.setGraphic(searchIcon);
        searchBtn.setGraphicTextGap(9);
        searchBtn.setPrefWidth(340);
        searchBtn.setMaxWidth(Region.USE_PREF_SIZE);
        searchBtn.setStyle(searchButtonStyle());
        hover(searchBtn, searchButtonStyle(), searchButtonHoverStyle());
        searchBtn.setId("searchAnswer");

        FontIcon confirmIcon = new FontIcon(Feather.CHECK);
        confirmIcon.setIconSize(12);
        confirmIcon.setIconColor(Color.web("#1c160c"));
        confirmBtn.setGraphic(confirmIcon);
        confirmBtn.setStyle(brassButton());
        hover(confirmBtn, brassButton(), brassHover());

        Region answerSpacer = new Region();
        HBox.setHgrow(answerSpacer, Priority.ALWAYS);
        answerRow = new HBox(10, Theme.label("YOUR ANSWER:", 9, FontWeight.BOLD, MUTED),
            answerValue, answerSpacer, confirmBtn);
        answerRow.setAlignment(Pos.CENTER_LEFT);
        answerRow.setVisible(false);
        answerRow.setManaged(false);

        statusLabel.setWrapText(true);
        HBox resultText = new HBox(10, resultTitle, statusLabel);
        resultText.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        nextBtn.setStyle(brassButton());
        hover(nextBtn, brassButton(), brassHover());
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);

        resultBanner = new HBox(12, resultText, nextBtn);
        resultBanner.setAlignment(Pos.CENTER_LEFT);
        resultBanner.setPadding(new Insets(6, 14, 6, 14));
        resultBanner.setStyle(neutralBanner());

        VBox answerPane = new VBox(5, searchBtn, answerRow);

        VBox slate = new VBox(7, videoPane, timeline, controls, answerPane, resultBanner);
        slate.setPadding(new Insets(10, 14, 8, 14));
        slate.setStyle("-fx-background-color: #101317;"
            + " -fx-background-radius: 0 0 6 6;"
            + " -fx-border-color: #252a30;"
            + " -fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 6 6;");
        VBox.setVgrow(videoPane, Priority.ALWAYS);
        VBox.setVgrow(slate, Priority.ALWAYS);

        VBox board = new VBox(new ClapperBar(), slate);
        VBox body = new VBox(board);
        body.setPadding(new Insets(0, 20, 6, 20));
        VBox.setVgrow(board, Priority.ALWAYS);

        FontIcon backIcon = new FontIcon(Feather.ARROW_LEFT);
        backIcon.setIconSize(12);
        backIcon.setIconColor(Color.web(SOFT_TEXT));
        Button backBtn = new Button("BACK", backIcon);
        backBtn.setStyle(slateButton());
        hover(backBtn, slateButton(), slateHover());
        backBtn.setOnAction(event -> {
            dispose();
            Navigator.go(new MovieView());
        });

        VBox headerLeft = new VBox(0, roundLabel, scoreLabel);
        Region headSpacer1 = new Region();
        HBox.setHgrow(headSpacer1, Priority.ALWAYS);
        Region headSpacer2 = new Region();
        HBox.setHgrow(headSpacer2, Priority.ALWAYS);
        HBox header = new HBox(16, backBtn, headerLeft, headSpacer1, worthLabel,
            headSpacer2, timerLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(8, 20, 6, 20));
        header.setStyle("-fx-background-color: " + Theme.BLACK + ";"
            + " -fx-border-color: transparent transparent rgba(236,230,218,0.18) transparent;"
            + " -fx-border-width: 0 0 1 0;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(body);

        getChildren().addAll(root, buildSearchLayer());

        wireActions();
        startRound();
        startTicker();
        if (showTutorial) {
            javafx.application.Platform.runLater(() ->
                getChildren().add(new GuidedTutorialOverlay(tutorialSteps(),
                    () -> tutorialActive = false)));
        }
    }

    private List<GuidedTutorialOverlay.Step> tutorialSteps() {
        return List.of(
            new GuidedTutorialOverlay.Step(roundLabel.getParent(), "Rounds and score",
                "A match has five rounds. Your running score stays here."),
            new GuidedTutorialOverlay.Step(videoPane, "Watch the scene",
                "Each round shows a short clip. The first three seconds are available first."),
            new GuidedTutorialOverlay.Step(playBtn, "Play or pause",
                "Replay the clip if you need another look. The round clock keeps running."),
            new GuidedTutorialOverlay.Step(timeline, "Follow the timeline",
                "The film strip shows how much of the clip is currently unlocked."),
            new GuidedTutorialOverlay.Step(moreBtn, "Reveal more footage",
                "Unlock 5 seconds, then the full clip. More footage lowers the points available."),
            new GuidedTutorialOverlay.Step(sceneBtn, "Try another scene",
                "Switch scenes for a 150-point cost if another angle might help."),
            new GuidedTutorialOverlay.Step(fileBtn, "Choose a clip",
                "Pick a bundled movie clip or load a video from your computer."),
            new GuidedTutorialOverlay.Step(searchBtn, "Make your guess",
                "Search for a title, select it, then confirm before the 60-second timer ends.")
        );
    }

    /* --------------------------------------------------------------- search */

    private StackPane buildSearchLayer() {
        FontIcon headerIcon = new FontIcon(Feather.SEARCH);
        headerIcon.setIconSize(12);
        headerIcon.setIconColor(Color.web(LIME));
        Label panelTitle = Theme.label("SEARCH MOVIES", 9, FontWeight.BOLD, SOFT_TEXT);
        Region headerGap = new Region();
        HBox.setHgrow(headerGap, Priority.ALWAYS);

        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(12);
        closeIcon.setIconColor(Color.web(SOFT_TEXT));
        Button searchClose = new Button("", closeIcon);
        searchClose.setStyle(iconButtonStyle());
        hover(searchClose, iconButtonStyle(), iconButtonHoverStyle());
        searchClose.setOnAction(event -> closeSearch());

        HBox panelHeader = new HBox(8, headerIcon, panelTitle, headerGap, searchClose);
        panelHeader.setAlignment(Pos.CENTER_LEFT);

        FontIcon fieldIcon = new FontIcon(Feather.SEARCH);
        fieldIcon.setIconSize(14);
        fieldIcon.setIconColor(Color.web(MUTED));

        searchField = new TextField();
        searchField.setPromptText("Search movies by title...");
        searchField.setStyle(searchInputStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        FontIcon clearIcon = new FontIcon(Feather.X);
        clearIcon.setIconSize(11);
        clearIcon.setIconColor(Color.web(MUTED));
        Button searchClear = new Button("", clearIcon);
        searchClear.setStyle(iconButtonStyle());
        hover(searchClear, iconButtonStyle(), iconButtonHoverStyle());
        searchClear.setOnAction(event -> {
            searchField.clear();
            searchField.requestFocus();
        });
        searchClear.visibleProperty().bind(searchField.textProperty().isNotEmpty());
        searchClear.managedProperty().bind(searchClear.visibleProperty());

        HBox searchBox = new HBox(8, fieldIcon, searchField, searchClear);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle(searchBoxStyle());

        searchInfo = Theme.label("TYPE A TITLE TO SEARCH", 8, FontWeight.BOLD, MUTED);

        searchPlaceholder = Theme.label("Start typing to search " + titles.size() + " movies",
            11, FontWeight.NORMAL, MUTED);
        FontIcon emptyIcon = new FontIcon(Feather.SEARCH);
        emptyIcon.setIconSize(20);
        emptyIcon.setIconColor(Color.web(MUTED));
        VBox emptyBox = new VBox(8, emptyIcon, searchPlaceholder);
        emptyBox.setAlignment(Pos.CENTER);

        searchResults = new ListView<>();
        searchResults.setPlaceholder(emptyBox);
        searchResults.setPrefHeight(80);
        searchResults.setMaxHeight(360);
        searchResults.setStyle("-fx-control-inner-background: #17140f;"
            + " -fx-background-color: #17140f; -fx-border-color: " + Theme.BORDER + ";"
            + " -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 2;");
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
                title.setStyle("-fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 12px;"
                    + " -fx-font-weight: " + (lit ? "bold" : "normal") + ";");
                row.setStyle("-fx-background-color: "
                    + (lit ? "#292319" : "transparent")
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
        hints.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(9, panelHeader, searchBox, searchInfo, searchResults, hints);
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(500);
        panel.setMaxWidth(500);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.setStyle("-fx-background-color: #1a1714; -fx-background-radius: 5;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 5;"
            + " -fx-border-width: 1;");
        StackPane.setAlignment(panel, Pos.TOP_CENTER);
        StackPane.setMargin(panel, new Insets(40, 0, 0, 0));

        searchLayer = new StackPane(panel);
        searchLayer.setStyle("-fx-background-color: rgba(10,9,8,0.80);");
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
            for (String title : titles) {
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
            searchPlaceholder.setText("Start typing to search " + titles.size() + " movies");
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
            searchResults.getSelectionModel().select(0);
            searchResults.scrollTo(0);
        }
        searchResults.setPrefHeight(matches.isEmpty()
            ? 80 : Math.min(8, matches.size()) * 42 + 10);
    }

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

    /* --------------------------------------------------------------- actions */

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
            double level = value.doubleValue() / 100.0;
            AppSettings.setVideoVolume(level);
            if (player != null) {
                player.setVolume(level);
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
            loadBundledClip(movies.get(sceneIdx - 1));
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
        if (answered || tutorialActive) {
            return;
        }
        if (remaining <= 0) {
            answer(null);
            return;
        }
        remaining--;
        timerLabel.setText(clock(remaining));
        if (remaining <= 10) {
            timerLabel.setTextFill(Color.web(WARN));
        }
        if (remaining <= 0) {
            answer(null);
        }
    }

    /* ------------------------------------------------------------------ round */

    private void startRound() {
        stopPlayback();
        answered = false;
        playing = false;
        Arrays.fill(stage, 1);
        Arrays.fill(seen, false);
        sceneIdx = (round - 1) % SCENES + 1;
        seen[sceneIdx - 1] = true;
        remaining = ROUND_SECONDS;

        timeline.setTotal(10_000);
        timeline.setPosition(0);

        roundLabel.setText("TAKE " + round + " OF 5");
        scoreLabel.setText("ROLL " + String.format("%,d", score));
        timerLabel.setText(clock(remaining));
        timerLabel.setTextFill(Color.web(WHITE));
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);
        nextBtn.setText(round >= ROUNDS ? "PLAY AGAIN" : "NEXT ROUND");
        resultTitle.setText("WATCH THE CLIP");
        resultTitle.setTextFill(Color.web(SOFT_TEXT));
        resultBanner.setStyle(neutralBanner());
        statusLabel.setTextFill(Color.web(SOFT_TEXT));

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
        loadBundledClip(movies.get(sceneIdx - 1));
    }

    private void refreshAnswer() {
        correctTitle = movies.get(sceneIdx - 1).getTitle();
    }

    private void refreshScene() {
        sceneBadge.setText("SCENE " + sceneIdx + " OF " + SCENES);
        videoPane.setStyle(screenStyle(
            SCENE_PLATES[(sceneIdx - 1) % SCENE_PLATES.length]));
    }

    /** Black rim, charcoal mount, then the scene plate. */
    private static String screenStyle(String plate) {
        return "-fx-background-color: #0f0e0d, " + PAPER + ", " + plate + ";"
            + " -fx-background-insets: 0, 2, 6;"
            + " -fx-background-radius: 6, 4, 2;";
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

    private int stageLevel() {
        return stage[sceneIdx - 1];
    }

    private int maxStage() {
        int max = 1;
        for (int value : stage) {
            max = Math.max(max, value);
        }
        return max;
    }

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

    private Duration windowDuration(MediaPlayer source) {
        if (stageLevel() >= 3) {
            Duration total = source.getTotalDuration();
            return total == null ? Duration.ZERO : total;
        }
        return Duration.millis(timeline.windowEnd());
    }

    private void applyStageLimit() {
        if (player == null) {
            return;
        }
        if (stageLevel() >= 3) {
            if (appliedStopMs != -1) {
                player.setStopTime(Duration.INDEFINITE);
                appliedStopMs = -1;
            }
            return;
        }
        Duration total = player.getTotalDuration();
        if (total == null || total.isUnknown()) {
            return;
        }
        long end = timeline.windowEnd();
        if (end != appliedStopMs) {
            player.setStopTime(Duration.millis(end));
            appliedStopMs = end;
        }
    }

    private void moreTimeline() {
        if (answered || stageLevel() >= 3) {
            return;
        }
        Duration at = player == null ? null : player.getCurrentTime();
        long oldEnd = timeline.windowEnd();
        boolean parkedAtEnd = player != null && !playing && at != null
            && at.toMillis() >= oldEnd - 120;

        stage[sceneIdx - 1]++;
        refreshTimeline();
        applyStageLimit();

        if (player != null) {
            if (parkedAtEnd) {
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
        refreshAnswer();
        timeline.setTotal(10_000);
        timeline.setPosition(0);
        clipTime.setText("0:00 / 0:00");
        fileLabel.setText("NO CLIP LOADED");
        pickedTitle = null;
        answerValue.setText("-");
        answerRow.setVisible(false);
        answerRow.setManaged(false);
        searchBtn.setText("SEARCH FOR A MOVIE");
        refreshTimeline();
        statusLabel.setTextFill(Color.web(SOFT_TEXT));
        statusLabel.setText("Scene " + sceneIdx + " of " + SCENES
            + " - each new scene costs " + SCENE_PENALTY + " points.");
        loadBundledClip(movies.get(sceneIdx - 1));
    }

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a movie clip");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video files",
                "*.mp4", "*.mkv", "*.avi", "*.mov", "*.webm", "*.m4v"),
            new FileChooser.ExtensionFilter("All files", "*.*"));
        File start = new File(System.getProperty("user.home", "."));
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

    private void loadBundledClip(Movie movie) {
        URL url = getClass().getResource(movie.getClipPath());
        if (url == null) {
            statusLabel.setTextFill(Color.web(WARN));
            statusLabel.setText("Bundled clip missing: " + movie.getClipPath() + ".");
            return;
        }
        fileLabel.setText(clipLabel(movie).toUpperCase(Locale.ROOT));
        sceneBadge.setText("SCENE " + sceneIdx + " OF " + SCENES);
        playClip(url.toExternalForm(), clipLabel(movie));
    }

    private void playClip(String source, String label) {
        playClip(source, label, 0);
    }

    private void playClip(String source, String label, int attempt) {
        stopPlayback();
        try {
            MediaPlayer created = mediaCache.get(source);
            boolean reusable = created != null
                && created.getStatus() != MediaPlayer.Status.DISPOSED
                && created.getStatus() != MediaPlayer.Status.UNKNOWN
                && created.getStatus() != MediaPlayer.Status.HALTED;
            if (!reusable) {
                if (created != null) {
                    mediaCache.remove(source);
                    created.stop();
                    created.dispose();
                    scheduleMediaMemoryCleanup();
                }
                Media media = new Media(source);
                created = new MediaPlayer(media);
                MediaPlayer built = created;
                built.setOnReady(() -> markReady(built, label));
                long[] lastProgressUpdateNanos = {0};
                built.currentTimeProperty().addListener((obs, old, time) -> {
                    if (built != player) {
                        return;
                    }
                    long now = System.nanoTime();
                    if (lastProgressUpdateNanos[0] == 0
                            || now - lastProgressUpdateNanos[0] >= 250_000_000L) {
                        lastProgressUpdateNanos[0] = now;
                        if (!seeking && time != null) {
                            timeline.setPosition(
                                Math.min((long) time.toMillis(), timeline.windowEnd()));
                        }
                        clipTime.setText(clockDuration(time) + " / "
                            + clockDuration(windowDuration(built)));
                    }
                });
                built.setOnEndOfMedia(() -> {
                    if (built != player) {
                        return;
                    }
                    if (loopBtn.isSelected()) {
                        return;
                    }
                    playing = false;
                    playBtn.setText("PLAY");
                    built.pause();
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
                built.setOnError(() -> {
                    if (built != player) {
                        return;
                    }
                    MediaException error = built.getError();
                    if (attempt == 0) {
                        mediaCache.remove(source);
                        stopPlayback();
                        built.dispose();
                        scheduleMediaMemoryCleanup();
                        javafx.application.Platform.runLater(() -> playClip(source, label, 1));
                        return;
                    }
                    showPlaybackError(error);
                });
                media.setOnError(() -> {
                    if (built == player) {
                        showPlaybackError(media.getError());
                    }
                });
                mediaCache.put(source, built);
                evictCachedPlayers(source);
            }
            player = created;
            appliedStopMs = Long.MIN_VALUE;
            created.setVolume(AppSettings.getVideoVolume());
            created.setCycleCount(loopBtn.isSelected() ? MediaPlayer.INDEFINITE : 1);
            mediaView.setMediaPlayer(created);
            mediaView.setVisible(true);
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
            playBtn.setText("PAUSE");
            playing = true;
            clipTime.setText("0:00 / 0:00");
            statusLabel.setTextFill(Color.web(SOFT_TEXT));
            statusLabel.setText("Loading " + label + "...");
            if (created.getStatus() == MediaPlayer.Status.READY
                    || created.getStatus() == MediaPlayer.Status.PAUSED
                    || created.getStatus() == MediaPlayer.Status.STOPPED
                    || created.getStatus() == MediaPlayer.Status.PLAYING) {
                markReady(created, label);
                Duration at = created.getCurrentTime();
                if (at == null || at.toMillis() > 40) {
                    created.seek(Duration.ZERO);
                }
                created.play();
            } else {
                created.setAutoPlay(true);
            }
        } catch (RuntimeException error) {
            showPlaybackError(error);
        }
    }

    private void markReady(MediaPlayer source, String label) {
        if (source != player) {
            return;
        }
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        Duration total = source.getTotalDuration();
        if (total != null && !total.isUnknown()) {
            timeline.setTotal(Math.round(total.toMillis()));
        }
        timeline.setPosition(0);
        applyStageLimit();
        clipTime.setText("0:00 / " + clockDuration(windowDuration(source)));
        statusLabel.setTextFill(Color.web(SOFT_TEXT));
        statusLabel.setText("Playing " + label + " - name the movie.");
    }

    private void evictCachedPlayers(String keepSource) {
        if (mediaCache.size() <= MAX_CACHED_PLAYERS) {
            return;
        }
        boolean evicted = false;
        Iterator<Map.Entry<String, MediaPlayer>> entries = mediaCache.entrySet().iterator();
        while (mediaCache.size() > MAX_CACHED_PLAYERS && entries.hasNext()) {
            MediaPlayer candidate = entries.next().getValue();
            if (candidate == player) {
                continue;
            }
            entries.remove();
            candidate.stop();
            candidate.dispose();
            evicted = true;
        }
        if (mediaCache.size() > MAX_CACHED_PLAYERS) {
            MediaPlayer spare = mediaCache.remove(keepSource);
            if (spare != null && spare != player) {
                spare.stop();
                spare.dispose();
                evicted = true;
            }
        }
        if (evicted) {
            scheduleMediaMemoryCleanup();
        }
    }

    private void stopPlayback() {
        MediaPlayer previous = player;
        player = null;
        mediaView.setMediaPlayer(null);
        mediaView.setVisible(false);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        if (previous != null) {
            previous.pause();
        }
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
        resultTitle.setTextFill(Color.web(WARN));
        statusLabel.setTextFill(Color.web(WARN));
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
        int awardedPoints = correct ? points() : 0;

        String result;
        if (picked == null) {
            result = "TIME'S UP";
            statusLabel.setTextFill(Color.web(WARN));
            statusLabel.setText("Time's up - it was " + correctTitle + ".");
        } else if (correct) {
            score += awardedPoints;
            scoreLabel.setText("ROLL " + String.format("%,d", score));
            result = "CORRECT";
            statusLabel.setTextFill(Color.web(GREEN));
            statusLabel.setText("Correct - +" + String.format("%,d", awardedPoints)
                + " points. Nice one.");
        } else {
            result = "WRONG";
            statusLabel.setTextFill(Color.web(WARN));
            statusLabel.setText("Not quite - it was " + correctTitle + ".");
        }

        GameHistory.record(correctTitle, awardedPoints);
        if (round >= ROUNDS) {
            result = result + " - MATCH OVER";
            statusLabel.setText(statusLabel.getText() + " Final score "
                + String.format("%,d", score) + ".");
        }

        resultTitle.setText(result);
        resultTitle.setTextFill(Color.web(correct ? GREEN : WARN));
        resultBanner.setStyle(correct ? correctBanner() : wrongBanner());
        nextBtn.setText(round >= ROUNDS ? "PLAY AGAIN" : "NEXT ROUND");
        nextBtn.setVisible(true);
        nextBtn.setManaged(true);
        refreshTimeline();
        worthLabel.setText("ROUND OVER");
    }

    private void nextRound() {
        round++;
        if (round > ROUNDS) {
            round = 1;
            score = 0;
        }
        startRound();
    }

    private void dispose() {
        if (ticker != null) {
            ticker.stop();
        }
        stopPlayback();
        for (MediaPlayer cached : mediaCache.values()) {
            try {
                cached.stop();
                cached.dispose();
            } catch (RuntimeException ignored) {
                // A pipeline that already failed may throw on dispose.
            }
        }
        mediaCache.clear();
        scheduleMediaMemoryCleanup();
    }

    private static void scheduleMediaMemoryCleanup() {
        if (pendingMediaCleanup != null && !pendingMediaCleanup.isDone()) {
            return;
        }
        pendingMediaCleanup = MEDIA_CLEANER.schedule(() -> {
            System.gc();
        }, 1200, TimeUnit.MILLISECONDS);
    }

    private static String clock(int seconds) {
        return String.format("%d:%02d", Math.max(0, seconds) / 60, Math.max(0, seconds) % 60);
    }

    private static String clipLabel(Movie movie) {
        String title = movie.getTitle();
        int colon = title.lastIndexOf(':');
        return colon >= 0 ? title.substring(colon + 1).trim() : title;
    }

    /* ------------------------------------------------------------- clip stage */

    /**
     * Three-part timeline (0-3s, 3-5s, 5s-end) drawn as sprocketed film stock.
     * Locked parts stay on unexposed stock, played parts print in brass, and
     * the playhead is a cream splice mark.
     */
    static final class DemoTimeline extends Pane {
        private static final double PAD = 12;
        private static final double TRACK_TOP = 5;
        private static final double TRACK_HEIGHT = 32;
        private static final long[] STAGE_MS = {3_000, 5_000};
        private static final String[] LABELS = {"3s", "5s", "FULL CLIP"};
        private static final Color STOCK = Color.web("#0f0e0d");
        private static final Color LOCKED = Color.web("#262421");
        private static final Color OPEN = Color.web("#4a463f");
        private static final Color LOCKED_TEXT = Color.web("#777064");
        private static final Color PLAYED = Color.web("#c6a15b");
        private static final Color SPROCKET = Color.web("#4d4740");
        private static final Color TRACK_EDGE = Color.web("#38332d");
        private static final Color KNOB = Color.web("#ece6da");

        private final Canvas canvas = new Canvas();
        private long totalMs = 10_000;
        private int unlocked = 1;
        private long positionMs;
        private long lastSeekNanos;

        Runnable onSeekStart = () -> { };
        java.util.function.LongConsumer onSeek = ms -> { };
        Runnable onSeekEnd = () -> { };

        DemoTimeline() {
            setMinHeight(56);
            setPrefHeight(56);
            setMaxHeight(56);
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

        long stageEnd(int index) {
            return index >= 2 ? totalMs : Math.min(STAGE_MS[index], totalMs);
        }

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

            double trackX = PAD;
            double trackW = Math.max(1, w - 2 * PAD);
            double innerTop = TRACK_TOP + 10;
            double innerH = TRACK_HEIGHT - 20;

            g.setFill(STOCK);
            g.fillRoundRect(trackX, TRACK_TOP, trackW, TRACK_HEIGHT, 5, 5);
            g.setStroke(TRACK_EDGE);
            g.setLineWidth(2);
            g.strokeRoundRect(trackX + 1, TRACK_TOP + 1, trackW - 2, TRACK_HEIGHT - 2, 4, 4);

            g.setFill(SPROCKET);
            for (double x = trackX + 8; x < trackX + trackW - 6; x += 12) {
                g.fillRect(x, TRACK_TOP + 4, 6, 4);
                g.fillRect(x, TRACK_TOP + TRACK_HEIGHT - 8, 6, 4);
            }

            long start = 0;
            for (int i = 0; i < 3; i++) {
                long end = stageEnd(i);
                double x0 = x(start) + 1;
                double x1 = x(end) - 1;
                boolean lit = i < unlocked;

                g.setFill(lit ? OPEN : LOCKED);
                g.fillRect(x0, innerTop, Math.max(0, x1 - x0), innerH);

                if (lit && positionMs > start) {
                    double px = Math.min(x(positionMs), x1);
                    g.setFill(PLAYED);
                    g.fillRect(x0, innerTop, Math.max(0, px - x0), innerH);
                }

                g.setStroke(STOCK);
                g.setLineWidth(2);
                g.strokeLine(x1, innerTop, x1, innerTop + innerH);
                start = end;
            }

            g.setFont(Font.font(Theme.MONO, 10));
            for (int i = 0; i < 3; i++) {
                long end = stageEnd(i);
                boolean lit = i < unlocked;
                g.setFill(lit ? KNOB : LOCKED_TEXT);
                if (i == 2) {
                    g.setTextAlign(TextAlignment.RIGHT);
                    g.fillText(LABELS[i], trackX + trackW, TRACK_TOP + TRACK_HEIGHT + 15);
                } else {
                    g.setTextAlign(TextAlignment.CENTER);
                    g.fillText(LABELS[i], x(end), TRACK_TOP + TRACK_HEIGHT + 15);
                }
            }
            g.setTextAlign(TextAlignment.LEFT);

            double px = Math.max(trackX + 2, Math.min(x(positionMs), trackX + trackW - 2));
            g.setFill(Color.web("#ffffff", 0.16));
            g.fillRect(px - 4, TRACK_TOP + 3, 8, TRACK_HEIGHT - 6);
            g.setFill(KNOB);
            g.fillRect(px - 2, TRACK_TOP + 3, 4, TRACK_HEIGHT - 6);
        }
    }

    /** Closed black clapperboard arm with cool-gray diagonal stripes and a hinge. */
    static final class ClapperBar extends Pane {
        private static final double BAR_H = 28;
        private static final Color STRIPE = Color.web("#c4cbd2");
        private static final Color DARK = Color.web("#101317");

        private final Canvas canvas = new Canvas();

        ClapperBar() {
            setMinHeight(30);
            setPrefHeight(30);
            setMaxHeight(30);
            getChildren().add(canvas);
        }

        @Override
        protected void layoutChildren() {
            canvas.setWidth(getWidth());
            canvas.setHeight(getHeight());
            draw();
        }

        private void draw() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, w, h);

            g.setFill(DARK);
            g.fillRect(0, 0, w, BAR_H);

            double period = 54;
            double stripe = period * 0.52;
            double skew = 15;
            g.save();
            g.beginPath();
            g.rect(0, 0, w, BAR_H);
            g.clip();
            g.setFill(DARK);
            g.fillRect(0, 0, w, BAR_H);
            for (double x = -period; x < w + period; x += period) {
                g.setFill(STRIPE);
                g.fillPolygon(
                    new double[]{x + skew, x + stripe + skew, x + stripe, x},
                    new double[]{0, 0, BAR_H, BAR_H}, 4);
            }
            g.restore();

            double plate = Math.min(120, w * 0.24);
            g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#252a30")), new Stop(1, Color.web("#101317"))));
            g.fillRect(0, 0, plate, BAR_H);
            g.setStroke(Color.web("#000000", 0.55));
            g.setLineWidth(1);
            g.strokeLine(plate, 0, plate, BAR_H);

            double[][] rivets = {{16, 9}, {16, 21}, {plate - 16, 21}};
            for (double[] rivet : rivets) {
                if (rivet[0] > plate - 6) {
                    continue;
                }
                g.setFill(Color.web("#89939d"));
                g.fillOval(rivet[0] - 3, rivet[1] - 3, 6, 6);
                g.setFill(Color.web("#454d56"));
                g.fillOval(rivet[0] - 1, rivet[1] - 1, 2, 2);
            }

            g.setStroke(Color.web("#ffffff", 0.12));
            g.strokeLine(0, 0.5, w, 0.5);
            g.setFill(Color.web("#000000", 0.32));
            g.fillRect(0, BAR_H - 3, w, 3);
        }
    }

    /* ------------------------------------------------------------ shared styles */

    private static void hover(Button button, String rest, String hovered) {
        button.setOnMouseEntered(event -> button.setStyle(hovered));
        button.setOnMouseExited(event -> button.setStyle(rest));
    }

    private static void hover(MenuButton button, String rest, String hovered) {
        button.setOnMouseEntered(event -> button.setStyle(hovered));
        button.setOnMouseExited(event -> button.setStyle(rest));
    }

    private static String brassButton() {
        return "-fx-background-color: " + LIME + ";"
            + " -fx-text-fill: #1c160c;"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 14 8 14; -fx-cursor: hand;";
    }

    private static String brassHover() {
        return "-fx-background-color: " + Theme.LIME_HOVER + ";"
            + " -fx-text-fill: #1c160c;"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 14 8 14; -fx-cursor: hand;";
    }

    private static String slateButton() {
        return "-fx-background-color: #1b1815; -fx-text-fill: " + SOFT_TEXT + ";"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: #4a443c; -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 13 8 13; -fx-cursor: hand;";
    }

    private static String slateHover() {
        return "-fx-background-color: #262019; -fx-text-fill: " + WHITE + ";"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: #7a6a50; -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 13 8 13; -fx-cursor: hand;";
    }

    private static String brickButton() {
        return "-fx-background-color: " + Theme.RED_DEEP + "; -fx-text-fill: #f1e8dc;"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 13 8 13; -fx-cursor: hand;";
    }

    private static String brickHover() {
        return "-fx-background-color: " + Theme.RED + "; -fx-text-fill: #f6eee2;"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: rgba(255,255,255,0.20); -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 8 13 8 13; -fx-cursor: hand;";
    }

    private static String searchButtonStyle() {
        return "-fx-background-color: #1b1815; -fx-text-fill: " + LIME + ";"
            + " -fx-font-family: '" + Theme.MONO + "'; -fx-font-size: 11px;"
            + " -fx-font-weight: bold; -fx-alignment: center-left;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: #4a443c; -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 10 15 10 15; -fx-cursor: hand;";
    }

    private static String searchButtonHoverStyle() {
        return "-fx-background-color: #262019; -fx-text-fill: " + Theme.LIME_HOVER + ";"
            + " -fx-font-family: '" + Theme.MONO + "'; -fx-font-size: 11px;"
            + " -fx-font-weight: bold; -fx-alignment: center-left;"
            + " -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: " + LIME + "; -fx-border-radius: 3; -fx-border-width: 1;"
            + " -fx-padding: 10 15 10 15; -fx-cursor: hand;";
    }

    private static String searchInputStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + WHITE + ";"
            + " -fx-prompt-text-fill: " + MUTED + ";"
            + " -fx-font-family: '" + Theme.MONO + "'; -fx-font-size: 12px;"
            + " -fx-border-color: transparent; -fx-padding: 0;";
    }

    private static String searchBoxStyle() {
        return "-fx-background-color: #14110f; -fx-background-radius: 3; -fx-background-insets: 0;"
            + " -fx-border-color: #4a443c; -fx-border-radius: 3;"
            + " -fx-border-width: 1; -fx-padding: 9 12 9 12;";
    }

    private static String iconButtonStyle() {
        return "-fx-background-color: transparent; -fx-background-radius: 2;"
            + " -fx-background-insets: 0; -fx-padding: 3 4 3 4; -fx-cursor: hand;";
    }

    private static String iconButtonHoverStyle() {
        return "-fx-background-color: #262019; -fx-background-radius: 2;"
            + " -fx-background-insets: 0; -fx-padding: 3 4 3 4; -fx-cursor: hand;";
    }

    private static HBox searchHint(String key, String action) {
        Label cap = Theme.mono(key, 8, FontWeight.BOLD, SOFT_TEXT);
        cap.setStyle("-fx-background-color: #262019; -fx-background-radius: 2;"
            + " -fx-border-color: #4a443c; -fx-border-radius: 2;"
            + " -fx-padding: 2 6 2 6;");
        HBox hint = new HBox(5, cap, Theme.label(action, 8, FontWeight.NORMAL, MUTED));
        hint.setAlignment(Pos.CENTER_LEFT);
        return hint;
    }

    private static String neutralBanner() {
        return bannerStyle("#1c1917", "#332e28", "#4a443c");
    }

    private static String correctBanner() {
        return bannerStyle("rgba(143,174,125,0.10)", "rgba(143,174,125,0.38)", GREEN);
    }

    private static String wrongBanner() {
        return bannerStyle("rgba(196,106,98,0.10)", "rgba(196,106,98,0.38)", WARN);
    }

    private static String bannerStyle(String background, String border, String edge) {
        return "-fx-background-color: " + background + "; -fx-background-radius: 3;"
            + " -fx-background-insets: 0;"
            + " -fx-border-color: " + border + " " + border + " " + border + " " + edge + ";"
            + " -fx-border-radius: 3; -fx-border-width: 1 1 1 3;";
    }
}
