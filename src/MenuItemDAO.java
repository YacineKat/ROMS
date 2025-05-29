package com.restaurant.roms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

/**
 * Data Access Object (DAO) for the MenuItem entity.
 * Handles database operations related to menu items.
 */
public class MenuItemDAO {

    private Connection connection;

    public MenuItemDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a new menu item into the database.
     * 
     * @param item The menu item to insert
     * @return The generated item ID if successful, -1 otherwise
     */
    public int insertMenuItem(MenuItem item) throws SQLException {
        String query = "INSERT INTO MenuItem (title, price, category_title, image_path, kitchen_id, quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getCategory());
            stmt.setString(4, item.getImagePath());
            stmt.setInt(5, item.getKitchenId());
            stmt.setInt(6, item.getQuantity());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating menu item failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating menu item failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Update an existing menu item in the database.
     * 
     * @param item The menu item to update
     * @return true if successful, false otherwise
     */
    public boolean updateMenuItem(MenuItem item) throws SQLException {
        String query = "UPDATE MenuItem SET title = ?, price = ?, category_title = ?, image_path = ?, kitchen_id = ? WHERE item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getCategory());
            stmt.setString(4, item.getImagePath());
            stmt.setInt(5, item.getKitchenId());
            stmt.setInt(6, item.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a menu item from the database.
     * 
     * @param itemId The ID of the menu item to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMenuItem(int itemId) throws SQLException {
        String query = "DELETE FROM MenuItem WHERE item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get a menu item by its ID.
     * 
     * @param itemId The ID of the menu item to retrieve
     * @return The menu item if found, null otherwise
     */
    public MenuItem getMenuItemById(int itemId) {
        String sql = "SELECT * FROM MenuItem WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    MenuItem item = new MenuItem();
                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("title"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCategory(rs.getString("category_title"));
                    item.setImagePath(rs.getString("image_path"));
                    item.setKitchenId(rs.getInt("kitchen_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu item: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all menu items from the database.
     * 
     * @return A list of all menu items
     */
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem ORDER BY item_id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("item_id"));
                item.setName(rs.getString("title"));
                item.setTitle(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setCategory(rs.getString("category_title"));
                item.setCategoryTitle(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                item.setQuantity(rs.getInt("quantity"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving menu items: " + e.getMessage());
            throw e;
        }
        return items;
    }

    /**
     * Get all menu items in a specific category.
     * 
     * @param category The category to filter by
     * @return A list of menu items in the specified category
     */
    public List<MenuItem> getMenuItemsByCategory(String categoryTitle) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem WHERE category_title = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryTitle);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("item_id"));
                item.setName(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setCategory(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Get a list of all categories from the database.
     * 
     * @return A list of all categories
     */
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT title FROM Category";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                categories.add(rs.getString("title"));
            }
        }
        return categories;
    }

    public void addMenuItem(MenuItem item) {
        String query = "INSERT INTO MenuItem (title, price, quantity, category_title, image_path, kitchen_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getImagePath());
            pstmt.setInt(6, item.getKitchenId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MenuItem> getMenuItemsByKitchen(int kitchenId) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItem WHERE kitchen_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, kitchenId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("item_id"));
                item.setName(rs.getString("title"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCategory(rs.getString("category_title"));
                item.setImagePath(rs.getString("image_path"));
                item.setKitchenId(rs.getInt("kitchen_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}