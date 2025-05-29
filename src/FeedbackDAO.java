package com.restaurant.roms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    private Connection connection;

    public FeedbackDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Feedback> getAllFeedback() throws SQLException {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback ORDER BY submission_date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(rs.getInt("id"));
                feedback.setCustomerName(rs.getString("name"));
                feedback.setPhone(rs.getString("phone"));
                feedback.setComment(rs.getString("comment"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setDate(rs.getString("submission_date"));
                feedbacks.add(feedback);
            }
        }
        return feedbacks;
    }

    public boolean deleteFeedback(int feedbackId) throws SQLException {
        String query = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, feedbackId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void addFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (name, phone, comment, rating, submission_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, feedback.getCustomerName());
            pstmt.setString(2, ""); // Since phone is required in DB but not in our model
            pstmt.setString(3, feedback.getComment());
            pstmt.setInt(4, feedback.getRating());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding feedback: " + e.getMessage());
            throw e;
        }
    }
}