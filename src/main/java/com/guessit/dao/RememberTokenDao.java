package com.guessit.dao;

import com.guessit.model.RememberToken;
import com.guessit.util.DBUtil;
import com.guessit.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the {@code remember_tokens} table.
 *
 * Schema (see resources/db/init.sql):
 *   remember_tokens(id, user_id, token_hash, expires_at, created_at)
 */
public class RememberTokenDao {

    /** Stores a new (hashed) token with a 30-day expiry. */
    public void create(int userId, String rawToken) throws SQLException {
        // remove any previous tokens for this user first
        deleteByUserId(userId);

        String sql = "INSERT INTO remember_tokens (user_id, token_hash, expires_at, created_at) "
                + "VALUES (?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, PasswordUtil.hash(rawToken));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(30)));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    /** Returns the token row for the user if a non-expired matching hash exists. */
    public RememberToken findValid(int userId, String rawToken) throws SQLException {
        String sql = "SELECT id, user_id, token_hash, expires_at, created_at "
                + "FROM remember_tokens WHERE user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String hash = rs.getString("token_hash");
                    Timestamp exp = rs.getTimestamp("expires_at");
                    if (exp != null && exp.toLocalDateTime().isAfter(LocalDateTime.now())
                            && PasswordUtil.matches(rawToken, hash)) {
                        RememberToken t = new RememberToken();
                        t.setId(rs.getInt("id"));
                        t.setUserId(rs.getInt("user_id"));
                        t.setTokenHash(hash);
                        t.setExpiresAt(exp.toLocalDateTime());
                        Timestamp created = rs.getTimestamp("created_at");
                        if (created != null) {
                            t.setCreatedAt(created.toLocalDateTime());
                        }
                        return t;
                    }
                }
            }
        }
        return null;
    }

    public void deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM remember_tokens WHERE user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void deleteExpired() throws SQLException {
        String sql = "DELETE FROM remember_tokens WHERE expires_at < ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    @SuppressWarnings("unused")
    private List<RememberToken> unused() {
        return new ArrayList<>();
    }
}
