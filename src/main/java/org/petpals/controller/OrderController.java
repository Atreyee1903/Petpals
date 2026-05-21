package org.petpals.controller;

import org.petpals.dto.CheckoutRequest;
import org.petpals.model.CartItem;
import org.petpals.model.Order;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.CartService;
import org.petpals.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/orders")
    public String orderHistory(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        List<Order> orders = orderService.getOrdersByUser(principal.getUserId());
        model.addAttribute("orders", orders);
        model.addAttribute("user", principal.getUser());
        return "orders";
    }

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        List<CartItem> items = cartService.getCartItems(principal.getUserId());
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.getCartTotal(principal.getUserId()));
        model.addAttribute("checkoutRequest", new CheckoutRequest());
        model.addAttribute("user", principal.getUser());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequest request,
                                  BindingResult result,
                                  @AuthenticationPrincipal CustomUserDetails principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<CartItem> items = cartService.getCartItems(principal.getUserId());
            model.addAttribute("cartItems", items);
            model.addAttribute("cartTotal", cartService.getCartTotal(principal.getUserId()));
            model.addAttribute("user", principal.getUser());
            return "checkout";
        }

        try {
            Order order = orderService.placeOrder(
                    principal.getUserId(),
                    request.getStreet(), request.getCity(), request.getState(),
                    request.getPostalCode(), request.getPhone(), request.getUpiId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order ID: " + order.getId());
            return "redirect:/orders";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to place order: " + e.getMessage());
            List<CartItem> items = cartService.getCartItems(principal.getUserId());
            model.addAttribute("cartItems", items);
            model.addAttribute("cartTotal", cartService.getCartTotal(principal.getUserId()));
            model.addAttribute("user", principal.getUser());
            return "checkout";
        }
    }
}

