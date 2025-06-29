package com.nimesh.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches")
public class ProductBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "batch_number", nullable = false)
    private String batchNumber;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "buying_price", nullable = false)
    private BigDecimal buyingPrice;
    
    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice;
    
    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;
    
    @Column(name = "initial_quantity", nullable = false)
    private BigDecimal initialQuantity;
    
    @Column(name = "remaining_quantity", nullable = false)
    private BigDecimal remainingQuantity;
    
    @Column(name = "supplier_reference")
    private String supplierReference;
    
    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;
    
    // Constructors
    public ProductBatch() {
        this.purchaseDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public BigDecimal getBuyingPrice() {
        return buyingPrice;
    }
    
    public void setBuyingPrice(BigDecimal buyingPrice) {
        this.buyingPrice = buyingPrice;
    }
    
    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }
    
    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    
    public BigDecimal getWholesalePrice() {
        return wholesalePrice;
    }
    
    public void setWholesalePrice(BigDecimal wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }
    
    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }
    
    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }
    
    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }
    
    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
    
    public String getSupplierReference() {
        return supplierReference;
    }
    
    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }
    
    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }
    
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
    
    /**
     * Checks if there is enough quantity remaining in this batch
     * @param requestedQuantity The quantity requested
     * @return true if enough quantity is available, false otherwise
     */
    public boolean hasEnoughQuantity(BigDecimal requestedQuantity) {
        return remainingQuantity.compareTo(requestedQuantity) >= 0;
    }
    
    /**
     * Reduces the remaining quantity by the given amount
     * @param quantity The quantity to reduce
     * @return true if reduction was successful, false if not enough remaining
     */
    public boolean reduceQuantity(BigDecimal quantity) {
        if (hasEnoughQuantity(quantity)) {
            remainingQuantity = remainingQuantity.subtract(quantity);
            return true;
        }
        return false;
    }
    
    /**
     * Generate a batch number based on product and date
     */
    public static String generateBatchNumber(Product product, LocalDateTime purchaseDate) {
        String productId = String.format("%06d", product.getId());
        String dateStr = purchaseDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "B" + productId + "-" + dateStr;
    }
}