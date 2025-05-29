package com.restaurant.roms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection utility class for the Restaurant Operations Management System.
 * Provides methods to connect to and disconnect from the MySQL database using HikariCP connection pool.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static final String CONFIG_FILE = "/database.properties";

    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void initializeDataSource() throws IOException {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new IOException("Cannot find " + CONFIG_FILE);
            }
            props.load(is);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/restaurant_db"));
        config.setUsername(props.getProperty("db.user", "root"));
        config.setPassword(props.getProperty("db.password", "root"));
        
        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "300000")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "20000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1200000")));
        
        // Connection test settings
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        // Additional MySQL settings
        config.addDataSourceProperty("cachePrepStmts", props.getProperty("db.mysql.cachePrepStmts", "true"));
        config.addDataSourceProperty("prepStmtCacheSize", props.getProperty("db.mysql.prepStmtCacheSize", "250"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", props.getProperty("db.mysql.prepStmtCacheSqlLimit", "2048"));
        config.addDataSourceProperty("useServerPrepStmts", props.getProperty("db.mysql.useServerPrepStmts", "true"));
        
        dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized successfully");
    }

    /**
     * Get a connection from the connection pool.
     * 
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to get connection from pool");
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Error getting database connection", e);
            throw e;
        }
    }

    /**
     * Close the database connection pool.
     * Only call this when the application is shutting down.
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                logger.info("Database connection pool closed");
            } catch (Exception e) {
                logger.error("Error closing database connection pool", e);
            }
        }
    }

    /**
     * Test the database connection.
     * 
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }

    /**
     * Get the current pool statistics.
     * 
     * @return String containing pool statistics
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Active: %d, Idle: %d, Total: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections()
            );
        }
        return "Pool not initialized";
    }
}