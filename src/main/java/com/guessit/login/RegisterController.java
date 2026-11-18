package com.guessit.login;

import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for view/register.fxml.
 *
 * Note: until the MySQL database is connected, registration only validates
 * the input and continues to OTP verification — no account is stored yet.
 */
public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            showStatus("Please fill in every field.", false);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showStatus("Please enter a valid email address.", false);
            return;
        }
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters.", false);
            return;
        }
        if (!password.equals(confirm)) {
            showStatus("Passwords do not match.", false);
            return;
        }

        // TODO: replace with UserDao.register(...) + BCrypt.hashpw(password)
        Session.setUsername(username);
        Session.setEmail(email);
        // Account created -> verify the email with a one-time code
        SceneSwitcher.switchTo(registerButton, "/view/otp_verification.fxml");
    }

    @FXML
    private void goToLogin() {
        SceneSwitcher.switchTo(usernameField, "/view/login.fxml");
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label");
        if (success) {
            statusLabel.getStyleClass().add("success-label");
        }
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
