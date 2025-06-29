package com.nimesh.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String barcode;
    
    @Column(name = "buying_price", nullable = false)
    private BigDecimal buyingPrice;
    
    @Column(name = "selling_price", nullable = false)
    private BigDecimal sellingPrice;
    
    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;
    
    @Column
    private String description;
    
    @Column(name = "current_stock", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentStock;  // Changed from Integer to BigDecimal
    
    @Column(name = "reorder_level", precision = 10, scale = 2)
    private BigDecimal reorderLevel;  // Changed from Integer to BigDecimal
    
    @Column(name = "date_added")
    private LocalDateTime dateAdded;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;
    
    // Constructors
    public Product() {
        this.dateAdded = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.currentStock = BigDecimal.ZERO;  // Initialize with zero
        this.reorderLevel = BigDecimal.ZERO;  // Initialize with zero
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
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getCurrentStock() {
        return currentStock;
    }
    
    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }
    
    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }
    
    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
    
    public LocalDateTime getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public Unit getUnit() {
        return unit;
    }
    
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
    /**
     * Checks if there is enough stock available
     * @param requestedQuantity The quantity requested for purchase
     * @return true if enough stock is available, false otherwise
     */
    public boolean hasEnoughStock(BigDecimal requestedQuantity) {
        return currentStock.compareTo(requestedQuantity) >= 0;
    }
    
    /**
     * Reduces the stock by the given quantity
     * @param quantity The quantity to reduce from stock
     * @return true if reduction was successful, false if not enough stock
     */
    public boolean reduceStock(BigDecimal quantity) {
        if (hasEnoughStock(quantity)) {
            currentStock = currentStock.subtract(quantity);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the given quantity to stock
     * @param quantity The quantity to add to stock
     */
    public void addStock(BigDecimal quantity) {
        currentStock = currentStock.add(quantity);
    }
    
    @Override
    public String toString() {
        return name;
    }
}