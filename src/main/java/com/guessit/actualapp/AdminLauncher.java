package com.guessit.actualapp;

import javafx.application.Application;

/** Plain Java entry point for the standalone admin workspace. */
public final class AdminLauncher {

    private AdminLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(AdminApplication.class, args);
    }
}
