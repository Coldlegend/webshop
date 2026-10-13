package com.webshop.service;

import com.webshop.dto.CreateProductRequest;
import com.webshop.entity.Product;
import com.webshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    // Konstruktor-injektálás (Spring automatikusan átadja a ProductRepository-t)
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findByIdWithLock(id);
    }

    public List<Product> searchProducts(String name, BigDecimal maxPrice) {
        if (name != null && maxPrice != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceLessThanEqual(name, maxPrice);
        } else if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name);
        } else if (maxPrice != null) {
            return productRepository.findByPriceLessThanEqual(maxPrice);
        }
        return productRepository.findAll();
    }

    public Product createProduct(CreateProductRequest createProductRequest) {
        Product product = new Product(createProductRequest.name(), createProductRequest.price(),
                createProductRequest.stockQuantity());
        return productRepository.save(product);
    }
}
