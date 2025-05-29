package com.restaurant.roms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.restaurant.roms.DatabaseConnection;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getConnection();
        }
    }
    
    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin"),
                        rs.getBoolean("is_manager")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<User> getAllManagers() {
        List<User> managers = new ArrayList<>();
        String query = "SELECT * FROM users WHERE is_manager = true ORDER BY name";
        try {
            ensureConnection();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    User manager = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin"),
                        rs.getBoolean("is_manager")
                    );
                    managers.add(manager);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting managers: " + e.getMessage());
            e.printStackTrace();
        }
        return managers;
    }
    
    public void addManager(User manager) throws SQLException {
        String query = "INSERT INTO users (username, password, name, email, is_admin, is_manager) VALUES (?, ?, ?, ?, false, true)";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, manager.getUsername());
                stmt.setString(2, manager.getPassword());
                stmt.setString(3, manager.getName());
                stmt.setString(4, manager.getEmail());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating manager failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        manager.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating manager failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding manager: " + e.getMessage());
            throw e;
        }
    }
    
    public void deleteManager(int managerId) throws SQLException {
        String query = "DELETE FROM users WHERE id = ? AND is_manager = true";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, managerId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Deleting manager failed, no rows affected.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting manager: " + e.getMessage());
            throw e;
        }
    }
    
    public void updateManager(User manager) throws SQLException {
        String query = "UPDATE users SET username = ?, password = ?, name = ?, email = ? WHERE id = ? AND is_manager = true";
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, manager.getUsername());
                stmt.setString(2, manager.getPassword());
                stmt.setString(3, manager.getName());
                stmt.setString(4, manager.getEmail());
                stmt.setInt(5, manager.getId());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating manager failed, no rows affected.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating manager: " + e.getMessage());
            throw e;
        }
    }
} 