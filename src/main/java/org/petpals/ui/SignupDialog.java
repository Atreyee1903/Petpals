package org.petpals.ui;


import org.petpals.db.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SignupDialog extends JDialog {

  private JTextField tfUsername;
  private JPasswordField pfPassword;
  private JPasswordField pfConfirmPassword;
  private JTextField tfEmail;
  private JTextField tfFullName;
  private JButton btnSignup;
  private JButton btnCancel;
  private UserDAO userDAO;
  private boolean signupSuccess = false;
  private String signedUpUsername = null;

  public SignupDialog(Frame owner) {
    super(owner, "Sign Up", true);
    userDAO = new UserDAO();
    initComponents();
    pack();
    setLocationRelativeTo(owner);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        signupSuccess = false;
        dispose();
      }
    });
  }

  private void initComponents() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Username
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Username*:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    tfUsername = new JTextField(20);
    panel.add(tfUsername, gbc);

    // Password
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Password*:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 1;
    pfPassword = new JPasswordField(20);
    panel.add(pfPassword, gbc);

    // Confirm Password
    gbc.gridx = 0;
    gbc.gridy = 2;
    panel.add(new JLabel("Confirm Pwd*:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 2;
    pfConfirmPassword = new JPasswordField(20);
    panel.add(pfConfirmPassword, gbc);

    // Email
    gbc.gridx = 0;
    gbc.gridy = 3;
    panel.add(new JLabel("Email*:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 3;
    tfEmail = new JTextField(20);
    panel.add(tfEmail, gbc);

    // Full Name (Optional)
    gbc.gridx = 0;
    gbc.gridy = 4;
    panel.add(new JLabel("Full Name:"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 4;
    tfFullName = new JTextField(20);
    panel.add(tfFullName, gbc);


    // Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Center buttons
    btnSignup = new JButton("Sign Up");
    btnSignup.addActionListener(this::signupActionPerformed);
    buttonPanel.add(btnSignup);

    btnCancel = new JButton("Cancel");
    btnCancel.addActionListener(e -> {
      signupSuccess = false;
      dispose();
    });
    buttonPanel.add(btnCancel);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2; // Span both columns
    gbc.fill = GridBagConstraints.NONE; // Don't stretch panel
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(15, 5, 5, 5); // Add top margin
    panel.add(buttonPanel, gbc);

    // Set default button
    getRootPane().setDefaultButton(btnSignup);

    getContentPane().add(panel, BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around panel
  }

  private void signupActionPerformed(ActionEvent e) {
    String username = tfUsername.getText().trim();
    String password = new String(pfPassword.getPassword());
    String confirmPassword = new String(pfConfirmPassword.getPassword());
    String email = tfEmail.getText().trim();
    String fullName = tfFullName.getText().trim();

    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please fill in all required fields (*).", "Input Error", JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (!password.equals(confirmPassword)) {
      JOptionPane.showMessageDialog(this, "Passwords do not match.", "Input Error", JOptionPane.WARNING_MESSAGE);
      pfPassword.setText("");
      pfConfirmPassword.setText("");
      pfPassword.requestFocus();
      return;
    }

    if (!email.contains("@") || !email.contains(".")) {
      JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Input Error", JOptionPane.WARNING_MESSAGE);
      tfEmail.requestFocus();
      return;
    }

    if (userDAO.usernameExists(username)) {
      JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken.", "Signup Error", JOptionPane.ERROR_MESSAGE);
      tfUsername.requestFocus();
      return;
    }
    if (userDAO.emailExists(email)) {
      JOptionPane.showMessageDialog(this, "Email '" + email + "' is already registered.", "Signup Error", JOptionPane.ERROR_MESSAGE);
      tfEmail.requestFocus();
      return;
    }

    boolean success = userDAO.addUser(username, password, email, fullName);

    if (success) {
      this.signupSuccess = true;
      this.signedUpUsername = username;
      dispose();
    } else {
      JOptionPane.showMessageDialog(this,
          "An error occurred during signup. Please try again or contact support.",
          "Signup Failed", JOptionPane.ERROR_MESSAGE);
      this.signupSuccess = false;
    }
  }

  public boolean isSignupSuccess() {
    return signupSuccess;
  }

  public String getSignedUpUsername() {
    return signedUpUsername;
  }
}
