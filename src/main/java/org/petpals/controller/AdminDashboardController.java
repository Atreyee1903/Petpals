package org.petpals.controller;

import org.petpals.dto.AnalyticsDTO;
import org.petpals.dto.UserDTO;
import org.petpals.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", adminService.getTotalUsers());
        metrics.put("totalOrders", adminService.getTotalOrders());
        metrics.put("totalRevenue", adminService.getTotalRevenue());
        metrics.put("activeUsers", adminService.getActiveUsersCount());
        metrics.put("blockedUsers", adminService.getBlockedUsersCount());
        
        model.addAttribute("metrics", metrics);
        model.addAttribute("recentActivities", adminService.getRecentActivities(5));
        model.addAttribute("topCustomers", adminService.getTopCustomersBySpending(5));
        
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users;
        
        if (search != null && !search.isEmpty()) {
            users = adminService.searchUsers(search, pageable);
        } else {
            users = adminService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("searchTerm", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "admin/userlist";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        UserDTO user = adminService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("userActivities", adminService.getUserActivities(id, 10));
        model.addAttribute("userOrders", adminService.getUserOrders(id));
        
        return "admin/userdetail";
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id, @RequestParam String reason, Model model) {
        try {
            adminService.blockUser(id, reason);
            model.addAttribute("successMessage", "User blocked successfully");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error blocking user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id) {
        try {
            adminService.unblockUser(id);
        } catch (Exception e) {
            // Handle error
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            Model model) {
        
        try {
            adminService.updateUser(id, fullName, email, phone, address);
            model.addAttribute("successMessage", "User updated successfully");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
        } catch (Exception e) {
            // Handle error
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        AnalyticsDTO analytics = adminService.getAnalytics();
        model.addAttribute("analytics", analytics);
        model.addAttribute("ordersChart", adminService.getOrdersOverTime());
        model.addAttribute("revenueChart", adminService.getRevenueOverTime());
        model.addAttribute("categoryChart", adminService.getRevenueByCategoryChart());
        model.addAttribute("topProducts", adminService.getTopProductsChart());
        
        return "admin/analytics";
    }

    @GetMapping("/activities")
    public String activities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("activities", adminService.getActivities(pageable));
        model.addAttribute("currentPage", page);
        
        return "admin/activities";
    }

    @GetMapping("/blocked-users")
    public String blockedUsersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("blockedUsers", adminService.getBlockedUsers(pageable));
        model.addAttribute("currentPage", page);
        
        return "admin/blockedusers";
    }
}
