package org.petpals.utils;

import org.petpals.db.CartItemDAO;
import org.petpals.model.CartItem;
import org.petpals.model.Product;
import org.petpals.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartManager {
  private static CartManager instance;
  private final CartItemDAO cartItemDAO;
  private List<CartItem> items;
  private User currentUser;

  private CartManager() {
    items = new ArrayList<>();
    cartItemDAO = new CartItemDAO();
  }

  public static synchronized CartManager getInstance() {
    if (instance == null) {
      instance = new CartManager();
    }
    return instance;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Set the current user and load their cart from the database
   *
   * @param user The current user
   */
  public void setCurrentUser(User user) {
    this.currentUser = user;
    loadCartFromDatabase();
  }

  /**
   * Load the cart from the database for the current user
   */
  private void loadCartFromDatabase() {
    if (currentUser == null) {
      return;
    }

    items.clear();
    items.addAll(cartItemDAO.getCartItems(currentUser.getId()));
    System.out.println("Loaded " + items.size() + " items from cart database");
  }

  /**
   * Save the cart to the database for the current user
   */
  private void saveCartToDatabase() {
    if (currentUser == null) {
      return;
    }

    boolean success = cartItemDAO.saveCartItems(currentUser.getId(), items);
    if (success) {
      System.out.println("Cart saved to database successfully");
    } else {
      System.err.println("Failed to save cart to database");
    }
  }

  public void addProduct(Product product, int quantity) {
    if (product == null || quantity <= 0) return;

    Optional<CartItem> existingItem = findItemByProduct(product);

    if (existingItem.isPresent()) {
      CartItem cartItem = existingItem.get();
      // Increase quantity only if adding more
      cartItem.setQuantity(cartItem.getQuantity() + quantity);
      System.out.println("Updated quantity in cart: " + product.getName() + " x" + cartItem.getQuantity());
    } else {
      items.add(new CartItem(product, quantity));
      System.out.println("Added to cart: " + product.getName() + " x" + quantity);
    }
    System.out.println("Cart items: " + items.size() + ", Total quantity: " + getTotalItemCount());

    // Save cart to database after modification
    saveCartToDatabase();
  }

  public void updateQuantity(Product product, int newQuantity) {
    if (product == null || newQuantity <= 0) return;
    Optional<CartItem> existingItem = findItemByProduct(product);
    if (existingItem.isPresent()) {
      existingItem.get().setQuantity(newQuantity);
      System.out.println("Set quantity for " + product.getName() + " to " + newQuantity);

      // Save cart to database after modification
      saveCartToDatabase();
    } else {
      System.err.println("Attempted to update quantity for product not in cart: " + product.getName());
    }
  }

  public void removeItem(Product product) {
    if (product == null) return;
    boolean removed = items.removeIf(item -> item.getProduct().equals(product));
    if (removed) {
      System.out.println("Removed from cart: " + product.getName());
      System.out.println("Cart items: " + items.size() + ", Total quantity: " + getTotalItemCount());

      // Save cart to database after modification
      saveCartToDatabase();
    }
  }

  private Optional<CartItem> findItemByProduct(Product product) {
    return items.stream()
        .filter(item -> item.getProduct().equals(product)) // Use product's equals method
        .findFirst();
  }

  public List<CartItem> getItems() {
    // Return a copy to prevent external modification
    return new ArrayList<>(items);
  }

  public double getTotalCost() {
    return items.stream()
        .mapToDouble(CartItem::getTotalPrice)
        .sum();
  }

  public int getTotalItemCount() {
    // Sum of quantities of all items
    return items.stream().mapToInt(CartItem::getQuantity).sum();
  }

  public void clearCart() {
    items.clear();
    System.out.println("Cart cleared.");

    // Clear cart in database too
    if (currentUser != null) {
      cartItemDAO.clearCartItems(currentUser.getId());
    }
  }
}
