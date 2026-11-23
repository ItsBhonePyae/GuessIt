package com.guessit.actualapp.view;

import com.guessit.actualapp.model.Session;
import com.guessit.actualapp.model.AppSettings;
import com.guessit.actualapp.model.GameHistory;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.time.format.DateTimeFormatter;

/**
 * The movie hub: a narrow navigation rail, a drifting poster wall and the
 * singleplayer bill. Layout follows the sliding-sidebar reference; colours are
 * the muted house palette.
 */
public class MovieView extends BorderPane {

    private static final double SIDEBAR_OPEN = 250;

    private static final String LIME = Theme.LIME;
    private static final String WHITE = Theme.WHITE;
    private static final String MUTED = Theme.MUTED;
    private static final String SOFT_TEXT = Theme.SOFT_TEXT;
    private static final String RED = Theme.RED;

    record Poster(String title, String tag, String artwork, String from, String to) { }

    private static final List<Poster> POSTERS = List.of(
        new Poster("Above the Below", "THRILLER", "above_the_below.jpg", "#35404c", "#202832"),
        new Poster("Avengers: Doomsday", "ACTION", "avengers_doomsday_ver7.jpg", "#303a46", "#20262f"),
        new Poster("The Cat in the Hat", "FAMILY", "cat_in_the_hat_ver4.jpg", "#3d4652", "#272f39"),
        new Poster("Dune: Part Three", "SCI-FI", "dune_part_three_ver24.jpg", "#252c35", "#1a2028"),
        new Poster("Ebenezer", "DRAMA", "ebenezer_ver2.jpg", "#414953", "#272e37"),
        new Poster("Hexed", "FANTASY", "hexed_ver3.jpg", "#222a34", "#303a46"),
        new Poster("I Play Rocky", "DRAMA", "i_play_rocky_ver2.jpg", "#434b57", "#2c343f"),
        new Poster("The Long Winter", "DRAMA", "long_winter_ver2.jpg", "#2b323c", "#1c232b"),
        new Poster("Moana", "ADVENTURE", "moana_ver15.jpg", "#343d48", "#242b34"),
        new Poster("Snowden", "THRILLER", "snowden.jpg", "#303944", "#414953"),
        new Poster("Spider-Man: Brand New Day", "ACTION", "spiderman_brand_new_day_ver25.jpg", "#222a34", "#3d4652"),
        new Poster("Star Wars", "SCI-FI", "star_wars_ver17.jpg", "#252c35", "#343d48"),
        new Poster("Whale Fall", "MYSTERY", "whalefall_ver4.jpg", "#272e37", "#1c232b")
    );

    private static final class PosterStrip {
        final VBox node;
        final TranslateTransition motion;

        PosterStrip(VBox node, TranslateTransition motion) {
            this.node = node;
            this.motion = motion;
        }
    }

    private static final List<PosterStrip> POSTER_STRIPS = new ArrayList<>();
    private static final Map<String, Optional<Image>> POSTER_ARTWORK = new ConcurrentHashMap<>();
    private static final double POSTER_PERIOD = 2272;
    private static AnimationTimer posterDrift;
    private final StackPane centerHost = new StackPane();
    private String historyFilter = "all";
    private Button activeNavButton;
    private Button singleplayerNavButton;
    private Button historyNavButton;

    public MovieView() {
        setStyle("-fx-background-color: " + Theme.INK + ";");
        setTop(buildTopBar());
        setLeft(buildSidebar());
        centerHost.getChildren().setAll(buildHero());
        setCenter(centerHost);
    }

    /* ---------------------------------------------------------------- top */

    private Node buildTopBar() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcome = Theme.label("Signed in as " + playerName(), 11, FontWeight.NORMAL,
            Theme.MUTED);

        Button notifications = iconButton(Feather.BELL, "Notifications");
        notifications.setOnAction(event -> showNotifications());

        Button settings = iconButton(Feather.SETTINGS, "Settings");
        settings.setOnAction(event -> showSettings());

