package com.guessit.login;

import com.guessit.util.SceneSwitcher;
import com.guessit.util.Session;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Controller for view/otp_verification.fxml.
 *
 * Demo mode: any 6-digit code is rejected except 123456, and no email is
 * actually sent. Replace with real OTP storage/verification later.
 */
public class OtpVerificationController {

    private static final int RESEND_SECONDS = 60;
    private static final String DEMO_CODE = "123456";

    @FXML
    private Label emailLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private TextField otpField;

    @FXML
    private Button verifyButton;

    @FXML
    private Button resendButton;

    private Timeline countdown;
    private int secondsLeft;

    @FXML
    private void initialize() {
        String email = Session.getEmail();
        if (email != null && !email.isEmpty()) {
            emailLabel.setText("Enter the 6-digit code we sent to " + email);
        }
        startCountdown();
    }

    @FXML
    private void handleVerify() {
        String code = otpField.getText().trim();

        if (!code.matches("\\d{6}")) {
            showStatus("Please enter the 6-digit code.", false);
            return;
        }
        if (!DEMO_CODE.equals(code)) {
            showStatus("Invalid code. Please try again (demo code: 123456).", false);
            return;
        }

        // TODO: persist the verified account via UserDao
        stopCountdown();
        SceneSwitcher.switchTo(verifyButton, "/view/login.fxml");
    }

    @FXML
    private void handleResend() {
        otpField.clear();
        startCountdown();
        showStatus("A new code has been sent (demo code: 123456).", true);
    }

    @FXML
    private void goToLogin() {
        stopCountdown();
        SceneSwitcher.switchTo(otpField, "/view/login.fxml");
    }

    // ------------------------------------------------------------ timer

    private void startCountdown() {
        stopCountdown();
        secondsLeft = RESEND_SECONDS;
        resendButton.setDisable(true);
        updateTimerLabel();

        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsLeft--;
            if (secondsLeft <= 0) {
                stopCountdown();
                resendButton.setDisable(false);
                timerLabel.setText("You can resend the code now.");
            } else {
                updateTimerLabel();
            }
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void stopCountdown() {
        if (countdown != null) {
            countdown.stop();
            countdown = null;
        }
    }

    private void updateTimerLabel() {
        timerLabel.setText("You can resend the code in " + secondsLeft + "s");
    }

    // ------------------------------------------------------------ helpers

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
