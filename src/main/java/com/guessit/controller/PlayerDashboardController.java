package com.guessit.controller;

import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for view/player_dashboard.fxml.
 */
public class PlayerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        String user = Session.getUsername();
        if (user != null && !user.isEmpty()) {
            welcomeLabel.setText("Welcome, " + user + "!");
        }
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        SceneSwitcher.switchTo(logoutButton, "/view/login.fxml");
    }
}
