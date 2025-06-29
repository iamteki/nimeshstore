package com.nimesh.service;

import com.nimesh.model.Supplier;
import com.nimesh.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    /**
     * Retrieves all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    /**
     * Retrieves a supplier by ID
     */
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }
    
    /**
     * Searches suppliers by name
     */
    public List<Supplier> searchSuppliersByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Saves a supplier (creates or updates)
     */
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    /**
     * Deletes a supplier by ID
     */
    public boolean deleteSupplier(Long id) {
        if (supplierRepository.existsById(id)) {
            supplierRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a supplier exists by contact number
     */
    public boolean existsByContactNo(String contactNo) {
        return supplierRepository.findByContactNo(contactNo) != null;
    }
    
    /**
     * Checks if a supplier exists by email
     */
    public boolean existsByEmail(String email) {
        return supplierRepository.findByEmail(email) != null;
    }
}