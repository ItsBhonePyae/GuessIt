
package Testing_for_bascially_everything.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import javafx.fxml.FXMLLoader;

public class mainwindow extends Application {

    @Override
    public void start(Stage stage) throws Exception {


        URL fxmlURL = getClass().getResource(
                "/Testing_for_bascially_everything/View/AdminView.fxml"
        );

        System.out.println("FXML URL: " + fxmlURL);

        if (fxmlURL == null) {
            System.out.println("FXML FILE NOT FOUND!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Scene scene = new Scene(loader.load(), 1200, 760);


        stage.setTitle("Guessit Admin Dashboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
