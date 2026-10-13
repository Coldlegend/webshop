package com.webshop.repository;

import com.webshop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // Név részletre keres (kis- és nagybetű függetlenül) ÉS maximális árra szűr
    List<Product> findByNameContainingIgnoreCaseAndPriceLessThanEqual(String name, BigDecimal maxPrice);

    // Csak névrészletre keres
    List<Product> findByNameContainingIgnoreCase(String name);

    // Csak maximális árra szűr
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);
}
