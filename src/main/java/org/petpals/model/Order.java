package org.petpals.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String status; // e.g., "Pending", "Shipped", "Delivered"

    // Shipping Information
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingPhone;

    // Payment Information (e.g., UPI ID used)
    private String paymentUpiId; // Example, adapt as needed

    public Order(int userId, BigDecimal totalAmount, String shippingStreet, String shippingCity,
                 String shippingState, String shippingPostalCode, String shippingPhone, String paymentUpiId) {
        this.userId = userId;
        this.items = new ArrayList<>(); // Initialize empty list
        this.totalAmount = totalAmount;
        this.orderDate = LocalDateTime.now(); // Set current date/time
        this.status = "Pending"; // Default status
        this.shippingStreet = shippingStreet;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingPostalCode = shippingPostalCode;
        this.shippingPhone = shippingPhone;
        this.paymentUpiId = paymentUpiId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public String getShippingStreet() {
        return shippingStreet;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public String getShippingState() {
        return shippingState;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public String getPaymentUpiId() {
        return paymentUpiId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for Order Date (needed when loading from DB)
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    // Method to add items
    public void addItem(OrderItem item) {
        this.items.add(item);
        // Note: You might want to recalculate totalAmount here if items are added after construction,
        // but for saving an order post-checkout, the total is usually fixed.
    }
}
