package com.guessit.actualapp;

import atlantafx.base.theme.PrimerDark;
import com.guessit.actualapp.view.Navigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class AdminApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL view = AdminApplication.class.getResource(
            "/com/guessit/actualapp/admin/AdminView.fxml");
        if (view == null) {
            throw new IOException("Admin view resource was not found.");
        }

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        Navigator.setStage(stage);
        FXMLLoader loader = new FXMLLoader(view);
        Scene scene = new Scene(loader.load(), 1360, 860);
        stage.setTitle("GuessIt | Admin Workspace");
        stage.setMinWidth(1080);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }
}
