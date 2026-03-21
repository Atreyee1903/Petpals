package org.petpals.ui.admin;

import org.petpals.db.OrderDAO; // Added for OrderManagementPanel
import org.petpals.db.PetDAO;
import org.petpals.db.ProductDAO;
import org.petpals.db.TestimonialDAO; // Needed if we instantiate TestimonialManagementPanel here
import org.petpals.model.User; // Import User
import org.petpals.ui.Constants;
import org.petpals.ui.SupportPanel; // Import SupportPanel
import org.petpals.ui.admin.TestimonialManagementPanel; // Import the new panel

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminPanel extends JPanel {

  private final PetDAO petDAO;
  private final ProductDAO productDAO;
  private final OrderDAO orderDAO; // Added for OrderManagementPanel
  // TestimonialDAO might not be needed directly if TestimonialManagementPanel handles its own DAO
  private PetManagementPanel petManagementPanel;
  private ProductManagementPanel productManagementPanel;
  private OrderManagementPanel orderManagementPanel; // Added
  private TestimonialManagementPanel testimonialManagementPanel; // Added
  private final User currentUser; // Store the current user

  public AdminPanel(User currentUser) { // Accept User in constructor
    this.currentUser = currentUser; // Store the user
    this.petDAO = new PetDAO();
    this.productDAO = new ProductDAO();
    this.orderDAO = new OrderDAO(); // Instantiate OrderDAO
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    setBackground(Constants.COLOR_BACKGROUND);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    JTabbedPane adminTabbedPane = new JTabbedPane();
    adminTabbedPane.setFont(Constants.FONT_BOLD.deriveFont(13f));

    // Pet Management Tab
    petManagementPanel = new PetManagementPanel(petDAO, this);
    adminTabbedPane.addTab("Manage Pets", petManagementPanel);

    // Product Management Tab
    productManagementPanel = new ProductManagementPanel(productDAO, this);
    adminTabbedPane.addTab("Manage Products", productManagementPanel);

    // Order Management Tab (New)
    orderManagementPanel = new OrderManagementPanel(); // Uses its own OrderDAO instance
    adminTabbedPane.addTab("Manage Orders", orderManagementPanel);

    // Testimonial Management Tab (New)
    testimonialManagementPanel = new TestimonialManagementPanel(); // Uses its own TestimonialDAO instance
    adminTabbedPane.addTab("Manage Testimonials", testimonialManagementPanel);

    // Support Tab (for Admin)
    SupportPanel supportPanel = new SupportPanel(currentUser); // Pass the current user
    adminTabbedPane.addTab("Support Queries", supportPanel);


    add(adminTabbedPane, BorderLayout.CENTER);
  }

  // Optional: Method to refresh data in both panels if needed from outside
  public void refreshData() {
    if (petManagementPanel != null) {
      petManagementPanel.loadPetsData();
    }
    if (productManagementPanel != null) {
      productManagementPanel.loadProductsData();
    }
    if (orderManagementPanel != null) { // Added
        orderManagementPanel.refreshOrders();
    }
    // Testimonial panel refreshes internally on actions, but could add a manual refresh if needed
    // if (testimonialManagementPanel != null) {
    //     testimonialManagementPanel.loadPendingTestimonials();
    // }
  }

  // Helper to get the parent Frame (useful for dialogs)
  public Frame getParentFrame() {
    Component parent = this;
    while (parent != null && !(parent instanceof Frame)) {
      parent = parent.getParent();
    }
    return (Frame) parent;
  }
}
