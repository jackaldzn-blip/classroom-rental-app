package classroomrental.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens JDBC connections without storing credentials in source code. */
public final class DatabaseConnection {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection(String url, String username, String password) {
        if (url == null || !url.startsWith("jdbc:mysql://")
                || url.length() == "jdbc:mysql://".length()) {
            throw new IllegalArgumentException("Set DB_URL to a MySQL JDBC URL.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Set DB_USER to your MySQL username.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Set DB_PASSWORD before connecting.");
        }
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConnection fromEnvironment() {
        return new DatabaseConnection(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD"));
    }

    /** The caller must close the returned connection. */
    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
