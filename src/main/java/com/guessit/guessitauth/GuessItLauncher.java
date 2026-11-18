package com.guessit.guessitauth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the new premium GuessIt authentication UI.
 *
 * Run this class (or set it as the Maven javafx:run mainClass) to start
 * the application on GuessItLogin.fxml instead of the default Login screen.
 */
public class GuessItLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/guessit/GuessItLogin.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("GuessIt");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
