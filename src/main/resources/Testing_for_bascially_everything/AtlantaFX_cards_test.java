package Testing_for_bascially_everything;

import atlantafx.base.theme.PrimerDark;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Horizontal card carousel test.
 *
 * - 3 big cards inside a horizontally scrollable pane (no visible scrollbar).
 * - Modern pill indicator below the cards; the pill for the card closest to
 *   the centre of the viewport is stretched and lit while scrolling.
 * - Clicking a pill animates the carousel to that card.
 * - Clicking a card opens its own detail page (with a back button).
 */
public class AtlantaFX_cards_test extends Application {

    private static final double CARD_WIDTH = 1000;
    private static final double CARD_HEIGHT = 540;
    private static final double CARD_SPACING = 20;
    private static final double PEEK = 140;
    private static final String ACCENT = "#4493f8";
    private static final String DOT_IDLE = "#8b949e";
    private static final double DOT_HEIGHT = 6;
    private static final double MAX_OVERSCROLL = 78;
    // Larger = stiffer: at the very start a pixel of scroll moves the row
    // MAX_OVERSCROLL / RESISTANCE pixels, then it asymptotically approaches the cap.
    private static final double RESISTANCE = 240;

    private final ScrollPane scroll = new ScrollPane();
    private final HBox cardRow = new HBox(CARD_SPACING);
    private final HBox dots = new HBox(8);
    private final BorderPane shell = new BorderPane();
    private final Rectangle[] dotRects = new Rectangle[3];
    private final Timeline[] dotTimelines = new Timeline[3];

    private final PauseTransition settle = new PauseTransition(Duration.millis(110));
    private final Timeline springBack = new Timeline();
    private double rawPull = 0;
    private double overscroll = 0;
    private int activeDot = -1;

    private final CardData[] cards = {
        new CardData(
            "Lord of the Rings",
            "An unexpected journey",
            "Three scenes from the trilogy, ready for the player.",
            "#7c3aed"),
        new CardData(
            "Dune",
            "Desert power",
            "Sand, spice and very large worms. Scene clips go here.",
            "#0ea5e9"),
        new CardData(
            "Interstellar",
            "Beyond the stars",
            "Space, time and a very loud organ. Scene clips go here.",
            "#f59e0b")
    };

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        cardRow.setPadding(new Insets(10, PEEK, 24, PEEK));
        cardRow.setAlignment(Pos.CENTER_LEFT);
        for (CardData data : cards) {
            cardRow.getChildren().add(createCard(data));
        }

        scroll.setContent(cardRow);
        // No visible scrollbars: scrolling works via drag, wheel and the pills.
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setPrefHeight(CARD_HEIGHT + 34);

