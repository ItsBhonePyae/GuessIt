package com.guessit.actualapp.model;

public final class Session {
    public static User currentUser;

    private Session() {
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
