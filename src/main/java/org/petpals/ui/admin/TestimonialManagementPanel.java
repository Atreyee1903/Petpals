package org.petpals.ui.admin;

import org.petpals.db.TestimonialDAO;
import org.petpals.model.Testimonial;
import org.petpals.ui.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TestimonialManagementPanel extends JPanel {

  private final TestimonialDAO testimonialDAO;
  private JPanel testimonialsListPanel;

  private JComboBox<String> statusFilterComboBox; // Added for filtering

  public TestimonialManagementPanel() {
    this.testimonialDAO = new TestimonialDAO();
    initComponents();
    loadTestimonials(); // Changed method name
  }

  private void initComponents() {
    setLayout(new BorderLayout(10, 10));
    setBackground(Constants.COLOR_BACKGROUND);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    // --- Top Panel with Title and Filter ---
    JPanel topPanel = new JPanel(new BorderLayout(10, 5));
    topPanel.setBackground(Constants.COLOR_BACKGROUND);

    JLabel titleLabel = new JLabel("Manage Testimonials");
    titleLabel.setFont(Constants.FONT_HEADING_2.deriveFont(20f));
    titleLabel.setForeground(Constants.COLOR_PRIMARY);
    topPanel.add(titleLabel, BorderLayout.WEST);

    // Status Filter
    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    filterPanel.setOpaque(false);
    filterPanel.add(new JLabel("Filter by Status:"));
    statusFilterComboBox = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
    statusFilterComboBox.setFont(Constants.FONT_NORMAL);
    statusFilterComboBox.addActionListener(e -> loadTestimonials()); // Reload when filter changes
    filterPanel.add(statusFilterComboBox);
    topPanel.add(filterPanel, BorderLayout.EAST);

    add(topPanel, BorderLayout.NORTH); // Add the combined top panel

    testimonialsListPanel = new JPanel();
    testimonialsListPanel.setLayout(new BoxLayout(testimonialsListPanel, BoxLayout.Y_AXIS));
    testimonialsListPanel.setBackground(Constants.COLOR_BACKGROUND);

    JScrollPane scrollPane = new JScrollPane(testimonialsListPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getVerticalScrollBar().setUnitIncrement(18);
    add(scrollPane, BorderLayout.CENTER);
  }

  // Renamed and updated to load all or filtered testimonials
  private void loadTestimonials() {
    testimonialsListPanel.removeAll();
    String selectedStatusFilter = (String) statusFilterComboBox.getSelectedItem();
    List<Testimonial> testimonials;

    // Fetch based on filter
    if ("Pending".equalsIgnoreCase(selectedStatusFilter)) {
      testimonials = testimonialDAO.getPendingTestimonials();
    } else if ("Approved".equalsIgnoreCase(selectedStatusFilter)) {
      testimonials = testimonialDAO.getAllApprovedTestimonials(); // Assuming this exists or filter manually
    } else if ("Rejected".equalsIgnoreCase(selectedStatusFilter)) {
      // Need a getRejected or filter manually from getAll
      testimonials = testimonialDAO.getAllTestimonials().stream()
          .filter(t -> t.getStatus() == Testimonial.Status.REJECTED)
          .toList();
    } else { // "All"
      testimonials = testimonialDAO.getAllTestimonials();
    }


    if (testimonials.isEmpty()) {
      String message = "No testimonials found";
      if (!"All".equalsIgnoreCase(selectedStatusFilter)) {
        message += " with status '" + selectedStatusFilter + "'";
      }
      message += ".";
      JLabel noTestimonialsLabel = new JLabel(message);
      noTestimonialsLabel.setFont(Constants.FONT_NORMAL.deriveFont(14f));
      noTestimonialsLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
      testimonialsListPanel.add(noTestimonialsLabel);
    } else {
      for (Testimonial t : testimonials) {
        JPanel testimonialPanel = createTestimonialPanel(t);
        testimonialsListPanel.add(testimonialPanel);
        testimonialsListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
      }
    }

    testimonialsListPanel.revalidate();
    testimonialsListPanel.repaint();
  }

  private JPanel createTestimonialPanel(Testimonial t) {
    // Removed duplicated line above
    JPanel panel = new JPanel(new BorderLayout(10, 5)); // Reduced vertical gap
    panel.setBackground(Constants.COLOR_PANEL_BACKGROUND);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Constants.COLOR_BORDER),
        new EmptyBorder(5, 5, 5, 5) // Add internal padding
    ));

    JTextArea textArea = new JTextArea(t.getText());
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    textArea.setBackground(Constants.COLOR_PANEL_BACKGROUND);
    // textArea.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding handled by panel border

    // --- Info Panel (Top Section) ---
    JPanel infoPanel = new JPanel(new BorderLayout(10, 2));
    infoPanel.setOpaque(false);

    // Left side of info panel (Name, Pet, Rating)
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setOpaque(false);

    JLabel nameLabel = new JLabel(t.getName() + (t.getLocation() != null && !t.getLocation().isEmpty() ? " from " + t.getLocation() : ""));
    nameLabel.setFont(Constants.FONT_BOLD.deriveFont(14f));
    nameLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);

    JLabel petLabel = new JLabel("Pet: " + (t.getPetName() != null && !t.getPetName().isEmpty() ? t.getPetName() : "N/A"));
    petLabel.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    petLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);

    JLabel ratingLabel = new JLabel("Rating: " + (t.getRating() > 0 ? t.getRating() + "/5" : "Not rated"));
    ratingLabel.setFont(Constants.FONT_NORMAL.deriveFont(13f));
    ratingLabel.setForeground(Constants.COLOR_ACCENT);

    detailsPanel.add(nameLabel);
    detailsPanel.add(petLabel);
    detailsPanel.add(ratingLabel);

    // Right side of info panel (Status)
    JLabel statusLabel = new JLabel("Status: " + t.getStatus().name());
    statusLabel.setFont(Constants.FONT_BOLD.deriveFont(13f));
    // Set color based on status
    switch (t.getStatus()) {
      case APPROVED:
        statusLabel.setForeground(Constants.COLOR_SUCCESS);
        break;
      case REJECTED:
        statusLabel.setForeground(Constants.COLOR_ERROR);
        break;
      case PENDING:
        statusLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        break;
    }

    infoPanel.add(detailsPanel, BorderLayout.WEST);
    infoPanel.add(statusLabel, BorderLayout.EAST);


    // --- Button Panel (Bottom Section) ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // Align buttons right
    buttonPanel.setOpaque(false);

    // Status Change Buttons (only show if status is not already this)
    if (t.getStatus() != Testimonial.Status.APPROVED) {
      JButton approveButton = createActionButton("Approve", Constants.COLOR_SUCCESS, e -> updateStatus(t.getId(), Testimonial.Status.APPROVED));
      buttonPanel.add(approveButton);
    }
    if (t.getStatus() != Testimonial.Status.REJECTED) {
      JButton rejectButton = createActionButton("Reject", Constants.COLOR_ERROR, e -> updateStatus(t.getId(), Testimonial.Status.REJECTED));
      buttonPanel.add(rejectButton);
    }
    if (t.getStatus() != Testimonial.Status.PENDING) {
      JButton pendingButton = createActionButton("Set Pending", Constants.COLOR_TEXT_SECONDARY, e -> updateStatus(t.getId(), Testimonial.Status.PENDING));
      buttonPanel.add(pendingButton);
    }

    // Delete Button (with confirmation)
    JButton deleteButton = createActionButton("Delete", Color.DARK_GRAY, e -> deleteTestimonial(t.getId()));
    buttonPanel.add(deleteButton);


    // --- Assemble Panel ---
    panel.add(infoPanel, BorderLayout.NORTH);
    panel.add(new JScrollPane(textArea), BorderLayout.CENTER); // Put text area in scroll pane
    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
  }

  // Helper method to create styled action buttons
  private JButton createActionButton(String text, Color bgColor, java.awt.event.ActionListener action) {
    JButton button = new JButton(text);
    button.setFont(Constants.FONT_BUTTON.deriveFont(11f)); // Smaller font for more buttons
    button.setBackground(bgColor);
    button.setForeground(Color.WHITE);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setMargin(new Insets(2, 5, 2, 5)); // Smaller margins
    button.addActionListener(action);
    return button;
  }

  // Helper method to handle status updates
  private void updateStatus(int testimonialId, Testimonial.Status newStatus) {
    boolean success = testimonialDAO.updateTestimonialStatus(testimonialId, newStatus);
    if (success) {
      JOptionPane.showMessageDialog(this, "Testimonial status updated to " + newStatus.name() + ".");
      loadTestimonials(); // Refresh the list
    } else {
      JOptionPane.showMessageDialog(this, "Failed to update testimonial status.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Helper method to handle deletion
  private void deleteTestimonial(int testimonialId) {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to permanently delete this testimonial?",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
      boolean success = testimonialDAO.deleteTestimonial(testimonialId);
      if (success) {
        JOptionPane.showMessageDialog(this, "Testimonial deleted successfully.");
        loadTestimonials(); // Refresh the list
      } else {
        JOptionPane.showMessageDialog(this, "Failed to delete testimonial.", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
