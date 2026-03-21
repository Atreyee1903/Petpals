package org.petpals.ui;

import org.petpals.model.Product;
import org.petpals.ui.components.ImageLabel;
import org.petpals.utils.CartManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProductPanel extends JPanel {

  private final Product product;
  private final CartManager cartManager;
  private final Runnable updateCartCallback;

  public ProductPanel(Product product, Runnable updateCartCallback) {
    this.product = product;
    this.cartManager = CartManager.getInstance();
    this.updateCartCallback = updateCartCallback;
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout(5, 8)); // Vertical gap increased slightly
    setBackground(Constants.COLOR_PANEL_BACKGROUND);
    setOpaque(true);

    // Consistent card styling
    Border line = BorderFactory.createLineBorder(Constants.COLOR_BORDER, 1, true); // Rounded
    Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    setBorder(new CompoundBorder(line, padding));

    setPreferredSize(new Dimension(200, 280)); // Adjusted size for product cards
    setMaximumSize(new Dimension(220, 300));

    // --- Top Panel for Details ---
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setOpaque(false); // Inherit background

    JLabel nameLabel = new JLabel(product.getName());
    nameLabel.setFont(Constants.FONT_BOLD.deriveFont(14f)); // Slightly smaller than pet name
    nameLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nameLabel.setHorizontalAlignment(SwingConstants.CENTER); // Ensure centered text
    nameLabel.setToolTipText(product.getName()); // Tooltip for long names

    JLabel priceLabel = new JLabel(Constants.INR_CURRENCY_FORMAT.format(product.getPrice()));
    priceLabel.setFont(Constants.FONT_BOLD.deriveFont(15f));
    priceLabel.setForeground(Constants.COLOR_PRICE); // Use defined price color
    priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    priceLabel.setHorizontalAlignment(SwingConstants.CENTER);

    detailsPanel.add(nameLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
    detailsPanel.add(priceLabel);

    add(detailsPanel, BorderLayout.NORTH); // Name and Price at the top

    // --- Image (Center) ---
    ImageLabel imageLabel = new ImageLabel();
    int imgWidth = 160; // Preferred width = Panel width - padding*2
    int imgHeight = 120;
    imageLabel.loadImage("resources/images/products/", product.getImage(), imgWidth, imgHeight);
    // Center image horizontally within its space
    JPanel imageContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
    imageContainer.setOpaque(false);
    imageContainer.add(imageLabel);
    add(imageContainer, BorderLayout.CENTER); // Image in the middle

    // --- Button Panel (Bottom) ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.setOpaque(false);
    buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Add space above button

    JButton addToCartButton = new JButton("Add to Cart");
    addToCartButton.setFont(Constants.FONT_BUTTON);
    addToCartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    // Style as primary action button
    addToCartButton.setBackground(Constants.COLOR_PRIMARY);
    addToCartButton.setForeground(Color.WHITE);
    addToCartButton.putClientProperty("JButton.buttonType", "roundRect"); // FlatLaf hint


    addToCartButton.addActionListener(e -> {
      cartManager.addProduct(product, 1);
      // More subtle confirmation
      // Option 1: Tooltip feedback (less intrusive)
      // addToCartButton.setToolTipText(product.getName() + " added!"); // Doesn't show automatically
      // Option 2: Console log (for dev) + cart button update is main feedback
      System.out.println(product.getName() + " added to cart.");
      // Option 3: Keep JOptionPane but make it less frequent or configurable?
      // JOptionPane.showMessageDialog(this,
      //     product.getName() + " added to cart.",
      //     "Cart Update",
      //     JOptionPane.INFORMATION_MESSAGE);
      updateCartCallback.run(); // Update the main cart button count
    });
    buttonPanel.add(addToCartButton);
    add(buttonPanel, BorderLayout.SOUTH); // Button at the bottom
  }
}