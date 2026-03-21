package org.petpals.ui;

import org.petpals.db.FavoriteDAO;
import org.petpals.model.Pet;
import org.petpals.model.User;
import org.petpals.ui.components.ImageLabel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class PetPanel extends JPanel {

  private final Pet pet;
  private final User currentUser; // Store current user
  private final FavoriteDAO favoriteDAO; // Store DAO instance
  private JButton toggleFavoriteButton; // Reference to the button in the dialog
  private boolean isCurrentlyFavorite; // State variable for the dialog

  // Updated constructor
  public PetPanel(Pet pet, User currentUser, FavoriteDAO favoriteDAO) {
    this.pet = pet;
    this.currentUser = currentUser; // Receive user
    this.favoriteDAO = favoriteDAO; // Receive DAO
    initComponents();
  }

  // ... (initComponents method for the main card remains largely the same) ...
  private void initComponents() {
    setLayout(new BorderLayout(8, 8)); // Gaps between components

    // Card-like appearance
    setBackground(Constants.COLOR_PANEL_BACKGROUND);
    setOpaque(true);
    Border line = BorderFactory.createLineBorder(Constants.COLOR_BORDER, 1, true); // Rounded light grey border
    Border padding = BorderFactory.createEmptyBorder(12, 12, 12, 12); // Inner padding
    setBorder(new CompoundBorder(line, padding));

    setPreferredSize(new Dimension(230, 320)); // Slightly adjusted preferred size
    setMaximumSize(new Dimension(260, 350)); // Control max size in WrapLayout

    // --- Image Panel (Top) ---
    JPanel imagePanel = new JPanel(new BorderLayout());
    imagePanel.setOpaque(false); // Inherit background
    ImageLabel imageLabel = new ImageLabel();
    int imgWidth = 190;
    int imgHeight = 150;
    imageLabel.loadImage("resources/images/pets/", pet.getImage(), imgWidth, imgHeight);
    imagePanel.add(imageLabel, BorderLayout.CENTER);
    imagePanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Space below image
    add(imagePanel, BorderLayout.NORTH);

    // --- Details Panel (Center) ---
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setOpaque(false); // Inherit background

    JLabel nameLabel = new JLabel(pet.getName());
    nameLabel.setFont(Constants.FONT_BOLD.deriveFont(16f));
    nameLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel speciesLabel = new JLabel(pet.getSpecies());
    speciesLabel.setFont(Constants.FONT_ITALIC.deriveFont(13f));
    speciesLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
    speciesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel breedAgeLabel = new JLabel(pet.getBreed() + " • " + pet.getAge()); // Use bullet separator
    breedAgeLabel.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    breedAgeLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
    breedAgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    detailsPanel.add(nameLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
    detailsPanel.add(speciesLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
    detailsPanel.add(breedAgeLabel);
    detailsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space below details

    add(detailsPanel, BorderLayout.CENTER);

    // --- Button Panel (Bottom) ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.setOpaque(false); // Inherit background

    JButton detailsButton = new JButton("View Details");
    detailsButton.setFont(Constants.FONT_BUTTON.deriveFont(13f));
    detailsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    detailsButton.putClientProperty("JButton.buttonType", "roundRect"); // Hint for FlatLaf
    detailsButton.addActionListener(e -> showPetDetails());
    buttonPanel.add(detailsButton);

    add(buttonPanel, BorderLayout.SOUTH);
  }


  private void showPetDetails() {
    JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pet Details: " + pet.getName(), true);
    detailsDialog.setMinimumSize(new Dimension(500, 600));
    detailsDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
    detailsDialog.setLayout(new BorderLayout(10, 10));
    detailsDialog.getContentPane().setBackground(Constants.COLOR_BACKGROUND);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
    mainPanel.setOpaque(false);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // --- Top Panel: Image, Basic Info, and Favorite Button ---
    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.setOpaque(false);
    GridBagConstraints gbcTop = new GridBagConstraints();
    gbcTop.insets = new Insets(5, 5, 5, 5);
    gbcTop.anchor = GridBagConstraints.NORTHWEST;

    // Image
    ImageLabel petImage = new ImageLabel();
    petImage.loadImage("resources/images/pets/", pet.getImage(), 200, 200);
    gbcTop.gridx = 0;
    gbcTop.gridy = 0;
    gbcTop.gridheight = 5;
    gbcTop.weighty = 0;
    gbcTop.insets = new Insets(5, 5, 5, 20);
    topPanel.add(petImage, gbcTop);

    // Basic Info Labels
    gbcTop.gridx = 1;
    gbcTop.gridheight = 1;
    gbcTop.weightx = 1.0;
    gbcTop.fill = GridBagConstraints.HORIZONTAL;
    gbcTop.insets = new Insets(5, 0, 5, 0);

    JLabel nameLabel = new JLabel(pet.getName());
    nameLabel.setFont(Constants.FONT_HEADING_2.deriveFont(22f));
    nameLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    gbcTop.gridy = 0;
    topPanel.add(nameLabel, gbcTop);

    JLabel speciesBreedLabel = new JLabel(pet.getSpecies() + " - " + pet.getBreed());
    speciesBreedLabel.setFont(Constants.FONT_ITALIC.deriveFont(15f));
    speciesBreedLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
    gbcTop.gridy++;
    topPanel.add(speciesBreedLabel, gbcTop);

    JLabel ageLabel = new JLabel("Age: " + pet.getAge());
    ageLabel.setFont(Constants.FONT_NORMAL.deriveFont(15f));
    ageLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    gbcTop.gridy++;
    topPanel.add(ageLabel, gbcTop);

    JLabel locationLabel = new JLabel("Location: " + pet.getLocation());
    locationLabel.setFont(Constants.FONT_NORMAL.deriveFont(15f));
    locationLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    gbcTop.gridy++;
    topPanel.add(locationLabel, gbcTop);

    // --- Favorite Button ---
    gbcTop.gridy++;
    gbcTop.fill = GridBagConstraints.NONE; // Don't stretch button
    gbcTop.anchor = GridBagConstraints.NORTHWEST; // Align button top-left in its cell
    gbcTop.weighty = 1.0; // Push button up if space allows
    gbcTop.insets = new Insets(15, 0, 5, 0); // Add top margin

    toggleFavoriteButton = new JButton(); // Initialize class field
    toggleFavoriteButton.setFont(Constants.FONT_BUTTON.deriveFont(12f));
    toggleFavoriteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    // Check initial favorite state
    isCurrentlyFavorite = favoriteDAO.isFavorite(currentUser.getId(), pet.getId());
    updateFavoriteButtonState(); // Set initial text/icon

    toggleFavoriteButton.addActionListener(e -> toggleFavoriteStatus());
    topPanel.add(toggleFavoriteButton, gbcTop);


    mainPanel.add(topPanel, BorderLayout.NORTH);

    // --- Center Panel: Traits and Description ---
    // ... (Center Panel code remains the same) ...
    JPanel centerPanel = new JPanel(new GridBagLayout());
    centerPanel.setOpaque(false);
    GridBagConstraints gbcCenter = new GridBagConstraints();
    gbcCenter.anchor = GridBagConstraints.NORTHWEST;
    gbcCenter.fill = GridBagConstraints.HORIZONTAL;
    gbcCenter.weightx = 1.0;

    // Traits
    JLabel traitsTitleLabel = new JLabel("Personality & Traits:");
    traitsTitleLabel.setFont(Constants.FONT_BOLD.deriveFont(14f));
    traitsTitleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    gbcCenter.gridx = 0;
    gbcCenter.gridy = 0;
    gbcCenter.insets = new Insets(10, 0, 2, 0); // Top margin
    centerPanel.add(traitsTitleLabel, gbcCenter);

    JLabel traitsLabel = new JLabel("<html><p style='width:300px;'>" + String.join(", ", pet.getTraits()) + "</p></html>"); // Wrap text
    traitsLabel.setFont(Constants.FONT_NORMAL.deriveFont(14f));
    traitsLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
    gbcCenter.gridy++;
    gbcCenter.insets = new Insets(0, 5, 10, 0); // Indent traits list, bottom margin
    centerPanel.add(traitsLabel, gbcCenter);

    // Description
    JLabel descTitleLabel = new JLabel("About " + pet.getName() + ":");
    descTitleLabel.setFont(Constants.FONT_BOLD.deriveFont(14f));
    descTitleLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    gbcCenter.gridy++;
    gbcCenter.insets = new Insets(10, 0, 2, 0); // Reset insets, top margin
    centerPanel.add(descTitleLabel, gbcCenter);

    JTextArea descArea = new JTextArea(pet.getDescription());
    descArea.setWrapStyleWord(true);
    descArea.setLineWrap(true);
    descArea.setEditable(false);
    descArea.setFont(Constants.FONT_NORMAL.deriveFont(14f));
    descArea.setForeground(Constants.COLOR_TEXT_SECONDARY);
    descArea.setBackground(Constants.COLOR_BACKGROUND); // Match dialog background
    descArea.setOpaque(true);
    JScrollPane descScrollPane = new JScrollPane(descArea);
    descScrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER)); // Border on scrollpane
    gbcCenter.gridy++;
    gbcCenter.fill = GridBagConstraints.BOTH;
    gbcCenter.weighty = 1.0; // Allow description to take vertical space
    gbcCenter.insets = new Insets(0, 0, 10, 0); // Bottom margin
    centerPanel.add(descScrollPane, gbcCenter);

    mainPanel.add(centerPanel, BorderLayout.CENTER);


    // --- Contact Panel ---
    // ... (Contact Panel code remains the same) ...
    JPanel contactPanel = new JPanel(new GridBagLayout());
    contactPanel.setOpaque(false);
    contactPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Constants.COLOR_BORDER),
        " Contact Information ", // Title with spaces for padding
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        Constants.FONT_BOLD,
        Constants.COLOR_PRIMARY
    ));
    GridBagConstraints gbcContact = new GridBagConstraints();
    gbcContact.insets = new Insets(4, 8, 4, 8); // Padding inside contact panel
    gbcContact.anchor = GridBagConstraints.WEST;

    String organizationName = pet.getLocation();
    String email = organizationName.toLowerCase().replace(" ", "").replace("-", "") + "@petpals-example.org";
    String phone = "+91 " + (70000 + Math.abs(organizationName.hashCode() % 30000)) + " " +
        (10000 + Math.abs((organizationName.hashCode() / 100) % 90000));

    // Labels
    gbcContact.gridx = 0;
    gbcContact.gridy = 0;
    gbcContact.fill = GridBagConstraints.NONE;
    gbcContact.weightx = 0;
    contactPanel.add(createContactLabel("Organization:"), gbcContact);
    gbcContact.gridy++;
    contactPanel.add(createContactLabel("Email:"), gbcContact);
    gbcContact.gridy++;
    contactPanel.add(createContactLabel("Phone:"), gbcContact);

    // Values
    gbcContact.gridx = 1;
    gbcContact.gridy = 0;
    gbcContact.fill = GridBagConstraints.HORIZONTAL;
    gbcContact.weightx = 1.0;
    contactPanel.add(createContactValue(organizationName), gbcContact);
    gbcContact.gridy++;
    contactPanel.add(createContactValue(email), gbcContact); // Add mailto: link potentially later
    gbcContact.gridy++;
    contactPanel.add(createContactValue(phone), gbcContact);

    mainPanel.add(contactPanel, BorderLayout.SOUTH);


    // --- Bottom Button Panel ---
    JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomButtonPanel.setOpaque(false);
    bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    JButton closeButton = new JButton("Close");
    closeButton.setFont(Constants.FONT_BUTTON);
    closeButton.addActionListener(e -> detailsDialog.dispose());
    bottomButtonPanel.add(closeButton);

    detailsDialog.add(mainPanel, BorderLayout.CENTER);
    detailsDialog.add(bottomButtonPanel, BorderLayout.SOUTH);

    detailsDialog.pack();
    detailsDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
    detailsDialog.setVisible(true);
  }

  // Action handler for the favorite button
  private void toggleFavoriteStatus() {
    boolean success;
    if (isCurrentlyFavorite) {
      // Try to remove
      success = favoriteDAO.removeFavorite(currentUser.getId(), pet.getId());
      if (success) {
        isCurrentlyFavorite = false;
      } else {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
            "Could not remove favorite. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      // Try to add
      success = favoriteDAO.addFavorite(currentUser.getId(), pet.getId());
      if (success) {
        isCurrentlyFavorite = true;
      } else {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
            "Could not add favorite. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    // Update button appearance regardless of success to reflect intended state if DB fails
    updateFavoriteButtonState();
  }

  // Updates the favorite button's text, icon, and tooltip
  private void updateFavoriteButtonState() {
    if (isCurrentlyFavorite) {
      toggleFavoriteButton.setText("Remove Favorite");
      toggleFavoriteButton.setIcon(loadIcon("heart_filled.png"));
      toggleFavoriteButton.setToolTipText("Remove " + pet.getName() + " from your favorites");
      // Optional: Change button style for "remove" action
      toggleFavoriteButton.setBackground(null); // Use default L&F color
      toggleFavoriteButton.setForeground(Constants.COLOR_ERROR); // Red text?
    } else {
      toggleFavoriteButton.setText("Add to Favorites");
      toggleFavoriteButton.setIcon(loadIcon("heart_outline.png"));
      toggleFavoriteButton.setToolTipText("Add " + pet.getName() + " to your favorites");
      // Optional: Style as a positive action
      toggleFavoriteButton.setBackground(Constants.COLOR_SUCCESS); // Green background?
      toggleFavoriteButton.setForeground(Color.WHITE);
    }
    // Ensure icon text gap is reasonable
    toggleFavoriteButton.setIconTextGap(6);
  }

  // Helper method to load icons safely
  private ImageIcon loadIcon(String iconName) {
    URL imgUrl = getClass().getResource("/images/icons/" + iconName);
    if (imgUrl != null) {
      // Optionally resize if needed:
      // Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
      // return new ImageIcon(image);
      return new ImageIcon(imgUrl);
    } else {
      System.err.println("Icon resource not found: /images/icons/" + iconName);
      return null; // Return null if icon not found
    }
  }


  // Helper for consistent contact labels
  private JLabel createContactLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    label.setForeground(Constants.COLOR_TEXT_SECONDARY);
    return label;
  }

  // Helper for consistent contact values
  private JLabel createContactValue(String text) {
    JLabel label = new JLabel(text);
    label.setFont(Constants.FONT_BOLD.deriveFont(13f));
    label.setForeground(Constants.COLOR_TEXT_PRIMARY);
    return label;
  }
}
