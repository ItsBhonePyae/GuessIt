package com.guessit.guessitauth;

import com.guessit.dao.RememberTokenDao;
import com.guessit.dao.UserDao;
import com.guessit.model.User;
import com.guessit.util.DBUtil;
import com.guessit.util.LocalTokenStore;
import com.guessit.util.PasswordUtil;
import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;
import com.guessit.util.TokenGenerator;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Controller for view/guessit/GuessItLogin.fxml.
 *
 * Reuses the existing authentication building blocks (UserDao, PasswordUtil,
 * Session, SceneSwitcher) instead of duplicating business logic. Adds the
 * "remember me" token flow on top.
 */
public class GuessItLoginController {

    @FXML
    private VBox authCard;

    @FXML
    private Pane overlayPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheck;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private final UserDao userDao = new UserDao();
    private final RememberTokenDao tokenDao = new RememberTokenDao();

    @FXML
    private void initialize() {
        GuessItAnimation.slideUp(authCard, 0.0);
        GuessItAnimation.hoverGlow(loginButton);
        GuessItAnimation.breathe(overlayPane, 0.85, 1.0, 4.5);

        // Try to restore a "remember me" session without blocking the UI.
        Platform.runLater(this::tryAutoLogin);
    }

    private void tryAutoLogin() {
        try {
            String stored = LocalTokenStore.load();
            if (stored == null || !stored.contains("::")) {
                return;
            }
            String[] parts = stored.split("::", 2);
            String username = parts[0];
            String rawToken = parts[1];

            if (!DBUtil.isAvailable()) {
                return;
            }
            User user = userDao.findByUsernameOrEmail(username);
            if (user == null) {
                LocalTokenStore.delete();
                return;
            }
            if (tokenDao.findValid(user.getId(), rawToken) != null) {
                Session.setUsername(user.getUsername());
                Session.setEmail(user.getEmail());
                Session.setRole(user.getRole());
                SceneSwitcher.switchTo(authCard, "/view/2fa_verification.fxml");
            } else {
                LocalTokenStore.delete();
            }
        } catch (Throwable t) {
            // Auto-login is best-effort: never let it break startup.
            System.err.println("Auto-login skipped: " + t);
            try {
                LocalTokenStore.delete();
            } catch (Throwable ignored) {
            }
        }
    }

    @FXML
    private void handleLogin() {
        String login = usernameField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Please enter both username/email and password.");
            return;
        }

        // Demo fallback when MySQL is not reachable.
        if (!DBUtil.isAvailable()) {
            Session.setUsername(login);
            Session.setRole("PLAYER");
            SceneSwitcher.switchTo(loginButton, "/view/2fa_verification.fxml");
            return;
        }

        try {
            User user = userDao.findByUsernameOrEmail(login);
            if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
                showError("Invalid username/email or password.");
                return;
            }

            Session.setUsername(user.getUsername());
            Session.setEmail(user.getEmail());
            Session.setRole(user.getRole());

            // "Remember me": issue a persistent token.
            if (rememberCheck.isSelected()) {
                String token = TokenGenerator.newToken();
                tokenDao.create(user.getId(), token);
                LocalTokenStore.save(user.getUsername(), token);
            } else {
                tokenDao.deleteByUserId(user.getId());
                LocalTokenStore.delete();
            }

            SceneSwitcher.switchTo(loginButton, "/view/2fa_verification.fxml");
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
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
        SceneSwitcher.switchTo(usernameField, "/view/guessit/GuessItRegister.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
