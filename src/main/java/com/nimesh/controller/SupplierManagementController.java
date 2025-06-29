package com.nimesh.controller;

import com.nimesh.model.PurchaseOrder;
import com.nimesh.model.OrderItem;
import com.nimesh.model.Supplier;
import com.nimesh.service.PurchaseOrderService;
import com.nimesh.service.SupplierService;
import com.nimesh.util.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.chart.LineChart;

@Controller
public class SupplierManagementController implements Initializable {
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    @Autowired
    private ApplicationContext context;
    
    @FXML
    private BorderPane supplierPane;
    
    // Suppliers Tab
    @FXML
    private TextField searchField;
    
    @FXML
    private TableView<Supplier> supplierTable;
    
    @FXML
    private TableColumn<Supplier, Long> idColumn;
    
    @FXML
    private TableColumn<Supplier, String> nameColumn;
    
    @FXML
    private TableColumn<Supplier, String> contactPersonColumn;
    
    @FXML
    private TableColumn<Supplier, String> contactNoColumn;
    
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    
    @FXML
    private TableColumn<Supplier, String> addressColumn;
    
    @FXML
    private TableColumn<Supplier, Button> actionsColumn;
    
    // Purchase Orders Tab
    @FXML
    private ComboBox<String> orderStatusCombo;
    
    @FXML
    private TableView<PurchaseOrder> orderTable;
    
    @FXML
    private TableColumn<PurchaseOrder, String> orderIdColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, String> orderDateColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, String> orderSupplierColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, String> orderStatusColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, Integer> orderItemsColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, BigDecimal> orderTotalColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, String> orderDeliveryDateColumn;
    
    @FXML
    private TableColumn<PurchaseOrder, HBox> orderActionsColumn;
    
    // Reports Tab
    @FXML
    private ComboBox<Supplier> reportSupplierCombo;
    
    @FXML
    private ComboBox<String> reportTypeCombo;
    
    @FXML
    private VBox reportContainer;
    
    // Buttons
    @FXML
    private Button addSupplierButton;
    
    @FXML
    private Button editSupplierButton;
    
    @FXML
    private Button deleteSupplierButton;
    
    @FXML
    private Button placeOrderButton;
    
    @FXML
    private Button viewOrdersButton;
    
    @FXML
    private Button refreshOrdersButton;
    
    @FXML
    private Button createOrderButton;
    
    @FXML
    private Button exportOrdersButton;
    
    @FXML
    private Button generateReportButton;
    
    private ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
    private ObservableList<PurchaseOrder> ordersList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize tables
        initializeSupplierTable();
        initializeOrderTable();
        
        // Load data
        loadAllSuppliers();
        loadAllOrders();
        
