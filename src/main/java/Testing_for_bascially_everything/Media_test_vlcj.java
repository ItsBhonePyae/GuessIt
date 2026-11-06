package Testing_for_bascially_everything;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;

public class Media_test_vlcj extends Application {

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;
    private ImageView imageView;

    private Slider timeSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Label statusLabel;
    private TextArea logArea;
    private Label infoLabel;
    private Button playPauseBtn;

    @Override
    public void start(Stage primaryStage) {
        com.sun.jna.NativeLibrary.addSearchPath(
                "libvlc",
                "C:\\Program Files\\VideoLAN\\VLC"
        );

        primaryStage.setTitle("JavaFX VLCJ Video Media Tester");

        Button openFileBtn = new Button("Open Video File");
        statusLabel = new Label("Status: No file loaded");
        HBox topBox = new HBox(10, openFileBtn, statusLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));

        imageView = new ImageView();
        imageView.setFitWidth(640);
        imageView.setFitHeight(360);
        imageView.setPreserveRatio(true);

        StackPane mediaPane = new StackPane(imageView);
        mediaPane.setStyle("-fx-background-color: black;");
        mediaPane.setPrefSize(640, 360);

        mediaPlayerFactory = new MediaPlayerFactory();
        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        embeddedMediaPlayer.videoSurface().set(
                new uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface(imageView)
        );

        playPauseBtn = new Button("Play");
        playPauseBtn.setDisable(true);

        Button stopBtn = new Button("Stop");
        stopBtn.setDisable(true);

        timeSlider = new Slider(0, 100, 0); // min: 0, max: 100, initial value: 0
        timeSlider.setDisable(true);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);

        timeLabel = new Label("00:00 / 00:00");

        Label volLabel = new Label("Vol:");
        volumeSlider = new Slider(0, 100, 75);
        volumeSlider.setPrefWidth(100);

        HBox controlBox = new HBox(10, playPauseBtn, stopBtn, timeSlider, timeLabel, volLabel, volumeSlider);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));

        infoLabel = new Label("Resolution: -\nDuration: -\nFormat: -");
        infoLabel.setWrapText(true);

        VBox metadataBox = new VBox(5, new Label("Media Info:"), infoLabel);
        metadataBox.setPadding(new Insets(10));
        metadataBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(10);

        VBox rightBox = new VBox(10, metadataBox, new Label("Event Logs:"), logArea);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(250);

        VBox centerBox = new VBox(mediaPane, controlBox);
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centerBox);
        root.setRight(rightBox);

        openFileBtn.setOnAction(e -> chooseFile(primaryStage));

        playPauseBtn.setOnAction(e -> {
            if (embeddedMediaPlayer.status().isPlaying()) {
                embeddedMediaPlayer.controls().pause();
            } else {
                embeddedMediaPlayer.controls().play();
            }
        });

        stopBtn.setOnAction(e -> {
            embeddedMediaPlayer.controls().stop();
            log("Media stopped.");
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            embeddedMediaPlayer.audio().setVolume(newVal.intValue());
        });

        timeSlider.setOnMousePressed(e -> embeddedMediaPlayer.controls().pause());
        timeSlider.setOnMouseReleased(e -> {
            embeddedMediaPlayer.controls().setTime((long) (timeSlider.getValue() * 1000));
            embeddedMediaPlayer.controls().play();
        });

        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> {
                    playPauseBtn.setText("Pause");
                    statusLabel.setText("Status: Playing");
                    log("Playback started.");
                });
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    statusLabel.setText("Status: Paused");
                    log("Playback paused.");
                });
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    statusLabel.setText("Status: Stopped");
                    timeSlider.setValue(0);
                });
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> {
                    playPauseBtn.setText("Play");
                    statusLabel.setText("Status: Ended");
                    log("Reached end of media.");
                });
            }

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                Platform.runLater(() -> {
                    if (newLength > 0) {
                        timeSlider.setMax(newLength / 1000.0);
                        log("Media length detected: " + formatTime(newLength));
                    }
                });
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                Platform.runLater(() -> {
                    if (!timeSlider.isValueChanging()) {
                        timeSlider.setValue(newTime / 1000.0);
                    }
                    long totalLength = embeddedMediaPlayer.status().length();
                    String totalFormatted = totalLength > 0 ? formatTime(totalLength) : "00:00";
                    timeLabel.setText(formatTime(newTime) + " / " + totalFormatted);
                });
            }
        });

        Scene scene = new Scene(root, 920, 480);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            embeddedMediaPlayer.release();
            mediaPlayerFactory.release();
        });

        log("Application started with VLCJ engine. Ready.");
    }

    private void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Video File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Video Files", "*.mp4", "*.m4v", "*.mkv", "*.avi", "*.mov", "*.flv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            loadMedia(selectedFile);
        }
    }

    private void loadMedia(File file) {
        log("Loading file: " + file.getName());
        statusLabel.setText("Status: Loading...");

        boolean success = embeddedMediaPlayer.media().play(file.getAbsolutePath());
        if (success) {
            statusLabel.setText("Status: Ready");
            log("Media loaded successfully via VLCJ.");

            timeSlider.setDisable(false);
            timeSlider.setMax(embeddedMediaPlayer.status().length() / 1000.0);
            playPauseBtn.setDisable(false);
            embeddedMediaPlayer.audio().setVolume((int) volumeSlider.getValue());

            infoLabel.setText(String.format(
                    "File: %s\nDuration: %s",
                    file.getName(),
                    formatTime(embeddedMediaPlayer.status().length())
            ));
        } else {
            log("Failed to load file through VLCJ.");
            statusLabel.setText("Status: Load Error");
        }
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private void log(String message) {
        logArea.appendText("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}