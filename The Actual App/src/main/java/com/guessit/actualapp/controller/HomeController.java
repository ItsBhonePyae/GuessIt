package com.guessit.actualapp.controller;

import com.guessit.actualapp.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (Session.currentUser != null) welcomeLabel.setText("Welcome, " + Session.currentUser.getUsername() + "!");
    }

    @FXML
    private void openMovie() { navigate("/fxml/movie.fxml"); }

    @FXML
    private void openSong() { alert("Song category coming soon."); }

    @FXML
    private void openCelebrity() { alert("Celebrity category coming soon."); }

    @FXML
    private void logout() {
        Session.currentUser = null;
        navigate("/fxml/login.fxml");
    }

    private void navigate(String fxml) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.setScene(new Scene(root, 960, 640));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void alert(String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, msg);
        a.showAndWait();
    }
}
