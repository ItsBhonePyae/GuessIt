package com.guessit.actualapp;

import javafx.application.Application;

/**
 * Plain Java entry point.
 *
 * <p>Running a class that extends {@link Application} directly makes the JDK
 * launcher demand the JavaFX modules on {@code --module-path}, which is why
 * "JavaFX runtime components are missing" appears when {@code Main} is run as a
 * normal application. Starting JavaFX from this class instead lets the jars sit
 * on the ordinary classpath, so a plain "Run" works.</p>
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(com.guessit.actualapp.Main.class, args);
    }
}
