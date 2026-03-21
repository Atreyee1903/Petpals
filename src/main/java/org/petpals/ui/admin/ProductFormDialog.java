package org.petpals.ui.admin;

import org.petpals.db.ProductDAO;
import org.petpals.model.Product;
import org.petpals.ui.Constants;

import org.petpals.db.ProductDAO;
import org.petpals.model.Product;
import org.petpals.ui.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;


public class ProductFormDialog extends JDialog {

  private final ProductDAO productDAO;
  private final Product productToEdit; // Null if adding new product
  private boolean saved = false;

  private JTextField nameField;
  private JTextField categoryField;
  private JFormattedTextField priceField; // Use JFormattedTextField for price
  // private JTextField imageField; // Replaced
  private JTextField imagePathField; // Displays selected image filename
  private JButton browseButton;
  private File selectedImageFile; // Holds the selected image file object

  public ProductFormDialog(Frame owner, ProductDAO productDAO, Product productToEdit) {
    super(owner, (productToEdit == null ? "Add New Product" : "Edit Product: " + productToEdit.getName()), true);
    this.productDAO = productDAO;
    this.productToEdit = productToEdit;
    initComponents();
    if (productToEdit != null) {
      populateFields();
    }
    pack();
    setMinimumSize(new Dimension(400, 300));
    setLocationRelativeTo(owner);
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    mainPanel.setBackground(Constants.COLOR_BACKGROUND);

    // --- Form Panel ---
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Labels
    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.gridy = 0;
    formPanel.add(new JLabel("Name*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Category*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Price (₹)*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Image*:"), gbc); // Changed label

    // Fields
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.gridy = 0;
    nameField = new JTextField(25);
    formPanel.add(nameField, gbc);
    gbc.gridy++;
    categoryField = new JTextField();
    formPanel.add(categoryField, gbc);

    // Price Field (Formatted) - Use default Locale number format
    gbc.gridy++;
    NumberFormat amountFormat = NumberFormat.getNumberInstance(); // More robust than currency for input
    amountFormat.setMaximumFractionDigits(2);
    amountFormat.setMinimumFractionDigits(2);
    priceField = new JFormattedTextField(amountFormat);
    priceField.setValue(0.00); // Default value
    priceField.setColumns(10); // Set width hint
    formPanel.add(priceField, gbc);

    // --- Image Selection Panel ---
    gbc.gridy++;
    JPanel imagePanel = new JPanel(new BorderLayout(5, 0)); // Panel for text field and button
    imagePanel.setOpaque(false);
    imagePathField = new JTextField();
    imagePathField.setEditable(false); // User cannot type path directly
    imagePathField.setBackground(Color.WHITE); // Indicate non-editable state clearly
    browseButton = new JButton("Browse...");
    browseButton.addActionListener(e -> browseImage());
    imagePanel.add(imagePathField, BorderLayout.CENTER);
    imagePanel.add(browseButton, BorderLayout.EAST);
    formPanel.add(imagePanel, gbc);
    // --- End Image Selection Panel ---


    mainPanel.add(formPanel, BorderLayout.CENTER);

    // --- Button Panel ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setOpaque(false);

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveProduct());
    buttonPanel.add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    buttonPanel.add(cancelButton);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(saveButton);

    getContentPane().add(mainPanel);
  }

  private void populateFields() {
    nameField.setText(productToEdit.getName());
    categoryField.setText(productToEdit.getCategory());
    priceField.setValue(productToEdit.getPrice()); // Set the numeric value
    imagePathField.setText(productToEdit.getImage()); // Set the path field
    // selectedImageFile remains null until user browses
  }

  private boolean validateInput() {
    if (nameField.getText().trim().isEmpty() ||
        categoryField.getText().trim().isEmpty() ||
        imagePathField.getText().trim().isEmpty() || // Check the path field
        priceField.getValue() == null) {
      JOptionPane.showMessageDialog(this, "Please fill in all required fields (*), including selecting an image.", "Validation Error", JOptionPane.WARNING_MESSAGE); // Updated message
      return false;
    }

    // Validate price is a positive number
    try {
      // Commit the edit to ensure the value is parsed
      priceField.commitEdit();
      double price = ((Number) priceField.getValue()).doubleValue();
      if (price < 0) {
        JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        return false;
      }
    } catch (ParseException ex) {
      JOptionPane.showMessageDialog(this, "Invalid price format. Please enter a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
      return false;
    }

    return true;
  }


  private void saveProduct() {
    if (!validateInput()) {
      return;
    }

    String name = nameField.getText().trim();
    String category = categoryField.getText().trim();
    double price = ((Number) priceField.getValue()).doubleValue();
    // String image = imageField.getText().trim(); // Old way
    String imageFilename = imagePathField.getText().trim(); // Get filename from display field

    // --- Handle Image Copying ---
    if (selectedImageFile != null) { // A new image file was selected
      try {
        // Define target directory within the project structure
        Path targetDir = Paths.get("src", "main", "resources", "images", "products");
        // Create directory if it doesn't exist
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(selectedImageFile.getName());

        // Copy the selected file to the target directory, replacing if it exists
        Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // The filename to store is just the name part
        imageFilename = selectedImageFile.getName();

      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
            "Error copying image file: " + ex.getMessage(),
            "File Error", JOptionPane.ERROR_MESSAGE);
        return; // Stop saving process if image copy fails
      }
    } else if (productToEdit == null) {
        // If adding a new product and no image was selected, validation should have caught this.
         JOptionPane.showMessageDialog(this, "Please select an image file.", "Validation Error", JOptionPane.WARNING_MESSAGE);
         return;
    }
    // If editing and no new file selected, imageFilename already holds the existing filename.
    // --- End Handle Image Copying ---


    int productId = (productToEdit != null) ? productToEdit.getId() : 0;

    // Create Product object with potentially updated imageFilename
    Product product = new Product(productId, name, price, imageFilename, category);

    boolean success;
    if (productToEdit == null) {
      success = productDAO.addProduct(product);
    } else {
      success = productDAO.updateProduct(product);
    }

    if (success) {
      saved = true;
      dispose();
    } else {
      JOptionPane.showMessageDialog(this,
          "Failed to save product details. Please check the logs.",
          "Database Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public boolean isSaved() {
    return saved;
  }

  // --- New method to handle image browsing ---
  private void browseImage() {
    JFileChooser fileChooser = new JFileChooser();
    // Set filter for common image types
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "Image Files (jpg, png, gif)", "jpg", "jpeg", "png", "gif");
    fileChooser.setFileFilter(filter);
    fileChooser.setAcceptAllFileFilterUsed(false); // Only allow specified image types

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      selectedImageFile = fileChooser.getSelectedFile();
      imagePathField.setText(selectedImageFile.getName()); // Display only the filename
    }
  }
  // --- End new method ---
}
