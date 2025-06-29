package com.nimesh.repository;

import com.nimesh.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    Supplier findByContactNo(String contactNo);
    
    Supplier findByEmail(String email);
}