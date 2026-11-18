package Testing_for_bascially_everything;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.LongConsumer;

/**
 * Guess It - movie guessing game built on the VLCJ player from Media_test_vlcj.
 *
 * Movies, genres and scenes come from the MariaDB/MySQL database "guessit"
 * (tables genre, movie, scene). The video files stay on disk in the clips
 * folder; scene.video_file stores where each file is:
 *
 *   - a path relative to the clips folder, e.g.  Inception/scene1.mp4
 *   - or an absolute path, e.g.                  D:/videos/inception1.mp4
 *
 * Connection settings and the clips folder are read from guessit.properties
 * (see the file next to pom.xml). Scenes are played in scene_priority order,
 * the first three per movie are used.
 *
 * GamePage is a normal JavaFX node, so it can be dropped into your project's
 * own navigation. GuessIt_Game below is just a small launcher with a settings
 * menu so you can run it on its own.
 */
public class Movie_Playing_Page extends Application {

    // ---------------------------------------------------------------- rules

    static final int ROUNDS_PER_MATCH = 5;
    static final int ROUND_SECONDS = 60;
    static final int[] STAGE_POINTS = {1000, 700, 400};   // 3s, 5s, full clip
    static final int SCENE_PENALTY = 150;                 // per extra scene opened
    static final int MIN_CORRECT_POINTS = 50;

    // ---------------------------------------------------------------- model

    enum GuessMode { SEARCH, MULTIPLE_CHOICE }

    static final class Movie {
        final int id;
        final String title;
        final String category;
        final String hint;
        final int priority;
        final int genreId;      // 0 when the movie has no genre
        final String genre;     // null when the movie has no genre
        final List<File> clips = new ArrayList<>();

