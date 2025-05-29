package com.restaurant.roms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    
    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        String query = "SELECT * FROM Customer_Feedback ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(rs.getInt("feedback_id"));
                feedback.setName(rs.getString("customer_name"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setComment(rs.getString("comment"));
                feedback.setSubmissionDate(rs.getTimestamp("created_at"));
                feedbackList.add(feedback);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }
    
    public boolean deleteFeedback(int id) {
        String query = "DELETE FROM Customer_Feedback WHERE feedback_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addFeedback(Feedback feedback) {
        String query = "INSERT INTO Customer_Feedback (customer_name, rating, comment, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, feedback.getName());
            stmt.setInt(2, feedback.getRating());
            stmt.setString(3, feedback.getComment());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}