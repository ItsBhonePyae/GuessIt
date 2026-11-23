package com.guessit.actualapp.view;

import javafx.animation.Interpolator;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.guessit.actualapp.model.Session;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Category menu in the AtlantaFX card-carousel style: three full-bleed
 * showtime billings on a dark lobby wall with quiet brass controls.
 */
public class MenuView extends BorderPane {

    private static final double CARD_WIDTH = 1000;
    private static final double CARD_HEIGHT = 540;
    private static final double CARD_SPACING = 20;
    private static final double PEEK = 140;
    private static final String ACCENT = Theme.LIME;
    private static final String DOT_IDLE = "#736b5f";
    private static final double DOT_HEIGHT = 6;
    private static final double MAX_OVERSCROLL = 78;
    private static final double RESISTANCE = 240;

    private final ScrollPane scroll = new ScrollPane();
    private final HBox cardRow = new HBox(CARD_SPACING);
    private final HBox dots = new HBox(8);
    private final Rectangle[] dotRects = new Rectangle[3];
    private final Timeline[] dotTimelines = new Timeline[3];

    private final PauseTransition settle = new PauseTransition(Duration.millis(110));
    private final Timeline springBack = new Timeline();
    private AnimationTimer scrollSmoothing;
    private double targetScrollOffset;
    private double rawPull;
    private double overscroll;
    private long lastScrollFrame;
    private boolean programmaticScroll;
    private boolean scrolling;
    private int activeDot = -1;

    private record CardData(String title, String tagline, String description, String details,
                            String artwork, String color, Feather icon) {
    }

    private final CardData[] cards = {
        new CardData("Movie", "Name that film",
            "Watch a short clip and choose the movie.",
            "Five rounds, 60 seconds each. Start with a 3-second clip and reveal more footage for fewer points.",
            "/assets/card posters/view-3d-cinema-theatre-room.jpg",
            "#35211f", Feather.FILM),
        new CardData("Song", "Guess the track",
            "Listen to a cue and identify the song.",
            "Planned mode: identify songs from short audio cues.",
            "/assets/card posters/beautiful-shot-live-concert-performance-with-yellow-light-show-big-crowd-cheering.jpg",
            "#302a20", Feather.MUSIC),
        new CardData("Celebrity", "Guess the star",
            "Identify an actor from a film still.",
            "Planned mode: identify performers from film stills or short clips.",
            "/assets/card posters/red-carpet-luxury-hotel.jpg",
            "#30242a", Feather.USERS)
    };
    private static final Map<String, Image> CARD_ARTWORK = new HashMap<>();

