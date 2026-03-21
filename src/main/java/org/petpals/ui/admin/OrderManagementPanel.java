package org.petpals.ui.admin;

import org.petpals.db.OrderDAO;
import org.petpals.model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class OrderManagementPanel extends JPanel {

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private OrderDAO orderDAO;
    private JComboBox<String> statusComboBox;
    private JButton updateStatusButton;

    private static final String[] STATUS_OPTIONS = {"Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OrderManagementPanel() {
        orderDAO = new OrderDAO();
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table setup
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        tableModel.addColumn("Order ID");
        tableModel.addColumn("User ID");
        tableModel.addColumn("Total Amount");
        tableModel.addColumn("Order Date");
        tableModel.addColumn("Status");
        tableModel.addColumn("Shipping City");
        tableModel.addColumn("Shipping Phone");

        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering

        JScrollPane scrollPane = new JScrollPane(orderTable);

        // Control panel for status updates
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Change Status:"));
        statusComboBox = new JComboBox<>(STATUS_OPTIONS);
        controlPanel.add(statusComboBox);

        updateStatusButton = new JButton("Update Status");
        updateStatusButton.setEnabled(false); // Disabled until an order is selected
        controlPanel.add(updateStatusButton);

        // Add components to the main panel
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Add listener for table selection changes
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                updateStatusButton.setEnabled(true);
                // Optionally set the combo box to the current status of the selected order
                String currentStatus = (String) tableModel.getValueAt(orderTable.getSelectedRow(), 4);
                statusComboBox.setSelectedItem(currentStatus);
            } else {
                updateStatusButton.setEnabled(false);
            }
        });

        // Add action listener for the update button
        updateStatusButton.addActionListener(e -> updateSelectedOrderStatus());
    }

    private void loadOrders() {
        // Clear existing rows
        tableModel.setRowCount(0);

        List<Order> orders = orderDAO.getAllOrders();
        if (orders.isEmpty()) {
            // Optionally display a message if no orders exist
            System.out.println("No orders found.");
            return;
        }

        for (Order order : orders) {
            Vector<Object> row = new Vector<>();
            row.add(order.getId());
            row.add(order.getUserId());
            row.add(order.getTotalAmount());
            row.add(order.getOrderDate() != null ? order.getOrderDate().format(DATE_FORMATTER) : "N/A");
            row.add(order.getStatus());
            row.add(order.getShippingCity());
            row.add(order.getShippingPhone());
            tableModel.addRow(row);
        }
    }

    private void updateSelectedOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to update.", "No Order Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0); // Assuming Order ID is in the first column
        String newStatus = (String) statusComboBox.getSelectedItem();
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 4); // Assuming Status is in the fifth column

        if (newStatus == null || newStatus.equals(currentStatus)) {
             JOptionPane.showMessageDialog(this, "Please select a different status.", "Status Unchanged", JOptionPane.INFORMATION_MESSAGE);
            return; // No change needed
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Update status for Order ID " + orderId + " to '" + newStatus + "'?",
                "Confirm Status Update",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderDAO.updateOrderStatus(orderId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Order status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Update the table visually
                tableModel.setValueAt(newStatus, selectedRow, 4);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update order status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     // Method to refresh the order list (e.g., called from AdminPanel)
    public void refreshOrders() {
        loadOrders();
    }
}
