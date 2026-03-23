package org.petpals.controller;

import org.petpals.model.CartItem;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cart(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        List<CartItem> items = cartService.getCartItems(principal.getUserId());
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.getCartTotal(principal.getUserId()));
        model.addAttribute("user", principal.getUser());
        return "cart";
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal CustomUserDetails principal) {
        cartService.addToCart(principal.getUserId(), productId, quantity);
        int count = cartService.getCartItemCount(principal.getUserId());
        return ResponseEntity.ok(Map.of("success", true, "cartCount", count));
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @AuthenticationPrincipal CustomUserDetails principal) {
        cartService.updateQuantity(principal.getUserId(), productId, quantity);
        int count = cartService.getCartItemCount(principal.getUserId());
        return ResponseEntity.ok(Map.of("success", true, "cartCount", count,
                "cartTotal", cartService.getCartTotal(principal.getUserId())));
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam Long productId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        cartService.removeFromCart(principal.getUserId(), productId);
        int count = cartService.getCartItemCount(principal.getUserId());
        return ResponseEntity.ok(Map.of("success", true, "cartCount", count,
                "cartTotal", cartService.getCartTotal(principal.getUserId())));
    }

    @PostMapping("/api/cart/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(
            @AuthenticationPrincipal CustomUserDetails principal) {
        cartService.clearCart(principal.getUserId());
        return ResponseEntity.ok(Map.of("success", true, "cartCount", 0));
    }
}

