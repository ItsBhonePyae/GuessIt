package com.guessit.actualapp.view;

import com.guessit.actualapp.dao.UserDao;
import com.guessit.actualapp.model.Session;
import com.guessit.actualapp.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Shared shell for the sign-in and registration screens: one wide ticket card,
 * split into a brick "stub" panel on the left and the tabbed form on the right.
 *
 * <p>The ticket silhouette (saw-tooth sides and the two seam notches) is painted
 * once onto a static canvas; nothing in the card is clipped per frame and there
 * are no per-node effects, so resizing and hover states stay cheap.</p>
 */
public class AuthView extends StackPane {

    protected enum Mode { LOGIN, REGISTER }

    /* card frame */
    private static final double CARD_W = 940;
    private static final double CARD_H = 600;
    private static final double EDGE_PAD = 18;
    private static final double SEAM = 404;
    private static final double SPIKE = 8;
    private static final double NOTCH = 13;

    /* graphic language */
    private static final String BRICK = "#8f2429";
    private static final String CARD_FILL = "#212121";
    private static final String CARD_EDGE = "#3a3a3a";
    private static final String INPUT_FILL = "#171717";
    private static final String INPUT_EDGE = "#343434";
    private static final String INK_ON_BRASS = "#1a1409";

    private final Mode initialMode;

    private final StackPane formHost = new StackPane();
    private Node loginForm;
    private Node registerForm;

    private final TextField loginUser = new TextField();
    private final PasswordField loginPass = new PasswordField();
    private final Label loginStatus = statusLabel();

    private final TextField regUser = new TextField();
    private final TextField regEmail = new TextField();
    private final PasswordField regPass = new PasswordField();
    private final PasswordField regConfirm = new PasswordField();
    private final Label registerStatus = statusLabel();

    private final DoubleProperty tabPosition = new SimpleDoubleProperty(0);
    private final SimpleBooleanProperty rememberTicket = new SimpleBooleanProperty(false);

    private Label signInTab;
    private Label registerTab;

    public AuthView(Mode mode) {
        this.initialMode = mode;
        build();
        Platform.runLater(() -> animateEntrance(getChildren().get(getChildren().size() - 1)));
    }

    /* ------------------------------------------------------------- shell */

    private void build() {
        setStyle("-fx-background-color: " + Theme.INK + ";");

        Region photo = Theme.photoBackdrop("/assets/backgrounds/cinema-auditorium.jpg");

        Region scrim = new Region();
        scrim.setMouseTransparent(true);
        scrim.setStyle("-fx-background-color: linear-gradient(to right,"
            + " rgba(6,7,9,0.62) 0%, rgba(6,7,9,0.34) 38%,"
            + " rgba(6,7,9,0.42) 68%, rgba(6,7,9,0.62) 100%);");

        HBox body = new HBox();
        Node brand = buildBrandPanel();
        Node form = buildFormPanel();
        HBox.setHgrow(form, Priority.ALWAYS);
        body.getChildren().addAll(brand, form);

        TicketCard card = new TicketCard(body);
        card.setMinSize(CARD_W + EDGE_PAD * 2, CARD_H + EDGE_PAD * 2);
        card.setPrefSize(CARD_W + EDGE_PAD * 2, CARD_H + EDGE_PAD * 2);
        card.setMaxSize(CARD_W + EDGE_PAD * 2, CARD_H + EDGE_PAD * 2);

        StackPane holder = new StackPane(card);
        holder.setPadding(new Insets(12));

        getChildren().addAll(photo, scrim, Theme.vignette(), holder);
    }

    /* ------------------------------------------------------- brand panel */

