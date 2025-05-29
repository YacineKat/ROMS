package com.restaurant.roms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database connection utility class for the Restaurant Operations Management
 * System.
 * Provides methods to connect to and disconnect from the MySQL database.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static Connection connection = null;
    private static int connectionAttempts = 0;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    /**
     * Get a connection to the database.
     * If a connection already exists and is valid, returns the existing connection.
     * Otherwise, creates a new connection.
     * 
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASS);
            props.setProperty("useSSL", "false");
            props.setProperty("autoReconnect", "true");
            props.setProperty("maxReconnects", "3");
            
            // Attempt to establish connection
            connection = DriverManager.getConnection(DB_URL, props);
            connectionAttempts = 0; // Reset attempts on successful connection
            LOGGER.info("Database connection established successfully");
            return connection;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            connectionAttempts++;
            LOGGER.log(Level.WARNING, "Failed to connect to database (Attempt " + connectionAttempts + ")", e);
            
            if (connectionAttempts < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                    return getConnection(); // Retry connection
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Connection retry interrupted", ie);
                }
            }
            throw new SQLException("Failed to connect to database after " + MAX_RETRIES + " attempts", e);
        }
    }

    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Test the database connection.
     * 
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn != null && !conn.isClosed();
            if (isValid) {
                LOGGER.info("Database connection test successful");
            }
            return isValid;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }

    // Add a method to check if the database is accessible
    public static boolean isDatabaseAccessible() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database is not accessible", e);
            return false;
        }
    }
}