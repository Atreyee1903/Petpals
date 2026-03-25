package org.petpals.controller;

import org.petpals.security.CustomUserDetails;
import org.petpals.service.TestimonialService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TestimonialController {

    private final TestimonialService testimonialService;

    public TestimonialController(TestimonialService testimonialService) {
        this.testimonialService = testimonialService;
    }

    @PostMapping("/testimonials/submit")
    public String submitTestimonial(@RequestParam String name,
                                    @RequestParam(required = false) String location,
                                    @RequestParam(required = false) String petName,
                                    @RequestParam String text,
                                    @RequestParam(defaultValue = "5") Integer rating,
                                    @RequestParam(required = false) String image,
                                    @AuthenticationPrincipal CustomUserDetails principal,
                                    RedirectAttributes redirectAttributes) {

        if (name.isBlank() || text.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Name and testimonial text are required.");
            return "redirect:/home";
        }

        testimonialService.submitTestimonial(
                principal.getUserId(), name, location, petName, text, rating,
                (image != null && !image.isBlank()) ? image : null);

        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you! Your testimonial has been submitted for review.");
        return "redirect:/home";
    }
}

