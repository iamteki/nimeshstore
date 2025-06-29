package com.nimesh.controller;

import com.nimesh.model.Category;
import com.nimesh.model.Product;
import com.nimesh.model.Unit;
import com.nimesh.service.CategoryService;
import com.nimesh.service.ProductService;
import com.nimesh.service.UnitService;
import com.nimesh.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import com.nimesh.service.ProductBatchService;
import com.nimesh.service.SystemConfigService;
import javafx.beans.property.SimpleObjectProperty;

@Controller
public class InventoryController implements Initializable {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UnitService unitService;
    
    @Autowired
    private ApplicationContext context;

    @FXML
    private BorderPane inventoryPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    // Products Tab
    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Long> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> barcodeColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;
    
    @FXML
    private TableColumn<Product, String> unitColumn;

    @FXML
    private TableColumn<Product, BigDecimal> buyingPriceColumn;

    @FXML
    private TableColumn<Product, BigDecimal> sellingPriceColumn;

    @FXML
    private TableColumn<Product, BigDecimal> wholesalePriceColumn;

    @FXML
    private TableColumn<Product, BigDecimal> stockColumn;

    @FXML
    private TableColumn<Product, BigDecimal> reorderLevelColumn;

    @FXML
    private Button addProductButton;

    @FXML
    private Button editProductButton;

    @FXML
    private Button deleteProductButton;

    @FXML
    private Button updateStockButton;

    // Categories Tab
    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, Long> categoryIdColumn;

    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableColumn<Category, String> categoryDescColumn;

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextField categoryDescField;

    @FXML
    private Button addCategoryButton;

    @FXML
    private Button updateCategoryButton;

    @FXML
    private Button deleteCategoryButton;

    @FXML
    private Button clearCategoryButton;

    // Low Stock Tab
    @FXML
    private TableView<Product> lowStockTable;

    @FXML
    private TableColumn<Product, Long> lowStockIdColumn;

    @FXML
    private TableColumn<Product, String> lowStockNameColumn;

    @FXML
    private TableColumn<Product, String> lowStockCategoryColumn;
    
    @FXML
    private TableColumn<Product, String> lowStockUnitColumn;

    @FXML
    private TableColumn<Product, BigDecimal> lowStockCurrentColumn;

    @FXML
    private TableColumn<Product, BigDecimal> lowStockReorderColumn;

    @FXML
    private TableColumn<Product, String> lowStockStatusColumn;

    @FXML
    private Button refreshLowStockButton;

    @FXML
    private Button orderSelectedButton;

    @FXML
    private Button orderAllButton;
    
    
    @Autowired
private ProductBatchService productBatchService;

@Autowired
private SystemConfigService configService;

    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private ObservableList<Unit> unitsList = FXCollections.observableArrayList();
    private ObservableList<Product> lowStockList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeProductTable();
        initializeCategoryTable();
        initializeLowStockTable();
        
        loadAllProducts();
        loadAllCategories();
        loadAllUnits();
        loadLowStockProducts();
        
        // Enable/disable buttons based on selection
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editProductButton.setDisable(!hasSelection);
            deleteProductButton.setDisable(!hasSelection);
            updateStockButton.setDisable(!hasSelection);
        });
        
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateCategoryButton.setDisable(!hasSelection);
            deleteCategoryButton.setDisable(!hasSelection);
            
            if (newSelection != null) {
                categoryNameField.setText(newSelection.getName());
                categoryDescField.setText(newSelection.getDescription());
            }
        });
        
        lowStockTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            orderSelectedButton.setDisable(newSelection == null);
        });
        
        // Initialize buttons to disabled state
        editProductButton.setDisable(true);
        deleteProductButton.setDisable(true);
        updateStockButton.setDisable(true);
        updateCategoryButton.setDisable(true);
        deleteCategoryButton.setDisable(true);
        orderSelectedButton.setDisable(true);
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadAllProducts();
            }
        });
        
        // Add key press event handler to search field to respond to Enter key
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });
    }
    
    // Extract search logic to a separate method that can be called from both the button click and Enter key press
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }
        
        productsList.clear();
        productsList.addAll(productService.searchProductsByName(searchTerm));
    }
    
   // Update the initializeProductTable method to show batch-based stock
