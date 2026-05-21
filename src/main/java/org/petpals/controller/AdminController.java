package org.petpals.controller;

import org.petpals.model.*;
import org.petpals.model.Testimonial.Status;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PetService petService;
    private final ProductService productService;
    private final OrderService orderService;
    private final TestimonialService testimonialService;
    private final SupportQueryService supportQueryService;

    public AdminController(PetService petService, ProductService productService,
                           OrderService orderService, TestimonialService testimonialService,
                           SupportQueryService supportQueryService) {
        this.petService = petService;
        this.productService = productService;
        this.orderService = orderService;
        this.testimonialService = testimonialService;
        this.supportQueryService = supportQueryService;
    }

    // ── Dashboard ──
    @GetMapping
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("user", principal.getUser());
        model.addAttribute("petCount", petService.getAllPets(null).size());
        model.addAttribute("productCount", productService.getAllProducts().size());
        model.addAttribute("orderCount", orderService.getAllOrders().size());
        model.addAttribute("pendingTestimonials", testimonialService.getPendingTestimonials().size());
        model.addAttribute("openQueries", supportQueryService.getOpenQueries().size());
        return "admin/dashboard";
    }

    // ── Pet Management ──
    @GetMapping("/pets")
    public String managePets(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("pets", petService.getAllPets(null));
        model.addAttribute("user", principal.getUser());
        return "admin/pets";
    }

    @PostMapping("/pets/save")
    public String savePet(@RequestParam(required = false) Long id,
                          @RequestParam String name,
                          @RequestParam String species,
                          @RequestParam String breed,
                          @RequestParam String age,
                          @RequestParam String image,
                          @RequestParam(required = false) String description,
                          @RequestParam String location,
                          @RequestParam(required = false) String traits,
                          RedirectAttributes redirectAttributes) {
        Pet pet;
        if (id != null) {
            pet = petService.getPetById(id).orElse(new Pet());
        } else {
            pet = new Pet();
        }
        pet.setName(name);
        pet.setSpecies(species);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setImage(image);
        pet.setDescription(description);
        pet.setLocation(location);
        pet.setTraits(traits);
        petService.savePet(pet);
        redirectAttributes.addFlashAttribute("successMessage",
                id != null ? "Pet updated successfully." : "Pet added successfully.");
        return "redirect:/admin/pets";
    }

    @PostMapping("/pets/delete/{id}")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        petService.deletePet(id);
        redirectAttributes.addFlashAttribute("successMessage", "Pet deleted successfully.");
        return "redirect:/admin/pets";
    }

    // ── Product Management ──
    @GetMapping("/products")
    public String manageProducts(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("user", principal.getUser());
        return "admin/products";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam BigDecimal price,
                              @RequestParam String image,
                              @RequestParam String category,
                              RedirectAttributes redirectAttributes) {
        Product product;
        if (id != null) {
            product = productService.getProductById(id).orElse(new Product());
        } else {
            product = new Product();
        }
        product.setName(name);
        product.setPrice(price);
        product.setImage(image);
        product.setCategory(category);
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("successMessage",
                id != null ? "Product updated successfully." : "Product added successfully.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        return "redirect:/admin/products";
    }

    // ── Order Management ──
    @GetMapping("/orders")
    public String manageOrders(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("user", principal.getUser());
        return "admin/orders";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String newStatus,
                                    RedirectAttributes redirectAttributes) {
        boolean success = orderService.updateOrderStatus(orderId, newStatus);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Order status updated." : "Failed to update order status.");
        return "redirect:/admin/orders";
    }

    // ── Testimonial Management ──
    @GetMapping("/testimonials")
    public String manageTestimonials(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("testimonials", testimonialService.getAllTestimonials());
        model.addAttribute("user", principal.getUser());
        return "admin/testimonials";
    }

    @PostMapping("/testimonials/update-status")
    public String updateTestimonialStatus(@RequestParam Long testimonialId,
                                          @RequestParam String newStatus,
                                          RedirectAttributes redirectAttributes) {
        Status status = Status.fromString(newStatus);
        boolean success = testimonialService.updateStatus(testimonialId, status);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Testimonial status updated." : "Failed to update testimonial status.");
        return "redirect:/admin/testimonials";
    }

    @PostMapping("/testimonials/delete/{id}")
    public String deleteTestimonial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        testimonialService.deleteTestimonial(id);
        redirectAttributes.addFlashAttribute("successMessage", "Testimonial deleted.");
        return "redirect:/admin/testimonials";
    }

    // ── Support Query Management ──
    @GetMapping("/support")
    public String manageSupport(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("queries", supportQueryService.getAllQueries());
        model.addAttribute("user", principal.getUser());
        return "admin/support";
    }

    @PostMapping("/support/reply")
    public String replyToQuery(@RequestParam Long queryId,
                               @RequestParam String replyText,
                               RedirectAttributes redirectAttributes) {
        if (replyText == null || replyText.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reply cannot be empty.");
            return "redirect:/admin/support";
        }
        boolean success = supportQueryService.replyToQuery(queryId, replyText);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Reply submitted." : "Failed to submit reply.");
        return "redirect:/admin/support";
    }

    @PostMapping("/support/delete/{id}")
    public String deleteQuery(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supportQueryService.deleteQuery(id);
        redirectAttributes.addFlashAttribute("successMessage", "Query deleted.");
        return "redirect:/admin/support";
    }
}

