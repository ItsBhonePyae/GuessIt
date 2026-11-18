package Testing_for_bascially_everything;

import animatefx.animation.*;
import animatefx.animation.partial.Contract;
import animatefx.animation.partial.Expand;
import animatefx.modifier.IllegalModifierException;
import animatefx.modifier.Modifier;
import animatefx.modifier.ScaleModifier;
import animatefx.modifier.SlideModifier;
import animatefx.util.ParallelAnimationFX;
import animatefx.util.SequentialAnimationFX;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.Styles;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * AnimateFX 1.3.0 full feature showcase.
 *
 * <p>Exercises every public class and every public method of the
 * {@code io.github.typhon0:AnimateFX} library:</p>
 *
 * <ul>
 *   <li>all 78 concrete animation classes in {@code animatefx.animation}
 *       (both the {@code (Node)} and the no-argument constructor),</li>
 *   <li>the abstract {@link AnimationFX} base: {@code play}, {@code stop},
 *       {@code getNode}/{@code setNode}, {@code getTimeline}/{@code setTimeline},
 *       {@code setCycleCount}, {@code setSpeed}, {@code setDelay},
 *       {@code setResetOnFinished}/{@code isResetOnFinished},
 *       {@code playOnFinished}/{@code hasNextAnimation}/{@code getNextAnimation},
 *       {@code setOnFinished}, {@code applyAnimationModifier} and the
 *       {@code INDEFINITE} constant,</li>
 *   <li>{@link Bounce#resetNode()} (the only public {@code resetNode} override),</li>
 *   <li>{@code AnimateFXInterpolator.EASE},</li>
 *   <li>{@code animatefx.animation.partial.Contract} and {@code Expand},</li>
 *   <li>{@code animatefx.modifier.Modifier}, {@code ScaleModifier},
 *       {@code SlideModifier} and {@code IllegalModifierException},</li>
 *   <li>{@code animatefx.util.ParallelAnimationFX} and
 *       {@code SequentialAnimationFX} including all four constructors,
 *       {@code nodeProperty}, {@code getAnimation}, {@code setNode},
 *       {@code play}, {@code stop}, {@code getStatus} and
 *       {@code setResetOnFinished}.</li>
 * </ul>
 *
 * <p>The self-test tab constructs every class, invokes the API, and prints a
 * pass/fail report.</p>
 */
public class AnimateFX_full_test extends Application {

    private static final String ACCENT = "#4493f8";
    private static final String SECONDARY = "#22c55e";

    /** One gallery entry: name, category and a factory using the {@code (Node)} constructor. */
    private record Demo(String name, String category, Function<Node, AnimationFX> factory) { }

    /** Every concrete animation class in AnimateFX 1.3.0. */
    private static final List<Demo> DEMOS = createDemos();

    private final TabPane tabs = new TabPane();

    // Preview nodes. Label is both a Region (GlowBackground) and a Labeled (GlowText).
    private final Label target = card("Node A", ACCENT);
    private final Label altTarget = card("Node B", SECONDARY);
    private final Background targetBackground = target.getBackground();
    private final Background altTargetBackground = altTarget.getBackground();

    // Global playback controls.
    private final Slider speed = new Slider(0.1, 3, 1);
    private final Spinner<Integer> delay = new Spinner<>(0, 2000, 0, 50);
    private final Spinner<Integer> cycles = new Spinner<>(1, 10, 1);
    private final CheckBox indefinite = new CheckBox("INDEFINITE");
    private final CheckBox resetOnFinished = new CheckBox("resetOnFinished");

    private final Label status = new Label("Ready. Pick any animation.");
    private final Label state = new Label();
    private final Label selfTestSummary = new Label("Self-test has not run yet.");
    private final TextArea report = new TextArea();

    private Button selectedButton;
    private Demo selectedDemo;
    private AnimationFX current;
    private ParallelAnimationFX currentParallel;
    private SequentialAnimationFX currentSequential;
    private int finishedCount;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        tabs.getTabs().addAll(
            tab("Gallery", Feather.ZAP, galleryTab()),
            tab("AnimationFX API", Feather.SETTINGS, baseApiTab()),
            tab("Modifiers & partials", Feather.SLIDERS, modifiersTab()),
            tab("Parallel & Sequential", Feather.LAYERS, utilTab()),
            tab("Self-test", Feather.CHECK_SQUARE, selfTestTab())
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BorderPane root = new BorderPane();
        root.setTop(header());
        root.setCenter(tabs);
        root.setRight(previewPane());

        stage.setTitle("AnimateFX 1.3.0 - every class and method");
        stage.setScene(new Scene(root, 1400, 880));
        stage.show();
    }

    // ------------------------------------------------------------------ layout

    private Node header() {
        Label title = new Label("AnimateFX 1.3.0");
        title.getStyleClass().add(Styles.TITLE_2);

        Label subtitle = new Label("78 animation classes | "
            + "AnimationFX API | modifiers | ParallelAnimationFX | SequentialAnimationFX");
        subtitle.getStyleClass().add(Styles.TEXT_MUTED);

        VBox text = new VBox(2, title, subtitle);
        HBox box = new HBox(text);
        box.setPadding(new Insets(18, 24, 14, 24));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node previewPane() {
        Label heading = new Label("Live preview");
        heading.getStyleClass().add(Styles.TITLE_4);

        VBox nodes = new VBox(22, target, altTarget);
        nodes.setAlignment(Pos.CENTER);

        StackPane stageArea = new StackPane(nodes);
        stageArea.setMinSize(360, 240);
        stageArea.setPrefSize(360, 240);
        stageArea.getStyleClass().add(Styles.BG_INSET);
        stageArea.setStyle("-fx-background-radius: 12;");

        Label speedValue = new Label();
        speedValue.textProperty().bind(speed.valueProperty().asString("%.1fx"));

        HBox speedRow = new HBox(8, speed, speedValue);
        speedRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(speed, Priority.ALWAYS);

        GridPane controls = new GridPane();
        controls.setHgap(10);
        controls.setVgap(8);
        controls.addRow(0, new Label("Speed"), speedRow);
        controls.addRow(1, new Label("Delay (ms)"), delay);
        controls.addRow(2, new Label("Cycles"), cycles);
        controls.addRow(3, new Label(""), indefinite);
        controls.addRow(4, new Label(""), resetOnFinished);

        indefinite.selectedProperty().addListener((o, a, b) -> cycles.setDisable(b));

        Button play = new Button("Play selected", new FontIcon(Feather.PLAY));
        play.setOnAction(e -> play(selectedDemo != null ? selectedDemo : DEMOS.get(0)));
        Button stop = new Button("Stop", new FontIcon(Feather.SQUARE));
        stop.setOnAction(e -> stopAll());
        Button reset = new Button("Reset", new FontIcon(Feather.ROTATE_CCW));
        reset.setOnAction(e -> resetTargets());
        HBox buttons = new HBox(8, play, stop, reset);

        state.setWrapText(true);
        state.getStyleClass().add(Styles.TEXT_SMALL);
        status.setWrapText(true);

        Label hint = new Label("Delay, speed, cycles and reset apply to the next Play.");
        hint.setWrapText(true);
        hint.getStyleClass().add(Styles.TEXT_MUTED);

        VBox right = new VBox(12, heading, stageArea, new Separator(), controls,
            buttons, state, hint, status);
        right.setPadding(new Insets(18, 20, 18, 4));
        right.setPrefWidth(390);
        right.setMinWidth(370);
        return right;
    }

    private Node galleryTab() {
        TextField filter = new TextField();
        filter.setPromptText("Filter by name or category...");
        filter.setPrefWidth(320);

        Label count = new Label();
        FlowPane flow = new FlowPane(8, 8);

        Runnable rebuild = () -> {
            flow.getChildren().clear();
            String query = filter.getText() == null ? "" : filter.getText().trim().toLowerCase();
            int shown = 0;
            for (Demo demo : DEMOS) {
                if (!query.isEmpty()
                    && !demo.name().toLowerCase().contains(query)
                    && !demo.category().toLowerCase().contains(query)) {
                    continue;
                }
                Button button = new Button(demo.name());
                button.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
                button.setTooltip(new Tooltip(demo.category()));
                button.setOnAction(e -> {
                    selectedDemo = demo;
                    select(button);
                    play(demo);
                });
                flow.getChildren().add(button);
                shown++;
            }
            count.setText(shown + " / " + DEMOS.size() + " animations");
        };

        filter.textProperty().addListener((o, a, b) -> rebuild.run());
        rebuild.run();

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(4, 4, 4, 4));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label hint = new Label("Click an animation to play it on Node A. "
            + "Out animations leave the node off-screen; use Reset to restore it.");
        hint.setWrapText(true);
        hint.getStyleClass().add(Styles.TEXT_MUTED);

        VBox box = new VBox(10, new HBox(12, filter, count), hint, scroll);
        box.setPadding(new Insets(16));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Node baseApiTab() {
        Label log = new Label("Every button below calls one AnimationFX method. "
            + "Results appear here and in the live preview.");
        log.setWrapText(true);
        log.setPadding(new Insets(10, 12, 10, 12));
        log.getStyleClass().addAll(Styles.BG_SUBTLE, Styles.ROUNDED);

        Button playBtn = new Button("play()");
        playBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new FadeIn(target));
            track(fx, "play() -> FadeIn started");
            fx.play();
        });

        Button stopBtn = new Button("stop()");
        stopBtn.setOnAction(e -> {
            if (current == null) {
                setStatus("stop(): nothing has been played yet");
                return;
            }
            current.stop();
            setStatus("stop() -> timeline status: " + current.getTimeline().getStatus());
        });

        Button timelineBtn = new Button("getTimeline()");
        timelineBtn.setOnAction(e -> {
            AnimationFX fx = new FadeIn(target);
            Timeline timeline = fx.getTimeline();
            setStatus("getTimeline() -> keyFrames=" + timeline.getKeyFrames().size()
                + ", duration=" + millis(timeline.getTotalDuration())
                + ", cycleCount=" + timeline.getCycleCount());
            register(fx);
        });

        Button setTimelineBtn = new Button("setTimeline(Timeline)");
        setTimelineBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            FadeIn fx = new FadeIn(target);
            Timeline custom = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(target.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(350),
                    new KeyValue(target.translateXProperty(), 110, AnimateFXInterpolator.EASE)),
                new KeyFrame(Duration.millis(700),
                    new KeyValue(target.translateXProperty(), 0, AnimateFXInterpolator.EASE)));
            fx.setTimeline(custom);
            track(fx, "setTimeline(custom) -> playing a replacement timeline ("
                + fx.getTimeline().getKeyFrames().size() + " key frames)");
            fx.play();
        });

        Button nodeBtn = new Button("getNode() / setNode(Node)");
        nodeBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            Bounce fx = new Bounce(target);
            String before = id(fx.getNode());
            fx.setNode(altTarget);
            track(fx, "getNode()=" + before + " -> setNode(Node B) -> getNode()="
                + id(fx.getNode()));
            fx.play();
        });

        Button cyclesBtn = new Button("setCycleCount(3)");
        cyclesBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new Bounce(target));
            fx.setCycleCount(3);
            track(fx, "setCycleCount(3) -> Bounce repeats three times");
            fx.play();
        });

        Button indefiniteBtn = new Button("setCycleCount(INDEFINITE)");
        indefiniteBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new Pulse(target));
            fx.setCycleCount(AnimationFX.INDEFINITE);
            fx.setResetOnFinished(false);
            track(fx, "setCycleCount(INDEFINITE=" + AnimationFX.INDEFINITE
                + ") -> runs until Stop");
            fx.play();
        });

        Button speed2Btn = new Button("setSpeed(2.0)");
        speed2Btn.setOnAction(e -> runSpeedDemo(2.0));

        Button speedHalfBtn = new Button("setSpeed(0.5)");
        speedHalfBtn.setOnAction(e -> runSpeedDemo(0.5));

        Button delayBtn = new Button("setDelay(1s)");
        delayBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new FadeIn(target));
            fx.setDelay(Duration.seconds(1));
            track(fx, "setDelay(1s) -> starts after one second");
            fx.play();
        });

        Button resetBtn = new Button("setResetOnFinished(true) / isResetOnFinished()");
        resetBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new FadeOut(target));
            fx.setResetOnFinished(true);
            track(fx, "setResetOnFinished(true) -> isResetOnFinished()="
                + fx.isResetOnFinished() + "; opacity returns to 1 after the fade");
            fx.play();
        });

        Button onFinishedBtn = new Button("setOnFinished(handler)");
        onFinishedBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX fx = tune(new FadeIn(target));
            register(fx);
            int[] count = {0};
            fx.setOnFinished(ev -> {
                count[0]++;
                setStatus("setOnFinished(): handler fired " + count[0] + " time(s)");
            });
            setStatus("setOnFinished(): waiting for FadeIn to finish...");
            fx.play();
        });

        Button chainBtn = new Button("playOnFinished(FadeOut)");
        chainBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            AnimationFX first = tune(new FadeIn(target));
            AnimationFX second = tune(new FadeOut(target));
            first.playOnFinished(second);
            track(first, "playOnFinished(FadeOut) -> FadeIn then FadeOut");
            first.play();
        });

        Button nextBtn = new Button("hasNextAnimation() / getNextAnimation()");
        nextBtn.setOnAction(e -> {
            AnimationFX fx = new FadeIn(target);
            boolean before = fx.hasNextAnimation();
            AnimationFX next = new FadeOut(target);
            AnimationFX returned = fx.playOnFinished(next);
            setStatus("hasNextAnimation(): before=" + before + ", after=" + fx.hasNextAnimation()
                + " | getNextAnimation()=" + (fx.getNextAnimation() == null
                    ? "null" : fx.getNextAnimation().getClass().getSimpleName())
                + " | playOnFinished() returns this=" + (returned == fx));
            register(fx);
        });

        Button modifierBtn = new Button("applyAnimationModifier(Modifier) on a base animation");
        modifierBtn.setOnAction(e -> {
            try {
                new FadeIn(target).applyAnimationModifier(new ScaleModifier(0.5, 1.5));
                setStatus("applyAnimationModifier(): unexpectedly succeeded");
            } catch (UnsupportedOperationException ex) {
                setStatus("applyAnimationModifier(): " + ex.getClass().getSimpleName()
                    + " -> " + ex.getMessage());
            }
        });

        Button bounceResetBtn = new Button("Bounce.resetNode()");
        bounceResetBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            target.setTranslateY(80);
            Bounce bounce = new Bounce(target);
            AnimationFX returned = bounce.resetNode();
            setStatus("Bounce.resetNode() -> translateY 80 -> " + target.getTranslateY()
                + ", returns this=" + (returned == bounce));
        });

        Button easeBtn = new Button("AnimateFXInterpolator.EASE");
        easeBtn.setOnAction(e -> {
            stopAll();
            resetTargets();
            FadeIn fx = new FadeIn(target);
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(target.rotateProperty(), 0)),
                new KeyFrame(Duration.millis(800),
                    new KeyValue(target.rotateProperty(), 180, AnimateFXInterpolator.EASE)),
                new KeyFrame(Duration.millis(1600),
                    new KeyValue(target.rotateProperty(), 0, AnimateFXInterpolator.EASE)));
            fx.setTimeline(timeline);
            track(fx, "AnimateFXInterpolator.EASE = " + AnimateFXInterpolator.EASE);
            fx.play();
        });

        return scroll(vbox(
            log,
            section("Lifecycle", hbox(playBtn, stopBtn, bounceResetBtn)),
            section("Timeline", hbox(timelineBtn, setTimelineBtn, easeBtn)),
            section("Node", nodeBtn),
            section("Cycles", hbox(cyclesBtn, indefiniteBtn)),
            section("Speed", hbox(speed2Btn, speedHalfBtn)),
            section("Delay", delayBtn),
            section("Reset", hbox(resetBtn, onFinishedBtn)),
            section("Chaining", hbox(chainBtn, nextBtn)),
            section("Modifiers", modifierBtn)
        ));
    }

    private void runSpeedDemo(double value) {
        stopAll();
        resetTargets();
        AnimationFX fx = tune(new FadeIn(target));
        fx.setSpeed(value);
        track(fx, "setSpeed(" + value + ") -> timeline rate=" + fx.getTimeline().getRate());
        fx.play();
    }

    private Node modifiersTab() {
        return scroll(vbox(
            section("Partial animations (ScaleAnimation)",
                hbox(
                    button("Expand(node) default 1.0 -> 1.05", () -> {
                        stopAll();
                        resetTargets();
                        AnimationFX fx = tune(new Expand(target));
                        track(fx, "Expand(target): default scale 1.0 -> 1.05");
                        fx.play();
                    }),
                    button("Contract(node) default 1.05 -> 1.0", () -> {
                        stopAll();
                        resetTargets();
                        AnimationFX fx = tune(new Contract(target));
                        track(fx, "Contract(target): default scale 1.05 -> 1.0");
                        fx.play();
                    }),
                    button("Expand + ScaleModifier(0.5, 1.4)", () -> {
                        stopAll();
                        resetTargets();
                        Expand fx = new Expand(target);
                        fx.applyAnimationModifier(new ScaleModifier(0.5, 1.4));
                        tune(fx);
                        track(fx, "Expand + ScaleModifier(0.5, 1.4)");
                        fx.play();
                    }),
                    button("Contract + ScaleModifier(1.4, 0.6)", () -> {
                        stopAll();
                        resetTargets();
                        Contract fx = new Contract(target);
                        fx.applyAnimationModifier(new ScaleModifier(1.4, 0.6));
                        tune(fx);
                        track(fx, "Contract + ScaleModifier(1.4, 0.6)");
                        fx.play();
                    })
                )),
            section("Slide animations (SlideAnimation)",
                hbox(
                    button("SlideInLeft + SlideModifier(180)", () -> {
                        stopAll();
                        resetTargets();
                        SlideInLeft fx = new SlideInLeft(target);
                        fx.applyAnimationModifier(new SlideModifier(180));
                        tune(fx);
                        track(fx, "SlideInLeft + SlideModifier(180)");
                        fx.play();
                    }),
                    button("SlideOutLeft + SlideModifier(180)", () -> {
                        stopAll();
                        resetTargets();
                        SlideOutLeft fx = new SlideOutLeft(target);
                        fx.applyAnimationModifier(new SlideModifier(180));
                        tune(fx);
                        track(fx, "SlideOutLeft + SlideModifier(180)");
                        fx.play();
                    }),
                    button("SlideInRight + SlideModifier(90)", () -> {
                        stopAll();
                        resetTargets();
                        SlideInRight fx = new SlideInRight(target);
                        fx.applyAnimationModifier(new SlideModifier(90));
                        tune(fx);
                        track(fx, "SlideInRight + SlideModifier(90)");
                        fx.play();
                    })
                )),
            section("ScaleModifier / SlideModifier API",
                hbox(
                    button("ScaleModifier() + setters + getters", () -> {
                        ScaleModifier modifier = new ScaleModifier();
                        modifier.setBeginScale(0.8);
                        modifier.setEndScale(1.25);
                        setStatus("ScaleModifier(): begin=" + modifier.getBeginScale()
                            + ", end=" + modifier.getEndScale());
                    }),
                    button("ScaleModifier(0.7, 1.3)", () -> {
                        ScaleModifier modifier = new ScaleModifier(0.7, 1.3);
                        setStatus("ScaleModifier(0.7, 1.3): begin=" + modifier.getBeginScale()
                            + ", end=" + modifier.getEndScale());
                    }),
                    button("SlideModifier() + setter + getter", () -> {
                        SlideModifier modifier = new SlideModifier();
                        modifier.setSlideBy(120);
                        setStatus("SlideModifier(): slideBy=" + modifier.getSlideBy());
                    }),
                    button("SlideModifier(60)", () -> {
                        SlideModifier modifier = new SlideModifier(60);
                        setStatus("SlideModifier(60): slideBy=" + modifier.getSlideBy());
                    })
                )),
            section("Illegal modifier cases",
                hbox(
                    button("Expand + ScaleModifier(2, 1)", () -> {
                        try {
                            new Expand(target).applyAnimationModifier(new ScaleModifier(2, 1));
                            setStatus("Expand + ScaleModifier(2, 1): no exception");
                        } catch (IllegalModifierException ex) {
                            setStatus("IllegalModifierException: " + ex.getMessage());
                        }
                    }),
                    button("Expand + SlideModifier", () -> {
                        try {
                            new Expand(target).applyAnimationModifier(new SlideModifier(10));
                            setStatus("Expand + SlideModifier: no exception");
                        } catch (IllegalArgumentException ex) {
                            setStatus(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        }
                    }),
                    button("Expand + null", () -> {
                        try {
                            new Expand(target).applyAnimationModifier(null);
                            setStatus("Expand + null: no exception");
                        } catch (NullPointerException ex) {
                            setStatus("NullPointerException: " + ex.getMessage());
                        }
                    }),
                    button("FadeIn + ScaleModifier", () -> {
                        try {
                            new FadeIn(target).applyAnimationModifier(new ScaleModifier(1, 2));
                            setStatus("FadeIn + ScaleModifier: no exception");
                        } catch (UnsupportedOperationException ex) {
                            setStatus("UnsupportedOperationException: " + ex.getMessage());
                        }
                    }),
                    button("new IllegalModifierException(...)", () -> {
                        IllegalModifierException ex =
                            new IllegalModifierException("manual IllegalModifierException");
                        setStatus("IllegalModifierException message: " + ex.getMessage()
                            + " | instanceof RuntimeException=" + (ex instanceof RuntimeException));
                    })
                ))
        ));
    }

    private Node utilTab() {
        return scroll(vbox(
            section("ParallelAnimationFX constructors",
                hbox(
                    button("new ParallelAnimationFX(node)", () -> {
                        resetTargets();
                        ParallelAnimationFX p = new ParallelAnimationFX(target);
                        p.getAnimation().addAll(new FadeIn(), new RotateIn());
                        currentParallel = p;
                        p.play();
                        setStatus("ParallelAnimationFX(node): node=" + id(p.nodeProperty().get())
                            + ", animations=" + p.getAnimation().size()
                            + ", status=" + p.getStatus());
                    }),
                    button("new ParallelAnimationFX(node, animations...)", () -> {
                        resetTargets();
                        ParallelAnimationFX p =
                            new ParallelAnimationFX(target, new FadeIn(), new RotateIn());
                        currentParallel = p;
                        p.play();
                        setStatus("ParallelAnimationFX(node, ...): animations="
                            + p.getAnimation().size() + ", status=" + p.getStatus());
                    }),
                    button("new ParallelAnimationFX(animations...)", () -> {
                        resetTargets();
                        ParallelAnimationFX p =
                            new ParallelAnimationFX(new FadeIn(), new RotateIn());
                        p.setNode(target);
                        currentParallel = p;
                        p.play();
                        setStatus("ParallelAnimationFX(...) + setNode(node): node="
                            + id(p.nodeProperty().get()) + ", status=" + p.getStatus());
                    }),
                    button("new ParallelAnimationFX()", () -> {
                        resetTargets();
                        ParallelAnimationFX p = new ParallelAnimationFX();
                        p.nodeProperty().set(target);
                        p.getAnimation().addAll(new FadeIn(), new RotateIn());
                        currentParallel = p;
                        p.play();
                        setStatus("ParallelAnimationFX(): nodeProperty().set(node), animations="
                            + p.getAnimation().size() + ", status=" + p.getStatus());
                    })
                ),
                hbox(
                    button("setResetOnFinished(true)", () -> {
                        if (currentParallel == null) {
                            setStatus("Start a parallel animation first");
                            return;
                        }
                        currentParallel.setResetOnFinished(true);
                        setStatus("ParallelAnimationFX.setResetOnFinished(true) called "
                            + "(the implementation is a no-op)");
                    }),
                    button("stop() / getStatus()", () -> {
                        if (currentParallel == null) {
                            setStatus("Start a parallel animation first");
                            return;
                        }
                        currentParallel.stop();
                        setStatus("ParallelAnimationFX.stop(): getStatus()="
                            + currentParallel.getStatus());
                    }),
                    button("play() with an empty list", () -> {
                        try {
                            new ParallelAnimationFX(target).play();
                            setStatus("Empty parallel play(): no exception");
                        } catch (RuntimeException ex) {
                            setStatus("Empty parallel play() -> "
                                + ex.getClass().getSimpleName()
                                + " (the list must contain at least one animation)");
                        }
                    })
                )),
            section("SequentialAnimationFX constructors",
                hbox(
                    button("new SequentialAnimationFX(node)", () -> {
                        resetTargets();
                        SequentialAnimationFX s = new SequentialAnimationFX(target);
                        s.getAnimation().addAll(new FadeIn(), new RotateIn(), new FadeOut());
                        currentSequential = s;
                        s.play();
                        setStatus("SequentialAnimationFX(node): node=" + id(s.nodeProperty().get())
                            + ", animations=" + s.getAnimation().size()
                            + ", status=" + s.getStatus());
                    }),
                    button("new SequentialAnimationFX(node, animations...)", () -> {
                        resetTargets();
                        SequentialAnimationFX s =
                            new SequentialAnimationFX(target, new FadeIn(), new RotateIn(), new FadeOut());
                        currentSequential = s;
                        s.play();
                        setStatus("SequentialAnimationFX(node, ...): animations="
                            + s.getAnimation().size() + ", status=" + s.getStatus());
                    }),
                    button("new SequentialAnimationFX(animations...)", () -> {
                        resetTargets();
                        SequentialAnimationFX s =
                            new SequentialAnimationFX(new FadeIn(), new RotateIn(), new FadeOut());
                        s.setNode(target);
                        currentSequential = s;
                        s.play();
                        setStatus("SequentialAnimationFX(...) + setNode(node): node="
                            + id(s.nodeProperty().get()) + ", status=" + s.getStatus());
                    }),
                    button("new SequentialAnimationFX()", () -> {
                        resetTargets();
                        SequentialAnimationFX s = new SequentialAnimationFX();
                        s.nodeProperty().set(target);
                        s.getAnimation().addAll(new FadeIn(), new RotateIn(), new FadeOut());
                        currentSequential = s;
                        s.play();
                        setStatus("SequentialAnimationFX(): nodeProperty().set(node), animations="
                            + s.getAnimation().size() + ", status=" + s.getStatus());
                    })
                ),
                hbox(
                    button("setResetOnFinished(true)", () -> {
                        resetTargets();
                        SequentialAnimationFX s = new SequentialAnimationFX(target,
                            new FadeIn(), new RotateIn(), new FadeOut());
                        s.setResetOnFinished(true);
                        currentSequential = s;
                        s.play();
                        setStatus("SequentialAnimationFX.setResetOnFinished(true): last animation "
                            + "isResetOnFinished="
                            + s.getAnimation().get(s.getAnimation().size() - 1).isResetOnFinished());
                    }),
                    button("stop() / getStatus()", () -> {
                        if (currentSequential == null) {
                            setStatus("Start a sequential animation first");
                            return;
                        }
                        currentSequential.stop();
                        setStatus("SequentialAnimationFX.stop(): getStatus()="
                            + currentSequential.getStatus());
                    }),
                    button("play() with an empty list", () -> {
                        try {
                            new SequentialAnimationFX(target).play();
                            setStatus("Empty sequential play(): no exception");
                        } catch (RuntimeException ex) {
                            setStatus("Empty sequential play() -> "
                                + ex.getClass().getSimpleName()
                                + " (the list must contain at least one animation)");
                        }
                    })
                ))
        ));
    }

    private Node selfTestTab() {
        Button run = new Button("Run full self-test", new FontIcon(Feather.CHECK_SQUARE));
        run.getStyleClass().add(Styles.ACCENT);
        run.setOnAction(e -> runSelfTest());

        Label hint = new Label("Builds every animation, calls every public method, "
            + "and reports PASS/FAIL for each check.");
        hint.setWrapText(true);
        hint.getStyleClass().add(Styles.TEXT_MUTED);

        report.setEditable(false);
        report.setWrapText(false);
        report.setPrefRowCount(20);
        report.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        VBox.setVgrow(report, Priority.ALWAYS);

        VBox box = new VBox(10, hbox(run, selfTestSummary), hint, report);
        box.setPadding(new Insets(16));
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    // ------------------------------------------------------------- self-test

    private void runSelfTest() {
        stopAll();
        resetTargets();

        StringBuilder sb = new StringBuilder();
        int[] pass = {0};
        int[] fail = {0};

        sb.append("== Animation classes (").append(DEMOS.size()).append(" concrete) ==\n");
        for (Demo demo : DEMOS) {
            try {
                AnimationFX fx = demo.factory().apply(target);
                Timeline timeline = fx.getTimeline();
                require(timeline != null, "getTimeline()");
                require(!timeline.getKeyFrames().isEmpty(), "keyFrames");
                require(fx.getNode() == target, "getNode()");

                fx.setCycleCount(2);
                require(timeline.getCycleCount() == 2, "setCycleCount(2)");
                fx.setSpeed(2);
                require(Math.abs(timeline.getRate() - 2) < 1e-9, "setSpeed(2)");
                fx.setDelay(Duration.millis(25));
                require(Math.abs(timeline.getDelay().toMillis() - 25) < 1e-9, "setDelay(25ms)");
                fx.setResetOnFinished(true);
                require(fx.isResetOnFinished(), "isResetOnFinished()");

                fx.play();
                require(timeline.getStatus() == Animation.Status.RUNNING, "play() status");
                fx.stop();
                require(timeline.getStatus() == Animation.Status.STOPPED, "stop() status");

                fx.setNode(altTarget);
                require(fx.getNode() == altTarget, "setNode()");
                require(fx.getTimeline() != null, "timeline after setNode()");

                if (fx.getClass() != GlowBackground.class && fx.getClass() != GlowText.class) {
                    AnimationFX noArg = (AnimationFX) fx.getClass()
                        .getDeclaredConstructor().newInstance();
                    noArg.setNode(target);
                    require(noArg.getTimeline() != null, "no-arg constructor + setNode()");
                }

                pass[0]++;
                sb.append("PASS ").append(demo.name())
                    .append(" [").append(demo.category()).append("] keyFrames=")
                    .append(timeline.getKeyFrames().size()).append('\n');
            } catch (Throwable ex) {
                fail[0]++;
                sb.append("FAIL ").append(demo.name()).append(" -> ")
                    .append(ex.getClass().getSimpleName()).append(": ")
                    .append(ex.getMessage()).append('\n');
            }
        }
        resetTargets();

        sb.append("\n== Special Glow constructors ==\n");
        check(sb, pass, fail, "GlowBackground(Background) + setNode()", () -> {
            GlowBackground fx = new GlowBackground(target.getBackground());
            fx.setNode(target);
            require(fx.getNode() == target, "getNode()");
            require(fx.getTimeline() != null, "getTimeline()");
        });
        check(sb, pass, fail, "GlowText(Paint, Paint, Paint) + setNode()", () -> {
            GlowText fx = new GlowText(Color.WHITE, Color.YELLOW, Color.ORANGE);
            fx.setNode(target);
            require(fx.getNode() == target, "getNode()");
            require(fx.getTimeline() != null, "getTimeline()");
        });

        sb.append("\n== AnimationFX base API ==\n");
        check(sb, pass, fail, "AnimationFX.INDEFINITE == -1",
            () -> require(AnimationFX.INDEFINITE == -1, "INDEFINITE"));
        check(sb, pass, fail, "getTimeline() / setTimeline(Timeline)", () -> {
            FadeIn fx = new FadeIn(target);
            Timeline original = fx.getTimeline();
            require(original != null, "getTimeline()");
            Timeline custom = new Timeline(new KeyFrame(Duration.millis(10)));
            fx.setTimeline(custom);
            require(fx.getTimeline() == custom, "setTimeline()");
            require(fx.getTimeline() != original, "timeline replaced");
        });
        check(sb, pass, fail, "setCycleCount(INDEFINITE)", () -> {
            Pulse fx = new Pulse(target);
            fx.setCycleCount(AnimationFX.INDEFINITE);
            require(fx.getTimeline().getCycleCount() == AnimationFX.INDEFINITE, "cycleCount");
        });
        check(sb, pass, fail, "playOnFinished() / hasNextAnimation() / getNextAnimation()", () -> {
            FadeIn a = new FadeIn(target);
            FadeOut b = new FadeOut(target);
            require(!a.hasNextAnimation(), "hasNextAnimation() before");
            AnimationFX returned = a.playOnFinished(b);
            require(returned == a, "playOnFinished() returns this");
            require(a.hasNextAnimation(), "hasNextAnimation() after");
            require(a.getNextAnimation() == b, "getNextAnimation()");
        });
        check(sb, pass, fail, "Bounce.resetNode()", () -> {
            target.setTranslateY(55);
            Bounce bounce = new Bounce(target);
            AnimationFX returned = bounce.resetNode();
            require(returned == bounce, "returns this");
            require(target.getTranslateY() == 0, "translateY reset");
        });
        check(sb, pass, fail, "AnimateFXInterpolator.EASE", () ->
            require(AnimateFXInterpolator.EASE != null, "EASE"));

        sb.append("\n== Modifiers & partial animations ==\n");
        check(sb, pass, fail, "Modifier is an interface", () ->
            require(Modifier.class.isInterface(), "Modifier.class.isInterface()"));
        check(sb, pass, fail, "ScaleAnimation is abstract", () ->
            require(java.lang.reflect.Modifier.isAbstract(ScaleAnimation.class.getModifiers()),
                "abstract"));
        check(sb, pass, fail, "SlideAnimation is abstract", () ->
            require(java.lang.reflect.Modifier.isAbstract(SlideAnimation.class.getModifiers()),
                "abstract"));
        check(sb, pass, fail, "ScaleModifier() + setters/getters", () -> {
            ScaleModifier modifier = new ScaleModifier();
            require(modifier.getBeginScale() == null, "default begin");
            require(modifier.getEndScale() == null, "default end");
            modifier.setBeginScale(0.7);
            modifier.setEndScale(1.3);
            require(modifier.getBeginScale().doubleValue() == 0.7, "getBeginScale()");
            require(modifier.getEndScale().doubleValue() == 1.3, "getEndScale()");
        });
        check(sb, pass, fail, "ScaleModifier(Number, Number)", () -> {
            ScaleModifier modifier = new ScaleModifier(0.4, 1.6);
            require(modifier.getBeginScale().doubleValue() == 0.4, "begin");
            require(modifier.getEndScale().doubleValue() == 1.6, "end");
        });
        check(sb, pass, fail, "SlideModifier() + setter/getter", () -> {
            SlideModifier modifier = new SlideModifier();
            require(modifier.getSlideBy() == null, "default slideBy");
            modifier.setSlideBy(75);
            require(modifier.getSlideBy().doubleValue() == 75, "getSlideBy()");
        });
        check(sb, pass, fail, "SlideModifier(Number)", () -> {
            SlideModifier modifier = new SlideModifier(42);
            require(modifier.getSlideBy().doubleValue() == 42, "slideBy");
        });
        check(sb, pass, fail, "Expand.applyAnimationModifier(ScaleModifier)", () -> {
            Expand fx = new Expand(target);
            fx.applyAnimationModifier(new ScaleModifier(0.6, 1.3));
            require(!fx.getTimeline().getKeyFrames().isEmpty(), "keyFrames");
            fx.play();
            fx.stop();
        });
        check(sb, pass, fail, "Contract.applyAnimationModifier(ScaleModifier)", () -> {
            Contract fx = new Contract(target);
            fx.applyAnimationModifier(new ScaleModifier(1.3, 0.7));
            require(!fx.getTimeline().getKeyFrames().isEmpty(), "keyFrames");
            fx.play();
            fx.stop();
        });
        check(sb, pass, fail, "SlideInLeft.applyAnimationModifier(SlideModifier)", () -> {
            SlideInLeft fx = new SlideInLeft(target);
            fx.applyAnimationModifier(new SlideModifier(150));
            require(!fx.getTimeline().getKeyFrames().isEmpty(), "keyFrames");
            fx.play();
            fx.stop();
        });
        check(sb, pass, fail, "Expand + ScaleModifier(2, 1) throws IllegalModifierException", () -> {
            try {
                new Expand(target).applyAnimationModifier(new ScaleModifier(2, 1));
                throw new AssertionError("expected IllegalModifierException");
            } catch (IllegalModifierException expected) {
                require(expected.getMessage().contains("beginScale"), "message");
            }
        });
        check(sb, pass, fail, "Expand + SlideModifier throws IllegalArgumentException", () -> {
            try {
                new Expand(target).applyAnimationModifier(new SlideModifier(10));
                throw new AssertionError("expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                require(expected.getMessage().contains("SlideModifier"), "message");
            }
        });
        check(sb, pass, fail, "Expand + null throws NullPointerException", () -> {
            try {
                new Expand(target).applyAnimationModifier(null);
                throw new AssertionError("expected NullPointerException");
            } catch (NullPointerException expected) {
                require(expected.getMessage().contains("modifier"), "message");
            }
        });
        check(sb, pass, fail, "FadeIn.applyAnimationModifier throws UnsupportedOperationException", () -> {
            try {
                new FadeIn(target).applyAnimationModifier(new ScaleModifier(1, 2));
                throw new AssertionError("expected UnsupportedOperationException");
            } catch (UnsupportedOperationException expected) {
                require(expected.getMessage().contains("doesn't support"), "message");
            }
        });
        check(sb, pass, fail, "IllegalModifierException(String)", () -> {
            IllegalModifierException ex = new IllegalModifierException("test message");
            require("test message".equals(ex.getMessage()), "getMessage()");
            require(ex instanceof RuntimeException, "RuntimeException");
        });

        sb.append("\n== ParallelAnimationFX / SequentialAnimationFX ==\n");
        check(sb, pass, fail, "ParallelAnimationFX(Node)", () -> {
            ParallelAnimationFX p = new ParallelAnimationFX(target);
            require(p.nodeProperty().get() == target, "nodeProperty()");
            p.setNode(altTarget);
            require(p.nodeProperty().get() == altTarget, "setNode()");
            p.getAnimation().addAll(new FadeIn(), new RotateIn());
            require(p.getAnimation().size() == 2, "getAnimation()");
            p.play();
            require(p.getStatus() == Animation.Status.RUNNING, "play() status");
            p.setResetOnFinished(true);
            p.stop();
            require(p.getStatus() == Animation.Status.STOPPED, "stop() status");
        });
        check(sb, pass, fail, "ParallelAnimationFX(Node, AnimationFX...)", () -> {
            ParallelAnimationFX p = new ParallelAnimationFX(target, new FadeIn(), new RotateIn());
            require(p.getAnimation().size() == 2, "animations");
            p.play();
            require(p.getStatus() == Animation.Status.RUNNING, "status");
            p.stop();
        });
        check(sb, pass, fail, "ParallelAnimationFX(AnimationFX...)", () -> {
            ParallelAnimationFX p = new ParallelAnimationFX(new FadeIn(), new RotateIn());
            p.setNode(target);
            p.play();
            require(p.getStatus() == Animation.Status.RUNNING, "status");
            p.stop();
        });
        check(sb, pass, fail, "ParallelAnimationFX() + nodeProperty()", () -> {
            ParallelAnimationFX p = new ParallelAnimationFX();
            p.nodeProperty().set(target);
            p.getAnimation().add(new FadeIn());
            p.play();
            require(p.getStatus() == Animation.Status.RUNNING, "status");
            p.stop();
        });
        check(sb, pass, fail, "ParallelAnimationFX empty play() throws IndexOutOfBoundsException", () -> {
            try {
                new ParallelAnimationFX(target).play();
                throw new AssertionError("expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // Documented behaviour: the list must not be empty.
            }
        });
        check(sb, pass, fail, "SequentialAnimationFX(Node)", () -> {
            SequentialAnimationFX s = new SequentialAnimationFX(target);
            require(s.nodeProperty().get() == target, "nodeProperty()");
            s.setNode(altTarget);
            require(s.nodeProperty().get() == altTarget, "setNode()");
            s.getAnimation().addAll(new FadeIn(), new RotateIn());
            require(s.getAnimation().size() == 2, "getAnimation()");
            s.play();
            require(s.getStatus() == Animation.Status.RUNNING, "play() status");
            s.stop();
            require(s.getStatus() == Animation.Status.STOPPED, "stop() status");
        });
        check(sb, pass, fail, "SequentialAnimationFX(Node, AnimationFX...)", () -> {
            SequentialAnimationFX s =
                new SequentialAnimationFX(target, new FadeIn(), new RotateIn());
            require(s.getAnimation().size() == 2, "animations");
            s.play();
            require(s.getStatus() == Animation.Status.RUNNING, "status");
            s.stop();
        });
        check(sb, pass, fail, "SequentialAnimationFX(AnimationFX...)", () -> {
            SequentialAnimationFX s = new SequentialAnimationFX(new FadeIn(), new RotateIn());
            s.setNode(target);
            s.play();
            require(s.getStatus() == Animation.Status.RUNNING, "status");
            s.stop();
        });
        check(sb, pass, fail, "SequentialAnimationFX() + nodeProperty()", () -> {
            SequentialAnimationFX s = new SequentialAnimationFX();
            s.nodeProperty().set(target);
            s.getAnimation().add(new FadeIn());
            s.play();
            require(s.getStatus() == Animation.Status.RUNNING, "status");
            s.stop();
        });
        check(sb, pass, fail, "SequentialAnimationFX.setResetOnFinished(true)", () -> {
            SequentialAnimationFX s =
                new SequentialAnimationFX(target, new FadeIn(), new RotateIn(), new FadeOut());
            s.setResetOnFinished(true);
            s.play();
            AnimationFX last = s.getAnimation().get(s.getAnimation().size() - 1);
            require(last.isResetOnFinished(), "last animation reset flag");
            s.stop();
        });
        check(sb, pass, fail, "SequentialAnimationFX empty play() throws IndexOutOfBoundsException", () -> {
            try {
                new SequentialAnimationFX(target).play();
                throw new AssertionError("expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // Documented behaviour: the list must not be empty.
            }
        });
        resetTargets();

        // Async checks: playOnFinished + setOnFinished need real animation cycles.
        sb.append("\n== Async completion (playOnFinished + setOnFinished) ==\n");
        int[] handlerCalls = {0};
        AnimationFX first = new FadeIn(target);
        AnimationFX second = new FadeOut(target);
        first.setSpeed(20);
        second.setSpeed(20);
        second.setOnFinished(e -> handlerCalls[0]++);
        first.playOnFinished(second);
        first.play();

        report.setText(sb + "\nWaiting for the chained animation to finish...");
        PauseTransition wait = new PauseTransition(Duration.millis(900));
        wait.setOnFinished(e -> {
            check(sb, pass, fail,
                "playOnFinished() runs the next animation; setOnFinished() handler fires",
                () -> require(handlerCalls[0] == 1, "handler calls=" + handlerCalls[0]));
            sb.append("\nRESULT: ").append(pass[0]).append(" passed, ")
                .append(fail[0]).append(" failed\n");
            report.setText(sb.toString());
            report.positionCaret(report.getLength());
            selfTestSummary.setText(fail[0] == 0
                ? "ALL " + pass[0] + " CHECKS PASSED"
                : pass[0] + " passed / " + fail[0] + " failed");
            resetTargets();
        });
        wait.play();
    }

    // --------------------------------------------------------------- helpers

    private static List<Demo> createDemos() {
        List<Demo> demos = new ArrayList<>();
        demos.add(new Demo("Bounce", "Attention", n -> new Bounce(n)));
        demos.add(new Demo("Flash", "Attention", n -> new Flash(n)));
        demos.add(new Demo("Pulse", "Attention", n -> new Pulse(n)));
        demos.add(new Demo("RubberBand", "Attention", n -> new RubberBand(n)));
        demos.add(new Demo("Shake", "Attention", n -> new Shake(n)));
        demos.add(new Demo("Swing", "Attention", n -> new Swing(n)));
        demos.add(new Demo("Tada", "Attention", n -> new Tada(n)));
        demos.add(new Demo("Wobble", "Attention", n -> new Wobble(n)));
        demos.add(new Demo("Jello", "Attention", n -> new Jello(n)));
        demos.add(new Demo("JackInTheBox", "Attention", n -> new JackInTheBox(n)));

        demos.add(new Demo("BounceIn", "Bounce in", n -> new BounceIn(n)));
        demos.add(new Demo("BounceInDown", "Bounce in", n -> new BounceInDown(n)));
        demos.add(new Demo("BounceInLeft", "Bounce in", n -> new BounceInLeft(n)));
        demos.add(new Demo("BounceInRight", "Bounce in", n -> new BounceInRight(n)));
        demos.add(new Demo("BounceInUp", "Bounce in", n -> new BounceInUp(n)));

        demos.add(new Demo("BounceOut", "Bounce out", n -> new BounceOut(n)));
        demos.add(new Demo("BounceOutDown", "Bounce out", n -> new BounceOutDown(n)));
        demos.add(new Demo("BounceOutLeft", "Bounce out", n -> new BounceOutLeft(n)));
        demos.add(new Demo("BounceOutRight", "Bounce out", n -> new BounceOutRight(n)));
        demos.add(new Demo("BounceOutUp", "Bounce out", n -> new BounceOutUp(n)));

        demos.add(new Demo("FadeIn", "Fade in", n -> new FadeIn(n)));
        demos.add(new Demo("FadeInDown", "Fade in", n -> new FadeInDown(n)));
        demos.add(new Demo("FadeInDownBig", "Fade in", n -> new FadeInDownBig(n)));
        demos.add(new Demo("FadeInLeft", "Fade in", n -> new FadeInLeft(n)));
        demos.add(new Demo("FadeInLeftBig", "Fade in", n -> new FadeInLeftBig(n)));
        demos.add(new Demo("FadeInRight", "Fade in", n -> new FadeInRight(n)));
        demos.add(new Demo("FadeInRightBig", "Fade in", n -> new FadeInRightBig(n)));
        demos.add(new Demo("FadeInUp", "Fade in", n -> new FadeInUp(n)));
        demos.add(new Demo("FadeInUpBig", "Fade in", n -> new FadeInUpBig(n)));

        demos.add(new Demo("FadeOut", "Fade out", n -> new FadeOut(n)));
        demos.add(new Demo("FadeOutDown", "Fade out", n -> new FadeOutDown(n)));
        demos.add(new Demo("FadeOutDownBig", "Fade out", n -> new FadeOutDownBig(n)));
        demos.add(new Demo("FadeOutLeft", "Fade out", n -> new FadeOutLeft(n)));
        demos.add(new Demo("FadeOutLeftBig", "Fade out", n -> new FadeOutLeftBig(n)));
        demos.add(new Demo("FadeOutRight", "Fade out", n -> new FadeOutRight(n)));
        demos.add(new Demo("FadeOutRightBig", "Fade out", n -> new FadeOutRightBig(n)));
        demos.add(new Demo("FadeOutUp", "Fade out", n -> new FadeOutUp(n)));
        demos.add(new Demo("FadeOutUpBig", "Fade out", n -> new FadeOutUpBig(n)));

        demos.add(new Demo("Flip", "Flip", n -> new Flip(n)));
        demos.add(new Demo("FlipInX", "Flip", n -> new FlipInX(n)));
        demos.add(new Demo("FlipInY", "Flip", n -> new FlipInY(n)));
        demos.add(new Demo("FlipOutX", "Flip", n -> new FlipOutX(n)));
        demos.add(new Demo("FlipOutY", "Flip", n -> new FlipOutY(n)));

        demos.add(new Demo("LightSpeedIn", "Light speed", n -> new LightSpeedIn(n)));
        demos.add(new Demo("LightSpeedOut", "Light speed", n -> new LightSpeedOut(n)));

        demos.add(new Demo("RotateIn", "Rotate in", n -> new RotateIn(n)));
        demos.add(new Demo("RotateInDownLeft", "Rotate in", n -> new RotateInDownLeft(n)));
        demos.add(new Demo("RotateInDownRight", "Rotate in", n -> new RotateInDownRight(n)));
        demos.add(new Demo("RotateInUpLeft", "Rotate in", n -> new RotateInUpLeft(n)));
        demos.add(new Demo("RotateInUpRight", "Rotate in", n -> new RotateInUpRight(n)));

        demos.add(new Demo("RotateOut", "Rotate out", n -> new RotateOut(n)));
        demos.add(new Demo("RotateOutDownLeft", "Rotate out", n -> new RotateOutDownLeft(n)));
        demos.add(new Demo("RotateOutDownRight", "Rotate out", n -> new RotateOutDownRight(n)));
        demos.add(new Demo("RotateOutUpLeft", "Rotate out", n -> new RotateOutUpLeft(n)));
        demos.add(new Demo("RotateOutUpRight", "Rotate out", n -> new RotateOutUpRight(n)));

        demos.add(new Demo("SlideInDown", "Slide in", n -> new SlideInDown(n)));
        demos.add(new Demo("SlideInLeft", "Slide in", n -> new SlideInLeft(n)));
        demos.add(new Demo("SlideInRight", "Slide in", n -> new SlideInRight(n)));
        demos.add(new Demo("SlideInUp", "Slide in", n -> new SlideInUp(n)));

        demos.add(new Demo("SlideOutDown", "Slide out", n -> new SlideOutDown(n)));
        demos.add(new Demo("SlideOutLeft", "Slide out", n -> new SlideOutLeft(n)));
        demos.add(new Demo("SlideOutRight", "Slide out", n -> new SlideOutRight(n)));
        demos.add(new Demo("SlideOutUp", "Slide out", n -> new SlideOutUp(n)));

        demos.add(new Demo("ZoomIn", "Zoom in", n -> new ZoomIn(n)));
        demos.add(new Demo("ZoomInDown", "Zoom in", n -> new ZoomInDown(n)));
        demos.add(new Demo("ZoomInLeft", "Zoom in", n -> new ZoomInLeft(n)));
        demos.add(new Demo("ZoomInRight", "Zoom in", n -> new ZoomInRight(n)));
        demos.add(new Demo("ZoomInUp", "Zoom in", n -> new ZoomInUp(n)));

        demos.add(new Demo("ZoomOut", "Zoom out", n -> new ZoomOut(n)));
        demos.add(new Demo("ZoomOutDown", "Zoom out", n -> new ZoomOutDown(n)));
        demos.add(new Demo("ZoomOutLeft", "Zoom out", n -> new ZoomOutLeft(n)));
        demos.add(new Demo("ZoomOutRight", "Zoom out", n -> new ZoomOutRight(n)));
        demos.add(new Demo("ZoomOutUp", "Zoom out", n -> new ZoomOutUp(n)));

        demos.add(new Demo("Hinge", "Special", n -> new Hinge(n)));
        demos.add(new Demo("RollIn", "Special", n -> new RollIn(n)));
        demos.add(new Demo("RollOut", "Special", n -> new RollOut(n)));
        demos.add(new Demo("GlowBackground", "Special",
            n -> new GlowBackground((Region) n, Color.web(ACCENT), Color.web("#f59e0b"), 10)));
        demos.add(new Demo("GlowText", "Special",
            n -> new GlowText((Labeled) n, Color.WHITE, Color.web("#f59e0b"))));
        return List.copyOf(demos);
    }

    private static Label card(String text, String color) {
        Label label = new Label(text);
        label.setUserData(text);
        label.setPadding(new Insets(22, 36, 22, 36));
        label.setFont(Font.font(24));
        label.setTextFill(Color.WHITE);
        label.setBackground(new Background(
            new BackgroundFill(Color.web(color), new CornerRadii(14), Insets.EMPTY)));
        return label;
    }

    private void play(Demo demo) {
        stopAll();
        resetTargets();
        AnimationFX fx = tune(demo.factory().apply(target));
        register(fx);
        fx.setOnFinished(e -> {
            finishedCount++;
            setStatus(demo.name() + ": onFinished fired (total " + finishedCount + ")");
            updateState(fx);
        });
        setStatus("Playing " + demo.name() + " [" + demo.category() + "]");
        fx.play();
    }

    private <T extends AnimationFX> T tune(T fx) {
        fx.setSpeed(speed.getValue());
        fx.setDelay(Duration.millis(delay.getValue()));
        fx.setCycleCount(indefinite.isSelected() ? AnimationFX.INDEFINITE : cycles.getValue());
        fx.setResetOnFinished(resetOnFinished.isSelected());
        return fx;
    }

    private void track(AnimationFX fx, String message) {
        register(fx);
        fx.setOnFinished(e -> setStatus(message + " -> finished"));
        setStatus(message);
    }

    private void register(AnimationFX fx) {
        current = fx;
        if (fx.getTimeline() != null) {
            fx.getTimeline().statusProperty().addListener((o, a, b) -> updateState(fx));
        }
        updateState(fx);
    }

    private void stopAll() {
        if (current != null) {
            current.stop();
            current = null;
        }
        if (currentParallel != null) {
            currentParallel.stop();
            currentParallel = null;
        }
        if (currentSequential != null) {
            currentSequential.stop();
            currentSequential = null;
        }
    }

    private void resetTargets() {
        reset(target, targetBackground);
        reset(altTarget, altTargetBackground);
    }

    private static void reset(Label node, Background background) {
        node.setOpacity(1);
        node.setScaleX(1);
        node.setScaleY(1);
        node.setScaleZ(1);
        node.setTranslateX(0);
        node.setTranslateY(0);
        node.setTranslateZ(0);
        node.setRotate(0);
        node.setBackground(background);
        node.setTextFill(Color.WHITE);
        node.setEffect(null);
    }

    private void updateState(AnimationFX fx) {
        if (fx == null) {
            state.setText("no animation");
            return;
        }
        Timeline timeline = fx.getTimeline();
        if (timeline == null) {
            state.setText("node=" + id(fx.getNode()) + " | timeline=null");
            return;
        }
        state.setText("node=" + id(fx.getNode())
            + " | keyFrames=" + timeline.getKeyFrames().size()
            + " | duration=" + millis(timeline.getTotalDuration())
            + " | cycles=" + timeline.getCycleCount()
            + " | rate=" + timeline.getRate()
            + " | " + timeline.getStatus());
    }

    private void setStatus(String text) {
        status.setText(text);
    }

    private void select(Button button) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove(Styles.ACCENT);
        }
        selectedButton = button;
        if (!button.getStyleClass().contains(Styles.ACCENT)) {
            button.getStyleClass().add(Styles.ACCENT);
        }
    }

    private static String id(Node node) {
        if (node == null) {
            return "null";
        }
        Object userData = node.getUserData();
        return userData == null ? node.getClass().getSimpleName() : userData.toString();
    }

    private static String millis(Duration duration) {
        return Math.round(duration.toMillis()) + "ms";
    }

    private static void require(boolean condition, String what) {
        if (!condition) {
            throw new IllegalStateException("check failed: " + what);
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private static void check(StringBuilder sb, int[] pass, int[] fail,
                              String what, CheckedAction action) {
        try {
            action.run();
            pass[0]++;
            sb.append("PASS ").append(what).append('\n');
        } catch (Throwable ex) {
            fail[0]++;
            sb.append("FAIL ").append(what).append(" -> ")
                .append(ex.getClass().getSimpleName()).append(": ")
                .append(ex.getMessage()).append('\n');
        }
    }

    private Tab tab(String title, Feather icon, Node content) {
        Tab tab = new Tab(title, content);
        tab.setGraphic(new FontIcon(icon));
        tab.setClosable(false);
        return tab;
    }

    private ScrollPane scroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(16));
        return scroll;
    }

    private VBox vbox(Node... nodes) {
        VBox box = new VBox(16, nodes);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private HBox hbox(Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox section(String title, Node... nodes) {
        Label heading = new Label(title);
        heading.getStyleClass().add(Styles.TITLE_4);
        VBox body = new VBox(10, heading);
        body.getChildren().addAll(nodes);
        body.setPadding(new Insets(12, 14, 12, 14));
        body.getStyleClass().add(Styles.BG_SUBTLE);
        body.setMaxWidth(Double.MAX_VALUE);
        return body;
    }

    private Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED);
        button.setOnAction(e -> action.run());
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
