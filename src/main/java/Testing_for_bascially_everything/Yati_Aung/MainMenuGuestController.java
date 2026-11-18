package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuGuestController implements Initializable {

    @FXML
    private Label lblLogo;

    @FXML
    private Button btnAccount;

    @FXML
    private Button btnSetting;

    @FXML
    private VBox vboxContent;

    @FXML
    private VBox vboxButton;

    @FXML
    private VBox middleSection;


    @FXML
    private Label lblDescription;

    @FXML
    private Label lblMainHeadLine;

    @FXML
    private Label lblSubHeadline;

    @FXML
    private Label lblContent;

    @FXML
    private Button btnPlay;

    @FXML
    private Label lblFooterInfo;

    @FXML
    void goToGameSelection(ActionEvent event) {
        try {
            if(!btnAccount.getText().equals("LogIn")) {
                SceneManager.switchScene("/fxml/GameMenuPage.fxml");
            } else{
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("GuessIt");
                alert.setHeaderText("LogIn First");
                alert.setContentText("You need to log in to play");

                alert.showAndWait();
            }
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("GuessIt");
            alert.setHeaderText("Error Found!");
            alert.setContentText(exception.getMessage());

            alert.showAndWait();
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
//        btnAccount.setDisable(true);
        btnSetting.setDisable(true);
        btnAccount.setText("Account");
    }
}
