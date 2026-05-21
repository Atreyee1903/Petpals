package org.petpals.service;

import org.petpals.model.Product;
import org.petpals.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByCategoryAscNameAsc();
    }

    public List<Product> searchProducts(String term) {
        if (term == null || term.isBlank()) {
            return getAllProducts();
        }
        return productRepository.searchByNameOrCategory(term.trim());
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

