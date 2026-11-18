package Testing_for_bascially_everything;

import Testing_for_bascially_everything.assets.AssetCatalog;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Standalone launcher for the themed {@code SidebarTest.fxml} view.
 *
 * <p>The layout is the MVC split of the old code-built sidebar test: the FXML
 * carries the structure, {@code SidebarTest.css} the theme, the controller the
 * wiring and the model the state.</p>
 *
 * <p>Settings is the gear beside the bell (not a sidebar item) and opens a
 * modal over the whole window.</p>
 *
 * <p>Startup costs are handled the same way as {@code SlidingSidebar_test}:
 * the heap flags are reported so an uncapped JVM cannot quietly reserve 25% of
 * system RAM, the Media runtime is checked for the JavaFX 26 native leak, and
 * every poster is decoded on the loader pool before the first frame.</p>
 */
public class SidebarTestApp extends Application {

    /** The posters the home wall references, in the files' own slug form. */
    private static final List<String> WALL_POSTERS = List.of(
        "blade-runner-2049", "moonlight", "knives-out", "dune-part-two",
        "nightcrawler", "her", "arrival", "parasite", "mad-max", "whiplash",
        "interstellar", "la-la-land", "the-batman");

    private SidebarTestController controller;

    @Override
    public void start(Stage stage) throws Exception {
        warnIfHeapIsUncapped();
        preloadPosterArtwork();

        SidebarTestModel model = new SidebarTestModel();
        controller = new SidebarTestController(model);

        // Same destinations as the code-built sidebar test.
        controller.setPlayAction(() -> Movie_Playing_Demo_Page.openPlay(stage));
        controller.setTutorialAction(() -> Movie_Playing_Demo_Page.openTutorial(stage));

        Parent root = SidebarTestController.load(controller);

        Scene scene = makeScene(root, 1320, 860);
        // While the movie page owns the scene, the sidebar view is detached:
        // suspend its animation and drop its caches so the decoder has room.
        scene.rootProperty().addListener((observable, oldRoot, newRoot) -> {
            boolean active = (newRoot == root);
            controller.setSuspended(!active);
        });
        stage.setTitle("GuessIt - sidebar test (MVC)");
        stage.setScene(scene);
        stage.setMinWidth(Math.min(1080, scene.getWidth()));
        stage.setMinHeight(Math.min(700, scene.getHeight()));
        centerStage(stage);
        stage.show();
    }

    /**
     * Prints the heap flags the app was actually started with, and warns when the
     * project's cap is missing. An uncapped JVM reserves 25% of system RAM
     * (4 GB on a 16 GB machine), which grows the process working set that Task
     * Manager reports even though the demo itself stays in the tens of MB.
     */
    private static void warnIfHeapIsUncapped() {
        Runtime runtime = Runtime.getRuntime();
        long maxHeapMb = runtime.maxMemory() / (1024L * 1024L);
        String heapArgs = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
            .filter(arg -> arg.startsWith("-Xms") || arg.startsWith("-Xmx")
                || arg.startsWith("-XX:MaxDirectMemory"))
            .reduce((left, right) -> left + " " + right)
            .orElse("(none - JVM defaults)");
        System.err.println("[GuessIt] heap flags: " + heapArgs + " | max=" + maxHeapMb + " MB");
        if (maxHeapMb > 1024) {
            System.err.println("[GuessIt] WARNING: no heap cap applied, so the process can grow"
                + " to " + maxHeapMb + " MB. Run the 'SidebarTest (MVC)' configuration"
                + " (-Xms96m -Xmx384m) instead of an auto-generated one.");
        }
        warnIfMediaRuntimeLeaks();
    }

    /**
     * JavaFX 26 (shipped inside some full JDKs) leaks the whole decoded frame set
     * every time a media pipeline restarts: seeking, replaying, or changing scene
     * costs ~165 MB of native memory that is never released, so a few minutes of
     * play push the process to several gigabytes even though the Java heap stays
     * at a few hundred megabytes. The project compiles against JavaFX 21, which
     * stays flat.
     */
    private static void warnIfMediaRuntimeLeaks() {
        String version = System.getProperty("javafx.runtime.version", "unknown");
        System.err.println("[GuessIt] JavaFX runtime: " + version);
        int major = 0;
        int dot = version.indexOf('.');
        try {
            major = Integer.parseInt(dot < 0 ? version : version.substring(0, dot));
        } catch (NumberFormatException ignored) {
            // A non-numeric version is reported above; nothing to compare.
        }
        if (major > 21) {
            System.err.println("[GuessIt] WARNING: JavaFX " + version + " leaks native"
                + " memory on every seek/replay (~165 MB each, up to several GB)."
                + " Project code targets JavaFX 21; run this configuration on a JDK"
                + " without bundled JavaFX (e.g. jdk-24) so the Maven JavaFX 21 jars"
                + " win, or the process will grow to 2-3 GB during play.");
        }
    }

    /** Decodes every poster on the loader pool so the first frame has artwork. */
    private static void preloadPosterArtwork() {
        AssetCatalog.preloadPosters(WALL_POSTERS);
    }

    /** Builds a scene that fits the available screen, scaled for HiDPI displays. */
    static Scene makeScene(Parent content, double maxWidth, double maxHeight) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double scale = Math.max(1.0, Screen.getPrimary().getOutputScaleX());
        double width = Math.min(maxWidth, bounds.getWidth() / scale - 40);
        double height = Math.min(maxHeight, bounds.getHeight() / scale - 40);
        Scene scene = new Scene(content, width, height);
        scene.setFill(Color.web("#000000"));
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

    public static void main(String[] args) {
        launch(args);
    }
}
