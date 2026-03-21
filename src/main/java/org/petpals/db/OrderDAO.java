package org.petpals.db;

import org.petpals.model.Order;
import org.petpals.model.OrderItem;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDAO {

    /**
     * Saves an order and its items to the database within a transaction.
     *
     * @param order The Order object to save (must contain OrderItems).
     * @return true if the order was saved successfully, false otherwise.
     */
    public boolean saveOrder(Order order) {
        String insertOrderSQL = "INSERT INTO orders (user_id, total_amount, shipping_street, shipping_city, " +
                                "shipping_state, shipping_postal_code, shipping_phone, payment_upi_id, status, order_date) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price_at_time_of_order) " +
                                    "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtItem = null;
        ResultSet generatedKeys = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert the main order record
            pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getUserId());
            pstmtOrder.setBigDecimal(2, order.getTotalAmount());
            pstmtOrder.setString(3, order.getShippingStreet());
            pstmtOrder.setString(4, order.getShippingCity());
            pstmtOrder.setString(5, order.getShippingState());
            pstmtOrder.setString(6, order.getShippingPostalCode());
            pstmtOrder.setString(7, order.getShippingPhone());
            pstmtOrder.setString(8, order.getPaymentUpiId());
            pstmtOrder.setString(9, order.getStatus());
            pstmtOrder.setTimestamp(10, Timestamp.valueOf(order.getOrderDate()));

            int affectedRows = pstmtOrder.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            // Get the generated order ID
            generatedKeys = pstmtOrder.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                order.setId(orderId); // Set the ID on the order object

                // Insert order items
                pstmtItem = conn.prepareStatement(insertOrderItemSQL);
                for (OrderItem item : order.getItems()) {
                    pstmtItem.setInt(1, orderId); // Use the generated order ID
                    pstmtItem.setInt(2, item.getProductId());
                    pstmtItem.setInt(3, item.getQuantity());
                    pstmtItem.setBigDecimal(4, item.getPriceAtTimeOfOrder());
                    pstmtItem.addBatch(); // Add to batch for efficiency
                }
                pstmtItem.executeBatch(); // Execute batch insert for items

                conn.commit(); // Commit transaction
                success = true;
                System.out.println("Order saved successfully with ID: " + orderId);

            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

        } catch (SQLException e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back.");
                    conn.rollback();
                } catch (SQLException excep) {
                    System.err.println("Error rolling back transaction: " + excep.getMessage());
                }
            }
        } finally {
            // Close resources
            try {
                if (generatedKeys != null) generatedKeys.close();
            } catch (SQLException e) { /* ignore */ }
            try {
                if (pstmtItem != null) pstmtItem.close();
            } catch (SQLException e) { /* ignore */ }
            try {
                if (pstmtOrder != null) pstmtOrder.close();
            } catch (SQLException e) { /* ignore */ }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    // DatabaseConnection.closeConnection(); // Don't close if shared
                } catch (SQLException e) { /* ignore */ }
            }
        }

        return success;
    }

    /**
     * Retrieves a list of all orders, ordered by date descending.
     * Suitable for admin views. Does not load OrderItems.
     *
     * @return A List of all Order objects, or an empty list if none found or on error.
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, user_id, total_amount, shipping_street, shipping_city, shipping_state, " +
                     "shipping_postal_code, shipping_phone, payment_upi_id, status, order_date " +
                     "FROM orders ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("user_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("shipping_street"),
                        rs.getString("shipping_city"),
                        rs.getString("shipping_state"),
                        rs.getString("shipping_postal_code"),
                        rs.getString("shipping_phone"),
                        rs.getString("payment_upi_id")
                );
                order.setId(rs.getInt("id"));
                order.setStatus(rs.getString("status"));
                Timestamp ts = rs.getTimestamp("order_date");
                if (ts != null) {
                    order.setOrderDate(ts.toLocalDateTime());
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }


    /**
     * Retrieves a list of orders for a specific user, ordered by date descending.
     * Note: This method intentionally does not load associated OrderItems for performance.
     *
     * @param userId The ID of the user whose orders to retrieve.
     * @return A List of Order objects, or an empty list if none found or on error.
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, total_amount, shipping_street, shipping_city, shipping_state, " +
                     "shipping_postal_code, shipping_phone, payment_upi_id, status, order_date " +
                     "FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create Order object without items
                    Order order = new Order(
                            userId, // user ID is known
                            rs.getBigDecimal("total_amount"),
                            rs.getString("shipping_street"),
                            rs.getString("shipping_city"),
                            rs.getString("shipping_state"),
                            rs.getString("shipping_postal_code"),
                            rs.getString("shipping_phone"),
                            rs.getString("payment_upi_id")
                    );
                    // Set fields not included in constructor
                    order.setId(rs.getInt("id"));
                    order.setStatus(rs.getString("status"));
                    // Order date requires conversion from Timestamp
                    Timestamp ts = rs.getTimestamp("order_date");
                    if (ts != null) {
                         // Use reflection to set orderDate as it's final in constructor
                         // Or modify Order constructor/add setter if preferred
                         // For simplicity here, we'll assume a setter or adjust constructor later if needed
                         order.setOrderDate(ts.toLocalDateTime());
                         // If no setter, need alternative approach like reflection or constructor modification
                         // Re-creating object might be simpler if constructor changes
                         // Let's stick to retrieving data for now, handling the date setting might need model adjustment
                         // TODO: Address setting orderDate properly (e.g., add setter or adjust constructor)
                    }

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders for user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            // Return empty list on error
        }

        return orders;
    }

    // TODO: Add getOrderDetailsById(int orderId) method later for viewing specific order details including items

    /**
     * Updates the status of a specific order.
     *
     * @param orderId   The ID of the order to update.
     * @param newStatus The new status string (e.g., "Shipped", "Delivered", "Cancelled").
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                success = true;
                System.out.println("Order ID " + orderId + " status updated to: " + newStatus);
            } else {
                System.err.println("Order ID " + orderId + " not found or status not updated.");
            }

        } catch (SQLException e) {
            System.err.println("Error updating order status for ID " + orderId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return success;
    }
}
