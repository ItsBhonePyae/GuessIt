package com.guessit.actualapp.dao;

import com.guessit.actualapp.model.User;
import com.guessit.actualapp.util.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {
    public static User login(String identifier, String password) {
        String sql = "SELECT user_id, username, email, password_hash, user_status FROM user_tbl WHERE (email = ? OR username = ?) LIMIT 1";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    String status = rs.getString("user_status");
                    if (hash != null && BCrypt.checkpw(password, hash) && !"banned".equals(status) && !"deleted".equals(status)) {
                        return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("email"));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean register(String username, String email, String password) {
        String sql = "INSERT INTO user_tbl (username, email, password_hash, user_status, created_at, updated_at) VALUES (?,?,?,?,NOW(),NOW())";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt(12)));
            ps.setString(4, "active");
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
