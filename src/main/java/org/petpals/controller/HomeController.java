package org.petpals.controller;

import org.petpals.model.Pet;
import org.petpals.model.Testimonial;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.PetService;
import org.petpals.service.TestimonialService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
public class HomeController {

    private final PetService petService;
    private final TestimonialService testimonialService;

    public HomeController(PetService petService, TestimonialService testimonialService) {
        this.petService = petService;
        this.testimonialService = testimonialService;
    }

    @GetMapping("/home")
    public String home(@RequestParam(value = "species", required = false) String species,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       Model model) {

        List<Pet> pets = petService.getAllPets(species);
        List<String> speciesList = petService.getDistinctSpecies();
        List<Testimonial> testimonials = testimonialService.getApprovedTestimonials();
        Set<Long> favoriteIds = petService.getFavoritePetIds(principal.getUserId());

        model.addAttribute("pets", pets);
        model.addAttribute("speciesList", speciesList);
        model.addAttribute("selectedSpecies", species != null ? species : "All");
        model.addAttribute("testimonials", testimonials);
        model.addAttribute("favoriteIds", favoriteIds);
        model.addAttribute("user", principal.getUser());

        return "home";
    }
}

