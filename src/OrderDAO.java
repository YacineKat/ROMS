package com.restaurant.roms;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the Order entity.
 * Handles database operations related to orders.
 */
public class OrderDAO {
    private Connection connection;

    public OrderDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a new order into the database.
     * 
     * @param order The order to insert
     * @return The generated order ID if successful, -1 otherwise
     */
    public int insertOrder(Order order) {
        String sql = "INSERT INTO `Order` (status, date, customer_id, kitchen_id, notes) VALUES (?, ?, ?, ?, ?)";

        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Insert the order first
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, order.getStatus().name());
                pstmt.setString(2, order.getDate());
                pstmt.setInt(3, order.getCustomerId());

                // Kitchen ID is optional
                if (order.getKitchenId() > 0) {
                    pstmt.setInt(4, order.getKitchenId());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }

                // Notes
                pstmt.setString(5, order.getNotes());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);

                            // Now insert the order items
                            if (insertOrderItems(connection, orderId, order.getItems())) {
                                connection.commit(); // Commit transaction
                                return orderId;
                            } else {
                                connection.rollback(); // Rollback on failure
                                return -1;
                            }
                        }
                    }
                }
                connection.rollback(); // Rollback on failure
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order: " + e.getMessage());
            try {
                if (connection != null)
                    connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Insert order items for an order.
     * 
     * @param conn    The database connection
     * @param orderId The order ID to associate items with
     * @param items   The list of items to insert
     * @return true if successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean insertOrderItems(Connection conn, int orderId, List<Order.OrderItem> items) throws SQLException {
        String sql = "INSERT INTO Order_Items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Order.OrderItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Update an order's status.
     * 
     * @param orderId The ID of the order to update
     * @param status  The new status
     * @return true if successful, false otherwise
     */
    public boolean updateOrderStatus(int orderId, Order.OrderStatus status) {
        String sql = "UPDATE `Order` SET Status = ? WHERE OrderID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get an order by its ID.
     * 
     * @param orderId The ID of the order to retrieve
     * @return The order if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `Order` WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                    order.setDate(rs.getString("date"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStaffId(rs.getString("staff_id"));
                    order.setKitchenId(rs.getInt("kitchen_id"));
                    order.setManagerId(rs.getString("manager_id"));
                    order.setNotes(rs.getString("notes"));

                    order.setItems(getOrderItems(conn, orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get items for a specific order.
     * 
     * @param conn    The database connection
     * @param orderId The order ID to get items for
     * @return A list of menu items in the order
     * @throws SQLException if a database error occurs
     */
    private List<Order.OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT m.item_id, m.title, m.price, m.category_title, m.image_path, m.kitchen_id, oi.quantity " +
                "FROM Order_Items oi " +
                "JOIN MenuItem m ON oi.item_id = m.item_id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Order.OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    MenuItem menuItem = new MenuItem(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            rs.getDouble("price"),
                            rs.getString("category_title"),
                            rs.getString("image_path"),
                            rs.getInt("kitchen_id"));
                    items.add(new Order.OrderItem(menuItem, rs.getInt("quantity")));
                }
                return items;
            }
        }
    }

    /**
     * Get all orders from the database.
     * 
     * @return A list of all orders
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order`";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Order order = new Order();
                order.setOrderId(orderId);
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getString("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setNotes(rs.getString("notes"));

                // Kitchen ID is optional
                if (rs.getObject("kitchen_id") != null) {
                    order.setKitchenId(rs.getInt("kitchen_id"));
                }

                // Get order items
                order.setItems(getOrderItems(connection, orderId));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
        }
        return orders;
    }

    /**
     * Get orders with a specific status.
     * 
     * @param status The status to filter by
     * @return A list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `Order` WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getString("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setKitchenId(rs.getInt("kitchen_id"));
                order.setNotes(rs.getString("notes"));
                
                // Load order items
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }

    private void loadOrderItems(Order order) throws SQLException {
        String query = "SELECT oi.*, mi.* FROM Order_Items oi " +
                      "JOIN MenuItem mi ON oi.item_id = mi.item_id " +
                      "WHERE oi.order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, order.getOrderId());
            ResultSet rs = stmt.executeQuery();
            
            List<Order.OrderItem> items = new ArrayList<>();
            while (rs.next()) {
                MenuItem menuItem = new MenuItem();
                menuItem.setItemId(rs.getInt("item_id"));
                menuItem.setName(rs.getString("title"));
                menuItem.setPrice(rs.getDouble("price"));
                menuItem.setCategory(rs.getString("category_title"));
                menuItem.setImagePath(rs.getString("image_path"));
                menuItem.setKitchenId(rs.getInt("kitchen_id"));
                
                Order.OrderItem orderItem = new Order.OrderItem(menuItem, rs.getInt("quantity"));
                items.add(orderItem);
            }
            order.setItems(items);
        }
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO `Order` (status, date, customer_id, staff_id, kitchen_id, manager_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            pstmt.setString(1, order.getStatus().name());
            pstmt.setString(2, order.getDate());
            pstmt.setInt(3, order.getCustomerId());
            pstmt.setString(4, order.getStaffId());
            pstmt.setInt(5, order.getKitchenId());
            pstmt.setString(6, order.getManagerId());
            pstmt.setString(7, order.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId);

                    if (insertOrderItemsBatch(conn, orderId, order.getItems())) {
                        conn.commit();
                        return orderId;
                    } else {
                        conn.rollback();
                        return -1;
                    }
                } else {
                    conn.rollback();
                    return -1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return -1;
        }
    }

    private boolean insertOrderItemsBatch(Connection conn, int orderId, List<Order.OrderItem> items)
            throws SQLException {
        String sql = "INSERT INTO Order_Items (order_id, item_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Order.OrderItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `Order` WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getString("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setItems(getOrderItems(connection, order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByStaff(String staffId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM `Order` WHERE staff_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
                order.setDate(rs.getString("date"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStaffId(rs.getString("staff_id"));
                order.setItems(getOrderItems(connection, order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}