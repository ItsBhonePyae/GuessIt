package Testing_for_bascially_everything;

import atlantafx.base.theme.PrimerDark;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import Testing_for_bascially_everything.assets.AssetCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sliding sidebar navigation demo with a three-page movie-guessing app shell.
 *
 * <p>Pages: Singleplayer (default), History and Settings. The sidebar collapses
 * to an icon rail and expands again with an animated width transition. All
 * colours come from the supplied palette:</p>
 *
 * <ul>
 *   <li>Verde Citrico {@code #b7e34e} (darkened from the reference lime)</li>
 *   <li>Roxo {@code #553199}</li>
 *   <li>Roxo Escuro {@code #342066}</li>
 *   <li>Grafite {@code #1e1b26}</li>
 *   <li>Preto {@code #000000}</li>
 *   <li>Branco {@code #ffffff}</li>
 * </ul>
 *
 * <p>The Singleplayer page uses a full-bleed, slowly drifting movie-poster wall
 * behind a dark cinematic gradient. PLAY NOW opens a separate movie-playing
 * stage (see {@link Movie_Playing_Demo_Page}); HOW TO PLAY opens the same stage
 * with a guided, popover-style tutorial.
 */
public class SlidingSidebar_test extends Application {

    // ------------------------------------------------------------------ theme

    static final String LIME = "#b7e34e";
    static final String LIME_HOVER = "#c9f26a";
    static final String VIOLET = "#553199";
    static final String DEEP_VIOLET = "#342066";
    static final String GRAPHITE = "#1e1b26";
    static final String BLACK = "#000000";
    static final String WHITE = "#ffffff";
    static final String MUTED = "#9b95a8";
    static final String SOFT_TEXT = "#cbc5d8";
    static final String BORDER = "#2b2735";
    static final String HOVER = "#262130";
    static final String FONT = "Segoe UI";

    private static final double SIDEBAR_OPEN = 260;
    private static final double SIDEBAR_CLOSED = 76;

    // ------------------------------------------------------------------- data

    /** A poster tile in the animated background wall. */
    record Poster(String title, String tag, String from, String to) { }

    private static final List<Poster> POSTERS = List.of(
        new Poster("Blade Runner 2049", "SCI-FI", "#553199", "#342066"),
        new Poster("Dune: Part Two", "SCI-FI", "#342066", "#1e1b26"),
        new Poster("Arrival", "MYSTERY", "#3d2470", "#553199"),
        new Poster("Whiplash", "DRAMA", "#221c35", "#342066"),
        new Poster("Parasite", "THRILLER", "#4a2b85", "#221c35"),
        new Poster("Interstellar", "SPACE", "#1e1b26", "#553199"),
        new Poster("La La Land", "MUSICAL", "#6a42b8", "#342066"),
        new Poster("Nightcrawler", "CRIME", "#2b1a4d", "#1e1b26"),
        new Poster("Her", "ROMANCE", "#553199", "#221c35"),
        new Poster("Knives Out", "MYSTERY", "#342066", "#4a2b85"),
        new Poster("Mad Max", "ACTION", "#1e1b26", "#3d2470"),
        new Poster("Moonlight", "DRAMA", "#221c35", "#553199"),
        new Poster("Whiplash", "MUSIC", "#553199", "#1e1b26"),
        new Poster("The Batman", "CRIME", "#2b1a4d", "#342066")
    );

    /** One row in the history list. */
    private record Round(String title, String year, String initials, String guesses,
                         String result, String score, String when, String accent) { }

    private static final List<Round> ROUNDS = List.of(
        new Round("Blade Runner 2049", "2017", "BR", "2 guesses", "WIN", "+1,250", "Today, 14:32", LIME),
        new Round("Dune: Part Two", "2024", "D2", "3 guesses", "LOSS", "+0", "Today, 13:10", VIOLET),
        new Round("Arrival", "2016", "AR", "1 guess", "WIN", "+1,500", "Yesterday, 21:05", LIME),
        new Round("Whiplash", "2014", "WH", "3 guesses", "LOSS", "+0", "Yesterday, 19:44", VIOLET),
        new Round("Parasite", "2019", "PA", "2 guesses", "WIN", "+1,250", "Nov 12, 18:20", LIME),
        new Round("Interstellar", "2014", "IN", "3 guesses", "LOSS", "+0", "Nov 11, 22:10", VIOLET)
    );

    // -------------------------------------------------------------------- ui

    private final BorderPane root = new BorderPane();
    private final StackPane pageHost = new StackPane();
    private final Map<String, Node> pages = new HashMap<>();
    private final Map<String, NavItem> navItems = new HashMap<>();
    private final List<Label> sidebarLabels = new ArrayList<>();

    private Stage mainStage;
    private VBox sidebar;
    private HBox brandRow;
    private HBox profileRow;
    private VBox promoCard;
    private Label pageTitle;

    private boolean sidebarOpen = true;
    private String activePage = "singleplayer";
    private boolean switching;

    private VBox historyList;
    private Label historyCount;
    private final Map<String, Button> historyFilters = new HashMap<>();
    private String historyFilter = "all";

    private final List<Button> difficultyButtons = new ArrayList<>();
    private Label settingsStatus;

    // =============================================================== lifecycle

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        mainStage = stage;

        pageHost.setId("pageHost");
        pageHost.setStyle("-fx-background-color: " + BLACK + ";");

        Node sidebarNode = buildSidebar();
        BorderPane main = new BorderPane();
        main.setTop(buildHeader());
        main.setCenter(pageHost);

        addPage("singleplayer", singleplayerPage());
        addPage("history", historyPage());
        addPage("settings", settingsPage());

        Node first = pages.get("singleplayer");
        first.setVisible(true);
        first.setManaged(true);
        first.setOpacity(1);

        root.setLeft(sidebarNode);
        root.setCenter(main);
        root.setStyle("-fx-background-color: " + BLACK + ";");

        Scene scene = makeScene(root, 1320, 860);
        stage.setScene(scene);
        stage.setTitle("GuessIt - Sliding sidebar");
        stage.setMinWidth(Math.min(1080, scene.getWidth()));
        stage.setMinHeight(Math.min(700, scene.getHeight()));
        centerStage(stage);
        stage.show();

        updateNavActive();
        setSidebar(true, false);
    }

    /** Builds a scene that fits the available screen, scaled for HiDPI displays. */
    static Scene makeScene(javafx.scene.Parent content, double maxWidth, double maxHeight) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double scale = Math.max(1.0, Screen.getPrimary().getOutputScaleX());
        double width = Math.min(maxWidth, bounds.getWidth() / scale - 40);
        double height = Math.min(maxHeight, bounds.getHeight() / scale - 40);
        Scene scene = new Scene(content, width, height);
        scene.setFill(Color.web(BLACK));
        return scene;
    }

    /** Explicitly centres a stage instead of trusting the window manager. */
    static void centerStage(Stage stage) {
        if (stage.getScene() == null) {
            return;
        }
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double scale = Math.max(1.0, Screen.getPrimary().getOutputScaleX());
        double usableWidth = bounds.getWidth() / scale;
        double usableHeight = bounds.getHeight() / scale;
        stage.setX(bounds.getMinX() / scale
            + Math.max(0, (usableWidth - stage.getScene().getWidth()) / 2));
        stage.setY(bounds.getMinY() / scale
            + Math.max(0, (usableHeight - stage.getScene().getHeight()) / 2));
    }

    // ================================================================ sidebar

    private Node buildSidebar() {
        FontIcon logoIcon = new FontIcon(Feather.FILM);
        logoIcon.setIconSize(20);
        logoIcon.setIconColor(Color.BLACK);
        StackPane logoBadge = new StackPane(logoIcon);
        logoBadge.setMinSize(40, 40);
        logoBadge.setPrefSize(40, 40);
        logoBadge.setMaxSize(40, 40);
        logoBadge.setStyle("-fx-background-color: " + LIME + "; -fx-background-radius: 12;");

        Label brandName = label("GUESSIT", 18, FontWeight.BLACK, WHITE);
        Label brandSub = label("MOVIE QUIZ", 9, FontWeight.BOLD, MUTED);
        VBox brandText = new VBox(0, brandName, brandSub);
        brandRow = new HBox(11, logoBadge, brandText);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        NavItem single = navItem("singleplayer", Feather.PLAY, "Singleplayer");
        NavItem history = navItem("history", Feather.CLOCK, "History");
        NavItem settings = navItem("settings", Feather.SETTINGS, "Settings");

        VBox nav = new VBox(6, single.button, history.button, settings.button);

        Label promoTitle = label("DAILY STREAK", 9, FontWeight.BOLD, LIME);
        Label promoValue = label("6 days strong", 12, FontWeight.BOLD, WHITE);
        promoCard = new VBox(3, promoTitle, promoValue);
        promoCard.setPadding(new Insets(12, 12, 12, 12));
        promoCard.setStyle("-fx-background-color: #17141f; -fx-background-radius: 12;"
            + " -fx-border-color: #2b2735; -fx-border-radius: 12; -fx-border-width: 1;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Circle avatar = new Circle(16, Color.web(VIOLET));
        Label avatarText = label("G", 12, FontWeight.BLACK, WHITE);
        StackPane avatarPane = new StackPane(avatar, avatarText);
        Label profileName = label("Guest Player", 12, FontWeight.SEMI_BOLD, WHITE);
        Label profileMeta = label("Level 1", 10, FontWeight.NORMAL, MUTED);
        VBox profileText = new VBox(0, profileName, profileMeta);
        profileRow = new HBox(10, avatarPane, profileText);
        profileRow.setAlignment(Pos.CENTER_LEFT);

        Label version = label("v1.0.0  -  demo build", 9, FontWeight.NORMAL, "#6f6980");
        VBox footer = new VBox(12, new Separator(), profileRow, version);
        footer.setPadding(new Insets(4, 0, 0, 0));

        sidebar = new VBox(22, brandRow, nav, spacer, promoCard, footer);
        sidebar.setId("sidebar");
        sidebar.setPadding(new Insets(22, 14, 20, 14));
        sidebar.setPrefWidth(SIDEBAR_OPEN);
        sidebar.setMinWidth(SIDEBAR_CLOSED);
        sidebar.setMaxWidth(SIDEBAR_OPEN);
        sidebar.setStyle("-fx-background-color: " + GRAPHITE + ";"
            + " -fx-border-color: transparent " + BORDER + " transparent transparent;"
            + " -fx-border-width: 0 1 0 0;");

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebar.widthProperty());
        clip.heightProperty().bind(sidebar.heightProperty());
        sidebar.setClip(clip);

        sidebarLabels.add(brandName);
        sidebarLabels.add(brandSub);
        sidebarLabels.add(promoTitle);
        sidebarLabels.add(promoValue);
        sidebarLabels.add(profileName);
        sidebarLabels.add(profileMeta);
        sidebarLabels.add(version);

        return sidebar;
    }

    private NavItem navItem(String key, Feather glyph, String text) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(18);
        icon.setIconColor(Color.web("#b8b2c6"));

        Label label = new Label(text);
        label.setFont(Font.font(FONT, FontWeight.SEMI_BOLD, 14));
        label.setTextFill(Color.web("#b8b2c6"));
        label.setMinWidth(0);

        HBox row = new HBox(13, icon, label);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinWidth(0);

        Button button = new Button();
        button.setId("nav" + Character.toUpperCase(key.charAt(0)) + key.substring(1));
        button.setGraphic(row);
        button.setMinWidth(0);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(12, 14, 12, 14));
        button.setStyle(idleNavStyle());
        button.setOnAction(event -> selectPage(key));
        button.setOnMouseEntered(event -> {
            if (!key.equals(activePage)) {
                button.setStyle(hoverNavStyle());
            }
        });
        button.setOnMouseExited(event -> {
            if (!key.equals(activePage)) {
                button.setStyle(idleNavStyle());
            }
        });

        NavItem item = new NavItem(key, button, icon, label);
        navItems.put(key, item);
        sidebarLabels.add(label);
        return item;
    }

    private void toggleSidebar() {
        setSidebar(!sidebarOpen, true);
    }

    private void setSidebar(boolean open, boolean animate) {
        if (sidebar == null) {
            return;
        }
        sidebarOpen = open;

        if (!animate) {
            sidebar.setPrefWidth(open ? SIDEBAR_OPEN : SIDEBAR_CLOSED);
            for (Label label : sidebarLabels) {
                label.setVisible(open);
                label.setManaged(open);
                label.setOpacity(open ? 1 : 0);
            }
            promoCard.setVisible(open);
            promoCard.setManaged(open);
            applySidebarAlignment(open);
            return;
        }

        if (open) {
            for (Label label : sidebarLabels) {
                label.setVisible(true);
                label.setManaged(true);
            }
            promoCard.setVisible(true);
            promoCard.setManaged(true);
            applySidebarAlignment(true);
        }

        Timeline width = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth())),
            new KeyFrame(Duration.millis(260),
                new KeyValue(sidebar.prefWidthProperty(), open ? SIDEBAR_OPEN : SIDEBAR_CLOSED,
                    Interpolator.EASE_BOTH)));

        ParallelTransition transition = new ParallelTransition(width);
        for (Label label : sidebarLabels) {
            FadeTransition fade = new FadeTransition(Duration.millis(open ? 180 : 110), label);
            fade.setToValue(open ? 1 : 0);
            fade.setDelay(open ? Duration.millis(70) : Duration.ZERO);
            transition.getChildren().add(fade);
        }
        transition.setOnFinished(event -> {
            if (!open) {
                for (Label label : sidebarLabels) {
                    label.setVisible(false);
                    label.setManaged(false);
                }
                promoCard.setVisible(false);
                promoCard.setManaged(false);
                applySidebarAlignment(false);
            }
        });
        transition.play();
    }

    private void applySidebarAlignment(boolean open) {
        if (brandRow != null) {
            brandRow.setAlignment(open ? Pos.CENTER_LEFT : Pos.CENTER);
        }
        if (profileRow != null) {
            profileRow.setAlignment(open ? Pos.CENTER_LEFT : Pos.CENTER);
        }
        for (NavItem item : navItems.values()) {
            item.button.setAlignment(open ? Pos.CENTER_LEFT : Pos.CENTER);
            item.button.setPadding(open ? new Insets(12, 14, 12, 14) : new Insets(12));
        }
    }

    private void updateNavActive() {
        for (NavItem item : navItems.values()) {
            boolean active = item.key.equals(activePage);
            item.button.setStyle(active ? activeNavStyle() : idleNavStyle());
            item.icon.setIconColor(active ? Color.BLACK : Color.web("#b8b2c6"));
            item.label.setTextFill(active ? Color.BLACK : Color.web("#b8b2c6"));
        }
    }

    private static String idleNavStyle() {
        return "-fx-background-color: transparent; -fx-background-radius: 12;"
            + " -fx-text-fill: " + WHITE + "; -fx-cursor: hand;";
    }

    private static String hoverNavStyle() {
        return "-fx-background-color: " + HOVER + "; -fx-background-radius: 12;"
            + " -fx-text-fill: " + WHITE + "; -fx-cursor: hand;";
    }

    private static String activeNavStyle() {
        return "-fx-background-color: " + LIME + "; -fx-background-radius: 12;"
            + " -fx-text-fill: " + BLACK + "; -fx-cursor: hand;";
    }

    // ================================================================= header

    private Node buildHeader() {
        Button toggle = iconButton(Feather.MENU, "Toggle sidebar");
        toggle.setId("sidebarToggle");
        toggle.setOnAction(event -> toggleSidebar());

        pageTitle = label("Singleplayer", 16, FontWeight.BOLD, WHITE);
        pageTitle.setId("pageTitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button search = iconButton(Feather.SEARCH, "Search");
        search.setId("headerSearch");
        Button bell = iconButton(Feather.BELL, "Notifications");
        bell.setId("headerBell");

        Label avatarText = label("G", 12, FontWeight.BLACK, WHITE);
        StackPane avatar = new StackPane(new Circle(17, Color.web(VIOLET)), avatarText);

        HBox header = new HBox(14, toggle, pageTitle, spacer, search, bell, avatar);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 30, 16, 24));
        header.setStyle("-fx-background-color: " + BLACK + ";"
            + " -fx-border-color: transparent transparent #201c28 transparent;"
            + " -fx-border-width: 0 0 1 0;");
        return header;
    }

    // ============================================================ page switch

    private void addPage(String key, Node page) {
        page.setVisible(false);
        page.setManaged(false);
        page.setOpacity(0);
        pages.put(key, page);
        pageHost.getChildren().add(page);
    }

    private void selectPage(String key) {
        if (!pages.containsKey(key) || switching) {
            return;
        }
        Node next = pages.get(key);
        Node current = pages.get(activePage);
        if (next == current && next.isVisible()) {
            return;
        }

        activePage = key;
        updateNavActive();
        pageTitle.setText(titleOf(key));

        if (current == null || current == next) {
            next.setVisible(true);
            next.setManaged(true);
            next.setOpacity(1);
            next.setTranslateX(0);
            return;
        }

        switching = true;
        next.setVisible(true);
        next.setManaged(true);
        next.setOpacity(0);
        next.setTranslateX(26);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(130), current);
        fadeOut.setToValue(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(130), current);
        slideOut.setToX(-16);
        ParallelTransition exit = new ParallelTransition(fadeOut, slideOut);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), next);
        fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(290), next);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition enter = new ParallelTransition(fadeIn, slideIn);

        exit.setOnFinished(event -> {
            current.setVisible(false);
            current.setManaged(false);
            current.setOpacity(1);
            current.setTranslateX(0);
            enter.setOnFinished(done -> switching = false);
            enter.play();
        });
        exit.play();
    }

    private static String titleOf(String key) {
        switch (key) {
            case "history":
                return "History";
            case "settings":
                return "Settings";
            default:
                return "Singleplayer";
        }
    }

    // ========================================================== singleplayer

    private Node singleplayerPage() {
        StackPane page = new StackPane();
        page.setId("singleplayerPage");

        Node wall = posterWall();

        Region shade = new Region();
        shade.setMouseTransparent(true);
        shade.setStyle("-fx-background-color: linear-gradient(to right,"
            + " rgba(0,0,0,0.93) 0%, rgba(0,0,0,0.80) 32%,"
            + " rgba(0,0,0,0.42) 62%, rgba(0,0,0,0.82) 100%);");

        Region bottomFade = new Region();
        bottomFade.setMouseTransparent(true);
        bottomFade.setStyle("-fx-background-color: linear-gradient(to bottom,"
            + " rgba(0,0,0,0) 42%, rgba(0,0,0,0.9) 100%);");

        HBox kicker = new HBox(pill("SINGLEPLAYER  ·  MOVIE QUIZ", LIME, BLACK, Feather.PLAY));

        Label headline = label("NAME THAT MOVIE.\nONE SCENE AT A TIME.", 52,
            FontWeight.BLACK, WHITE);
        headline.setLineSpacing(-4);

        Label subtitle = label("One iconic scene. Three guesses. Sixty seconds on the clock. "
            + "See how many films you can name before the timer runs out.",
            15, FontWeight.NORMAL, SOFT_TEXT);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(560);

        FontIcon playIcon = new FontIcon(Feather.PLAY);
        playIcon.setIconSize(14);
        playIcon.setIconColor(Color.web(BLACK));
        Button play = new Button("PLAY NOW", playIcon);
        play.setId("play");
        play.setStyle(limeButtonStyle());
        addHover(play, limeButtonStyle(), limeHoverStyle());
        play.setOnAction(event -> openPlayStage());

        FontIcon helpIcon = new FontIcon(Feather.HELP_CIRCLE);
        helpIcon.setIconSize(14);
        helpIcon.setIconColor(Color.web(WHITE));
        Button how = new Button("HOW TO PLAY", helpIcon);
        how.setId("howToPlay");
        how.setStyle(violetButtonStyle());
        addHover(how, violetButtonStyle(), violetHoverStyle());
        how.setOnAction(event -> openTutorialStage());

        HBox actions = new HBox(12, play, how);
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox chips = new HBox(8,
            chip("5 ROUNDS", LIME),
            chip("60 SECONDS", WHITE),
            chip("3 GUESSES", WHITE));
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(16, kicker, headline, subtitle, actions, chips);
        copy.setPadding(new Insets(0, 60, 0, 68));
        copy.setMaxWidth(780);
        copy.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(copy, Pos.CENTER_LEFT);

        page.getChildren().addAll(wall, shade, bottomFade, copy);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(page.widthProperty());
        clip.heightProperty().bind(page.heightProperty());
        page.setClip(clip);

        return page;
    }

    /** Full-bleed wall of poster tiles that drift slowly in alternating directions. */
    private static Node posterWall() {
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
            strip.setTranslateY(downward ? -2288 : 0);

            Pane view = new Pane(strip);
            view.setPrefWidth(180);
            view.setMinWidth(180);
            view.setMaxWidth(180);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(view.widthProperty());
            clip.heightProperty().bind(view.heightProperty());
            view.setClip(clip);
            wall.getChildren().add(view);

            marquee(strip, downward, 78 + column * 8);
        }

        return wall;
    }

    private static void marquee(VBox strip, boolean downward, double seconds) {
        double period = 2288;
        Timeline drift = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(strip.translateYProperty(), downward ? -period : 0)),
            new KeyFrame(Duration.seconds(seconds),
                new KeyValue(strip.translateYProperty(), downward ? 0 : -period,
                    Interpolator.LINEAR)));
        drift.setCycleCount(Animation.INDEFINITE);
        drift.play();
    }

    private static StackPane posterCard(Poster poster) {
        double width = 180;
        double height = 270;

        StackPane card = new StackPane();
        card.setMinSize(width, height);
        card.setPrefSize(width, height);
        card.setMaxSize(width, height);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + poster.from + ", " + poster.to + "); -fx-background-radius: 14;"
            + " -fx-border-color: rgba(255,255,255,0.07); -fx-border-radius: 14;"
            + " -fx-border-width: 1;");

        Image artwork = AssetCatalog.poster(slugify(poster.title));
        if (artwork != null && !artwork.isError()) {
            ImageView view = new ImageView(artwork);
            view.setSmooth(true);
            view.setMouseTransparent(true);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setViewport(coverViewport(artwork, width, height));
            card.getChildren().add(view);
        } else {
            Label initial = label(poster.title.substring(0, 1), 84, FontWeight.BLACK,
                Color.web(WHITE, 0.10));
            StackPane.setAlignment(initial, Pos.CENTER);
            card.getChildren().add(initial);
        }

        Region scrim = new Region();
        scrim.setMouseTransparent(true);
        scrim.setStyle("-fx-background-color: linear-gradient(to bottom,"
            + " rgba(0,0,0,0) 40%, rgba(0,0,0,0.52) 68%, rgba(0,0,0,0.88) 100%);");

        Label title = label(poster.title.toUpperCase(Locale.ROOT), 14, FontWeight.BOLD, WHITE);
        title.setWrapText(true);
        title.setMaxWidth(width - 26);

        Label tag = label(poster.tag, 8, FontWeight.BOLD, Color.web(WHITE, 0.62));

        VBox meta = new VBox(3, tag, title);
        meta.setPadding(new Insets(13));
        StackPane.setAlignment(meta, Pos.BOTTOM_LEFT);

        card.getChildren().addAll(scrim, meta);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        card.setClip(clip);

        return card;
    }

    /** Centre-crops a poster image so it covers a 2:3 card without stretching. */
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

    /** "Dune: Part Two" becomes "dune-part-two", matching the poster file names. */
    static String slugify(String text) {
        return text.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    private void openPlayStage() {
        Movie_Playing_Demo_Page.openPlay(mainStage);
    }

    private void openTutorialStage() {
        Movie_Playing_Demo_Page.openTutorial(mainStage);
    }

    // ================================================================ history

    private Node historyPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 38, 46, 38));
        content.setMaxWidth(1180);

        HBox eyebrowRow = new HBox(10, pill("HISTORY", VIOLET, WHITE, Feather.CLOCK));
        eyebrowRow.setAlignment(Pos.CENTER_LEFT);

        Label title = label("Your game history", 34, FontWeight.BLACK, WHITE);
        Label subtitle = label("Every round you have played, with the guesses it took "
            + "and the points you earned.", 14, FontWeight.NORMAL, SOFT_TEXT);
        subtitle.setWrapText(true);

        historyCount = label("", 11, FontWeight.BOLD, MUTED);
        historyCount.setId("historyCount");

        HBox filters = new HBox(8);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getChildren().addAll(
            filterButton("all", "All rounds"),
            filterButton("wins", "Wins"),
            filterButton("losses", "Losses"));
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        HBox filterRow = new HBox(8, filters, filterSpacer, historyCount);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        historyList = new VBox(0);
        historyList.setId("historyList");

        VBox card = new VBox(historyList);
        card.setPadding(new Insets(4, 14, 4, 14));
        card.setStyle("-fx-background-color: " + GRAPHITE + "; -fx-background-radius: 20;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-border-width: 1;");

        content.getChildren().addAll(eyebrowRow, title, subtitle, filterRow, card);

        renderHistory();

        VBox wrapper = new VBox(content);
        wrapper.setStyle("-fx-background-color: " + BLACK + ";");
        ScrollPane scroll = scroll(wrapper);
        scroll.setId("historyPage");
        return scroll;
    }

    private Button filterButton(String key, String text) {
        Button button = new Button(text);
        button.setId("filter" + Character.toUpperCase(key.charAt(0)) + key.substring(1));
        button.setOnAction(event -> {
            historyFilter = key;
            updateFilterStyles();
            renderHistory();
        });
        historyFilters.put(key, button);
        styleFilter(button, key.equals(historyFilter));
        return button;
    }

    private void updateFilterStyles() {
        for (Map.Entry<String, Button> entry : historyFilters.entrySet()) {
            styleFilter(entry.getValue(), entry.getKey().equals(historyFilter));
        }
    }

    private static void styleFilter(Button button, boolean active) {
        button.setStyle("-fx-background-color: " + (active ? LIME : "transparent") + ";"
            + " -fx-text-fill: " + (active ? BLACK : SOFT_TEXT) + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 11px; -fx-font-weight: bold;"
            + " -fx-background-radius: 999; -fx-border-color: " + (active ? LIME : BORDER) + ";"
            + " -fx-border-radius: 999; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
    }

    private void renderHistory() {
        if (historyList == null) {
            return;
        }
        historyList.getChildren().clear();
        int shown = 0;
        for (Round round : ROUNDS) {
            if ("wins".equals(historyFilter) && !"WIN".equals(round.result)) {
                continue;
            }
            if ("losses".equals(historyFilter) && !"LOSS".equals(round.result)) {
                continue;
            }
            historyList.getChildren().add(historyRow(round));
            shown++;
        }
        if (shown == 0) {
            Label empty = label("No rounds in this filter yet.", 12, FontWeight.NORMAL, MUTED);
            empty.setPadding(new Insets(28));
            historyList.getChildren().add(empty);
        }
        if (historyCount != null) {
            historyCount.setText(shown + " / " + ROUNDS.size() + " ROUNDS");
        }
    }

    private HBox historyRow(Round round) {
        Label initials = label(round.initials, 12, FontWeight.BLACK, WHITE);
        StackPane poster = new StackPane(initials);
        poster.setMinSize(46, 46);
        poster.setPrefSize(46, 46);
        poster.setMaxSize(46, 46);
        poster.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + round.accent + ", " + DEEP_VIOLET + "); -fx-background-radius: 12;");

        Label title = label(round.title, 14, FontWeight.SEMI_BOLD, WHITE);
        Label meta = label(round.year + "   -   " + round.guesses + "   -   " + round.when,
            11, FontWeight.NORMAL, MUTED);
        VBox info = new VBox(3, title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean win = "WIN".equals(round.result);
        Label score = label(round.score, 13, FontWeight.BOLD, win ? LIME : MUTED);

        Label badge = label(round.result, 9, FontWeight.BLACK, win ? BLACK : WHITE);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: " + (win ? LIME : VIOLET) + ";"
            + " -fx-background-radius: 999;");

        HBox row = new HBox(14, poster, info, spacer, score, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 10, 12, 10));
        row.setStyle("-fx-border-color: transparent transparent " + BORDER + " transparent;"
            + " -fx-border-width: 0 0 1 0;");
        row.setOnMouseEntered(event -> row.setStyle(
            "-fx-background-color: " + HOVER + "; -fx-background-radius: 12;"));
        row.setOnMouseExited(event -> row.setStyle(
            "-fx-background-color: transparent; -fx-background-radius: 12;"
                + " -fx-border-color: transparent transparent " + BORDER + " transparent;"
                + " -fx-border-width: 0 0 1 0;"));
        return row;
    }

    // =============================================================== settings

    private Node settingsPage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 38, 46, 38));
        content.setMaxWidth(900);

        HBox eyebrowRow = new HBox(10, pill("SETTINGS", LIME, BLACK, Feather.SETTINGS));
        eyebrowRow.setAlignment(Pos.CENTER_LEFT);

        Label title = label("Make it yours", 34, FontWeight.BLACK, WHITE);
        Label subtitle = label("Audio, difficulty and appearance. Changes apply instantly in this demo.",
            14, FontWeight.NORMAL, SOFT_TEXT);
        subtitle.setWrapText(true);

        ToggleButton sound = toggleSwitch("soundToggle", true);
        ToggleButton music = toggleSwitch("musicToggle", false);

        VBox audio = settingCard("Audio", Feather.VOLUME_2,
            settingRow("Sound effects", "Clicks, guesses and round results", sound),
            settingRow("Background music", "Low-key cinema ambience", music));

        Button easy = difficultyButton("difficultyEasy", "Easy", false);
        Button normal = difficultyButton("difficultyNormal", "Normal", true);
        Button hard = difficultyButton("difficultyHard", "Hard", false);
        HBox difficultyGroup = new HBox(6, easy, normal, hard);
        difficultyGroup.setAlignment(Pos.CENTER_LEFT);

        VBox gameplay = settingCard("Gameplay", Feather.TARGET,
            settingRow("Difficulty", "How much time you get per round", difficultyGroup));

        Button lime = accentButton("accentLime", "Verde Citrico", LIME, true);
        Button violet = accentButton("accentViolet", "Roxo", VIOLET, false);
        HBox accents = new HBox(8, lime, violet);
        accents.setAlignment(Pos.CENTER_LEFT);

        VBox appearance = settingCard("Appearance", Feather.SUN,
            settingRow("Accent colour", "Used for highlights and primary actions", accents));

        FontIcon resetIcon = new FontIcon(Feather.ROTATE_CCW);
        resetIcon.setIconColor(Color.WHITE);
        Button reset = new Button("Reset all progress", resetIcon);
        reset.setId("resetProgress");
        reset.setStyle(violetButtonStyle());
        addHover(reset, violetButtonStyle(), violetHoverStyle());
        reset.setOnAction(event -> settingsStatus.setText(
            "Demo only - no progress was changed, but the button works."));

        Label dangerText = label("Clear scores, streaks and the round history.", 12,
            FontWeight.NORMAL, MUTED);
        VBox danger = settingCard("Danger zone", Feather.ALERT_TRIANGLE, dangerText, reset);

        settingsStatus = label("All settings are stored in memory for this demo.", 11,
            FontWeight.NORMAL, MUTED);
        settingsStatus.setId("settingsStatus");

        content.getChildren().addAll(eyebrowRow, title, subtitle, audio, gameplay, appearance,
            danger, settingsStatus);

        VBox wrapper = new VBox(content);
        wrapper.setStyle("-fx-background-color: " + BLACK + ";");
        ScrollPane scroll = scroll(wrapper);
        scroll.setId("settingsPage");
        return scroll;
    }

    private VBox settingCard(String title, Feather icon, Node... rows) {
        FontIcon glyph = new FontIcon(icon);
        glyph.setIconSize(16);
        glyph.setIconColor(Color.web(LIME));
        StackPane badge = new StackPane(glyph);
        badge.setMinSize(34, 34);
        badge.setPrefSize(34, 34);
        badge.setMaxSize(34, 34);
        badge.setStyle("-fx-background-color: " + DEEP_VIOLET + "; -fx-background-radius: 10;");

        Label heading = label(title, 14, FontWeight.BOLD, WHITE);
        HBox head = new HBox(10, badge, heading);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, head);
        card.getChildren().addAll(rows);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color: " + GRAPHITE + "; -fx-background-radius: 18;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 18; -fx-border-width: 1;");
        return card;
    }

    private static HBox settingRow(String title, String subtitle, Node control) {
        Label name = label(title, 13, FontWeight.SEMI_BOLD, WHITE);
        Label meta = label(subtitle, 11, FontWeight.NORMAL, MUTED);
        meta.setWrapText(true);
        VBox text = new VBox(2, name, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, text, spacer, control);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        return row;
    }

    private static ToggleButton toggleSwitch(String id, boolean selected) {
        ToggleButton toggle = new ToggleButton();
        toggle.setId(id);
        toggle.setSelected(selected);
        toggle.setMinWidth(56);
        refreshToggle(toggle);
        toggle.selectedProperty().addListener((observable, oldValue, newValue) -> refreshToggle(toggle));
        return toggle;
    }

    private static void refreshToggle(ToggleButton toggle) {
        boolean on = toggle.isSelected();
        toggle.setText(on ? "ON" : "OFF");
        toggle.setStyle("-fx-background-color: " + (on ? LIME : "#2a2633") + ";"
            + " -fx-text-fill: " + (on ? BLACK : MUTED) + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 9px; -fx-font-weight: bold;"
            + " -fx-background-radius: 999; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
    }

    private Button difficultyButton(String id, String text, boolean active) {
        Button button = new Button(text);
        button.setId(id);
        button.setUserData(text);
        difficultyButtons.add(button);
        refreshDifficulty(button, active);
        button.setOnAction(event -> {
            for (Button other : difficultyButtons) {
                refreshDifficulty(other, other == button);
            }
            if (settingsStatus != null) {
                settingsStatus.setText("Difficulty set to " + text + ".");
            }
        });
        return button;
    }

    private static void refreshDifficulty(Button button, boolean active) {
        button.setStyle("-fx-background-color: " + (active ? LIME : "transparent") + ";"
            + " -fx-text-fill: " + (active ? BLACK : SOFT_TEXT) + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 11px; -fx-font-weight: bold;"
            + " -fx-background-radius: 999; -fx-border-color: " + (active ? LIME : BORDER) + ";"
            + " -fx-border-radius: 999; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
    }

    private Button accentButton(String id, String text, String colour, boolean active) {
        Circle dot = new Circle(5, Color.web(colour));
        Button button = new Button(text, dot);
        button.setId(id);
        button.setStyle(accentStyle(active));
        button.setOnAction(event -> {
            button.setStyle(accentStyle(true));
            if (settingsStatus != null) {
                settingsStatus.setText("Accent preview set to " + text + ".");
            }
        });
        return button;
    }

    private static String accentStyle(boolean active) {
        return "-fx-background-color: " + (active ? "#262130" : "transparent") + ";"
            + " -fx-text-fill: " + (active ? WHITE : SOFT_TEXT) + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 11px; -fx-font-weight: bold;"
            + " -fx-background-radius: 999; -fx-border-color: " + (active ? LIME : BORDER) + ";"
            + " -fx-border-radius: 999; -fx-padding: 7 14 7 14; -fx-cursor: hand;";
    }

    // ================================================================ helpers

    private static ScrollPane scroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background-color: " + BLACK + "; -fx-background: " + BLACK + ";");
        return scroll;
    }

    static Label label(String text, double size, FontWeight weight, String colour) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT, weight, size));
        label.setTextFill(Color.web(colour));
        return label;
    }

    static Label label(String text, double size, FontWeight weight, Color colour) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT, weight, size));
        label.setTextFill(colour);
        return label;
    }

    private static HBox pill(String text, String background, String foreground, Feather glyph) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(11);
        icon.setIconColor(Color.web(foreground));
        Label label = label(text, 9, FontWeight.BOLD, foreground);
        HBox box = new HBox(6, icon, label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 11, 5, 11));
        box.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 999;");
        box.setMaxWidth(Region.USE_PREF_SIZE);
        return box;
    }

    private static HBox chip(String text, String colour) {
        Label label = label(text, 9, FontWeight.BOLD, colour);
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setStyle("-fx-background-color: #17141f; -fx-background-radius: 999;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 999; -fx-border-width: 1;");
        return box;
    }

    private Button iconButton(Feather glyph, String tooltip) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(18);
        icon.setIconColor(Color.web(SOFT_TEXT));
        Button button = new Button();
        button.setGraphic(icon);
        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        button.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;"
            + " -fx-padding: 9; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: " + HOVER + "; -fx-background-radius: 10;"
                + " -fx-padding: 9; -fx-cursor: hand;");
            icon.setIconColor(Color.WHITE);
        });
        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;"
                + " -fx-padding: 9; -fx-cursor: hand;");
            icon.setIconColor(Color.web(SOFT_TEXT));
        });
        return button;
    }

    static String limeButtonStyle() {
        return "-fx-background-color: " + LIME + "; -fx-text-fill: " + BLACK + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px; -fx-font-weight: bold;"
            + " -fx-background-radius: 14; -fx-padding: 13 24 13 24; -fx-cursor: hand;";
    }

    static String limeHoverStyle() {
        return "-fx-background-color: " + LIME_HOVER + "; -fx-text-fill: " + BLACK + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px; -fx-font-weight: bold;"
            + " -fx-background-radius: 14; -fx-padding: 13 24 13 24; -fx-cursor: hand;";
    }

    static String violetButtonStyle() {
        return "-fx-background-color: " + VIOLET + "; -fx-text-fill: " + WHITE + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px; -fx-font-weight: bold;"
            + " -fx-background-radius: 14; -fx-padding: 13 24 13 24; -fx-cursor: hand;";
    }

    static String violetHoverStyle() {
        return "-fx-background-color: #6a42b8; -fx-text-fill: " + WHITE + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px; -fx-font-weight: bold;"
            + " -fx-background-radius: 14; -fx-padding: 13 24 13 24; -fx-cursor: hand;";
    }

    static String ghostButtonStyle() {
        return "-fx-background-color: #1b1724; -fx-text-fill: " + SOFT_TEXT + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 10; -fx-border-color: " + BORDER + ";"
            + " -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 10 14 10 14;"
            + " -fx-cursor: hand;";
    }

    static String ghostHoverStyle() {
        return "-fx-background-color: #221c35; -fx-text-fill: " + WHITE + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 10px; -fx-font-weight: bold;"
            + " -fx-background-radius: 10; -fx-border-color: " + LIME + ";"
            + " -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 10 14 10 14;"
            + " -fx-cursor: hand;";
    }

    static void addHover(ButtonBase button, String normal, String hover) {
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }

    /** One sidebar navigation entry. */
    private static final class NavItem {
        final String key;
        final Button button;
        final FontIcon icon;
        final Label label;

        NavItem(String key, Button button, FontIcon icon, Label label) {
            this.key = key;
            this.button = button;
            this.icon = icon;
            this.label = label;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
