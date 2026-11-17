package com.guessit.util;

/**
 * Holds the currently logged-in user for this application session.
 * Will be replaced by a proper User model once the database is connected.
 */
public final class Session {

    private static String username;

    private Session() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String value) {
        username = value;
    }

    public static void clear() {
        username = null;
    }
}
