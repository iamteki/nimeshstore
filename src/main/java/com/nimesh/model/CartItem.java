package com.nimesh.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart in the POS interface.
 * This is not a database entity but a helper class for the UI.
 */
public class CartItem {
    private Product product;
    private final StringProperty name;
    private final StringProperty unit;
    private final ObjectProperty<BigDecimal> unitPrice;
    private final ObjectProperty<BigDecimal> quantity;
    private final ObjectProperty<BigDecimal> total;
    private final ObjectProperty<Button> actionButton;
    
    public CartItem(Product product, BigDecimal quantity, BigDecimal unitPrice) {
        this.product = product;
        this.name = new SimpleStringProperty(product.getName());
        this.unit = new SimpleStringProperty(product.getUnit() != null ? product.getUnit().getName() : "");
        this.unitPrice = new SimpleObjectProperty<>(unitPrice);
        this.quantity = new SimpleObjectProperty<>(quantity);
        this.total = new SimpleObjectProperty<>(unitPrice.multiply(quantity));
        
        // Create remove button
        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("remove-button");
        this.actionButton = new SimpleObjectProperty<>(removeButton);
    }
    
    /**
     * Updates the total when quantity or unit price changes
     */
    private final ObjectProperty<BigDecimal> discountAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);

    public void updateTotal() {
        BigDecimal itemTotal = unitPrice.get().multiply(quantity.get());
        BigDecimal discountedTotal = itemTotal.subtract(discountAmount.get());
        total.set(discountedTotal.compareTo(BigDecimal.ZERO) > 0 ? discountedTotal : BigDecimal.ZERO);
    }

    // Updated toInvoiceItem method in CartItem.java

public InvoiceItem toInvoiceItem() {
    // Create the invoice item with the current unit price (which may have been manually edited)
    InvoiceItem invoiceItem = new InvoiceItem(product, quantity.get(), unitPrice.get());
    invoiceItem.setDiscountAmount(discountAmount.get());
    
    // The buying price will be set in InvoiceService based on the pricing strategy
    // but we preserve the selling price as set in the cart (including manual edits)
    
    return invoiceItem;
}
    // Add getter and setter
    public BigDecimal getDiscountAmount() { return discountAmount.get(); }
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount.set(discountAmount);
        updateTotal();
    }
    public ObjectProperty<BigDecimal> discountAmountProperty() { return discountAmount; }
    
    // Getters
    public Product getProduct() {
        return product;
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getUnit() {
        return unit.get();
    }
    
    public StringProperty unitProperty() {
        return unit;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice.get();
    }
    
    public ObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPrice;
    }
    
    public BigDecimal getQuantity() {
        return quantity.get();
    }
    
    public ObjectProperty<BigDecimal> quantityProperty() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity.set(quantity);
        updateTotal();
    }
    
    public BigDecimal getTotal() {
        return total.get();
    }
    
    public ObjectProperty<BigDecimal> totalProperty() {
        return total;
    }
    
    public Button getActionButton() {
        return actionButton.get();
    }
    
    public ObjectProperty<Button> actionButtonProperty() {
        return actionButton;
    }
    
    // Add this to the CartItem class to handle quantity updates
public void incrementQuantity() {
    this.quantity.set(this.quantity.get().add(BigDecimal.ONE));
    updateTotal();
}

public void decrementQuantity() {
    if (this.quantity.get().compareTo(BigDecimal.ONE) > 0) {
        this.quantity.set(this.quantity.get().subtract(BigDecimal.ONE));
        updateTotal();
    }
}

public boolean setQuantity(String quantityStr) {
    try {
        BigDecimal newQuantity = new BigDecimal(quantityStr);
        if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            this.quantity.set(newQuantity);
            updateTotal();
            return true;
        }
        return false;
    } catch (NumberFormatException e) {
        return false;
    }
}

// Add this to CartItem class
//public boolean setUnitPrice(String priceStr) {
//    try {
//        BigDecimal newPrice = new BigDecimal(priceStr);
//        if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
//            this.unitPrice.set(newPrice);
//            updateTotal();
//            return true;
//        }
//        return false;
//    } catch (NumberFormatException e) {
//        return false;
//    }
//}

// In CartItem.java, add or modify these methods
public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice.set(unitPrice);
    updateTotal();
}



}