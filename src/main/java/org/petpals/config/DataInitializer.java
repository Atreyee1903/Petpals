package org.petpals.config;

import org.petpals.model.*;
import org.petpals.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               PetRepository petRepo,
                               ProductRepository productRepo,
                               TestimonialRepository testimonialRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // Only seed if database is empty
            if (userRepo.count() > 0) {
                System.out.println("Database already seeded. Skipping initialization.");
                return;
            }

            System.out.println("Seeding database with sample data...");

            // ── Users ──
            User admin = new User("admin", encoder.encode("admin"), "admin@petpals.org", "Admin User");
            admin.setAdmin(true);
            admin = userRepo.save(admin);

            User alice = new User("alice", encoder.encode("alice123"), "alice@example.com", "Alice Smith");
            alice = userRepo.save(alice);

            User bob = new User("bob", encoder.encode("bob123"), "bob@example.com", "Bob Johnson");
            bob = userRepo.save(bob);

            // ── Pets ──
            petRepo.save(new Pet("Buddy", "DOG", "Golden Retriever", "2 years", "buddy.jpg",
                    "Friendly and energetic, loves fetch.", "City Shelter West",
                    "Friendly,Energetic,Good with kids"));
            petRepo.save(new Pet("Whiskers", "CAT", "Siamese", "1 year", "whiskers.jpg",
                    "Curious and vocal, enjoys naps in sunny spots.", "Maple Animal Rescue",
                    "Curious,Vocal,Affectionate"));
            petRepo.save(new Pet("Kiwi", "BIRD", "Parakeet", "6 months", "kiwi.jpg",
                    "Cheerful and chirpy, learning to talk.", "Avian Friends Sanctuary",
                    "Chirpy,Social,Intelligent"));
            petRepo.save(new Pet("Luna", "DOG", "German Shepherd", "3 years", "luna.jpg",
                    "Loyal and protective, needs an active owner.", "City Shelter East",
                    "Loyal,Protective,Active"));
            petRepo.save(new Pet("Max", "DOG", "Labrador Retriever", "4 years", "max_labrador.jpg",
                    "Playful and loves water.", "City Shelter North",
                    "Playful,Loyal,Swimmer"));
            petRepo.save(new Pet("Cleo", "CAT", "Tabby", "2 years", "cleo_tabby.jpg",
                    "Independent but sweet, loves quiet corners.", "Feline Friends",
                    "Independent,Sweet,Quiet"));
            petRepo.save(new Pet("Rocky", "DOG", "Boxer", "5 years", "rocky_boxer.jpg",
                    "Energetic and goofy, great family dog.", "Canine Corner",
                    "Energetic,Goofy,Strong"));

            // ── Products ──
            productRepo.save(new Product("Premium Dog Food (5kg)", new BigDecimal("25.99"), "dog_food.jpg", "Food"));
            productRepo.save(new Product("Interactive Cat Wand", new BigDecimal("8.50"), "cat_wand.jpg", "Toys"));
            productRepo.save(new Product("Comfy Pet Bed (Medium)", new BigDecimal("35.00"), "pet_bed.jpg", "Accessories"));
            productRepo.save(new Product("Bird Seed Mix (1kg)", new BigDecimal("12.75"), "bird_seed.jpg", "Food"));
            productRepo.save(new Product("Durable Chew Toy", new BigDecimal("10.99"), "chew_toy.jpg", "Toys"));

            // ── Testimonials (approved for display) ──
            Testimonial t1 = new Testimonial();
            t1.setUser(alice);
            t1.setName("Alice Smith");
            t1.setLocation("Springfield");
            t1.setImage("alice.jpg");
            t1.setPetName("Buddy");
            t1.setText("Adopting Buddy was the best decision! He brings so much joy to our family. The process was smooth.");
            t1.setRating(5);
            t1.setStatus(Testimonial.Status.APPROVED);
            testimonialRepo.save(t1);

            Testimonial t2 = new Testimonial();
            t2.setUser(bob);
            t2.setName("Bob Johnson");
            t2.setLocation("Greenville");
            t2.setImage("bob.jpg");
            t2.setPetName("Whiskers");
            t2.setText("Whiskers settled in right away. She's the perfect companion. Thanks to the team for their help!");
            t2.setRating(4);
            t2.setStatus(Testimonial.Status.APPROVED);
            testimonialRepo.save(t2);

            System.out.println("Database seeding complete.");
            System.out.println("  Admin login: admin / admin");
            System.out.println("  User logins: alice / alice123, bob / bob123");
        };
    }
}

