package org.petpals.model;

public class Product {
  private int id;
  private String name;
  private double price;
  private String image; // filename or path
  private String category;

  public Product(int id, String name, double price, String image, String category) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.image = image;
    this.category = category;
  }

  // Getters
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public double getPrice() {
    return price;
  }

  public String getImage() {
    return image;
  }

  public String getCategory() {
    return category;
  }

  @Override
  public String toString() {
    return name + " - $" + String.format("%.2f", price);
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Product other = (Product) obj;
    return id == other.id;
  }
}
