package util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage stage;

    // Initialize the main window
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    // Load and show a screen in one step
    public static FXMLLoader switchScene(String fxmlPath)
            throws IOException {

        FXMLLoader loader = loadScene(fxmlPath);
        showScene(loader.getRoot());
        return loader;
    }

    // Load a screen without showing it, so its controller can be prepared first
    public static FXMLLoader loadScene(String fxmlPath)
            throws IOException {

        URL resource = SceneManager.class.getResource(fxmlPath);

        if (resource == null) {
            throw new IOException(
                    "FXML file not found: " + fxmlPath
            );
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.load();
        return loader;
    }

    // Show an already loaded screen
    public static void showScene(Parent root) {

        if (stage == null) {
            throw new IllegalStateException(
                    "SceneManager has not been initialized."
            );
        }

        Scene scene = stage.getScene();

        // Create the first Scene
        if (scene == null) {
            scene = new Scene(root, 1000, 650);
            stage.setScene(scene);
        } else {
            // Reuse the existing Scene
            scene.setRoot(root);
        }

        // Fade animation
        root.setOpacity(0);

        FadeTransition fade = new FadeTransition(
                Duration.millis(300), root
        );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
