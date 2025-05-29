package com.restaurant.roms;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class PermissionManager {
    private Connection connection;
    private static PermissionManager instance;
    private Set<String> adminPermissions;
    private Set<String> managerPermissions;

    private PermissionManager() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadPermissions();
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    private void loadPermissions() {
        adminPermissions = new HashSet<>();
        managerPermissions = new HashSet<>();

        String query = "SELECT rp.role, p.name FROM Role_Permissions rp " +
                      "JOIN Permissions p ON rp.permission_id = p.permission_id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String role = rs.getString("role");
                String permission = rs.getString("name");
                
                if ("admin".equals(role)) {
                    adminPermissions.add(permission);
                } else if ("manager".equals(role)) {
                    managerPermissions.add(permission);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermission(User user, String permission) {
        if (user == null || permission == null) {
            return false;
        }

        if (user.isAdmin()) {
            return adminPermissions.contains(permission);
        } else if (user.isManager()) {
            return managerPermissions.contains(permission);
        }

        return false;
    }

    public boolean canAddManager(User user) {
        return hasPermission(user, "add_manager");
    }

    public boolean canDeleteManager(User user) {
        return hasPermission(user, "delete_manager");
    }

    public boolean canAddItem(User user) {
        return hasPermission(user, "add_item");
    }

    public boolean canDeleteItem(User user) {
        return hasPermission(user, "delete_item");
    }

    public boolean canDeleteComment(User user) {
        return hasPermission(user, "delete_comment");
    }

    public boolean canEditProfile(User user) {
        return hasPermission(user, "edit_profile");
    }
} 