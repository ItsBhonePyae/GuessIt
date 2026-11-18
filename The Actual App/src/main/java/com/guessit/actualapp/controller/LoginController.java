package com.guessit.actualapp.controller;

import com.guessit.actualapp.dao.UserDao;
import com.guessit.actualapp.model.User;
import com.guessit.actualapp.session.Session;
import com.guessit.actualapp.util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        if (u.isEmpty() || p.isEmpty()) { statusLabel.setText("Enter username and password."); return; }
        if (!DBUtil.isAvailable()) { statusLabel.setText("Database unavailable."); return; }
        User user = UserDao.login(u, p);
        if (user == null) { statusLabel.setText("Invalid credentials."); return; }
        Session.currentUser = user;
        goHome();
    }

    @FXML
    private void goRegister() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            stage.setScene(new Scene(root, 960, 640));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void goHome() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
            stage.setScene(new Scene(root, 960, 640));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
