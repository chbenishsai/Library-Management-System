package com.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton utility responsible for supplying JDBC {@link Connection}
 * instances to the repository layer.
 *
 * <p>Design notes:
 * <ul>
 *     <li>Uses the classic lazy-initialization Singleton pattern with
 *     double-checked locking so it is safe under concurrent access.</li>
 *     <li>Reads connection settings from {@code db.properties} on the
 *     classpath, falling back to sensible local defaults if the file
 *     is not present.</li>
 *     <li>Does NOT hold a single shared {@link Connection} for the whole
 *     application's lifetime. Instead, {@link #getConnection()} hands out
 *     a fresh connection per call, and callers are expected to close it
 *     (ideally via try-with-resources). This avoids issues with
 *     concurrent statements on a single shared connection, while the
 *     class itself remains a Singleton that centralizes configuration
 *     and driver bootstrap.</li>
 * </ul>
 */
public final class DatabaseConnectionManager {

    private static volatile DatabaseConnectionManager instance;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnectionManager() {
        Properties props = loadProperties();
        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC");
        this.username = props.getProperty("db.username", "root");
        this.password = props.getProperty("db.password", "root");

        try {
            // Explicit driver load; modern JDBC 4+ drivers auto-register via
            // SPI, but doing this explicitly keeps the class portable across
            // older runtime configurations and makes failures easier to diagnose.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL JDBC Driver not found on classpath. "
                            + "Ensure mysql-connector-j is included as a dependency.", e);
        }
    }

    /**
     * Returns the single shared instance, creating it on first use.
     */
    public static DatabaseConnectionManager getInstance() {
        DatabaseConnectionManager result = instance;
        if (result == null) {
            synchronized (DatabaseConnectionManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseConnectionManager();
                }
            }
        }
        return result;
    }

    /**
     * Opens and returns a brand-new JDBC connection. Callers MUST close
     * this connection themselves, preferably via try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnectionManager.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            // Fall back silently to defaults; the connection attempt itself
            // will surface a clear error if configuration is genuinely bad.
            System.err.println("Warning: could not load db.properties, using defaults. " + e.getMessage());
        }
        return props;
    }
}