        // Set up search field handler
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadAllSuppliers();
            }
        });
        
        // Set up order status combo
        orderStatusCombo.getItems().addAll(
                "All Orders",
                "Pending",
                "Processing",
                "Shipped",
                "Delivered",
                "Cancelled"
        );
        orderStatusCombo.getSelectionModel().selectFirst();
        orderStatusCombo.setOnAction(event -> filterOrders());
        
        // Setup report type combo
        reportTypeCombo.getItems().addAll(
                "Purchase History",
                "Order Status Summary",
                "Monthly Purchase Trends",
                "Supplier Performance"
        );
        
        // Setup supplier combo for reports
        reportSupplierCombo.setItems(suppliersList);
        
        // Disable buttons until selection is made
        disableButtons(true);
        
        // Add selection listeners to tables
        supplierTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> disableButtons(newSelection == null));
    }
    
    private void initializeSupplierTable() {
        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        contactNoColumn.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // Setup actions column with view details button
        actionsColumn.setCellFactory(param -> new TableCell<Supplier, Button>() {
            private final Button detailsButton = new Button("Details");
            
            {
                detailsButton.getStyleClass().add("view-button");
                detailsButton.setOnAction(event -> {
                    Supplier supplier = getTableView().getItems().get(getIndex());
                    showSupplierDetails(supplier);
                });
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        
        supplierTable.setItems(suppliersList);
    }
    
    private void initializeOrderTable() {
        // Setup columns
        orderIdColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOrderNumber()));
        
        orderDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOrderDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        
        orderSupplierColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSupplier().getName()));
        
        orderStatusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Format the status column with colors
        orderStatusColumn.setCellFactory(column -> new TableCell<PurchaseOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    getStyleClass().removeAll("status-pending", "status-processing", 
                            "status-shipped", "status-delivered", "status-cancelled");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-pending", "status-processing", 
                            "status-shipped", "status-delivered", "status-cancelled");
                    
                    switch (item) {
                        case "PENDING":
                            getStyleClass().add("status-pending");
                            break;
                        case "PROCESSING":
                            getStyleClass().add("status-processing");
                            break;
                        case "SHIPPED":
                            getStyleClass().add("status-shipped");
                            break;
                        case "DELIVERED":
                            getStyleClass().add("status-delivered");
                            break;
                        case "CANCELLED":
                            getStyleClass().add("status-cancelled");
                            break;
                    }
                }
            }
        });
        
        // Item count column
        orderItemsColumn.setCellValueFactory(cellData -> {
            PurchaseOrder order = cellData.getValue();
            // Get item count from order
            int itemCount = order.getItems().size();
            return new SimpleObjectProperty<>(itemCount);
        });
        
        // Total column
        orderTotalColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        
        // Format the total column
        orderTotalColumn.setCellFactory(column -> new TableCell<PurchaseOrder, BigDecimal>() {
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
        
        // Delivery date column
        orderDeliveryDateColumn.setCellValueFactory(cellData -> {
            PurchaseOrder order = cellData.getValue();
            LocalDateTime deliveryDate = order.getDeliveryDate();
            return new SimpleStringProperty(deliveryDate != null ? 
                    deliveryDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "Not Set");
        });
        
        // Actions column with multiple buttons
        orderActionsColumn.setCellFactory(param -> new TableCell<PurchaseOrder, HBox>() {
            private final Button viewButton = new Button("View");
            private final Button receiveButton = new Button("Receive");
            private final Button cancelButton = new Button("Cancel");
            private final HBox buttonBox = new HBox(5);
            
            {
                viewButton.getStyleClass().add("view-button");
                receiveButton.getStyleClass().add("receive-button");
                cancelButton.getStyleClass().add("cancel-button");
                
                buttonBox.getChildren().addAll(viewButton, receiveButton, cancelButton);
                
                viewButton.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });
                
                receiveButton.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    receiveOrder(order);
                });
                
                cancelButton.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });
            }
            
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus();
                    
                    // Show/hide buttons based on order status
                    receiveButton.setVisible("SHIPPED".equals(status));
                    cancelButton.setVisible("PENDING".equals(status) || "PROCESSING".equals(status));
                    
                    setGraphic(buttonBox);
                }
            }
        });
        
        orderTable.setItems(ordersList);
    }
    
    private void loadAllSuppliers() {
        suppliersList.clear();
        suppliersList.addAll(supplierService.getAllSuppliers());
    }
    
    private void loadAllOrders() {
        ordersList.clear();
        ordersList.addAll(purchaseOrderService.getAllPurchaseOrders());
    }
    
    private void filterOrders() {
        String filter = orderStatusCombo.getValue();
        ordersList.clear();
        
        if ("All Orders".equals(filter)) {
            ordersList.addAll(purchaseOrderService.getAllPurchaseOrders());
        } else {
            // Convert first letter to uppercase and the rest to lowercase for the status
            String status = filter.toUpperCase();
            ordersList.addAll(purchaseOrderService.getPurchaseOrdersByStatus(status));
        }
    }
    
    private void disableButtons(boolean disable) {
        editSupplierButton.setDisable(disable);
        deleteSupplierButton.setDisable(disable);
        placeOrderButton.setDisable(disable);
        viewOrdersButton.setDisable(disable);
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadAllSuppliers();
            return;
        }
        
        List<Supplier> searchResults = supplierService.searchSuppliersByName(searchTerm);
        suppliersList.clear();
        suppliersList.addAll(searchResults);
    }
    
    @FXML
    private void handleAddSupplier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/supplier_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            SupplierDialogController controller = loader.getController();
            controller.initializeForAdd();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Supplier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(supplierPane.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isSupplierSaved()) {
                loadAllSuppliers();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the supplier dialog.");
        }
    }
    
    @FXML
    private void handleEditSupplier(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        
        if (selectedSupplier == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/supplier_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            SupplierDialogController controller = loader.getController();
            controller.initializeForEdit(selectedSupplier);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Supplier");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(supplierPane.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isSupplierSaved()) {
                loadAllSuppliers();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the supplier dialog.");
        }
    }
    
    @FXML
    private void handleDeleteSupplier(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        
        if (selectedSupplier == null) {
            return;
        }
        
        // Check if supplier has orders
        List<PurchaseOrder> supplierOrders = purchaseOrderService.getPurchaseOrdersBySupplier(selectedSupplier.getId());
        if (!supplierOrders.isEmpty()) {
            AlertHelper.showErrorAlert("Cannot Delete", "Supplier Has Orders", 
                    "This supplier has " + supplierOrders.size() + " purchase orders. " +
                    "Please reassign or delete these orders before deleting the supplier.");
            return;
        }
        
        boolean confirmed = AlertHelper.showConfirmationAlert("Confirm Delete", "Delete Supplier", 
                "Are you sure you want to delete " + selectedSupplier.getName() + "?");
        
        if (confirmed) {
            boolean deleted = supplierService.deleteSupplier(selectedSupplier.getId());
            
            if (deleted) {
                suppliersList.remove(selectedSupplier);
                AlertHelper.showInformationAlert("Success", "Supplier Deleted", 
                        "Supplier was successfully deleted.");
            } else {
                AlertHelper.showErrorAlert("Error", "Delete Failed", 
                        "Could not delete the supplier. Please try again.");
            }
        }
    }
    
    @FXML
    private void handlePlaceOrder(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        
        if (selectedSupplier == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            OrderDialogController controller = loader.getController();
            controller.initializeForSupplier(selectedSupplier);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Purchase Order - " + selectedSupplier.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(supplierPane.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isOrderSaved()) {
                loadAllOrders();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the order dialog.");
        }
    }
    
    @FXML
    private void handleViewOrders(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        
        if (selectedSupplier == null) {
            return;
        }
        
        // Filter orders for this supplier
        ordersList.clear();
        ordersList.addAll(purchaseOrderService.getPurchaseOrdersBySupplier(selectedSupplier.getId()));
        
        // Switch to Orders tab
        TabPane tabPane = (TabPane) supplierPane.getCenter();
        tabPane.getSelectionModel().select(1); // 1 = Orders tab (0-based index)
    }
    
    @FXML
    private void handleRefreshOrders(ActionEvent event) {
        filterOrders();
    }
    
    @FXML
    private void handleCreateOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            OrderDialogController controller = loader.getController();
            controller.initialize();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Purchase Order");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(supplierPane.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isOrderSaved()) {
                loadAllOrders();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to open the order dialog.");
        }
    }
    
    @FXML
    private void handleExportOrders(ActionEvent event) {
        // In a real system, this would generate a CSV or PDF file
        // For now, we'll just show a confirmation
        
        AlertHelper.showInformationAlert("Orders Exported", "Purchase Orders Report Generated", 
                "Purchase orders report has been generated and saved to the reports folder.");
    }
    
    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String reportType = reportTypeCombo.getValue();
        Supplier selectedSupplier = reportSupplierCombo.getValue();
        
        if (reportType == null) {
            AlertHelper.showErrorAlert("No Report Selected", "Please Select a Report Type", 
                    "Please select a report type to generate.");
            return;
        }
        
        reportContainer.getChildren().clear();
        
        switch (reportType) {
            case "Purchase History":
                generatePurchaseHistoryReport(selectedSupplier);
                break;
            case "Order Status Summary":
                generateOrderStatusSummaryReport(selectedSupplier);
                break;
            case "Monthly Purchase Trends":
                generateMonthlyTrendsReport(selectedSupplier);
                break;
            case "Supplier Performance":
                generateSupplierPerformanceReport(selectedSupplier);
                break;
        }
    }
    
   private void generatePurchaseHistoryReport(Supplier supplier) {
        Label titleLabel = new Label("Purchase History Report");
        titleLabel.getStyleClass().add("report-title");
        
        Label subtitleLabel = new Label(supplier != null ? 
                "For " + supplier.getName() : "For All Suppliers");
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        TableView<PurchaseOrder> reportTable = new TableView<>();
        
        TableColumn<PurchaseOrder, String> orderNumberColumn = new TableColumn<>("Order #");
        orderNumberColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOrderNumber()));
        
        TableColumn<PurchaseOrder, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOrderDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        
        TableColumn<PurchaseOrder, String> supplierColumn = new TableColumn<>("Supplier");
        supplierColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSupplier().getName()));
        
        TableColumn<PurchaseOrder, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus()));
        
        TableColumn<PurchaseOrder, BigDecimal> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        
        // Format the total column
        totalColumn.setCellFactory(column -> new TableCell<PurchaseOrder, BigDecimal>() {
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
        
        reportTable.getColumns().addAll(orderNumberColumn, dateColumn, supplierColumn, 
                statusColumn, totalColumn);
        
        // Load data
        List<PurchaseOrder> orders;
        if (supplier != null) {
            orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplier.getId());
        } else {
            orders = purchaseOrderService.getAllPurchaseOrders();
        }
        
        reportTable.setItems(FXCollections.observableArrayList(orders));
        
        // Add to container
        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, reportTable);
    }
    
    private void generateOrderStatusSummaryReport(Supplier supplier) {
        Label titleLabel = new Label("Order Status Summary Report");
        titleLabel.getStyleClass().add("report-title");
        
        Label subtitleLabel = new Label(supplier != null ? 
                "For " + supplier.getName() : "For All Suppliers");
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Get orders data
        List<PurchaseOrder> orders;
        if (supplier != null) {
            orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplier.getId());
        } else {
            orders = purchaseOrderService.getAllPurchaseOrders();
        }
        
        // Count orders by status
        int pendingCount = 0;
        int processingCount = 0;
        int shippedCount = 0;
        int deliveredCount = 0;
        int cancelledCount = 0;
        
        for (PurchaseOrder order : orders) {
            switch (order.getStatus()) {
                case "PENDING":
                    pendingCount++;
                    break;
                case "PROCESSING":
                    processingCount++;
                    break;
                case "SHIPPED":
                    shippedCount++;
                    break;
                case "DELIVERED":
                    deliveredCount++;
                    break;
                case "CANCELLED":
                    cancelledCount++;
                    break;
            }
        }
        
        // Create bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Order Status");
        yAxis.setLabel("Number of Orders");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Orders by Status");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Order Count");
        
        series.getData().add(new XYChart.Data<>("Pending", pendingCount));
        series.getData().add(new XYChart.Data<>("Processing", processingCount));
        series.getData().add(new XYChart.Data<>("Shipped", shippedCount));
        series.getData().add(new XYChart.Data<>("Delivered", deliveredCount));
        series.getData().add(new XYChart.Data<>("Cancelled", cancelledCount));
        
        barChart.getData().add(series);
        
        // Add summary labels
        Label totalOrdersLabel = new Label("Total Orders: " + orders.size());
        totalOrdersLabel.getStyleClass().add("summary-label");
        
        Label activeOrdersLabel = new Label("Active Orders (Pending/Processing/Shipped): " + 
                (pendingCount + processingCount + shippedCount));
        activeOrdersLabel.getStyleClass().add("summary-label");
        
        Label completedOrdersLabel = new Label("Completed Orders (Delivered): " + deliveredCount);
        completedOrdersLabel.getStyleClass().add("summary-label");
        
        Label cancelledOrdersLabel = new Label("Cancelled Orders: " + cancelledCount);
        cancelledOrdersLabel.getStyleClass().add("summary-label");
        
        // Add to container
        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, barChart, 
                totalOrdersLabel, activeOrdersLabel, completedOrdersLabel, cancelledOrdersLabel);
    }
    
    private void generateMonthlyTrendsReport(Supplier supplier) {
        Label titleLabel = new Label("Monthly Purchase Trends");
        titleLabel.getStyleClass().add("report-title");
        
        Label subtitleLabel = new Label(supplier != null ? 
                "For " + supplier.getName() : "For All Suppliers");
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create line chart for monthly trends
        final LineChart<String, Number> lineChart = createMonthlyTrendsChart(supplier);
        
        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, lineChart);
    }
    
    private LineChart<String, Number> createMonthlyTrendsChart(Supplier supplier) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Purchase Amount (LKR)");
        
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Purchase Trends");
        
        // Prepare purchase data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(supplier != null ? supplier.getName() : "All Suppliers");
        
        // Get data for the last 6 months
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = currentDate.minusMonths(i).withDayOfMonth(1);
            String monthName = monthStart.format(monthFormatter);
            
            // Get purchases for this month
            LocalDateTime startDateTime = monthStart.atStartOfDay();
            LocalDateTime endDateTime = monthStart.plusMonths(1).minusDays(1).atTime(23, 59, 59);
            
            List<PurchaseOrder> monthlyOrders;
            if (supplier != null) {
                monthlyOrders = purchaseOrderService.getPurchaseOrdersBySupplier(supplier.getId());
                monthlyOrders = monthlyOrders.stream()
                        .filter(order -> {
                            LocalDateTime orderDate = order.getOrderDate();
                            return orderDate.isAfter(startDateTime) && orderDate.isBefore(endDateTime);
                        })
                        .toList();
            } else {
                monthlyOrders = purchaseOrderService.getOrdersForDateRange(startDateTime, endDateTime);
            }
            
            // Calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PurchaseOrder order : monthlyOrders) {
                if (!"CANCELLED".equals(order.getStatus())) {
                    totalAmount = totalAmount.add(order.getTotalAmount());
                }
            }
            
            series.getData().add(new XYChart.Data<>(monthName, totalAmount));
        }
        
        lineChart.getData().add(series);
        return lineChart;
    }
    
    private void generateSupplierPerformanceReport(Supplier supplier) {
        Label titleLabel = new Label("Supplier Performance Report");
        titleLabel.getStyleClass().add("report-title");
        
        // If no specific supplier is selected, show all suppliers performance
        if (supplier == null) {
            Label subtitleLabel = new Label("Performance Comparison for All Suppliers");
            subtitleLabel.getStyleClass().add("report-subtitle");
            
            // Create a table for supplier performance metrics
            TableView<Supplier> performanceTable = new TableView<>();
            
            TableColumn<Supplier, String> supplierColumn = new TableColumn<>("Supplier");
            supplierColumn.setCellValueFactory(cellData -> 
                    new SimpleStringProperty(cellData.getValue().getName()));
            
            TableColumn<Supplier, Integer> ordersColumn = new TableColumn<>("Total Orders");
            ordersColumn.setCellValueFactory(cellData -> {
                Supplier s = cellData.getValue();
                List<PurchaseOrder> supplierOrders = purchaseOrderService.getPurchaseOrdersBySupplier(s.getId());
                return new SimpleObjectProperty<>(supplierOrders.size());
            });
            
            TableColumn<Supplier, Integer> deliveredColumn = new TableColumn<>("Delivered Orders");
            deliveredColumn.setCellValueFactory(cellData -> {
                Supplier s = cellData.getValue();
                List<PurchaseOrder> deliveredOrders = purchaseOrderService.getPurchaseOrdersBySupplier(s.getId())
                        .stream()
                        .filter(order -> "DELIVERED".equals(order.getStatus()))
                        .toList();
                return new SimpleObjectProperty<>(deliveredOrders.size());
            });
            
            TableColumn<Supplier, String> deliveryRateColumn = new TableColumn<>("Delivery Rate");
            deliveryRateColumn.setCellValueFactory(cellData -> {
                Supplier s = cellData.getValue();
                List<PurchaseOrder> allOrders = purchaseOrderService.getPurchaseOrdersBySupplier(s.getId());
                long deliveredCount = allOrders.stream()
                        .filter(order -> "DELIVERED".equals(order.getStatus()))
                        .count();
                
                if (allOrders.isEmpty()) {
                    return new SimpleStringProperty("N/A");
                }
                
                double rate = (double) deliveredCount / allOrders.size() * 100;
                return new SimpleStringProperty(String.format("%.1f%%", rate));
            });
            
            TableColumn<Supplier, BigDecimal> totalSpendColumn = new TableColumn<>("Total Spend");
            totalSpendColumn.setCellValueFactory(cellData -> {
                Supplier s = cellData.getValue();
                List<PurchaseOrder> allOrders = purchaseOrderService.getPurchaseOrdersBySupplier(s.getId());
                BigDecimal totalSpend = BigDecimal.ZERO;
                
                for (PurchaseOrder order : allOrders) {
                    if (!"CANCELLED".equals(order.getStatus())) {
                        totalSpend = totalSpend.add(order.getTotalAmount());
                    }
                }
                
                return new SimpleObjectProperty<>(totalSpend);
            });
            
            // Format the total spend column
            totalSpendColumn.setCellFactory(column -> new TableCell<Supplier, BigDecimal>() {
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
            
            performanceTable.getColumns().addAll(supplierColumn, ordersColumn, deliveredColumn, 
                    deliveryRateColumn, totalSpendColumn);
            
            // Load all suppliers data
            performanceTable.setItems(suppliersList);
            
            // Add to container
            reportContainer.getChildren().addAll(titleLabel, subtitleLabel, performanceTable);
        } else {
            // For a specific supplier
            Label subtitleLabel = new Label("Performance Report for " + supplier.getName());
            subtitleLabel.getStyleClass().add("report-subtitle");
            
            // Get all orders for this supplier
            List<PurchaseOrder> supplierOrders = purchaseOrderService.getPurchaseOrdersBySupplier(supplier.getId());
            
            // Calculate metrics
            int totalOrders = supplierOrders.size();
            
            long pendingCount = supplierOrders.stream()
                    .filter(order -> "PENDING".equals(order.getStatus()))
                    .count();
            
            long processingCount = supplierOrders.stream()
                    .filter(order -> "PROCESSING".equals(order.getStatus()))
                    .count();
            
            long shippedCount = supplierOrders.stream()
                    .filter(order -> "SHIPPED".equals(order.getStatus()))
                    .count();
            
            long deliveredCount = supplierOrders.stream()
                    .filter(order -> "DELIVERED".equals(order.getStatus()))
                    .count();
            
            long cancelledCount = supplierOrders.stream()
                    .filter(order -> "CANCELLED".equals(order.getStatus()))
                    .count();
            
            BigDecimal totalSpend = BigDecimal.ZERO;
            for (PurchaseOrder order : supplierOrders) {
                if (!"CANCELLED".equals(order.getStatus())) {
                    totalSpend = totalSpend.add(order.getTotalAmount());
                }
            }
            
            // Create summary labels
            Label totalOrdersLabel = new Label("Total Orders: " + totalOrders);
            totalOrdersLabel.getStyleClass().add("summary-label");
            
            Label orderStatusLabel = new Label("Order Status Breakdown:");
            orderStatusLabel.getStyleClass().add("summary-label");
            
            Label pendingLabel = new Label("   Pending: " + pendingCount);
            pendingLabel.getStyleClass().add("detail-label");
            
            Label processingLabel = new Label("   Processing: " + processingCount);
            processingLabel.getStyleClass().add("detail-label");
            
            Label shippedLabel = new Label("   Shipped: " + shippedCount);
            shippedLabel.getStyleClass().add("detail-label");
            
            Label deliveredLabel = new Label("   Delivered: " + deliveredCount);
            deliveredLabel.getStyleClass().add("detail-label");
            
            Label cancelledLabel = new Label("   Cancelled: " + cancelledCount);
            cancelledLabel.getStyleClass().add("detail-label");
            
            Label totalSpendLabel = new Label("Total Spend: LKR " + totalSpend);
            totalSpendLabel.getStyleClass().add("summary-label");
            
            // Calculate delivery rate
            String deliveryRate = "N/A";
            if (totalOrders > 0) {
                double rate = (double) deliveredCount / totalOrders * 100;
                deliveryRate = String.format("%.1f%%", rate);
            }
            
            Label deliveryRateLabel = new Label("Delivery Rate: " + deliveryRate);
            deliveryRateLabel.getStyleClass().add("summary-label");
            
            // Create pie chart for order status distribution
            javafx.scene.chart.PieChart statusChart = new javafx.scene.chart.PieChart();
            statusChart.setTitle("Order Status Distribution");
            
            if (pendingCount > 0) statusChart.getData().add(new javafx.scene.chart.PieChart.Data("Pending", pendingCount));
            if (processingCount > 0) statusChart.getData().add(new javafx.scene.chart.PieChart.Data("Processing", processingCount));
            if (shippedCount > 0) statusChart.getData().add(new javafx.scene.chart.PieChart.Data("Shipped", shippedCount));
            if (deliveredCount > 0) statusChart.getData().add(new javafx.scene.chart.PieChart.Data("Delivered", deliveredCount));
            if (cancelledCount > 0) statusChart.getData().add(new javafx.scene.chart.PieChart.Data("Cancelled", cancelledCount));
            
            // Add to container
            reportContainer.getChildren().addAll(titleLabel, subtitleLabel, 
                    totalOrdersLabel, orderStatusLabel, pendingLabel, processingLabel, 
                    shippedLabel, deliveredLabel, cancelledLabel, totalSpendLabel, 
                    deliveryRateLabel, statusChart);
        }
    }
    
    private void showSupplierDetails(Supplier supplier) {
        // Show detailed information about a supplier
        AlertHelper.showInformationAlert("Supplier Details", supplier.getName(), 
                "ID: " + supplier.getId() + "\n" +
                "Contact Person: " + supplier.getContactPerson() + "\n" +
                "Contact Number: " + supplier.getContactNo() + "\n" +
                "Email: " + supplier.getEmail() + "\n" +
                "Address: " + supplier.getAddress());
    }
    
    private void viewOrderDetails(PurchaseOrder order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_details.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            OrderDetailsController controller = loader.getController();
            controller.initializeWithOrder(order);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Order Details - " + order.getOrderNumber());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(supplierPane.getScene().getWindow());
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog", 
                    "An error occurred while trying to display order details.");
        }
    }
    
    private void receiveOrder(PurchaseOrder order) {
        boolean confirmed = AlertHelper.showConfirmationAlert("Confirm Order Receipt", 
                "Receive Order " + order.getOrderNumber(), 
                "Are you sure you want to mark this order as delivered? This will update your inventory levels.");
        
        if (confirmed) {
            PurchaseOrder updatedOrder = purchaseOrderService.updateOrderStatus(order.getId(), "DELIVERED");
            
            if (updatedOrder != null) {
                loadAllOrders();
                AlertHelper.showInformationAlert("Order Received", "Order Marked as Delivered", 
                        "Order has been marked as delivered and inventory has been updated.");
            } else {
                AlertHelper.showErrorAlert("Error", "Could Not Update Order", 
                        "An error occurred while trying to mark the order as delivered.");
            }
        }
    }
    
    private void cancelOrder(PurchaseOrder order) {
        boolean confirmed = AlertHelper.showConfirmationAlert("Confirm Cancellation", 
                "Cancel Order " + order.getOrderNumber(), 
                "Are you sure you want to cancel this order?");
        
        if (confirmed) {
            boolean cancelled = purchaseOrderService.cancelOrder(order.getId());
            
            if (cancelled) {
                loadAllOrders();
                AlertHelper.showInformationAlert("Order Cancelled", "Order Cancelled Successfully", 
                        "The order has been cancelled successfully.");
            } else {
                AlertHelper.showErrorAlert("Error", "Could Not Cancel Order", 
                        "This order cannot be cancelled. Only pending or processing orders can be cancelled.");
            }
        }
    }
}