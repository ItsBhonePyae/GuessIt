package com.guessit.actualapp.view;

import com.guessit.actualapp.model.Session;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class MainMenuGuestController {

    @FXML
    private StackPane root;
    @FXML
    private VBox middleSection;
    @FXML
    private Button btnAccount;
    @FXML
    private Button btnSetting;
    @FXML
    private Button btnPlay;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblMainHeadLine;
    @FXML
    private Label lblSubHeadline;
    @FXML
    private Label lblContent;

    private ScaleTransition backdropMotion;

    public static Parent load() throws IOException {
        URL fxml = MainMenuGuestController.class.getResource("MainMenuGuest.fxml");
        if (fxml == null) {
            throw new IOException("MainMenuGuest.fxml was not found.");
        }
        FXMLLoader loader = new FXMLLoader(fxml);
        return loader.load();
    }

    public static void show() {
        try {
            Navigator.go(load());
        } catch (IOException error) {
            throw new IllegalStateException("Could not load the GuessIt landing page.", error);
        }
    }

    @FXML
    private void initialize() {
        Region photo = Theme.photoBackdrop("/assets/backgrounds/cinema-auditorium.jpg");
        root.getChildren().add(0, photo);

        Region scrim = new Region();
        scrim.setMouseTransparent(true);
        scrim.setStyle("-fx-background-color: linear-gradient(to right,"
            + " rgba(8,10,13,0.58) 0%, rgba(8,10,13,0.48) 40%,"
            + " rgba(8,10,13,0.38) 72%, rgba(8,10,13,0.30) 100%);");
        root.getChildren().add(1, scrim);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        backdropMotion = new ScaleTransition(Duration.seconds(28), photo);
        backdropMotion.setFromX(1);
        backdropMotion.setFromY(1);
        backdropMotion.setToX(1.035);
        backdropMotion.setToY(1.035);
        backdropMotion.setInterpolator(Interpolator.EASE_BOTH);
        backdropMotion.setAutoReverse(true);
        backdropMotion.setCycleCount(ScaleTransition.INDEFINITE);
        root.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                backdropMotion.stop();
            } else {
                backdropMotion.playFromStart();
            }
        });

        if (Session.currentUser == null) {
            btnAccount.setText("Account");
            lblDescription.setText("A movie quiz");
            lblMainHeadLine.setText("Can you name the movie?");
            lblSubHeadline.setText("One clip at a time.");
            lblContent.setText("Watch a short scene and name the film before time runs out.");
            btnPlay.setText("Start playing");
        } else {
            String username = Session.currentUser.getUsername();
            btnAccount.setText("Account");
            lblDescription.setText("Welcome back");
            lblMainHeadLine.setText("Ready for another round?");
            lblSubHeadline.setText("Choose your game mode.");
            lblContent.setText("Pick a category and see how many you can get right.");
            btnPlay.setText("Choose a game");
        }
        middleSection.setAlignment(Pos.CENTER);
        middleSection.setPrefWidth(650);
        middleSection.setMaxWidth(650);
        centerLabel(lblDescription);
        centerLabel(lblMainHeadLine);
        centerLabel(lblSubHeadline);
        centerLabel(lblContent);
    }

    private void centerLabel(Label label) {
        label.setMinWidth(650);
        label.setPrefWidth(650);
        label.setMaxWidth(650);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
    }

    @FXML
    private void viewProfile() {
        if (Session.currentUser == null) {
            Navigator.go(new LoginView());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account");
            alert.setHeaderText(null);
            alert.setContentText("Signed in as " + Session.currentUser.getUsername() + ".");
            alert.showAndWait();
        }
    }

    @FXML
    private void viewSetting() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText(null);
        alert.setContentText("Settings are not available yet.");
        alert.showAndWait();
    }

    @FXML
    private void goToGameSelection() {
        if (Session.currentUser == null) {
            Navigator.go(new LoginView());
        } else {
            Navigator.go(new MenuView(Session.currentUser.getUsername()));
        }
    }
}
