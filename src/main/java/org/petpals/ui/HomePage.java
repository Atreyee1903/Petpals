package org.petpals.ui;

import org.petpals.Main;
import org.petpals.db.*;
import org.petpals.model.Pet;
import org.petpals.model.Product;
import org.petpals.model.Testimonial;
import org.petpals.model.User;
import org.petpals.ui.admin.AdminPanel;
import org.petpals.ui.components.WrapLayout;
import org.petpals.ui.SupportPanel; // Import the new panel
import org.petpals.utils.AuthPreferences;
import org.petpals.utils.CartManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;


public class HomePage extends JFrame {
  private static final int SCROLL_SPEED = 18; // Slightly adjusted scroll speed
  private final PetDAO petDAO;
  private final ProductDAO productDAO;
  private final TestimonialDAO testimonialDAO;
  private final CartManager cartManager;
  private final FavoriteDAO favoriteDAO;
  private final User currentUser; // User object now contains isAdmin()
  private JTabbedPane tabbedPane;
  private JPanel petsPanelContainer;
  private JComboBox<String> speciesFilterComboBox; // Added for pet filtering
  private JPanel productsPanelContainer; // Holds the ProductPanel instances
  private JScrollPane productsScrollPane; // Scroll pane for products
  private JTextField productSearchField;
  private JButton productSearchButton;
  private JPanel testimonialsPanelContainer;
  private JButton cartButton;
  private JPanel favoritesPanelContainer; // Panel for the favorites tab
  private int favoritesTabIndex = -1; // Store the index of the favorites tab
  private int adminTabIndex = -1; // Store index for Admin tab
  private int orderHistoryTabIndex = -1; // Store index for Order History tab
  private int supportTabIndex = -1; // Store index for Support tab

