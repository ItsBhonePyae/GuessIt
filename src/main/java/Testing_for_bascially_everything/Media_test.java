package Testing_for_bascially_everything;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;


@SuppressWarnings("all")
public class Media_test extends Application {

    private MediaPlayer mediaPlayer;
    private Slider volumeSlider;
    private Slider timeSlider;
    private MediaView mediaView;
    private Label statusLabel;
    private Label timeLabel;
    private Button playPauseBtn;
    private Label infoLabel;
    private TextArea logArea;

    public static  void main(String[] args){
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        Button openFileBtn = new Button("Open Video File");
        statusLabel = new Label("Status: No File Loaded");
        HBox topBar = new HBox(10,openFileBtn,statusLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        mediaView = new MediaView();
        mediaView.setFitWidth(640);
        mediaView.setFitHeight(360);
        mediaView.setPreserveRatio(true);

        StackPane mediaPane = new StackPane(mediaView);
        mediaPane.setStyle("-fx-background-color:black;");
        mediaPane.setPrefSize(640,360);

        playPauseBtn = new Button("Play");
        playPauseBtn.setDisable(true);

        Button stopBtn = new Button("Stop");
        stopBtn.setDisable(true);

        timeSlider = new Slider();
        timeSlider.setDisable(true);
        HBox.setHgrow(timeSlider, Priority.ALWAYS);

        timeLabel = new Label("00:00 / 00:00");

        Label volLabel = new Label("Vol: ");
        volumeSlider = new Slider(0,1,0.75);
        volumeSlider.setPrefWidth(100);

        HBox controlBox = new HBox(10,playPauseBtn,stopBtn,timeSlider,timeLabel,volLabel,volumeSlider);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));

        infoLabel = new Label("Resolution: -\nDuration: -\nFormat: -");
        infoLabel.setWrapText(true);

        VBox metadateBox = new VBox(5);
        metadateBox.getChildren().addAll(new Label("Media Info:"),infoLabel);
        metadateBox.setPadding(new Insets(10));
        metadateBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(10);

        VBox rightBox = new VBox(10,metadateBox,new Label("Event Log:"),logArea);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(250);

        VBox centreBox = new VBox(mediaPane,controlBox);
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centreBox);
        root.setRight(rightBox);

        openFileBtn.setOnAction(e -> chooseFile(primaryStage));

        playPauseBtn.setOnAction(e -> {
            if(mediaPlayer == null) return;
            if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING){
                mediaPlayer.pause();
            }
            else {
                mediaPlayer.play();
            }
        });
        stopBtn.setOnAction(e -> {
            if(mediaPlayer != null){
                mediaPlayer.stop();
                log("Media stopped");
            }
        });

        volumeSlider.valueProperty().addListener((obs,oldVal,newVal) -> {
            if(mediaPlayer != null){
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        timeSlider.setOnMousePressed(e -> {
            if(mediaPlayer != null) mediaPlayer.pause();
        });

        timeSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                mediaPlayer.play();
            }
        });


        Scene scene = new Scene(root, 920, 480);

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("Media viewer Test");
        log("Application started. Ready to test media files.");
    }
    private void chooseFile(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Video File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.fxm", "*.flv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null){
            loadMedia(selectedFile);
        }

    }
    private void loadMedia(File file){
        if (mediaPlayer != null){
            mediaPlayer.dispose();
        }
        log("Loading file: "+file.getName());
        statusLabel.setText("Status: Loading...");
        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer  = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.setOnReady(() -> {
                statusLabel.setText("Status: Ready");
                log("Media loaded Successfully");
                log("Duration: "+formatDuration(media.getDuration()));

                timeSlider.setDisable(false);
                timeSlider.setMax(media.getDuration().toSeconds());
                playPauseBtn.setDisable(false);

                infoLabel.setText(String.format(
                        "Resolution: %dx%d\nDuration: %s\nTracks: %d",
                        media.getWidth(),
                        media.getHeight(),
                        formatDuration(media.getDuration()),
                        media.getTracks().size()
                ));

            });

            mediaPlayer.setOnPlaying(() -> {
                playPauseBtn.setText("Pause");
                statusLabel.setText("Status: Playing");
                log("Playback: started");
            });

            mediaPlayer.setOnPaused(() -> {
                playPauseBtn.setText("Play");
                statusLabel.setText("Status: Paused");
                log("Playback: paused");
            });

            mediaPlayer.setOnStopped(() -> {
                playPauseBtn.setText("Play");
                statusLabel.setText("Ststus: Stopped");
                timeSlider.setValue(0);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                playPauseBtn.setText("Play");
                statusLabel .setText("Status: Ended");
                log("Reached end of media.");
                mediaPlayer.stop();
            });

            mediaPlayer.setOnError(() -> {
                statusLabel.setText("Status: Error");
                log("Error: "+mediaPlayer.getError().getMessage());
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if(!timeSlider.isValueChanging()){
                    timeSlider.setValue(newTime.toSeconds());
                }
                timeLabel.setText((formatDuration(newTime)) +" / " + formatDuration(media.getDuration()));
            });
            mediaPlayer.setVolume(volumeSlider.getValue());

        }catch (Exception e) {
                log("Failed to load file: " + e.getMessage());
                statusLabel.setText("Status: Load Error");
            }
    }
    private String formatDuration(Duration duration){
        int seconds = (int) Math.floor(duration.toSeconds() %60);
        int minutes = (int) Math.floor(duration.toMinutes() %60);
        int hours = (int) Math.floor(duration.toHours());

        if(hours >0 ){
            return String.format("%02d:%02d:%02d",hours,minutes,seconds);
        }
        else {
            return String.format("%02d:%02d",minutes,seconds);
        }
    }

    private void log(String message){
        logArea.appendText("[" + java.time.LocalTime.now().toString().substring(0,8)+"]"+message+"\n");
    }
}
