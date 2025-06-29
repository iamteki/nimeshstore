package com.nimesh.controller;

import com.nimesh.model.Product;
import com.nimesh.service.ProductService;
import com.nimesh.service.ProductBatchService;
import com.nimesh.service.SystemConfigService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.DecimalFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

@Controller
public class StockDialogController implements Initializable {

    @Autowired
    private ProductBatchService productBatchService;

    @Autowired
    private SystemConfigService configService;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label currentStockLabel;

    @FXML
    private Label pricingStrategyLabel;

    @FXML
    private ComboBox<String> operationComboBox;

    @FXML
    private TextField stockChangeField;

    @FXML
    private TextField reasonField;

    @FXML
    private Button updateButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button viewBatchesButton;

    @FXML
    private TextArea batchInfoArea;

    private Stage dialogStage;
    private Product product;
    private ProductService productService;
    private boolean stockUpdated = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup operation combo box
        operationComboBox.getItems().addAll("Add", "Remove");
        operationComboBox.setValue("Add");

        // Setup batch info area
        batchInfoArea.setEditable(false);
        batchInfoArea.setPrefRowCount(6);
        
        // Add listener to operation selection
        operationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateOperationHelp();
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public boolean isStockUpdated() {
        return stockUpdated;
    }

    public void initializeProduct(Product product) {
        this.product = product;
        productNameLabel.setText(product.getName());
        
        // Display pricing strategy
        String strategy = configService.getPricingStrategy();
        pricingStrategyLabel.setText("Pricing Strategy: " + strategy);
        
        refreshCurrentStock();
        loadBatchInformation();
        updateOperationHelp();
    }

    private void refreshCurrentStock() {
        // Get current stock from batches (more accurate)
        BigDecimal batchStock = productBatchService.getTotalRemainingQuantity(product.getId());
        if (batchStock == null) batchStock = BigDecimal.ZERO;
        
        currentStockLabel.setText("Current Stock: " + batchStock + 
                                " (Product Table: " + product.getCurrentStock() + ")");
        
        // Update product object's current stock if there's a discrepancy
        if (!batchStock.equals(product.getCurrentStock())) {
            product.setCurrentStock(batchStock);
        }
    }

    private void loadBatchInformation() {
        List<ProductBatchService.BatchInfo> batchInfo = 
            productBatchService.getBatchAvailabilityInfo(product.getId());
        
        StringBuilder info = new StringBuilder();
        info.append("=== BATCH INFORMATION ===\n");
        info.append("Pricing Strategy: ").append(configService.getPricingStrategy()).append("\n\n");
        
        if (batchInfo.isEmpty()) {
            info.append("No batches available.\n");
            info.append("Adding stock will create a new batch.\n");
        } else {
            info.append("Available Batches:\n");
            for (ProductBatchService.BatchInfo batch : batchInfo) {
                info.append("• Batch #").append(batch.getBatchNumber()).append("\n");
                info.append("  Quantity: ").append(batch.getAvailableQuantity()).append("\n");
                info.append("  Selling Price: ").append(DecimalFormatter.format(batch.getSellingPrice())).append("\n");
                if (batch.getWholesalePrice() != null) {
                    info.append("  Wholesale Price: ").append(DecimalFormatter.format(batch.getWholesalePrice())).append("\n");
                }
                info.append("  Purchase Date: ").append(batch.getPurchaseDate().toLocalDate()).append("\n\n");
            }
            
            // Add strategy-specific note
            String strategy = configService.getPricingStrategy();
            switch (strategy) {
                case SystemConfigService.FIFO_STRATEGY:
                    info.append("FIFO: Stock removal will use oldest batches first.\n");
                    break;
                case SystemConfigService.LIFO_STRATEGY:
                    info.append("LIFO: Stock removal will use newest batches first.\n");
                    break;
                case SystemConfigService.AVERAGE_STRATEGY:
                    info.append("AVERAGE: Physical removal uses FIFO, pricing uses average cost.\n");
                    break;
            }
        }
        
        batchInfoArea.setText(info.toString());
    }

    private void updateOperationHelp() {
        String operation = operationComboBox.getValue();
        if ("Add".equals(operation)) {
            reasonField.setPromptText("Reason for adding stock (optional)");
        } else {
            reasonField.setPromptText("Reason for removing stock (required)");
        }
    }

    @FXML
    private void handleViewBatches(ActionEvent event) {
        showDetailedBatchDialog();
    }

