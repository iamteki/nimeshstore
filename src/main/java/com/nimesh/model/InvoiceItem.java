package com.nimesh.model;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private BigDecimal total;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "buying_price")
    private BigDecimal buyingPrice;
    
    // Constructors
    public InvoiceItem() {
    }
    
    public InvoiceItem(Product product, BigDecimal quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    // Helper methods
    public void calculateTotal() {
        BigDecimal itemTotal = this.unitPrice.multiply(this.quantity);
        this.total = itemTotal.subtract(this.discountAmount);
    }
    
    /**
     * Calculate profit for this item based on buying price
     * @return The profit amount
     */
    public BigDecimal calculateProfit() {
        if (this.buyingPrice == null) {
            // Fallback to product's buying price if item buying price not set
            if (this.product != null) {
                return this.total.subtract(this.product.getBuyingPrice().multiply(this.quantity));
            }
            return BigDecimal.ZERO;
        }
        return this.total.subtract(this.buyingPrice.multiply(this.quantity));
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Invoice getInvoice() {
        return invoice;
    }
    
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotal();
    }
    
    public BigDecimal getBuyingPrice() {
        return buyingPrice;
    }
    
    public void setBuyingPrice(BigDecimal buyingPrice) {
        this.buyingPrice = buyingPrice;
    }
}