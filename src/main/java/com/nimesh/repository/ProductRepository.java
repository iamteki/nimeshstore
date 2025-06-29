package com.nimesh.repository;

import com.nimesh.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByBarcode(String barcode);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryId(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.reorderLevel")
    List<Product> findLowStockProducts();
}