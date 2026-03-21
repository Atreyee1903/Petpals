package org.petpals.ui;

import org.petpals.db.OrderDAO;
import org.petpals.model.Order;
import org.petpals.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryPanel extends JPanel {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private final User currentUser;
  private final OrderDAO orderDAO;
  private JTable orderTable;
  private DefaultTableModel tableModel;

  public OrderHistoryPanel(User currentUser) {
    this.currentUser = currentUser;
    this.orderDAO = new OrderDAO();
    setLayout(new BorderLayout(10, 10));
    setBackground(Constants.COLOR_PANEL_BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    initComponents();
    loadOrderHistory();
  }

  private void initComponents() {
    // Title Label
    JLabel titleLabel = new JLabel("Your Order History");
    titleLabel.setFont(Constants.FONT_HEADING_2);
    titleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    add(titleLabel, BorderLayout.NORTH);

    // Table Setup
    String[] columnNames = {"Order ID", "Date", "Total Amount", "Status"};
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Read-only table
      }
    };
    orderTable = new JTable(tableModel);
    orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    orderTable.setAutoCreateRowSorter(true); // Allow sorting
    orderTable.getTableHeader().setFont(Constants.FONT_BOLD);
    orderTable.setFillsViewportHeight(true);
    orderTable.setRowHeight(25);
    orderTable.setGridColor(Constants.COLOR_BORDER);
    orderTable.setShowGrid(true);

    // Set preferred column widths
    TableColumnModel columnModel = orderTable.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(80);  // ID
    columnModel.getColumn(1).setPreferredWidth(150); // Date
    columnModel.getColumn(2).setPreferredWidth(120); // Amount
    columnModel.getColumn(3).setPreferredWidth(100); // Status

    // Price Renderer (reusing from ProductManagementPanel might be good, or create specific CurrencyRenderer)
    columnModel.getColumn(2).setCellRenderer(new CurrencyRenderer());

    JScrollPane scrollPane = new JScrollPane(orderTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER));
    add(scrollPane, BorderLayout.CENTER);

    // Optional: Add a refresh button if needed
    // JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    // JButton refreshButton = new JButton("Refresh");
    // styleSecondaryButton(refreshButton); // Assuming styling methods exist or are added
    // refreshButton.addActionListener(e -> loadOrderHistory());
    // bottomPanel.add(refreshButton);
    // add(bottomPanel, BorderLayout.SOUTH);
  }

  public void loadOrderHistory() {
    SwingUtilities.invokeLater(() -> {
      tableModel.setRowCount(0); // Clear existing rows
      if (currentUser == null) {
        // Handle case where user is somehow null
        tableModel.addRow(new Object[]{"Error", "User not logged in", "", ""});
        return;
      }

      List<Order> orders = orderDAO.getOrdersByUserId(currentUser.getId());

      if (orders.isEmpty()) {
        // Handle case with no orders
        tableModel.addRow(new Object[]{"-", "No orders found", "-", "-"});
      } else {
        for (Order order : orders) {
          String formattedDate = (order.getOrderDate() != null)
              ? order.getOrderDate().format(DATE_TIME_FORMATTER)
              : "N/A";
          tableModel.addRow(new Object[]{
              order.getId(),
              formattedDate,
              order.getTotalAmount(), // Use BigDecimal directly for renderer
              order.getStatus()
          });
        }
      }
    });
  }

  // Inner class for formatting currency in the table (similar to PriceRenderer)
  static class CurrencyRenderer extends javax.swing.table.DefaultTableCellRenderer {
    public CurrencyRenderer() {
      setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public void setValue(Object value) {
      if (value instanceof Number) {
        setText(Constants.INR_CURRENCY_FORMAT.format(value));
      } else {
        super.setValue(value);
      }
    }
  }

  // Placeholder for button styling method if used
  // private void styleSecondaryButton(JButton button) { ... }
}
