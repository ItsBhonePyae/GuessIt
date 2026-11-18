package application;

import com.guessit.util.SceneSwitcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for the landing page (view/guessit/GuessItLogin.fxml).
 *
 * The landing page only offers navigation to the Login and Register forms;
 * all authentication logic lives in GuessItLoginController / GuessItRegisterController.
 */
public class GuessItController {

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private void handleLogin() {
        SceneSwitcher.switchTo(loginButton, "/view/guessit/GuessItLogin.fxml");
    }

    @FXML
    private void handleRegister() {
        SceneSwitcher.switchTo(registerButton, "/view/guessit/GuessItRegister.fxml");
    }
}
