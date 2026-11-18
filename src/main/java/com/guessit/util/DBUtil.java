package com.guessit.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place for obtaining a JDBC connection to the MySQL "guessit" database.
 *
 * Settings are read from /guessit.properties on the classpath (see the template
 * in src/main/resources). If the file is missing, sensible defaults are used.
 */
public final class DBUtil {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBUtil.class.getResourceAsStream("/guessit.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("Could not load guessit.properties: " + e.getMessage());
        }
        PROPS.putIfAbsent("db.url",
                "jdbc:mysql://localhost:3306/guessit?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
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

    /** Quick probe used by controllers to decide between real auth and demo mode. */
    public static boolean isAvailable() {
        try (Connection c = getConnection()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
