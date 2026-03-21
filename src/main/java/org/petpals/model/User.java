package org.petpals.model;

public class User {
  private final int id;
  private final String username;
  private final String passwordHash;
  private final String email;
  private final String fullName;
  private final boolean isAdmin; // Added admin flag

  public User(int id, String username, String passwordHash, String email, String fullName, boolean isAdmin) { // Updated constructor
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
    this.fullName = fullName;
    this.isAdmin = isAdmin; // Assign admin flag
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isAdmin() { // Getter for admin flag
    return isAdmin;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", email='" + email + '\'' +
        ", fullName='" + fullName + '\'' +
        ", isAdmin=" + isAdmin + // Include in toString
        '}';
  }
}