    private void showDetailedBatchDialog() {
        List<ProductBatchService.BatchInfo> batchInfo = 
            productBatchService.getBatchAvailabilityInfo(product.getId());
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detailed Batch Information");
        alert.setHeaderText("Product: " + product.getName() + " (Strategy: " + 
                           configService.getPricingStrategy() + ")");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        if (batchInfo.isEmpty()) {
            content.getChildren().add(new Label("No batches available for this product."));
            content.getChildren().add(new Label("Stock operations will create new batches."));
        } else {
            BigDecimal totalStock = BigDecimal.ZERO;
            
            for (ProductBatchService.BatchInfo batch : batchInfo) {
                VBox batchBox = new VBox(5);
                batchBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-padding: 10;");
                
                Label batchLabel = new Label("Batch #" + batch.getBatchNumber());
                batchLabel.setStyle("-fx-font-weight: bold;");
                
                batchBox.getChildren().addAll(
                    batchLabel,
                    new Label("Available Quantity: " + batch.getAvailableQuantity()),
                    new Label("Selling Price: " + DecimalFormatter.format(batch.getSellingPrice())),
                    new Label("Wholesale Price: " + (batch.getWholesalePrice() != null ? 
                             DecimalFormatter.format(batch.getWholesalePrice()) : "N/A")),
                    new Label("Purchase Date: " + batch.getPurchaseDate().toLocalDate())
                );
                
                content.getChildren().add(batchBox);
                totalStock = totalStock.add(batch.getAvailableQuantity());
            }
            
            content.getChildren().add(new Separator());
            Label totalLabel = new Label("Total Available: " + totalStock + " units");
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            content.getChildren().add(totalLabel);
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @FXML
    private void handleUpdateStock(ActionEvent event) {
        try {
            BigDecimal stockChange = new BigDecimal(stockChangeField.getText().trim());
            String selectedOperation = operationComboBox.getValue();
            String reason = reasonField.getText().trim();
            
            if (stockChange.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showErrorAlert("Invalid Input", "Invalid Quantity", 
                        "Stock change must be a positive number.");
                return;
            }
            
            if (selectedOperation == null) {
                AlertHelper.showErrorAlert("Invalid Input", "Select Operation", 
                        "Please select an operation type.");
                return;
            }
            
            // For removal operations, require a reason
            if ("Remove".equals(selectedOperation) && reason.isEmpty()) {
                AlertHelper.showErrorAlert("Invalid Input", "Reason Required", 
                        "Please provide a reason for removing stock.");
                return;
            }
            
            // For removal operations, check if enough stock is available
            if ("Remove".equals(selectedOperation)) {
                BigDecimal availableStock = productBatchService.getTotalRemainingQuantity(product.getId());
                if (availableStock == null) availableStock = BigDecimal.ZERO;
                
                if (stockChange.compareTo(availableStock) > 0) {
                    AlertHelper.showErrorAlert("Insufficient Stock", "Cannot Remove Stock", 
                            "Only " + availableStock + " units are available. Cannot remove " + stockChange + " units.");
                    return;
                }
                
                // Show confirmation for stock removal with strategy info
                if (!showRemovalConfirmation(stockChange, availableStock)) {
                    return;
                }
            }
            
            // Update stock using batch-aware method
            String operationType = "Add".equals(selectedOperation) ? "ADD" : "REMOVE";
            boolean success = productService.updateProductStock(product.getId(), stockChange, operationType);
            
            if (success) {
                // Log the stock update activity (if you have activity logging)
                String description = selectedOperation + " " + stockChange + " units for product: " + product.getName();
                if (!reason.isEmpty()) {
                    description += " - Reason: " + reason;
                }
                
                stockUpdated = true;
                AlertHelper.showInformationAlert("Success", "Stock Updated", 
                        "Stock has been successfully updated using " + configService.getPricingStrategy() + " strategy.");
                
                // Refresh displays
                refreshCurrentStock();
                loadBatchInformation();
                
                // Clear fields for next operation
                stockChangeField.clear();
                reasonField.clear();
                
            } else {
                AlertHelper.showErrorAlert("Error", "Update Failed", 
                        "Could not update the stock. Please try again.");
            }
            
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Input", "Invalid Number", 
                    "Please enter a valid number for stock change.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Update Failed", 
                    "An error occurred while updating stock: " + e.getMessage());
        }
    }

    private boolean showRemovalConfirmation(BigDecimal removeQuantity, BigDecimal availableStock) {
        String strategy = configService.getPricingStrategy();
        List<ProductBatchService.BatchInfo> batchInfo = 
            productBatchService.getBatchAvailabilityInfo(product.getId());
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Stock Removal");
        alert.setHeaderText("Remove " + removeQuantity + " units using " + strategy + " strategy");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        content.getChildren().add(new Label("Current available stock: " + availableStock + " units"));
        content.getChildren().add(new Label("Quantity to remove: " + removeQuantity + " units"));
        content.getChildren().add(new Label("Remaining after removal: " + 
                                          availableStock.subtract(removeQuantity) + " units"));
        
        content.getChildren().add(new Separator());
        
        Label strategyLabel = new Label("Removal Strategy: " + strategy);
        strategyLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(strategyLabel);
        
        // Show which batches will be affected
        content.getChildren().add(new Label("Batches that will be affected:"));
        
        BigDecimal remaining = removeQuantity;
        for (ProductBatchService.BatchInfo batch : batchInfo) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal fromThisBatch = remaining.min(batch.getAvailableQuantity());
            Label batchLabel = new Label("• Batch #" + batch.getBatchNumber() + 
                                       ": " + fromThisBatch + " units");
            content.getChildren().add(batchLabel);
            remaining = remaining.subtract(fromThisBatch);
        }
        
        alert.getDialogPane().setContent(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
}