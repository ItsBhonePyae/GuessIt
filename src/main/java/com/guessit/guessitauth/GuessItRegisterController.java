package com.guessit.guessitauth;

import com.guessit.dao.UserDao;
import com.guessit.util.DBUtil;
import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Controller for view/guessit/GuessItRegister.fxml.
 *
 * Reuses UserDao for persistence and Session/SceneSwitcher for navigation.
 */
public class GuessItRegisterController {

    @FXML
    private VBox authCard;

    @FXML
    private Pane overlayPane;

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

    private final UserDao userDao = new UserDao();

    @FXML
    private void initialize() {
        GuessItAnimation.slideUp(authCard, 0.0);
        GuessItAnimation.hoverGlow(registerButton);
        GuessItAnimation.breathe(overlayPane, 0.85, 1.0, 4.5);
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
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

        // Demo fallback when MySQL is not reachable.
        if (!DBUtil.isAvailable()) {
            Session.setUsername(username);
            Session.setEmail(email);
            Session.setRole("PLAYER");
            SceneSwitcher.switchTo(registerButton, "/view/otp_verification.fxml");
            return;
        }

        try {
            if (userDao.usernameOrEmailExists(username, email)) {
                showStatus("Username or email is already taken.", false);
                return;
            }
            int id = userDao.register(fullName, email, username, password);
            if (id <= 0) {
                showStatus("Registration failed. Please try again.", false);
                return;
            }

            Session.setUsername(username);
            Session.setEmail(email);
            Session.setRole("PLAYER");
            SceneSwitcher.switchTo(registerButton, "/view/otp_verification.fxml");
        } catch (Exception e) {
            showStatus("Registration error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void goToLogin() {
        SceneSwitcher.switchTo(usernameField, "/view/guessit/GuessItLogin.fxml");
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