    private Node buildBrandPanel() {
        Label admit = Theme.mono("ADMIT ONE \u2014 #70382", 8.5, FontWeight.BOLD, "#e4d9c9");
        admit.setPadding(new Insets(5, 11, 5, 11));
        admit.setStyle("-fx-background-color: rgba(0,0,0,0.22);"
            + " -fx-background-radius: 999;"
            + " -fx-border-color: rgba(240,230,215,0.34);"
            + " -fx-border-radius: 999; -fx-border-width: 1;");

        FontIcon clapper = new FontIcon(Feather.FILM);
        clapper.setIconSize(20);
        clapper.setIconColor(Color.web("#e4d9c9", 0.70));

        Region topGap = new Region();
        topGap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(topGap, Priority.ALWAYS);
        HBox top = new HBox(admit, topGap, clapper);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane(new FontIcon(Feather.FILM));
        FontIcon badgeIcon = (FontIcon) badge.getChildren().get(0);
        badgeIcon.setIconSize(26);
        badgeIcon.setIconColor(Color.web(Theme.LIME));
        badge.setMinSize(58, 58);
        badge.setPrefSize(58, 58);
        badge.setMaxSize(58, 58);
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.20);"
            + " -fx-background-radius: 11;"
            + " -fx-border-color: rgba(240,230,215,0.24);"
            + " -fx-border-radius: 11; -fx-border-width: 1;");

        Label title = new Label("GUESSIT");
        title.setFont(Font.font(Theme.FONT, FontWeight.BLACK, 46));
        title.setTextFill(Color.web("#f4ece0"));
        DropShadow titleShadow = new DropShadow(12, Color.web("#2a0b0c", 0.55));
        titleShadow.setOffsetY(3);
        title.setEffect(titleShadow);

        Label tagline = Theme.label("TEST YOUR MOVIE IQ", 14, FontWeight.BOLD, Theme.LIME);

        Label copy = Theme.label(
            "Guess iconic movie scenes, legendary quotes and cinematic "
                + "masterpieces against cinephiles worldwide in real-time battles.",
            12.5, FontWeight.NORMAL, "#d5ccbf");
        copy.setWrapText(true);
        copy.setMaxWidth(300);
        copy.setLineSpacing(4);
        copy.setTextAlignment(TextAlignment.LEFT);

        Region growA = new Region();
        Region growB = new Region();
        growA.setMaxHeight(Double.MAX_VALUE);
        growB.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(growA, Priority.ALWAYS);
        VBox.setVgrow(growB, Priority.ALWAYS);

        Label rowSeat = Theme.mono("ROW 07 \u00b7 SEAT 12", 7.5, FontWeight.BOLD, "#e4d9c9");
        Label screen = Theme.mono("SCREEN 03 \u00b7 21:30", 7.5, FontWeight.BOLD, "#e4d9c9");
        VBox codes = new VBox(3, rowSeat, screen);
        codes.setOpacity(0.62);
        Region detailGap = new Region();
        detailGap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detailGap, Priority.ALWAYS);
        HBox details = new HBox(14, barcode(), detailGap, codes);
        details.setAlignment(Pos.CENTER_LEFT);

        Region footerRule = new Region();
        footerRule.setMinHeight(1);
        footerRule.setPrefHeight(1);
        footerRule.setMaxHeight(1);
        footerRule.setStyle("-fx-background-color: rgba(240,230,215,0.16);");

        Label vip = Theme.mono("VIP CINEMA PASS", 8.5, FontWeight.BOLD, "#d5c9ba");
        Label terms = Theme.mono("NON-TRANSFERABLE", 6.5, FontWeight.NORMAL, "#d5c9ba");
        Label est = Theme.mono("EST. 2024", 8.5, FontWeight.BOLD, "#d5c9ba");
        vip.setOpacity(0.72);
        terms.setOpacity(0.42);
        est.setOpacity(0.72);
        Region footerGap = new Region();
        footerGap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(footerGap, Priority.ALWAYS);
        HBox footer = new HBox(12, vip, footerGap, terms, est);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(top, growA, badge, title, tagline, copy, growB,
            details, footerRule, footer);
        panel.setPadding(new Insets(30, 26, 26, 38));
        panel.setMinWidth(SEAM);
        panel.setPrefWidth(SEAM);
        panel.setMaxWidth(SEAM);
        VBox.setMargin(badge, new Insets(0, 0, 20, 0));
        VBox.setMargin(title, new Insets(0, 0, 2, -3));
        VBox.setMargin(tagline, new Insets(0, 0, 18, 0));
        VBox.setMargin(details, new Insets(0, 0, 16, 0));
        VBox.setMargin(footerRule, new Insets(0, 0, 14, 0));

        return panel;
    }

    /** Static barcode for the stub; drawn once, never updated. */
    private static Canvas barcode() {
        Canvas canvas = new Canvas(92, 22);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#e4d9c9", 0.55));
        long seed = 70382L;
        int x = 0;
        while (x < 90) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int bar = 1 + Math.floorMod(seed >>> 33, 3);
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int gap = 1 + Math.floorMod(seed >>> 35, 3);
            gc.fillRect(x, 0, bar, 22);
            x += bar + gap;
        }
        canvas.setMouseTransparent(true);
        return canvas;
    }

    /* -------------------------------------------------------- form panel */

    private Node buildFormPanel() {
        loginForm = buildLoginForm();
        registerForm = buildRegisterForm();

        formHost.getChildren().addAll(loginForm, registerForm);
        formHost.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(loginForm, Pos.TOP_LEFT);
        StackPane.setAlignment(registerForm, Pos.TOP_LEFT);

        Node tabs = buildTabs();

        Region panelGrow = new Region();
        panelGrow.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(panelGrow, Priority.ALWAYS);

        Label legal = Theme.mono("BOX OFFICE \u00b7 GUESSIT \u00b7 EST. 2024",
            7, FontWeight.NORMAL, "#969696");
        legal.setMaxWidth(Double.MAX_VALUE);
        legal.setAlignment(Pos.CENTER_RIGHT);
        legal.setOpacity(0.55);

        VBox panel = new VBox(tabs, formHost, panelGrow, legal);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setPadding(new Insets(30, 42, 26, 38));
        VBox.setMargin(formHost, new Insets(26, 0, 0, 0));

        applyMode(initialMode, false);
        return panel;
    }

    private Node buildTabs() {
        signInTab = tabLabel("Sign In");
        registerTab = tabLabel("Register");

        GridPane row = new GridPane();
        row.setPrefHeight(46);
        row.setMinHeight(46);
        row.setMaxHeight(46);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        half.setHgrow(Priority.ALWAYS);
        row.getColumnConstraints().addAll(half, half);
        row.add(signInTab, 0, 0);
        row.add(registerTab, 1, 0);
        for (Label tab : new Label[]{signInTab, registerTab}) {
            tab.setMaxWidth(Double.MAX_VALUE);
            tab.setMinHeight(46);
            tab.setPrefHeight(46);
            tab.setMaxHeight(46);
            tab.setAlignment(Pos.CENTER);
            tab.setCursor(Cursor.HAND);
            GridPane.setHgrow(tab, Priority.ALWAYS);
            GridPane.setFillHeight(tab, true);
        }

        Region pill = new Region();
        pill.setMouseTransparent(true);
        pill.setStyle("-fx-background-color: " + Theme.LIME + "; -fx-background-radius: 4;");
        pill.prefWidthProperty().bind(row.widthProperty().divide(2));
        pill.maxWidthProperty().bind(row.widthProperty().divide(2));
        pill.maxHeightProperty().bind(row.heightProperty());
        pill.translateXProperty().bind(tabPosition.multiply(row.widthProperty().divide(2)));
        StackPane.setAlignment(pill, Pos.CENTER_LEFT);

        StackPane wrap = new StackPane(pill, row);
        wrap.setMinHeight(46);
        wrap.setPrefHeight(46);
        wrap.setMaxHeight(46);

        signInTab.setOnMouseClicked(event -> applyMode(Mode.LOGIN, true));
        registerTab.setOnMouseClicked(event -> applyMode(Mode.REGISTER, true));
        return wrap;
    }

    private Label tabLabel(String text) {
        Label tab = new Label(text);
        tab.setFont(Font.font(Theme.FONT, FontWeight.BOLD, 13.5));
        tab.setTextFill(Color.web(Theme.SOFT_TEXT));
        tab.setMaxWidth(Double.MAX_VALUE);
        tab.setAlignment(Pos.CENTER);
        return tab;
    }

    /* -------------------------------------------------------- login form */

    private Node buildLoginForm() {
        VBox form = new VBox();
        form.setMaxWidth(Double.MAX_VALUE);
        form.setFillWidth(true);

        Label emailLabel = fieldLabel("EMAIL ADDRESS");
        HBox email = iconField(loginUser, Feather.MAIL, "name@cinema.com");
        Label passLabel = fieldLabel("PASSWORD");
        HBox pass = iconField(loginPass, Feather.LOCK, "Password");

        loginUser.setOnAction(event -> handleLogin());
        loginPass.setOnAction(event -> handleLogin());

        loginStatus.setMinHeight(18);

        HBox options = new HBox(rememberToggle(), grow(), forgotLink());
        options.setAlignment(Pos.CENTER_LEFT);

        Button enter = primaryButton("ENTER THEATER", Feather.ARROW_RIGHT);
        enter.setOnAction(event -> handleLogin());

        Label caption = captionLabel("Or continue instantly with");
        HBox social = new HBox(12,
            socialButton("Google", Feather.GLOBE, () -> loginStatus.setText("Google sign-in isn't connected in this demo.")),
            socialButton("Guest Play", Feather.USER, this::playAsGuest));

        for (Node node : social.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        gap(emailLabel, 7);
        gap(email, 16);
        gap(passLabel, 7);
        gap(pass, 6);
        gap(loginStatus, 10);
        gap(options, 20);
        gap(enter, 24);
        Region rule = divider();
        gap(rule, 16);
        gap(caption, 12);

        form.getChildren().addAll(emailLabel, email, passLabel, pass, loginStatus,
            options, enter, rule, caption, social);
        return form;
    }

    /* ----------------------------------------------------- register form */

    private Node buildRegisterForm() {
        VBox form = new VBox();
        form.setMaxWidth(Double.MAX_VALUE);
        form.setFillWidth(true);

        Label userLabel = fieldLabel("USERNAME");
        HBox user = iconField(regUser, Feather.USER, "Your screen name");
        Label emailLabel = fieldLabel("EMAIL ADDRESS");
        HBox email = iconField(regEmail, Feather.MAIL, "name@cinema.com");
        Label passLabel = fieldLabel("PASSWORD");
        HBox pass = iconField(regPass, Feather.LOCK, "At least 4 characters");
        Label confirmLabel = fieldLabel("CONFIRM PASSWORD");
        HBox confirm = iconField(regConfirm, Feather.LOCK, "Repeat your password");

        regConfirm.setOnAction(event -> handleRegister());
        registerStatus.setMinHeight(18);

        Button create = primaryButton("CREATE TICKET", Feather.ARROW_RIGHT);
        create.setOnAction(event -> handleRegister());

        Label note = captionLabel("By creating a ticket you agree to the house rules.");
        note.setWrapText(true);

        gap(userLabel, 7);
        gap(user, 12);
        gap(emailLabel, 7);
        gap(email, 12);
        gap(passLabel, 7);
        gap(pass, 12);
        gap(confirmLabel, 7);
        gap(confirm, 6);
        gap(registerStatus, 8);
        gap(create, 22);
        Region rule = divider();
        gap(rule, 14);

        form.getChildren().addAll(userLabel, user, emailLabel, email,
            passLabel, pass, confirmLabel, confirm, registerStatus, create,
            rule, note);
        return form;
    }

    /* ------------------------------------------------------------- mode */

    private void applyMode(Mode mode, boolean animate) {
        boolean register = mode == Mode.REGISTER;

        Node incoming = register ? registerForm : loginForm;
        Node outgoing = register ? loginForm : registerForm;
        outgoing.setVisible(false);
        incoming.setVisible(true);

        setTabStyle(signInTab, !register);
        setTabStyle(registerTab, register);

        if (animate) {
            Timeline slide = new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(tabPosition, register ? 1 : 0, Interpolator.EASE_BOTH)));
            slide.play();

            incoming.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(180), incoming);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setInterpolator(Interpolator.EASE_OUT);
            fade.play();
        } else {
            tabPosition.set(register ? 1 : 0);
            incoming.setOpacity(1);
        }
    }

    private void setTabStyle(Label tab, boolean active) {
        tab.setTextFill(Color.web(active ? INK_ON_BRASS : Theme.SOFT_TEXT));
        if (active) {
            tab.setOnMouseEntered(null);
            tab.setOnMouseExited(null);
        } else {
            tab.setOnMouseEntered(event -> tab.setTextFill(Color.web(Theme.WHITE)));
            tab.setOnMouseExited(event -> tab.setTextFill(Color.web(Theme.SOFT_TEXT)));
        }
    }

    /* -------------------------------------------------------- auth logic */

    private void handleLogin() {
        String identifier = loginUser.getText().trim();
        String password = loginPass.getText();
        if (identifier.isEmpty() || password.isEmpty()) {
            loginStatus.setTextFill(Color.web(Theme.RED));
            loginStatus.setText("Enter your username and password.");
            return;
        }
        User user = UserDao.login(identifier, password);
        if (user == null) {
            loginStatus.setTextFill(Color.web(Theme.RED));
            loginStatus.setText("That ticket doesn't match our records.");
            return;
        }
        Session.currentUser = user;
        MainMenuGuestController.show();
    }

    private void handleRegister() {
        String username = regUser.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPass.getText();
        String confirm = regConfirm.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            registerFailed("Fill in every field.");
            return;
        }
        if (!password.equals(confirm)) {
            registerFailed("Passwords do not match.");
            return;
        }
        if (password.length() < 4) {
            registerFailed("Password must be at least 4 characters.");
            return;
        }
        if (UserDao.usernameExists(username)) {
            registerFailed("That username is taken.");
            return;
        }
        if (UserDao.emailExists(email)) {
            registerFailed("That email is already registered.");
            return;
        }
        if (!UserDao.register(username, email, password)) {
            registerFailed("Registration failed. Is the database running?");
            return;
        }

        loginUser.setText(username);
        loginPass.clear();
        loginStatus.setTextFill(Color.web(Theme.LIME));
        loginStatus.setText("Ticket created \u2014 enter your password to go in.");
        applyMode(Mode.LOGIN, true);
        Platform.runLater(loginPass::requestFocus);
    }

    private void registerFailed(String message) {
        registerStatus.setTextFill(Color.web(Theme.RED));
        registerStatus.setText(message);
    }

    private void playAsGuest() {
        Session.currentUser = null;
        Navigator.go(new MenuView("Guest Player"));
    }

    /* ------------------------------------------------------- small parts */

    private static Label statusLabel() {
        Label label = Theme.label("", 11, FontWeight.BOLD, Theme.RED);
        label.setWrapText(true);
        label.setMinHeight(18);
        return label;
    }

    private static Label fieldLabel(String text) {
        return Theme.mono(text, 9, FontWeight.BOLD, "#a8a08f");
    }

    private static Label captionLabel(String text) {
        Label label = Theme.label(text, 10.5, FontWeight.NORMAL, Theme.MUTED);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private HBox iconField(TextField field, Feather glyph, String prompt) {
        field.setPromptText(prompt);
        field.setStyle(inputStyle());
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);

        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(14);
        icon.setIconColor(Color.web(Theme.MUTED));

        HBox wrap = new HBox(10, icon, field);
        wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(0, 14, 0, 13));
        wrap.setMinHeight(44);
        wrap.setPrefHeight(44);
        wrap.setMaxHeight(44);
        wrap.setMaxWidth(Double.MAX_VALUE);
        wrap.setStyle(inputWrapStyle(false));

        field.focusedProperty().addListener((observable, oldValue, focused) ->
            wrap.setStyle(inputWrapStyle(focused)));
        return wrap;
    }

    private Button primaryButton(String text, Feather glyph) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(14);
        icon.setIconColor(Color.web(INK_ON_BRASS));

        Button button = new Button(text);
        button.setGraphic(icon);
        button.setContentDisplay(ContentDisplay.RIGHT);
        button.setGraphicTextGap(10);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(48);
        button.setPrefHeight(48);
        button.setStyle(primaryStyle(false));
        button.setOnMouseEntered(event -> button.setStyle(primaryStyle(true)));
        button.setOnMouseExited(event -> {
            button.setTranslateY(0);
            button.setStyle(primaryStyle(false));
        });
        button.setOnMousePressed(event -> button.setTranslateY(1));
        button.setOnMouseReleased(event -> button.setTranslateY(0));
        return button;
    }

    private Button socialButton(String text, Feather glyph, Runnable action) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize(14);
        icon.setIconColor(Color.web(Theme.SOFT_TEXT));

        Button button = new Button(text);
        button.setGraphic(icon);
        button.setGraphicTextGap(9);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(46);
        button.setPrefHeight(46);
        button.setStyle(socialStyle(false));
        button.setOnMouseEntered(event -> button.setStyle(socialStyle(true)));
        button.setOnMouseExited(event -> button.setStyle(socialStyle(false)));
        button.setOnAction(event -> action.run());
        return button;
    }

    private Node rememberToggle() {
        StackPane box = new StackPane();
        box.setMinSize(15, 15);
        box.setPrefSize(15, 15);
        box.setMaxSize(15, 15);

        FontIcon tick = new FontIcon(Feather.CHECK);
        tick.setIconSize(9);
        tick.setIconColor(Color.web(INK_ON_BRASS));
        tick.setVisible(false);
        box.getChildren().add(tick);
        box.setStyle(boxStyle(false));

        Label text = Theme.label("Remember ticket", 11, FontWeight.NORMAL, Theme.SOFT_TEXT);

        HBox row = new HBox(8, box, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setOnMouseClicked(event -> {
            rememberTicket.set(!rememberTicket.get());
            box.setStyle(boxStyle(rememberTicket.get()));
            tick.setVisible(rememberTicket.get());
        });
        return row;
    }

    private Node forgotLink() {
        Label link = Theme.label("Forgot password?", 11, FontWeight.BOLD, Theme.LIME);
        link.setCursor(Cursor.HAND);
        link.setOnMouseEntered(event -> link.setTextFill(Color.web(Theme.LIME_HOVER)));
        link.setOnMouseExited(event -> link.setTextFill(Color.web(Theme.LIME)));
        link.setOnMouseClicked(event -> {
            loginStatus.setTextFill(Color.web(Theme.MUTED));
            loginStatus.setText("Password reset isn't available in this demo.");
        });
        return link;
    }

    private static Region grow() {
        Region region = new Region();
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private static void gap(Node node, double bottom) {
        VBox.setMargin(node, new Insets(0, 0, bottom, 0));
    }

    private static Region divider() {
        Region rule = new Region();
        rule.setMinHeight(1);
        rule.setPrefHeight(1);
        rule.setMaxHeight(1);
        rule.setStyle("-fx-background-color: rgba(255,255,255,0.10);");
        return rule;
    }

    /* ----------------------------------------------------------- styling */

    private static String inputStyle() {
        return "-fx-background-color: transparent; -fx-background-insets: 0;"
            + " -fx-padding: 0; -fx-text-fill: " + Theme.WHITE + ";"
            + " -fx-prompt-text-fill: #6e7783;"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 13px;"
            + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;"
            + " -fx-highlight-fill: rgba(198,161,91,0.35);"
            + " -fx-highlight-text-fill: " + Theme.WHITE + ";";
    }

    private static String inputWrapStyle(boolean focused) {
        return "-fx-background-color: " + INPUT_FILL + ";"
            + " -fx-background-radius: 4;"
            + " -fx-border-color: " + (focused ? "rgba(198,161,91,0.80)" : INPUT_EDGE) + ";"
            + " -fx-border-radius: 4; -fx-border-width: 1;";
    }

    private static String primaryStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? Theme.LIME_HOVER : Theme.LIME) + ";"
            + " -fx-background-radius: 5; -fx-background-insets: 0;"
            + " -fx-text-fill: " + INK_ON_BRASS + ";"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 12.5px;"
            + " -fx-font-weight: bold; -fx-padding: 0 18; -fx-cursor: hand;";
    }

    private static String socialStyle(boolean hover) {
        return "-fx-background-color: " + (hover ? "#313131" : "#2a2a2a") + ";"
            + " -fx-background-radius: 4; -fx-background-insets: 0;"
            + " -fx-border-color: " + (hover ? "#4c4c4c" : "#3a3a3a") + ";"
            + " -fx-border-radius: 4; -fx-border-width: 1;"
            + " -fx-text-fill: " + (hover ? Theme.WHITE : "#e6e6e6") + ";"
            + " -fx-font-family: '" + Theme.FONT + "'; -fx-font-size: 12px;"
            + " -fx-font-weight: bold; -fx-padding: 0 12; -fx-cursor: hand;";
    }

    private static String boxStyle(boolean on) {
        return "-fx-background-color: " + (on ? Theme.LIME : "transparent") + ";"
            + " -fx-background-radius: 3;"
            + " -fx-border-color: " + (on ? Theme.LIME : "#4a525c") + ";"
            + " -fx-border-radius: 3; -fx-border-width: 1;";
    }

    /* --------------------------------------------------------- animation */

    private void animateEntrance(Node node) {
        node.setOpacity(0);
        node.setTranslateY(12);

        FadeTransition fade = new FadeTransition(Duration.millis(520), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition rise = new TranslateTransition(Duration.millis(520), node);
        rise.setFromY(12);
        rise.setToY(0);
        rise.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, rise).play();
    }

    /* ------------------------------------------------------- ticket card */

    /**
     * Paints the retro ticket silhouette once per resize. The saw-tooth left
     * and right edges plus the two seam notches are part of the path, so the
     * photo shows through the notches without any clip nodes.
     */
    private static final class TicketCard extends Region {

        private final Canvas canvas = new Canvas();
        private final HBox content;
        private double drawnWidth = -1;
        private double drawnHeight = -1;

        TicketCard(HBox content) {
            this.content = content;
            canvas.setMouseTransparent(true);
            DropShadow shadow = new DropShadow(30, 0, 12, Color.web("#000000", 0.58));
            shadow.setSpread(0.02);
            canvas.setEffect(shadow);
            getChildren().addAll(canvas, content);
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            if (width != drawnWidth || height != drawnHeight) {
                canvas.setWidth(width);
                canvas.setHeight(height);
                paint(width, height);
                drawnWidth = width;
                drawnHeight = height;
            }
            content.resizeRelocate(EDGE_PAD, EDGE_PAD,
                width - EDGE_PAD * 2, height - EDGE_PAD * 2);
        }

        private void paint(double width, double height) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);

            double x0 = EDGE_PAD;
            double y0 = EDGE_PAD;
            double x1 = width - EDGE_PAD;
            double y1 = height - EDGE_PAD;
            double seam = x0 + SEAM;

            int teeth = Math.max(4, (int) Math.round((y1 - y0) / 18.0));
            double step = (y1 - y0) / teeth;

            // card body
            buildBody(gc, x0, y0, x1, y1, seam, teeth, step);
            gc.setFill(Color.web(CARD_FILL));
            gc.fill();

            // brick stub panel
            buildStub(gc, x0, y0, y1, seam, teeth, step);
            gc.setFill(Color.web(BRICK));
            gc.fill();

            // perforation dots on the stub
            gc.save();
            buildStub(gc, x0, y0, y1, seam, teeth, step);
            gc.clip();
            gc.setFill(Color.web("#000000", 0.13));
            for (double y = y0 + 11; y < y1 - 6; y += 18) {
                for (double x = x0 + 11; x < seam - 8; x += 18) {
                    gc.fillOval(x, y, 2.2, 2.2);
                }
            }
            gc.restore();

            // outline on top of both fills
            buildBody(gc, x0, y0, x1, y1, seam, teeth, step);
            gc.setStroke(Color.web(CARD_EDGE));
            gc.setLineWidth(1.1);
            gc.stroke();

            // perforation down the seam
            gc.setStroke(Color.web("#f0e6d7", 0.22));
            gc.setLineWidth(1.2);
            gc.setLineDashes(3, 7);
            gc.strokeLine(seam, y0 + NOTCH, seam, y1 - NOTCH);
            gc.setLineDashes();
        }

        private static void buildBody(GraphicsContext gc, double x0, double y0,
                                      double x1, double y1, double seam,
                                      int teeth, double step) {
            gc.beginPath();
            gc.moveTo(x0 + SPIKE, y0);
            gc.lineTo(seam - NOTCH, y0);
            gc.arc(seam, y0, NOTCH, NOTCH, 180, 180);
            gc.lineTo(x1 - SPIKE, y0);
            for (int i = 0; i < teeth; i++) {
                double top = y0 + i * step;
                gc.lineTo(x1, top + step * 0.5);
                gc.lineTo(x1 - SPIKE, top + step);
            }
            gc.lineTo(seam + NOTCH, y1);
            gc.arc(seam, y1, NOTCH, NOTCH, 0, 180);
            gc.lineTo(x0 + SPIKE, y1);
            for (int i = teeth - 1; i >= 0; i--) {
                double top = y0 + i * step;
                gc.lineTo(x0, top + step * 0.5);
                gc.lineTo(x0 + SPIKE, top);
            }
            gc.closePath();
        }

        private static void buildStub(GraphicsContext gc, double x0, double y0,
                                      double y1, double seam, int teeth, double step) {
            gc.beginPath();
            gc.moveTo(x0 + SPIKE, y0);
            gc.lineTo(seam - NOTCH, y0);
            gc.arc(seam, y0, NOTCH, NOTCH, 180, 90);
            gc.lineTo(seam, y1 - NOTCH);
            gc.arc(seam, y1, NOTCH, NOTCH, 90, 90);
            gc.lineTo(x0 + SPIKE, y1);
            for (int i = teeth - 1; i >= 0; i--) {
                double top = y0 + i * step;
                gc.lineTo(x0, top + step * 0.5);
                gc.lineTo(x0 + SPIKE, top);
            }
            gc.closePath();
        }
    }
}
