package Controller;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {



    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(
                new PrimerDark().getUserAgentStylesheet()
        );
        SceneManager.setStage(stage);
        SceneManager.switchScene("/fxml/MainMenu.fxml");

        stage.setTitle("GuessIt - Movie Guessing Game");

        stage.show();
    }
}