private void initializeProductTable() {
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
    categoryColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        Category category = product.getCategory();
        return new SimpleStringProperty(category != null ? category.getName() : "");
    });
    unitColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        Unit unit = product.getUnit();
        return new SimpleStringProperty(unit != null ? unit.getName() : "");
    });
    buyingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("buyingPrice"));
    sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
    wholesalePriceColumn.setCellValueFactory(new PropertyValueFactory<>("wholesalePrice"));
    
    // Update stock column to show batch-based stock with strategy indication
    stockColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        BigDecimal batchStock = productBatchService.getTotalRemainingQuantity(product.getId());
        return new SimpleObjectProperty<>(batchStock != null ? batchStock : BigDecimal.ZERO);
    });
    
    reorderLevelColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
    
    // Format the columns to display the values nicely
    buyingPriceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.toString());
            }
        }
    });
    
    sellingPriceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.toString());
            }
        }
    });
    
    wholesalePriceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.toString());
            }
        }
    });
    
    // Custom cell factory for stock column to show strategy and batch info
    stockColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setTooltip(null);
            } else {
                setText(item.toString());
                
                // Add tooltip with batch information and current strategy
                Product product = getTableRow().getItem();
                if (product != null) {
                    String strategy = configService.getPricingStrategy();
                    List<ProductBatchService.BatchInfo> batchInfo = 
                        productBatchService.getBatchAvailabilityInfo(product.getId());
                    
                    StringBuilder tooltipText = new StringBuilder();
                    tooltipText.append("Pricing Strategy: ").append(strategy).append("\n");
                    tooltipText.append("Total Stock: ").append(item).append(" units\n");
                    
                    if (!batchInfo.isEmpty()) {
                        tooltipText.append("\nBatches:\n");
                        for (ProductBatchService.BatchInfo batch : batchInfo) {
                            tooltipText.append("• Batch #").append(batch.getBatchNumber())
                                     .append(": ").append(batch.getAvailableQuantity())
                                     .append(" units\n");
                        }
                    } else {
                        tooltipText.append("\nNo batch information available");
                    }
                    
                    setTooltip(new Tooltip(tooltipText.toString()));
                }
            }
        }
    });
    
    productTable.setItems(productsList);
}
    
    private void initializeCategoryTable() {
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        categoryTable.setItems(categoriesList);
    }
    
  // Update initializeLowStockTable to use batch-based stock levels
