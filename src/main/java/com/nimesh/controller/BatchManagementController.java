package com.nimesh.controller;

import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.service.ProductBatchService;
import com.nimesh.service.ProductService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.DecimalFormatter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Controller
public class BatchManagementController {
    
    @FXML
    private TableView<ProductBatch> batchTable;
    
    @FXML
    private TableColumn<ProductBatch, String> batchNumberColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> purchaseDateColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> expiryDateColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> buyingPriceColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> sellingPriceColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> wholesalePriceColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> initialQuantityColumn;
    
    @FXML
    private TableColumn<ProductBatch, String> remainingQuantityColumn;
    
    @FXML
    private ComboBox<Product> productComboBox;
    
    @FXML
    private TextField productSearchField;
    
    @FXML
    private Label clearSearchButton;
    
    @FXML
    private Button addBatchButton;
    
    @FXML
    private Button editBatchButton;
    
    @FXML
    private Label dateTimeLabel;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductBatchService productBatchService;
    
    private ObservableList<ProductBatch> batchData = FXCollections.observableArrayList();
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;
    private Product selectedProduct;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeFormatter headerDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy | hh:mm a");
    
    @FXML
    public void initialize() {
        // Set current date and time in header
        updateDateTime();
        
        // Initialize table columns
        batchNumberColumn.setCellValueFactory(new PropertyValueFactory<>("batchNumber"));
        
        purchaseDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getPurchaseDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });
        
        expiryDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getExpiryDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "N/A");
        });
        
        buyingPriceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DecimalFormatter.format(cellData.getValue().getBuyingPrice())));
            
        sellingPriceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DecimalFormatter.format(cellData.getValue().getSellingPrice())));
            
        wholesalePriceColumn.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getWholesalePrice();
            return new SimpleStringProperty(price != null ? DecimalFormatter.format(price) : "N/A");
        });
        
        initialQuantityColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DecimalFormatter.format(cellData.getValue().getInitialQuantity())));
            
        remainingQuantityColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DecimalFormatter.format(cellData.getValue().getRemainingQuantity())));
        
        // Set up product combo box with search functionality
        setupProductSearch();
        
        // Connect table to data
        batchTable.setItems(batchData);
        
        // Set up button actions
        clearSearchButton.setOnMouseClicked(event -> clearSearch());
        addBatchButton.setOnAction(event -> showAddBatchDialog());
        editBatchButton.setOnAction(event -> {
            ProductBatch selectedBatch = batchTable.getSelectionModel().getSelectedItem();
            if (selectedBatch != null) {
                showEditBatchDialog(selectedBatch);
            } else {
                AlertHelper.showErrorAlert("No Selection", "No Batch Selected", "Please select a batch to edit.");
            }
        });
        
        // Add row factory to apply color styles based on stock status
        batchTable.setRowFactory(tv -> new TableRow<ProductBatch>() {
            @Override
            protected void updateItem(ProductBatch batch, boolean empty) {
                super.updateItem(batch, empty);
                
                if (batch == null || empty) {
                    setStyle("");
                    getStyleClass().removeAll("expired-row", "low-stock-row", "out-of-stock-row");
                    return;
                }
                
                // Clear previous styles
                getStyleClass().removeAll("expired-row", "low-stock-row", "out-of-stock-row");
                
                // Check batch expiry
                if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDateTime.now())) {
                    getStyleClass().add("expired-row");
                    return;
                }
                
                // Check batch stock status
                BigDecimal remainingQty = batch.getRemainingQuantity();
                if (remainingQty.compareTo(BigDecimal.ZERO) == 0) {
                    getStyleClass().add("out-of-stock-row");
                } else if (remainingQty.compareTo(new BigDecimal("5.0")) < 0) {
                    getStyleClass().add("low-stock-row");
                }
            }
        });
    }
    
    /**
     * Updates the date/time displayed in the header
     */
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        dateTimeLabel.setText(now.format(headerDateFormatter));
    }
    
    /**
     * Clears the search field and resets the product combo box
     */
    private void clearSearch() {
        productSearchField.clear();
        filteredProducts.setPredicate(p -> true);
        productComboBox.getSelectionModel().clearSelection();
        selectedProduct = null;
        batchData.clear();
        
        // Set focus back to search field for convenience
        productSearchField.requestFocus();
    }
    
    private void setupProductSearch() {
        // Load all products
        List<Product> products = productService.getAllProducts();
        allProducts.addAll(products);
        
        // Create a filtered list wrapping the observable list
        filteredProducts = new FilteredList<>(allProducts, p -> true);
        
        // Set the filtered list as the items of the ComboBox
        productComboBox.setItems(filteredProducts);
        
        // Setup the display value for each product in the combo box
        productComboBox.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    // Just show the product name without stock info
                    setText(product.getName());
                }
            }
        });
        
        // Same for the button cell
        productComboBox.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName());
                }
            }
        });
        
        // Add listener to the search field
        productSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProducts.setPredicate(createPredicate(newValue));
            
            // Make clear button visible only when there's text
            clearSearchButton.setVisible(newValue != null && !newValue.isEmpty());
            
            // If filtered list is not empty, show the popup
            if (!filteredProducts.isEmpty() && !productComboBox.isShowing() && newValue != null && !newValue.isEmpty()) {
                productComboBox.show();
            } else if (newValue == null || newValue.isEmpty()) {
                productComboBox.hide();
            }
        });
        
        // Initialize clear button visibility
        clearSearchButton.setVisible(false);
        
        // Add event handler to search field to handle keyboard actions
        productSearchField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN:
                    // Move selection to first item and show dropdown
                    if (!productComboBox.isShowing()) {
                        productComboBox.show();
                    }
                    productComboBox.getSelectionModel().selectFirst();
                    event.consume();
                    break;
                case ENTER:
                    // Select first item if dropdown is showing
                    if (productComboBox.isShowing() && !filteredProducts.isEmpty()) {
                        productComboBox.getSelectionModel().selectFirst();
                        productComboBox.hide();
                    }
                    event.consume();
                    break;
                case ESCAPE:
                    // Clear search if text exists, otherwise hide dropdown
                    if (!productSearchField.getText().isEmpty()) {
                        clearSearch();
                    } else {
                        productComboBox.hide();
                    }
                    event.consume();
                    break;
                default:
                    break;
            }
        });
        
        // Add listener to product combo box
        productComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProduct = newVal;
                productSearchField.setText(newVal.getName());
                clearSearchButton.setVisible(true);
                loadBatchesForProduct(selectedProduct.getId());
            }
        });
    }
    
    /**
     * Creates a predicate for filtering products based on search text
     */
    private Predicate<Product> createPredicate(String searchText) {
        return product -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            String lowerCaseSearch = searchText.toLowerCase();
            
            // Check if product name contains search text
            if (product.getName().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }
            
            // Check if barcode contains search text (if barcode exists)
            if (product.getBarcode() != null && product.getBarcode().toLowerCase().contains(lowerCaseSearch)) {
                return true;
            }
            
            return false;
        };
    }
    
    private void loadBatchesForProduct(Long productId) {
        List<ProductBatch> batches = productBatchService.getBatchesByProductId(productId);
        batchData.clear();
        batchData.addAll(batches);
    }
    
    private void showAddBatchDialog() {
        if (selectedProduct == null) {
            AlertHelper.showErrorAlert("No Selection", "No Product Selected", "Please select a product first.");
            return;
        }
        
        // Create dialog
        Dialog<ProductBatch> dialog = new Dialog<>();
        dialog.setTitle("Add New Batch");
        dialog.setHeaderText("Add a new batch for " + selectedProduct.getName());
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        TextField buyingPriceField = new TextField();
        buyingPriceField.setPromptText("Buying Price");
        buyingPriceField.setText(selectedProduct.getBuyingPrice().toString());
        
        TextField sellingPriceField = new TextField();
        sellingPriceField.setPromptText("Selling Price");
        sellingPriceField.setText(selectedProduct.getSellingPrice().toString());
        
        TextField wholesalePriceField = new TextField();
        wholesalePriceField.setPromptText("Wholesale Price (optional)");
        if (selectedProduct.getWholesalePrice() != null) {
            wholesalePriceField.setText(selectedProduct.getWholesalePrice().toString());
        }
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        
        DatePicker expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Expiry Date (optional)");
        
        // Create layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        grid.add(new Label("Buying Price:"), 0, 0);
        grid.add(buyingPriceField, 1, 0);
        grid.add(new Label("Selling Price:"), 0, 1);
        grid.add(sellingPriceField, 1, 1);
        grid.add(new Label("Wholesale Price:"), 0, 2);
        grid.add(wholesalePriceField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Expiry Date:"), 0, 4);
        grid.add(expiryDatePicker, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the price field by default
        buyingPriceField.requestFocus();
        
        // Convert the result to a batch when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal buyingPrice = new BigDecimal(buyingPriceField.getText());
                    BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText());
                    BigDecimal wholesalePrice = wholesalePriceField.getText().isEmpty() ? 
                            null : new BigDecimal(wholesalePriceField.getText());
                    BigDecimal quantity = new BigDecimal(quantityField.getText());
                    
                    LocalDateTime expiryDate = null;
                    if (expiryDatePicker.getValue() != null) {
                        expiryDate = expiryDatePicker.getValue().atStartOfDay();
                    }
                    
                    // Create the batch
                    return productBatchService.createBatch(
                        selectedProduct.getId(),
                        quantity,
                        buyingPrice,
                        sellingPrice,
                        wholesalePrice,
                        null, // No purchase order
                        expiryDate
                    );
                } catch (NumberFormatException e) {
                    AlertHelper.showErrorAlert("Invalid Input", "Invalid Entry", 
                                              "Please enter valid numeric values for prices and quantity.");
                    return null;
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<ProductBatch> result = dialog.showAndWait();
        
        result.ifPresent(batch -> {
            // Refresh the table
            loadBatchesForProduct(selectedProduct.getId());
        });
    }
    
    private void showEditBatchDialog(ProductBatch batch) {
        // Similar to add dialog but pre-filled with batch values
        // Only allow editing selling prices and expiry date, not quantities or buying price
        
        // Create dialog
        Dialog<ProductBatch> dialog = new Dialog<>();
        dialog.setTitle("Edit Batch");
        dialog.setHeaderText("Edit batch #" + batch.getBatchNumber());
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields - only editable fields
        TextField sellingPriceField = new TextField();
        sellingPriceField.setPromptText("Selling Price");
        sellingPriceField.setText(batch.getSellingPrice().toString());
        
        TextField wholesalePriceField = new TextField();
        wholesalePriceField.setPromptText("Wholesale Price (optional)");
        if (batch.getWholesalePrice() != null) {
            wholesalePriceField.setText(batch.getWholesalePrice().toString());
        }
        
        DatePicker expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Expiry Date (optional)");
        if (batch.getExpiryDate() != null) {
            expiryDatePicker.setValue(batch.getExpiryDate().toLocalDate());
        }
        
        // Read-only fields
        Label batchNumberValue = new Label(batch.getBatchNumber());
        Label purchaseDateValue = new Label(batch.getPurchaseDate().format(dateFormatter));
        Label buyingPriceValue = new Label(DecimalFormatter.format(batch.getBuyingPrice()));
        Label initialQuantityValue = new Label(DecimalFormatter.format(batch.getInitialQuantity()));
        Label remainingQuantityValue = new Label(DecimalFormatter.format(batch.getRemainingQuantity()));
        
        // Create layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        // Read-only fields
        grid.add(new Label("Batch Number:"), 0, 0);
        grid.add(batchNumberValue, 1, 0);
        grid.add(new Label("Purchase Date:"), 0, 1);
        grid.add(purchaseDateValue, 1, 1);
        grid.add(new Label("Buying Price:"), 0, 2);
        grid.add(buyingPriceValue, 1, 2);
        grid.add(new Label("Initial Quantity:"), 0, 3);
        grid.add(initialQuantityValue, 1, 3);
        grid.add(new Label("Remaining Quantity:"), 0, 4);
        grid.add(remainingQuantityValue, 1, 4);
        
        // Editable fields
        grid.add(new Label("Selling Price:"), 0, 5);
        grid.add(sellingPriceField, 1, 5);
        grid.add(new Label("Wholesale Price:"), 0, 6);
        grid.add(wholesalePriceField, 1, 6);
        grid.add(new Label("Expiry Date:"), 0, 7);
        grid.add(expiryDatePicker, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the selling price field by default
        sellingPriceField.requestFocus();
        
        // Convert the result to a batch when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText());
                    BigDecimal wholesalePrice = wholesalePriceField.getText().isEmpty() ? 
                            null : new BigDecimal(wholesalePriceField.getText());
                    
                    LocalDateTime expiryDate = null;
                    if (expiryDatePicker.getValue() != null) {
                        expiryDate = expiryDatePicker.getValue().atStartOfDay();
                    }
                    
                    // Update the batch
                    batch.setSellingPrice(sellingPrice);
                    batch.setWholesalePrice(wholesalePrice);
                    batch.setExpiryDate(expiryDate);
                    
                    return productBatchService.updateBatch(batch);
                } catch (NumberFormatException e) {
                    AlertHelper.showErrorAlert("Invalid Input", "Invalid Entry", 
                                             "Please enter valid numeric values for prices.");
                    return null;
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<ProductBatch> result = dialog.showAndWait();
        
        result.ifPresent(updatedBatch -> {
            // Refresh the table
            loadBatchesForProduct(selectedProduct.getId());
        });
    }
}