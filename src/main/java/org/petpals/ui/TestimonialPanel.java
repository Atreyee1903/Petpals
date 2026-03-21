package org.petpals.ui;

import org.petpals.model.Testimonial;
import org.petpals.ui.components.ImageLabel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TestimonialPanel extends JPanel {

  private final Testimonial testimonial;

  public TestimonialPanel(Testimonial testimonial) {
    this.testimonial = testimonial;
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout(15, 10)); // Increased horizontal gap
    // Use panel background color, rely on parent's background setting
    setBackground(Constants.COLOR_PANEL_BACKGROUND);
    setOpaque(true); // Make sure background color is visible

    // Subtle border and padding
    Border line = BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.COLOR_BORDER); // Bottom border only
    Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    setBorder(new CompoundBorder(padding, line)); // Put padding outside the line

    // User Image (West) - Rounded maybe? (Requires custom painting or specific LaF support)
    ImageLabel userImage = new ImageLabel();
    userImage.loadImage("resources/images/testimonials/", testimonial.getImage(), 60, 60);
    // Add padding around image
    JPanel imagePanel = new JPanel(new BorderLayout());
    imagePanel.setOpaque(false);
    imagePanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Vertical padding
    imagePanel.add(userImage, BorderLayout.CENTER);
    add(imagePanel, BorderLayout.WEST);

    // Text Content Panel (Center)
    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false); // Inherit background

    // Name and Pet Name Label
    JLabel nameLabel = new JLabel(testimonial.getName() + " & " + testimonial.getPetName());
    nameLabel.setFont(Constants.FONT_BOLD.deriveFont(14f));
    nameLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
    nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // Testimonial Text Area
    JTextArea textArea = new JTextArea("\"" + testimonial.getText() + "\"");
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(Constants.FONT_ITALIC.deriveFont(13f));
    textArea.setForeground(Constants.COLOR_TEXT_SECONDARY);
    textArea.setBackground(getBackground()); // Match panel background
    textArea.setOpaque(false); // Allow panel background to show through
    textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    // Remove text area border provided by default L&F
    textArea.setBorder(null);

    // Rating Label (If available) - could use stars later
    JLabel ratingLabel = new JLabel();
    ratingLabel.setFont(Constants.FONT_NORMAL.deriveFont(12f));
    ratingLabel.setForeground(Constants.COLOR_ACCENT); // Use accent color for rating
    ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    if (testimonial.getRating() > 0) {
      // Simple text rating for now
      ratingLabel.setText("Rating: " + "★".repeat(testimonial.getRating()) + "☆".repeat(5 - testimonial.getRating()));
      // ratingLabel.setText("Rating: " + testimonial.getRating() + "/5");
    } else {
      ratingLabel.setText(""); // Hide if no rating
    }


    textPanel.add(nameLabel);
    textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    textPanel.add(textArea);
    textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    if (testimonial.getRating() > 0) {
      textPanel.add(ratingLabel);
    }

    add(textPanel, BorderLayout.CENTER);
  }
}