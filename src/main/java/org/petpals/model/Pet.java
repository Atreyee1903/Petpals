package org.petpals.model;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String species; // DOG, CAT, BIRD

    @Column(length = 100)
    private String breed;

    @Column(length = 50)
    private String age;

    @Column(length = 255)
    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String location;

    @Column(length = 255)
    private String traits;

    public Pet() {}

    public Pet(String name, String species, String breed, String age, String image,
               String description, String location, String traits) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.image = image;
        this.description = description;
        this.location = location;
        this.traits = traits;
    }

    @Transient
    public List<String> getTraitsList() {
        if (traits != null && !traits.isEmpty()) {
            return Arrays.asList(traits.split("\\s*,\\s*"));
        }
        return Collections.emptyList();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTraits() { return traits; }
    public void setTraits(String traits) { this.traits = traits; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return id != null && id.equals(pet.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
