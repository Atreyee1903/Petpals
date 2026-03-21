package org.petpals.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UpiPaymentDialog extends JDialog {

  private final double amount;
  private JTextField tfUpiId;
  private JTextField tfStreetAddress;
  private JTextField tfCity;
  private JTextField tfState;
  private JTextField tfPostalCode;
  private JTextField tfPhoneNumber;
  private boolean paymentSubmitted = false;
  private String upiId = null;
  private String streetAddress = null;
  private String city = null;
  private String state = null;
  private String postalCode = null;
  private String phoneNumber = null;

  public UpiPaymentDialog(Dialog owner, double amount) {
    super(owner, "UPI Payment", true);
    this.amount = amount;
    initComponents();
    pack();
    setLocationRelativeTo(owner);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        paymentSubmitted = false;
        dispose();
      }
    });
  }

  private void initComponents() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Amount Label
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    JLabel amountLabel = new JLabel(String.format("Amount to Pay: ₹%.2f", amount));
    amountLabel.setFont(amountLabel.getFont().deriveFont(Font.BOLD, 14f));
    panel.add(amountLabel, gbc);

    // --- Shipping Address Section ---
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(15, 10, 5, 10); // Add some space above the section
    JLabel shippingLabel = new JLabel("Shipping Address");
    shippingLabel.setFont(shippingLabel.getFont().deriveFont(Font.BOLD));
    panel.add(shippingLabel, gbc);
    gbc.insets = new Insets(5, 10, 5, 10); // Reset insets for fields
    gbc.gridwidth = 1; // Reset width

    // Street Address
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel("Street Address:"), gbc);
    gbc.gridx = 1;
    tfStreetAddress = new JTextField(25);
    panel.add(tfStreetAddress, gbc);

    // City
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel("City:"), gbc);
    gbc.gridx = 1;
    tfCity = new JTextField(25);
    panel.add(tfCity, gbc);

    // State
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel("State:"), gbc);
    gbc.gridx = 1;
    tfState = new JTextField(25);
    panel.add(tfState, gbc);

    // Postal Code
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel("Postal Code:"), gbc);
    gbc.gridx = 1;
    tfPostalCode = new JTextField(10); // Shorter field
    panel.add(tfPostalCode, gbc);

    // Phone Number
    gbc.gridy++;
    gbc.gridx = 0;
    panel.add(new JLabel("Phone Number:"), gbc);
    gbc.gridx = 1;
    tfPhoneNumber = new JTextField(15); // Field width hint
    panel.add(tfPhoneNumber, gbc);

    // --- UPI Payment Section ---
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(15, 10, 5, 10); // Space above UPI section
    JLabel upiSectionLabel = new JLabel("Payment Details");
    upiSectionLabel.setFont(upiSectionLabel.getFont().deriveFont(Font.BOLD));
    panel.add(upiSectionLabel, gbc);
    gbc.insets = new Insets(5, 10, 5, 10); // Reset insets
    gbc.gridwidth = 1; // Reset width

    // UPI ID Label
    gbc.gridy++; // Increment row
    gbc.gridx = 0;
    panel.add(new JLabel("Enter UPI ID:"), gbc);

    // UPI ID Field
    gbc.gridx = 1;
    tfUpiId = new JTextField(25); // Wider field
    tfUpiId.setToolTipText("e.g., yourname@bank");
    panel.add(tfUpiId, gbc);

    // Button Panel
    gbc.gridx = 0;
    gbc.gridy++; // Increment row
    gbc.gridwidth = 2; // Span columns
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(15, 10, 10, 10);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

    JButton btnPay = new JButton("Pay Now");
    btnPay.addActionListener(this::payActionPerformed);
    buttonPanel.add(btnPay);

    JButton btnCancel = new JButton("Cancel");
    btnCancel.addActionListener(e -> {
      paymentSubmitted = false;
      dispose();
    });
    buttonPanel.add(btnCancel);

    panel.add(buttonPanel, gbc);

    getRootPane().setDefaultButton(btnPay);

    getContentPane().add(panel, BorderLayout.CENTER);
  }

  private void payActionPerformed(ActionEvent e) {
    String enteredUpi = tfUpiId.getText().trim();
    String enteredStreet = tfStreetAddress.getText().trim();
    String enteredCity = tfCity.getText().trim();
    String enteredState = tfState.getText().trim();
    String enteredPostalCode = tfPostalCode.getText().trim();
    String enteredPhone = tfPhoneNumber.getText().trim();

    // Validate Address Fields
    if (enteredStreet.isEmpty() || enteredCity.isEmpty() || enteredState.isEmpty() ||
        enteredPostalCode.isEmpty() || enteredPhone.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please fill in all shipping address fields.",
          "Input Error", JOptionPane.WARNING_MESSAGE);
      // Focus the first empty field (optional)
      if (enteredStreet.isEmpty()) tfStreetAddress.requestFocus();
      else if (enteredCity.isEmpty()) tfCity.requestFocus();
      else if (enteredState.isEmpty()) tfState.requestFocus();
      else if (enteredPostalCode.isEmpty()) tfPostalCode.requestFocus();
      else tfPhoneNumber.requestFocus();
      return;
    }

    // Validate UPI ID
    if (!enteredUpi.contains("@")) {
      JOptionPane.showMessageDialog(this,
          "Please enter your UPI ID.",
          "Input Error", JOptionPane.WARNING_MESSAGE);
      tfUpiId.requestFocus();
      return;
    }

    this.upiId = enteredUpi;
    this.streetAddress = enteredStreet;
    this.city = enteredCity;
    this.state = enteredState;
    this.postalCode = enteredPostalCode;
    this.phoneNumber = enteredPhone;
    this.paymentSubmitted = true;

    System.out.println("Simulating payment with UPI ID: " + this.upiId + " for amount: " + this.amount);
    System.out.println("Shipping to: " + streetAddress + ", " + city + ", " + state + " " + postalCode + ", Phone: " + phoneNumber);
    dispose();
  }

  public boolean isPaymentSubmitted() {
    return paymentSubmitted;
  }

  public String getUpiId() {
    return upiId;
  }

  // Getters for Address Details
  public String getStreetAddress() {
    return streetAddress;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }
}
