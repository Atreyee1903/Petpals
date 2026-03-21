package org.petpals.db;

import org.petpals.model.Testimonial;
import org.petpals.model.Testimonial.Status; // Import the enum

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestimonialDAO {

    // Helper method to map a ResultSet row to a Testimonial object
    private Testimonial mapRowToTestimonial(ResultSet rs) throws SQLException {
        return new Testimonial(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getString("image"),
                rs.getString("petName"),
                rs.getString("text"),
                rs.getInt("rating"), // Use getInt, handle potential NULL if rating is optional and not defaulted
                Status.fromString(rs.getString("status")), // Convert string status to enum
                rs.getTimestamp("submitted_at")
        );
    }

    // Fetch only APPROVED testimonials (for public display)
    public List<Testimonial> getAllApprovedTestimonials() {
        List<Testimonial> testimonials = new ArrayList<>();
        // Select only approved testimonials
        String sql = "SELECT * FROM testimonials WHERE status = 'approved' ORDER BY submitted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                testimonials.add(mapRowToTestimonial(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching approved testimonials: " + e.getMessage());
            e.printStackTrace(); // Consider more robust logging
        }
        return testimonials;
    }

    // Fetch PENDING testimonials (for admin review)
    public List<Testimonial> getPendingTestimonials() {
        List<Testimonial> testimonials = new ArrayList<>();
        // Select only pending testimonials
        String sql = "SELECT * FROM testimonials WHERE status = 'pending' ORDER BY submitted_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                testimonials.add(mapRowToTestimonial(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending testimonials: " + e.getMessage());
            e.printStackTrace();
        }
        return testimonials;
    }

     // Fetch ALL testimonials (potentially useful for a comprehensive admin view)
    public List<Testimonial> getAllTestimonials() {
        List<Testimonial> testimonials = new ArrayList<>();
        String sql = "SELECT * FROM testimonials ORDER BY submitted_at DESC"; // Order by submission time

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                testimonials.add(mapRowToTestimonial(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all testimonials: " + e.getMessage());
            e.printStackTrace();
        }
        return testimonials;
    }


    // Add a new testimonial (status defaults to 'pending' in DB)
    public boolean addTestimonial(Testimonial testimonial) {
        // Note: The status and submitted_at are handled by DB defaults
        String sql = "INSERT INTO testimonials (user_id, name, location, image, petName, text, rating) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, testimonial.getUserId());
            pstmt.setString(2, testimonial.getName());
            pstmt.setString(3, testimonial.getLocation());
            pstmt.setString(4, testimonial.getImage());
            pstmt.setString(5, testimonial.getPetName());
            pstmt.setString(6, testimonial.getText());
            // Handle potential 0 rating if not provided or optional
            if (testimonial.getRating() > 0 && testimonial.getRating() <= 5) {
                pstmt.setInt(7, testimonial.getRating());
            } else {
                pstmt.setNull(7, Types.INTEGER); // Set SQL NULL if rating is invalid/not provided
            }


            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding testimonial: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update the status of a testimonial
    public boolean updateTestimonialStatus(int testimonialId, Status newStatus) {
        String sql = "UPDATE testimonials SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.name().toLowerCase()); // Store status as lowercase string in DB
            pstmt.setInt(2, testimonialId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating testimonial status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

     // Delete a testimonial (useful for admin cleanup)
    public boolean deleteTestimonial(int testimonialId) {
        String sql = "DELETE FROM testimonials WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, testimonialId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting testimonial: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
