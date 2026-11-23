package com.guessit.actualapp.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBUtil.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties: " + e.getMessage());
        }
        PROPS.putIfAbsent("db.url",
            "jdbc:mysql://localhost:3306/java_project_guessit?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        PROPS.putIfAbsent("db.user", "root");
        PROPS.putIfAbsent("db.password", "");
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            PROPS.getProperty("db.url"),
            PROPS.getProperty("db.user"),
            PROPS.getProperty("db.password"));
    }

    public static boolean isAvailable() {
        try (Connection c = getConnection()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
