package com.guessit.actualapp;

import atlantafx.base.theme.PrimerDark;
import com.guessit.actualapp.view.MainMenuGuestController;
import com.guessit.actualapp.view.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        warnIfHeapIsUncapped();
        ensureSupportedMediaRuntime();
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        Navigator.setStage(stage);
        stage.setTitle("GuessIt");
        stage.setMinWidth(1080);
        stage.setMinHeight(700);
        Navigator.go(MainMenuGuestController.load());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void warnIfHeapIsUncapped() {
        long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
        String heapArgs = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
            .filter(arg -> arg.startsWith("-Xms") || arg.startsWith("-Xmx")
                || arg.startsWith("-XX:MaxDirectMemory"))
            .reduce((left, right) -> left + " " + right)
            .orElse("(none - JVM defaults)");
        System.err.println("[GuessIt] heap flags: " + heapArgs + " | max=" + maxHeapMb + " MB");
        if (maxHeapMb > 1024) {
            System.err.println("[GuessIt] WARNING: no heap cap applied. Run the 'GuessIt'"
                + " configuration (-Xms96m -Xmx384m) with JDK 24 instead of an auto-generated"
                + " run configuration.");
        }
    }

    private static void ensureSupportedMediaRuntime() {
        String version = System.getProperty("javafx.runtime.version", "unknown");
        System.err.println("[GuessIt] JavaFX runtime: " + version);
        int dot = version.indexOf('.');
        try {
            int major = Integer.parseInt(dot < 0 ? version : version.substring(0, dot));
            if (major > 21) {
                throw new IllegalStateException("Unsupported JavaFX runtime " + version
                    + ". GuessIt targets JavaFX 21; newer runtimes can leak native video"
                    + " memory. Launch with the 'GuessIt' run configuration and JDK 24.");
            }
        } catch (NumberFormatException ignored) {
            // Some JavaFX builds do not expose a numeric version string.
        }
    }
}
