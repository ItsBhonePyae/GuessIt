package com.guessit.login;

import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for view/2fa_verification.fxml.
 *
 * Demo mode: any 6-digit code is accepted.
 */
public class TwoFactorController {

    @FXML
    private Label statusLabel;

    @FXML
    private TextField otpField;

    @FXML
    private Button verifyButton;

    @FXML
    private void handleVerify() {
        String code = otpField.getText().trim();

        if (!code.matches("\\d{6}")) {
            showStatus("Please enter the 6-digit code from your authenticator app.");
            return;
        }

        // TODO: validate against the user's stored 2FA secret
        String target = "ADMIN".equalsIgnoreCase(Session.getRole())
                ? "/view/admin_dashboard.fxml"
                : "/view/player_dashboard.fxml";
        SceneSwitcher.switchTo(verifyButton, target);
    }

    @FXML
    private void goToLogin() {
        SceneSwitcher.switchTo(otpField, "/view/login.fxml");
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
