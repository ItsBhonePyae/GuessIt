package com.guessit.login;

import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for fxml/login.fxml.
 *
 * Note: until the MySQL database is connected, any non-empty credentials
 * are accepted and lead to the dashboard.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // TODO: replace with UserDao.login(username, password) + BCrypt check
        Session.setUsername(username);
        SceneSwitcher.switchTo(loginButton, "/fxml/dashboard.fxml");
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guess It");
        alert.setHeaderText(null);
        alert.setContentText("Forgot Password feature will be added later.");
        alert.showAndWait();
    }

    @FXML
    private void goToRegister() {
        SceneSwitcher.switchTo(usernameField, "/fxml/register.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
