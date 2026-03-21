package org.petpals;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.petpals.db.DatabaseConnection;
import org.petpals.model.User;
import org.petpals.ui.HomePage;
import org.petpals.ui.LoginDialog;
import org.petpals.utils.AuthPreferences;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Objects;

public class Main {

  public static void main(String[] args) {
    // Apply FlatLaf theme early
    try {
      // Option 1: Use a specific FlatLaf theme
      FlatMacLightLaf.setup();

      // Update UI defaults after setting LaF
      FlatLaf.updateUI();

    } catch (Exception ex) {
      System.err.println("Failed to initialize LaF: " + ex.getMessage());
    }

    SwingUtilities.invokeLater(() -> {
      User loggedInUser;
      try {
        // Ensure LaF setup again just in case (might be redundant but safe)
        // FlatMacLightLaf.setup();
        // FlatLaf.updateUI(); // Update if re-applying

        DatabaseConnection.getConnection(); // Establish DB connection early
        loggedInUser = AuthPreferences.checkRecentLogin();

        if (loggedInUser == null) {
          LoginDialog loginDialog = new LoginDialog(null); // Pass null owner initially
          loginDialog.setVisible(true);
          loggedInUser = loginDialog.getAuthenticatedUser();
        }

        if (loggedInUser != null) {
          System.out.println("Login successful for user: " + loggedInUser.getUsername());
          // Create HomePage only after successful login
          HomePage homePage = new HomePage(loggedInUser);

          // Set App Icon
          ImageIcon appIcon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/images/app/app_icon.png")));
          if (appIcon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
            homePage.setIconImage(appIcon.getImage());
            // Also set for Taskbar on supported systems (like macOS)
            try {
              Taskbar taskbar = Taskbar.getTaskbar();
              taskbar.setIconImage(appIcon.getImage());
            } catch (UnsupportedOperationException | SecurityException e) {
              System.err.println("Taskbar icon setting not supported or permission denied.");
            }
          } else {
            System.err.println("Warning: Could not load application icon.");
          }

          homePage.setVisible(true);
        } else {
          System.out.println("Login failed or cancelled. Exiting application.");
          DatabaseConnection.closeConnection();
          System.exit(0);
        }

        // Add shutdown hook for DB connection cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));

      } catch (SQLException ex) {
        handleFatalError("Database Error", "Could not connect to the database. Please check settings and ensure MySQL is running.\nError: " + ex.getMessage(), ex);
      } catch (Exception e) {
        handleFatalError("Application Startup Error", "An unexpected error occurred during startup: " + e.getMessage(), e);
      }
    });
  }

  private static void handleFatalError(String title, String message, Exception exception) {
    System.err.println("FATAL: " + title + " - " + message);
    if (exception != null) {
      exception.printStackTrace();
    }
    // Ensure error dialog appears over everything
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    DatabaseConnection.closeConnection(); // Attempt cleanup
    System.exit(1);
  }
}