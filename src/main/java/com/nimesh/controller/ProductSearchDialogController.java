package com.nimesh.controller;

import com.nimesh.model.Product;
import com.nimesh.service.ProductService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class ProductSearchDialogController implements Initializable {
    
    @Autowired
    private ProductService productService;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TableView<Product> productTable;
    
    @FXML
    private TableColumn<Product, Long> idColumn;
    
    @FXML
    private TableColumn<Product, String> nameColumn;
    
    @FXML
    private TableColumn<Product, String> categoryColumn;
    
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    
    @FXML
    private Button selectButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private Product selectedProduct;
    private boolean productSelected = false;
    private String customerType = "RETAIL"; // Default
    
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeTable();
        
        // Enable select button only when a product is selected
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectButton.setDisable(newSelection == null));
        
        // Double-click to select a product
        productTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && productTable.getSelectionModel().getSelectedItem() != null) {
                handleSelect(new ActionEvent());
            }
        });
        
        // Clear initial results and disable select button
        selectButton.setDisable(true);
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
        // Update price column based on customer type
        updatePriceColumn();
    }
    
    public Product getSelectedProduct() {
        return selectedProduct;
    }
    
    public boolean isProductSelected() {
        return productSelected;
    }
    
    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            return new SimpleStringProperty(product.getCategory() != null ? 
                    product.getCategory().getName() : "");
        });
        
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        
        // Initialize product list
        productTable.setItems(productsList);
        
        // Set up price column
        updatePriceColumn();
    }
    
    private void updatePriceColumn() {
        priceColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            BigDecimal price;
            
            if ("WHOLESALE".equals(customerType) && product.getWholesalePrice() != null) {
                price = product.getWholesalePrice();
            } else {
                price = product.getSellingPrice();
            }
            
            return new javafx.beans.property.SimpleObjectProperty<>(price);
        });
        
        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
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
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            productsList.clear();
            return;
        }
        
        productsList.clear();
        productsList.addAll(productService.searchProductsByName(searchTerm));
    }
    
    @FXML
    private void handleSelect(ActionEvent event) {
        selectedProduct = productTable.getSelectionModel().getSelectedItem();
        
        if (selectedProduct != null) {
            productSelected = true;
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
}