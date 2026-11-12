package Testing_for_bascially_everything;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Calendar;
import atlantafx.base.controls.Card;
import atlantafx.base.controls.CaptionMenuItem;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.MaskTextField;
import atlantafx.base.controls.Message;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Notification;
import atlantafx.base.controls.PasswordTextField;
import atlantafx.base.controls.Popover;
import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.Tile;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.layout.DeckPane;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.layout.ModalBox;
import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import atlantafx.base.theme.Tweaks;
import atlantafx.base.util.Animations;
import atlantafx.base.util.BBCodeHandler;
import atlantafx.base.util.BBCodeParser;
import atlantafx.base.util.DoubleStringConverter;
import atlantafx.base.util.IntegerStringConverter;
import atlantafx.base.util.MaskChar;
import atlantafx.base.util.MaskTextFormatter;
import atlantafx.base.util.PasswordTextFormatter;
import atlantafx.base.util.PlatformUtils;
import atlantafx.base.util.SimpleMaskChar;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.util.List;
import java.util.Map;

/**
 * AtlantaFX 2.0.1 full feature showcase.
 *
 * <p>Covers every public class and public method of {@code atlantafx-base}:
 * controls (Breadcrumbs, Calendar, Card, CaptionMenuItem, CustomTextField,
 * MaskTextField, Message, ModalPane, Notification, PasswordTextField, Popover,
 * RingProgressIndicator, Spacer, Tile, ToggleSwitch), layouts (DeckPane,
 * InputGroup, ModalBox), theme (Theme, Styles, Tweaks and all bundled themes)
 * and util (Animations, BBCodeParser/BBCodeHandler, MaskChar/SimpleMaskChar,
 * MaskTextFormatter, PasswordTextFormatter, Integer/DoubleStringConverter,
 * PlatformUtils).</p>
 *
 * <p>Skin and Behavior classes are internal and are exercised implicitly by
 * the controls above.</p>
 *
 * <p>Icons use Ikonli: {@code ikonli-javafx} provides {@code FontIcon} and the
 * {@code ikonli-feather-pack} dependency provides the actual glyphs
 * ({@code Feather.SEARCH}, {@code Feather.STAR}, ...). Without an icon pack the
 * font has no glyphs and every icon renders as a missing-glyph box.</p>
 */
public class AtlantaFX_full_test extends Application {

    private final StackPane root = new StackPane();
    private final TabPane tabs = new TabPane();
    private final ModalPane modal = new ModalPane();
    private final Popover popover = new Popover();

    /** Every Animations factory method, including overloads. */
    private static final List<String> ANIMATIONS = List.of(
        "flash", "pulse", "pulse(double)", "shakeX", "shakeX(double)",
        "shakeY", "shakeY(double)", "wobble",
        "fadeIn", "fadeOut", "fadeInDown", "fadeOutDown", "fadeInLeft",
        "fadeOutLeft", "fadeInRight", "fadeOutRight", "fadeInUp", "fadeOutUp",
        "rollIn", "rollOut", "rotateIn", "rotateOut",
        "rotateInDownLeft", "rotateOutDownLeft", "rotateInDownRight",
        "rotateOutDownRight", "rotateInUpLeft", "rotateOutUpLeft",
        "rotateInUpRight", "rotateOutUpRight",
        "slideInDown", "slideOutDown", "slideInLeft", "slideOutLeft",
        "slideInRight", "slideOutRight", "slideInUp", "slideOutUp",
        "zoomIn", "zoomIn(double)", "zoomOut", "zoomOut(double)"
    );

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        tabs.getTabs().addAll(
            tab("Themes & Text", Feather.TYPE, themesAndTextTab()),
            tab("Buttons", Feather.SQUARE, buttonsTab()),
            tab("Inputs", Feather.EDIT_3, inputsTab()),
            tab("Card / Tile", Feather.LAYERS, cardAndTileTab()),
            tab("Navigation", Feather.COMPASS, navigationTab()),
            tab("Calendar", Feather.CALENDAR, calendarTab()),
            tab("Overlays", Feather.LAYOUT, overlaysTab()),
            tab("Layouts", Feather.GRID, layoutsTab()),
            tab("Animations", Feather.ZAP, animationsTab()),
            tab("Utilities", Feather.TOOL, utilitiesTab())
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        root.getChildren().addAll(tabs, modal);

        stage.setTitle("AtlantaFX 2.0.1 - full feature showcase");
        stage.setScene(new Scene(root, 1280, 860));
        stage.show();
    }

    // ------------------------------------------------------------------ themes

    private Node themesAndTextTab() {
        ComboBox<Theme> themes = new ComboBox<>();
        themes.getItems().setAll(
            new PrimerLight(), new PrimerDark(),
            new NordLight(), new NordDark(),
            new CupertinoLight(), new CupertinoDark(),
            new Dracula()
        );
        themes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Theme theme) {
                return theme == null ? "" : theme.getName();
            }