        // Horizontal wheel / trackpad gestures move the carousel sideways.
        // Consume the event so ScrollPane's own scroll handling can't fight it.
        // ScrollEvent deltas are inverted relative to hvalue: a positive deltaX
        // means "scroll back", so subtract it.
        scroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaX() != 0) {
                applyScroll(-e.getDeltaX());
            }
            e.consume();
        });

        settle.setOnFinished(e -> springBack());

        for (int i = 0; i < cards.length; i++) {
            Rectangle dot = new Rectangle(6, DOT_HEIGHT, Color.web(DOT_IDLE));
            dot.setArcWidth(DOT_HEIGHT);
            dot.setArcHeight(DOT_HEIGHT);
            dot.setOpacity(0.45);

            // Fixed-width slot keeps the row from shifting while the pill grows.
            StackPane slot = new StackPane(dot);
            slot.setPrefSize(26, 16);
            slot.setMinSize(26, 16);
            slot.setMaxSize(26, 16);
            slot.setCursor(Cursor.HAND);
            final int index = i;
            slot.setOnMouseClicked(e -> scrollToCard(index));

            dotRects[i] = dot;
            dots.getChildren().add(slot);
        }
        dots.setAlignment(Pos.CENTER);
        dots.setPadding(new Insets(0, 0, 30, 0));

        shell.setTop(createHeader());
        shell.setCenter(scroll);
        shell.setBottom(dots);

        scroll.hvalueProperty().addListener((obs, old, val) -> updateDots());
        cardRow.boundsInParentProperty().addListener((obs, old, val) -> updateDots());
        scroll.widthProperty().addListener((obs, old, val) -> updateDots());
        shell.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.widthProperty().addListener((o, a, b) -> updateDots());
                updateDots();
            }
        });

        stage.setTitle("AtlantaFX - Card Carousel");
        stage.setScene(new javafx.scene.Scene(shell, 1280, 860));
        stage.show();

        Platform.runLater(this::updateDots);
    }

    private StackPane createCard(CardData data) {
        Rectangle bg = new Rectangle(CARD_WIDTH, CARD_HEIGHT, Color.TRANSPARENT);
        bg.setArcWidth(30);
        bg.setArcHeight(30);
        bg.setFill(new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(data.color())),
            new Stop(1, Color.web(data.color()).darker())));

        Label tag = new Label("SCENE CLIP");
        tag.getStyleClass().add("caption");
        tag.setTextFill(Color.web("#ffffff", 0.7));

        Label heading = new Label(data.title());
        heading.getStyleClass().add("title-1");
        heading.setTextFill(Color.WHITE);
        heading.setWrapText(true);

        Label tagline = new Label(data.tagline());
        tagline.getStyleClass().add("subtitle");
        tagline.setTextFill(Color.web("#ffffff", 0.85));

        Label hint = new Label("Click to open  →");
        hint.setStyle("-fx-background-color: rgba(255,255,255,0.18);"
            + "-fx-background-radius: 999; -fx-padding: 9 18; -fx-text-fill: white;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox content = new VBox(12, tag, spacer, heading, tagline, hint);
        content.setPadding(new Insets(28));
        content.setAlignment(Pos.BASELINE_LEFT);
        content.setMaxWidth(CARD_WIDTH);

        StackPane card = new StackPane(bg, content);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        card.setCursor(Cursor.HAND);

        // Only treat it as a click if the pointer barely moved, so dragging to
        // scroll doesn't accidentally open the card.
        final double[] press = new double[2];
        card.setOnMousePressed(e -> {
            press[0] = e.getX();
            press[1] = e.getY();
        });
        card.setOnMouseReleased(e -> {
            double moved = Math.hypot(e.getX() - press[0], e.getY() - press[1]);
            if (moved < 6) {
                showDetail(data);
            }
        });

        // Hover: shadow only (translating the card can cause hover flicker).
        card.setOnMouseEntered(e -> card.setEffect(new DropShadow(20, Color.web("#000000", 0.45))));
        card.setOnMouseExited(e -> card.setEffect(null));

        return card;
    }

    /**
     * Moves the carousel by {@code deltaPx}. Anything past either end is turned
     * into a damped rubber-band pull that springs back after the gesture stops.
     */
    private void applyScroll(double deltaPx) {
        double max = maxOffset();
        if (max <= 0) {
            return;
        }

        // A running spring-back would fight the live translation.
        springBack.stop();

        double desired = scroll.getHvalue() * max + deltaPx;
        double clamped = Math.max(0, Math.min(max, desired));
        scroll.setHvalue(clamped / max);

        double excess = desired - clamped;
        if (excess != 0) {
            // Progressive resistance: raw force accumulates, but the visible
            // stretch approaches MAX_OVERSCROLL asymptotically.
            rawPull = Math.max(-4000, Math.min(4000, rawPull + excess));
            overscroll = MAX_OVERSCROLL * Math.tanh(rawPull / RESISTANCE);
            cardRow.setTranslateX(-overscroll);
            // Only while actually stretched does the release timer run.
            settle.playFromStart();
        } else if (overscroll != 0) {
            // Back in range: release the pull without any animation.
            rawPull *= 0.5;
            overscroll = MAX_OVERSCROLL * Math.tanh(rawPull / RESISTANCE);
            if (Math.abs(overscroll) < 1.0) {
                rawPull = 0;
                overscroll = 0;
            } else {
                settle.playFromStart();
            }
            cardRow.setTranslateX(-overscroll);
        }
    }

    private void springBack() {
        double pull = overscroll;
        // A negligible pull (or a clean stop at the end) must not animate.
        if (Math.abs(pull) < 1.0) {
            overscroll = 0;
            rawPull = 0;
            cardRow.setTranslateX(0);
            return;
        }

        overscroll = 0;
        rawPull = 0;
        springBack.stop();
        // Allow a small rebound past the resting point, sized to the stretch.
        double bounce = Math.max(-9.0, Math.min(9.0, pull * 0.12));
        double totalMs = 200 + Math.min(Math.abs(pull) * 4, 320);
        springBack.getKeyFrames().setAll(
            new KeyFrame(Duration.millis(totalMs * 0.6),
                new KeyValue(cardRow.translateXProperty(), bounce,
                    Interpolator.SPLINE(0.25, 0.9, 0.4, 1.0))),
            new KeyFrame(Duration.millis(totalMs),
                new KeyValue(cardRow.translateXProperty(), 0.0,
                    Interpolator.EASE_OUT)));
        springBack.playFromStart();
    }

    private void updateDots() {
        if (cardRow.getScene() == null) {
            return;
        }

        double viewportWidth = scroll.getViewportBounds().getWidth();
        if (viewportWidth <= 0) {
            return;
        }

        double contentWidth = cardRow.getBoundsInParent().getWidth();
        double max = Math.max(0, contentWidth - viewportWidth);
        // Viewport centre expressed in card-row content coordinates.
        double viewportCentre = scroll.getHvalue() * max + viewportWidth / 2.0;

        int closest = 0;
        double best = Double.MAX_VALUE;
        for (int i = 0; i < cardRow.getChildren().size(); i++) {
            Node node = cardRow.getChildren().get(i);
            Bounds bounds = node.getBoundsInParent();
            if (bounds == null) {
                continue;
            }
            double centre = bounds.getMinX() + bounds.getWidth() / 2.0;
            double distance = Math.abs(centre - viewportCentre);
            if (distance < best) {
                best = distance;
                closest = i;
            }
        }

        if (closest != activeDot) {
            styleDot(activeDot, false);
            activeDot = closest;
            styleDot(activeDot, true);
        }
    }

    private void styleDot(int index, boolean active) {
        if (index < 0 || index >= dotRects.length) {
            return;
        }
        Rectangle dot = dotRects[index];
        if (dotTimelines[index] != null) {
            dotTimelines[index].stop();
        }

        double width = active ? 26 : DOT_HEIGHT;
        double opacity = active ? 1 : 0.45;
        Color fill = active ? Color.web(ACCENT) : Color.web(DOT_IDLE);
        dot.setEffect(active ? new DropShadow(10, Color.web(ACCENT, 0.65)) : null);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(220),
                new KeyValue(dot.widthProperty(), width, Interpolator.EASE_BOTH),
                new KeyValue(dot.opacityProperty(), opacity, Interpolator.EASE_BOTH),
                new KeyValue(dot.fillProperty(), fill, Interpolator.EASE_BOTH)));
        dotTimelines[index] = timeline;
        timeline.play();
    }

    private double maxOffset() {
        double contentWidth = cardRow.getBoundsInParent().getWidth();
        double viewportWidth = scroll.getViewportBounds().getWidth();
        return Math.max(0, contentWidth - viewportWidth);
    }

    private void scrollToCard(int index) {
        springBack.stop();
        rawPull = 0;
        overscroll = 0;
        cardRow.setTranslateX(0);

        double contentWidth = cardRow.getBoundsInParent().getWidth();
        double viewportWidth = scroll.getViewportBounds().getWidth();
        double max = Math.max(0, contentWidth - viewportWidth);
        if (max <= 0) {
            return;
        }

        Node node = cardRow.getChildren().get(index);
        Bounds bounds = node.getBoundsInParent();
        if (bounds == null) {
            return;
        }

        // Align the card's centre with the viewport's centre.
        double target = clamp((bounds.getMinX() + bounds.getWidth() / 2.0 - viewportWidth / 2.0) / max);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(320),
                new KeyValue(scroll.hvalueProperty(), target, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private VBox createHeader() {
        Label title = new Label("Featured");
        title.getStyleClass().add("title-2");

        Label hint = new Label("Scroll, drag or tap an indicator");
        hint.getStyleClass().add("subtitle");
        hint.getStyleClass().add("text-muted");

        VBox header = new VBox(4, title, hint);
        header.setPadding(new Insets(34, (int) PEEK, 10, (int) PEEK));
        return header;
    }

    private void showDetail(CardData data) {
        Button back = new Button("← Back");
        back.setOnAction(e -> {
            shell.setTop(createHeader());
            shell.setCenter(scroll);
            shell.setBottom(dots);
            updateDots();
        });

        Label title = new Label(data.title());
        title.getStyleClass().add("title-1");

        Label tagline = new Label(data.tagline());
        tagline.getStyleClass().add("subtitle");

        Label description = new Label(data.description());
        description.setWrapText(true);
        description.setMaxWidth(560);

        // Placeholder for the actual player.
        StackPane player = new StackPane();
        player.setStyle("-fx-background-color: black; -fx-background-radius: 16;");
        player.setPrefSize(760, 400);
        FontIcon playIcon = new FontIcon(Feather.PLAY);
        // Keep the inline -fx-font-family that FontIcon needs, then add a colour.
        playIcon.setStyle(playIcon.getStyle() + " -fx-icon-size: 22px; -fx-icon-color: white;");
        Label play = new Label("Player goes here", playIcon);
        play.setContentDisplay(ContentDisplay.LEFT);
        play.setGraphicTextGap(10);
        play.setTextFill(Color.WHITE);
        player.getChildren().add(play);

        VBox body = new VBox(16, back, title, tagline, description, player);
        body.setPadding(new Insets(28, (int) PEEK, 28, (int) PEEK));
        body.setAlignment(Pos.TOP_LEFT);
        body.setFillWidth(true);

        shell.setTop(new VBox());
        shell.setCenter(new StackPane(body));
        shell.setBottom(null);
    }

    private record CardData(String title, String tagline, String description, String color) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}