package com.guessit.actualapp.view;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Swaps the root of the single stage, like the reference test apps do. */
public final class Navigator {

    private static Stage stage;

    private Navigator() {
    }

    public static void setStage(Stage value) {
        stage = value;
    }

    public static void go(Parent root) {
        if (stage == null) {
            return;
        }
        StackPane transition = createTransition(root);
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(transition, 1320, 860));
        } else {
            scene.setRoot(transition);
        }

        FadeTransition reveal = new FadeTransition(Duration.millis(360), root);
        reveal.setFromValue(0.82);
        reveal.setToValue(1);
        reveal.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition rise = new TranslateTransition(Duration.millis(380), root);
        rise.setFromY(8);
        rise.setToY(0);
        rise.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(reveal, rise).play();

        FadeTransition hideLoader = new FadeTransition(Duration.millis(280), transition.getChildren().get(1));
        hideLoader.setFromValue(1);
        hideLoader.setToValue(0);
        hideLoader.setInterpolator(Interpolator.EASE_OUT);
        hideLoader.setOnFinished(event -> transition.getChildren().remove(1));
        new SequentialTransition(new PauseTransition(Duration.millis(140)), hideLoader).play();
    }

    private static StackPane createTransition(Parent page) {
        page.setOpacity(0.82);
        page.setTranslateY(8);

        Region scrim = new Region();
        scrim.setStyle("-fx-background-color: rgba(8,7,6,0.78);");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(34, 34);
        spinner.setMaxSize(34, 34);
        spinner.setStyle("-fx-progress-color: " + Theme.LIME + ";");

        Label label = Theme.label("Loading", 12, javafx.scene.text.FontWeight.NORMAL,
            Theme.SOFT_TEXT);
        VBox loader = new VBox(11, spinner, label);
        loader.setAlignment(Pos.CENTER);
        loader.setPadding(new Insets(16, 22, 16, 22));
        loader.setStyle("-fx-background-color: rgba(20,17,14,0.94);"
            + " -fx-background-radius: 8;");

        StackPane loadingLayer = new StackPane(scrim, loader);
        loadingLayer.setPickOnBounds(true);

        return new StackPane(page, loadingLayer);
    }
}
