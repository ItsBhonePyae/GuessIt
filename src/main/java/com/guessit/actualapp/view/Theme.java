package com.guessit.actualapp.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;

/**
 * Muted cinema palette: cool charcoal surfaces, aged brass for actions and a
 * desaturated brick red for signage. Effects are kept low so the screens read
 * like a real product rather than a neon mock-up.
 */
public final class Theme {

    /* ------------------------------------------------------------ palette */

    public static final String INK = "#0b0e12";
    public static final String BLACK = "#0f1318";
    public static final String GRAPHITE = "#151a20";
    public static final String PANEL = "#1c2229";
    public static final String HOVER = "#252d36";
    public static final String BORDER = "#303944";

    public static final String WHITE = "#edf0f3";
    public static final String SOFT_TEXT = "#c5ccd4";
    public static final String MUTED = "#929ba6";

    /** Aged brass for calls to action. */
    public static final String LIME = "#c6a15b";
    public static final String LIME_HOVER = "#d8b878";
    public static final String GOLD_DEEP = "#96733a";

    /** Desaturated brick red for signage. */
    public static final String RED = "#9d3f3f";
    public static final String RED_DEEP = "#7a2c2d";
    public static final String RED_DARK = "#3a1718";

    /* legacy names kept so the existing views keep compiling */
    public static final String VIOLET = "#8a3a3c";
    public static final String DEEP_VIOLET = "#5e2426";
    public static final String PAPER = "#e6ddcd";

    public static final String FONT = "Segoe UI";
    public static final String MONO = "Consolas";

    private Theme() {
    }

    /* ------------------------------------------------------------ typography */

    public static Label label(String text, double size, FontWeight weight, String color) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT, weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    public static Label mono(String text, double size, FontWeight weight, String color) {
        Label label = new Label(text);
        label.setFont(Font.font(MONO, weight, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    public static Label italic(String text, double size, String color) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT, FontWeight.BLACK, FontPosture.ITALIC, size));
        label.setTextFill(Color.web(color));
        return label;
    }

    /* ------------------------------------------------------------ buttons */

    public static String limeButtonStyle() {
        return "-fx-background-color: " + LIME + ";"
            + " -fx-text-fill: #1c160c;"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 6; -fx-background-insets: 0;"
            + " -fx-padding: 12 22; -fx-cursor: hand;";
    }

    public static String limeHoverStyle() {
        return "-fx-background-color: " + LIME_HOVER + ";"
            + " -fx-text-fill: #1c160c;"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 6; -fx-background-insets: 0;"
            + " -fx-padding: 12 22; -fx-cursor: hand;";
    }

    public static String violetButtonStyle() {
        return "-fx-background-color: " + RED_DEEP + ";"
            + " -fx-text-fill: #f1e8dc;"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 6; -fx-background-insets: 0;"
            + " -fx-padding: 12 22; -fx-cursor: hand;";
    }

    public static String violetHoverStyle() {
        return "-fx-background-color: " + RED + ";"
            + " -fx-text-fill: #f6eee2;"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 6; -fx-background-insets: 0;"
            + " -fx-padding: 12 22; -fx-cursor: hand;";
    }

