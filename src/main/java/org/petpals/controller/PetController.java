package org.petpals.controller;

import org.petpals.model.Pet;
import org.petpals.security.CustomUserDetails;
import org.petpals.service.PetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

@Controller
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/pets/{id}")
    public String petDetail(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails principal,
                            Model model) {
        Pet pet = petService.getPetById(id).orElse(null);
        if (pet == null) {
            return "redirect:/home";
        }
        boolean isFav = petService.isFavorite(principal.getUserId(), id);

        model.addAttribute("pet", pet);
        model.addAttribute("isFavorite", isFav);
        model.addAttribute("user", principal.getUser());
        return "pet-detail";
    }

    @GetMapping("/favorites")
    public String favorites(@AuthenticationPrincipal CustomUserDetails principal,
                            Model model) {
        Set<Long> favIds = petService.getFavoritePetIds(principal.getUserId());
        List<Pet> pets = petService.getPetsByIds(favIds);
        model.addAttribute("pets", pets);
        model.addAttribute("favoriteIds", favIds);
        model.addAttribute("user", principal.getUser());
        return "favorites";
    }
}

