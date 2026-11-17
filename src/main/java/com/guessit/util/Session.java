package com.guessit.util;

/**
 * Holds the current authentication flow state for this application session.
 * Will be replaced by a proper User model once the database is connected.
 */
public final class Session {

    private static String username;
    private static String email;
    private static String role;

    private Session() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String value) {
        username = value;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String value) {
        email = value;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String value) {
        role = value;
    }

    public static void clear() {
        username = null;
        email = null;
        role = null;
    }
}
