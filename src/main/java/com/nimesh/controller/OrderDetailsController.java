package com.nimesh.controller;

import com.nimesh.model.OrderItem;
import com.nimesh.model.PurchaseOrder;
import com.nimesh.service.PurchaseOrderService;
import com.nimesh.util.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Controller
public class OrderDetailsController {
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    @FXML
    private Label orderNumberLabel;
    
    @FXML
    private Label orderDateLabel;
    
    @FXML
    private Label supplierNameLabel;
    
    @FXML
    private Label supplierContactLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label deliveryDateLabel;
    
    @FXML
    private TextArea notesArea;
    
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
    private Label totalAmountLabel;
    
    @FXML
    private Button updateStatusButton;
    
    @FXML
    private Button closeButton;
    
    private Stage dialogStage;
    private PurchaseOrder order;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void initializeWithOrder(PurchaseOrder order) {
        this.order = order;
        
        // Set order details
        orderNumberLabel.setText(order.getOrderNumber());
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        orderDateLabel.setText(order.getOrderDate().format(dateFormatter));
        
        supplierNameLabel.setText(order.getSupplier().getName());
        supplierContactLabel.setText(order.getSupplier().getContactNo());
        
        statusLabel.setText(order.getStatus());
        
        if (order.getDeliveryDate() != null) {
            deliveryDateLabel.setText(order.getDeliveryDate().format(dateFormatter));
        } else {
            deliveryDateLabel.setText("Not specified");
        }
        
        notesArea.setText(order.getNotes());
        notesArea.setEditable(false);
        
        // Initialize items table
        initializeItemsTable();
        
        // Load items
        itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));
        
        // Set total amount
        totalAmountLabel.setText("LKR " + order.getTotalAmount().toString());
        
        // Update status button visibility based on current status
        updateStatusButtonVisibility();
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
    }
    
    private void updateStatusButtonVisibility() {
        // Show/hide update status button based on current status
        switch (order.getStatus()) {
            case "PENDING":
                updateStatusButton.setText("Mark as Processing");
                updateStatusButton.setVisible(true);
                break;
            case "PROCESSING":
                updateStatusButton.setText("Mark as Shipped");
                updateStatusButton.setVisible(true);
                break;
            case "SHIPPED":
                updateStatusButton.setText("Mark as Delivered");
                updateStatusButton.setVisible(true);
                break;
            default:
                updateStatusButton.setVisible(false);
                break;
        }
    }
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        String newStatus;
        
        switch (order.getStatus()) {
            case "PENDING":
                newStatus = "PROCESSING";
                break;
            case "PROCESSING":
                newStatus = "SHIPPED";
                break;
            case "SHIPPED":
                newStatus = "DELIVERED";
                break;
            default:
                return;
        }
        
        // Confirm status update
        boolean confirmed = AlertHelper.showConfirmationAlert("Update Status", 
                "Update Order Status", "Are you sure you want to update the status to " + newStatus + "?");
        
        if (!confirmed) {
            return;
        }
        
        // Update status
        PurchaseOrder updatedOrder = purchaseOrderService.updateOrderStatus(order.getId(), newStatus);
        
        if (updatedOrder != null) {
            // Update the local order
            order = updatedOrder;
            statusLabel.setText(order.getStatus());
            
            // Update button visibility
            updateStatusButtonVisibility();
            
            AlertHelper.showInformationAlert("Status Updated", "Order Status Updated", 
                    "Order status has been updated to " + newStatus + ".");
        } else {
            AlertHelper.showErrorAlert("Error", "Could Not Update Status", 
                    "An error occurred while updating the order status.");
        }
    }
    
    @FXML
    private void handleClose(ActionEvent event) {
        dialogStage.close();
    }
}