package org.petpals.repository;

import org.petpals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT p.id FROM User u JOIN u.favoritePets p WHERE u.id = :userId")
    Set<Long> findFavoritePetIds(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM User u JOIN u.favoritePets p WHERE u.id = :userId AND p.id = :petId")
    boolean isFavorite(@Param("userId") Long userId, @Param("petId") Long petId);
}

