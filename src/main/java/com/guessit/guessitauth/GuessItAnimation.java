package com.guessit.guessitauth;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Small helper that applies the premium animations used by the new GuessIt
 * authentication screens (fade-in, slide-up, hover glow).
 */
public final class GuessItAnimation {

    private GuessItAnimation() {
    }

    /** Fades a node in after the given delay (seconds). */
    public static void fadeIn(Node node, double delaySeconds) {
        if (node == null) {
            return;
        }
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(700), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.seconds(delaySeconds));
        ft.play();
    }

    /** Slides a node up while fading it in. */
    public static void slideUp(Node node, double delaySeconds) {
        if (node == null) {
            return;
        }
        node.setTranslateY(50);
        node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(800), node);
        tt.setFromY(50);
        tt.setToY(0);
        tt.setDelay(Duration.seconds(delaySeconds));
        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setDelay(Duration.seconds(delaySeconds));
        tt.play();
        ft.play();
    }

    /** Gentle breathing fade (e.g. for a vignette overlay). */
    public static void breathe(Node node, double minOpacity, double maxOpacity, double seconds) {
        if (node == null) {
            return;
        }
        FadeTransition ft = new FadeTransition(Duration.seconds(seconds), node);
        ft.setFromValue(minOpacity);
        ft.setToValue(maxOpacity);
        ft.setAutoReverse(true);
        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.play();
    }

    /** Adds a golden hover glow + slight scale to a button. */
    public static void hoverGlow(Button button) {
        if (button == null) {
            return;
        }
        DropShadow glow = new DropShadow(22, Color.web("#D4AF37"));
        ScaleTransition scale = new ScaleTransition(Duration.millis(160), button);
        button.setOnMouseEntered(e -> {
            button.setEffect(glow);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.playFromStart();
        });
        button.setOnMouseExited(e -> {
            button.setEffect(null);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.playFromStart();
        });
    }
}
