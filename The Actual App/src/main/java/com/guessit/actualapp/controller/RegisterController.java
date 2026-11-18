package com.guessit.actualapp.controller;

import com.guessit.actualapp.dao.UserDao;
import com.guessit.actualapp.util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleRegister() {
        String u = usernameField.getText().trim();
        String e = emailField.getText().trim();
        String p = passwordField.getText();
        if (u.isEmpty() || e.isEmpty() || p.isEmpty()) { statusLabel.setText("Fill all fields."); return; }
        if (!DBUtil.isAvailable()) { statusLabel.setText("Database unavailable."); return; }
        if (UserDao.register(u, e, p)) {
            statusLabel.setText("Account created. Go to login.");
        } else {
            statusLabel.setText("Registration failed (duplicate?).");
        }
    }

    @FXML
    private void goLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(root, 960, 640));
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
