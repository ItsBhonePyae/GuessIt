package Testing_for_bascially_everything.Yati_Aung;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Application.setUserAgentStylesheet(
                new PrimerDark().getUserAgentStylesheet()
        );

        java.net.URL fxml = getClass().getResource("MainMenuGuest.fxml");
        if (fxml == null) {
            System.out.println("FXML not found");
            return;
        }
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root);

        stage.setTitle("GuessIt - Movie Guessing Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
