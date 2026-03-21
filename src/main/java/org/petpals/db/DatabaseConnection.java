package org.petpals.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/petpals";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "root";

  private static Connection connection = null;

  private DatabaseConnection() {
  }

  public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      try {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Database connection successful!");
      } catch (SQLException e) {
        System.err.println("Database Connection Error: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }
    return connection;
  }

  public static void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
        connection = null;
        System.out.println("Database connection closed.");
      } catch (SQLException e) {
        System.err.println("Error closing connection: " + e.getMessage());
      }
    }
  }
}
