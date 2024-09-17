package com.dev.inventoryManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.inventoryManagement.models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Custom queries (e.g., find by category, filter by price range) can go here
}
