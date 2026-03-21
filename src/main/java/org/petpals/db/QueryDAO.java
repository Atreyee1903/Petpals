package org.petpals.db;

import org.petpals.model.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryDAO {

    public boolean addQuery(Query query) {
        String sql = "INSERT INTO support_queries (user_id, query_text, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, query.getUserId());
            pstmt.setString(2, query.getQueryText());
            pstmt.setString(3, "Open"); // Default status

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding query: " + e.getMessage());
            return false;
        }
    }

    public List<Query> getQueriesByUserId(int userId) {
        List<Query> queries = new ArrayList<>();
        // Joining with users table to get username
        String sql = "SELECT q.*, u.username FROM support_queries q JOIN users u ON q.user_id = u.id WHERE q.user_id = ? ORDER BY q.query_timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                queries.add(mapRowToQuery(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching queries by user ID: " + e.getMessage());
        }
        return queries;
    }

    public List<Query> getAllOpenQueries() {
        List<Query> queries = new ArrayList<>();
         // Joining with users table to get username
        String sql = "SELECT q.*, u.username FROM support_queries q JOIN users u ON q.user_id = u.id WHERE q.status = 'Open' ORDER BY q.query_timestamp ASC"; // Oldest first for admins
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                 queries.add(mapRowToQuery(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching open queries: " + e.getMessage());
        }
        return queries;
    }

     public List<Query> getAllQueries() {
        List<Query> queries = new ArrayList<>();
         // Joining with users table to get username
        String sql = "SELECT q.*, u.username FROM support_queries q JOIN users u ON q.user_id = u.id ORDER BY q.query_timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                 queries.add(mapRowToQuery(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all queries: " + e.getMessage());
        }
        return queries;
    }


    public boolean updateQueryReply(int queryId, String adminReply) {
        String sql = "UPDATE support_queries SET admin_reply = ?, reply_timestamp = CURRENT_TIMESTAMP, status = 'Answered' WHERE query_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminReply);
            pstmt.setInt(2, queryId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating query reply: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuery(int queryId) {
        String sql = "DELETE FROM support_queries WHERE query_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, queryId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting query: " + e.getMessage());
            return false;
        }
    }

    private Query mapRowToQuery(ResultSet rs) throws SQLException {
        Query query = new Query();
        query.setQueryId(rs.getInt("query_id"));
        query.setUserId(rs.getInt("user_id"));
        query.setQueryText(rs.getString("query_text"));
        query.setQueryTimestamp(rs.getTimestamp("query_timestamp"));
        query.setAdminReply(rs.getString("admin_reply"));
        query.setReplyTimestamp(rs.getTimestamp("reply_timestamp"));
        query.setStatus(rs.getString("status"));
        query.setUsername(rs.getString("username")); // Set username from join
        return query;
    }
}
