package com.nimesh.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item_batches")
public class InvoiceItemBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "invoice_item_id", nullable = false)
    private InvoiceItem invoiceItem;
    
    @ManyToOne
    @JoinColumn(name = "product_batch_id", nullable = false)
    private ProductBatch productBatch;
    
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    
// Updated InvoiceItemBatch constructors

public InvoiceItemBatch() {
}

/**
 * Constructor that uses the batch's own prices (legacy - mainly for backwards compatibility)
 */
public InvoiceItemBatch(InvoiceItem invoiceItem, ProductBatch productBatch, BigDecimal quantity) {
    this.invoiceItem = invoiceItem;
    this.productBatch = productBatch;
    this.quantity = quantity;
    
    // Always use the batch's buying price for unit cost
    this.unitCost = productBatch.getBuyingPrice();
    
    // For backwards compatibility, use batch prices based on customer type
    // However, in practice, we should use the constructor with explicit unit price
    if ("WHOLESALE".equals(invoiceItem.getInvoice().getCustomerType()) && productBatch.getWholesalePrice() != null) {
        this.unitPrice = productBatch.getWholesalePrice();
    } else {
        this.unitPrice = productBatch.getSellingPrice();
    }
}

/**
 * Constructor with explicit unit price (recommended)
 * This preserves any manual price edits made in the POS
 */
public InvoiceItemBatch(InvoiceItem invoiceItem, ProductBatch productBatch, 
                       BigDecimal quantity, BigDecimal unitPrice) {
    this.invoiceItem = invoiceItem;
    this.productBatch = productBatch;
    this.quantity = quantity;
    
    // Always use the batch's actual buying price for unit cost
    this.unitCost = productBatch.getBuyingPrice();
    
    // Use the explicitly provided selling price (preserves manual edits)
    this.unitPrice = unitPrice;
}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public InvoiceItem getInvoiceItem() {
        return invoiceItem;
    }
    
    public void setInvoiceItem(InvoiceItem invoiceItem) {
        this.invoiceItem = invoiceItem;
    }
    
    public ProductBatch getProductBatch() {
        return productBatch;
    }
    
    public void setProductBatch(ProductBatch productBatch) {
        this.productBatch = productBatch;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitCost() {
        return unitCost;
    }
    
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    /**
     * Calculate the profit for this batch usage
     */
    public BigDecimal calculateProfit() {
        return unitPrice.subtract(unitCost).multiply(quantity);
    }
    
    /**
     * Calculate the total cost (buying price * quantity)
     */
    public BigDecimal getTotalCost() {
        return unitCost.multiply(quantity);
    }
    
    /**
     * Calculate the total selling amount (selling price * quantity)
     */
    public BigDecimal getTotalSellingAmount() {
        return unitPrice.multiply(quantity);
    }
}