  public HomePage(User user) {
    this.currentUser = user;
    this.petDAO = new PetDAO();
    this.productDAO = new ProductDAO();
    this.testimonialDAO = new TestimonialDAO();
    this.cartManager = CartManager.getInstance();
    this.favoriteDAO = new FavoriteDAO();

    // Set current user in CartManager to load saved cart items
    this.cartManager.setCurrentUser(user);

    setTitle("PetPals - Welcome, " + currentUser.getUsername() + "!");
    // We handle closing manually now to show a confirmation
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    getContentPane().setBackground(Constants.COLOR_BACKGROUND);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        showExitConfirmation();
      }
    });

    initComponents(); // This now sets up the favorites tab structure
    loadInitialData(); // Load data for initially visible tabs
    updateCartButton();
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));

    // --- Top Panel (Same as before) ---
    JPanel topPanel = new JPanel(new BorderLayout(10, 0));
    topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
    topPanel.setBackground(Constants.COLOR_PANEL_BACKGROUND);
    topPanel.setOpaque(true);

    // --- App Name Label with Icon ---
    JLabel appNameLabel = new JLabel("PetPals");
    appNameLabel.setFont(Constants.FONT_HEADING_1.deriveFont(28f));
    appNameLabel.setForeground(Constants.COLOR_PRIMARY);
    appNameLabel.setBorder(new EmptyBorder(0, 0, 0, 10)); // Adjusted left padding later with icon gap
    appNameLabel.setIconTextGap(10); // Space between icon and text

    // Load and set the icon
    try (InputStream iconStream = getClass().getResourceAsStream("/images/app/app_icon.png")) {
      if (iconStream != null) {
        BufferedImage originalImage = ImageIO.read(iconStream);
        // Scale the icon
        int iconSize = 32; // Desired icon height/width
        Image scaledImage = originalImage.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        appNameLabel.setIcon(new ImageIcon(scaledImage));
      } else {
        System.err.println("Warning: App icon not found at /images/app/app_icon.png");
        appNameLabel.setBorder(new EmptyBorder(0, 5, 0, 10)); // Fallback padding if icon fails
      }
    } catch (IOException e) {
      System.err.println("Error loading app icon: " + e.getMessage());
      appNameLabel.setBorder(new EmptyBorder(0, 5, 0, 10)); // Fallback padding on error
    }

    topPanel.add(appNameLabel, BorderLayout.WEST);

    // --- Right Panel (Greeting, Cart, Logout) ---
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    rightPanel.setOpaque(false);

    JLabel greetingLabel = new JLabel("Hi, " + currentUser.getUsername() + "!");
    greetingLabel.setFont(Constants.FONT_NORMAL.deriveFont(14f));
    greetingLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
    rightPanel.add(greetingLabel);

    cartButton = new JButton("Cart (0)");
    cartButton.setFont(Constants.FONT_BUTTON);
    cartButton.setToolTipText("View your shopping cart");
    cartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    cartButton.addActionListener(e -> showCart());
    rightPanel.add(cartButton);

    JButton logoutButton = new JButton("Logout");
    logoutButton.setFont(Constants.FONT_BUTTON);
    logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logoutButton.addActionListener(e -> logout());
    rightPanel.add(logoutButton);

    topPanel.add(rightPanel, BorderLayout.EAST);
    add(topPanel, BorderLayout.NORTH);


    // --- Tabbed Pane for content ---
    tabbedPane = new JTabbedPane();
    tabbedPane.setFont(Constants.FONT_BOLD.deriveFont(13f));
    tabbedPane.setBackground(Constants.COLOR_BACKGROUND);
    tabbedPane.setOpaque(false);

    // --- Tab 1: Pets & Testimonials ---
    JPanel petsAndTestimonialsPanel = createTabPanel();
    // ... (GridBagLayout setup for pets/testimonials sections as before) ...
    GridBagConstraints gbcPT = new GridBagConstraints();
    gbcPT.fill = GridBagConstraints.HORIZONTAL;
    gbcPT.weightx = 1.0;
    gbcPT.anchor = GridBagConstraints.NORTHWEST; // Align content to top-left

    // Pets Section Label
    gbcPT.gridx = 0;
    gbcPT.gridx = 0;
    gbcPT.gridy = 0;
    gbcPT.weighty = 0;
    gbcPT.fill = GridBagConstraints.HORIZONTAL; // Title/Filter row should only fill horizontally
    gbcPT.insets = new Insets(0, 0, 5, 0);

    // --- Pets Title and Filter Row ---
    JPanel petsTitleFilterPanel = new JPanel(new BorderLayout(10, 0));
    petsTitleFilterPanel.setOpaque(false); // Transparent background

    JLabel petsLabel = createSectionLabel("Meet Our Furry Friends");
    petsTitleFilterPanel.add(petsLabel, BorderLayout.WEST);

    // Species Filter ComboBox
    speciesFilterComboBox = new JComboBox<>();
    speciesFilterComboBox.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    speciesFilterComboBox.setPreferredSize(new Dimension(150, speciesFilterComboBox.getPreferredSize().height)); // Give it a reasonable width
    speciesFilterComboBox.setToolTipText("Filter pets by species");
    // Action listener will be added after populating
    petsTitleFilterPanel.add(speciesFilterComboBox, BorderLayout.EAST);

    petsAndTestimonialsPanel.add(petsTitleFilterPanel, gbcPT); // Add the combined panel

    // Pets Section Content (Scroll Pane)
    gbcPT.gridy++; // Move to the next row
    gbcPT.fill = GridBagConstraints.BOTH; // Pet list takes remaining vertical space
    gbcPT.weighty = 0.65;
    gbcPT.insets = new Insets(0, 0, 15, 0);
    petsPanelContainer = new JPanel(new WrapLayout(WrapLayout.LEFT, Constants.CARD_H_GAP, Constants.CARD_V_GAP));
    petsPanelContainer.setBackground(Constants.COLOR_BACKGROUND);
    petsPanelContainer.setOpaque(true);
    JScrollPane petsScrollPane = createScrollPane(petsPanelContainer);
    petsAndTestimonialsPanel.add(petsScrollPane, gbcPT);

    // Testimonials Section Label
    gbcPT.gridy++;
    gbcPT.fill = GridBagConstraints.HORIZONTAL;
    gbcPT.weighty = 0;
    gbcPT.insets = new Insets(0, 0, 5, 0);
    JLabel testimonialsLabel = createSectionLabel("Happy Tails: Adoption Stories");
    petsAndTestimonialsPanel.add(testimonialsLabel, gbcPT);

    // Testimonials Section Content
    gbcPT.gridy++;
    gbcPT.fill = GridBagConstraints.BOTH;
    gbcPT.weighty = 0.35;
    gbcPT.insets = new Insets(0, 0, 0, 0);
    testimonialsPanelContainer = new JPanel();
    testimonialsPanelContainer.setLayout(new BoxLayout(testimonialsPanelContainer, BoxLayout.Y_AXIS));
    testimonialsPanelContainer.setBackground(Constants.COLOR_PANEL_BACKGROUND);
    testimonialsPanelContainer.setOpaque(true);
    testimonialsPanelContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
    JScrollPane testimonialsScrollPane = createScrollPane(testimonialsPanelContainer);
    testimonialsScrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER));
    petsAndTestimonialsPanel.add(testimonialsScrollPane, gbcPT);

    // --- Submit Testimonial Button ---
    gbcPT.gridy++; // Move to the next row below testimonials
    gbcPT.fill = GridBagConstraints.NONE; // Button should not stretch
    gbcPT.anchor = GridBagConstraints.CENTER; // Center the button
    gbcPT.weighty = 0; // No vertical expansion
    gbcPT.insets = new Insets(15, 0, 5, 0); // Add some top/bottom margin

    // Only show the submit button for non-admin users
    if (currentUser != null && !currentUser.isAdmin()) {
        JButton submitTestimonialButton = new JButton("Share Your Adoption Story");
        submitTestimonialButton.setFont(Constants.FONT_BUTTON);
        submitTestimonialButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitTestimonialButton.addActionListener(e -> openSubmitTestimonialDialog());
        petsAndTestimonialsPanel.add(submitTestimonialButton, gbcPT);
    }


    tabbedPane.addTab("Home", petsAndTestimonialsPanel); // Renamed first tab slightly


    // --- Tab 2: Products ---
    JPanel productsTabPanel = createTabPanel();
    // ... (GridBagLayout setup for products section as before) ...
    GridBagConstraints gbcProd = new GridBagConstraints();
    gbcProd.fill = GridBagConstraints.HORIZONTAL;
    gbcProd.weightx = 1.0;
    gbcProd.anchor = GridBagConstraints.NORTHWEST;

    // --- Title and Search Row ---
    gbcProd.gridx = 0;
    gbcProd.gridy = 0;
    gbcProd.weighty = 0; // This row doesn't expand vertically
    gbcProd.fill = GridBagConstraints.HORIZONTAL;
    gbcProd.insets = new Insets(0, 0, 10, 0); // Bottom margin for spacing

    JPanel titleSearchPanel = new JPanel(new BorderLayout(15, 0)); // Panel to hold title and search
    titleSearchPanel.setBackground(Constants.COLOR_BACKGROUND);
    titleSearchPanel.setOpaque(true);

    // Products Section Label (aligned left)
    JLabel productsLabel = createSectionLabel("Pet Supplies & Goodies");
    titleSearchPanel.add(productsLabel, BorderLayout.WEST);

    // Search Panel (aligned right)
    JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
    searchPanel.setBackground(Constants.COLOR_BACKGROUND);
    searchPanel.setOpaque(true);
    searchPanel.setPreferredSize(new Dimension(300, searchPanel.getPreferredSize().height)); // Give search a preferred width

    productSearchField = new JTextField();
    productSearchField.setFont(Constants.FONT_NORMAL);
    productSearchField.setToolTipText("Search products by name or category");
    productSearchField.addActionListener(e -> performProductSearch()); // Trigger search on Enter

    productSearchButton = new JButton("Search");
    productSearchButton.setFont(Constants.FONT_BUTTON);
    productSearchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    productSearchButton.addActionListener(e -> performProductSearch()); // Trigger search on click

    searchPanel.add(productSearchField, BorderLayout.CENTER);
    searchPanel.add(productSearchButton, BorderLayout.EAST);
    titleSearchPanel.add(searchPanel, BorderLayout.EAST); // Add search panel to the right

    productsTabPanel.add(titleSearchPanel, gbcProd); // Add the combined panel


    // --- Products Section Content ---
    gbcProd.gridy++; // Move to the next row
    gbcProd.fill = GridBagConstraints.BOTH; // Product list takes remaining vertical space
    gbcProd.weighty = 1.0;
    gbcProd.insets = new Insets(0, 0, 0, 0);
    productsPanelContainer = new JPanel(new WrapLayout(WrapLayout.LEFT, Constants.CARD_H_GAP, Constants.CARD_V_GAP));
    productsPanelContainer.setBackground(Constants.COLOR_BACKGROUND);
    productsPanelContainer.setOpaque(true);
    productsScrollPane = createScrollPane(productsPanelContainer); // Assign to class field
    productsTabPanel.add(productsScrollPane, gbcProd);

    tabbedPane.addTab("Products", productsTabPanel);


    // --- Tab 3: Favorites ---
    JPanel favoritesTabPanel = createTabPanel(); // Use helper
    GridBagConstraints gbcFav = new GridBagConstraints();
    gbcFav.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
    gbcFav.weightx = 1.0;
    gbcFav.weighty = 1.0; // The scroll pane takes all space

    favoritesPanelContainer = new JPanel(new WrapLayout(WrapLayout.LEFT, Constants.CARD_H_GAP, Constants.CARD_V_GAP));
    favoritesPanelContainer.setBackground(Constants.COLOR_BACKGROUND);
    favoritesPanelContainer.setOpaque(true);
    // Initial message before loading
    favoritesPanelContainer.add(new JLabel("Loading your favorite pets..."));

    JScrollPane favoritesScrollPane = createScrollPane(favoritesPanelContainer);
    favoritesTabPanel.add(favoritesScrollPane, gbcFav); // Add scroll pane to the tab panel

    // Add the tab and store its index
    tabbedPane.addTab("My Favorites", favoritesTabPanel);
    favoritesTabIndex = tabbedPane.indexOfComponent(favoritesTabPanel); // Get index

    // --- Tab 4: Order History ---
    OrderHistoryPanel orderHistoryPanel = new OrderHistoryPanel(currentUser);
    tabbedPane.addTab("Order History", orderHistoryPanel);
    orderHistoryTabIndex = tabbedPane.indexOfComponent(orderHistoryPanel);

    // --- Conditional Tabs: Support & Admin ---
    if (currentUser.isAdmin()) {
      // Admin sees Admin Panel (which will contain Support)
      AdminPanel adminPanel = new AdminPanel(currentUser); // Pass user to AdminPanel
      tabbedPane.addTab("Admin", adminPanel);
      adminTabIndex = tabbedPane.indexOfComponent(adminPanel); // Get index
      supportTabIndex = -1; // Support is inside Admin panel for admins
      // Add specific styling or icon for admin tab if desired
      tabbedPane.setForegroundAt(adminTabIndex, Constants.COLOR_ACCENT);
    } else {
      // Regular user sees Support Panel directly
      SupportPanel supportPanel = new SupportPanel(currentUser);
      tabbedPane.addTab("Support", supportPanel);
      supportTabIndex = tabbedPane.indexOfComponent(supportPanel);
      adminTabIndex = -1; // No admin tab for regular users
    }

    // Add ChangeListener to reload data when relevant tabs are selected
    tabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int selectedIndex = tabbedPane.getSelectedIndex();

        Component selectedComponent = tabbedPane.getSelectedComponent(); // Keep getting the component for other checks

        if (selectedIndex == favoritesTabIndex) { // Correct check: Just use the index for favorites
             loadFavoritePets();
        } else if (selectedComponent instanceof AdminPanel && selectedIndex == adminTabIndex) { // Keep component check for AdminPanel
             // AdminPanel might handle its own internal refreshes now
             // ((AdminPanel) selectedComponent).refreshData(); // Keep if AdminPanel needs top-level refresh trigger
        } else if (selectedComponent instanceof OrderHistoryPanel && selectedIndex == orderHistoryTabIndex) {
             ((OrderHistoryPanel) selectedComponent).loadOrderHistory();
        } else if (selectedComponent instanceof SupportPanel && selectedIndex == supportTabIndex) {
             // Refresh support panel if it's a top-level tab (for regular users)
             // ((SupportPanel) selectedComponent).refreshQueriesTable(); // Example call if needed
        }
        // Add other conditions if needed
      }
    });


    // Add the tabbed pane to the frame's center
    add(tabbedPane, BorderLayout.CENTER);
  }

  // Helper methods (createTabPanel, createSectionLabel, createScrollPane) remain the same
  private JPanel createTabPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(new EmptyBorder(Constants.PANEL_PADDING, Constants.PANEL_PADDING, Constants.PANEL_PADDING, Constants.PANEL_PADDING)); // Padding inside tab
    panel.setBackground(Constants.COLOR_BACKGROUND);
    panel.setOpaque(true);
    return panel;
  }

  private JLabel createSectionLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(Constants.FONT_HEADING_2.deriveFont(18f));
    label.setForeground(Constants.COLOR_TEXT_PRIMARY);
    label.setBorder(new EmptyBorder(5, 0, 5, 0)); // Vertical padding
    return label;
  }

  private JScrollPane createScrollPane(Component view) {
    JScrollPane scrollPane = new JScrollPane(view);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
    scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scrollpane border
    scrollPane.setBackground(Constants.COLOR_BACKGROUND);
    scrollPane.getViewport().setOpaque(false); // Allow panel background to show through viewport
    scrollPane.setOpaque(true);
    return scrollPane;
  }

  // Renamed loadData to loadInitialData to avoid confusion
  private void loadInitialData() {
    // Load data only for the initially visible tabs or essential data
    populateSpeciesFilter(); // Populate filter first
    loadPets(null); // Initial load with no filter (or "All")
    loadTestimonials(); // Load testimonials separately
    loadProducts(null); // Initial load without search term
    // Favorites will be loaded when the tab is selected due to the ChangeListener
    // Admin data is loaded within the AdminPanel itself upon creation/selection

    // Add action listener AFTER populating the combo box
    speciesFilterComboBox.addActionListener(e -> {
        String selectedSpecies = (String) speciesFilterComboBox.getSelectedItem();
        loadPets(selectedSpecies);
    });
  }

  // Method to populate the species filter dropdown
  private void populateSpeciesFilter() {
      SwingUtilities.invokeLater(() -> {
          List<String> species = petDAO.getDistinctPetSpecies();
          speciesFilterComboBox.removeAllItems(); // Clear existing items
          speciesFilterComboBox.addItem("All"); // Add "All" option first
          for (String s : species) {
              speciesFilterComboBox.addItem(s);
          }
          speciesFilterComboBox.setSelectedItem("All"); // Default to "All"
      });
  }


  // Updated loadPets method to handle filtering
  private void loadPets(String speciesFilter) {
      SwingUtilities.invokeLater(() -> {
          // System.out.println("Loading pets with filter: " + speciesFilter); // Debugging
          List<Pet> pets = petDAO.getAllPets(speciesFilter); // Use the new DAO method
          petsPanelContainer.removeAll();
          if (pets.isEmpty()) {
              String message = "No pets available";
              if (speciesFilter != null && !speciesFilter.equalsIgnoreCase("All")) {
                  message += " of species '" + speciesFilter + "'";
              }
              message += ". Check back soon!";
              petsPanelContainer.add(new JLabel(message));
          } else {
              for (Pet pet : pets) {
                  petsPanelContainer.add(new PetPanel(pet, currentUser, favoriteDAO));
              }
          }
          petsPanelContainer.revalidate();
          petsPanelContainer.repaint();
          // Scroll pet scroll pane to top after loading/filtering
          Component parent = petsPanelContainer.getParent(); // Should be JViewport
          if (parent instanceof JViewport) parent = parent.getParent(); // Should be JScrollPane
          if (parent instanceof JScrollPane) {
              JScrollPane scrollPane = (JScrollPane) parent;
              SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
          }
      });
  }

  // Separated loading logic for testimonials - Fetch only APPROVED ones
  private void loadTestimonials() {
      SwingUtilities.invokeLater(() -> {
      // Fetch only approved testimonials
      List<Testimonial> testimonials = testimonialDAO.getAllApprovedTestimonials();
      testimonialsPanelContainer.removeAll();
      if (testimonials.isEmpty()) {
        // Keep the prompt to submit, even if none are approved yet
        testimonialsPanelContainer.add(new JLabel("No adoption stories shared yet. Be the first!"));
      } else {
        for (Testimonial testimonial : testimonials) {
          testimonialsPanelContainer.add(new TestimonialPanel(testimonial));
          testimonialsPanelContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        // Remove last spacer
        if (testimonialsPanelContainer.getComponentCount() > 0) {
          Component last = testimonialsPanelContainer.getComponent(testimonialsPanelContainer.getComponentCount() - 1);
          if (last instanceof Box.Filler) testimonialsPanelContainer.remove(last);
        }
      }
      testimonialsPanelContainer.revalidate();
      testimonialsPanelContainer.repaint();
      // Revalidate scroll pane containing testimonials
      SwingUtilities.invokeLater(() -> {
        Component parent = testimonialsPanelContainer.getParent(); // Should be JViewport
        if (parent instanceof JViewport) parent = parent.getParent(); // Should be JScrollPane
        if (parent instanceof JScrollPane) {
          parent.revalidate();
          parent.repaint();
        }
      });
    });
  }

  // Method to open the submission dialog
  private void openSubmitTestimonialDialog() {
      if (currentUser == null) {
          JOptionPane.showMessageDialog(this, "You must be logged in to submit a testimonial.", "Login Required", JOptionPane.WARNING_MESSAGE);
          return;
      }
      // Pass the current user's ID to the dialog
      SubmitTestimonialDialog dialog = new SubmitTestimonialDialog(this, currentUser.getId());
      dialog.setVisible(true);
      // Optionally, reload testimonials after submission attempt (though it needs approval)
      // loadTestimonials(); // Might not show immediately as it needs approval
  }

  // Updated loadProducts to accept a search term
  private void loadProducts(String searchTerm) {
    SwingUtilities.invokeLater(() -> {
      List<Product> products;
      if (searchTerm != null && !searchTerm.trim().isEmpty()) {
        products = productDAO.searchProducts(searchTerm.trim());
        System.out.println("Searching products for: '" + searchTerm.trim() + "', found: " + products.size());
      } else {
        products = productDAO.getAllProducts();
        System.out.println("Loading all products, found: " + products.size());
      }

      productsPanelContainer.removeAll(); // Clear previous results

      if (products.isEmpty()) {
        String message = (searchTerm != null && !searchTerm.trim().isEmpty())
            ? "No products found matching '" + searchTerm.trim() + "'."
            : "No products available right now.";
        productsPanelContainer.add(new JLabel(message));
      } else {
        for (Product product : products) {
          productsPanelContainer.add(new ProductPanel(product, this::updateCartButton)); // Pass cart update callback
        }
      }
      // Revalidate and repaint the container AND scroll back to top
      productsPanelContainer.revalidate();
      productsPanelContainer.repaint();
      SwingUtilities.invokeLater(() -> productsScrollPane.getVerticalScrollBar().setValue(0)); // Scroll to top
    });
  }

  // Action method for product search
  private void performProductSearch() {
    String searchTerm = productSearchField.getText();
    loadProducts(searchTerm); // Reload products with the search term
  }


  // Method to load/refresh the favorite pets tab
  private void loadFavoritePets() {
    System.out.println("Executing loadFavoritePets for user: " + currentUser.getId());
    // Ensure execution on the EDT
    SwingUtilities.invokeLater(() -> {
      // 1. Get favorite pet IDs from DAO
      Set<Integer> favoritePetIds = favoriteDAO.getFavoritePetIds(currentUser.getId());
      System.out.println("Found " + favoritePetIds.size() + " favorite pet IDs.");

      // Clear the panel before adding new items or the empty message
      favoritesPanelContainer.removeAll(); // Crucial: Clear previous content

      if (favoritePetIds.isEmpty()) {
        // 2a. If no favorites, display a message
        JLabel emptyLabel = new JLabel("<html><center>You haven't added any pets to your favorites yet.<br>Click 'View Details' on a pet and use the ♡ button!</center></html>");
        emptyLabel.setFont(Constants.FONT_NORMAL.deriveFont(14f));
        emptyLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Add to a wrapper panel to center it within the WrapLayout's scroll pane
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Constants.COLOR_BACKGROUND);
        wrapper.setOpaque(true);
        wrapper.add(emptyLabel);
        favoritesPanelContainer.add(wrapper); // Add the wrapper
        System.out.println("Displaying empty favorites message.");

      } else {
        // 2b. If favorites exist, fetch Pet details from PetDAO
        List<Pet> favoritePets = petDAO.getPetsByIds(favoritePetIds);
        System.out.println("Fetched details for " + favoritePets.size() + " favorite pets.");

        if (favoritePets.isEmpty() && !favoritePetIds.isEmpty()) {
          // Indicates favorite IDs exist but pets couldn't be fetched (maybe deleted?)
          JLabel errorLabel = new JLabel("Could not load details for some favorite pets. They may no longer be available.");
          errorLabel.setForeground(Constants.COLOR_ERROR);
          favoritesPanelContainer.add(errorLabel);
          System.err.println("Warning: Favorite IDs found but Pet details were empty.");
        } else {
          // 3. Create PetPanel for each favorite pet and add to the container
          for (Pet pet : favoritePets) {
            // Reuse the PetPanel, passing the necessary data
            favoritesPanelContainer.add(new PetPanel(pet, currentUser, favoriteDAO));
          }
          System.out.println("Added PetPanels for favorites.");
        }
      }

      // 4. Revalidate and repaint the container to show changes
      favoritesPanelContainer.revalidate();
      favoritesPanelContainer.repaint();
      System.out.println("Favorites panel revalidated.");

      // Also revalidate the parent scroll pane viewport, especially after clearing/adding
      Component parent = favoritesPanelContainer.getParent();
      if (parent instanceof JViewport) {
        parent.revalidate();
        parent.repaint();
      }
    });
  }


  private void showCart() {
    CartDialog cartDialog = new CartDialog(this, this::updateCartButton);
    cartDialog.setVisible(true);
    // After cart dialog closes, potentially refresh product list if stock was managed?
    // loadProducts(productSearchField.getText()); // Example refresh
  }

  private void updateCartButton() {
    SwingUtilities.invokeLater(() -> {
      int itemCount = cartManager.getTotalItemCount();
      cartButton.setText("Cart (" + itemCount + ")");
      cartButton.setForeground(itemCount > 0 ? Constants.COLOR_ACCENT : Constants.COLOR_TEXT_PRIMARY);
      cartButton.setFont(itemCount > 0 ? Constants.FONT_BOLD : Constants.FONT_BUTTON);
    });
  }

  private void logout() {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      AuthPreferences.clearLoginSession();
      dispose();
      // Restart the application by calling the main method again
      SwingUtilities.invokeLater(() -> Main.main(new String[]{}));
    }
  }

  private void showExitConfirmation() {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to exit PetPals?",
        "Confirm Exit",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE); // Use warning icon

    if (confirm == JOptionPane.YES_OPTION) {
      // Perform any cleanup if needed before exiting
      DatabaseConnection.closeConnection(); // Ensure DB connection is closed cleanly
      dispose(); // Close the window
      System.exit(0); // Terminate the application
    }
    // If NO_OPTION or dialog closed, do nothing, window stays open.
  }
}
// --- END OF FILE HomePage.java ---
