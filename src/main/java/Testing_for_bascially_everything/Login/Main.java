package Testing_for_bascially_everything.Login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        String path =
                "/com.guessit.login/Login.fxml";

        URL fxmlURL = Main.class.getResource(path);


        if (fxmlURL == null) {
            System.out.println("FXML NOT FOUND!");
            System.out.println("Path: " + path);
            throw new RuntimeException("Cannot find Login.fxml");
        }

        System.out.println("FXML FOUND: " + fxmlURL);

        FXMLLoader loader = new FXMLLoader(fxmlURL);

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Guess It - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}