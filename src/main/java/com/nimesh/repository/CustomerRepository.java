package com.nimesh.repository;

import com.nimesh.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCustomerType(String customerType);
    
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    Customer findByContactNo(String contactNo);
}