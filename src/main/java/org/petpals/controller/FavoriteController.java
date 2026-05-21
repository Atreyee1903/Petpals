package org.petpals.controller;

import org.petpals.security.CustomUserDetails;
import org.petpals.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final PetService petService;

    public FavoriteController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam Long petId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean isFav = petService.toggleFavorite(principal.getUserId(), petId);
        return ResponseEntity.ok(Map.of("success", true, "favorited", isFav));
    }
}

