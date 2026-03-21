package org.petpals.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class FavoriteDAO {

  /**
   * Checks if a specific pet is marked as favorite by a specific user.
   *
   * @param userId The ID of the user.
   * @param petId  The ID of the pet.
   * @return true if the pet is a favorite for the user, false otherwise.
   */
  public boolean isFavorite(int userId, int petId) {
    String sql = "SELECT 1 FROM user_favorites WHERE user_id = ? AND pet_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, userId);
      pstmt.setInt(2, petId);

      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next(); // Returns true if a row exists
      }
    } catch (SQLException e) {
      System.err.println("Error checking favorite status for user " + userId + ", pet " + petId + ": " + e.getMessage());
      e.printStackTrace();
      return false; // Assume not favorite on error
    }
  }

  /**
   * Adds a pet to the user's favorites.
   *
   * @param userId The ID of the user.
   * @param petId  The ID of the pet.
   * @return true if the favorite was added successfully, false otherwise.
   */
  public boolean addFavorite(int userId, int petId) {
    // Optional: Check if already favorite to avoid unnecessary insert attempts
    if (isFavorite(userId, petId)) {
      System.out.println("Pet " + petId + " is already a favorite for user " + userId);
      return true; // Already exists, consider it a success
    }

    String sql = "INSERT INTO user_favorites (user_id, pet_id) VALUES (?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, userId);
      pstmt.setInt(2, petId);

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Added favorite: User " + userId + ", Pet " + petId);
        return true;
      } else {
        System.err.println("Failed to add favorite (no rows affected): User " + userId + ", Pet " + petId);
        return false;
      }
    } catch (SQLException e) {
      // Handle potential duplicate key error gracefully if the initial check wasn't performed or in race conditions
      if (e.getSQLState().startsWith("23")) { // SQLState for integrity constraint violation (like duplicate key)
        System.out.println("Favorite already exists (caught duplicate key): User " + userId + ", Pet " + petId);
        return true; // Treat duplicate key as success in this context
      }
      System.err.println("Error adding favorite for user " + userId + ", pet " + petId + ": " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Removes a pet from the user's favorites.
   *
   * @param userId The ID of the user.
   * @param petId  The ID of the pet.
   * @return true if the favorite was removed successfully, false otherwise.
   */
  public boolean removeFavorite(int userId, int petId) {
    String sql = "DELETE FROM user_favorites WHERE user_id = ? AND pet_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, userId);
      pstmt.setInt(2, petId);

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Removed favorite: User " + userId + ", Pet " + petId);
        return true;
      } else {
        // This might happen if it wasn't a favorite to begin with
        System.out.println("No favorite found to remove for User " + userId + ", Pet " + petId);
        return true; // Consider success if it's not there anymore
      }
    } catch (SQLException e) {
      System.err.println("Error removing favorite for user " + userId + ", pet " + petId + ": " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Gets a set of all pet IDs favorited by a specific user.
   * (Useful for potentially highlighting favorites in the main list later)
   *
   * @param userId The ID of the user.
   * @return A Set of Integer containing the IDs of favorite pets. Returns an empty set on error or if none exist.
   */
  public Set<Integer> getFavoritePetIds(int userId) {
    Set<Integer> favoriteIds = new HashSet<>();
    String sql = "SELECT pet_id FROM user_favorites WHERE user_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          favoriteIds.add(rs.getInt("pet_id"));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching favorite pet IDs for user " + userId + ": " + e.getMessage());
      e.printStackTrace();
      // Return empty set on error
    }
    return favoriteIds;
  }
}