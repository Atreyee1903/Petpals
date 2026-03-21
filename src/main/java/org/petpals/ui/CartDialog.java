package org.petpals.ui;

import org.petpals.db.OrderDAO;
import org.petpals.model.CartItem;
import org.petpals.model.Order;
import org.petpals.model.OrderItem;
import org.petpals.model.User;
import org.petpals.utils.CartManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;


public class CartDialog extends JDialog {

  private final CartManager cartManager;
  private final Runnable updateCartCallback;
  private DefaultTableModel tableModel;
  private JTable cartTable;
  private JLabel totalLabel;
  private JButton checkoutButton;

  public CartDialog(Frame owner, Runnable updateCartCallback) {
    super(owner, "Shopping Cart", true);
    this.cartManager = CartManager.getInstance();
    this.updateCartCallback = updateCartCallback;
    setSize(650, 450); // Slightly wider
    setLocationRelativeTo(owner);
    initComponents();
    populateCartTable();
    getContentPane().setBackground(Constants.COLOR_BACKGROUND);
  }

  private void initComponents() {
    setLayout(new BorderLayout(10, 10));
    // Add padding around the dialog content
    getRootPane().setBorder(BorderFactory.createEmptyBorder(Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING));

    // Table setup
    String[] columnNames = {"Product", "Price", "Quantity", "Subtotal", "Action"}; // Added Action column
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        // Only Quantity and Action column might be interactive
        return column == 2 || column == 4;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Integer.class; // Quantity column
        if (columnIndex == 4) return JButton.class; // Action column
        return String.class; // Others are strings (product name, formatted price)
      }
    };
    cartTable = new JTable(tableModel);
    cartTable.setRowHeight(30); // Increased row height
    cartTable.setFont(Constants.FONT_NORMAL);
    cartTable.getTableHeader().setFont(Constants.FONT_BOLD);
    cartTable.setFillsViewportHeight(true); // Table fills scroll pane height
    cartTable.setGridColor(Constants.COLOR_BORDER);
    cartTable.setShowGrid(true); // Show subtle grid lines
    cartTable.setIntercellSpacing(new Dimension(0, 1)); // Minimal vertical spacing

    // Set column widths (adjust as needed)
    setColumnWidths(cartTable, 200, 100, 80, 100, 80);

    // Custom renderer/editor for the Remove button
    cartTable.getColumn("Action").setCellRenderer(new ButtonRenderer("Remove"));
    cartTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), "Remove", this::removeCartItem));

    // Optional: Editor for quantity (e.g., Spinner or validate input)
    // For simplicity, keeping default integer editor for now.

    // Add listener for quantity changes (if default editor is used)
    tableModel.addTableModelListener(e -> {
      if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column == 2) { // Quantity column updated
          updateQuantityFromTable(row);
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(cartTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER)); // Border for scroll pane
    add(scrollPane, BorderLayout.CENTER);

    // Bottom Panel (Total and Buttons)
    JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
    bottomPanel.setOpaque(false); // Inherit background
    bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Padding above bottom panel

    // Total Label (Left)
    totalLabel = new JLabel("Total: ₹0.00");
    totalLabel.setFont(Constants.FONT_HEADING_3.deriveFont(18f)); // Larger total font
    totalLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    bottomPanel.add(totalLabel, BorderLayout.WEST);

    // Button Panel (Right)
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Gaps between buttons
    buttonPanel.setOpaque(false);

    JButton clearButton = new JButton("Clear Cart");
    styleSecondaryButton(clearButton);
    clearButton.addActionListener(e -> clearCart());

    checkoutButton = new JButton("Proceed to Checkout");
    stylePrimaryButton(checkoutButton);
    checkoutButton.addActionListener(e -> checkout());
    checkoutButton.setEnabled(cartManager.getTotalItemCount() > 0);

    JButton closeButton = new JButton("Close");
    styleSecondaryButton(closeButton);
    closeButton.addActionListener(e -> dispose());

    buttonPanel.add(clearButton);
    buttonPanel.add(checkoutButton);
    buttonPanel.add(closeButton);
    bottomPanel.add(buttonPanel, BorderLayout.EAST);

    add(bottomPanel, BorderLayout.SOUTH);
  }

  private void setColumnWidths(JTable table, int... widths) {
    TableColumn column;
    for (int i = 0; i < widths.length; i++) {
      if (i < table.getColumnModel().getColumnCount()) {
        column = table.getColumnModel().getColumn(i);
        column.setPreferredWidth(widths[i]);
      }
    }
  }

  private void populateCartTable() {
    tableModel.setRowCount(0); // Clear existing rows
    List<CartItem> items = cartManager.getItems();

    for (CartItem item : items) {
      Object[] rowData = {
          item.getProduct().getName(),
          Constants.INR_CURRENCY_FORMAT.format(item.getProduct().getPrice()),
          item.getQuantity(),
          Constants.INR_CURRENCY_FORMAT.format(item.getTotalPrice()),
          "Remove" // Placeholder text for the button column
      };
      tableModel.addRow(rowData);
    }
    updateTotal();
    checkoutButton.setEnabled(!items.isEmpty());
  }

  // Action to be called by the button editor
  private void removeCartItem(int row) {
    if (row >= 0 && row < tableModel.getRowCount()) {
      List<CartItem> items = cartManager.getItems();
      if (row < items.size()) {
        CartItem itemToRemove = items.get(row);
        // Directly remove based on the product in CartManager (more robust than relying on row index after potential shifts)
        cartManager.removeItem(itemToRemove.getProduct());
        populateCartTable(); // Refresh table from CartManager data
      }
    }
  }

  // Update CartManager when quantity is edited in the table
  private void updateQuantityFromTable(int row) {
    if (row >= 0 && row < tableModel.getRowCount()) {
      List<CartItem> items = cartManager.getItems();
      if (row < items.size()) {
        try {
          int newQuantity = (Integer) tableModel.getValueAt(row, 2);
          CartItem itemToUpdate = items.get(row);

          if (newQuantity <= 0) {
            // If quantity becomes 0 or less, remove the item
            cartManager.removeItem(itemToUpdate.getProduct());
            // Use invokeLater to avoid modifying table model during notification
            SwingUtilities.invokeLater(this::populateCartTable);
          } else {
            cartManager.updateQuantity(itemToUpdate.getProduct(), newQuantity);
            // Update subtotal in the table immediately for visual feedback
            tableModel.setValueAt(Constants.INR_CURRENCY_FORMAT.format(itemToUpdate.getTotalPrice()), row, 3);
            updateTotal(); // Update grand total label
          }
        } catch (NumberFormatException | ClassCastException ex) {
          System.err.println("Invalid quantity entered at row " + row);
          // Optionally revert the change or show an error
          // For now, just log it. Repopulating will fix display.
          SwingUtilities.invokeLater(this::populateCartTable);
        }
      }
    }
  }


  private void updateTotal() {
    totalLabel.setText("Total: " + Constants.INR_CURRENCY_FORMAT.format(cartManager.getTotalCost()));
    updateCartCallback.run(); // Update the button on the main HomePage
  }

  private void clearCart() {
    if (cartManager.getTotalItemCount() == 0) return; // No need to clear empty cart

    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to remove all items from your cart?",
        "Confirm Clear Cart",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE); // Warning icon
    if (confirm == JOptionPane.YES_OPTION) {
      cartManager.clearCart();
      populateCartTable(); // Update table view
    }
  }

  private void checkout() {
    double totalAmount = cartManager.getTotalCost();
    if (totalAmount <= 0) {
      JOptionPane.showMessageDialog(this, "Your cart is empty.", "Cannot Checkout", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Ensure UPI Dialog is also themed if needed
    UpiPaymentDialog paymentDialog = new UpiPaymentDialog(this, totalAmount);
    paymentDialog.setVisible(true);

    if (paymentDialog.isPaymentSubmitted()) {
      String upiId = paymentDialog.getUpiId();
      String street = paymentDialog.getStreetAddress();
      String city = paymentDialog.getCity();
      String state = paymentDialog.getState();
      String postalCode = paymentDialog.getPostalCode();
      String phone = paymentDialog.getPhoneNumber();

      // --- Save Order to Database --- START ---
      User currentUser = cartManager.getCurrentUser(); // Assuming CartManager has this method
      if (currentUser == null) {
        JOptionPane.showMessageDialog(this, "Error: User information not found. Cannot save order.", "Order Error", JOptionPane.ERROR_MESSAGE);
        System.err.println("Cannot save order: currentUser is null in CartDialog.");
        return;
      }

      OrderDAO orderDAO = new OrderDAO();
      Order order = new Order(
          currentUser.getId(),
          BigDecimal.valueOf(totalAmount),
          street, city, state, postalCode, phone, upiId
      );

      List<CartItem> cartItems = cartManager.getItems();
      for (CartItem cartItem : cartItems) {
        OrderItem orderItem = new OrderItem(
            cartItem.getProduct().getId(),
            cartItem.getQuantity(),
            BigDecimal.valueOf(cartItem.getProduct().getPrice()) // Price at time of order
        );
        order.addItem(orderItem);
      }

      boolean saved = orderDAO.saveOrder(order);
      // --- Save Order to Database --- END ---

      if (saved) {
        // Nicer confirmation message
        String thankYouMsg = String.format(
            "<html><body>" +
                "<h2 style='color: %s;'>Order Placed Successfully! (ID: %d)</h2>" +
                "<p>Thank you for shopping with PetPals.</p>" +
                "<p>Total Amount: <b>%s</b></p>" +
                "<p>Payment initiated via UPI ID: <i>%s</i></p>" +
                "<hr>" + // Separator
                "<p><b>Shipping Address:</b><br>" +
                "%s<br>" +
                "%s, %s %s<br>" +
                "Phone: %s</p>" +
                "<hr>" +
                "<p style='font-size: smaller;'>(This is a simulation - no real payment occurred or items shipped)</p>" +
                "</body></html>",
            "#" + Integer.toHexString(Constants.COLOR_SUCCESS.getRGB()).substring(2), // HTML color
            order.getId(), // Show the generated order ID
            Constants.INR_CURRENCY_FORMAT.format(totalAmount),
            upiId,
            street, city, state, postalCode, phone // Add address details here
        );

        JOptionPane.showMessageDialog(this,
            thankYouMsg,
            "Order Confirmation",
            JOptionPane.INFORMATION_MESSAGE); // Use Information icon

        cartManager.clearCart(); // Clear cart ONLY after successful save and confirmation
        populateCartTable(); // Update view
        updateCartCallback.run(); // Ensure main window cart button updates
        dispose(); // Close cart dialog
      } else {
        // Handle database save failure
        JOptionPane.showMessageDialog(this,
            "There was an error saving your order to the database. Please try again later or contact support.",
            "Order Save Failed",
            JOptionPane.ERROR_MESSAGE);
        System.err.println("Failed to save order to database for user ID: " + currentUser.getId());
        // Do NOT clear cart or close dialog if save failed
      }
    } else {
      System.out.println("Payment cancelled by user.");
      // Optional: Show a less intrusive message
      // JOptionPane.showMessageDialog(this, "Payment cancelled.", "Checkout", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  // --- Helper Button Styling ---
  private void stylePrimaryButton(JButton button) {
    button.setFont(Constants.FONT_BUTTON.deriveFont(Font.BOLD));
    button.setBackground(Constants.COLOR_PRIMARY);
    button.setForeground(Color.WHITE);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.putClientProperty("JButton.buttonType", "roundRect"); // FlatLaf hint
  }

  private void styleSecondaryButton(JButton button) {
    button.setFont(Constants.FONT_BUTTON);
    // Use default L&F background or a subtle color
    // button.setBackground(Color.LIGHT_GRAY);
    button.setForeground(Constants.COLOR_TEXT_PRIMARY);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.putClientProperty("JButton.buttonType", "roundRect"); // FlatLaf hint
  }

  // --- Inner Classes for Button Column ---

  // Renders the button in the cell
  class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer(String text) {
      setOpaque(true);
      setForeground(Constants.COLOR_ERROR); // Red text for remove
      setBackground(UIManager.getColor("Button.background")); // Use L&F background
      setBorder(UIManager.getBorder("Button.border"));
      // setBorderPainted(false); // Optional: make it look flatter
      setFont(Constants.FONT_BUTTON.deriveFont(11f)); // Smaller font
      setText(text);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
      // Make button look slightly different when selected (optional)
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        setBackground(table.getSelectionBackground());
      } else {
        setForeground(Constants.COLOR_ERROR);
        setBackground(UIManager.getColor("Button.background"));
      }
      setText((value == null) ? "" : value.toString());
      return this;
    }
  }

  // Handles button clicks
  class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private int selectedRow;
    private java.util.function.IntConsumer action; // Action to perform on click

    public ButtonEditor(JCheckBox checkBox, String text, java.util.function.IntConsumer action) {
      super(checkBox);
      this.action = action;
      button = new JButton();
      button.setOpaque(true);
      button.setForeground(Constants.COLOR_ERROR);
      button.setFont(Constants.FONT_BUTTON.deriveFont(11f));
      // button.setBorderPainted(false);
      button.addActionListener(e -> fireEditingStopped());
      label = text;
      button.setText(label);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
      selectedRow = row;
      if (isSelected) {
        button.setForeground(table.getSelectionForeground());
        button.setBackground(table.getSelectionBackground());
      } else {
        button.setForeground(Constants.COLOR_ERROR);
        button.setBackground(UIManager.getColor("Button.background"));
      }
      button.setText(label);
      isPushed = true;
      return button;
    }

    public Object getCellEditorValue() {
      if (isPushed) {
        // Perform the action when button is clicked
        action.accept(selectedRow);
      }
      isPushed = false;
      return label; // Return the label for the cell value
    }

    public boolean stopCellEditing() {
      isPushed = false;
      return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
      try {
        super.fireEditingStopped();
      } catch (Exception e) {
        // Catch potential exceptions during action execution within stopCellEditing
        System.err.println("Error during cell editing stop/action: " + e.getMessage());
      }
    }
  }
}
