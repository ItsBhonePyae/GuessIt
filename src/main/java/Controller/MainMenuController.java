package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private Region rgMedium;

    @FXML
    private Label lblLogo;

    @FXML
    private Button btnAccount;

    @FXML
    private Button btnSetting;

    @FXML
    private VBox middleSection;

    @FXML
    private VBox vboxContent;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblMainHeadLine;

    @FXML
    private Label lblSubHeadline;

    @FXML
    private Label lblContent;

    @FXML
    private VBox vboxButton;

    @FXML
    private Button btnPlay;

    @FXML
    private Label lblFooterInfo;

    @FXML
    void goToGameSelection(ActionEvent event) throws IOException {
        if(btnAccount.getText().equals("LogIn")){
            SceneManager.switchScene("/fxml/Login.fxml");
        }
    }

    @FXML
    void viewProfile(ActionEvent event) {

    }

    @FXML
    void viewSetting(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnAccount.setText("LogIn");
        btnSetting.setVisible(false);

    }
}
