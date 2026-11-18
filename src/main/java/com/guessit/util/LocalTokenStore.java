package com.guessit.util;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;

/**
 * Stores the "remember me" token in the OS keyring (Windows Credential Manager
 * on Windows) so it never sits in a plain-text file.
 *
 * The value is stored as "username::token" under a single well-known account.
 */
public final class LocalTokenStore {

    private static final String SERVICE = "GuessIt";
    private static final String ACCOUNT = "remembered";

    private LocalTokenStore() {
    }

    public static void save(String username, String token) {
        try {
            Keyring.create().setPassword(SERVICE, ACCOUNT, username + "::" + token);
        } catch (Exception e) {
            System.err.println("Could not store remember-me token: " + e.getMessage());
        }
    }

    /** Returns "username::token" or null if nothing is stored. */
    public static String load() {
        try {
            return Keyring.create().getPassword(SERVICE, ACCOUNT);
        } catch (Exception e) {
            return null;
        }
    }

    public static void delete() {
        try {
            Keyring.create().deletePassword(SERVICE, ACCOUNT);
        } catch (Exception ignored) {
            // nothing to delete
        }
    }
}
