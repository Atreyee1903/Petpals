package org.petpals.controller;

import org.petpals.dto.SignupRequest;
import org.petpals.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("signupRequest") SignupRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "signup";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "signup";
        }

        if (userService.usernameExists(request.getUsername())) {
            model.addAttribute("errorMessage", "Username '" + request.getUsername() + "' is already taken.");
            return "signup";
        }

        if (userService.emailExists(request.getEmail())) {
            model.addAttribute("errorMessage", "Email '" + request.getEmail() + "' is already registered.");
            return "signup";
        }

        userService.registerUser(request.getUsername(), request.getPassword(),
                request.getEmail(), request.getFullName());

        redirectAttributes.addFlashAttribute("successMessage",
                "Signup successful! Please log in with your new credentials.");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
}

