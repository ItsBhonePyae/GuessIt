package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class RegisterController {

    @FXML
    private Label lblLogin;

    @FXML
    private Label lblRegister;

    @FXML
    private HBox usernameWrap;

    @FXML
    private TextField usernameField;

    @FXML
    private HBox emailWrap;

    @FXML
    private TextField emailField;

    @FXML
    private HBox passwordWrap;

    @FXML
    private PasswordField passwordField;

    @FXML
    private HBox confirmWrap;

    @FXML
    private PasswordField confirmField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button createButton;

    @FXML
    void goToLogIn(MouseEvent event) throws IOException {
        SceneManager.switchScene("/fxml/Login.fxml");
    }

    @FXML
    void goToRegister(MouseEvent event) throws IOException {
        SceneManager.switchScene("/fxml/Rgister.fxml");
    }

    @FXML
    void handleRegister(ActionEvent event) {

    }

}
