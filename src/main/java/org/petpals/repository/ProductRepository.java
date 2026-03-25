package org.petpals.repository;

import org.petpals.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCategoryAscNameAsc();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(p.category) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.category, p.name")
    List<Product> searchByNameOrCategory(@Param("term") String term);
}

