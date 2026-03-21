package org.petpals.db;

import org.mindrot.jbcrypt.BCrypt;
import org.petpals.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

  public User findUserByUsername(String username) {
    // Select the new is_admin column
    String sql = "SELECT id, username, password_hash, email, full_name, is_admin FROM users WHERE username = ?";
    User user = null;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          user = new User(
              rs.getInt("id"),
              rs.getString("username"),
              rs.getString("password_hash"),
              rs.getString("email"),
              rs.getString("full_name"),
              rs.getBoolean("is_admin") // Get the admin status
          );
        }
      }
    } catch (SQLException e) {
      System.err.println("Error finding user by username: " + e.getMessage());
      e.printStackTrace();
    }
    return user;
  }

  public boolean usernameExists(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, username);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      System.err.println("Error checking username existence: " + e.getMessage());
      e.printStackTrace();
      return true;
    }
  }

  public boolean emailExists(String email) {
    String sql = "SELECT 1 FROM users WHERE email = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, email);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      System.err.println("Error checking email existence: " + e.getMessage());
      e.printStackTrace();
      return true;
    }
  }


  public boolean addUser(String username, String plainPassword, String email, String fullName) {
    // Note: is_admin defaults to FALSE in the database schema
    if (usernameExists(username)) {
      System.err.println("Attempted to add user with existing username: " + username);
      return false;
    }
    if (emailExists(email)) {
      System.err.println("Attempted to add user with existing email: " + email);
      return false;
    }

    String sql = "INSERT INTO users (username, password_hash, email, full_name) VALUES (?, ?, ?, ?)";
    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, username);
      pstmt.setString(2, hashedPassword);
      pstmt.setString(3, email);
      pstmt.setString(4, fullName);

      int affectedRows = pstmt.executeUpdate();
      return affectedRows > 0;

    } catch (SQLException e) {
      System.err.println("Error adding user: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public User verifyUser(String username, String plainPassword) {
    User user = findUserByUsername(username); // This now fetches the admin status too
    if (user != null) {
      if (BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
        return user; // Return the full user object including admin status
      }
    }
    return null;
  }
}
