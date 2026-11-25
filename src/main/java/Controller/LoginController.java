package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class LoginController {

    @FXML
    private Label lblLogin;

    @FXML
    private Label lblRegister;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private CheckBox CbRememberme;

    @FXML
    private Hyperlink HLForgetPassword;

    @FXML
    private Button btnLogin;

    @FXML
    void clickEmail(ActionEvent event) {

    }

    @FXML
    void clickForgetPassword(ActionEvent event) {

    }

    @FXML
    void clickLogin(ActionEvent event) {

    }

    @FXML
    void clickPassword(ActionEvent event) {

    }

    @FXML
    void goToLogIn(MouseEvent event) throws IOException {
        SceneManager.switchScene("/fxml/Login.fxml");
    }

    @FXML
    void goToRegister(MouseEvent event) throws IOException {
        SceneManager.switchScene("/fxml/Register.fxml");
    }

}
