package com.nimesh.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "credit_accounts")
public class CreditAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "customer_id", unique = true)
    private Customer customer;
    
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "credit_limit")
    private BigDecimal creditLimit;
    
    // Constructors
    public CreditAccount() {
    }
    
    public CreditAccount(Customer customer, BigDecimal creditLimit) {
        this.customer = customer;
        this.creditLimit = creditLimit;
        this.balance = BigDecimal.ZERO;
    }
    
    // Helper methods
    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    
    public void subtractFromBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
    
    public boolean hasAvailableCredit(BigDecimal amount) {
        if (creditLimit == null) {
            return true; // No limit set
        }
        
        BigDecimal potentialBalance = balance.add(amount);
        return potentialBalance.compareTo(creditLimit) <= 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
}