package com.guessit.actualapp.dao;

import com.guessit.actualapp.model.User;
import com.guessit.actualapp.util.DBUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class UserDao {

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static User login(String identifier, String password) {
        // Demo account so the app is usable even without the database.
        if ("user".equalsIgnoreCase(identifier) && "123".equals(password)) {
            return new User(1, "user", "user@example.com", "Demo User", 1, "active");
        }
        String sql = "SELECT user_id, username, email, full_name, role_id, user_status, password_hash "
            + "FROM user_tbl WHERE (email = ? OR username = ?) LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    String status = rs.getString("user_status");
                    if (hash != null && hash.equals(hashPassword(password))
                        && !"banned".equals(status) && !"deleted".equals(status)) {
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("full_name"),
                            rs.getInt("role_id"),
                            status);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean register(String username, String email, String password) {
        String sql = "INSERT INTO user_tbl (username, email, password_hash, user_status, created_at, updated_at) "
            + "VALUES (?, ?, ?, 'active', NOW(), NOW())";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashPassword(password));
            ps.setString(4, "active");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM user_tbl WHERE username = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT 1 FROM user_tbl WHERE email = ? LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