        Movie(int id, String title, String category, String hint, int priority,
              int genreId, String genre) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.hint = hint;
            this.priority = priority;
            this.genreId = genreId;
            this.genre = genre;
        }
    }

    static final class RoundResult {
        final String title;
        final String answer;
        final int points;
        final boolean correct;

        RoundResult(String title, String answer, int points, boolean correct) {
            this.title = title;
            this.answer = answer;
            this.points = points;
            this.correct = correct;
        }
    }

    /** Database login and clips folder. Read from guessit.properties when present. */
    static final class Config {
        String dbUrl = "jdbc:mariadb://127.0.0.1:3306/guessit_test";
        String dbUser = "root";
        String dbPassword = "";
        File clipsDir = new File("clips");

        static Config load() {
            Config c = new Config();
            File f = new File("guessit.properties");
            if (f.isFile()) {
                Properties p = new Properties();
                try (Reader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                    p.load(r);
                } catch (IOException ignored) {
                    // fall back to the defaults above
                }
                c.dbUrl = p.getProperty("db.url", c.dbUrl).trim();
                c.dbUser = p.getProperty("db.user", c.dbUser).trim();
                c.dbPassword = p.getProperty("db.password", c.dbPassword);
                c.clipsDir = new File(p.getProperty("clips.dir", "clips").trim());
            }
            return c;
        }
    }

    static final class MovieLibrary {
        final List<Movie> allMovies = new ArrayList<>();   // every movie row (search + wrong answers)
        final List<Movie> movies = new ArrayList<>();      // movies with at least one playable clip
        final List<String> allTitles = new ArrayList<>();
        int missingFiles;                                  // scene rows whose file wasn't found

        static MovieLibrary load(Config cfg) throws SQLException {
            MovieLibrary lib = new MovieLibrary();
            Map<Integer, Movie> byId = new LinkedHashMap<>();

            DriverManager.setLoginTimeout(5);
            try (Connection con = DriverManager.getConnection(cfg.dbUrl, cfg.dbUser, cfg.dbPassword)) {
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT m.movie_id, m.title, m.category, m.hint, m.movie_priority, "
                                     + "m.genre_id, g.genre_name "
                                     + "FROM movie m LEFT JOIN genre g ON g.genre_id = m.genre_id "
                                     + "ORDER BY m.movie_id")) {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        if (title == null || title.isBlank()) continue;
                        Movie m = new Movie(rs.getInt("movie_id"), title.trim(),
                                rs.getString("category"), rs.getString("hint"),
                                rs.getInt("movie_priority"),          // 0 when NULL
                                rs.getInt("genre_id"), rs.getString("genre_name"));
                        byId.put(m.id, m);
                    }
                }
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT movie_id, video_file FROM scene WHERE video_file IS NOT NULL "
                                     + "ORDER BY movie_id, scene_priority, scene_id")) {
                    while (rs.next()) {
                        Movie m = byId.get(rs.getInt("movie_id"));
                        String path = rs.getString("video_file");
                        if (m == null || path == null || path.isBlank() || m.clips.size() >= 3) continue;
                        File f = resolve(cfg.clipsDir, path.trim());
                        if (f.isFile()) m.clips.add(f);
                        else lib.missingFiles++;
                    }
                }
            }

            Set<String> titles = new LinkedHashSet<>();
            for (Movie m : byId.values()) {
                lib.allMovies.add(m);
                titles.add(m.title);
                if (!m.clips.isEmpty()) lib.movies.add(m);
            }
            lib.allTitles.addAll(titles);
            lib.allTitles.sort(String.CASE_INSENSITIVE_ORDER);
            return lib;
        }

        /** video_file may be absolute, relative to the clips folder, or relative to the working folder. */
        private static File resolve(File clipsDir, String path) {
            File direct = new File(path);
            if (direct.isAbsolute()) return direct;
            File inClips = new File(clipsDir, path);
            if (inClips.isFile()) return inClips;
            if (direct.isFile()) return direct;                       // e.g. "clips/Inception/scene1.mp4"
            File byName = new File(clipsDir, direct.getName());
            return byName.isFile() ? byName : inClips;
        }
    }

    // ------------------------------------------------------------- styling

    private static final String CSS = ".game-root { -fx-background-color: #12141C; }\n" +
                                      ".label { -fx-text-fill: #ECE9E1; -fx-font-size: 14px; }\n" +
                                      ".label.muted { -fx-text-fill: #8E93A6; }\n" +
                                      ".label.title { -fx-font-family: \"Georgia\"; -fx-font-size: 30px; -fx-font-weight: bold; }\n" +
                                      ".label.big { -fx-font-family: \"Georgia\"; -fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #F2B134; }\n" +
                                      ".label.good { -fx-text-fill: #4CC38A; -fx-font-weight: bold; }\n" +
                                      ".label.bad { -fx-text-fill: #E5604D; -fx-font-weight: bold; }\n" +
                                      ".label.worth { -fx-text-fill: #F2B134; -fx-font-size: 16px; -fx-font-weight: bold; }\n" +
                                      ".label.timer { -fx-font-family: \"Consolas\"; -fx-font-size: 32px; -fx-font-weight: bold; }\n" +
                                      ".label.timer.urgent { -fx-text-fill: #E5604D; }\n" +
                                      ".label.badge { -fx-background-color: rgba(18,20,28,0.75); -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 12px; }\n" +
                                      ".button, .toggle-button { -fx-background-color: #262A3A; -fx-text-fill: #ECE9E1; -fx-background-radius: 8; -fx-padding: 10 18; -fx-font-size: 14px; -fx-cursor: hand; }\n" +
                                      ".button:hover, .toggle-button:hover { -fx-background-color: #343A55; }\n" +
                                      ".button:disabled, .toggle-button:disabled { -fx-opacity: 0.4; }\n" +
                                      ".toggle-button:selected { -fx-background-color: #5C4720; -fx-text-fill: #F2B134; }\n" +
                                      ".button.primary { -fx-background-color: #F2B134; -fx-text-fill: #1B1405; -fx-font-weight: bold; }\n" +
                                      ".button.primary:hover { -fx-background-color: #FFC552; }\n" +
                                      ".button.choice { -fx-font-size: 15px; -fx-padding: 14 16; -fx-wrap-text: true; -fx-alignment: CENTER_LEFT; }\n" +
                                      ".button.choice.correct { -fx-background-color: #2E7D5B; }\n" +
                                      ".button.choice.wrong { -fx-background-color: #A5392B; }\n" +
                                      ".text-field { -fx-background-color: #20243A; -fx-text-fill: #ECE9E1; -fx-prompt-text-fill: #8E93A6; -fx-background-radius: 8; -fx-padding: 12 14; -fx-font-size: 17px; }\n" +
                                      ".list-view { -fx-background-color: transparent; -fx-control-inner-background: transparent; }\n" +
                                      ".list-view .placeholder .label { -fx-text-fill: #8E93A6; }\n" +
                                      ".list-cell { -fx-background-color: transparent; -fx-text-fill: #ECE9E1; -fx-padding: 10 14; -fx-font-size: 15px; }\n" +
                                      ".list-cell:filled:hover, .list-cell:filled:selected { -fx-background-color: #343A55; -fx-text-fill: #ECE9E1; }\n" +
                                      ".panel { -fx-background-color: #1B1E2A; -fx-background-radius: 12; }\n" +
                                      ".slider .track { -fx-background-color: #2B2F3E; }\n" +
                                      ".slider .thumb { -fx-background-color: #F2B134; }\n";

    private static String cssUrl;

    static synchronized String cssUrl() {
        if (cssUrl == null) {
            try {
                File f = File.createTempFile("guessit", ".css");
                f.deleteOnExit();
                Files.writeString(f.toPath(), CSS);
                cssUrl = f.toURI().toString();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return cssUrl;
    }

    private static Label label(String text, String... styleClasses) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styleClasses);
        return l;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static String fmt(long ms) {
        long s = Math.max(0, ms) / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    // ------------------------------------------------- segmented timeline

    /**
     * Three-part timeline: 0-3s, 3-5s, 5s-end. Unlocked parts are lit, the
     * rest stay dim. Click or drag inside the lit part to seek.
     */
    static final class SegmentedTimeline extends Pane {
        private static final double PAD = 12;
        private static final long[] STAGE_MS = {3_000, 5_000};
        private static final String[] LABELS = {"3s", "5s", "Full clip"};
        private static final Color LOCKED = Color.web("#2B2F3E");
        private static final Color LOCKED_TEXT = Color.web("#6B7186");
        private static final Color LIT = Color.web("#5C4720");
        private static final Color PLAYED = Color.web("#F2B134");

        private final Canvas canvas = new Canvas();
        private long totalMs = 12_000;
        private int unlocked = 1;
        private long positionMs;
        private long lastSeekNanos;

        Runnable onSeekStart = () -> { };
        LongConsumer onSeek = ms -> { };
        Runnable onSeekEnd = () -> { };

        SegmentedTimeline() {
            setMinHeight(52);
            setPrefHeight(52);
            setMaxHeight(52);
            setCursor(Cursor.HAND);
            getChildren().add(canvas);
            setOnMousePressed(e -> {
                onSeekStart.run();
                seekTo(e, true);
            });
            setOnMouseDragged(e -> seekTo(e, false));
            setOnMouseReleased(e -> {
                seekTo(e, true);
                onSeekEnd.run();
            });
        }

        void setTotal(long ms) {
            totalMs = Math.max(ms, 1000);
            redraw();
        }

        void setUnlocked(int stage) {
            unlocked = Math.max(1, Math.min(3, stage));
            redraw();
        }

        void setPosition(long ms) {
            positionMs = Math.max(0, ms);
            redraw();
        }

        long stageEnd(int index) {
            return index >= 2 ? totalMs : Math.min(STAGE_MS[index], totalMs);
        }

        /** Last playable millisecond of the part the player has unlocked. */
        long windowEnd() {
            return unlocked >= 3 ? Math.max(totalMs - 300, 500) : stageEnd(unlocked - 1);
        }

        private double x(long ms) {
            return PAD + (double) ms / totalMs * Math.max(1, getWidth() - 2 * PAD);
        }

        private void seekTo(MouseEvent e, boolean force) {
            double frac = (e.getX() - PAD) / Math.max(1, getWidth() - 2 * PAD);
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
            if (w <= 0 || h <= 0) return;
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
            g.setFill(Color.web("#ECE9E1"));
            g.fillOval(px - 7, barY + barH / 2 - 7, 14, 14);
        }
    }

    // ------------------------------------------------------------ game page

    static final class GamePage extends StackPane {

        private final MovieLibrary library;
        private final GuessMode mode;
        private final Runnable onExit;

        private final MediaPlayerFactory factory;
        private final EmbeddedMediaPlayer player;
        private final ImageView imageView = new ImageView();
        private final Timeline ticker;

        // match state
        private List<Movie> rounds = new ArrayList<>();
        private int roundIndex;
        private int totalScore;
        private final List<RoundResult> history = new ArrayList<>();

        // round state
        private Movie movie;
        private int sceneIdx;
        private final int[] stage = {1, 1, 1};
        private final Set<Integer> viewed = new LinkedHashSet<>();
        private boolean roundActive;
        private long deadlineNanos;
        private String selectedAnswer;
        private long clipLengthMs;
        private boolean seeking;
        private boolean resumeAfterSeek;
        private boolean finishedFlag;

        // header
        private final Label roundLabel = label("Round 1 of 5");
        private final Label scoreLabel = label("Score 0", "muted");
        private final Label worthLabel = label("", "worth");
        private final Label timerLabel = label("1:00", "timer");

        // video + controls
        private final StackPane videoPane = new StackPane();
        private final Label sceneBadge = label("", "badge");
        private final SegmentedTimeline timeline = new SegmentedTimeline();
        private final Button playBtn = new Button("Play");
        private final ToggleButton loopBtn = new ToggleButton("Loop");
        private final Button moreBtn = new Button("More timeline");
        private final Button sceneBtn = new Button("Another scene");
        private final Label timeLabel = label("0:00 / 0:03", "muted");
        private final Slider volume = new Slider(0, 100, 75);

        // answer area
        private final VBox answerBox = new VBox(10);
        private final VBox searchPane = new VBox(10);
        private final Button searchBtn = new Button("Search for a movie");
        private final HBox answerRow = new HBox(12);
        private final Label answerValue = label("", "worth");
        private final Button confirmBtn = new Button("Confirm answer");
        private final GridPane choiceGrid = new GridPane();
        private final List<Button> choiceButtons = new ArrayList<>();

        // overlays
        private final StackPane searchLayer = new StackPane();
        private final TextField searchField = new TextField();
        private final ListView<String> searchResults = new ListView<>();
        private final StackPane summaryLayer = new StackPane();

        GamePage(MovieLibrary library, GuessMode mode, Runnable onExit) {
            this.library = library;
            this.mode = mode;
            this.onExit = onExit;

            getStyleClass().add("game-root");
            getStylesheets().add(cssUrl());

            factory = new MediaPlayerFactory();
            player = factory.mediaPlayers().newEmbeddedMediaPlayer();
            player.videoSurface().set(new ImageViewVideoSurface(imageView));
            player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> finishedFlag = true);
                }
            });

            getChildren().addAll(buildMain(), buildSearchLayer(), buildSummaryLayer());
            wireControls();

            ticker = new Timeline(new KeyFrame(Duration.millis(40), e -> tick()));
            ticker.setCycleCount(Animation.INDEFINITE);

            startMatch();
            ticker.play();
        }

        // ------------------------------------------------------ UI building

        private BorderPane buildMain() {
            // header: round + score on the left, points at stake in the middle, timer top right
            VBox left = new VBox(2, roundLabel, scoreLabel);
            roundLabel.getStyleClass().add("title");
            HBox header = new HBox(16, left, spacer(), worthLabel, spacer(), timerLabel);
            header.setAlignment(Pos.CENTER);

            // video in the middle
            videoPane.setStyle("-fx-background-color: black; -fx-background-radius: 10;");
            videoPane.setMinSize(0, 0);
            videoPane.setPrefSize(720, 405);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(videoPane.widthProperty());
            imageView.fitHeightProperty().bind(videoPane.heightProperty());
            StackPane.setAlignment(sceneBadge, Pos.TOP_LEFT);
            StackPane.setMargin(sceneBadge, new Insets(10));
            videoPane.getChildren().addAll(imageView, sceneBadge);
            VBox.setVgrow(videoPane, Priority.ALWAYS);

            // buttons under the video
            playBtn.getStyleClass().add("primary");
            playBtn.setPrefWidth(90);
            volume.setPrefWidth(90);
            HBox controls = new HBox(10, playBtn, loopBtn, moreBtn, sceneBtn,
                    spacer(), timeLabel, label("Vol", "muted"), volume);
            controls.setAlignment(Pos.CENTER_LEFT);

            // answer area under the buttons
            searchBtn.setMaxWidth(Double.MAX_VALUE);
            confirmBtn.getStyleClass().add("primary");
            answerRow.setAlignment(Pos.CENTER_LEFT);
            answerRow.getChildren().addAll(label("Your answer:", "muted"), answerValue,
                    spacer(), confirmBtn);
            answerRow.setVisible(false);
            answerRow.setManaged(false);
            searchPane.getChildren().addAll(searchBtn, answerRow);

            choiceGrid.setHgap(10);
            choiceGrid.setVgap(10);
            for (int i = 0; i < 3; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / 3);
                choiceGrid.getColumnConstraints().add(cc);
            }

            VBox center = new VBox(12, videoPane, timeline, controls, answerBox);
            center.setPadding(new Insets(14, 0, 0, 0));

            BorderPane main = new BorderPane();
            main.setPadding(new Insets(18, 24, 20, 24));
            main.setTop(header);
            main.setCenter(center);
            return main;
        }

        private StackPane buildSearchLayer() {
            searchField.setPromptText("Type a movie title (Esc to close)");
            searchResults.setPlaceholder(label("Start typing to see matching movies", "muted"));
            searchResults.setPrefHeight(80);
            searchResults.setMaxHeight(360);

            VBox panel = new VBox(10, searchField, searchResults);
            panel.getStyleClass().add("panel");
            panel.setPadding(new Insets(14));
            panel.setMaxWidth(560);
            panel.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane.setAlignment(panel, Pos.TOP_CENTER);
            StackPane.setMargin(panel, new Insets(20, 0, 0, 0));

            searchLayer.setStyle("-fx-background-color: rgba(10,11,16,0.72);");
            searchLayer.getChildren().add(panel);
            searchLayer.setVisible(false);
            searchLayer.setOnMouseClicked(e -> {
                if (e.getTarget() == searchLayer) closeSearch();
            });

            searchField.textProperty().addListener((obs, o, n) -> refreshSearchResults());
            searchField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    closeSearch();
                } else if (e.getCode() == KeyCode.ENTER) {
                    String pick = searchResults.getSelectionModel().getSelectedItem();
                    if (pick == null && !searchResults.getItems().isEmpty()) {
                        pick = searchResults.getItems().get(0);
                    }
                    if (pick != null) pickAnswer(pick);
                } else if (e.getCode() == KeyCode.DOWN && !searchResults.getItems().isEmpty()) {
                    searchResults.requestFocus();
                    searchResults.getSelectionModel().select(0);
                }
            });
            searchResults.setOnKeyPressed(e -> {
                String pick = searchResults.getSelectionModel().getSelectedItem();
                if (e.getCode() == KeyCode.ENTER && pick != null) pickAnswer(pick);
                if (e.getCode() == KeyCode.ESCAPE) closeSearch();
            });
            searchResults.setOnMouseClicked(e -> {
                String pick = searchResults.getSelectionModel().getSelectedItem();
                if (pick != null) pickAnswer(pick);
            });
            return searchLayer;
        }

        private StackPane buildSummaryLayer() {
            summaryLayer.setStyle("-fx-background-color: rgba(10,11,16,0.9);");
            summaryLayer.setVisible(false);
            return summaryLayer;
        }

        private void wireControls() {
            playBtn.setOnAction(e -> togglePlay());
            moreBtn.setOnAction(e -> unlockMore());
            sceneBtn.setOnAction(e -> nextScene());
            searchBtn.setOnAction(e -> openSearch());
            confirmBtn.setOnAction(e -> {
                if (selectedAnswer != null) finishRound(selectedAnswer);
            });
            volume.valueProperty().addListener((obs, o, n) ->
                    player.audio().setVolume(n.intValue()));

            timeline.onSeekStart = () -> {
                seeking = true;
                resumeAfterSeek = player.status().isPlaying();
                if (resumeAfterSeek) player.controls().setPause(true);
            };
            timeline.onSeek = ms -> {
                player.controls().setTime(ms);
                // Stepping one frame makes VLC draw the frame at the new position
                // even though the player is paused. Remove if your VLC already does it.
                player.controls().nextFrame();
                timeLabel.setText(fmt(ms) + " / " + fmt(displayEnd()));
            };
            timeline.onSeekEnd = () -> {
                seeking = false;
                if (resumeAfterSeek) player.controls().setPause(false);
            };
        }

        // --------------------------------------------------------- match flow

        private void startMatch() {
            rounds = pickRounds(ROUNDS_PER_MATCH);
            roundIndex = 0;
            totalScore = 0;
            history.clear();
            summaryLayer.setVisible(false);
            startRound();
        }

        /** Random movies without repeats; a higher movie_priority makes a movie more likely. */
        private List<Movie> pickRounds(int count) {
            List<Movie> pool = new ArrayList<>(library.movies);
            List<Movie> picked = new ArrayList<>();
            Random rnd = new Random();
            while (picked.size() < count && !pool.isEmpty()) {
                double total = 0;
                for (Movie m : pool) total += Math.max(1, m.priority);
                double r = rnd.nextDouble() * total;
                Movie chosen = pool.get(pool.size() - 1);
                for (Movie m : pool) {
                    r -= Math.max(1, m.priority);
                    if (r < 0) {
                        chosen = m;
                        break;
                    }
                }
                pool.remove(chosen);
                picked.add(chosen);
            }
            return picked;
        }

        private void startRound() {
            movie = rounds.get(roundIndex);
            Arrays.fill(stage, 1);
            viewed.clear();
            selectedAnswer = null;
            roundActive = true;
            deadlineNanos = System.nanoTime() + ROUND_SECONDS * 1_000_000_000L;
            roundLabel.setText("Round " + (roundIndex + 1) + " of " + rounds.size());
            timerLabel.getStyleClass().remove("urgent");
            buildAnswerArea();
            loadScene(0);
        }

        private void nextRound() {
            roundIndex++;
            if (roundIndex >= rounds.size()) {
                showSummary();
            } else {
                startRound();
            }
        }

        private void finishRound(String answer) {
            if (!roundActive) return;
            roundActive = false;
            closeSearch();

            boolean correct = answer != null && answer.equalsIgnoreCase(movie.title);
            int points = correct ? potentialPoints() : 0;
            totalScore += points;
            history.add(new RoundResult(movie.title, answer, points, correct));

            // let the player watch the whole clip now that the round is over
            Arrays.fill(stage, 3);
            searchBtn.setDisable(true);
            confirmBtn.setDisable(true);
            for (Button b : choiceButtons) {
                if (b.getText().equalsIgnoreCase(movie.title)) {
                    b.getStyleClass().add("correct");
                } else if (answer != null && b.getText().equalsIgnoreCase(answer)) {
                    b.getStyleClass().add("wrong");
                }
            }

            String headline = correct ? "Correct! +" + String.format("%,d", points) + " points"
                    : (answer == null ? "Time's up" : "Not quite");
            Label head = label(headline, "title", correct ? "good" : "bad");
            Label detail = label("The movie was " + movie.title
                    + (!correct && answer != null ? "   (you answered " + answer + ")" : ""));
            Button next = new Button(roundIndex + 1 >= rounds.size() ? "See results" : "Next round");
            next.getStyleClass().add("primary");
            next.setOnAction(e -> nextRound());

            List<String> meta = new ArrayList<>();
            if (movie.genre != null && !movie.genre.isBlank()) meta.add(movie.genre);
            if (movie.category != null && !movie.category.isBlank()) meta.add(movie.category);
            VBox text = new VBox(4, head, detail);
            if (!meta.isEmpty()) text.getChildren().add(label(String.join(" / ", meta), "muted"));

            HBox card = new HBox(16, text, spacer(), next);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("panel");
            card.setPadding(new Insets(14, 18, 14, 18));
            answerBox.getChildren().add(card);

            refreshHud();
        }

        private void showSummary() {
            VBox card = new VBox(12);
            card.getStyleClass().add("panel");
            card.setPadding(new Insets(28));
            card.setMaxWidth(520);
            card.setMaxHeight(Region.USE_PREF_SIZE);

            card.getChildren().addAll(label("Match complete", "title"),
                    label(String.format("%,d points", totalScore), "big"));
            for (int i = 0; i < history.size(); i++) {
                RoundResult r = history.get(i);
                HBox row = new HBox(10, label((i + 1) + ".", "muted"), label(r.title), spacer(),
                        label(r.correct ? "+" + r.points : "0", r.correct ? "good" : "bad"));
                card.getChildren().add(row);
            }

            Button again = new Button("Play again");
            again.getStyleClass().add("primary");
            again.setOnAction(e -> startMatch());
            Button menu = new Button("Back to menu");
            menu.setOnAction(e -> onExit.run());
            HBox buttons = new HBox(10, again, menu);
            buttons.setPadding(new Insets(10, 0, 0, 0));
            card.getChildren().add(buttons);

            summaryLayer.getChildren().setAll(card);
            summaryLayer.setVisible(true);
        }

        // ----------------------------------------------------- answer handling

        private void buildAnswerArea() {
            if (mode == GuessMode.SEARCH) {
                answerValue.setText("");
                answerRow.setVisible(false);
                answerRow.setManaged(false);
                searchBtn.setText("Search for a movie");
                searchBtn.setDisable(false);
                confirmBtn.setDisable(false);
                answerBox.getChildren().setAll(searchPane);
            } else {
                List<Movie> pool = new ArrayList<>(library.allMovies);
                pool.removeIf(o -> o.id == movie.id || o.title.equalsIgnoreCase(movie.title));
                Collections.shuffle(pool);
                // wrong answers from the same genre come first, so they look believable
                pool.sort(Comparator.comparing((Movie o) -> !(movie.genreId != 0 && o.genreId == movie.genreId)));
                List<String> options = new ArrayList<>();
                options.add(movie.title);
                Set<String> seen = new HashSet<>();
                seen.add(movie.title.toLowerCase(Locale.ROOT));
                for (Movie o : pool) {
                    if (options.size() >= 6) break;
                    if (seen.add(o.title.toLowerCase(Locale.ROOT))) options.add(o.title);
                }
                Collections.shuffle(options);

                choiceGrid.getChildren().clear();
                choiceButtons.clear();
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i);
                    Button b = new Button(option);
                    b.getStyleClass().add("choice");
                    b.setMaxWidth(Double.MAX_VALUE);
                    // a multiple choice pick is the final answer straight away
                    b.setOnAction(e -> finishRound(option));
                    GridPane.setHgrow(b, Priority.ALWAYS);
                    choiceGrid.add(b, i % 3, i / 3);
                    choiceButtons.add(b);
                }
                answerBox.getChildren().setAll(choiceGrid);
            }
        }

        private void openSearch() {
            if (!roundActive) return;
            searchField.clear();
            refreshSearchResults();
            searchLayer.setVisible(true);
            searchField.requestFocus();
        }

        private void closeSearch() {
            searchLayer.setVisible(false);
        }

        private void refreshSearchResults() {
            String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            if (!q.isEmpty()) {
                for (String t : library.allTitles) {
                    if (t.toLowerCase(Locale.ROOT).contains(q)) matches.add(t);
                    if (matches.size() >= 8) break;
                }
            }
            searchResults.getItems().setAll(matches);
            searchResults.setPrefHeight(matches.isEmpty() ? 80 : Math.min(8, matches.size()) * 42 + 6);
        }

        /** Search mode: remember the chosen title; the round only ends on Confirm. */
        private void pickAnswer(String title) {
            selectedAnswer = title;
            answerValue.setText(title);
            answerRow.setVisible(true);
            answerRow.setManaged(true);
            searchBtn.setText("Change answer");
            closeSearch();
        }

        // ------------------------------------------------------ video control

        private void loadScene(int idx) {
            sceneIdx = idx;
            viewed.add(idx);
            clipLengthMs = 0;
            finishedFlag = false;
            seeking = false;
            timeline.setTotal(12_000);
            timeline.setUnlocked(stage[idx]);
            timeline.setPosition(0);
            boolean ok = player.media().play(movie.clips.get(idx).getAbsolutePath());
            player.audio().setVolume((int) volume.getValue());
            refreshHud();
            if (!ok) sceneBadge.setText("Could not play this clip");
        }

        private void restartScene() {
            finishedFlag = false;
            player.media().play(movie.clips.get(sceneIdx).getAbsolutePath());
            player.audio().setVolume((int) volume.getValue());
        }

        private void togglePlay() {
            if (player.status().isPlaying()) {
                player.controls().setPause(true);
                return;
            }
            if (finishedFlag) {
                restartScene();
                return;
            }
            if (player.status().time() >= timeline.windowEnd() - 80) {
                player.controls().setTime(0);
            }
            player.controls().play();
        }

        private void unlockMore() {
            int s = stage[sceneIdx];
            if (!roundActive || s >= 3) return;
            long oldEnd = timeline.windowEnd();
            boolean pausedAtEnd = !player.status().isPlaying()
                    && player.status().time() >= oldEnd - 80;
            stage[sceneIdx] = s + 1;
            refreshHud();
            if (pausedAtEnd) player.controls().play();   // carry on into the newly lit part
        }

        private void nextScene() {
            int n = movie.clips.size();
            if (n < 2) return;
            loadScene((sceneIdx + 1) % n);
        }

        private long displayEnd() {
            return stage[sceneIdx] >= 3 && clipLengthMs > 0 ? clipLengthMs : timeline.windowEnd();
        }

        // ----------------------------------------------------------- scoring

        private int maxStage() {
            int max = 1;
            for (int s : stage) max = Math.max(max, s);
            return max;
        }

        private int potentialPoints() {
            int base = STAGE_POINTS[maxStage() - 1];
            int penalty = SCENE_PENALTY * (viewed.size() - 1);
            return Math.max(MIN_CORRECT_POINTS, base - penalty);
        }

        // ---------------------------------------------------------- UI state

        private void refreshHud() {
            if (movie == null) return;
            int n = movie.clips.size();
            int s = stage[sceneIdx];
            timeline.setUnlocked(s);

            sceneBadge.setText("Scene " + (sceneIdx + 1) + " of " + n);
            scoreLabel.setText("Score " + String.format("%,d", totalScore));
            worthLabel.setText(roundActive ? "Worth " + String.format("%,d", potentialPoints()) + " pts" : "");

            if (s >= 3) {
                moreBtn.setText("Full timeline");
            } else {
                moreBtn.setText("More timeline (" + (s == 1 ? "5s" : "full") + ")");
            }
            moreBtn.setDisable(!roundActive || s >= 3);

            boolean freshScene = roundActive && viewed.size() < n;
            sceneBtn.setText(freshScene ? "Another scene (-" + SCENE_PENALTY + ")" : "Next scene");
            sceneBtn.setDisable(n < 2);
        }

        /** Runs every 40 ms: round timer, clip length, play/pause label, window limit. */
        private void tick() {
            if (roundActive) {
                long remainingMs = (deadlineNanos - System.nanoTime()) / 1_000_000L;
                if (remainingMs <= 0) {
                    timerLabel.setText("0:00");
                    finishRound(null);
                } else {
                    timerLabel.setText(fmt(remainingMs + 999));
                    boolean urgent = remainingMs < 10_000;
                    boolean has = timerLabel.getStyleClass().contains("urgent");
                    if (urgent && !has) timerLabel.getStyleClass().add("urgent");
                }
            }

            long len = player.status().length();
            if (len > 0 && len != clipLengthMs) {
                clipLengthMs = len;
                timeline.setTotal(len);
                refreshHud();
            }

            boolean playing = player.status().isPlaying();
            playBtn.setText(playing ? "Pause" : "Play");
            if (seeking) return;

            long end = timeline.windowEnd();
            if (finishedFlag && loopBtn.isSelected()) {
                restartScene();
                return;
            }

            long t = Math.max(0, player.status().time());
            if (playing && t >= end) {
                if (loopBtn.isSelected()) {
                    player.controls().setTime(0);
                } else {
                    player.controls().setPause(true);   // park at the end of the lit part
                }
            }
            long shown = Math.min(t, end);
            timeline.setPosition(shown);
            timeLabel.setText(fmt(shown) + " / " + fmt(displayEnd()));
        }

        void dispose() {
            ticker.stop();
            player.release();
            factory.release();
        }
    }

    // ------------------------------------------------------------- launcher

    private Stage stage;
    private Scene scene;
    private GamePage currentPage;
    private GuessMode mode = GuessMode.SEARCH;
    private final Config config = Config.load();
    private MovieLibrary library = new MovieLibrary();
    private String loadStatus = "Connecting to the database...";
    private Runnable menuRefresh = () -> { };

    @Override
    public void start(Stage primaryStage) {
        com.sun.jna.NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");

        stage = primaryStage;
        stage.setTitle("Guess It");
        stage.setOnCloseRequest(e -> {
            if (currentPage != null) currentPage.dispose();
        });
        showMenu();
        stage.show();
        reloadLibrary();
    }

    /** Reads movies and scenes from the database on a background thread. */
    private void reloadLibrary() {
        loadStatus = "Connecting to the database...";
        menuRefresh.run();
        Thread t = new Thread(() -> {
            MovieLibrary lib = null;
            String err = null;
            try {
                lib = MovieLibrary.load(config);
            } catch (SQLException ex) {
                err = "Couldn't load the database: " + ex.getMessage();
            }
            MovieLibrary loaded = lib;
            String error = err;
            Platform.runLater(() -> {
                if (loaded != null) {
                    library = loaded;
                    loadStatus = null;
                } else {
                    loadStatus = error;
                }
                menuRefresh.run();
            });
        }, "guessit-db");
        t.setDaemon(true);
        t.start();
    }

    private void setRoot(Parent root) {
        if (scene == null) {
            scene = new Scene(root, 1100, 780);
            scene.getStylesheets().add(cssUrl());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }

    private void showMenu() {
        if (currentPage != null) {
            currentPage.dispose();
            currentPage = null;
        }

        Label title = label("Guess It", "title");
        Label subtitle = label("Name the movie from a short clip.", "muted");

        ToggleGroup group = new ToggleGroup();
        ToggleButton searchMode = new ToggleButton("Search");
        ToggleButton choiceMode = new ToggleButton("Multiple choice (6)");
        searchMode.setToggleGroup(group);
        choiceMode.setToggleGroup(group);
        (mode == GuessMode.SEARCH ? searchMode : choiceMode).setSelected(true);

        Label dbLabel = label(config.dbUrl, "muted");
        dbLabel.setWrapText(true);
        Label folderLabel = label("", "muted");
        folderLabel.setWrapText(true);
        Button folderBtn = new Button("Choose clips folder");
        Button reloadBtn = new Button("Reload from database");
        Label info = label("");
        info.setWrapText(true);
        Button start = new Button("Start match");
        start.getStyleClass().add("primary");

        Runnable refresh = () -> {
            folderLabel.setText(config.clipsDir.getAbsolutePath());
            boolean needsSix = mode == GuessMode.MULTIPLE_CHOICE && library.allTitles.size() < 6;
            String missing = library.missingFiles > 0
                    ? " " + library.missingFiles + " scene file(s) listed in the database weren't found in the clips folder."
                    : "";
            if (loadStatus != null) {
                info.setText(loadStatus);
            } else if (library.movies.isEmpty()) {
                info.setText("Connected, but no movie has a playable scene." + missing
                        + " Check scene.video_file and the clips folder.");
            } else if (needsSix) {
                info.setText("Multiple choice needs at least 6 movies in the database.");
            } else {
                info.setText(library.movies.size() + " playable movies, " + library.allTitles.size()
                        + " titles to search. Each match plays "
                        + Math.min(ROUNDS_PER_MATCH, library.movies.size()) + " rounds." + missing);
            }
            start.setDisable(loadStatus != null || library.movies.isEmpty() || needsSix);
        };
        menuRefresh = refresh;

        group.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) {
                o.setSelected(true);   // never allow "no mode"
                return;
            }
            mode = n == searchMode ? GuessMode.SEARCH : GuessMode.MULTIPLE_CHOICE;
            refresh.run();
        });

        folderBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose the clips folder");
            if (config.clipsDir.isDirectory()) chooser.setInitialDirectory(config.clipsDir);
            File picked = chooser.showDialog(stage);
            if (picked != null) {
                config.clipsDir = picked;
                reloadLibrary();
            }
        });
        reloadBtn.setOnAction(e -> reloadLibrary());

        start.setOnAction(e -> {
            currentPage = new GamePage(library, mode, this::showMenu);
            setRoot(currentPage);
        });

        Label rules = label("Each round: 60 seconds. Clips unlock at 3s, then 5s, then the full clip. "
                + "Watching more of the clip, or opening another scene, lowers your points.", "muted");
        rules.setWrapText(true);

        VBox box = new VBox(14, title, subtitle,
                label("Guessing mode"), new HBox(8, searchMode, choiceMode),
                label("Database"), dbLabel,
                label("Clips"), folderLabel, new HBox(8, folderBtn, reloadBtn),
                info, rules, start);
        box.setMaxWidth(560);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.setPadding(new Insets(30));
        box.getStyleClass().add("panel");

        StackPane root = new StackPane(box);
        root.getStyleClass().add("game-root");
        refresh.run();
        setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
