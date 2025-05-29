package com.restaurant.roms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public User authenticate(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getInt("created_by")
                );
                user.setSalt(rs.getString("salt"));
                
                // Verify password
                if (user.verifyPassword(password)) {
                    // Update last login
                    user.updateLastLogin();
                    
                    // Load user profile
                    user.setProfile(getUserProfile(user.getId()));
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Error authenticating user: " + username, e);
        }
        return null;
    }

    public boolean addUser(User user, String password) {
        String query = "INSERT INTO Users (username, password, salt, role, created_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getCreatedBy());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    // Add user profile if exists
                    if (user.getProfile() != null) {
                        addUserProfile(user.getId(), user.getProfile());
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Error adding user: " + user.getUsername(), e);
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        String query = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user: " + userId, e);
        }
        return false;
    }

    public boolean updateUser(int userId, String username, String password) {
        StringBuilder query = new StringBuilder("UPDATE Users SET username = ?");
        if (password != null && !password.isEmpty()) {
            query.append(", password = ?, salt = ?");
        }
        query.append(" WHERE user_id = ?");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            stmt.setString(1, username);
            int paramIndex = 2;
            if (password != null && !password.isEmpty()) {
                User tempUser = new User(0, username, password, "", 0);
                stmt.setString(paramIndex++, tempUser.getPassword());
                stmt.setString(paramIndex++, tempUser.getSalt());
            }
            stmt.setInt(paramIndex, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user: " + userId, e);
        }
        return false;
    }

    public List<User> getAllManagers() {
        List<User> managers = new ArrayList<>();
        String query = "SELECT * FROM Users WHERE role = 'manager'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getInt("created_by")
                );
                user.setSalt(rs.getString("salt"));
                user.setProfile(getUserProfile(user.getId()));
                managers.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error getting all managers", e);
        }
        return managers;
    }

    public boolean updateUserProfile(int userId, UserProfile profile) {
        String query = "UPDATE User_Profiles SET full_name = ?, email = ?, phone = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, profile.getFullName());
            stmt.setString(2, profile.getEmail());
            stmt.setString(3, profile.getPhone());
            stmt.setInt(4, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user profile: " + userId, e);
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        String query = "UPDATE Users SET password = ?, salt = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            User tempUser = new User(0, "", newPassword, "", 0);
            stmt.setString(1, tempUser.getPassword());
            stmt.setString(2, tempUser.getSalt());
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user: " + userId, e);
        }
        return false;
    }

    public boolean hasPermission(int userId, String permission) {
        String query = "SELECT COUNT(*) FROM Role_Permissions rp " +
                      "JOIN Users u ON u.role = rp.role " +
                      "WHERE u.user_id = ? AND rp.permission = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, permission);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking permission for user: " + userId, e);
        }
        return false;
    }

    public void updateLastLogin(User user) {
        String query = "UPDATE Users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating last login for user: " + user.getId(), e);
        }
    }

    public UserProfile getUserProfile(int userId) {
        String query = "SELECT * FROM User_Profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserProfile(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            logger.error("Error getting user profile: " + userId, e);
        }
        return null;
    }

    public boolean addUserProfile(int userId, UserProfile profile) {
        String query = "INSERT INTO User_Profiles (user_id, full_name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, profile.getFullName());
            stmt.setString(3, profile.getEmail());
            stmt.setString(4, profile.getPhone());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding user profile: " + userId, e);
        }
        return false;
    }

    public boolean addManager(User manager, int createdBy) {
        String sql = "INSERT INTO Users (username, password, salt, role, created_by) VALUES (?, ?, ?, 'manager', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, manager.getUsername());
            pstmt.setString(2, manager.getPassword());
            pstmt.setString(3, manager.getSalt());
            pstmt.setInt(4, createdBy);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding manager: " + manager.getUsername(), e);
            return false;
        }
    }

    public boolean deleteManager(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ? AND role = 'manager'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting manager: " + userId, e);
            return false;
        }
    }
} 