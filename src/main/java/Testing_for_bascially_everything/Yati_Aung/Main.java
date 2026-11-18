package Controller;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;

import util.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Application.setUserAgentStylesheet(
                new PrimerDark().getUserAgentStylesheet()
        );

        SceneManager.setStage(stage);

        SceneManager.switchScene("/fxml/MainMenuGuest.fxml");

        stage.setTitle("GuessIt - Movie Guessing Game");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
