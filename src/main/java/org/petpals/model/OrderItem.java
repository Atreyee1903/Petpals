package org.petpals.model;

import java.math.BigDecimal;

public class OrderItem {
    private int id; // Database ID for the order item itself
    private int orderId; // Foreign key to the orders table
    private int productId; // Foreign key to the products table
    private int quantity;
    private BigDecimal priceAtTimeOfOrder; // Price of the product when the order was placed

    // Constructor used when creating an item to be saved
    public OrderItem(int productId, int quantity, BigDecimal priceAtTimeOfOrder) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtTimeOfOrder = priceAtTimeOfOrder;
        // orderId will be set by the OrderDAO when saving
    }

    // Constructor used when loading from the database (includes all IDs)
    public OrderItem(int id, int orderId, int productId, int quantity, BigDecimal priceAtTimeOfOrder) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtTimeOfOrder = priceAtTimeOfOrder;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceAtTimeOfOrder() {
        return priceAtTimeOfOrder;
    }

    // Setters (primarily for orderId when saving)
    public void setId(int id) {
        this.id = id;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", orderId=" + orderId +
               ", productId=" + productId +
               ", quantity=" + quantity +
               ", priceAtTimeOfOrder=" + priceAtTimeOfOrder +
               '}';
    }
}
