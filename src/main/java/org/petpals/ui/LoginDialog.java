package org.petpals.ui;

import org.petpals.db.UserDAO;
import org.petpals.model.User;
import org.petpals.utils.AuthPreferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {

  private final UserDAO userDAO;
  private JTextField tfUsername;
  private JPasswordField pfPassword;
  private User authenticatedUser = null;

  public LoginDialog(Frame owner) {
    // Use the main application frame (HomePage) as owner if available, otherwise null
    super(owner, "Login to PetPals", true);
    userDAO = new UserDAO();
    initComponents();
    pack(); // Pack after components are added
    setResizable(false);
    setLocationRelativeTo(owner); // Center relative to owner

    // Handle closing via 'X' button
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // We handle closing
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        handleCancel();
      }
    });
  }

  private void initComponents() {
    // Use BorderLayout for overall structure
    getContentPane().setLayout(new BorderLayout(10, 10));
    getContentPane().setBackground(Constants.COLOR_BACKGROUND);

    // --- Title Panel (Icon + Welcome Message) ---
    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS)); // Vertical layout
    titlePanel.setOpaque(false); // Transparent background
    titlePanel.setBorder(new EmptyBorder(15, 15, 5, 15)); // Padding around the panel

    // App Icon
    URL iconUrl = getClass().getResource("/images/app/app_icon.png");
    if (iconUrl != null) {
        ImageIcon appIcon = new ImageIcon(iconUrl);
        // Scale icon if needed (optional, adjust size as desired)
        Image scaledImage = appIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing
    } else {
        System.err.println("Error: Could not find app icon resource.");
        // Optionally add a placeholder or error message label
    }


    // Welcome Message
    JLabel titleLabel = new JLabel("Welcome to PetPals!", SwingConstants.CENTER);
    titleLabel.setFont(Constants.FONT_HEADING_2.deriveFont(18f));
    titleLabel.setForeground(Constants.COLOR_PRIMARY);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
    // titleLabel.setBorder(new EmptyBorder(15, 15, 5, 15)); // Border moved to titlePanel
    titlePanel.add(titleLabel);

    getContentPane().add(titlePanel, BorderLayout.NORTH); // Add the combined panel

    // --- Input Panel ---
    JPanel inputPanel = new JPanel(new GridBagLayout());
    inputPanel.setOpaque(false); // Transparent background
    inputPanel.setBorder(new EmptyBorder(10, Constants.DIALOG_PADDING, 10, Constants.DIALOG_PADDING));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST; // Align labels left

    // Username Label and Field
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    JLabel userLabel = new JLabel("Username:");
    userLabel.setFont(Constants.FONT_NORMAL);
    inputPanel.add(userLabel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    tfUsername = new JTextField(20);
    tfUsername.setFont(Constants.FONT_NORMAL);
    inputPanel.add(tfUsername, gbc);

    // Password Label and Field
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    JLabel passLabel = new JLabel("Password:");
    passLabel.setFont(Constants.FONT_NORMAL);
    inputPanel.add(passLabel, gbc);

    gbc.gridx = 1;
    // gbc.gridy++; // Removed this line to fix alignment
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    pfPassword = new JPasswordField(20);
    pfPassword.setFont(Constants.FONT_NORMAL);
    // Add action listener to password field for Enter key press
    pfPassword.addActionListener(this::loginActionPerformed);
    inputPanel.add(pfPassword, gbc);

    getContentPane().add(inputPanel, BorderLayout.CENTER);

    // --- Button Panel ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center buttons
    buttonPanel.setOpaque(false);
    buttonPanel.setBorder(new EmptyBorder(5, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING, Constants.DIALOG_PADDING));

    JButton btnLogin = new JButton("Login");
    stylePrimaryButton(btnLogin);
    btnLogin.addActionListener(this::loginActionPerformed);

    JButton btnSignup = new JButton("Sign Up");
    styleSecondaryButton(btnSignup);
    btnSignup.setToolTipText("Create a new PetPals account");
    btnSignup.addActionListener(this::signupActionPerformed);

    JButton btnCancel = new JButton("Cancel");
    styleSecondaryButton(btnCancel);
    btnCancel.addActionListener(e -> handleCancel());

    buttonPanel.add(btnLogin);
    buttonPanel.add(btnSignup);
    buttonPanel.add(btnCancel);

    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    // Set Login as the default button (activated by Enter)
    getRootPane().setDefaultButton(btnLogin);
  }

  private void handleCancel() {
    authenticatedUser = null;
    dispose(); // Close the dialog
  }

  private void loginActionPerformed(ActionEvent e) {
    String username = tfUsername.getText().trim();
    String password = new String(pfPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      showErrorDialog("Please enter both username and password.");
      if (username.isEmpty()) tfUsername.requestFocus();
      else pfPassword.requestFocus();
      return;
    }

    // Perform verification using UserDAO
    authenticatedUser = userDAO.verifyUser(username, password);

    if (authenticatedUser != null) {
      AuthPreferences.saveLoginSession(authenticatedUser.getUsername()); // Save session
      System.out.println("Login successful via dialog for: " + authenticatedUser.getUsername());
      dispose(); // Close the dialog successfully
    } else {
      showErrorDialog("Invalid username or password. Please try again.");
      pfPassword.setText(""); // Clear password field
      pfPassword.requestFocus();
    }
  }

  private void signupActionPerformed(ActionEvent e) {
    // Hide login, show signup
    setVisible(false);

    SignupDialog signupDialog = new SignupDialog((Frame) getOwner()); // Pass owner frame
    signupDialog.setVisible(true); // This blocks until signupDialog is closed

    // After signupDialog closes
    if (signupDialog.isSignupSuccess()) {
      // Show success message and pre-fill login fields
      JOptionPane.showMessageDialog(this, // Show relative to this (now hidden) dialog
          "Signup successful! Please log in with your new credentials.",
          "Signup Complete", JOptionPane.INFORMATION_MESSAGE);
      tfUsername.setText(signupDialog.getSignedUpUsername());
      pfPassword.setText("");
      tfUsername.requestFocus(); // Focus username after signup
      setVisible(true); // Show login dialog again
    } else {
      // Signup was cancelled or failed, just show login dialog again
      System.out.println("Signup cancelled or failed.");
      setVisible(true);
      tfUsername.requestFocus(); // Focus username if returning from cancelled signup
    }
  }

  public User getAuthenticatedUser() {
    // This is called after the dialog is closed (either successfully or cancelled)
    return authenticatedUser;
  }

  // --- Helper Methods ---
  private void stylePrimaryButton(JButton button) {
    button.setFont(Constants.FONT_BUTTON.deriveFont(Font.BOLD));
    button.setBackground(Constants.COLOR_PRIMARY);
    button.setForeground(Color.WHITE);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.putClientProperty("JButton.buttonType", "roundRect");
  }

  private void styleSecondaryButton(JButton button) {
    button.setFont(Constants.FONT_BUTTON);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.putClientProperty("JButton.buttonType", "roundRect");
  }

  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, // Relative to this dialog
        message,
        "Login Error",
        JOptionPane.ERROR_MESSAGE);
  }
}
