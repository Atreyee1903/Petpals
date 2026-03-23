package org.petpals.controller;

import org.petpals.model.Product;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String products(@RequestParam(value = "search", required = false) String search,
                           @AuthenticationPrincipal CustomUserDetails principal,
                           Model model) {
        List<Product> products = productService.searchProducts(search);
        model.addAttribute("products", products);
        model.addAttribute("searchTerm", search != null ? search : "");
        model.addAttribute("user", principal.getUser());
        return "products";
    }
}