private void initializeLowStockTable() {
    lowStockIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    lowStockNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    lowStockCategoryColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        Category category = product.getCategory();
        return new SimpleStringProperty(category != null ? category.getName() : "");
    });
    lowStockUnitColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        Unit unit = product.getUnit();
        return new SimpleStringProperty(unit != null ? unit.getName() : "");
    });
    
    // Update to show batch-based current stock
    lowStockCurrentColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        BigDecimal batchStock = productBatchService.getTotalRemainingQuantity(product.getId());
        return new SimpleObjectProperty<>(batchStock != null ? batchStock : BigDecimal.ZERO);
    });
    
    lowStockReorderColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
    lowStockStatusColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        BigDecimal current = productBatchService.getTotalRemainingQuantity(product.getId());
        if (current == null) current = BigDecimal.ZERO;
        BigDecimal reorder = product.getReorderLevel();
        
        if (current.compareTo(BigDecimal.ZERO) <= 0) {
            return new SimpleStringProperty("Out of Stock");
        } else if (current.compareTo(reorder.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP)) <= 0) {
            return new SimpleStringProperty("Critical");
        } else {
            return new SimpleStringProperty("Low");
        }
    });
    
    // Set row styling based on status (updated for batch-based stock)
    lowStockTable.setRowFactory(tv -> new TableRow<Product>() {
        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);
            
            if (product == null || empty) {
                setStyle("");
            } else {
                BigDecimal current = productBatchService.getTotalRemainingQuantity(product.getId());
                if (current == null) current = BigDecimal.ZERO;
                BigDecimal reorder = product.getReorderLevel();
                BigDecimal half = reorder.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
                
                // First remove any existing style classes
                getStyleClass().removeAll("out-of-stock-row", "critical-stock-row", "low-stock-row");
                
                // Set appropriate style class based on stock status
                if (current.compareTo(BigDecimal.ZERO) <= 0) {
                    getStyleClass().add("out-of-stock-row");
                } else if (current.compareTo(half) <= 0) {
                    getStyleClass().add("critical-stock-row");
                } else {
                    getStyleClass().add("low-stock-row");
                }
            }
        }
    });
    
    lowStockTable.setItems(lowStockList);
}
    
    private void loadAllProducts() {
        productsList.clear();
        productsList.addAll(productService.getAllProducts());
    }
    
    private void loadAllCategories() {
        categoriesList.clear();
        categoriesList.addAll(categoryService.getAllCategories());
    }
    
    private void loadAllUnits() {
        unitsList.clear();
        unitsList.addAll(unitService.getAllUnits());
    }
    
    private void loadLowStockProducts() {
        lowStockList.clear();
        lowStockList.addAll(productService.getLowStockProducts());
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        performSearch();
    }
    
    @FXML
    private void handleAddProduct(ActionEvent event) {
        openProductDialog(null);
    }
    
    @FXML
    private void handleEditProduct(ActionEvent event) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            return;
        }
        
        openProductDialog(selectedProduct);
    }
    
    private void openProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            ProductDialogController controller = loader.getController();
            
            if (product == null) {
                controller.initializeForAdd(categoriesList);
            } else {
                controller.initializeForEdit(product, categoriesList);
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(product == null ? "Add New Product" : "Edit Product");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            // Get the main window
            Scene currentScene = inventoryPane.getScene();
            if (currentScene != null && currentScene.getWindow() != null) {
                dialogStage.initOwner(currentScene.getWindow());
            }
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isProductSaved()) {
                loadAllProducts();
                loadLowStockProducts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the product dialog.");
        }
    }
    
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
    
        try{
            
              Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            return;
        }
        
        boolean confirmDelete = AlertHelper.showConfirmationAlert("Confirm Deletion", 
                "Delete Product", 
                "Are you sure you want to delete the product: " + selectedProduct.getName() + "?");
        
        if (confirmDelete) {
            boolean deleted = productService.deleteProduct(selectedProduct.getId());
            
            if (deleted) {
                productsList.remove(selectedProduct);
                loadLowStockProducts();
                AlertHelper.showInformationAlert("Success", "Product Deleted", 
                        "Product was successfully deleted.");
            } else {
                AlertHelper.showErrorAlert("Error", "Delete Failed", 
                        "Could not delete the product. It might be referenced by other records.");
            }
        }
            
            
        }catch(Exception e){
              AlertHelper.showErrorAlert("Error", "Delete Failed", 
                        "Could not delete the product. It might be referenced by other records.");
        }
            
    }
    
    @FXML
    private void handleUpdateStock(ActionEvent event) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stock_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            StockDialogController controller = loader.getController();
            controller.initializeProduct(selectedProduct);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Stock");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            // Get the main window
            Scene currentScene = inventoryPane.getScene();
            if (currentScene != null && currentScene.getWindow() != null) {
                dialogStage.initOwner(currentScene.getWindow());
            }
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            controller.setProductService(productService);
            
            dialogStage.showAndWait();
            
            if (controller.isStockUpdated()) {
                loadAllProducts();
                loadLowStockProducts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the stock update dialog.");
        }
    }
    
    @FXML
    private void handleAddCategory(ActionEvent event) {
        String name = categoryNameField.getText().trim();
        String description = categoryDescField.getText().trim();
        
        if (name.isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Invalid Input", 
                    "Category name cannot be empty.");
            return;
        }
        
        // Check if category with this name already exists
        Category existingCategory = categoryService.getCategoryByName(name);
        if (existingCategory != null) {
            AlertHelper.showErrorAlert("Validation Error", "Duplicate Category", 
                    "A category with this name already exists.");
            return;
        }
        
        Category newCategory = new Category(name, description);
        Category savedCategory = categoryService.saveCategory(newCategory);
        
        if (savedCategory != null && savedCategory.getId() != null) {
            categoriesList.add(savedCategory);
            clearCategoryFields();
            AlertHelper.showInformationAlert("Success", "Category Added", 
                    "Category was successfully added.");
        } else {
            AlertHelper.showErrorAlert("Error", "Could Not Save", 
                    "Could not save the category. Please try again.");
        }
    }
    
    @FXML
    private void handleUpdateCategory(ActionEvent event) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        
        if (selectedCategory == null) {
            return;
        }
        
        String name = categoryNameField.getText().trim();
        String description = categoryDescField.getText().trim();
        
        if (name.isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Invalid Input", 
                    "Category name cannot be empty.");
            return;
        }
        
        // Check if another category with this name exists (excluding the current one)
        Category existingCategory = categoryService.getCategoryByName(name);
        if (existingCategory != null && !existingCategory.getId().equals(selectedCategory.getId())) {
            AlertHelper.showErrorAlert("Validation Error", "Duplicate Category", 
                    "Another category with this name already exists.");
            return;
        }
        
        selectedCategory.setName(name);
        selectedCategory.setDescription(description);
        
        Category updatedCategory = categoryService.saveCategory(selectedCategory);
        
        if (updatedCategory != null) {
            loadAllCategories(); // Refresh the list
            clearCategoryFields();
            categoryTable.getSelectionModel().clearSelection();
            AlertHelper.showInformationAlert("Success", "Category Updated", 
                    "Category was successfully updated.");
        } else {
            AlertHelper.showErrorAlert("Error", "Could Not Update", 
                    "Could not update the category. Please try again.");
        }
    }
    
    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        
        if (selectedCategory == null) {
            return;
        }
        
        // Check if this category is used by any products
        List<Product> productsInCategory = productService.getProductsByCategory(selectedCategory.getId());
        
        if (!productsInCategory.isEmpty()) {
            AlertHelper.showErrorAlert("Cannot Delete", "Category In Use", 
                    "This category is being used by " + productsInCategory.size() + 
                    " product(s). Please reassign these products before deleting.");
            return;
        }
        
        boolean confirmDelete = AlertHelper.showConfirmationAlert("Confirm Deletion", 
                "Delete Category", 
                "Are you sure you want to delete the category: " + selectedCategory.getName() + "?");
        
        if (confirmDelete) {
            boolean deleted = categoryService.deleteCategory(selectedCategory.getId());
            
            if (deleted) {
                categoriesList.remove(selectedCategory);
                clearCategoryFields();
                AlertHelper.showInformationAlert("Success", "Category Deleted", 
                        "Category was successfully deleted.");
            } else {
                AlertHelper.showErrorAlert("Error", "Delete Failed", 
                        "Could not delete the category. Please try again.");
            }
        }
    }
    
    @FXML
    private void handleClearCategory(ActionEvent event) {
        clearCategoryFields();
        categoryTable.getSelectionModel().clearSelection();
    }
    
    private void clearCategoryFields() {
        categoryNameField.clear();
        categoryDescField.clear();
    }
    
    @FXML
    private void handleRefreshLowStock(ActionEvent event) {
        loadLowStockProducts();
    }
    
    @FXML
    private void handleOrderSelected(ActionEvent event) {
        Product selectedProduct = lowStockTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            return;
        }
        
        // In a real application, this would open an order form or place an order
        // For now, we'll just show a confirmation dialog
        
        AlertHelper.showInformationAlert("Order Placed", "Order Request Generated", 
                "An order request has been generated for: " + selectedProduct.getName());
    }
    
    @FXML
    private void handleOrderAllLowStock(ActionEvent event) {
        if (lowStockList.isEmpty()) {
            AlertHelper.showInformationAlert("No Low Stock", "No Low Stock Items", 
                    "There are no low stock items to order.");
            return;
        }
        
        // In a real application, this would generate orders for all low stock items
        // For now, we'll just show a confirmation dialog
        
        AlertHelper.showInformationAlert("Orders Placed", "Order Requests Generated", 
                "Order requests have been generated for " + lowStockList.size() + " low stock items.");
    }
}