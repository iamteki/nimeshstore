package com.nimesh.controller;

import com.nimesh.model.OrderItem;
import com.nimesh.model.Product;
import com.nimesh.model.PurchaseOrder;
import com.nimesh.model.Supplier;
import com.nimesh.service.ProductService;
import com.nimesh.service.PurchaseOrderService;
import com.nimesh.service.SupplierService;
import com.nimesh.util.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderDialogController {
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    @FXML
    private ComboBox<Supplier> supplierComboBox;
    
    @FXML
    private DatePicker deliveryDatePicker;
    
    @FXML
    private TextArea notesArea;
    
    @FXML
    private ComboBox<Product> productComboBox;
    
    @FXML
    private TextField quantityField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private Button addItemButton;
    
    @FXML
    private TableView<OrderItem> itemsTable;
    
    @FXML
    private TableColumn<OrderItem, String> productColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> quantityColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> priceColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> totalColumn;
    
    @FXML
    private TableColumn<OrderItem, Button> removeColumn;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button saveOrderButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private PurchaseOrder order;
    private boolean orderSaved = false;
    
    private ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderItemsList = FXCollections.observableArrayList();
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isOrderSaved() {
        return orderSaved;
    }
    
    public void initialize() {
        // Create a new order
        order = new PurchaseOrder();
        
        // Set the delivery date picker to 7 days from now by default
        deliveryDatePicker.setValue(LocalDate.now().plusDays(7));
        
        // Load suppliers and products
        loadSuppliers();
        loadProducts();
        
        // Initialize the order items table
        initializeItemsTable();
        
        // Add change listeners for product selection
        productComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldProduct, newProduct) -> {
                    if (newProduct != null) {
                        // Set default price to buying price of the product
                        priceField.setText(newProduct.getBuyingPrice().toString());
                        // Set default quantity to 1
                        quantityField.setText("1");
                        addItemButton.setDisable(false);
                    } else {
                        addItemButton.setDisable(true);
                    }
                });
    }
    
    public void initializeForSupplier(Supplier supplier) {
        initialize();
        
        // Set the supplier and disable the combo box
        supplierComboBox.getSelectionModel().select(supplier);
        supplierComboBox.setDisable(true);
    }
    
    private void loadSuppliers() {
        suppliersList.clear();
        suppliersList.addAll(supplierService.getAllSuppliers());
        supplierComboBox.setItems(suppliersList);
    }
    
    private void loadProducts() {
        productsList.clear();
        productsList.addAll(productService.getAllProducts());
        productComboBox.setItems(productsList);
    }
    
    private void initializeItemsTable() {
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Format currency columns
        priceColumn.setCellFactory(column -> new TableCell<OrderItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + item.toString());
                }
            }
        });
        
        totalColumn.setCellFactory(column -> new TableCell<OrderItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + item.toString());
                }
            }
        });
        
        // Setup remove button column
        removeColumn.setCellFactory(param -> new TableCell<OrderItem, Button>() {
            private final Button removeButton = new Button("Remove");
            
            {
                removeButton.getStyleClass().add("cancel-button");
                removeButton.setOnAction(event -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    orderItemsList.remove(item);
                    updateOrderTotal();
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        itemsTable.setItems(orderItemsList);
    }
    
    @FXML
    private void handleAddItem(ActionEvent event) {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        
        if (selectedProduct == null) {
            AlertHelper.showErrorAlert("No Product Selected", "Please select a product", 
                    "You must select a product to add to the order.");
            return;
        }
        
        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal price = new BigDecimal(priceField.getText());
            
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showErrorAlert("Invalid Quantity", "Quantity must be greater than zero", 
                        "Please enter a positive quantity.");
                return;
            }
            
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showErrorAlert("Invalid Price", "Price must be greater than zero", 
                        "Please enter a positive price.");
                return;
            }
            
            // Check if product already exists in the order
            for (OrderItem item : orderItemsList) {
                if (item.getProduct().getId().equals(selectedProduct.getId())) {
                    // Update quantity instead of adding a new item
                    BigDecimal newQuantity = item.getQuantity().add(quantity);
                    item.setQuantity(newQuantity);
                    itemsTable.refresh();
                    updateOrderTotal();
                    
                    // Clear selection fields
                    productComboBox.getSelectionModel().clearSelection();
                    quantityField.clear();
                    priceField.clear();
                    return;
                }
            }
            
            // Create a new order item
            OrderItem item = new OrderItem(selectedProduct, quantity, price);
            orderItemsList.add(item);
            
            // Update order total
            updateOrderTotal();
            
            // Clear selection fields
            productComboBox.getSelectionModel().clearSelection();
            quantityField.clear();
            priceField.clear();
            
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Number", "Please enter valid numbers", 
                    "Quantity and price must be valid numbers.");
        }
    }
    
    private void updateOrderTotal() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : orderItemsList) {
            total = total.add(item.getTotal());
        }
        
        totalLabel.setText("LKR " + total.toString());
    }
    
    @FXML
    private void handleSaveOrder(ActionEvent event) {
        if (!validateOrder()) {
            return;
        }
        
        // Set supplier for the order
        order.setSupplier(supplierComboBox.getValue());
        
        // Set delivery date
        if (deliveryDatePicker.getValue() != null) {
            order.setDeliveryDate(deliveryDatePicker.getValue().atStartOfDay());
        }
        
        // Set notes
        order.setNotes(notesArea.getText());
        
        // Set items
        order.setItems(new ArrayList<>(orderItemsList));
        for (OrderItem item : orderItemsList) {
            item.setPurchaseOrder(order);
        }
        
        // Save the order
        PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(order);
        
        if (savedOrder != null && savedOrder.getId() != null) {
            orderSaved = true;
            dialogStage.close();
        } else {
            AlertHelper.showErrorAlert("Save Error", "Could Not Save Order", 
                    "An error occurred while saving the order. Please try again.");
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
    
    private boolean validateOrder() {
        StringBuilder errorMessage = new StringBuilder();
        
        // Supplier is required
        if (supplierComboBox.getValue() == null) {
            errorMessage.append("Please select a supplier.\n");
        }
        
        // At least one item is required
        if (orderItemsList.isEmpty()) {
            errorMessage.append("Please add at least one item to the order.\n");
        }
        
        if (errorMessage.length() > 0) {
            AlertHelper.showErrorAlert("Validation Error", "Please correct the following errors:", 
                    errorMessage.toString());
            return false;
        }
        
        return true;
    }
}