package com.nimesh.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "units")
public class Unit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 45)
    private String name;
    
    @Column(name = "conversion_factor")
    private BigDecimal conversionFactor;
    
    @OneToMany(mappedBy = "unit", cascade = CascadeType.PERSIST)
    private List<Product> products = new ArrayList<>();
    
    // Constructors
    public Unit() {
    }
    
    public Unit(String name, BigDecimal conversionFactor) {
        this.name = name;
        this.conversionFactor = conversionFactor;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }
    
    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    @Override
    public String toString() {
        return name;
    }
}