package org.petpals.db;

import org.petpals.model.CartItem;
import org.petpals.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAO {

    /**
     * Get all cart items for the specified user
     *
     * @param userId The ID of the user whose cart items to retrieve
     * @return A list of cart items
     */
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, p.* FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("p.id"),
                        rs.getString("p.name"),
                        rs.getDouble("p.price"),
                        rs.getString("p.image"),
                        rs.getString("p.category")
                    );

                    CartItem item = new CartItem(product, rs.getInt("ci.quantity"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving cart items for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Save all cart items for a user
     *
     * @param userId The ID of the user whose cart to save
     * @param items The list of cart items to save
     * @return true if successful, false otherwise
     */
    public boolean saveCartItems(int userId, List<CartItem> items) {
        // First clear existing cart
        if (!clearCartItems(userId)) {
            return false;
        }

        // Insert new items
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        boolean success = true;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (CartItem item : items) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, item.getProduct().getId());
                pstmt.setInt(3, item.getQuantity());

                int result = pstmt.executeUpdate();
                if (result <= 0) {
                    success = false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving cart items for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * Remove all cart items for a user
     *
     * @param userId The ID of the user whose cart to clear
     * @return true if successful, false otherwise
     */
    public boolean clearCartItems(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error clearing cart items for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
