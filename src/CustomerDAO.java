import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {
    private Connection connection;

    public CustomerDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize CustomerDAO", e);
        }
    }

    /**
     * Create a new customer in the database.
     * 
     * @param address The customer's address (can be null)
     * @param tableNumber The customer's table number (can be null)
     * @param toDeliver Whether the order is for delivery
     * @return The generated customer ID if successful, -1 otherwise
     */
    public int createCustomer(String address, Integer tableNumber, boolean toDeliver) {
        String sql = "INSERT INTO Customer (address, table_number, to_deliver) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, address);
            if (tableNumber != null) {
                pstmt.setInt(2, tableNumber);
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setBoolean(3, toDeliver);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error creating customer: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get a customer by their ID.
     * 
     * @param customerId The ID of the customer to retrieve
     * @return The customer if found, null otherwise
     */
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setAddress(rs.getString("address"));
                    customer.setTableNumber(rs.getInt("table_number"));
                    customer.setToDeliver(rs.getBoolean("to_deliver"));
                    return customer;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer: " + e.getMessage());
        }
        return null;
    }
} 