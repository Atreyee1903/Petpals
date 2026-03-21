package org.petpals.ui.admin;

import org.petpals.db.PetDAO;
import org.petpals.model.Pet;
import org.petpals.ui.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class PetManagementPanel extends JPanel {

  private final PetDAO petDAO;
  private final AdminPanel parentAdminPanel; // To access parent frame for dialogs
  private JTable petTable;
  private DefaultTableModel tableModel;

  public PetManagementPanel(PetDAO petDAO, AdminPanel parentAdminPanel) {
    this.petDAO = petDAO;
    this.parentAdminPanel = parentAdminPanel;
    initComponents();
    loadPetsData();
  }

  private void initComponents() {
    setLayout(new BorderLayout(10, 10));
    setBackground(Constants.COLOR_BACKGROUND);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    // --- Button Panel (Top) ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setOpaque(false);

    JButton addButton = new JButton("Add Pet");
    addButton.setIcon(UIManager.getIcon("Tree.leafIcon")); // Example Icon
    addButton.addActionListener(e -> addPet());
    buttonPanel.add(addButton);

    JButton editButton = new JButton("Edit Selected Pet");
    editButton.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon")); // Example Icon
    editButton.addActionListener(e -> editPet());
    buttonPanel.add(editButton);

    JButton deleteButton = new JButton("Delete Selected Pet");
    deleteButton.setForeground(Constants.COLOR_ERROR);
    deleteButton.setIcon(UIManager.getIcon("InternalFrame.closeIcon")); // Example Icon
    deleteButton.addActionListener(e -> deletePet());
    buttonPanel.add(deleteButton);

    add(buttonPanel, BorderLayout.NORTH);

    // --- Table (Center) ---
    String[] columnNames = {"ID", "Name", "Species", "Breed", "Age", "Image", "Location", "Traits"};
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Table not directly editable
      }
    };
    petTable = new JTable(tableModel);
    petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    petTable.setAutoCreateRowSorter(true); // Allow sorting
    petTable.getTableHeader().setFont(Constants.FONT_BOLD);
    petTable.setFillsViewportHeight(true);
    petTable.setRowHeight(25);

    // Set preferred column widths (adjust as needed)
    TableColumnModel columnModel = petTable.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(40);  // ID
    columnModel.getColumn(1).setPreferredWidth(150); // Name
    columnModel.getColumn(2).setPreferredWidth(80);  // Species
    columnModel.getColumn(3).setPreferredWidth(120); // Breed
    columnModel.getColumn(4).setPreferredWidth(60);  // Age
    columnModel.getColumn(5).setPreferredWidth(120); // Image
    columnModel.getColumn(6).setPreferredWidth(150); // Location
    columnModel.getColumn(7).setPreferredWidth(200); // Traits


    JScrollPane scrollPane = new JScrollPane(petTable);
    scrollPane.setBorder(BorderFactory.createLineBorder(Constants.COLOR_BORDER));
    add(scrollPane, BorderLayout.CENTER);
  }

  public void loadPetsData() {
    // Ensure UI updates are on the EDT
    SwingUtilities.invokeLater(() -> {
      tableModel.setRowCount(0); // Clear existing data
      List<Pet> pets = petDAO.getAllPets(null);
      for (Pet pet : pets) {
        tableModel.addRow(new Object[]{
            pet.getId(),
            pet.getName(),
            pet.getSpecies(),
            pet.getBreed(),
            pet.getAge(),
            pet.getImage(),
            pet.getLocation(),
            String.join(", ", pet.getTraits()) // Display traits nicely
        });
      }
    });
  }

  private void addPet() {
    PetFormDialog dialog = new PetFormDialog(parentAdminPanel.getParentFrame(), petDAO, null); // null pet for adding
    dialog.setVisible(true);
    if (dialog.isSaved()) {
      loadPetsData(); // Refresh table if saved
    }
  }

  private void editPet() {
    int selectedRow = petTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Please select a pet to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Get ID from the *model* row, considering sorting
    int modelRow = petTable.convertRowIndexToModel(selectedRow);
    int petId = (int) tableModel.getValueAt(modelRow, 0); // Assuming ID is in column 0

    Pet petToEdit = petDAO.getPetById(petId); // Fetch the full pet object
    if (petToEdit == null) {
      JOptionPane.showMessageDialog(this, "Could not find pet details (ID: " + petId + "). It might have been deleted.", "Error", JOptionPane.ERROR_MESSAGE);
      loadPetsData(); // Refresh in case it was deleted
      return;
    }

    PetFormDialog dialog = new PetFormDialog(parentAdminPanel.getParentFrame(), petDAO, petToEdit); // Pass pet for editing
    dialog.setVisible(true);

    if (dialog.isSaved()) {
      loadPetsData(); // Refresh table if saved
    }
  }

  private void deletePet() {
    int selectedRow = petTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Please select a pet to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    int modelRow = petTable.convertRowIndexToModel(selectedRow);
    int petId = (int) tableModel.getValueAt(modelRow, 0);
    String petName = (String) tableModel.getValueAt(modelRow, 1);

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to permanently delete '" + petName + "' (ID: " + petId + ")?\nThis action cannot be undone.",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      boolean deleted = petDAO.deletePet(petId);
      if (deleted) {
        JOptionPane.showMessageDialog(this, "'" + petName + "' deleted successfully.", "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
        loadPetsData(); // Refresh table
      } else {
        JOptionPane.showMessageDialog(this, "Failed to delete '" + petName + "'. Check logs for details.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
