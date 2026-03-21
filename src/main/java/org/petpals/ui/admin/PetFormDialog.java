package org.petpals.ui.admin;

import org.petpals.db.PetDAO;
import org.petpals.model.Pet;
import org.petpals.ui.Constants;

import org.petpals.db.PetDAO;
import org.petpals.model.Pet;
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


public class PetFormDialog extends JDialog {

  private final PetDAO petDAO;
  private final Pet petToEdit; // Null if adding new pet
  private boolean saved = false;

  private JTextField nameField;
  private JComboBox<String> speciesComboBox;
  private JTextField breedField;
  private JTextField ageField;
  // private JTextField imageField; // Replaced by imagePathField and browseButton
  private JTextField imagePathField; // Displays selected image filename
  private JButton browseButton;
  private File selectedImageFile; // Holds the selected image file object
  private JTextArea descriptionArea;
  private JTextField locationField;
  private JTextField traitsField; // Comma-separated

  public PetFormDialog(Frame owner, PetDAO petDAO, Pet petToEdit) {
    super(owner, (petToEdit == null ? "Add New Pet" : "Edit Pet: " + petToEdit.getName()), true);
    this.petDAO = petDAO;
    this.petToEdit = petToEdit;
    initComponents();
    if (petToEdit != null) {
      populateFields();
    }
    pack();
    setMinimumSize(new Dimension(450, 500)); // Ensure minimum size
    setLocationRelativeTo(owner);
  }

  private void initComponents() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    mainPanel.setBackground(Constants.COLOR_BACKGROUND);

    // --- Form Panel (GridBagLayout) ---
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Labels
    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.gridy = 0;
    formPanel.add(new JLabel("Name*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Species*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Breed*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Age*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Image*:"), gbc); // Changed label
    gbc.gridy++;
    formPanel.add(new JLabel("Location*:"), gbc);
    gbc.gridy++;
    formPanel.add(new JLabel("Traits (comma-sep):"), gbc);
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    formPanel.add(new JLabel("Description:"), gbc);

    // Fields
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST; // Reset anchor

    gbc.gridy = 0;
    nameField = new JTextField(25);
    formPanel.add(nameField, gbc);
    gbc.gridy++;
    // Use species values consistent with the Pet model/database enum
    speciesComboBox = new JComboBox<>(new String[]{"DOG", "CAT", "BIRD"});
    formPanel.add(speciesComboBox, gbc);
    gbc.gridy++;
    breedField = new JTextField();
    formPanel.add(breedField, gbc);
    gbc.gridy++;
    ageField = new JTextField();
    formPanel.add(ageField, gbc);

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

    gbc.gridy++;
    locationField = new JTextField();
    formPanel.add(locationField, gbc);
    gbc.gridy++;
    traitsField = new JTextField();
    traitsField.setToolTipText("e.g., playful, friendly, house-trained");
    formPanel.add(traitsField, gbc);

    // Description Text Area
    gbc.gridy++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1.0; // Allow description to grow
    descriptionArea = new JTextArea(5, 25);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER));
    formPanel.add(descScrollPane, gbc);

    mainPanel.add(formPanel, BorderLayout.CENTER);


    // --- Button Panel ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setOpaque(false);

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> savePet());
    buttonPanel.add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    buttonPanel.add(cancelButton);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(saveButton);

    getContentPane().add(mainPanel);
  }

  private void populateFields() {
    nameField.setText(petToEdit.getName());
    speciesComboBox.setSelectedItem(petToEdit.getSpecies());
    breedField.setText(petToEdit.getBreed());
    ageField.setText(petToEdit.getAge());
    imagePathField.setText(petToEdit.getImage()); // Set the path field
    // selectedImageFile remains null until user browses for a new file
    descriptionArea.setText(petToEdit.getDescription());
    locationField.setText(petToEdit.getLocation());
    traitsField.setText(String.join(", ", petToEdit.getTraits()));
  }

  private boolean validateInput() {
    if (nameField.getText().trim().isEmpty() ||
        breedField.getText().trim().isEmpty() ||
        ageField.getText().trim().isEmpty() ||
        imagePathField.getText().trim().isEmpty() || // Check the path field
        locationField.getText().trim().isEmpty() ||
        speciesComboBox.getSelectedItem() == null) {
      JOptionPane.showMessageDialog(this, "Please fill in all required fields (*), including selecting an image.", "Validation Error", JOptionPane.WARNING_MESSAGE); // Updated message
      return false;
    }
    // Add more specific validation if needed (e.g., image format)
    return true;
  }

  private void savePet() {
    if (!validateInput()) {
      return;
    }

    String name = nameField.getText().trim();
    String species = (String) speciesComboBox.getSelectedItem();
    String breed = breedField.getText().trim();
    String age = ageField.getText().trim();
    // String image = imageField.getText().trim(); // Old way
    String imageFilename = imagePathField.getText().trim(); // Get filename from the display field
    String description = descriptionArea.getText().trim();
    String location = locationField.getText().trim();
    String traitsString = traitsField.getText().trim();

    // --- Handle Image Copying ---
    if (selectedImageFile != null) { // A new image file was selected
      try {
        // Define target directory within the project structure
        Path targetDir = Paths.get("src", "main", "resources", "images", "pets");
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
    } else if (petToEdit == null) {
        // If adding a new pet and no image was selected, validation should have caught this.
        // But as a safeguard:
         JOptionPane.showMessageDialog(this, "Please select an image file.", "Validation Error", JOptionPane.WARNING_MESSAGE);
         return;
    }
    // If editing and no new file selected, imageFilename already holds the existing filename.
    // --- End Handle Image Copying ---


    // Use the existing ID if editing, otherwise ID is handled by DB auto-increment
    int petId = (petToEdit != null) ? petToEdit.getId() : 0;

    // Create a new Pet object with the form data (using the potentially updated imageFilename)
    Pet pet = new Pet(petId, name, species, breed, age, imageFilename, description, location, traitsString);


    boolean success;
    if (petToEdit == null) { // Adding new pet
      success = petDAO.addPet(pet);
    } else { // Updating existing pet
      success = petDAO.updatePet(pet);
    }

    if (success) {
      saved = true;
      dispose(); // Close dialog on successful save
    } else {
      JOptionPane.showMessageDialog(this,
          "Failed to save pet details. Please check the logs.",
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

    // Set initial directory (optional, could be user's home or last used)
    // fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      selectedImageFile = fileChooser.getSelectedFile();
      imagePathField.setText(selectedImageFile.getName()); // Display only the filename
    }
  }
  // --- End new method ---
}
