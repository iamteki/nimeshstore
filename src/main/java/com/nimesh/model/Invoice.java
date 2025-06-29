package com.nimesh.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    
    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;
    
    @Column(name = "cash_received")
    private BigDecimal cashReceived;
    
    @Column(name = "change_amount")
    private BigDecimal changeAmount;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // CASH, CREDIT
    
    @Column(name = "payment_status")
    private String paymentStatus; // PAID, PENDING, PARTIALLY_PAID
    
    @Column(name = "customer_type", nullable = false)
    private String customerType; // RETAIL, WHOLESALE
    
     @Column(name = "item_discounts_total")
    private BigDecimal itemDiscountsTotal = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();
    
    // Constructors
    public Invoice() {
        this.date = LocalDateTime.now();
        this.paymentStatus = "PAID"; // Default status
        this.cashReceived = BigDecimal.ZERO;
        this.changeAmount = BigDecimal.ZERO;
    }
    
    // Helper methods
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }
    
    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }
    
    public void calculateTotals() {
        // Calculate total amount from items
       BigDecimal itemsTotal = BigDecimal.ZERO;
        this.itemDiscountsTotal = BigDecimal.ZERO;
        
        for (InvoiceItem item : items) {
            itemsTotal = itemsTotal.add(item.getUnitPrice().multiply(item.getQuantity()));
            this.itemDiscountsTotal = this.itemDiscountsTotal.add(item.getDiscountAmount());
        }
        
        this.totalAmount = itemsTotal;
        
        if (this.discountPercentage != null && this.discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = this.totalAmount.subtract(this.itemDiscountsTotal)
                    .multiply(this.discountPercentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }
        
        this.finalAmount = this.totalAmount
                .subtract(this.itemDiscountsTotal)
                .subtract(this.discountAmount);
        
        // Calculate change if cash payment and cash received is set
        if ("CASH".equals(this.paymentMethod) && this.cashReceived != null 
                && this.cashReceived.compareTo(BigDecimal.ZERO) > 0) {
            this.changeAmount = this.cashReceived.subtract(this.finalAmount);
            if (this.changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.changeAmount = BigDecimal.ZERO; // Ensure change is not negative
            }
        }
    }
    
     // Add getter and setter
    public BigDecimal getItemDiscountsTotal() { return itemDiscountsTotal; }
    public void setItemDiscountsTotal(BigDecimal itemDiscountsTotal) {
        this.itemDiscountsTotal = itemDiscountsTotal;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public BigDecimal getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(BigDecimal cashReceived) {
        this.cashReceived = cashReceived;
        // Auto-calculate change when cash received is set
        if (cashReceived != null && finalAmount != null && "CASH".equals(paymentMethod)) {
            this.changeAmount = cashReceived.subtract(finalAmount);
            if (this.changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.changeAmount = BigDecimal.ZERO; // Ensure change is not negative
            }
        }
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        
        // Reset cash received and change if payment method is not CASH
        if (!"CASH".equals(paymentMethod)) {
            this.cashReceived = BigDecimal.ZERO;
            this.changeAmount = BigDecimal.ZERO;
        }
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }
}