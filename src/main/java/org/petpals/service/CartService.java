package org.petpals.service;

import org.petpals.model.CartItem;
import org.petpals.model.Product;
import org.petpals.model.User;
import org.petpals.repository.CartItemRepository;
import org.petpals.repository.ProductRepository;
import org.petpals.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public int getCartItemCount(Long userId) {
        return getCartItems(userId).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getCartTotal(Long userId) {
        return getCartItems(userId).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            Product product = productRepository.findById(productId).orElseThrow();
            cartItemRepository.save(new CartItem(user, product, quantity));
        }
    }

    @Transactional
    public void updateQuantity(Long userId, Long productId, int newQuantity) {
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            if (newQuantity <= 0) {
                cartItemRepository.delete(existing.get());
            } else {
                existing.get().setQuantity(newQuantity);
                cartItemRepository.save(existing.get());
            }
        }
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        existing.ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}

