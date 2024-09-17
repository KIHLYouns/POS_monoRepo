package com.dev.inventoryManagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.inventoryManagement.models.Product;
import com.dev.inventoryManagement.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product saveProduct(Product product) {
        // Add validation logic (e.g., check for null or empty fields)
        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product product) {
        // Add validation logic
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setCategory(product.getCategory());
                    // Update other fields as needed
                    return productRepository.save(existingProduct);
                });
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