    public static String ghostButtonStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + SOFT_TEXT + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 8; -fx-border-color: rgba(198,161,91,0.28);"
            + " -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 11 20; -fx-cursor: hand;";
    }

    public static String ghostHoverStyle() {
        return "-fx-background-color: rgba(198,161,91,0.08); -fx-text-fill: " + WHITE + ";"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + " -fx-background-radius: 8; -fx-border-color: rgba(198,161,91,0.60);"
            + " -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 11 20; -fx-cursor: hand;";
    }

    public static String idleNavStyle() {
        return "-fx-background-color: transparent; -fx-background-radius: 7;"
            + " -fx-text-fill: " + SOFT_TEXT + "; -fx-cursor: hand;";
    }

    public static String hoverNavStyle() {
        return "-fx-background-color: rgba(255,255,255,0.045); -fx-background-radius: 7;"
            + " -fx-text-fill: " + WHITE + "; -fx-cursor: hand;";
    }

    public static String activeNavStyle() {
        return "-fx-background-color: rgba(157,63,63,0.12);"
            + " -fx-background-radius: 7;"
            + " -fx-border-color: transparent transparent transparent " + RED + ";"
            + " -fx-border-width: 0 0 0 2; -fx-border-radius: 7;"
            + " -fx-text-fill: " + LIME_HOVER + "; -fx-cursor: hand;";
    }

    /* ------------------------------------------------------------ fields */

    public static String fieldStyle(boolean focused) {
        String border = focused ? "#8f7440" : "#3a342d";
        return "-fx-background-color: #14110f;"
            + " -fx-text-fill: " + WHITE + ";"
            + " -fx-prompt-text-fill: #6f685d;"
            + " -fx-font-family: '" + FONT + "'; -fx-font-size: 13px;"
            + " -fx-background-radius: 7;"
            + " -fx-border-color: " + border + "; -fx-border-radius: 7; -fx-border-width: 1;"
            + " -fx-padding: 12 15;";
    }

    /* ------------------------------------------------------------ surfaces */

    public static String panelStyle() {
        return "-fx-background-color: " + PANEL + ";"
            + " -fx-background-radius: 8;"
            + " -fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-border-width: 1;";
    }

    public static String chipStyle(String accent) {
        return "-fx-background-color: transparent;"
            + " -fx-background-radius: 999;"
            + " -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 999; -fx-border-width: 1;"
            + " -fx-text-fill: " + accent + "; -fx-font-family: '" + FONT + "';"
            + " -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 6 12;";
    }

    /* ------------------------------------------------------------ decor */

    /** Thin brass hairline that fades at both ends. */
    public static Region goldRule() {
        Region rule = new Region();
        rule.setMinHeight(1);
        rule.setPrefHeight(1);
        rule.setMaxHeight(1);
        rule.setMouseTransparent(true);
        rule.setStyle("-fx-background-color: rgba(198,161,91,0.38);");
        return rule;
    }

    /** Left or right velvet drape. Stretches vertically. */
    public static Region curtain(boolean left) {
        Region region = new Region();
        region.setMouseTransparent(true);
        region.setMinWidth(40);
        region.setPrefWidth(40);
        region.setMaxWidth(40);
        String direction = left ? "right" : "left";
        region.setStyle("-fx-background-color: linear-gradient(to " + direction + ","
            + " #24090b 0%, #4a1416 22%, #2e0c0e 44%, #54181a 64%,"
            + " #260a0c 82%, rgba(13,12,11,0) 100%);");
        return region;
    }

    /** Soft warm light falling from the top of the stage. */
    public static Region spotlight(double centerXPercent, double strength) {
        Region region = new Region();
        region.setMouseTransparent(true);
        region.setStyle("-fx-background-color: radial-gradient(center "
            + centerXPercent + "% 0%, radius 78%,"
            + " rgba(198,161,91," + strength + ") 0%,"
            + " rgba(198,161,91," + (strength * 0.40) + ") 34%,"
            + " rgba(198,161,91,0) 74%);");
        return region;
    }

    /** Warm red glow rising from the bottom of the stage. */
    public static Region redGlow() {
        Region region = new Region();
        region.setMouseTransparent(true);
        region.setStyle("-fx-background-color: radial-gradient(center 50% 110%, radius 85%,"
            + " rgba(122,44,45,0.22) 0%, rgba(122,44,45,0.07) 42%, rgba(122,44,45,0) 76%);");
        return region;
    }

    /** Full-bleed photograph, scaled to cover the view without distortion. */
    public static Region photoBackdrop(String resource) {
        Region region = new Region();
        region.setMouseTransparent(true);
        URL url = Theme.class.getResource(resource);
        if (url == null) {
            region.setStyle("-fx-background-color: " + INK + ";");
            return region;
        }
        region.setStyle("-fx-background-image: url('" + url.toExternalForm() + "');"
            + " -fx-background-size: cover;"
            + " -fx-background-position: center center;"
            + " -fx-background-repeat: no-repeat;");
        return region;
    }

    /** Darkened edges, like a projected print. */
    public static Region vignette() {
        Region region = new Region();
        region.setMouseTransparent(true);
        region.setStyle("-fx-background-color: radial-gradient(center 50% 42%, radius 90%,"
            + " rgba(0,0,0,0) 52%, rgba(0,0,0,0.20) 80%, rgba(0,0,0,0.52) 100%);");
        return region;
    }

    /** Brass logo tile. */
    public static StackPane logoBadge(double size, Feather glyph) {
        FontIcon icon = new FontIcon(glyph);
        icon.setIconSize((int) (size * 0.46));
        icon.setIconColor(Color.web("#1a150d"));
        StackPane badge = new StackPane(icon);
        badge.setMinSize(size, size);
        badge.setPrefSize(size, size);
        badge.setMaxSize(size, size);
        badge.setStyle("-fx-background-color: " + LIME + ";"
            + " -fx-background-radius: " + (size * 0.26) + ";");
        return badge;
    }

    /** Small muted signage tag. */
    public static Label ribbon(String text, String background, String foreground) {
        Label label = label(text, 9, FontWeight.BOLD, foreground);
        label.setPadding(new Insets(5, 10, 5, 10));
        label.setStyle("-fx-background-color: " + background + ";"
            + " -fx-background-radius: 2;");
        return label;
    }

    /** Solid brass star rating row. */
    public static HBox stars(int filled, int total, double size) {
        HBox row = new HBox(3);
        row.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < total; i++) {
            row.getChildren().add(starShape(size, i < filled));
        }
        return row;
    }

    private static Polygon starShape(double size, boolean filled) {
        double outer = size / 2.0;
        double inner = outer * 0.44;
        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? outer : inner;
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            star.getPoints().add(outer + Math.cos(angle) * radius);
            star.getPoints().add(outer + Math.sin(angle) * radius);
        }
        star.setFill(Color.web(filled ? LIME : "#332e26"));
        star.setStroke(Color.web(filled ? "#d3b273" : "#403a31"));
        star.setStrokeWidth(0.6);
        return star;
    }

    /** Small bordered spec block. */
    public static Node ticket(String key, String value, String accent) {
        Label keyLabel = label(key, 8, FontWeight.BOLD, MUTED);
        Label valueLabel = label(value, 12, FontWeight.BOLD, WHITE);
        VBox text = new VBox(1, keyLabel, valueLabel);
        StackPane stub = new StackPane(text);
        stub.setPadding(new Insets(8, 15, 8, 15));
        stub.setStyle("-fx-background-color: rgba(255,255,255,0.02);"
            + " -fx-background-radius: 4;"
            + " -fx-border-color: rgba(255,255,255,0.09); -fx-border-radius: 4; -fx-border-width: 1;");
        return stub;
    }

    /** Very soft static glow for display type. */
    public static void glow(Label label) {
        DropShadow shadow = new DropShadow(14, Color.web("#000000", 0.55));
        shadow.setOffsetY(3);
        label.setEffect(shadow);
    }
}
