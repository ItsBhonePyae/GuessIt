package Testing_for_bascially_everything;
//AtlantaFX_video_playing_test



import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.Styles;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * JavaFX video player: vlcj for playback, AtlantaFX for the theme.
 * Pick a video (or a folder) and every video in that folder is queued and played in order.
 */
public class AtlantaFX_video_playing_test extends Application {

    private static final Set<String> VIDEO_EXTENSIONS =
            Set.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg", "ts");

    // Material-style 24x24 SVG paths: drawn as shapes, so they never depend on installed fonts
    private static final String ICON_PLAY = "M8 5v14l11-7z";
    private static final String ICON_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";
    private static final String ICON_PREV = "M6 6h2v12H6zm3.5 6l8.5 6V6z";
    private static final String ICON_NEXT = "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z";
    private static final String ICON_VOLUME =
            "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z";

    private boolean released = false;

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer player;

    private final List<Path> playlist = new ArrayList<>();
    private int index = -1;

    private final ListView<String> playlistView = new ListView<>();
    private final Button playPauseBtn = new Button();
    private final Slider timeSlider = new Slider(0, 1, 0);
    private final Label currentLabel = new Label("00:00");
    private final Label totalLabel = new Label("00:00");
    private final Label titleLabel = new Label("No video loaded");

    /** true while the slider is being set by the player (not by the user) */
    private boolean updatingFromPlayer = false;
    /** true while the user drags the slider */
    private boolean userSeeking = false;

    private AnimationTimer timer;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // software decoding avoids the h264 "get_buffer() failed" errors seen with hardware decoding + callback rendering
        factory = new MediaPlayerFactory("--avcodec-hw=none", "--no-video-title-show");
        player = factory.mediaPlayers().newEmbeddedMediaPlayer();

        // ---------- Video surface ----------
        ImageView videoView = new ImageView();
        videoView.setPreserveRatio(true);
        player.videoSurface().set(new ImageViewVideoSurface(videoView));