        Button home = new Button("Home");
        home.setStyle(Theme.ghostButtonStyle());
        home.setOnMouseEntered(event -> home.setStyle(Theme.ghostHoverStyle()));
        home.setOnMouseExited(event -> home.setStyle(Theme.ghostButtonStyle()));
        home.setOnAction(event -> {
            stopPosterDrift();
            Navigator.go(new MenuView(playerName()));
        });

        HBox bar = new HBox(10, spacer, welcome, notifications, settings, home);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(9, 22, 9, 18));
        bar.setStyle("-fx-background-color: " + Theme.BLACK + ";"
            + " -fx-border-color: transparent transparent " + Theme.BORDER + " transparent;"
            + " -fx-border-width: 0 0 1 0;");
        return bar;
    }

    private Button iconButton(Feather glyph, String description) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(15);
        icon.setIconColor(Color.web(Theme.SOFT_TEXT));

        Button button = new Button();
        button.setGraphic(icon);
        button.setTooltip(new Tooltip(description));
        button.setAccessibleText(description);
        button.setMinSize(36, 36);
        button.setPrefSize(36, 36);
        button.setStyle("-fx-background-color: transparent;"
            + " -fx-background-radius: 6;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 6;"
            + " -fx-border-width: 1; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: rgba(255,255,255,0.06);"
            + " -fx-background-radius: 6; -fx-border-color: " + Theme.LIME + ";"
            + " -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: transparent;"
            + " -fx-background-radius: 6;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 6;"
            + " -fx-border-width: 1; -fx-cursor: hand;"));
        return button;
    }

    /* ---------------------------------------------------------------- sidebar */

    private Node buildSidebar() {
        StackPane badge = Theme.logoBadge(38, Feather.FILM);

        Label brandName = Theme.label("GuessIt", 18, FontWeight.SEMI_BOLD, WHITE);
        Label brandSub = Theme.label("Movie quiz", 10, FontWeight.NORMAL, MUTED);
        VBox brandText = new VBox(0, brandName, brandSub);
        HBox brandRow = new HBox(10, badge, brandText);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Circle live = new Circle(3.5, Color.web(RED));
        Label liveText = Theme.label("Movie mode", 10, FontWeight.NORMAL, SOFT_TEXT);
        HBox liveRow = new HBox(7, live, liveText);
        liveRow.setAlignment(Pos.CENTER_LEFT);
        liveRow.setPadding(new Insets(6, 0, 0, 2));

        VBox brandBlock = new VBox(9, brandRow, liveRow);

        singleplayerNavButton = navButton(Feather.PLAY, "Singleplayer", true);
        singleplayerNavButton.setOnAction(event -> showSingleplayerPage());

        historyNavButton = navButton(Feather.CLOCK, "History", false);
        historyNavButton.setOnAction(event -> showHistory());

        Button howToPlay = navButton(Feather.HELP_CIRCLE, "How to play", false);
        howToPlay.setOnAction(event -> showHowToPlay());

        VBox nav = new VBox(5, singleplayerNavButton, historyNavButton, howToPlay);

        Label promoTitle = Theme.label("Round format", 10, FontWeight.NORMAL, MUTED);
        Label promoValue = Theme.label("5 rounds  ·  60 sec", 12, FontWeight.SEMI_BOLD, WHITE);
        VBox promoCard = new VBox(4, promoTitle, promoValue);
        promoCard.setPadding(new Insets(12));
        promoCard.setStyle("-fx-background-color: " + Theme.PANEL + ";"
            + " -fx-background-radius: 8;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 8; -fx-border-width: 1;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Circle avatar = new Circle(17, Color.web(Theme.RED_DEEP));
        avatar.setStroke(Color.web(Theme.BORDER));
        avatar.setStrokeWidth(1);
        Label avatarText = Theme.label(playerName().substring(0, 1).toUpperCase(),
            12, FontWeight.BLACK, WHITE);
        StackPane avatarPane = new StackPane(avatar, avatarText);

        Label profileName = Theme.label(playerName(), 12, FontWeight.SEMI_BOLD, WHITE);
        Label profileMeta = Theme.label("GuessIt player", 10, FontWeight.NORMAL, MUTED);
        VBox profileText = new VBox(0, profileName, profileMeta);
        HBox profileRow = new HBox(9, avatarPane, profileText);
        profileRow.setAlignment(Pos.CENTER_LEFT);

        Label logout = Theme.label("Log out", 10, FontWeight.SEMI_BOLD, MUTED);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setOnMouseEntered(event -> logout.setTextFill(Color.web(Theme.SOFT_TEXT)));
        logout.setOnMouseExited(event -> logout.setTextFill(Color.web(Theme.MUTED)));
        logout.setOnMouseClicked(event -> {
            stopPosterDrift();
            Session.currentUser = null;
            MainMenuGuestController.show();
        });

        VBox footer = new VBox(11, new Separator(), profileRow, logout);
        footer.setPadding(new Insets(4, 0, 0, 0));

        VBox sidebar = new VBox(20, brandBlock, nav, spacer, promoCard, footer);
        sidebar.setPadding(new Insets(20, 14, 18, 14));
        sidebar.setPrefWidth(SIDEBAR_OPEN);
        sidebar.setMinWidth(SIDEBAR_OPEN);
        sidebar.setMaxWidth(SIDEBAR_OPEN);
        sidebar.setStyle("-fx-background-color: " + Theme.GRAPHITE + ";"
            + " -fx-border-color: transparent " + Theme.BORDER + " transparent transparent;"
            + " -fx-border-width: 0 1 0 0;");
        return sidebar;
    }

    private Button navButton(Feather glyph, String text, boolean active) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(17);
        icon.setIconColor(Color.web(active ? LIME : MUTED));

        Label label = new Label(text);
        label.setFont(javafx.scene.text.Font.font(Theme.FONT, FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web(active ? LIME : MUTED));

        HBox row = new HBox(12, icon, label);
        row.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(row);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(11, 13, 11, 13));
        button.setStyle(active ? Theme.activeNavStyle() : Theme.idleNavStyle());
        if (active) {
            activeNavButton = button;
        }
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setOnMouseEntered(event -> {
            if (button != activeNavButton) {
                button.setStyle(Theme.hoverNavStyle());
            }
        });
        button.setOnMouseExited(event -> button.setStyle(
            button == activeNavButton ? Theme.activeNavStyle() : Theme.idleNavStyle()));
        return button;
    }

    private void activateNavButton(Button button) {
        if (activeNavButton == button) {
            return;
        }
        Button previous = activeNavButton;
        activeNavButton = button;
        updateNavButton(previous, false);
        updateNavButton(button, true);
    }

    private void updateNavButton(Button button, boolean active) {
        if (button == null) {
            return;
        }
        HBox row = (HBox) button.getGraphic();
        FontIcon icon = (FontIcon) row.getChildren().get(0);
        Label label = (Label) row.getChildren().get(1);
        Color color = Color.web(active ? LIME : MUTED);
        icon.setIconColor(color);
        label.setTextFill(color);
        button.setStyle(active ? Theme.activeNavStyle() : Theme.idleNavStyle());
    }

    /* ------------------------------------------------------------------ hero */

    private Node buildHero() {
        Label kicker = Theme.label("Singleplayer", 11, FontWeight.SEMI_BOLD, RED);
        Label heading = Theme.label("Guess the movie", 44, FontWeight.SEMI_BOLD, WHITE);

        Label subtitle = Theme.label(
            "Watch a short clip and choose the film before the timer runs out.",
            15, FontWeight.NORMAL, SOFT_TEXT);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(560);

        FontIcon playIcon = new FontIcon(Feather.PLAY);
        playIcon.setIconSize(14);
        playIcon.setIconColor(Color.web("#1c160c"));
        Button play = new Button("Play now", playIcon);
        play.setStyle(Theme.limeButtonStyle());
        play.setOnMouseEntered(event -> play.setStyle(Theme.limeHoverStyle()));
        play.setOnMouseExited(event -> play.setStyle(Theme.limeButtonStyle()));
        play.setOnAction(event -> {
            stopPosterDrift();
            Navigator.go(new PlayView());
        });

        FontIcon helpIcon = new FontIcon(Feather.HELP_CIRCLE);
        helpIcon.setIconSize(14);
        helpIcon.setIconColor(Color.web("#efe6d8"));
        Button how = new Button("How to play", helpIcon);
        how.setStyle(Theme.ghostButtonStyle());
        how.setOnMouseEntered(event -> how.setStyle(Theme.ghostHoverStyle()));
        how.setOnMouseExited(event -> how.setStyle(Theme.ghostButtonStyle()));
        how.setOnAction(event -> showHowToPlay());

        HBox actions = new HBox(12, play, how);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label details = Theme.label("5 rounds  ·  60 seconds per round", 12,
            FontWeight.NORMAL, MUTED);

        VBox copy = new VBox(16, kicker, heading, subtitle, actions, details);
        copy.setPadding(new Insets(0, 60, 0, 60));
        copy.setMaxWidth(780);
        copy.setMaxHeight(Region.USE_PREF_SIZE);

        Region shade = new Region();
        shade.setMouseTransparent(true);
        shade.setStyle("-fx-background-color: linear-gradient(to right,"
            + " rgba(8,10,13,0.95) 0%, rgba(8,10,13,0.84) 36%,"
            + " rgba(8,10,13,0.58) 72%, rgba(8,10,13,0.50) 100%);");

        Region theaterTint = new Region();
        theaterTint.setMouseTransparent(true);
        theaterTint.setStyle("-fx-background-color: linear-gradient(to right,"
            + " rgba(90,20,24,0) 34%, rgba(90,20,24,0.15) 100%);");

        Node wall = posterWall();
        StackPane page = new StackPane(wall, shade, theaterTint, copy);
        StackPane.setAlignment(copy, Pos.CENTER_LEFT);

        page.setStyle("-fx-background-color: " + Theme.INK + ";");
        page.setMinWidth(0);
        page.setMinHeight(0);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(page.widthProperty());
        clip.heightProperty().bind(page.heightProperty());
        page.setClip(clip);

        return page;
    }

    /* ------------------------------------------------------------- poster wall */

    private static Node posterWall() {
        stopPosterDrift();

        HBox wall = new HBox(16);
        wall.setId("posterBackdrop");
        wall.setAlignment(Pos.CENTER);
        wall.setTranslateX(-26);

        for (int column = 0; column < 7; column++) {
            VBox strip = new VBox(16);
            strip.setPrefWidth(180);
            strip.setMinWidth(180);
            strip.setMaxWidth(180);

            List<Poster> base = new ArrayList<>();
            for (int n = 0; n < 8; n++) {
                base.add(POSTERS.get((n + column * 2) % POSTERS.size()));
            }
            for (int repeat = 0; repeat < 2; repeat++) {
                for (Poster poster : base) {
                    strip.getChildren().add(posterCard(poster));
                }
            }

            boolean downward = column % 2 != 0;
            double fromY = downward ? -POSTER_PERIOD : 0;
            double toY = downward ? 0 : -POSTER_PERIOD;
            strip.setTranslateY(fromY);
            strip.setCacheHint(CacheHint.SPEED);

            Pane view = new Pane(strip);
            view.setPrefWidth(180);
            view.setMinWidth(180);
            view.setMaxWidth(180);
            wall.getChildren().add(view);

            TranslateTransition motion = new TranslateTransition(
                Duration.seconds(78 + column * 8), strip);
            motion.setFromY(fromY);
            motion.setToY(toY);
            motion.setInterpolator(Interpolator.LINEAR);
            motion.setCycleCount(Animation.INDEFINITE);
            POSTER_STRIPS.add(new PosterStrip(strip, motion));
        }

        startPosterDrift(wall);
        return wall;
    }

    private static void stopPosterDrift() {
        if (posterDrift != null) {
            posterDrift.stop();
            posterDrift = null;
        }
        for (PosterStrip strip : POSTER_STRIPS) {
            strip.motion.stop();
            strip.node.setCache(false);
        }
        POSTER_STRIPS.clear();
    }

    private static void startPosterDrift(Node wall) {
        posterDrift = new AnimationTimer() {
            private boolean playing;

            @Override
            public void handle(long now) {
                Node page = wall.getParent();
                javafx.scene.Scene scene = wall.getScene();
                javafx.stage.Window window = scene == null ? null : scene.getWindow();
                boolean shouldPlay = page != null && page.isVisible()
                    && window != null && window.isShowing() && window.isFocused();
                if (playing == shouldPlay) {
                    return;
                }
                for (PosterStrip strip : POSTER_STRIPS) {
                    if (shouldPlay) {
                        strip.node.setCache(true);
                        strip.motion.play();
                    } else {
                        strip.motion.pause();
                        strip.node.setCache(false);
                    }
                }
                playing = shouldPlay;
            }
        };
        posterDrift.start();
    }

    private static StackPane posterCard(Poster poster) {
        double width = 180;
        double height = 270;

        StackPane card = new StackPane();
        card.setMinSize(width, height);
        card.setPrefSize(width, height);
        card.setMaxSize(width, height);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + poster.from() + ", " + poster.to() + "); -fx-background-radius: 12;"
            + " -fx-border-color: rgba(236,230,218,0.07); -fx-border-radius: 12;"
            + " -fx-border-width: 1;");

        Image artwork = POSTER_ARTWORK.computeIfAbsent(poster.artwork(), MovieView::loadPosterArtwork)
            .orElse(null);
        if (artwork != null && !artwork.isError()) {
            ImageView view = new ImageView(artwork);
            view.setSmooth(true);
            view.setMouseTransparent(true);
            view.setFitWidth(width);
            view.setFitHeight(height);
            Runnable cropArtwork = () -> {
                if (artwork.getProgress() >= 1 && !artwork.isError()) {
                    view.setViewport(coverViewport(artwork, width, height));
                }
            };
            artwork.progressProperty().addListener(
                (observable, oldValue, progress) -> cropArtwork.run());
            cropArtwork.run();
            card.getChildren().add(view);
        } else {
            Label initial = Theme.label(poster.title().substring(0, 1), 84,
                FontWeight.BLACK, "rgba(236,230,218,0.10)");
            StackPane.setAlignment(initial, Pos.CENTER);
            card.getChildren().add(initial);
        }

        Region scrim = new Region();
        scrim.setMouseTransparent(true);
        scrim.setStyle("-fx-background-color: linear-gradient(to bottom,"
            + " rgba(0,0,0,0) 40%, rgba(0,0,0,0.52) 68%, rgba(0,0,0,0.90) 100%);");

        Label title = Theme.label(poster.title().toUpperCase(Locale.ROOT), 14,
            FontWeight.BOLD, WHITE);
        title.setWrapText(true);
        title.setMaxWidth(width - 26);

        Label tag = Theme.label(poster.tag(), 8, FontWeight.BOLD, "rgba(236,230,218,0.62)");

        VBox meta = new VBox(3, tag, title);
        meta.setPadding(new Insets(13));
        StackPane.setAlignment(meta, Pos.BOTTOM_LEFT);

        card.getChildren().addAll(scrim, meta);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        card.setClip(clip);

        return card;
    }

    private static Optional<Image> loadPosterArtwork(String slug) {
        try {
            URL url = MovieView.class.getResource("/assets/the movie posters/" + slug);
            if (url == null) {
                return Optional.empty();
            }
            Image image = new Image(url.toExternalForm(), 360, 540, true, true, true);
            return image.isError() ? Optional.empty() : Optional.of(image);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Rectangle2D coverViewport(Image image, double targetWidth, double targetHeight) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double targetRatio = targetWidth / targetHeight;
        double imageRatio = imageWidth / imageHeight;
        if (imageRatio > targetRatio) {
            double cropWidth = imageHeight * targetRatio;
            return new Rectangle2D((imageWidth - cropWidth) / 2, 0, cropWidth, imageHeight);
        }
        double cropHeight = imageWidth / targetRatio;
        return new Rectangle2D(0, (imageHeight - cropHeight) / 2, imageWidth, cropHeight);
    }

    private void showHistory() {
        stopPosterDrift();
        activateNavButton(historyNavButton);
        transitionCenter(buildHistoryPage());
    }

    private void showSingleplayerPage() {
        stopPosterDrift();
        activateNavButton(singleplayerNavButton);
        transitionCenter(buildHero());
    }

    private Node buildHistoryPage() {
        Label eyebrow = Theme.label("RECENT ROUNDS", 10, FontWeight.SEMI_BOLD, LIME);
        Label title = Theme.label("History", 32, FontWeight.SEMI_BOLD, WHITE);
        Label description = Theme.label(
            "Example entries are shown here; each round you play is added to this list.",
            12, FontWeight.NORMAL, MUTED);
        description.setWrapText(true);

        List<GameHistory.Entry> entries = GameHistory.recent();
        List<GameHistory.Entry> filtered = entries.stream()
            .filter(entry -> "all".equals(historyFilter)
                || ("wins".equals(historyFilter) == (entry.score() > 0)))
            .toList();

        HBox filters = new HBox(8,
            historyFilterButton("All rounds", "all"),
            historyFilterButton("Wins", "wins"),
            historyFilterButton("Losses", "losses"));
        filters.setAlignment(Pos.CENTER_LEFT);

        Label count = Theme.label(filtered.size() + " / " + entries.size() + " rounds",
            10, FontWeight.NORMAL, MUTED);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox filterBar = new HBox(10, filters, spacer, count);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        VBox rows = new VBox(8);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM d, h:mm a");
        for (GameHistory.Entry entry : filtered) {
            rows.getChildren().add(historyRow(entry, dateFormat));
        }
        if (filtered.isEmpty()) {
            rows.getChildren().add(Theme.label("No rounds in this filter yet.",
                13, FontWeight.NORMAL, MUTED));
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox page = new VBox(16, eyebrow, title, description, filterBar, scroll);
        page.setPadding(new Insets(34, 44, 30, 44));
        page.setStyle("-fx-background-color: " + Theme.INK + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return page;
    }

    private void transitionCenter(Node page) {
        Node previous = centerHost.getChildren().isEmpty()
            ? null : centerHost.getChildren().get(0);

        page.setOpacity(0);
        page.setTranslateY(7);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(28, 28);
        spinner.setMaxSize(28, 28);
        spinner.setStyle("-fx-progress-color: " + Theme.LIME + ";");

        StackPane loading = new StackPane(spinner);
        loading.setStyle("-fx-background-color: rgba(13,12,11,0.34);");
        loading.setPickOnBounds(true);
        loading.setOpacity(0);

        if (previous == null) {
            centerHost.getChildren().setAll(page, loading);
        } else {
            centerHost.getChildren().setAll(previous, page, loading);
        }

        FadeTransition showLoader = new FadeTransition(Duration.millis(80), loading);
        showLoader.setFromValue(0);
        showLoader.setToValue(1);
        showLoader.setInterpolator(Interpolator.EASE_OUT);
        showLoader.setOnFinished(event -> {
            PauseTransition shortPause = new PauseTransition(Duration.millis(35));
            shortPause.setOnFinished(pauseEvent -> {
                FadeTransition reveal = new FadeTransition(Duration.millis(190), page);
                reveal.setFromValue(0);
                reveal.setToValue(1);
                reveal.setInterpolator(Interpolator.EASE_OUT);
                TranslateTransition rise = new TranslateTransition(Duration.millis(190), page);
                rise.setFromY(7);
                rise.setToY(0);
                rise.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition hideLoader = new FadeTransition(Duration.millis(160), loading);
                hideLoader.setFromValue(1);
                hideLoader.setToValue(0);
                hideLoader.setInterpolator(Interpolator.EASE_IN);
                ParallelTransition transition = new ParallelTransition(reveal, rise, hideLoader);
                transition.setOnFinished(done -> {
                    centerHost.getChildren().remove(loading);
                    if (previous != null) {
                        centerHost.getChildren().remove(previous);
                    }
                });
                transition.play();
            });
            shortPause.play();
        });
        showLoader.play();
    }

    private Button historyFilterButton(String text, String filter) {
        Button button = new Button(text);
        button.setStyle(historyFilter.equals(filter)
            ? Theme.limeButtonStyle() : Theme.ghostButtonStyle());
        button.setOnAction(event -> {
            historyFilter = filter;
            showHistory();
        });
        return button;
    }

    private HBox historyRow(GameHistory.Entry entry, DateTimeFormatter dateFormat) {
        String title = entry.movieTitle();
        String initials = title.length() < 2
            ? title.toUpperCase(Locale.ROOT)
            : title.substring(0, 2).toUpperCase(Locale.ROOT);
        Label badgeText = Theme.label(initials, 10, FontWeight.BOLD, SOFT_TEXT);
        StackPane badge = new StackPane(badgeText);
        badge.setMinSize(38, 38);
        badge.setPrefSize(38, 38);
        badge.setMaxSize(38, 38);
        badge.setStyle("-fx-background-color: " + Theme.PANEL + ";"
            + " -fx-background-radius: 6;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 6;"
            + " -fx-border-width: 1;");

        String source = entry.sample() ? "Example round" : "Played this session";
        VBox info = new VBox(3,
            Theme.label(title, 13, FontWeight.SEMI_BOLD, WHITE),
            Theme.label(source + "  \u00b7  " + entry.completedAt().format(dateFormat),
                10, FontWeight.NORMAL, MUTED));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        String resultText = entry.score() > 0 ? "WIN" : "LOSS";
        Label result = Theme.label(resultText, 9, FontWeight.BOLD,
            entry.score() > 0 ? LIME : Theme.RED);
        Label score = Theme.label(String.format(Locale.ROOT, "%,d", entry.score()),
            13, FontWeight.SEMI_BOLD, WHITE);

        HBox row = new HBox(13, badge, info, spacer, score, result);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: " + Theme.PANEL + ";"
            + " -fx-background-radius: 6;"
            + " -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 6;"
            + " -fx-border-width: 1;");
        return row;
    }

    private void showHowToPlay() {
        stopPosterDrift();
        Navigator.go(new PlayView(true));
    }

    private void showSettings() {
        Label volumeTitle = Theme.label("Video volume", 13, FontWeight.SEMI_BOLD, WHITE);
        Label volumeValue = Theme.label(
            Math.round(AppSettings.getVideoVolume() * 100) + "%", 12,
            FontWeight.SEMI_BOLD, LIME);
        Slider volume = new Slider(0, 100, AppSettings.getVideoVolume() * 100);
        volume.setShowTickMarks(true);
        volume.setMajorTickUnit(25);
        volume.setBlockIncrement(5);
        volume.setMaxWidth(Double.MAX_VALUE);
        volume.valueProperty().addListener((observable, oldValue, newValue) -> {
            AppSettings.setVideoVolume(newValue.doubleValue() / 100);
            volumeValue.setText(Math.round(newValue.doubleValue()) + "%");
        });

        Label note = Theme.label("Applies to clips played in the game.", 11,
            FontWeight.NORMAL, MUTED);
        HBox volumeRow = new HBox(12, volume, volumeValue);
        volumeRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(volume, Priority.ALWAYS);
        VBox content = new VBox(8, volumeTitle, volumeRow, note);
        showModal("Settings", content);
    }

    private void showNotifications() {
        VBox content = new VBox(6,
            Theme.label("You're all caught up.", 14, FontWeight.SEMI_BOLD, WHITE),
            Theme.label("New notifications will appear here.", 12, FontWeight.NORMAL, MUTED));
        showModal("Notifications", content);
    }

    private void showModal(String title, Node content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        if (getScene() != null) {
            dialog.initOwner(getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
        }

        VBox body = new VBox(14, Theme.label(title, 18, FontWeight.SEMI_BOLD, WHITE), content);
        body.setPadding(new Insets(4, 4, 8, 4));

        dialog.getDialogPane().setHeaderText(null);
        dialog.getDialogPane().setContent(body);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setStyle("-fx-background-color: " + Theme.GRAPHITE + ";");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button close = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        close.setText("Close");
        close.setStyle(Theme.ghostButtonStyle());
        dialog.showAndWait();
    }

    private String playerName() {
        return Session.currentUser == null ? "Guest Player" : Session.currentUser.getUsername();
    }
}
