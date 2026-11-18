package com.guessit.util;

import java.util.UUID;

/**
 * Generates opaque, high-entropy tokens for the "remember me" feature.
 * The raw token is handed to the client; only its BCrypt hash is persisted.
 */
public final class TokenGenerator {

    private TokenGenerator() {
    }

    public static String newToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    public static String hash(String token) {
        return PasswordUtil.hash(token);
    }
}
