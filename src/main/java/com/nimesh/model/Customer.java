package com.nimesh.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "contact_no")
    private String contactNo;
    
    @Column
    private String address;
    
    @Column(name = "customer_type", nullable = false)
    private String customerType; // RETAIL, WHOLESALE
    
    @OneToMany(mappedBy = "customer")
    private List<Invoice> invoices = new ArrayList<>();
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private CreditAccount creditAccount;
    
    // Constructors
    public Customer() {
    }
    
    public Customer(String name, String contactNo, String address, String customerType) {
        this.name = name;
        this.contactNo = contactNo;
        this.address = address;
        this.customerType = customerType;
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

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public CreditAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(CreditAccount creditAccount) {
        this.creditAccount = creditAccount;
    }
    
    @Override
    public String toString() {
        return name;
    }
}