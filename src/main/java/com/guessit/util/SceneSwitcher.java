package com.guessit.util;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Switches the current window to another FXML view.
 *
 * Usage from any controller event handler:
 *   SceneSwitcher.switchTo(eventSource, "/fxml/register.fxml");
 */
public final class SceneSwitcher {

    private SceneSwitcher() {
    }

    /**
     * Loads the given FXML (absolute path on the classpath) and replaces
     * the scene of the window that contains the given node.
     */
    public static void switchTo(Node source, String fxmlPath) {
        try {
            URL view = SceneSwitcher.class.getResource(fxmlPath);
            if (view == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(view);
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Failed to load " + fxmlPath + ": " + e.getMessage());
        }
    }
}
