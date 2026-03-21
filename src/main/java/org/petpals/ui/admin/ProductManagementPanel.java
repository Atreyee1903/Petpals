package org.petpals.ui.admin;

import org.petpals.db.ProductDAO;
import org.petpals.model.Product;
import org.petpals.ui.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class ProductManagementPanel extends JPanel {

  private final ProductDAO productDAO;
  private final AdminPanel parentAdminPanel;
  private JTable productTable;
  private DefaultTableModel tableModel;

  public ProductManagementPanel(ProductDAO productDAO, AdminPanel parentAdminPanel) {
    this.productDAO = productDAO;
    this.parentAdminPanel = parentAdminPanel;
    initComponents();
    loadProductsData();
  }

  private void initComponents() {
    setLayout(new BorderLayout(10, 10));
    setBackground(Constants.COLOR_BACKGROUND);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    // --- Button Panel (Top) ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setOpaque(false);

    JButton addButton = new JButton("Add Product");
    addButton.setIcon(UIManager.getIcon("Tree.leafIcon"));
    addButton.addActionListener(e -> addProduct());
    buttonPanel.add(addButton);

    JButton editButton = new JButton("Edit Selected Product");
    editButton.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
    editButton.addActionListener(e -> editProduct());
    buttonPanel.add(editButton);

    JButton deleteButton = new JButton("Delete Selected Product");
    deleteButton.setForeground(Constants.COLOR_ERROR);
    deleteButton.setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
    deleteButton.addActionListener(e -> deleteProduct());
    buttonPanel.add(deleteButton);

    add(buttonPanel, BorderLayout.NORTH);

    // --- Table (Center) ---
    String[] columnNames = {"ID", "Name", "Category", "Price", "Image"};
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    productTable = new JTable(tableModel);
    productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    productTable.setAutoCreateRowSorter(true);
    productTable.getTableHeader().setFont(Constants.FONT_BOLD);
    productTable.setFillsViewportHeight(true);
    productTable.setRowHeight(25);

    // Set preferred column widths
    TableColumnModel columnModel = productTable.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(40);  // ID
    columnModel.getColumn(1).setPreferredWidth(200); // Name
    columnModel.getColumn(2).setPreferredWidth(120); // Category
    columnModel.getColumn(3).setPreferredWidth(80);  // Price
    columnModel.getColumn(4).setPreferredWidth(150); // Image

    // Price Renderer (Optional but nice)
    columnModel.getColumn(3).setCellRenderer(new PriceRenderer());


    JScrollPane scrollPane = new JScrollPane(productTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER));
    add(scrollPane, BorderLayout.CENTER);
  }

  public void loadProductsData() {
    SwingUtilities.invokeLater(() -> {
      tableModel.setRowCount(0);
      List<Product> products = productDAO.getAllProducts();
      for (Product product : products) {
        tableModel.addRow(new Object[]{
            product.getId(),
            product.getName(),
            product.getCategory(),
            product.getPrice(), // Store raw price for sorting
            product.getImage()
        });
      }
    });
  }

  private void addProduct() {
    ProductFormDialog dialog = new ProductFormDialog(parentAdminPanel.getParentFrame(), productDAO, null);
    dialog.setVisible(true);
    if (dialog.isSaved()) {
      loadProductsData();
    }
  }

  private void editProduct() {
    int selectedRow = productTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    int modelRow = productTable.convertRowIndexToModel(selectedRow);
    int productId = (int) tableModel.getValueAt(modelRow, 0);

    Product productToEdit = productDAO.getProductById(productId);
    if (productToEdit == null) {
      JOptionPane.showMessageDialog(this, "Could not find product details (ID: " + productId + ").", "Error", JOptionPane.ERROR_MESSAGE);
      loadProductsData();
      return;
    }

    ProductFormDialog dialog = new ProductFormDialog(parentAdminPanel.getParentFrame(), productDAO, productToEdit);
    dialog.setVisible(true);

    if (dialog.isSaved()) {
      loadProductsData();
    }
  }

  private void deleteProduct() {
    int selectedRow = productTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    int modelRow = productTable.convertRowIndexToModel(selectedRow);
    int productId = (int) tableModel.getValueAt(modelRow, 0);
    String productName = (String) tableModel.getValueAt(modelRow, 1);

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to permanently delete '" + productName + "' (ID: " + productId + ")?\nThis will NOT remove it from existing carts.",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      boolean deleted = productDAO.deleteProduct(productId);
      if (deleted) {
        JOptionPane.showMessageDialog(this, "'" + productName + "' deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
        loadProductsData();
      } else {
        JOptionPane.showMessageDialog(this, "Failed to delete '" + productName + "'. Check logs.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  // Inner class for formatting price in the table
  static class PriceRenderer extends javax.swing.table.DefaultTableCellRenderer {
    public PriceRenderer() {
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
}
