package org.petpals.service;

import org.petpals.model.Pet;
import org.petpals.model.User;
import org.petpals.repository.PetRepository;
import org.petpals.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetService(PetRepository petRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    public List<Pet> getAllPets(String speciesFilter) {
        if (speciesFilter != null && !speciesFilter.isBlank() && !"All".equalsIgnoreCase(speciesFilter)) {
            return petRepository.findBySpeciesIgnoreCaseOrderByNameAsc(speciesFilter);
        }
        return petRepository.findAllByOrderByNameAsc();
    }

    public Optional<Pet> getPetById(Long id) {
        return petRepository.findById(id);
    }

    public List<String> getDistinctSpecies() {
        return petRepository.findDistinctSpecies();
    }

    public List<Pet> getPetsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return petRepository.findByIdIn(ids);
    }

    public Pet savePet(Pet pet) {
        return petRepository.save(pet);
    }

    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }

    // ── Favorites ──

    public Set<Long> getFavoritePetIds(Long userId) {
        return userRepository.findFavoritePetIds(userId);
    }

    public boolean isFavorite(Long userId, Long petId) {
        return userRepository.isFavorite(userId, petId);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long petId) {
        User user = userRepository.findById(userId).orElse(null);
        Pet pet = petRepository.findById(petId).orElse(null);
        if (user == null || pet == null) return false;

        if (user.getFavoritePets().contains(pet)) {
            user.getFavoritePets().remove(pet);
            userRepository.save(user);
            return false; // no longer favorite
        } else {
            user.getFavoritePets().add(pet);
            userRepository.save(user);
            return true; // now favorite
        }
    }
}

