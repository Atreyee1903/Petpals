package org.petpals.db;

import org.petpals.model.Pet;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PetDAO {

  /**
   * Fetches all pets, optionally filtering by species.
   *
   * @param speciesFilter The species to filter by (e.g., "Dog", "Cat"). If null or empty, all pets are returned.
   * @return A List of Pet objects matching the criteria, ordered by name. Returns empty list on error.
   */
  public List<Pet> getAllPets(String speciesFilter) {
    List<Pet> pets = new ArrayList<>();
    StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM pets");
    boolean hasFilter = speciesFilter != null && !speciesFilter.trim().isEmpty() && !"All".equalsIgnoreCase(speciesFilter.trim());

    if (hasFilter) {
      sqlBuilder.append(" WHERE LOWER(species) = LOWER(?)"); // Case-insensitive comparison
    }
    sqlBuilder.append(" ORDER BY name");

    String sql = sqlBuilder.toString();
    // System.out.println("Executing SQL: " + sql + (hasFilter ? " with filter: " + speciesFilter : "")); // Debugging

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      if (hasFilter) {
        pstmt.setString(1, speciesFilter.trim());
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          pets.add(mapRowToPet(rs)); // Use helper method
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching pets (filter: " + speciesFilter + "): " + e.getMessage());
      e.printStackTrace();
    }
    return pets;
  }

   /**
   * Fetches a list of distinct pet species from the database.
   *
   * @return A List of unique species names, ordered alphabetically. Returns empty list on error.
   */
  public List<String> getDistinctPetSpecies() {
    List<String> speciesList = new ArrayList<>();
    String sql = "SELECT DISTINCT species FROM pets WHERE species IS NOT NULL AND species != '' ORDER BY species"; // Ensure non-empty species

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        speciesList.add(rs.getString("species"));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching distinct pet species: " + e.getMessage());
      e.printStackTrace();
    }
    return speciesList;
  }


  /**
   * Fetches a single pet by its ID.
   *
   * @param petId The ID of the pet.
   * @return The Pet object, or null if not found or on error.
   */
  public Pet getPetById(int petId) {
    String sql = "SELECT * FROM pets WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, petId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapRowToPet(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching pet by ID " + petId + ": " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Fetches specific pets based on a set of IDs.
   *
   * @param petIds A Set of pet IDs to retrieve.
   * @return A List of Pet objects matching the IDs, ordered by name. Returns empty list if input is null/empty or on error.
   */
  public List<Pet> getPetsByIds(Set<Integer> petIds) {
    if (petIds == null || petIds.isEmpty()) {
      return Collections.emptyList(); // Return empty list immediately
    }

    List<Pet> pets = new ArrayList<>();
    // Build "IN (?, ?, ...)" clause dynamically and safely
    String placeholders = String.join(",", Collections.nCopies(petIds.size(), "?"));
    String sql = "SELECT * FROM pets WHERE id IN (" + placeholders + ") ORDER BY name";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // Set the parameters for the prepared statement
      int index = 1;
      for (Integer id : petIds) {
        pstmt.setInt(index++, id);
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          pets.add(mapRowToPet(rs)); // Use helper method
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching pets by IDs: " + e.getMessage());
      e.printStackTrace();
    }
    return pets;
  }

  // Helper method to map a ResultSet row to a Pet object
  private Pet mapRowToPet(ResultSet rs) throws SQLException {
    return new Pet(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("species"),
        rs.getString("breed"),
        rs.getString("age"),
        rs.getString("image"),
        rs.getString("description"),
        rs.getString("location"),
        rs.getString("traits")
    );
  }

  // --- CRUD Methods for Admin ---

  /**
   * Adds a new pet to the database.
   * Assumes the input Pet object does not have an ID set (or it's ignored).
   *
   * @param pet The Pet object to add (without ID).
   * @return true if the pet was added successfully, false otherwise.
   */
  public boolean addPet(Pet pet) {
    String sql = "INSERT INTO pets (name, species, breed, age, image, description, location, traits) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, pet.getName());
      pstmt.setString(2, pet.getSpecies());
      pstmt.setString(3, pet.getBreed());
      pstmt.setString(4, pet.getAge());
      pstmt.setString(5, pet.getImage()); // Assuming image is just a filename
      pstmt.setString(6, pet.getDescription());
      pstmt.setString(7, pet.getLocation());
      // Convert List<String> back to comma-separated string for DB
      String traitsString = pet.getTraits() != null ? String.join(",", pet.getTraits()) : "";
      pstmt.setString(8, traitsString);

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Added Pet: " + pet.getName());
        return true;
      }
    } catch (SQLException e) {
      System.err.println("Error adding pet " + pet.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Updates an existing pet in the database.
   * Uses the ID from the Pet object to identify the record to update.
   *
   * @param pet The Pet object with updated details (including the correct ID).
   * @return true if the pet was updated successfully, false otherwise.
   */
  public boolean updatePet(Pet pet) {
    String sql = "UPDATE pets SET name = ?, species = ?, breed = ?, age = ?, image = ?, description = ?, location = ?, traits = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, pet.getName());
      pstmt.setString(2, pet.getSpecies());
      pstmt.setString(3, pet.getBreed());
      pstmt.setString(4, pet.getAge());
      pstmt.setString(5, pet.getImage());
      pstmt.setString(6, pet.getDescription());
      pstmt.setString(7, pet.getLocation());
      String traitsString = pet.getTraits() != null ? String.join(",", pet.getTraits()) : "";
      pstmt.setString(8, traitsString);
      pstmt.setInt(9, pet.getId()); // WHERE clause

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Updated Pet ID: " + pet.getId());
        return true;
      } else {
        System.out.println("Update Pet ID: " + pet.getId() + " - No rows affected (maybe ID not found).");
        // Return false if no rows were updated, might indicate the ID didn't exist
        return false;
      }
    } catch (SQLException e) {
      System.err.println("Error updating pet ID " + pet.getId() + ": " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Deletes a pet from the database.
   * Also attempts to remove associated favorites (optional but good practice).
   *
   * @param petId The ID of the pet to delete.
   * @return true if the pet was deleted successfully, false otherwise.
   */
  public boolean deletePet(int petId) {
    // Optional: Delete associated favorites first to avoid FK constraints if they exist
    String deleteFavSql = "DELETE FROM user_favorites WHERE pet_id = ?";
    String deletePetSql = "DELETE FROM pets WHERE id = ?";

    Connection conn = null;
    PreparedStatement pstmtFav = null;
    PreparedStatement pstmtPet = null;
    boolean success = false;

    try {
      conn = DatabaseConnection.getConnection();
      conn.setAutoCommit(false); // Start transaction

      // Delete favorites
      pstmtFav = conn.prepareStatement(deleteFavSql);
      pstmtFav.setInt(1, petId);
      pstmtFav.executeUpdate(); // Execute even if no favorites exist
      System.out.println("Attempted to remove favorites for Pet ID: " + petId);


      // Delete pet
      pstmtPet = conn.prepareStatement(deletePetSql);
      pstmtPet.setInt(1, petId);
      int affectedRows = pstmtPet.executeUpdate();

      if (affectedRows > 0) {
        conn.commit(); // Commit transaction
        System.out.println("Deleted Pet ID: " + petId);
        success = true;
      } else {
        System.out.println("Delete Pet ID: " + petId + " - No rows affected (maybe ID not found).");
        conn.rollback(); // Rollback if pet not found
      }

    } catch (SQLException e) {
      System.err.println("Error deleting pet ID " + petId + ": " + e.getMessage());
      e.printStackTrace();
      if (conn != null) {
        try {
          conn.rollback(); // Rollback on error
        } catch (SQLException ex) {
          System.err.println("Error rolling back transaction: " + ex.getMessage());
        }
      }
    } finally {
      // Close resources in reverse order of creation
      try {
        if (pstmtPet != null) pstmtPet.close();
      } catch (SQLException e) { /* ignore */ }
      try {
        if (pstmtFav != null) pstmtFav.close();
      } catch (SQLException e) { /* ignore */ }
      if (conn != null) {
        try {
          conn.setAutoCommit(true); // Reset auto-commit
          // Do not close the shared connection here if it's managed globally
          // DatabaseConnection.closeConnection(); // Only if connection is per-operation
        } catch (SQLException e) { /* ignore */ }
      }
    }
    return success;
  }
}
