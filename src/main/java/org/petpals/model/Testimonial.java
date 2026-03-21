package org.petpals.model;

import java.sql.Timestamp;

public class Testimonial {

  public enum Status {
    PENDING, APPROVED, REJECTED;

    // Helper to convert string from DB to enum
    public static Status fromString(String statusStr) {
        if (statusStr == null) {
            return PENDING; // Or throw an exception, depending on desired handling
        }
        try {
            return Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Log the error or handle unknown status
            System.err.println("Unknown testimonial status string: " + statusStr);
            return PENDING; // Default or fallback status
        }
    }
  }

  private int id;
  private int userId; // Added
  private String name;
  private String location;
  private String image; // Submitter's image
  private String petName; // Optional pet name
  private String text;
  private int rating; // Optional rating
  private Status status; // Added
  private Timestamp submittedAt; // Added

  // Constructor updated to include new fields
  public Testimonial(int id, int userId, String name, String location, String image, String petName, String text, int rating, Status status, Timestamp submittedAt) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.location = location;
    this.image = image;
    this.petName = petName;
    this.text = text;
    this.rating = rating;
    this.status = status;
    this.submittedAt = submittedAt;
  }

  // Getters
  public int getId() {
    return id;
  }

  public int getUserId() { return userId; } // Added getter

  public String getName() {
    return name;
  }

  public String getLocation() {
    return location;
  }

  public String getImage() {
    return image;
  }

  public String getPetName() {
    return petName;
  }

  public String getText() {
    return text;
  }

  public int getRating() {
    return rating;
  }

  public Status getStatus() { return status; } // Added getter

  public Timestamp getSubmittedAt() { return submittedAt; } // Added getter

  @Override
  public String toString() {
    // Updated toString for better representation, especially in admin lists
    return String.format("ID: %d, User: %d, Name: %s, Pet: %s, Status: %s",
                         id, userId, name, (petName != null ? petName : "N/A"), status);
  }
}
