package com.webshop.controller;

import com.webshop.dto.CreateProductRequest;
import com.webshop.service.ProductService;
import com.webshop.entity.Product;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products/{id} -> Visszaadja a terméket, vagy 404 Not Found-ot ha nem létezik
    @GetMapping("/{id}")
    public ResponseEntity<Product> findProductById(@PathVariable Long id) {
        return productService.findProductById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    // GET /api/products
    // GET /api/products?name=macbook
    // GET /api/products?maxPrice=100000
    // GET /api/products?name=logi&maxPrice=50000
    // 1. Végpont: Listázás ÉS szűrés (ha nincsenek paraméterek, mindent visszaad)
    // URL: GET /api/products vagy GET /api/products?name=...&maxPrice=...
    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return productService.searchProducts(name, maxPrice);
    }
    @PostMapping
    public Product createProduct(@Valid @RequestBody CreateProductRequest productRequest){
        return productService.createProduct(productRequest);
    }
}
