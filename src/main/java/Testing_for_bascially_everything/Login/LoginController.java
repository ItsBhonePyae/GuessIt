package Testing_for_bascially_everything.Login;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import animatefx.animation.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheck;


    // Login Button

    @FXML
    private void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {

            showAlert("Please enter username and password!");

        } else {

            showAlert("Welcome to Guess It!");

        }
    }


    // Register Link

    @FXML
    private void openRegister() {

        showAlert("Register Page will open here.");

    }


    // Forgot Password

    @FXML
    private void forgotPassword() {

        showAlert("Forgot Password feature.");

    }


    // Alert Message

    private void showAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Guess It");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @FXML
    private VBox loginForm;

    @FXML
    public void initialize() {

        // Login Form Animation
        new FadeIn(loginForm).play();

    }
}