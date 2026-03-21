package org.petpals.ui;

import org.petpals.db.TestimonialDAO;
import org.petpals.model.Testimonial;
import org.petpals.model.Testimonial.Status;

import javax.swing.*;
import java.awt.*;
// Timestamp no longer needed for constructing the object to be saved

public class SubmitTestimonialDialog extends JDialog {

  private final JTextField nameField;
  private final JTextField locationField;
  private final JTextField petNameField;
  private final JTextArea testimonialArea;
  private final JComboBox<Integer> ratingCombo;
  private final JTextField imageField;

  private final TestimonialDAO testimonialDAO;
  private final int userId; // Added field to store the logged-in user's ID

  // Constructor updated to accept userId
  public SubmitTestimonialDialog(Frame owner, int userId) {
    super(owner, "Submit Testimonial", true);
    this.testimonialDAO = new TestimonialDAO();
    this.userId = userId; // Store the passed userId

    setLayout(new BorderLayout(10, 10));
    setSize(550, 450);
    setLocationRelativeTo(owner);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 10, 8, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    nameField = new JTextField(25);
    locationField = new JTextField(25);
    petNameField = new JTextField(25);
    testimonialArea = new JTextArea(5, 25);
    testimonialArea.setLineWrap(true);
    testimonialArea.setWrapStyleWord(true);
    JScrollPane testimonialScroll = new JScrollPane(testimonialArea);
    ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    imageField = new JTextField(25);

    int row = 0;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    panel.add(new JLabel("Your Name:"), gbc);
    gbc.gridx = 1;
    panel.add(nameField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1;
    panel.add(locationField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(new JLabel("Pet's Name:"), gbc);
    gbc.gridx = 1;
    panel.add(petNameField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    panel.add(new JLabel("Testimonial:"), gbc);
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(testimonialScroll, gbc);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(new JLabel("Rating (1-5):"), gbc);
    gbc.gridx = 1;
    panel.add(ratingCombo, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(new JLabel("Image filename (optional):"), gbc);
    gbc.gridx = 1;
    panel.add(imageField, gbc);

    // Submit button
    JButton submitButton = new JButton("Submit");
    submitButton.addActionListener(e -> submitTestimonial());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(submitButton);

    add(panel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private void submitTestimonial() {
    String name = nameField.getText().trim();
    String location = locationField.getText().trim();
    String petName = petNameField.getText().trim();
    String text = testimonialArea.getText().trim();
    int rating = (Integer) ratingCombo.getSelectedItem();
    String image = imageField.getText().trim();

    if (name.isEmpty() || text.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Name and testimonial text are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Create a Testimonial object for submission.
    // ID, status, and submittedAt are handled by the DB/DAO.
    // We only need to provide the data collected from the form + the userId.
    // The constructor we need matches the fields required by addTestimonial DAO method.
    // Let's assume a simplified constructor or pass null/defaults for fields not directly set by addTestimonial.
    // Re-checking Testimonial constructor and addTestimonial DAO...
    // The DAO's addTestimonial expects a Testimonial object containing:
    // userId, name, location, image, petName, text, rating.
    // So we need a Testimonial object with these fields.
    // The full Testimonial constructor includes id, status, submittedAt which we don't have yet.
    // Let's create the object with dummy values for those, as the DAO ignores them for insertion.
    Testimonial testimonialToSubmit = new Testimonial(
        0, // ID is auto-generated
        this.userId, // Use the stored userId
        name,
        location,
        image.isEmpty() ? null : image, // Use null if image field is empty
        petName.isEmpty() ? null : petName, // Use null if petName is empty
        text,
        rating,
        Status.PENDING, // Status is set by DB default, but constructor needs a value
        null // submittedAt is set by DB default, constructor needs a value
    );


    boolean success = testimonialDAO.addTestimonial(testimonialToSubmit);
    if (success) {
      JOptionPane.showMessageDialog(this, "Thank you! Your testimonial has been submitted for review.");
      dispose();
    } else {
      JOptionPane.showMessageDialog(this, "Failed to submit testimonial.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}
