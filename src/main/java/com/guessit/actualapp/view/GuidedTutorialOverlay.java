package com.guessit.actualapp.view;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class GuidedTutorialOverlay extends StackPane {

    public record Step(Node target, String title, String body) {
    }

    private final List<Step> steps;
    private final Runnable onFinish;
    private final Rectangle highlight = new Rectangle();
    private final Polygon caret = new Polygon();
    private final VBox card;
    private final Label stepLabel;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final Button backButton;
    private final Button nextButton;
    private final List<Circle> dots = new ArrayList<>();
    private Node highlighted;
    private Effect previousEffect;
    private FadeTransition pulse;
    private int index = -1;
    private boolean finished;

    public GuidedTutorialOverlay(List<Step> steps, Runnable onFinish) {
        this.steps = List.copyOf(steps);
        this.onFinish = onFinish;
        setPickOnBounds(true);

        Region scrim = new Region();
        scrim.setStyle("-fx-background-color: rgba(0,0,0,0.62);");

        highlight.setFill(Color.TRANSPARENT);
        highlight.setStroke(Color.web(Theme.LIME));
        highlight.setStrokeWidth(2.5);
        highlight.setArcWidth(12);
        highlight.setArcHeight(12);
        highlight.setManaged(false);
        highlight.setEffect(new DropShadow(20, Color.web(Theme.LIME, 0.5)));
        highlight.setMouseTransparent(true);

        caret.setFill(Color.web(Theme.GRAPHITE));
        caret.setStroke(Color.web(Theme.LIME));
        caret.setManaged(false);
        caret.setMouseTransparent(true);

        stepLabel = Theme.label("", 9, FontWeight.BOLD, Theme.LIME);
        Button skipButton = new Button("Skip tour");
        skipButton.setStyle(Theme.ghostButtonStyle());
        skipButton.setOnAction(event -> finish());
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox top = new HBox(10, stepLabel, topSpacer, skipButton);
        top.setAlignment(Pos.CENTER_LEFT);

        titleLabel = Theme.label("", 17, FontWeight.SEMI_BOLD, Theme.WHITE);
        titleLabel.setWrapText(true);
        bodyLabel = Theme.label("", 12, FontWeight.NORMAL, Theme.SOFT_TEXT);
        bodyLabel.setWrapText(true);

        HBox dotRow = new HBox(6);
        for (int i = 0; i < steps.size(); i++) {
            Circle dot = new Circle(3, Color.web(Theme.BORDER));
            dots.add(dot);
            dotRow.getChildren().add(dot);
        }

        backButton = new Button("Back");
        backButton.setStyle(Theme.ghostButtonStyle());
        backButton.setOnAction(event -> showStep(index - 1));
        nextButton = new Button("Next");
        nextButton.setStyle(Theme.limeButtonStyle());
        nextButton.setOnAction(event -> {
            if (index >= steps.size() - 1) {
                finish();
            } else {
                showStep(index + 1);
            }
        });
        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox nav = new HBox(8, backButton, navSpacer, nextButton);
        nav.setAlignment(Pos.CENTER_LEFT);

        card = new VBox(11, top, titleLabel, bodyLabel, dotRow, nav);
        card.setManaged(false);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color: " + Theme.GRAPHITE + ";"
            + " -fx-background-radius: 10; -fx-border-color: " + Theme.BORDER + ";"
            + " -fx-border-radius: 10; -fx-border-width: 1;");
        card.setEffect(new DropShadow(24, Color.web(Theme.BLACK, 0.6)));

        getChildren().addAll(scrim, highlight, caret, card);
        widthProperty().addListener((observable, oldValue, newValue) -> refresh());
        heightProperty().addListener((observable, oldValue, newValue) -> refresh());
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && oldScene != null) {
                finish();
            } else if (newScene != null) {
                javafx.application.Platform.runLater(this::refresh);
            }
        });
        showStep(0);
    }

    private void showStep(int stepIndex) {
        index = Math.max(0, Math.min(stepIndex, steps.size() - 1));
        Step step = steps.get(index);
        stepLabel.setText("STEP " + (index + 1) + " OF " + steps.size());
        titleLabel.setText(step.title());
        bodyLabel.setText(step.body());
        backButton.setDisable(index == 0);
        nextButton.setText(index == steps.size() - 1 ? "Done" : "Next");

        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setFill(Color.web(i == index ? Theme.LIME
                : i < index ? Theme.SOFT_TEXT : Theme.BORDER));
        }

        if (highlighted != null) {
            highlighted.setEffect(previousEffect);
        }
        highlighted = step.target();
        previousEffect = highlighted.getEffect();
        highlighted.setEffect(new DropShadow(18, Color.web(Theme.LIME, 0.58)));

        card.applyCss();
        double cardWidth = card.prefWidth(-1);
        card.resize(cardWidth, card.prefHeight(cardWidth));
        card.layout();
        refresh();

        if (pulse != null) {
            pulse.stop();
        }
        pulse = new FadeTransition(Duration.millis(820), highlight);
        pulse.setFromValue(0.5);
        pulse.setToValue(1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void refresh() {
        if (finished || index < 0 || getScene() == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        Node target = steps.get(index).target();
        Bounds bounds = sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return;
        }

        double pad = 7;
        highlight.setX(bounds.getMinX() - pad);
        highlight.setY(bounds.getMinY() - pad);
        highlight.setWidth(bounds.getWidth() + pad * 2);
        highlight.setHeight(bounds.getHeight() + pad * 2);

        double cardWidth = card.getWidth();
        double cardHeight = card.getHeight();
        double gap = 24;
        boolean right = bounds.getCenterX() < getWidth() / 2
            && bounds.getMaxX() + gap + cardWidth < getWidth() - 18;
        double x = right ? bounds.getMaxX() + gap : bounds.getMinX() - gap - cardWidth;
        x = Math.max(18, Math.min(x, getWidth() - cardWidth - 18));
        double y = Math.max(18, Math.min(bounds.getCenterY() - cardHeight / 2,
            getHeight() - cardHeight - 18));
        card.relocate(x, y);

        double arrowY = Math.max(y + 16,
            Math.min(bounds.getCenterY() - 9, y + cardHeight - 34));
        if (right) {
            caret.getPoints().setAll(12.0, 0.0, 0.0, 9.0, 12.0, 18.0);
            caret.relocate(x - 12, arrowY);
        } else {
            caret.getPoints().setAll(0.0, 0.0, 12.0, 9.0, 0.0, 18.0);
            caret.relocate(x + cardWidth - 1, arrowY);
        }
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        if (pulse != null) {
            pulse.stop();
        }
        if (highlighted != null) {
            highlighted.setEffect(previousEffect);
        }
        if (getParent() instanceof Pane parent) {
            parent.getChildren().remove(this);
        }
        onFinish.run();
    }
}
