package com.nimesh.controller;

import com.nimesh.model.Category;
import com.nimesh.model.Product;
import com.nimesh.model.Unit;
import com.nimesh.service.ProductService;
import com.nimesh.service.UnitService;
import com.nimesh.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class ProductDialogController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UnitService unitService;
    
    @FXML
    private Label dialogTitleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField barcodeField;
    
    @FXML
    private ComboBox<Category> categoryComboBox;
    
    @FXML
    private ComboBox<Unit> unitComboBox;
    
    @FXML
    private TextField descriptionField;
    
    @FXML
    private TextField buyingPriceField;
    
    @FXML
    private TextField sellingPriceField;
    
    @FXML
    private TextField wholesalePriceField;
    
    @FXML
    private TextField stockField;
    
    @FXML
    private TextField reorderLevelField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private Product product;
    private boolean productSaved = false;
    private boolean isEditMode = false;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isProductSaved() {
        return productSaved;
    }
    
    public void initializeForAdd(ObservableList<Category> categories) {
        this.product = new Product();
        this.isEditMode = false;
        this.dialogTitleLabel.setText("Add New Product");
        setupCategoryComboBox(categories);
        setupUnitComboBox();
        
        // Set default values
        stockField.setText("0");
        reorderLevelField.setText("5");
    }
    
    public void initializeForEdit(Product product, ObservableList<Category> categories) {
        this.product = product;
        this.isEditMode = true;
        this.dialogTitleLabel.setText("Edit Product");
        setupCategoryComboBox(categories);
        setupUnitComboBox();
        
        // Populate fields with product data
        nameField.setText(product.getName());
        barcodeField.setText(product.getBarcode());
        descriptionField.setText(product.getDescription());
        
        if (product.getBuyingPrice() != null) {
            buyingPriceField.setText(product.getBuyingPrice().toString());
        }
        
        if (product.getSellingPrice() != null) {
            sellingPriceField.setText(product.getSellingPrice().toString());
        }
        
        if (product.getWholesalePrice() != null) {
            wholesalePriceField.setText(product.getWholesalePrice().toString());
        }
        
        if (product.getCurrentStock() != null) {
            stockField.setText(product.getCurrentStock().toString());
        }
        
        if (product.getReorderLevel() != null) {
            reorderLevelField.setText(product.getReorderLevel().toString());
        }
        
        if (product.getCategory() != null) {
            categoryComboBox.getSelectionModel().select(product.getCategory());
        }
        
        if (product.getUnit() != null) {
            unitComboBox.getSelectionModel().select(product.getUnit());
        }
    }
    
    private void setupCategoryComboBox(ObservableList<Category> categories) {
        categoryComboBox.setItems(categories);
        
        // Set cell factory to display category name
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }
            
            @Override
            public Category fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(category -> category.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }
    
    private void setupUnitComboBox() {
        // Load units from the database
        ObservableList<Unit> units = FXCollections.observableArrayList(unitService.getAllUnits());
        unitComboBox.setItems(units);
        
        // Set cell factory to display unit name
        unitComboBox.setConverter(new StringConverter<Unit>() {
            @Override
            public String toString(Unit unit) {
                return unit != null ? unit.getName() : "";
            }
            
            @Override
            public Unit fromString(String string) {
                return unitComboBox.getItems().stream()
                        .filter(unit -> unit.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        
        product.setName(nameField.getText().trim());
        product.setBarcode(barcodeField.getText().trim());
        product.setDescription(descriptionField.getText().trim());
        product.setCategory(categoryComboBox.getValue());
        product.setUnit(unitComboBox.getValue());
        
       try {
    product.setBuyingPrice(new BigDecimal(buyingPriceField.getText().trim()));
    product.setSellingPrice(new BigDecimal(sellingPriceField.getText().trim()));
    
    String wholesalePrice = wholesalePriceField.getText().trim();
    if (!wholesalePrice.isEmpty()) {
        product.setWholesalePrice(new BigDecimal(wholesalePrice));
    } else {
        product.setWholesalePrice(null);  // Allow empty wholesale price
    }
    
    // Updated to use BigDecimal for stock values
    product.setCurrentStock(new BigDecimal(stockField.getText().trim()));
    product.setReorderLevel(new BigDecimal(reorderLevelField.getText().trim()));
} catch (NumberFormatException e) {
    AlertHelper.showErrorAlert("Validation Error", "Invalid Number Format", 
            "Please enter valid numbers for prices, stock, and reorder level.");
    return;
}
        
        // Set timestamps
        if (!isEditMode) {
            product.setDateAdded(LocalDateTime.now());
        }
        product.setLastUpdated(LocalDateTime.now());
        
        // Save to database
        Product savedProduct = productService.saveProduct(product);
        
        if (savedProduct != null && savedProduct.getId() != null) {
            productSaved = true;
            dialogStage.close();
        } else {
            AlertHelper.showErrorAlert("Save Error", "Could Not Save Product", 
                    "An error occurred while saving the product. Please try again.");
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
    
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        // Required fields validation
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage.append("Product name is required.\n");
        }
        
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Category is required.\n");
        }
        
        if (unitComboBox.getValue() == null) {
            errorMessage.append("Unit is required.\n");
        }
        
        // Validate prices
        try {
            if (buyingPriceField.getText().trim().isEmpty()) {
                errorMessage.append("Buying price is required.\n");
            } else {
                BigDecimal buyingPrice = new BigDecimal(buyingPriceField.getText().trim());
                if (buyingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Buying price must be greater than zero.\n");
                }
            }
            
            if (sellingPriceField.getText().trim().isEmpty()) {
                errorMessage.append("Selling price is required.\n");
            } else {
                BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText().trim());
                if (sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Selling price must be greater than zero.\n");
                }
                
                // Compare selling price with buying price
                if (!buyingPriceField.getText().trim().isEmpty()) {
                    BigDecimal buyingPrice = new BigDecimal(buyingPriceField.getText().trim());
                    if (sellingPrice.compareTo(buyingPrice) < 0) {
                        errorMessage.append("Warning: Selling price is less than buying price.\n");
                    }
                }
            }
            
            // Wholesale price is optional
            if (!wholesalePriceField.getText().trim().isEmpty()) {
                BigDecimal wholesalePrice = new BigDecimal(wholesalePriceField.getText().trim());
                if (wholesalePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Wholesale price must be greater than zero if provided.\n");
                }
                
                // Compare wholesale price with buying price
                if (!buyingPriceField.getText().trim().isEmpty()) {
                    BigDecimal buyingPrice = new BigDecimal(buyingPriceField.getText().trim());
                    if (wholesalePrice.compareTo(buyingPrice) < 0) {
                        errorMessage.append("Warning: Wholesale price is less than buying price.\n");
                    }
                }
                
                // Compare wholesale price with selling price
                if (!sellingPriceField.getText().trim().isEmpty()) {
                    BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText().trim());
                    if (wholesalePrice.compareTo(sellingPrice) > 0) {
                        errorMessage.append("Warning: Wholesale price is greater than selling price.\n");
                    }
                }
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Please enter valid numbers for all price fields.\n");
        }
        
        // Validate stock and reorder level
        try {
    if (stockField.getText().trim().isEmpty()) {
        errorMessage.append("Initial stock is required.\n");
    } else {
        BigDecimal stock = new BigDecimal(stockField.getText().trim());
        if (stock.compareTo(BigDecimal.ZERO) < 0) {
            errorMessage.append("Initial stock cannot be negative.\n");
        }
    }
    
    if (reorderLevelField.getText().trim().isEmpty()) {
        errorMessage.append("Reorder level is required.\n");
    } else {
        BigDecimal reorderLevel = new BigDecimal(reorderLevelField.getText().trim());
        if (reorderLevel.compareTo(BigDecimal.ZERO) < 0) {
            errorMessage.append("Reorder level cannot be negative.\n");
        }
    }
} catch (NumberFormatException e) {
    errorMessage.append("Please enter valid decimal numbers for stock and reorder level.\n");
}
        
        if (errorMessage.length() > 0) {
            // Check if there are only warnings (all warnings start with "Warning:")
            boolean onlyWarnings = true;
            for (String line : errorMessage.toString().split("\n")) {
                if (!line.trim().startsWith("Warning:")) {
                    onlyWarnings = false;
                    break;
                }
            }
            
            if (onlyWarnings) {
                // If there are only warnings, show a confirmation dialog
                boolean confirmSave = AlertHelper.showConfirmationAlert("Save with Warnings", 
                        "There are some warnings. Do you want to continue?", 
                        errorMessage.toString());
                
                return confirmSave;
            } else {
                // If there are errors, show an error dialog
                AlertHelper.showErrorAlert("Validation Error", "Please correct the following errors:", 
                        errorMessage.toString());
                return false;
            }
        }
        
        return true;
    }
}