            @Override
            public Theme fromString(String string) {
                return null;
            }
        });
        themes.getSelectionModel().select(1);
        themes.setOnAction(e -> {
            Theme theme = themes.getSelectionModel().getSelectedItem();
            if (theme != null) {
                Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
            }
        });

        Theme custom = Theme.of("Custom theme", new PrimerLight().getUserAgentStylesheet(), false);
        Label themeInfo = new Label("Theme.of(): " + custom.getName()
            + " | dark=" + custom.isDarkMode()
            + " | default=" + custom.isDefault());

        VBox typography = new VBox(6,
            label("title-1", Styles.TITLE_1),
            label("title-2", Styles.TITLE_2),
            label("title-3", Styles.TITLE_3),
            label("title-4", Styles.TITLE_4),
            label("text-caption", Styles.TEXT_CAPTION),
            label("text-small", Styles.TEXT_SMALL),
            label("text-bold", Styles.TEXT_BOLD),
            label("text-bolder", Styles.TEXT_BOLDER),
            label("text-normal", Styles.TEXT_NORMAL),
            label("text-lighter", Styles.TEXT_LIGHTER),
            label("text-italic", Styles.TEXT_ITALIC),
            label("text-oblique", Styles.TEXT_OBLIQUE),
            label("text-strikethrough", Styles.TEXT_STRIKETHROUGH),
            label("text-underlined", Styles.TEXT_UNDERLINED),
            label("text-muted", Styles.TEXT_MUTED),
            label("text-subtle", Styles.TEXT_SUBTLE)
        );

        FlowPane backgrounds = new FlowPane(10, 10);
        for (String bg : new String[]{
            Styles.BG_DEFAULT, Styles.BG_INSET, Styles.BG_SUBTLE,
            Styles.BG_NEUTRAL_EMPHASIS_PLUS, Styles.BG_NEUTRAL_EMPHASIS,
            Styles.BG_NEUTRAL_MUTED, Styles.BG_NEUTRAL_SUBTLE,
            Styles.BG_ACCENT_EMPHASIS, Styles.BG_ACCENT_MUTED, Styles.BG_ACCENT_SUBTLE,
            Styles.BG_WARNING_EMPHASIS, Styles.BG_WARNING_MUTED, Styles.BG_WARNING_SUBTLE,
            Styles.BG_SUCCESS_EMPHASIS, Styles.BG_SUCCESS_MUTED, Styles.BG_SUCCESS_SUBTLE,
            Styles.BG_DANGER_EMPHASIS, Styles.BG_DANGER_MUTED, Styles.BG_DANGER_SUBTLE}) {
            Label chip = new Label(bg);
            chip.getStyleClass().addAll(bg, Styles.TEXT_ON_EMPHASIS);
            chip.setPadding(new Insets(8, 12, 8, 12));
            backgrounds.getChildren().add(chip);
        }

        return vbox(
            section("Theme switcher (all bundled themes)", themes, themeInfo),
            section("Typography style classes", typography),
            section("Background helpers", backgrounds)
        );
    }

    // ----------------------------------------------------------------- buttons

    private Node buttonsTab() {
        HBox variants = new HBox(10,
            button("Default", null),
            button("Accent", Styles.ACCENT),
            button("Success", Styles.SUCCESS),
            button("Warning", Styles.WARNING),
            button("Danger", Styles.DANGER),
            button("Outlined", Styles.BUTTON_OUTLINED),
            button("Flat", Styles.FLAT),
            button("Bordered", Styles.BORDERED)
        );
        variants.setAlignment(Pos.CENTER_LEFT);

        HBox sizes = new HBox(10,
            button("Small", Styles.SMALL),
            button("Medium", Styles.MEDIUM),
            button("Large", Styles.LARGE)
        );

        HBox pills = new HBox(0,
            button("Left pill", Styles.LEFT_PILL),
            button("Center pill", Styles.CENTER_PILL),
            button("Right pill", Styles.RIGHT_PILL)
        );

        Button circle = new Button("", new FontIcon(Feather.PLUS));
        circle.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);
        Button icon = new Button("", new FontIcon(Feather.STAR));
        icon.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        HBox circles = new HBox(10, circle, icon);

        // Styles utility helpers.
        Label target = new Label("Styles target");
        target.setPadding(new Insets(6, 10, 6, 10));
        Styles.addStyleClass(target, Styles.BG_ACCENT_SUBTLE, Styles.TEXT_BOLD, Styles.ROUNDED);
        Styles.appendStyle(target, "-fx-padding", "8 14 8 14");

        Button toggleClass = new Button("toggleStyleClass(BORDERED)");
        toggleClass.setOnAction(e -> Styles.toggleStyleClass(target, Styles.BORDERED));
        Button removeStyle = new Button("removeStyle(TEXT_BOLD)");
        removeStyle.setOnAction(e -> Styles.removeStyle(target, Styles.TEXT_BOLD));
        Button pseudoSuccess = new Button("activatePseudoClass(SUCCESS)");
        pseudoSuccess.setOnAction(e -> Styles.activatePseudoClass(target, Styles.STATE_SUCCESS));
        Button pseudoDanger = new Button("activatePseudoClass(DANGER)");
        pseudoDanger.setOnAction(e -> Styles.activatePseudoClass(target, Styles.STATE_DANGER));
        Button pseudoInteractive = new Button("activatePseudoClass(INTERACTIVE)");
        pseudoInteractive.setOnAction(e -> Styles.activatePseudoClass(target, Styles.STATE_INTERACTIVE));
        Button pseudoAccent = new Button("activatePseudoClass(ACCENT)");
        pseudoAccent.setOnAction(e -> Styles.activatePseudoClass(target, Styles.STATE_ACCENT));
        Button pseudoWarning = new Button("activatePseudoClass(WARNING)");
        pseudoWarning.setOnAction(e -> Styles.activatePseudoClass(target, Styles.STATE_WARNING));

        ToggleButton toggle = new ToggleButton("ToggleButton");
        ToggleGroup group = new ToggleGroup();
        ToggleButton t1 = new ToggleButton("One");
        ToggleButton t2 = new ToggleButton("Two");
        t1.setToggleGroup(group);
        t2.setToggleGroup(group);

        MenuButton menu = new MenuButton("Menu with CaptionMenuItem");
        menu.getItems().addAll(
            new CaptionMenuItem("Section"),
            new MenuItem("Item 1"),
            new MenuItem("Item 2"),
            new CaptionMenuItem("Danger zone"),
            new MenuItem("Delete")
        );
        menu.getStyleClass().add(Tweaks.NO_ARROW);

        Button withTooltip = new Button("Hover for Tooltip");
        withTooltip.setTooltip(new Tooltip("AtlantaFX tooltip"));

        // Ikonli icons. ikonli-javafx supplies FontIcon; the glyphs themselves
        // come from an icon pack (here: ikonli-feather-pack). AtlantaFX pins
        // .ikonli-font-icon to 18px, so inline styles are used for the size demo.
        HBox iconSizes = new HBox(14);
        for (int size : new int[]{14, 20, 28, 40}) {
            FontIcon sized = iconStyle(new FontIcon(Feather.HEART),
                "-fx-icon-size: " + size + "px;");
            iconSizes.getChildren().addAll(sized, new Label(size + "px"));
        }
        iconSizes.setAlignment(Pos.CENTER_LEFT);

        HBox iconColors = new HBox(14);
        for (String color : new String[]{"#4493f8", "#22c55e", "#f59e0b", "#ef4444"}) {
            FontIcon colored = iconStyle(new FontIcon(Feather.STAR),
                "-fx-icon-size: 26px; -fx-icon-color: " + color + ";");
            iconColors.getChildren().add(colored);
        }
        iconColors.setAlignment(Pos.CENTER_LEFT);

        HBox iconStyleClasses = new HBox(14);
        for (String styleClass : new String[]{
            Styles.ACCENT, Styles.SUCCESS, Styles.WARNING, Styles.DANGER}) {
            FontIcon styled = new FontIcon(Feather.INFO);
            styled.getStyleClass().add(styleClass);
            iconStyleClasses.getChildren().add(styled);
        }
        iconStyleClasses.setAlignment(Pos.CENTER_LEFT);

        Label fontIconLabel = new Label("Styles.FONT_ICON", new FontIcon(Feather.SETTINGS));
        fontIconLabel.getStyleClass().add(Styles.FONT_ICON);

        Button buttonIcon = new Button("button-icon", new FontIcon(Feather.DOWNLOAD));
        buttonIcon.getStyleClass().add(Styles.BUTTON_ICON);
        Button circleIcon = new Button("", new FontIcon(Feather.UPLOAD));
        circleIcon.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);

        MenuButton iconMenu = new MenuButton("Icons in menu items", null,
            new MenuItem("Home", new FontIcon(Feather.HOME)),
            new MenuItem("User", new FontIcon(Feather.USER)),
            new MenuItem("Settings", new FontIcon(Feather.SETTINGS)),
            new MenuItem("Delete", new FontIcon(Feather.TRASH_2)));

        return vbox(
            section("Button variants", variants),
            section("Sizes", sizes),
            section("Pill buttons", pills),
            section("Circle / icon buttons", circles),
            section("Ikonli FontIcon (size / color / style classes)", iconSizes, iconColors,
                iconStyleClasses, hbox(fontIconLabel, buttonIcon, circleIcon), iconMenu),
            section("Styles utility", target, hbox(toggleClass, removeStyle),
                hbox(pseudoAccent, pseudoSuccess, pseudoWarning, pseudoDanger, pseudoInteractive)),
            section("Toggle buttons", hbox(toggle, t1, t2)),
            section("CaptionMenuItem", menu),
            section("Tooltip", withTooltip)
        );
    }

    // ------------------------------------------------------------------ inputs

    private Node inputsTab() {
        TextField textField = new TextField("Plain text field");

        PasswordTextField password = new PasswordTextField("hunter2");
        password.setRevealPassword(true);
        Button reveal = new Button("Toggle reveal");
        reveal.setOnAction(e -> password.setRevealPassword(!password.getRevealPassword()));
        Label passwordValue = new Label("password=" + password.getPassword());
        password.passwordProperty().addListener((o, a, b) -> passwordValue.setText("password=" + b));

        MaskTextField masked = new MaskTextField("(999) 999-9999");
        masked.setPromptText("(999) 999-9999");

        CustomTextField custom = new CustomTextField("CustomTextField");
        custom.setLeft(new FontIcon(Feather.SEARCH));
        custom.setRight(new Button("", new FontIcon(Feather.X)));

        InputGroup inputGroup = new InputGroup(
            new Label("https://"),
            new TextField("example.com"),
            new Button("Open")
        );

        Spinner<Integer> intSpinner = new Spinner<>(0, 100, 42);
        intSpinner.setEditable(true);
        intSpinner.getValueFactory().setConverter(IntegerStringConverter.createFor(intSpinner));

        Spinner<Double> doubleSpinner = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        doubleSpinner.setEditable(true);
        doubleSpinner.getValueFactory().setConverter(DoubleStringConverter.createFor(doubleSpinner));

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("One", "Two", "Three");
        combo.getSelectionModel().selectFirst();

        CheckBox check = new CheckBox("CheckBox");
        RadioButton radio1 = new RadioButton("Radio A");
        RadioButton radio2 = new RadioButton("Radio B");
        ToggleGroup radioGroup = new ToggleGroup();
        radio1.setToggleGroup(radioGroup);
        radio2.setToggleGroup(radioGroup);
        radio1.setSelected(true);

        ToggleSwitch toggleSwitch = new ToggleSwitch("ToggleSwitch");
        toggleSwitch.setSelected(true);
        ToggleSwitch leftLabel = new ToggleSwitch("Label left");
        leftLabel.setLabelPosition(HorizontalDirection.LEFT);

        Slider slider = new Slider(0, 100, 35);
        slider.setShowTickLabels(true);
        ProgressBar progress = new ProgressBar(0.42);
        progress.setPrefWidth(240);

        RingProgressIndicator ring = new RingProgressIndicator(0.65);
        ring.setGraphic(new Label("65%"));
        ring.setStringConverter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : Math.round(value * 100) + "%";
            }

            @Override
            public Double fromString(String string) {
                return 0.0;
            }
        });
        RingProgressIndicator ringIndeterminate = new RingProgressIndicator(-1);

        ProgressBar striped = new ProgressBar(0.7);
        striped.getStyleClass().add(Styles.STRIPED);
        striped.setPrefWidth(240);

        return vbox(
            section("Text fields", vbox(textField, hbox(password, reveal, passwordValue),
                masked, custom, inputGroup)),
            section("Spinners with converters", hbox(intSpinner, doubleSpinner)),
            section("Selection controls", vbox(combo, hbox(check, radio1, radio2))),
            section("ToggleSwitch", hbox(toggleSwitch, leftLabel)),
            section("Sliders & progress", vbox(hbox(new Label("Slider"), slider), progress,
                hbox(ring, ringIndeterminate), striped))
        );
    }

    // --------------------------------------------------------------- card/tile

    private Node cardAndTileTab() {
        Card card = new Card();
        card.setHeader(new Label("Card header"));
        card.setSubHeader(new Label("Card sub header"));
        card.setBody(new Label("Card body. Cards group related content and have "
            + "optional header, sub header and footer sections."));
        card.setFooter(new Button("Card footer action"));
        card.setMaxWidth(600);

        Tile tile = new Tile("Tile", "TileBase with a description, graphic and action.");
        tile.setGraphic(new FontIcon(Feather.PLAY));
        tile.setAction(new Button("Open"));
        tile.setActionHandler(() -> System.out.println("Tile action handler"));

        Message message = new Message("Message", "A Message is a Tile with a close button and an action.",
            new FontIcon(Feather.ALERT_CIRCLE));
        message.setOnClose(e -> System.out.println("Message closed"));
        message.setActionHandler(() -> System.out.println("Message action handler"));
        message.setMaxWidth(600);

        Spacer spacer = new Spacer(Orientation.HORIZONTAL);
        HBox spacerRow = new HBox(new Label("Left"), spacer, new Label("Right"));
        spacerRow.setMaxWidth(400);
        HBox spacerFixed = new HBox(new Label("Left"), new Spacer(60), new Label("Right after 60px"));

        return vbox(
            section("Card", card),
            section("Tile", tile),
            section("Message", message),
            section("Spacer", spacerRow, spacerFixed)
        );
    }

    // -------------------------------------------------------------- navigation

    private Node navigationTab() {
        Breadcrumbs.BreadCrumbItem<String> model =
            Breadcrumbs.buildTreeModel("Home", "Components", "Navigation", "Breadcrumbs");
        Breadcrumbs.BreadCrumbItem<String> last = model;
        while (!last.getChildren().isEmpty()) {
            last = (Breadcrumbs.BreadCrumbItem<String>) last.getChildren().get(0);
        }

        Breadcrumbs<String> crumbs = new Breadcrumbs<>(model);
        crumbs.setSelectedCrumb(last);
        crumbs.setAutoNavigationEnabled(false);

        Label action = new Label("Clicked crumb: -");
        crumbs.setOnCrumbAction(e -> action.setText(
            "Clicked crumb: " + e.getSelectedCrumb().getValue()));

        crumbs.setCrumbFactory(item -> {
            Button crumb = new Button(item.getValue());
            crumb.getStyleClass().add(Styles.FLAT);
            crumb.setOnAction(e -> crumbs.setSelectedCrumb(item));
            return crumb;
        });
        crumbs.setDividerFactory(item -> new FontIcon(Feather.CHEVRON_RIGHT));

        Breadcrumbs<String> autoCrumbs = new Breadcrumbs<>(model);
        autoCrumbs.setAutoNavigationEnabled(true);
        autoCrumbs.setSelectedCrumb(model);

        final Breadcrumbs.BreadCrumbItem<String> lastCrumb = last;
        Button selectLast = new Button("setSelectedCrumb(last)");
        selectLast.setOnAction(e -> crumbs.setSelectedCrumb(lastCrumb));
        Button autoToggle = new Button("toggle autoNavigationEnabled");
        autoToggle.setOnAction(e -> crumbs.setAutoNavigationEnabled(!crumbs.isAutoNavigationEnabled()));

        ListView<String> list = new ListView<>();
        list.getItems().addAll("List item 1", "List item 2", "List item 3");
        list.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        list.setPrefHeight(120);

        TableView<String> table = new TableView<>();
        TableColumn<String, String> column = new TableColumn<>("Column");
        column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
        table.getColumns().add(column);
        table.getItems().addAll("Row 1", "Row 2", "Row 3");
        table.getStyleClass().addAll(Tweaks.NO_HEADER, Styles.DENSE, Styles.STRIPED);
        table.setPrefHeight(120);

        return vbox(
            section("Breadcrumbs", crumbs, action,
                hbox(selectLast, autoToggle),
                new Label("autoNavigationEnabled=" + autoCrumbs.isAutoNavigationEnabled())),
            section("Tweaks", new Label("NO_ARROW=" + Tweaks.NO_ARROW
                + ", EDGE_TO_EDGE=" + Tweaks.EDGE_TO_EDGE
                + ", NO_HEADER=" + Tweaks.NO_HEADER
                + ", ALIGN_LEFT=" + Tweaks.ALIGN_LEFT
                + ", ALIGN_CENTER=" + Tweaks.ALIGN_CENTER
                + ", ALIGN_RIGHT=" + Tweaks.ALIGN_RIGHT
                + ", ALT_ICON=" + Tweaks.ALT_ICON), list, table)
        );
    }

    // ---------------------------------------------------------------- calendar

    private Node calendarTab() {
        Calendar calendar = new Calendar(LocalDate.now());
        calendar.setShowWeekNumbers(true);
        calendar.setChronology(IsoChronology.INSTANCE);
        calendar.setTopNode(new Label("topNode"));
        calendar.setBottomNode(new Label("bottomNode"));

        Label value = new Label("value=" + calendar.getValue());
        calendar.valueProperty().addListener((o, a, b) -> value.setText("value=" + b));

        calendar.setDayCellFactory(cal -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date != null && date.getDayOfWeek().getValue() == 7) {
                    setTextFill(Color.ORANGE);
                }
            }
        });

        return vbox(
            section("Calendar (all properties)", calendar, value)
        );
    }

    // ---------------------------------------------------------------- overlays

    private Node overlaysTab() {
        modal.setAlignment(Pos.CENTER);
        modal.setPersistent(false);
        modal.usePredefinedTransitionFactories(Side.TOP);
        modal.setInTransitionFactory(node -> Animations.fadeIn(node, Duration.millis(220)));
        modal.setOutTransitionFactory(node -> Animations.fadeOut(node, Duration.millis(180)));

        Button openModal = new Button("ModalPane.show(ModalBox)");
        openModal.setOnAction(e -> {
            Button close = new Button("Close");
            ModalBox box = new ModalBox("ModalBox title", new Label("Modal content"), close);
            close.setOnAction(ev -> modal.hide());
            box.setOnClose(ev -> modal.hide());
            modal.show(box);
        });

        Button showDisplay = new Button("setDisplay(true)");
        showDisplay.setOnAction(e -> {
            modal.setContent(new Label("Content set with setContent(), shown with setDisplay(true)"));
            modal.setDisplay(true);
        });
        Button hideModal = new Button("hide()");
        hideModal.setOnAction(e -> modal.hide());

        Label modalInfo = new Label("topViewOrder=" + modal.getTopViewOrder()
            + " | persistent=" + modal.getPersistent()
            + " | display=" + modal.isDisplay());

        popover.setTitle("Popover");
        popover.setContentNode(new Label("Popover content set with setContentNode()."));
        popover.setArrowLocation(Popover.ArrowLocation.BOTTOM_CENTER);
        popover.setHeaderAlwaysVisible(true);
        popover.setCloseButtonEnabled(true);
        popover.setDetachable(false);
        popover.setAnimated(true);
        popover.setFadeInDuration(Duration.millis(180));
        popover.setFadeOutDuration(Duration.millis(140));
        popover.setCornerRadius(10);
        popover.setArrowSize(10);
        popover.setArrowIndent(12);
        Button popoverAnchor = new Button("Show Popover");
        popoverAnchor.setOnAction(e -> {
            if (popover.isShowing()) {
                popover.hide();
            } else {
                popover.show(popoverAnchor);
            }
        });

        VBox notifications = new VBox(10);
        Button addNotification = new Button("Add Notification");
        addNotification.setOnAction(e -> {
            Notification notification = new Notification("New notification",
                new FontIcon(Feather.INFO));
            notification.setPrimaryActions(new Button("Undo"), new Button("View"));
            notification.setSecondaryActions(new MenuItem("Dismiss"), new MenuItem("Mute"));
            notification.setOnClose(ev -> notifications.getChildren().remove(notification));
            notifications.getChildren().add(notification);
        });

        return vbox(
            section("ModalPane / ModalBox", hbox(openModal, showDisplay, hideModal), modalInfo,
                new Label("In transition: fadeIn 220ms | Out transition: fadeOut 180ms")),
            section("Popover", popoverAnchor,
                new Label("ArrowLocation=" + popover.getArrowLocation()
                    + " | closeButton=" + popover.isCloseButtonEnabled()
                    + " | detachable=" + popover.isDetachable()
                    + " | animated=" + popover.isAnimated())),
            section("Notification", addNotification, notifications)
        );
    }

    // ----------------------------------------------------------------- layouts

    private Node layoutsTab() {
        Node[] pages = {
            deckPage("Page A", "#7c3aed"),
            deckPage("Page B", "#0ea5e9"),
            deckPage("Page C", "#f59e0b")
        };
        DeckPane deck = new DeckPane(pages);
        deck.setPrefSize(560, 300);
        deck.setAnimationDuration(Duration.millis(280));

        Label status = new Label("DeckPane ready");
        deck.setBeforeShowCallback(node -> status.setText("beforeShow: " + (node instanceof Label l ? l.getText() : "page")));
        deck.setAfterHideCallback(node -> status.setText("afterHide: " + (node instanceof Label l ? l.getText() : "page")));

        Label topOverlay = new Label("topNode overlay");
        Button setTop = new Button("setTopNode");
        setTop.setOnAction(e -> deck.setTopNode(topOverlay));
        Button resetTop = new Button("resetTopNode");
        resetTop.setOnAction(e -> deck.resetTopNode());

        Node extra = deckPage("Extra page", "#22c55e");
        Button addChildren = new Button("addChildren(Insets, node)");
        addChildren.setOnAction(e -> {
            deck.addChildren(new Insets(20), extra);
            deck.swipeLeft(extra);
        });

        HBox swipes = hbox(
            deckButton("swipeUp", () -> deck.swipeUp(pages[0])),
            deckButton("swipeDown", () -> deck.swipeDown(pages[1])),
            deckButton("swipeLeft", () -> deck.swipeLeft(pages[2])),
            deckButton("swipeRight", () -> deck.swipeRight(pages[0]))
        );
        HBox slides = hbox(
            deckButton("slideUp", () -> deck.slideUp(pages[1])),
            deckButton("slideDown", () -> deck.slideDown(pages[2])),
            deckButton("slideLeft", () -> deck.slideLeft(pages[0])),
            deckButton("slideRight", () -> deck.slideRight(pages[1]))
        );

        InputGroup group = new InputGroup(
            new Label("@" ),
            new TextField("username"),
            new Button("Check")
        );

        Spacer spacer = new Spacer();
        HBox spacerRow = new HBox(new Label("Spacer pushes this right"), spacer);

        return vbox(
            section("DeckPane", deck, hbox(swipes), hbox(slides), hbox(setTop, resetTop, addChildren),
                status),
            section("InputGroup", group),
            section("Spacer", spacerRow)
        );
    }

    private Button deckButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Node deckPage(String text, String color) {
        Label page = new Label(text);
        page.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"
            + "-fx-font-size: 20px; -fx-padding: 24;");
        page.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return page;
    }

    // -------------------------------------------------------------- animations

    private Node animationsTab() {
        Label target = new Label("Animate me");
        target.setPadding(new Insets(40));
        target.getStyleClass().addAll(Styles.BG_ACCENT_SUBTLE, Styles.ROUNDED, Styles.TITLE_3);

        ComboBox<String> picker = new ComboBox<>();
        picker.getItems().addAll(ANIMATIONS);
        picker.getSelectionModel().selectFirst();

        Button play = new Button("Play");
        play.getStyleClass().add(Styles.ACCENT);
        play.setOnAction(e -> {
            target.setOpacity(1);
            target.setScaleX(1);
            target.setScaleY(1);
            target.setRotate(0);
            target.setTranslateX(0);
            target.setTranslateY(0);
            Timeline timeline = animationFor(picker.getValue(), target);
            if (timeline != null) {
                timeline.playFromStart();
            }
        });

        FlowPane names = new FlowPane(8, 6);
        for (String name : ANIMATIONS) {
            Label chip = new Label(name);
            chip.getStyleClass().addAll(Styles.BG_NEUTRAL_SUBTLE, Styles.TEXT_SMALL, Styles.ROUNDED);
            chip.setPadding(new Insets(4, 8, 4, 8));
            names.getChildren().add(chip);
        }

        return vbox(
            section("Animations", new StackPane(target) {{
                setMinHeight(160);
            }}, hbox(picker, play)),
            section("All Animations factory methods", names)
        );
    }

    private Timeline animationFor(String name, Node node) {
        Duration d = Duration.millis(700);
        return switch (name) {
            case "flash" -> Animations.flash(node);
            case "pulse" -> Animations.pulse(node);
            case "pulse(double)" -> Animations.pulse(node, 1.15);
            case "shakeX" -> Animations.shakeX(node);
            case "shakeX(double)" -> Animations.shakeX(node, 12);
            case "shakeY" -> Animations.shakeY(node);
            case "shakeY(double)" -> Animations.shakeY(node, 12);
            case "wobble" -> Animations.wobble(node);
            case "fadeIn" -> Animations.fadeIn(node, d);
            case "fadeOut" -> Animations.fadeOut(node, d);
            case "fadeInDown" -> Animations.fadeInDown(node, d);
            case "fadeOutDown" -> Animations.fadeOutDown(node, d);
            case "fadeInLeft" -> Animations.fadeInLeft(node, d);
            case "fadeOutLeft" -> Animations.fadeOutLeft(node, d);
            case "fadeInRight" -> Animations.fadeInRight(node, d);
            case "fadeOutRight" -> Animations.fadeOutRight(node, d);
            case "fadeInUp" -> Animations.fadeInUp(node, d);
            case "fadeOutUp" -> Animations.fadeOutUp(node, d);
            case "rollIn" -> Animations.rollIn(node, d);
            case "rollOut" -> Animations.rollOut(node, d);
            case "rotateIn" -> Animations.rotateIn(node, d);
            case "rotateOut" -> Animations.rotateOut(node, d);
            case "rotateInDownLeft" -> Animations.rotateInDownLeft(node, d);
            case "rotateOutDownLeft" -> Animations.rotateOutDownLeft(node, d);
            case "rotateInDownRight" -> Animations.rotateInDownRight(node, d);
            case "rotateOutDownRight" -> Animations.rotateOutDownRight(node, d);
            case "rotateInUpLeft" -> Animations.rotateInUpLeft(node, d);
            case "rotateOutUpLeft" -> Animations.rotateOutUpLeft(node, d);
            case "rotateInUpRight" -> Animations.rotateInUpRight(node, d);
            case "rotateOutUpRight" -> Animations.rotateOutUpRight(node, d);
            case "slideInDown" -> Animations.slideInDown(node, d);
            case "slideOutDown" -> Animations.slideOutDown(node, d);
            case "slideInLeft" -> Animations.slideInLeft(node, d);
            case "slideOutLeft" -> Animations.slideOutLeft(node, d);
            case "slideInRight" -> Animations.slideInRight(node, d);
            case "slideOutRight" -> Animations.slideOutRight(node, d);
            case "slideInUp" -> Animations.slideInUp(node, d);
            case "slideOutUp" -> Animations.slideOutUp(node, d);
            case "zoomIn" -> Animations.zoomIn(node, d);
            case "zoomIn(double)" -> Animations.zoomIn(node, d, 1.2);
            case "zoomOut" -> Animations.zoomOut(node, d);
            case "zoomOut(double)" -> Animations.zoomOut(node, d, 0.8);
            default -> null;
        };
    }

    // --------------------------------------------------------------- utilities

    private Node utilitiesTab() {
        Label platform = new Label("PlatformUtils: isWindows=" + PlatformUtils.isWindows()
            + ", isMac=" + PlatformUtils.isMac()
            + ", isLinux=" + PlatformUtils.isLinux()
            + ", isUnix=" + PlatformUtils.isUnix());

        Label dataUri = new Label("Styles.toDataURI -> " + truncate(Styles.toDataURI("Label { -fx-text-fill: red; }"), 60));
        Label styleConstants = new Label("Styles constants: ACCENT=" + Styles.ACCENT
            + ", DANGER=" + Styles.DANGER
            + ", FLAT=" + Styles.FLAT
            + ", ELEVATED_1=" + Styles.ELEVATED_1
            + ", TABS_FLOATING=" + Styles.TABS_FLOATING
            + ", INTERACTIVE=" + Styles.INTERACTIVE);
        styleConstants.setWrapText(true);

        // Formatters.
        TextField maskedA = MaskTextFormatter.createTextField("(999) 999-9999");
        TextField maskedB = new TextField();
        MaskTextFormatter.create(maskedB, "999-999");
        TextField maskedC = new TextField();
        MaskTextFormatter.create(maskedC, List.of(
            new SimpleMaskChar(Character::isDigit),
            SimpleMaskChar.fixed('-'),
            new SimpleMaskChar(Character::isLetter),
            new SimpleMaskChar(Character::isLetter)
        ));

        TextField passwordField = new TextField();
        PasswordTextFormatter formatter = PasswordTextFormatter.create(passwordField, PasswordTextFormatter.BULLET);
        Label passwordValue = new Label("PasswordTextFormatter password=" + formatter.getPassword());
        formatter.passwordProperty().addListener((o, a, b) ->
            passwordValue.setText("PasswordTextFormatter password=" + b));
        Button revealPassword = new Button("setRevealPassword");
        revealPassword.setOnAction(e -> formatter.setRevealPassword(!formatter.getRevealPassword()));

        Label maskCharInfo = new Label("MaskChar: DIGIT='" + MaskChar.INPUT_MASK_DIGIT
            + "', LETTER='" + MaskChar.INPUT_MASK_LETTER
            + "', DIGIT_OR_LETTER='" + MaskChar.INPUT_MASK_DIGIT_OR_LETTER
            + "', ANY_NON_SPACE='" + MaskChar.INPUT_MASK_ANY_NON_SPACE
            + "', HEX='" + MaskChar.INPUT_MASK_HEX
            + "', NON_ZERO_DIGIT='" + MaskChar.INPUT_MASK_DIGIT_NON_ZERO
            + "', 0_TO_1='" + MaskChar.INPUT_MASK_DIGIT_0_TO_1
            + "', ZERO='" + MaskChar.INPUT_MASK_DIGIT_ZERO
            + "', UNDERSCORE='" + MaskChar.UNDERSCORE + "'");
        maskCharInfo.setWrapText(true);

        SimpleMaskChar digitMask = new SimpleMaskChar(Character::isDigit);
        Label simpleMaskInfo = new Label("SimpleMaskChar(isDigit): isAllowed('5')="
            + digitMask.isAllowed('5') + ", isAllowed('x')=" + digitMask.isAllowed('x')
            + ", placeholder=" + digitMask.getPlaceholder()
            + ", isFixed=" + digitMask.isFixed()
            + ", fixed('-').isFixed=" + SimpleMaskChar.fixed('-').isFixed());

        // BBCode.
        TextFlow bbCodeFlow = BBCodeParser.createFormattedText(
            "[b]Bold[/b] [i]italic[/i] [u]underlined[/u] [s]strikethrough[/s]");
        VBox bbCodeLayout = BBCodeParser.createLayout(
            "[b]BBCodeParser.createLayout[/b] with a [color=#4493f8]colored[/color] word.");
        VBox bbCodeLayoutTyped = BBCodeParser.createLayout("[i]createLayout(String, Pane)[/i]", new VBox(4));

        StringBuilder bbLog = new StringBuilder();
        BBCodeHandler handler = new BBCodeHandler() {
            @Override
            public void startDocument(char[] data) {
                bbLog.append("startDocument(").append(data.length).append(" chars) ");
            }

            @Override
            public void endDocument() {
                bbLog.append("endDocument");
            }

            @Override
            public void startTag(String tag, Map<String, String> attributes, int start, int end) {
                bbLog.append("startTag(").append(tag).append(") ");
            }

            @Override
            public void endTag(String tag, int start, int end) {
                bbLog.append("endTag(").append(tag).append(") ");
            }

            @Override
            public void characters(int start, int end) {
                bbLog.append("characters ");
            }
        };
        new BBCodeParser("[b]Hello[/b] [i]world[/i]", handler).parse();
        Label bbLogLabel = new Label("BBCodeHandler log: " + bbLog);
        bbLogLabel.setWrapText(true);

        Label converters = new Label("IntegerStringConverter / DoubleStringConverter: see the Inputs tab; "
            + "both also have createFor(Spinner) and (TextField, min, max) constructors.");

        return vbox(
            section("PlatformUtils", platform),
            section("Styles", dataUri, styleConstants),
            section("Mask formatters", maskedA, maskedB, maskedC, maskCharInfo, simpleMaskInfo),
            section("PasswordTextFormatter", hbox(passwordField, revealPassword), passwordValue),
            section("BBCode", bbCodeFlow, bbCodeLayout, bbCodeLayoutTyped, bbLogLabel),
            section("Converters", converters)
        );
    }

    // ----------------------------------------------------------------- helpers

    /**
     * {@link FontIcon} stores its glyph font in an inline {@code -fx-font-family}
     * declaration. Assigning {@code setStyle} would wipe that out (the icon then
     * falls back to the system font and renders as a missing-glyph box), so
     * append the extra declarations instead.
     */
    private static FontIcon iconStyle(FontIcon icon, String inlineStyle) {
        String current = icon.getStyle();
        icon.setStyle((current == null || current.isBlank() ? "" : current + " ") + inlineStyle);
        return icon;
    }

    private Tab tab(String title, Feather icon, Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(18));
        Tab tab = new Tab(title, scroll);
        tab.setGraphic(new FontIcon(icon));
        tab.setClosable(false);
        return tab;
    }

    private VBox vbox(Node... nodes) {
        VBox box = new VBox(18, nodes);
        box.setPadding(new Insets(20, 40, 20, 20));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private HBox hbox(Node... nodes) {
        HBox box = new HBox(10, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox section(String title, Node... nodes) {
        Label heading = new Label(title);
        heading.getStyleClass().add(Styles.TITLE_4);
        VBox body = new VBox(10, heading);
        body.getChildren().addAll(nodes);
        body.setPadding(new Insets(14, 18, 14, 18));
        body.getStyleClass().add(Styles.BG_SUBTLE);
        body.setMaxWidth(Double.MAX_VALUE);
        return body;
    }

    private Label label(String text, String styleClass) {
        Label label = new Label(text + "  (" + styleClass + ")");
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Button button(String text, String styleClass) {
        Button button = new Button(text);
        if (styleClass != null) {
            button.getStyleClass().add(styleClass);
        }
        return button;
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    public static void main(String[] args) {
        launch(args);
    }
}