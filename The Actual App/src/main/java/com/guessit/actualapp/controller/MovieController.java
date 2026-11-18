package com.guessit.actualapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.net.URL;

public class MovieController {
    @FXML private Button playButton;
    @FXML private MediaView mediaView;

    private MediaPlayer player;

    @FXML
    public void initialize() {
        try {
            URL url = getClass().getResource("/assets/clips/LordOfTheRings/scene1.mp4");
            if (url == null) url = getClass().getResource("/assets/clips/LordOfTheRings/scene1.mp4");
            if (url != null) {
                Media media = new Media(url.toExternalForm());
                player = new MediaPlayer(media);
                mediaView.setMediaPlayer(player);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePlay() {
        if (player == null) return;
        if (player.getStatus() == MediaPlayer.Status.PLAYING) { player.pause(); playButton.setText("Play"); }
        else { player.play(); playButton.setText("Pause"); }
    }

    @FXML
    private void goHome() {
        try {
            if (player != null) player.stop();
            Stage stage = (Stage) playButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
            stage.setScene(new Scene(root, 960, 640));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
