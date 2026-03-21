package org.petpals.model;

import java.util.Arrays;
import java.util.List;

public class Pet {
  private final int id;
  private final String name;
  private final String species; // "DOG", "CAT", "BIRD"
  private final String breed;
  private final String age;
  private final String image; // filename or path
  private final String description;
  private final String location;
  private List<String> traits;

  public Pet(int id, String name, String species, String breed, String age, String image, String description, String location, String traitsString) {
    this.id = id;
    this.name = name;
    this.species = species;
    this.breed = breed;
    this.age = age;
    this.image = image;
    this.description = description;
    this.location = location;
    setTraitsFromString(traitsString);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSpecies() {
    return species;
  }

  public String getBreed() {
    return breed;
  }

  public String getAge() {
    return age;
  }

  public String getImage() {
    return image;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public List<String> getTraits() {
    return traits;
  }

  private void setTraitsFromString(String traitsString) {
    if (traitsString != null && !traitsString.isEmpty()) {
      this.traits = Arrays.asList(traitsString.split("\\s*,\\s*")); // Split by comma, trim whitespace
    } else {
      this.traits = List.of();
    }
  }

  @Override
  public String toString() {
    return name + " (" + species + ")";
  }
}