        StackPane videoPane = new StackPane(videoView);
        videoPane.setStyle("-fx-background-color: black;");
        videoPane.setMinSize(0, 0);
        videoView.fitWidthProperty().bind(videoPane.widthProperty());
        videoView.fitHeightProperty().bind(videoPane.heightProperty());
        videoPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                stage.setFullScreen(!stage.isFullScreen());
            } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                togglePlayPause();
            }
        });

        // ---------- Playlist ----------
        playlistView.setPrefWidth(260);
        playlistView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                int i = playlistView.getSelectionModel().getSelectedIndex();
                if (i >= 0) playIndex(i);
            }
        });

        // ---------- Controls ----------
        Button openFileBtn = new Button("Open file");
        openFileBtn.setOnAction(e -> chooseFile(stage));
        Button openFolderBtn = new Button("Open folder");
        openFolderBtn.setOnAction(e -> chooseFolder(stage));

        Button prevBtn = new Button();
        prevBtn.setGraphic(icon(prevBtn, ICON_PREV));
        prevBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
        prevBtn.setOnAction(e -> previous());

        playPauseBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);
        setPlaying(false);
        playPauseBtn.setOnAction(e -> togglePlayPause());

        Button nextBtn = new Button();
        nextBtn.setGraphic(icon(nextBtn, ICON_NEXT));
        nextBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
        nextBtn.setOnAction(e -> next());

        Slider volume = new Slider(0, 100, 80);
        volume.setPrefWidth(110);
        volume.valueProperty().addListener((o, a, b) -> player.audio().setVolume(b.intValue()));
        Label volIcon = new Label();
        volIcon.setGraphic(icon(volIcon, ICON_VOLUME));

        timeSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        currentLabel.getStyleClass().add(Styles.TEXT_MUTED);
        totalLabel.getStyleClass().add(Styles.TEXT_MUTED);
        titleLabel.getStyleClass().add(Styles.TEXT_BOLD);

        // user interaction with the time slider
        timeSlider.valueChangingProperty().addListener((obs, was, isChanging) -> {
            userSeeking = isChanging;
            if (!isChanging) {
                player.controls().setPosition((float) timeSlider.getValue());
            }
        });
        // click on the track (not a drag)
        timeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (updatingFromPlayer) return;
            updateTimeLabels(newV.doubleValue());
            if (!timeSlider.isValueChanging()) {
                player.controls().setPosition(newV.floatValue());
            }
        });

        HBox timeRow = new HBox(10, currentLabel, timeSlider, totalLabel);
        timeRow.setAlignment(Pos.CENTER);

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);
        HBox buttonRow = new HBox(8, openFileBtn, openFolderBtn, spacerL,
                prevBtn, playPauseBtn, nextBtn, spacerR, volIcon, volume);
        buttonRow.setAlignment(Pos.CENTER);

        VBox controls = new VBox(8, titleLabel, timeRow, buttonRow);
        controls.setPadding(new Insets(10, 14, 12, 14));

        BorderPane root = new BorderPane();
        root.setCenter(videoPane);
        root.setRight(playlistView);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1100, 680);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                togglePlayPause();
                e.consume();
            } else if (e.getCode() == KeyCode.RIGHT) {
                player.controls().skipTime(5000);
            } else if (e.getCode() == KeyCode.LEFT) {
                player.controls().skipTime(-5000);
            } else if (e.getCode() == KeyCode.N) {
                next();
            } else if (e.getCode() == KeyCode.P) {
                previous();
            }
        });

        // ---------- Player events ----------
        player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mp) {
                Platform.runLater(() -> setPlaying(true));
            }

            @Override
            public void paused(MediaPlayer mp) {
                Platform.runLater(() -> setPlaying(false));
            }

            @Override
            public void stopped(MediaPlayer mp) {
                Platform.runLater(() -> setPlaying(false));
            }

            @Override
            public void finished(MediaPlayer mp) {
                // never call the player from its own event thread: hop to the FX thread
                Platform.runLater(() -> {
                    if (index + 1 < playlist.size()) {
                        playIndex(index + 1);
                    } else {
                        setPlaying(false);
                    }
                });
            }
        });

        // ---------- Smooth slider: updated every rendered frame ----------
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (userSeeking) return;
                long length = player.status().length();
                if (length <= 0) return;
                double pos = Math.max(0, Math.min(1, player.status().position()));
                updatingFromPlayer = true;
                timeSlider.setValue(pos);
                updatingFromPlayer = false;
                updateTimeLabels(pos);
            }
        };
        timer.start();

        stage.setTitle("vlcj Video Player");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();
    }

    // ------------------------------------------------------------------ playlist

    private void chooseFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a video");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Video files", VIDEO_EXTENSIONS.stream().map(e -> "*." + e).toList()));
        File file = fc.showOpenDialog(stage);
        if (file == null) return;
        loadFolder(file.getParentFile().toPath(), file.toPath());
    }

    private void chooseFolder(Stage stage) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose a folder with videos");
        File dir = dc.showDialog(stage);
        if (dir == null) return;
        loadFolder(dir.toPath(), null);
    }

    /** Fills the playlist with every video in the folder and starts at {@code startFile} (or the first). */
    private void loadFolder(Path folder, Path startFile) {
        List<Path> videos;
        try (Stream<Path> s = Files.list(folder)) {
            videos = s.filter(Files::isRegularFile)
                    .filter(p -> VIDEO_EXTENSIONS.contains(extension(p)))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT),
                            AtlantaFX_video_playing_test::naturalCompare))
                    .toList();
        } catch (IOException ex) {
            titleLabel.setText("Could not read folder: " + ex.getMessage());
            return;
        }
        if (videos.isEmpty()) {
            titleLabel.setText("No videos found in " + folder);
            return;
        }

        playlist.clear();
        playlist.addAll(videos);
        playlistView.getItems().setAll(videos.stream().map(p -> p.getFileName().toString()).toList());

        int start = 0;
        if (startFile != null) {
            int found = playlist.indexOf(startFile);
            if (found >= 0) start = found;
        }
        playIndex(start);
    }

    private void playIndex(int i) {
        if (i < 0 || i >= playlist.size()) return;
        index = i;
        Path p = playlist.get(i);
        titleLabel.setText(p.getFileName().toString());
        playlistView.getSelectionModel().select(i);
        playlistView.scrollTo(i);
        player.media().play(p.toAbsolutePath().toString());
    }

    private void next() {
        if (index + 1 < playlist.size()) playIndex(index + 1);
    }

    /** Like most players: if more than 3s in, restart the current video; otherwise go to the previous one. */
    private void previous() {
        if (playlist.isEmpty()) return;
        if (player.status().time() > 3000 || index == 0) {
            player.controls().setTime(0);
        } else {
            playIndex(index - 1);
        }
    }

    private void togglePlayPause() {
        if (playlist.isEmpty()) return;
        if (player.status().isPlaying()) {
            player.controls().pause();
        } else if (player.status().length() > 0 || player.status().isPlayable()) {
            player.controls().play();
        } else {
            playIndex(Math.max(index, 0));
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Icon whose color always follows the owner's text color (so it matches the theme and button state). */
    private static SVGPath icon(Labeled owner, String content) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.fillProperty().bind(owner.textFillProperty());
        return path;
    }

    private void setPlaying(boolean playing) {
        playPauseBtn.setGraphic(icon(playPauseBtn, playing ? ICON_PAUSE : ICON_PLAY));
    }

    private void updateTimeLabels(double position) {
        long length = player.status().length();
        currentLabel.setText(format((long) (position * length)));
        totalLabel.setText(format(length));
    }

    private static String format(long ms) {
        long totalSeconds = Math.max(0, ms) / 1000;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    private static String extension(Path p) {
        String name = p.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** "ep2" sorts before "ep10". */
    private static int naturalCompare(String a, String b) {
        int i = 0, j = 0;
        while (i < a.length() && j < b.length()) {
            char ca = a.charAt(i), cb = b.charAt(j);
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int si = i, sj = j;
                while (i < a.length() && Character.isDigit(a.charAt(i))) i++;
                while (j < b.length() && Character.isDigit(b.charAt(j))) j++;
                java.math.BigInteger na = new java.math.BigInteger(a.substring(si, i));
                java.math.BigInteger nb = new java.math.BigInteger(b.substring(sj, j));
                int c = na.compareTo(nb);
                if (c != 0) return c;
            } else {
                if (ca != cb) return Character.compare(ca, cb);
                i++;
                j++;
            }
        }
        return Integer.compare(a.length() - i, b.length() - j);
    }

    private void shutdown() {
        // called from both setOnCloseRequest and stop(); releasing twice crashes natively
        if (released) return;
        released = true;
        if (timer != null) timer.stop();
        if (player != null) player.release();
        if (factory != null) factory.release();
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}