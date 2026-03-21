package org.petpals.db;

import org.petpals.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM products ORDER BY category, name";

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        products.add(mapRowToProduct(rs));
      }
    } catch (SQLException e) {
      System.err.println("Error fetching products: " + e.getMessage());
      e.printStackTrace();
    }
    return products;
  }

  /**
   * Fetches a single product by its ID.
   *
   * @param productId The ID of the product.
   * @return The Product object, or null if not found or on error.
   */
  public Product getProductById(int productId) {
    String sql = "SELECT * FROM products WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, productId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapRowToProduct(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Error fetching product by ID " + productId + ": " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<Product> searchProducts(String searchTerm) {
    List<Product> products = new ArrayList<>();
    // Use LOWER() for case-insensitive search and LIKE for partial matching
    String sql = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) ORDER BY category, name";
    String queryParam = "%" + searchTerm + "%"; // Add wildcards for partial matching

    try (Connection conn = DatabaseConnection.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, queryParam); // Set the search term for name
      pstmt.setString(2, queryParam); // Set the search term for category

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          products.add(mapRowToProduct(rs));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error searching products: " + e.getMessage());
      e.printStackTrace();
    }
    return products;
  }

  // Helper method to map ResultSet row to Product
  private Product mapRowToProduct(ResultSet rs) throws SQLException {
    return new Product(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getDouble("price"),
        rs.getString("image"),
        rs.getString("category")
    );
  }

  // --- CRUD Methods for Admin ---

  /**
   * Adds a new product to the database.
   *
   * @param product The Product object to add (ID is ignored).
   * @return true if added successfully, false otherwise.
   */
  public boolean addProduct(Product product) {
    String sql = "INSERT INTO products (name, price, image, category) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, product.getName());
      pstmt.setDouble(2, product.getPrice());
      pstmt.setString(3, product.getImage());
      pstmt.setString(4, product.getCategory());

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Added Product: " + product.getName());
        return true;
      }
    } catch (SQLException e) {
      System.err.println("Error adding product " + product.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Updates an existing product in the database.
   *
   * @param product The Product object with updated details (including ID).
   * @return true if updated successfully, false otherwise.
   */
  public boolean updateProduct(Product product) {
    String sql = "UPDATE products SET name = ?, price = ?, image = ?, category = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, product.getName());
      pstmt.setDouble(2, product.getPrice());
      pstmt.setString(3, product.getImage());
      pstmt.setString(4, product.getCategory());
      pstmt.setInt(5, product.getId()); // WHERE clause

      int affectedRows = pstmt.executeUpdate();
      if (affectedRows > 0) {
        System.out.println("Updated Product ID: " + product.getId());
        return true;
      } else {
        System.out.println("Update Product ID: " + product.getId() + " - No rows affected (maybe ID not found).");
        return false;
      }
    } catch (SQLException e) {
      System.err.println("Error updating product ID " + product.getId() + ": " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Deletes a product from the database.
   * Note: This does NOT currently handle removing the product from active carts.
   *
   * @param productId The ID of the product to delete.
   * @return true if deleted successfully, false otherwise.
   */
  public boolean deleteProduct(int productId) {
    String sql = "DELETE FROM products WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, productId);
      int affectedRows = pstmt.executeUpdate();

      if (affectedRows > 0) {
        System.out.println("Deleted Product ID: " + productId);
        return true;
      } else {
        System.out.println("Delete Product ID: " + productId + " - No rows affected (maybe ID not found).");
        return false;
      }
    } catch (SQLException e) {
      System.err.println("Error deleting product ID " + productId + ": " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

}
