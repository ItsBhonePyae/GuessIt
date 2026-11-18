package Testing_for_bascially_everything.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AdminController {

    @FXML
    private ComboBox<?> assetTypeBox;

    @FXML
    private VBox assetsPage;

    @FXML
    private TableView<?> assetsTable;

    @FXML
    private VBox dashboardPage;

    @FXML
    private VBox logsPage;

    @FXML
    private TableView<?> logsTable;

    @FXML
    private StackPane pageHost;

    @FXML
    private Label statusLabel;

    @FXML
    private Label totalMoviesLabel;

    @FXML
    private Label totalPlaysLabel;

    @FXML
    private Label totalSongsLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private TextField userSearchField;

    @FXML
    private VBox usersPage;

    @FXML
    private TableView<?> usersTable;

    @FXML
    void addAsset(ActionEvent event) {
        System.out.println("Add Asset button clicked!");

    }

    @FXML
    void addUser(ActionEvent event) {
        System.out.println("Add User button clicked!");

    }

    @FXML
    void deleteAsset(ActionEvent event) {
        System.out.println("Delete Asset button clicked!");

    }

    @FXML
    void deleteUser(ActionEvent event) {
        System.out.println("Delete User button clicked!");

    }

    @FXML
    void playGame(ActionEvent event) {
        System.out.println("Play Game button  clicked!");

    }

    @FXML
    void showAssets(ActionEvent event) {

        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);

        usersPage.setVisible(false);
        usersPage.setManaged(false);

        logsPage.setVisible(false);
        logsPage.setManaged(false);

        assetsPage.setVisible(true);
        assetsPage.setManaged(true);

        statusLabel.setText("Assets Management");

    }

    @FXML
    void showDashboard(ActionEvent event) {

        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);

        usersPage.setVisible(false);
        usersPage.setManaged(false);

        logsPage.setVisible(false);
        logsPage.setManaged(false);

        assetsPage.setVisible(true);
        assetsPage.setManaged(true);

        statusLabel.setText("Dashboard");


    }

    @FXML
    void showLogs(ActionEvent event) {
        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);

        usersPage.setVisible(false);
        usersPage.setManaged(false);

        logsPage.setVisible(false);
        logsPage.setManaged(false);

        assetsPage.setVisible(true);
        assetsPage.setManaged(true);

        statusLabel.setText(" Admin Activities Log");

    }

    @FXML
    void showUsers(ActionEvent event) {

        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);

        usersPage.setVisible(false);
        usersPage.setManaged(false);

        logsPage.setVisible(false);
        logsPage.setManaged(false);

        assetsPage.setVisible(true);
        assetsPage.setManaged(true);

        statusLabel.setText("Users Management");

    }

}
