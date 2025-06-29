package com.nimesh.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "contact_no")
    private String contactNo;
    
    @Column
    private String email;
    
    @Column
    private String address;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PurchaseOrder> orders = new ArrayList<>();
    
    // Constructors
    public Supplier() {
    }
    
    public Supplier(String name, String contactPerson, String contactNo, String email, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
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

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<PurchaseOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<PurchaseOrder> orders) {
        this.orders = orders;
    }
    
    @Override
    public String toString() {
        return name;
    }
}