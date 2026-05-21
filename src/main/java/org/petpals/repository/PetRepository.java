package org.petpals.repository;

import org.petpals.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByOrderByNameAsc();

    List<Pet> findBySpeciesIgnoreCaseOrderByNameAsc(String species);

    @Query("SELECT DISTINCT p.species FROM Pet p WHERE p.species IS NOT NULL ORDER BY p.species")
    List<String> findDistinctSpecies();

    List<Pet> findByIdIn(Set<Long> ids);
}