    public MenuView(String username) {
        setStyle("-fx-background-color: " + Theme.INK + ";");

        cardRow.setPadding(new Insets(10, PEEK, 24, PEEK));
        cardRow.setAlignment(Pos.CENTER_LEFT);
        for (CardData data : cards) {
            cardRow.getChildren().add(createCard(data));
        }

        scroll.setContent(cardRow);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setPrefHeight(CARD_HEIGHT + 34);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.hvalueProperty().addListener((obs, oldValue, newValue) -> {
            if (!programmaticScroll) {
                targetScrollOffset = newValue.doubleValue() * maxOffset();
            }
        });

        scroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaX() != 0
                ? -event.getDeltaX() : -event.getDeltaY();
            if (delta != 0) {
                applyScroll(delta);
                event.consume();
            }
        });

        settle.setOnFinished(event -> springBack());

        for (int i = 0; i < cards.length; i++) {
            Rectangle dot = new Rectangle(6, DOT_HEIGHT, Color.web(DOT_IDLE));
            dot.setArcWidth(DOT_HEIGHT);
            dot.setArcHeight(DOT_HEIGHT);
            dot.setOpacity(0.45);

            StackPane slot = new StackPane(dot);
            slot.setPrefSize(26, 16);
            slot.setMinSize(26, 16);
            slot.setMaxSize(26, 16);
            slot.setCursor(Cursor.HAND);
            final int index = i;
            slot.setOnMouseClicked(event -> scrollToCard(index));

            dotRects[i] = dot;
            dots.getChildren().add(slot);
        }
        dots.setAlignment(Pos.CENTER);
        dots.setPadding(new Insets(0, 0, 22, 0));

        StackPane stage = new StackPane(scroll);

        setTop(createHeader(username));
        setCenter(stage);
        setBottom(dots);

        scroll.hvalueProperty().addListener((obs, old, val) -> updateDots());
        cardRow.boundsInParentProperty().addListener((obs, old, val) -> updateDots());
        scroll.widthProperty().addListener((obs, old, val) -> updateDots());
        scroll.viewportBoundsProperty().addListener((obs, old, val) -> updateDots());
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.widthProperty().addListener((o, a, b) -> updateDots());
                javafx.application.Platform.runLater(this::updateDots);
                javafx.animation.PauseTransition kick =
                    new javafx.animation.PauseTransition(Duration.millis(250));
                kick.setOnFinished(event -> updateDots());
                kick.play();
            }
        });
    }

    // ----------------------------------------------------------------- header

    private VBox createHeader(String username) {
        StackPane badge = Theme.logoBadge(38, Feather.FILM);

        Label brand = Theme.label("GuessIt", 19, FontWeight.SEMI_BOLD, Theme.WHITE);
        Label sub = Theme.label("Movie quiz", 10, FontWeight.NORMAL, Theme.MUTED);
        VBox brandText = new VBox(0, brand, sub);
        HBox brandRow = new HBox(10, badge, brandText);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcome = Theme.label("Welcome back, " + username, 12, FontWeight.NORMAL,
            Theme.SOFT_TEXT);

        Button logout = new Button("Log out");
        logout.setStyle(Theme.ghostButtonStyle());
        logout.setOnMouseEntered(event -> logout.setStyle(Theme.ghostHoverStyle()));
        logout.setOnMouseExited(event -> logout.setStyle(Theme.ghostButtonStyle()));
        logout.setOnAction(event -> {
            Session.currentUser = null;
            MainMenuGuestController.show();
        });

        HBox bar = new HBox(14, brandRow, spacer, welcome, logout);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(15, 30, 15, 24));
        bar.setStyle("-fx-background-color: " + Theme.BLACK + ";");

        Label title = Theme.label("Choose a game", 26, FontWeight.SEMI_BOLD, Theme.WHITE);
        Label hint = Theme.label("More modes will be added later.", 13, FontWeight.NORMAL,
            Theme.MUTED);
        VBox intro = new VBox(5, title, hint);
        intro.setPadding(new Insets(24, (int) PEEK, 6, (int) PEEK));

        return new VBox(bar, Theme.goldRule(), intro);
    }

    // ------------------------------------------------------------------ cards

    private StackPane createCard(CardData data) {
        Color base = Color.web(data.color());
        Rectangle bg = new Rectangle(CARD_WIDTH, CARD_HEIGHT, base);
        bg.setArcWidth(18);
        bg.setArcHeight(18);

        Image cardImage = CARD_ARTWORK.computeIfAbsent(data.artwork(), MenuView::loadCardArtwork);
        ImageView artwork = null;
        if (cardImage != null && !cardImage.isError()) {
            ImageView imageView = new ImageView(cardImage);
            imageView.setFitWidth(CARD_WIDTH);
            imageView.setFitHeight(CARD_HEIGHT);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setMouseTransparent(true);
            artwork = imageView;
            Runnable cropArtwork = () -> {
                if (cardImage.getProgress() >= 1 && !cardImage.isError()) {
                    imageView.setViewport(coverViewport(cardImage, CARD_WIDTH, CARD_HEIGHT));
                }
            };
            cardImage.progressProperty().addListener(
                (observable, oldValue, progress) -> cropArtwork.run());
            cropArtwork.run();
        }

        Rectangle posterFade = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        posterFade.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, new Color(0.04, 0.05, 0.06, 0.94)),
            new Stop(0.55, new Color(0.04, 0.05, 0.06, 0.72)),
            new Stop(1, new Color(base.getRed(), base.getGreen(), base.getBlue(), 0.25))));
        posterFade.setMouseTransparent(true);

        Label availability = Theme.label(data.title().equals("Movie")
            ? "Available now" : "Coming soon", 10, FontWeight.NORMAL, Theme.SOFT_TEXT);
        Label heading = Theme.label(data.title(), 40, FontWeight.SEMI_BOLD, Theme.WHITE);
        heading.setWrapText(true);
        Label tagline = Theme.label(data.tagline(), 15, FontWeight.SEMI_BOLD, Theme.SOFT_TEXT);
        Label copy = Theme.label(data.description(), 13, FontWeight.NORMAL, Theme.SOFT_TEXT);
        copy.setWrapText(true);
        copy.setMaxWidth(430);
        Label details = Theme.label(data.details(), 11, FontWeight.NORMAL, Theme.MUTED);
        Label action = Theme.label(data.title().equals("Movie")
            ? "Open movie mode  \u2192" : "Details", 12, FontWeight.SEMI_BOLD,
            data.title().equals("Movie") ? Theme.LIME : Theme.MUTED);
        details.setWrapText(true);
        details.setMaxWidth(560);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox content = new VBox(10, availability, spacer, iconBadge(data.icon()),
            heading, tagline, copy, details, action);
        content.setPadding(new Insets(34, 38, 34, 38));
        content.setAlignment(Pos.BASELINE_LEFT);
        content.setMaxWidth(640);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);

        Rectangle frame = new Rectangle(CARD_WIDTH, CARD_HEIGHT, Color.TRANSPARENT);
        frame.setArcWidth(18);
        frame.setArcHeight(18);
        frame.setStroke(Color.web("#ffffff", 0.12));
        frame.setStrokeWidth(1);
        frame.setMouseTransparent(true);

        StackPane card = artwork == null
            ? new StackPane(bg, posterFade, content, frame)
            : new StackPane(bg, artwork, posterFade, content, frame);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        card.setCursor(Cursor.HAND);
        card.setFocusTraversable(true);
        card.setAccessibleText(data.title() + ". " + data.description());

        Rectangle clip = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        card.setClip(clip);

        final double[] press = new double[2];
        card.setOnMousePressed(event -> {
            press[0] = event.getX();
            press[1] = event.getY();
        });
        card.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            double moved = Math.hypot(event.getX() - press[0], event.getY() - press[1]);
            if (moved < 6) {
                open(data);
            }
        });
        card.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                open(data);
                event.consume();
            }
        });

        card.setOnMouseEntered(event -> {
            frame.setStroke(Color.web(Theme.LIME, 0.55));
        });
        card.setOnMouseExited(event -> {
            frame.setStroke(Color.web("#ffffff", 0.12));
        });

        return card;
    }

    private static Image loadCardArtwork(String resource) {
        URL url = MenuView.class.getResource(resource);
        if (url == null) {
            return null;
        }
        Image image = new Image(url.toExternalForm(), CARD_WIDTH, CARD_HEIGHT,
            true, true, true);
        return image.isError() ? null : image;
    }

    private static javafx.geometry.Rectangle2D coverViewport(
        Image image, double targetWidth, double targetHeight) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double targetRatio = targetWidth / targetHeight;
        double imageRatio = imageWidth / imageHeight;
        if (imageRatio > targetRatio) {
            double cropWidth = imageHeight * targetRatio;
            return new javafx.geometry.Rectangle2D(
                (imageWidth - cropWidth) / 2, 0, cropWidth, imageHeight);
        }
        double cropHeight = imageWidth / targetRatio;
        return new javafx.geometry.Rectangle2D(
            0, (imageHeight - cropHeight) / 2, imageWidth, cropHeight);
    }

    private StackPane iconBadge(Feather glyph) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(20);
        icon.setIconColor(Color.web(Theme.SOFT_TEXT));

        StackPane badge = new StackPane(icon);
        badge.setMinSize(48, 48);
        badge.setPrefSize(48, 48);
        badge.setMaxSize(48, 48);
        badge.setStyle("-fx-background-color: rgba(10,9,8,0.25);"
            + " -fx-background-radius: 8;"
            + " -fx-border-color: rgba(236,230,218,0.16); -fx-border-radius: 8; -fx-border-width: 1;");
        StackPane.setAlignment(badge, Pos.BOTTOM_LEFT);
        return badge;
    }

    private void open(CardData data) {
        switch (data.title()) {
            case "Movie" -> Navigator.go(new MovieView());
            default -> comingSoon(data);
        }
    }

    private void comingSoon(CardData data) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(data.title() + " mode");
        alert.setHeaderText("Coming soon");
        alert.setContentText(data.description() + "\n\n" + data.details());
        alert.showAndWait();
    }

    // -------------------------------------------------------------- carousel

    private void applyScroll(double deltaPx) {
        double max = maxOffset();
        if (max <= 0) {
            return;
        }
        double currentTarget = scrolling ? targetScrollOffset : scroll.getHvalue() * max;
        double desired = currentTarget + deltaPx;
        targetScrollOffset = Math.max(0, Math.min(max, desired));

        double excess = desired - targetScrollOffset;
        if (excess != 0) {
            rawPull = Math.max(-4000, Math.min(4000, rawPull + excess));
            overscroll = MAX_OVERSCROLL * Math.tanh(rawPull / RESISTANCE);
            cardRow.setTranslateX(-overscroll);
            settle.playFromStart();
        } else if (overscroll != 0) {
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
        startScrollSmoothing();
    }

    private void startScrollSmoothing() {
        if (scrollSmoothing == null) {
            scrollSmoothing = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (lastScrollFrame == 0) {
                        lastScrollFrame = now;
                        return;
                    }
                    double elapsed = Math.min(0.05, (now - lastScrollFrame) / 1_000_000_000.0);
                    lastScrollFrame = now;
                    double max = maxOffset();
                    if (max <= 0) {
                        stopScrollSmoothing();
                        return;
                    }

                    double current = scroll.getHvalue() * max;
                    double remaining = targetScrollOffset - current;
                    if (Math.abs(remaining) < 0.5) {
                        programmaticScroll = true;
                        scroll.setHvalue(targetScrollOffset / max);
                        programmaticScroll = false;
                        stopScrollSmoothing();
                        return;
                    }

                    double progress = 1 - Math.exp(-elapsed / 0.055);
                    programmaticScroll = true;
                    scroll.setHvalue((current + remaining * progress) / max);
                    programmaticScroll = false;
                }
            };
        }
        if (!scrolling) {
            lastScrollFrame = 0;
            scrolling = true;
            scrollSmoothing.start();
        }
    }

    private void stopScrollSmoothing() {
        if (scrollSmoothing != null) {
            scrollSmoothing.stop();
        }
        scrolling = false;
        lastScrollFrame = 0;
    }

    private void springBack() {
        double pull = overscroll;
        if (Math.abs(pull) < 1.0) {
            overscroll = 0;
            rawPull = 0;
            cardRow.setTranslateX(0);
            return;
        }

        overscroll = 0;
        rawPull = 0;
        springBack.stop();
        double bounce = Math.max(-9.0, Math.min(9.0, pull * 0.12));
        double totalMs = 200 + Math.min(Math.abs(pull) * 4, 320);
        springBack.getKeyFrames().setAll(
            new KeyFrame(Duration.millis(totalMs * 0.6),
                new KeyValue(cardRow.translateXProperty(), bounce,
                    Interpolator.SPLINE(0.25, 0.9, 0.4, 1.0))),
            new KeyFrame(Duration.millis(totalMs),
                new KeyValue(cardRow.translateXProperty(), 0.0, Interpolator.EASE_OUT)));
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
        dot.setEffect(null);

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
        stopScrollSmoothing();
        springBack.stop();
        settle.stop();
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

        double target = Math.max(0, Math.min(1,
            (bounds.getMinX() + bounds.getWidth() / 2.0 - viewportWidth / 2.0) / max));
        targetScrollOffset = target * max;

        targetScrollOffset = target * max;
        startScrollSmoothing();
    }
}
