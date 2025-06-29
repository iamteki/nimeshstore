package com.nimesh.controller;

import com.nimesh.model.Category;
import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.model.InvoiceItem;
import com.nimesh.model.Product;
import com.nimesh.model.ProductBatch;
import com.nimesh.model.PurchaseOrder;
import com.nimesh.service.CategoryService;
import com.nimesh.service.CustomerService;
import com.nimesh.service.InvoiceService;
import com.nimesh.service.ProductService;
import com.nimesh.service.PurchaseOrderService;
import com.nimesh.service.ReportingService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.ExportUtil;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;


// Add these imports if not already present
import com.nimesh.service.SystemConfigService;
import java.util.LinkedHashMap;



@Controller
public class ReportingController implements Initializable {

    
    
    // Add these constants after the existing constants
private static final int DECIMAL_SCALE = 2;
private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

// Add this autowired service after existing autowired services
@Autowired
private SystemConfigService systemConfigService;
    
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    @Autowired
    private ReportingService reportingService;
    
    @Autowired
    private ExportUtil exportUtil;
    
    @FXML
    private BorderPane reportsPane;
    
    // Sales Report Tab
    @FXML
    private ComboBox<String> salesDateRangeCombo;
    
    @FXML
    private DatePicker salesStartDatePicker;
    
    @FXML
    private DatePicker salesEndDatePicker;
    
    @FXML
    private ComboBox<String> salesReportTypeCombo;
    
    @FXML
    private Button generateSalesReportBtn;
    
    @FXML
    private Button exportSalesReportBtn;
    
    @FXML
    private VBox salesReportContainer;
    
    // Inventory Report Tab
    @FXML
    private ComboBox<Category> inventoryCategoryCombo;
    
    @FXML
    private ComboBox<String> inventoryReportTypeCombo;
    
    @FXML
    private Button generateInventoryReportBtn;
    
    @FXML
    private Button exportInventoryReportBtn;
    
    @FXML
    private VBox inventoryReportContainer;
    
    // Customer Report Tab
    @FXML
    private ComboBox<Customer> customerCombo;
    
    @FXML
    private ComboBox<String> customerReportTypeCombo;
    
    @FXML
    private Button generateCustomerReportBtn;
    
    @FXML
    private VBox customerReportContainer;
    
    // Financial Report Tab
    @FXML
    private ComboBox<String> financialDateRangeCombo;
    
    @FXML
    private DatePicker financialStartDatePicker;
    
    @FXML
    private DatePicker financialEndDatePicker;
    
    @FXML
    private ComboBox<String> financialReportTypeCombo;
    
    @FXML
    private Button generateFinancialReportBtn;
    
    @FXML
    private Button exportFinancialReportBtn;
    
    @FXML
    private VBox financialReportContainer;
    
    
    
    // Batch Report Tab Fields
    @FXML
    private ComboBox<Product> batchProductCombo;
    
    @FXML
    private ComboBox<String> batchStatusCombo;
    
    @FXML
    private ComboBox<String> batchDateRangeCombo;
    
    @FXML
    private ComboBox<String> batchReportTypeCombo;
    
    @FXML
    private Button generateBatchReportBtn;
    
    @FXML
    private Button exportBatchReportBtn;
    
    @FXML
    private VBox batchReportContainer;
    
    
    
    
    // Current report data for exporting
    private Object currentReportData;
    private String currentReportType;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize date range and report type combos
        initializeComboBoxes();
        
        // Set default date values
        setDefaultDates();
        
        // Add listeners for date range selections
        setupDateRangeListeners();
        
        // Export buttons start disabled until report is generated
        exportSalesReportBtn.setDisable(true);
        exportInventoryReportBtn.setDisable(true);
        exportFinancialReportBtn.setDisable(true);
        // Batch export button starts disabled until report is generated
        exportBatchReportBtn.setDisable(true);
    }
    
    
    
    private void generateBatchPerformanceAnalysis(Product selectedProduct, String batchStatus, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, batchStatus, dateRange);
        
        // Get performance metrics
        Map<ProductBatch, Map<String, Object>> batchMetrics = reportingService.getBatchPerformanceMetrics(batches);
        
        // Create report title
        Label titleLabel = new Label("Batch Performance Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Status: " + batchStatus + " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate summary metrics
        int totalBatches = batches.size();
        double avgUtilization = batchMetrics.values().stream()
                .mapToDouble(metrics -> (Double) metrics.get("utilizationPct"))
                .average().orElse(0.0);
        
        BigDecimal totalInvestment = batchMetrics.values().stream()
                .map(metrics -> (BigDecimal) metrics.get("totalInvestment"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCurrentValue = batchMetrics.values().stream()
                .map(metrics -> (BigDecimal) metrics.get("currentValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double avgSalesVelocity = batchMetrics.values().stream()
                .mapToDouble(metrics -> (Double) metrics.get("salesVelocity"))
                .average().orElse(0.0);
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches: " + totalBatches);
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label avgUtilizationLabel = new Label("Average Utilization: " + String.format("%.2f%%", avgUtilization));
        avgUtilizationLabel.getStyleClass().add("summary-item");
        
        Label totalInvestmentLabel = new Label("Total Investment: LKR " + formatDecimal(totalInvestment));
        totalInvestmentLabel.getStyleClass().add("summary-item");
        
        Label currentValueLabel = new Label("Current Value: LKR " + formatDecimal(totalCurrentValue));
        currentValueLabel.getStyleClass().add("summary-item");
        
        Label avgVelocityLabel = new Label("Average Sales Velocity: " + String.format("%.2f units/day", avgSalesVelocity));
        avgVelocityLabel.getStyleClass().add("summary-item");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, avgUtilizationLabel, totalInvestmentLabel, 
                currentValueLabel, avgVelocityLabel);
        
        // Create utilization distribution chart
        CategoryAxis utilizationXAxis = new CategoryAxis();
        NumberAxis utilizationYAxis = new NumberAxis();
        utilizationXAxis.setLabel("Utilization Range");
        utilizationYAxis.setLabel("Number of Batches");
        
        BarChart<String, Number> utilizationChart = new BarChart<>(utilizationXAxis, utilizationYAxis);
        utilizationChart.setTitle("Batch Utilization Distribution");
        
        // Group batches by utilization ranges
        Map<String, Integer> utilizationRanges = new TreeMap<>();
        utilizationRanges.put("0-20%", 0);
        utilizationRanges.put("21-40%", 0);
        utilizationRanges.put("41-60%", 0);
        utilizationRanges.put("61-80%", 0);
        utilizationRanges.put("81-100%", 0);
        
        for (Map<String, Object> metrics : batchMetrics.values()) {
            double utilization = (Double) metrics.get("utilizationPct");
            
            if (utilization <= 20) {
                utilizationRanges.put("0-20%", utilizationRanges.get("0-20%") + 1);
            } else if (utilization <= 40) {
                utilizationRanges.put("21-40%", utilizationRanges.get("21-40%") + 1);
            } else if (utilization <= 60) {
                utilizationRanges.put("41-60%", utilizationRanges.get("41-60%") + 1);
            } else if (utilization <= 80) {
                utilizationRanges.put("61-80%", utilizationRanges.get("61-80%") + 1);
            } else {
                utilizationRanges.put("81-100%", utilizationRanges.get("81-100%") + 1);
            }
        }
        
        XYChart.Series<String, Number> utilizationSeries = new XYChart.Series<>();
        utilizationSeries.setName("Batches");
        
        for (Map.Entry<String, Integer> entry : utilizationRanges.entrySet()) {
            utilizationSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        utilizationChart.getData().add(utilizationSeries);
        
        // Create performance table
        TableView<Map.Entry<ProductBatch, Map<String, Object>>> performanceTable = new TableView<>();
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, String> productColumn = 
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getProduct().getName()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, String> batchColumn = 
                new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getBatchNumber()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, BigDecimal> initialQtyColumn = 
                new TableColumn<>("Initial Qty");
        initialQtyColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getKey().getInitialQuantity()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, BigDecimal> remainingQtyColumn = 
                new TableColumn<>("Remaining Qty");
        remainingQtyColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getKey().getRemainingQuantity()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, String> utilizationColumn = 
                new TableColumn<>("Utilization %");
        utilizationColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%.2f%%", 
                        (Double) data.getValue().getValue().get("utilizationPct"))));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, String> velocityColumn = 
                new TableColumn<>("Sales Velocity");
        velocityColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%.2f units/day", 
                        (Double) data.getValue().getValue().get("salesVelocity"))));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, Long> ageColumn = 
                new TableColumn<>("Age (Days)");
        ageColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((Long) data.getValue().getValue().get("daysSincePurchase")));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, Object>>, String> expiryStatusColumn = 
                new TableColumn<>("Expiry Status");
        expiryStatusColumn.setCellValueFactory(data -> 
                new SimpleStringProperty((String) data.getValue().getValue().get("expiryStatus")));
        
        // Color code expiry status
        expiryStatusColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, Object>>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "Expired":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red
                            break;
                        case "Critical":
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Orange
                            break;
                        case "Warning":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Yellow
                            break;
                        case "Good":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d;"); // Gray
                            break;
                    }
                }
            }
        });
        
        // Format decimal columns
        initialQtyColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, Object>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        remainingQtyColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, Object>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        performanceTable.getColumns().addAll(productColumn, batchColumn, initialQtyColumn, 
                remainingQtyColumn, utilizationColumn, velocityColumn, ageColumn, expiryStatusColumn);
        
        // Sort by utilization percentage (descending)
        List<Map.Entry<ProductBatch, Map<String, Object>>> sortedMetrics = 
                batchMetrics.entrySet().stream()
                        .sorted((e1, e2) -> {
                            Double util1 = (Double) e1.getValue().get("utilizationPct");
                            Double util2 = (Double) e2.getValue().get("utilizationPct");
                            return util2.compareTo(util1);
                        })
                        .collect(Collectors.toList());
        
        performanceTable.setItems(FXCollections.observableArrayList(sortedMetrics));
        
        // Store report data for export
        currentReportData = sortedMetrics;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, utilizationChart, performanceTable);
    }
    
    
    
    @FXML
    private void handleGenerateBatchReport(ActionEvent event) {
        // Clear previous report
        batchReportContainer.getChildren().clear();
        
        // Get filter parameters
        Product selectedProduct = batchProductCombo.getValue();
        String batchStatus = batchStatusCombo.getValue();
        String dateRange = batchDateRangeCombo.getValue();
        String reportType = batchReportTypeCombo.getValue();
        
        currentReportType = reportType;
        
        switch (reportType) {
            case "Batch Performance Analysis":
                generateBatchPerformanceAnalysis(selectedProduct, batchStatus, dateRange);
                break;
                
            case "Batch Cost Analysis":
                generateBatchCostAnalysis(selectedProduct, batchStatus, dateRange);
                break;
                
            case "Batch Expiry Report":
                generateBatchExpiryReport(selectedProduct, dateRange);
                break;
                
            case "Batch Turnover Analysis":
                generateBatchTurnoverAnalysis(selectedProduct, batchStatus, dateRange);
                break;
                
            case "Batch Profitability Analysis":
                generateBatchProfitabilityAnalysis(selectedProduct, batchStatus, dateRange);
                break;
                
            case "Batch Age Analysis":
                generateBatchAgeAnalysis(selectedProduct, batchStatus, dateRange);
                break;
        }
        
        // Enable export button
        exportBatchReportBtn.setDisable(false);
    }
    
    
    private void generateBatchAgeAnalysis(Product selectedProduct, String batchStatus, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, batchStatus, dateRange);
        
        // Create report title
        Label titleLabel = new Label("Batch Age Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Status: " + batchStatus + " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate age statistics
        LocalDate today = LocalDate.now();
        List<Long> ages = batches.stream()
                .map(batch -> ChronoUnit.DAYS.between(batch.getPurchaseDate().toLocalDate(), today))
                .collect(Collectors.toList());
        
        double avgAge = ages.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxAge = ages.stream().mapToLong(Long::longValue).max().orElse(0);
        long minAge = ages.stream().mapToLong(Long::longValue).min().orElse(0);
        
        // Count batches by age ranges
        int veryNew = (int) ages.stream().filter(age -> age <= 30).count();
        int newBatches = (int) ages.stream().filter(age -> age > 30 && age <= 90).count();
        int mature = (int) ages.stream().filter(age -> age > 90 && age <= 180).count();
        int old = (int) ages.stream().filter(age -> age > 180 && age <= 365).count();
        int veryOld = (int) ages.stream().filter(age -> age > 365).count();
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches: " + batches.size());
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label avgAgeLabel = new Label("Average Age: " + String.format("%.1f days", avgAge));
        avgAgeLabel.getStyleClass().add("summary-item");
        
        Label maxAgeLabel = new Label("Oldest Batch: " + maxAge + " days");
        maxAgeLabel.getStyleClass().add("summary-item");
        
        Label minAgeLabel = new Label("Newest Batch: " + minAge + " days");
        minAgeLabel.getStyleClass().add("summary-item");
        
        Label veryNewLabel = new Label("Very New (≤30 days): " + veryNew);
        veryNewLabel.getStyleClass().add("summary-item");
        veryNewLabel.setStyle("-fx-text-fill: #2ecc71;");
        
        Label newLabel = new Label("New (31-90 days): " + newBatches);
        newLabel.getStyleClass().add("summary-item");
        newLabel.setStyle("-fx-text-fill: #3498db;");
        
        Label matureLabel = new Label("Mature (91-180 days): " + mature);
        matureLabel.getStyleClass().add("summary-item");
        matureLabel.setStyle("-fx-text-fill: #f39c12;");
        
        Label oldLabel = new Label("Old (181-365 days): " + old);
        oldLabel.getStyleClass().add("summary-item");
        oldLabel.setStyle("-fx-text-fill: #e67e22;");
        
        Label veryOldLabel = new Label("Very Old (>365 days): " + veryOld);
        veryOldLabel.getStyleClass().add("summary-item");
        veryOldLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, avgAgeLabel, maxAgeLabel, minAgeLabel,
                veryNewLabel, newLabel, matureLabel, oldLabel, veryOldLabel);
        
        // Create age distribution chart
        CategoryAxis ageXAxis = new CategoryAxis();
        NumberAxis ageYAxis = new NumberAxis();
        ageXAxis.setLabel("Age Range (Days)");
        ageYAxis.setLabel("Number of Batches");
        
        BarChart<String, Number> ageChart = new BarChart<>(ageXAxis, ageYAxis);
        ageChart.setTitle("Batch Age Distribution");
        
        XYChart.Series<String, Number> ageSeries = new XYChart.Series<>();
        ageSeries.setName("Batches");
        
        ageSeries.getData().add(new XYChart.Data<>("≤30 days", veryNew));
        ageSeries.getData().add(new XYChart.Data<>("31-90 days", newBatches));
        ageSeries.getData().add(new XYChart.Data<>("91-180 days", mature));
        ageSeries.getData().add(new XYChart.Data<>("181-365 days", old));
        ageSeries.getData().add(new XYChart.Data<>(">365 days", veryOld));
        
        ageChart.getData().add(ageSeries);
        
        // Create age vs utilization scatter chart
        NumberAxis utilizationXAxis = new NumberAxis();
        NumberAxis ageScatterYAxis = new NumberAxis();
        utilizationXAxis.setLabel("Utilization %");
        ageScatterYAxis.setLabel("Age (Days)");
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
        LineChart<Number, Number> ageUtilizationChart = new LineChart(utilizationXAxis, ageScatterYAxis);
        ageUtilizationChart.setTitle("Age vs Utilization");
        ageUtilizationChart.setCreateSymbols(true);
        ageUtilizationChart.setLegendVisible(false);
        
        XYChart.Series<Number, Number> scatterSeries = new XYChart.Series<>();
        scatterSeries.setName("Batches");
        
        for (ProductBatch batch : batches) {
            long age = ChronoUnit.DAYS.between(batch.getPurchaseDate().toLocalDate(), today);
            BigDecimal utilization = batch.getInitialQuantity().subtract(batch.getRemainingQuantity())
                    .divide(batch.getInitialQuantity(), 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
            
            scatterSeries.getData().add(new XYChart.Data<>(utilization.doubleValue(), age));
        }
        
        ageUtilizationChart.getData().add(scatterSeries);
        
        // Create charts container
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(ageChart, ageUtilizationChart);
        
        // Create age analysis table
        TableView<ProductBatch> ageTable = new TableView<>();
        
        TableColumn<ProductBatch, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        
        TableColumn<ProductBatch, String> batchColumn = new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBatchNumber()));
        
        TableColumn<ProductBatch, String> purchaseDateColumn = new TableColumn<>("Purchase Date");
        purchaseDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPurchaseDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        
        TableColumn<ProductBatch, Long> ageColumn = new TableColumn<>("Age (Days)");
        ageColumn.setCellValueFactory(cellData -> {
            long age = ChronoUnit.DAYS.between(
                    cellData.getValue().getPurchaseDate().toLocalDate(), 
                    today
            );
            return new SimpleObjectProperty<>(age);
        });
        
        TableColumn<ProductBatch, String> ageCategoryColumn = new TableColumn<>("Age Category");
        ageCategoryColumn.setCellValueFactory(cellData -> {
            long age = ChronoUnit.DAYS.between(
                    cellData.getValue().getPurchaseDate().toLocalDate(), 
                    today
            );
            
            String category;
            if (age <= 30) {
                category = "Very New";
            } else if (age <= 90) {
                category = "New";
            } else if (age <= 180) {
                category = "Mature";
            } else if (age <= 365) {
                category = "Old";
            } else {
                category = "Very Old";
            }
            
            return new SimpleStringProperty(category);
        });
        
        TableColumn<ProductBatch, BigDecimal> initialQtyColumn = new TableColumn<>("Initial Qty");
        initialQtyColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getInitialQuantity()));
        
        TableColumn<ProductBatch, BigDecimal> remainingQtyColumn = new TableColumn<>("Remaining Qty");
        remainingQtyColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getRemainingQuantity()));
        
        TableColumn<ProductBatch, String> utilizationColumn = new TableColumn<>("Utilization %");
        utilizationColumn.setCellValueFactory(cellData -> {
            ProductBatch batch = cellData.getValue();
            BigDecimal utilization = batch.getInitialQuantity().subtract(batch.getRemainingQuantity())
                    .divide(batch.getInitialQuantity(), 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
            return new SimpleStringProperty(formatDecimal(utilization) + "%");
        });
        
        TableColumn<ProductBatch, BigDecimal> currentValueColumn = new TableColumn<>("Current Value");
        currentValueColumn.setCellValueFactory(cellData -> {
            ProductBatch batch = cellData.getValue();
            BigDecimal value = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            return new SimpleObjectProperty<>(value);
        });
        
        // Color code age category column
        ageCategoryColumn.setCellFactory(column -> new TableCell<ProductBatch, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "Very New":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "New":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "Mature":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Old":
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                            break;
                        case "Very Old":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Format decimal columns
        initialQtyColumn.setCellFactory(column -> new TableCell<ProductBatch, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        remainingQtyColumn.setCellFactory(column -> new TableCell<ProductBatch, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        currentValueColumn.setCellFactory(column -> new TableCell<ProductBatch, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        ageTable.getColumns().addAll(productColumn, batchColumn, purchaseDateColumn, 
                ageColumn, ageCategoryColumn, initialQtyColumn, remainingQtyColumn, 
                utilizationColumn, currentValueColumn);
        
        // Sort by age (oldest first)
        List<ProductBatch> sortedBatches = batches.stream()
                .sorted((b1, b2) -> b1.getPurchaseDate().compareTo(b2.getPurchaseDate()))
                .collect(Collectors.toList());
        
        ageTable.setItems(FXCollections.observableArrayList(sortedBatches));
        
        // Store report data for export
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (ProductBatch batch : sortedBatches) {
            Map<String, Object> data = new HashMap<>();
            data.put("product", batch.getProduct().getName());
            data.put("batchNumber", batch.getBatchNumber());
            data.put("purchaseDate", batch.getPurchaseDate());
            
            long age = ChronoUnit.DAYS.between(batch.getPurchaseDate().toLocalDate(), today);
            data.put("ageInDays", age);
            
            String ageCategory;
            if (age <= 30) {
                ageCategory = "Very New";
            } else if (age <= 90) {
                ageCategory = "New";
            } else if (age <= 180) {
                ageCategory = "Mature";
            } else if (age <= 365) {
                ageCategory = "Old";
            } else {
                ageCategory = "Very Old";
            }
            data.put("ageCategory", ageCategory);
            
            data.put("initialQuantity", batch.getInitialQuantity());
            data.put("remainingQuantity", batch.getRemainingQuantity());
            
            BigDecimal utilization = batch.getInitialQuantity().subtract(batch.getRemainingQuantity())
                    .divide(batch.getInitialQuantity(), 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
            data.put("utilizationPercent", utilization);
            
            BigDecimal currentValue = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            data.put("currentValue", currentValue);
            
            exportData.add(data);
        }
        
        currentReportData = exportData;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, chartsBox, ageTable);
    }
    
    
    
    private void generateBatchProfitabilityAnalysis(Product selectedProduct, String batchStatus, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, batchStatus, dateRange);
        
        // Get date range for profitability calculation (use all time)
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = endDateTime.minusYears(10); // Large range to capture all sales
        
        // Get batch profitability data
        Map<ProductBatch, Map<String, BigDecimal>> batchProfitability = 
                reportingService.getBatchProfitability(batches, startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Batch Profitability Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Status: " + batchStatus + " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate overall profitability metrics
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int profitableBatches = 0;
        int lossMakingBatches = 0;
        
        for (Map<String, BigDecimal> profitData : batchProfitability.values()) {
            BigDecimal revenue = profitData.get("revenue");
            BigDecimal cost = profitData.get("cost");
            BigDecimal profit = profitData.get("profit");
            
            totalRevenue = totalRevenue.add(revenue);
            totalCost = totalCost.add(cost);
            totalProfit = totalProfit.add(profit);
            
            if (profit.compareTo(BigDecimal.ZERO) > 0) {
                profitableBatches++;
            } else if (profit.compareTo(BigDecimal.ZERO) < 0) {
                lossMakingBatches++;
            }
        }
        
        double overallMargin = 0.0;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            overallMargin = totalProfit.divide(totalRevenue, 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100")).doubleValue();
        }
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches Analyzed: " + batches.size());
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label totalRevenueLabel = new Label("Total Revenue: LKR " + formatDecimal(totalRevenue));
        totalRevenueLabel.getStyleClass().add("summary-item");
        
        Label totalCostLabel = new Label("Total Cost: LKR " + formatDecimal(totalCost));
        totalCostLabel.getStyleClass().add("summary-item");
        
        Label totalProfitLabel = new Label("Total Profit: LKR " + formatDecimal(totalProfit));
        totalProfitLabel.getStyleClass().add("summary-item");
        if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else if (totalProfit.compareTo(BigDecimal.ZERO) < 0) {
            totalProfitLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        
        Label overallMarginLabel = new Label("Overall Profit Margin: " + String.format("%.2f%%", overallMargin));
        overallMarginLabel.getStyleClass().add("summary-item");
        
        Label profitableBatchesLabel = new Label("Profitable Batches: " + profitableBatches);
        profitableBatchesLabel.getStyleClass().add("summary-item");
        profitableBatchesLabel.setStyle("-fx-text-fill: #2ecc71;");
        
        Label lossBatchesLabel = new Label("Loss-Making Batches: " + lossMakingBatches);
        lossBatchesLabel.getStyleClass().add("summary-item");
        lossBatchesLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, totalRevenueLabel, totalCostLabel, totalProfitLabel,
                overallMarginLabel, profitableBatchesLabel, lossBatchesLabel);
        
        // Create profit margin distribution chart
        CategoryAxis marginXAxis = new CategoryAxis();
        NumberAxis marginYAxis = new NumberAxis();
        marginXAxis.setLabel("Profit Margin Range");
        marginYAxis.setLabel("Number of Batches");
        
        BarChart<String, Number> marginChart = new BarChart<>(marginXAxis, marginYAxis);
        marginChart.setTitle("Profit Margin Distribution");
        
        // Group batches by margin ranges
        Map<String, Integer> marginRanges = new LinkedHashMap<>();
        marginRanges.put("< -20%", 0);
        marginRanges.put("-20% to 0%", 0);
        marginRanges.put("0% to 10%", 0);
        marginRanges.put("10% to 25%", 0);
        marginRanges.put("25% to 50%", 0);
        marginRanges.put("> 50%", 0);
        marginRanges.put("No Sales", 0);
        
        for (Map.Entry<ProductBatch, Map<String, BigDecimal>> entry : batchProfitability.entrySet()) {
            Map<String, BigDecimal> profitData = entry.getValue();
            BigDecimal revenue = profitData.get("revenue");
            
            if (revenue.compareTo(BigDecimal.ZERO) == 0) {
                marginRanges.put("No Sales", marginRanges.get("No Sales") + 1);
            } else {
                BigDecimal marginPct = profitData.get("profitMargin");
                double margin = marginPct.doubleValue();
                
                if (margin < -20) {
                    marginRanges.put("< -20%", marginRanges.get("< -20%") + 1);
                } else if (margin < 0) {
                    marginRanges.put("-20% to 0%", marginRanges.get("-20% to 0%") + 1);
                } else if (margin < 10) {
                    marginRanges.put("0% to 10%", marginRanges.get("0% to 10%") + 1);
                } else if (margin < 25) {
                    marginRanges.put("10% to 25%", marginRanges.get("10% to 25%") + 1);
                } else if (margin < 50) {
                    marginRanges.put("25% to 50%", marginRanges.get("25% to 50%") + 1);
                } else {
                    marginRanges.put("> 50%", marginRanges.get("> 50%") + 1);
                }
            }
        }
        
        XYChart.Series<String, Number> marginSeries = new XYChart.Series<>();
        marginSeries.setName("Batches");
        
        for (Map.Entry<String, Integer> entry : marginRanges.entrySet()) {
            if (entry.getValue() > 0) {
                marginSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        
        marginChart.getData().add(marginSeries);
        
        // Create pie chart for profit/loss distribution
        PieChart profitLossChart = new PieChart();
        profitLossChart.setTitle("Batch Profit/Loss Distribution");
        
        if (profitableBatches > 0) {
            profitLossChart.getData().add(new PieChart.Data("Profitable (" + profitableBatches + ")", profitableBatches));
        }
        if (lossMakingBatches > 0) {
            profitLossChart.getData().add(new PieChart.Data("Loss-Making (" + lossMakingBatches + ")", lossMakingBatches));
        }
        int breakEvenBatches = batches.size() - profitableBatches - lossMakingBatches;
        if (breakEvenBatches > 0) {
            profitLossChart.getData().add(new PieChart.Data("Break-Even/No Sales (" + breakEvenBatches + ")", breakEvenBatches));
        }
        
        // Create charts container
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(marginChart, profitLossChart);
        
        // Create profitability table
        TableView<Map.Entry<ProductBatch, Map<String, BigDecimal>>> profitabilityTable = new TableView<>();
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, String> productColumn = 
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getProduct().getName()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, String> batchColumn = 
                new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getBatchNumber()));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal> quantitySoldColumn = 
                new TableColumn<>("Quantity Sold");
        quantitySoldColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getValue().get("quantitySold")));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal> revenueColumn = 
                new TableColumn<>("Revenue");
        revenueColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getValue().get("revenue")));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal> costColumn = 
                new TableColumn<>("Cost");
        costColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getValue().get("cost")));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal> profitColumn = 
                new TableColumn<>("Profit");
        profitColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getValue().get("profit")));
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, String> marginColumn = 
                new TableColumn<>("Margin %");
        marginColumn.setCellValueFactory(data -> {
            BigDecimal margin = data.getValue().getValue().get("profitMargin");
            return new SimpleStringProperty(String.format("%.2f%%", margin));
        });
        
        TableColumn<Map.Entry<ProductBatch, Map<String, BigDecimal>>, String> statusColumn = 
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            BigDecimal profit = data.getValue().getValue().get("profit");
            BigDecimal revenue = data.getValue().getValue().get("revenue");
            
            if (revenue.compareTo(BigDecimal.ZERO) == 0) {
                return new SimpleStringProperty("No Sales");
            } else if (profit.compareTo(BigDecimal.ZERO) > 0) {
                return new SimpleStringProperty("Profitable");
            } else if (profit.compareTo(BigDecimal.ZERO) < 0) {
                return new SimpleStringProperty("Loss");
            } else {
                return new SimpleStringProperty("Break-Even");
            }
        });
        
        // Color code status and profit columns
        statusColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, BigDecimal>>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "Profitable":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "Loss":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Break-Even":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "No Sales":
                            setStyle("-fx-text-fill: #7f8c8d;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        profitColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("LKR " + formatDecimal(item));
                    
                    if (item.compareTo(BigDecimal.ZERO) > 0) {
                        setStyle("-fx-text-fill: #2ecc71;");
                    } else if (item.compareTo(BigDecimal.ZERO) < 0) {
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Format currency columns
        revenueColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        costColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        quantitySoldColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Map<String, BigDecimal>>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        profitabilityTable.getColumns().addAll(productColumn, batchColumn, quantitySoldColumn, 
                revenueColumn, costColumn, profitColumn, marginColumn, statusColumn);
        
        // Sort by profit (highest first)
        List<Map.Entry<ProductBatch, Map<String, BigDecimal>>> sortedProfitability = 
                batchProfitability.entrySet().stream()
                        .sorted((e1, e2) -> {
                            BigDecimal profit1 = e1.getValue().get("profit");
                            BigDecimal profit2 = e2.getValue().get("profit");
                            return profit2.compareTo(profit1);
                        })
                        .collect(Collectors.toList());
        
        profitabilityTable.setItems(FXCollections.observableArrayList(sortedProfitability));
        
        // Store report data for export
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (Map.Entry<ProductBatch, Map<String, BigDecimal>> entry : sortedProfitability) {
            ProductBatch batch = entry.getKey();
            Map<String, BigDecimal> profitData = entry.getValue();
            
            Map<String, Object> data = new HashMap<>();
            data.put("product", batch.getProduct().getName());
            data.put("batchNumber", batch.getBatchNumber());
            data.put("quantitySold", profitData.get("quantitySold"));
            data.put("revenue", profitData.get("revenue"));
            data.put("cost", profitData.get("cost"));
            data.put("profit", profitData.get("profit"));
            data.put("profitMargin", profitData.get("profitMargin"));
            
            BigDecimal profit = profitData.get("profit");
            BigDecimal revenue = profitData.get("revenue");
            
            if (revenue.compareTo(BigDecimal.ZERO) == 0) {
                data.put("status", "No Sales");
            } else if (profit.compareTo(BigDecimal.ZERO) > 0) {
                data.put("status", "Profitable");
            } else if (profit.compareTo(BigDecimal.ZERO) < 0) {
                data.put("status", "Loss");
            } else {
                data.put("status", "Break-Even");
            }
            
            exportData.add(data);
        }
        
        currentReportData = exportData;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, chartsBox, profitabilityTable);
    }
    
    
    
    
    
    
    @FXML
    private void handleExportBatchReport(ActionEvent event) {
        if (currentReportData == null) {
            AlertHelper.showErrorAlert("No Report Data", "No report data available to export", 
                    "Please generate a report first before attempting to export.");
            return;
        }
        
        exportReport(currentReportData, "batch_report_" + currentReportType.toLowerCase().replace(" ", "_"));
    }
    
    
    private void generateBatchExpiryReport(Product selectedProduct, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, "All Batches", dateRange);
        
        // Get expiry analysis
        Map<String, Object> expiryAnalysis = reportingService.getBatchExpiryAnalysis(batches);
        
        // Create report title
        Label titleLabel = new Label("Batch Expiry Report");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Extract analysis data
        int expired = (Integer) expiryAnalysis.get("expired");
        int critical = (Integer) expiryAnalysis.get("critical");
        int warning = (Integer) expiryAnalysis.get("warning");
        int good = (Integer) expiryAnalysis.get("good");
        int noExpiry = (Integer) expiryAnalysis.get("noExpiry");
        
        BigDecimal expiredValue = (BigDecimal) expiryAnalysis.get("expiredValue");
        BigDecimal criticalValue = (BigDecimal) expiryAnalysis.get("criticalValue");
        BigDecimal warningValue = (BigDecimal) expiryAnalysis.get("warningValue");
        
        @SuppressWarnings("unchecked")
        List<ProductBatch> expiringBatches = (List<ProductBatch>) expiryAnalysis.get("expiringBatches");
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches: " + batches.size());
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label expiredLabel = new Label("Expired Batches: " + expired);
        expiredLabel.getStyleClass().add("summary-item");
        expiredLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label criticalLabel = new Label("Critical (≤7 days): " + critical);
        criticalLabel.getStyleClass().add("summary-item");
        criticalLabel.setStyle("-fx-text-fill: #e67e22;");
        
        Label warningLabel = new Label("Warning (≤30 days): " + warning);
        warningLabel.getStyleClass().add("summary-item");
        warningLabel.setStyle("-fx-text-fill: #f39c12;");
        
        Label goodLabel = new Label("Good (>30 days): " + good);
        goodLabel.getStyleClass().add("summary-item");
        goodLabel.setStyle("-fx-text-fill: #2ecc71;");
        
        Label noExpiryLabel = new Label("No Expiry Date: " + noExpiry);
        noExpiryLabel.getStyleClass().add("summary-item");
        
        Label riskValueLabel = new Label("At-Risk Value: LKR " + 
                formatDecimal(expiredValue.add(criticalValue).add(warningValue)));
        riskValueLabel.getStyleClass().add("summary-item");
        riskValueLabel.setStyle("-fx-font-weight: bold;");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, expiredLabel, criticalLabel, warningLabel, 
                goodLabel, noExpiryLabel, riskValueLabel);
        
        // Create expiry status pie chart
        PieChart expiryStatusChart = new PieChart();
        expiryStatusChart.setTitle("Batch Expiry Status Distribution");
        
        if (expired > 0) {
            expiryStatusChart.getData().add(new PieChart.Data("Expired (" + expired + ")", expired));
        }
        if (critical > 0) {
            expiryStatusChart.getData().add(new PieChart.Data("Critical (" + critical + ")", critical));
        }
        if (warning > 0) {
            expiryStatusChart.getData().add(new PieChart.Data("Warning (" + warning + ")", warning));
        }
        if (good > 0) {
            expiryStatusChart.getData().add(new PieChart.Data("Good (" + good + ")", good));
        }
        if (noExpiry > 0) {
            expiryStatusChart.getData().add(new PieChart.Data("No Expiry (" + noExpiry + ")", noExpiry));
        }
        
        // Create expiry timeline chart (next 90 days)
        CategoryAxis timelineXAxis = new CategoryAxis();
        NumberAxis timelineYAxis = new NumberAxis();
        timelineXAxis.setLabel("Days from Now");
        timelineYAxis.setLabel("Number of Batches Expiring");
        
        BarChart<String, Number> timelineChart = new BarChart<>(timelineXAxis, timelineYAxis);
        timelineChart.setTitle("Batch Expiry Timeline (Next 90 Days)");
        
        // Group batches by expiry periods
        Map<String, Integer> expiryTimeline = new LinkedHashMap<>();
        expiryTimeline.put("0-7 days", 0);
        expiryTimeline.put("8-14 days", 0);
        expiryTimeline.put("15-30 days", 0);
        expiryTimeline.put("31-60 days", 0);
        expiryTimeline.put("61-90 days", 0);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (ProductBatch batch : batches) {
            if (batch.getExpiryDate() != null && batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                long daysToExpiry = ChronoUnit.DAYS.between(now.toLocalDate(), batch.getExpiryDate().toLocalDate());
                
                if (daysToExpiry >= 0 && daysToExpiry <= 7) {
                    expiryTimeline.put("0-7 days", expiryTimeline.get("0-7 days") + 1);
                } else if (daysToExpiry <= 14) {
                    expiryTimeline.put("8-14 days", expiryTimeline.get("8-14 days") + 1);
                } else if (daysToExpiry <= 30) {
                    expiryTimeline.put("15-30 days", expiryTimeline.get("15-30 days") + 1);
                } else if (daysToExpiry <= 60) {
                    expiryTimeline.put("31-60 days", expiryTimeline.get("31-60 days") + 1);
                } else if (daysToExpiry <= 90) {
                    expiryTimeline.put("61-90 days", expiryTimeline.get("61-90 days") + 1);
                }
            }
        }
        
        XYChart.Series<String, Number> timelineSeries = new XYChart.Series<>();
        timelineSeries.setName("Expiring Batches");
        
        for (Map.Entry<String, Integer> entry : expiryTimeline.entrySet()) {
            timelineSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        timelineChart.getData().add(timelineSeries);
        
        // Create charts container
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(expiryStatusChart, timelineChart);
        
        // Create detailed expiry table
        TableView<ProductBatch> expiryTable = new TableView<>();
        
        TableColumn<ProductBatch, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        
        TableColumn<ProductBatch, String> batchColumn = new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getBatchNumber()));
        
        TableColumn<ProductBatch, BigDecimal> remainingQtyColumn = new TableColumn<>("Remaining Qty");
        remainingQtyColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getRemainingQuantity()));
        
        TableColumn<ProductBatch, String> purchaseDateColumn = new TableColumn<>("Purchase Date");
        purchaseDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPurchaseDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        
        TableColumn<ProductBatch, String> expiryDateColumn = new TableColumn<>("Expiry Date");
        expiryDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExpiryDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getExpiryDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            } else {
                return new SimpleStringProperty("No Expiry");
            }
        });
        
        TableColumn<ProductBatch, String> daysToExpiryColumn = new TableColumn<>("Days to Expiry");
        daysToExpiryColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExpiryDate() != null) {
                long days = ChronoUnit.DAYS.between(
                        LocalDate.now(), 
                        cellData.getValue().getExpiryDate().toLocalDate()
                );
                return new SimpleStringProperty(String.valueOf(days));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        
        TableColumn<ProductBatch, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExpiryDate() == null) {
                return new SimpleStringProperty("No Expiry");
            }
            
            long days = ChronoUnit.DAYS.between(
                    LocalDate.now(), 
                    cellData.getValue().getExpiryDate().toLocalDate()
            );
            
            if (days < 0) {
                return new SimpleStringProperty("EXPIRED");
            } else if (days <= 7) {
                return new SimpleStringProperty("CRITICAL");
            } else if (days <= 30) {
                return new SimpleStringProperty("WARNING");
            } else {
                return new SimpleStringProperty("GOOD");
            }
        });
        
        TableColumn<ProductBatch, BigDecimal> valueAtRiskColumn = new TableColumn<>("Value at Risk");
        valueAtRiskColumn.setCellValueFactory(cellData -> {
            BigDecimal value = cellData.getValue().getBuyingPrice()
                    .multiply(cellData.getValue().getRemainingQuantity());
            return new SimpleObjectProperty<>(value);
        });
        
        // Color code status column
        statusColumn.setCellFactory(column -> new TableCell<ProductBatch, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "EXPIRED":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "CRITICAL":
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                            break;
                        case "WARNING":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "GOOD":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d;");
                            break;
                    }
                }
            }
        });
        
        // Color code days to expiry column
        daysToExpiryColumn.setCellFactory(column -> new TableCell<ProductBatch, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty || item.equals("N/A")) {
                    setText(item);
                    setStyle("");
                } else {
                    setText(item);
                    
                    try {
                        int days = Integer.parseInt(item);
                        if (days < 0) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else if (days <= 7) {
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                        } else if (days <= 30) {
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #2ecc71;");
                        }
                    } catch (NumberFormatException e) {
                        setStyle("");
                    }
                }
            }
        });
        
        // Format quantity and value columns
        remainingQtyColumn.setCellFactory(column -> new TableCell<ProductBatch, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        valueAtRiskColumn.setCellFactory(column -> new TableCell<ProductBatch, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        expiryTable.getColumns().addAll(productColumn, batchColumn, remainingQtyColumn, 
                purchaseDateColumn, expiryDateColumn, daysToExpiryColumn, statusColumn, valueAtRiskColumn);
        
        // Sort by expiry status (most urgent first)
        List<ProductBatch> sortedBatches = batches.stream()
                .sorted((b1, b2) -> {
                    // Sort by expiry urgency
                    if (b1.getExpiryDate() == null && b2.getExpiryDate() == null) return 0;
                    if (b1.getExpiryDate() == null) return 1;
                    if (b2.getExpiryDate() == null) return -1;
                    
                    long days1 = ChronoUnit.DAYS.between(LocalDate.now(), b1.getExpiryDate().toLocalDate());
                    long days2 = ChronoUnit.DAYS.between(LocalDate.now(), b2.getExpiryDate().toLocalDate());
                    
                    return Long.compare(days1, days2);
                })
                .collect(Collectors.toList());
        
        expiryTable.setItems(FXCollections.observableArrayList(sortedBatches));
        
        // Store report data for export
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (ProductBatch batch : sortedBatches) {
            Map<String, Object> data = new HashMap<>();
            data.put("batch", batch);
            data.put("product", batch.getProduct().getName());
            data.put("batchNumber", batch.getBatchNumber());
            data.put("remainingQty", batch.getRemainingQuantity());
            data.put("purchaseDate", batch.getPurchaseDate());
            data.put("expiryDate", batch.getExpiryDate());
            
            if (batch.getExpiryDate() != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate().toLocalDate());
                data.put("daysToExpiry", days);
                
                if (days < 0) {
                    data.put("status", "EXPIRED");
                } else if (days <= 7) {
                    data.put("status", "CRITICAL");
                } else if (days <= 30) {
                    data.put("status", "WARNING");
                } else {
                    data.put("status", "GOOD");
                }
            } else {
                data.put("daysToExpiry", null);
                data.put("status", "No Expiry");
            }
            
            BigDecimal valueAtRisk = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            data.put("valueAtRisk", valueAtRisk);
            
            exportData.add(data);
        }
        
        currentReportData = exportData;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, chartsBox, expiryTable);
    }
    
    
    
    
    
    
    private void initializeComboBoxes() {
      
        
        // Batch analysis filters
        batchProductCombo.getItems().add(null); // "All Products" option
        batchProductCombo.getItems().addAll(productService.getAllProducts());
        batchProductCombo.getSelectionModel().select(0); // Select "All Products"
        
        // Batch status options
        batchStatusCombo.getItems().addAll(
                "All Batches",
                "Active Batches", 
                "Expired Batches",
                "Low Stock Batches", 
                "Empty Batches",
                "Near Expiry Batches"
        );
        batchStatusCombo.getSelectionModel().select("All Batches");
        
        // Batch date ranges (for purchase dates)
        batchDateRangeCombo.getItems().addAll(
                "All Time",
                "Last 30 Days", 
                "Last 3 Months", 
                "Last 6 Months", 
                "This Year", 
                "Last Year"
        );
        batchDateRangeCombo.getSelectionModel().select("All Time");
        
        // Batch report types
        batchReportTypeCombo.getItems().addAll(
                "Batch Performance Analysis",
                "Batch Cost Analysis", 
                "Batch Expiry Report",
                "Batch Turnover Analysis",
                "Batch Profitability Analysis",
                "Batch Age Analysis"
        );
        batchReportTypeCombo.getSelectionModel().select("Batch Performance Analysis");
        
        
        
        
        
        
        


// Sales date ranges
        salesDateRangeCombo.getItems().addAll(
                "Today", 
                "Yesterday", 
                "This Week", 
                "Last Week", 
                "This Month", 
                "Last Month", 
                "This Year", 
                "Last Year", 
                "Custom Range"
        );
        salesDateRangeCombo.getSelectionModel().select("This Month");
        
        // Sales report types
        salesReportTypeCombo.getItems().addAll(
                "Daily Sales Summary", 
                "Sales by Product", 
                "Sales by Category", 
                "Sales Trends", 
                "Payment Method Analysis"
        );
        salesReportTypeCombo.getSelectionModel().select("Daily Sales Summary");
        
        // Inventory categories
        inventoryCategoryCombo.getItems().add(null); // "All Categories" option
        inventoryCategoryCombo.getItems().addAll(categoryService.getAllCategories());
        inventoryCategoryCombo.getSelectionModel().select(0); // Select "All Categories"
        
        // Inventory report types
        inventoryReportTypeCombo.getItems().addAll(
                "Stock Levels", 
                "Low Stock Items", 
                "Stock Valuation", 
                "Inventory Turnover", 
                "Category Distribution"
        );
        inventoryReportTypeCombo.getSelectionModel().select("Stock Levels");
        
        // Customer list
        customerCombo.getItems().add(null); // "All Customers" option
        customerCombo.getItems().addAll(customerService.getAllCustomers());
        customerCombo.getSelectionModel().select(0); // Select "All Customers"
        
        // Customer report types
        customerReportTypeCombo.getItems().addAll(
                "Purchase History", 
                "Credit Status", 
                "Top Customers", 
                "Customer Segmentation"
        );
        customerReportTypeCombo.getSelectionModel().select("Purchase History");
        
        // Financial date ranges
        financialDateRangeCombo.getItems().addAll(
                "This Month", 
                "Last Month", 
                "This Quarter", 
                "Last Quarter", 
                "This Year", 
                "Last Year", 
                "Custom Range"
        );
        financialDateRangeCombo.getSelectionModel().select("This Month");
        
        // Financial report types
        financialReportTypeCombo.getItems().addAll(
                "Sales Summary", 
                "Profit & Loss", 
                "Revenue vs Expenses", 
                "Margin Analysis"
        );
        financialReportTypeCombo.getSelectionModel().select("Sales Summary");
    }
    
    
    
    private void generateBatchTurnoverAnalysis(Product selectedProduct, String batchStatus, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, batchStatus, dateRange);
        
        // Get turnover rates
        Map<ProductBatch, Double> turnoverRates = reportingService.getBatchTurnoverRates(batches);
        
        // Create report title
        Label titleLabel = new Label("Batch Turnover Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Status: " + batchStatus + " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate turnover statistics
        double avgTurnoverRate = turnoverRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        
        double maxTurnoverRate = turnoverRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .max().orElse(0.0);
        
        double minTurnoverRate = turnoverRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .min().orElse(0.0);
        
        int fastMovingBatches = (int) turnoverRates.values().stream()
                .filter(rate -> rate > 4.0) // More than 4 turnovers per year
                .count();
        
        int slowMovingBatches = (int) turnoverRates.values().stream()
                .filter(rate -> rate < 1.0) // Less than 1 turnover per year
                .count();
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches: " + batches.size());
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label avgTurnoverLabel = new Label("Average Turnover Rate: " + String.format("%.2f x/year", avgTurnoverRate));
        avgTurnoverLabel.getStyleClass().add("summary-item");
        
        Label maxTurnoverLabel = new Label("Highest Turnover Rate: " + String.format("%.2f x/year", maxTurnoverRate));
        maxTurnoverLabel.getStyleClass().add("summary-item");
        
        Label minTurnoverLabel = new Label("Lowest Turnover Rate: " + String.format("%.2f x/year", minTurnoverRate));
        minTurnoverLabel.getStyleClass().add("summary-item");
        
        Label fastMovingLabel = new Label("Fast Moving Batches (>4x/year): " + fastMovingBatches);
        fastMovingLabel.getStyleClass().add("summary-item");
        fastMovingLabel.setStyle("-fx-text-fill: #2ecc71;");
        
        Label slowMovingLabel = new Label("Slow Moving Batches (<1x/year): " + slowMovingBatches);
        slowMovingLabel.getStyleClass().add("summary-item");
        slowMovingLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, avgTurnoverLabel, maxTurnoverLabel, 
                minTurnoverLabel, fastMovingLabel, slowMovingLabel);
        
        // Create turnover distribution chart
        CategoryAxis turnoverXAxis = new CategoryAxis();
        NumberAxis turnoverYAxis = new NumberAxis();
        turnoverXAxis.setLabel("Turnover Rate (times/year)");
        turnoverYAxis.setLabel("Number of Batches");
        
        BarChart<String, Number> turnoverChart = new BarChart<>(turnoverXAxis, turnoverYAxis);
        turnoverChart.setTitle("Batch Turnover Rate Distribution");
        
        // Group batches by turnover ranges
        Map<String, Integer> turnoverRanges = new LinkedHashMap<>();
        turnoverRanges.put("0-0.5", 0);
        turnoverRanges.put("0.5-1", 0);
        turnoverRanges.put("1-2", 0);
        turnoverRanges.put("2-4", 0);
        turnoverRanges.put("4-6", 0);
        turnoverRanges.put("6+", 0);
        
        for (Double rate : turnoverRates.values()) {
            if (rate < 0.5) {
                turnoverRanges.put("0-0.5", turnoverRanges.get("0-0.5") + 1);
            } else if (rate < 1.0) {
                turnoverRanges.put("0.5-1", turnoverRanges.get("0.5-1") + 1);
            } else if (rate < 2.0) {
                turnoverRanges.put("1-2", turnoverRanges.get("1-2") + 1);
            } else if (rate < 4.0) {
                turnoverRanges.put("2-4", turnoverRanges.get("2-4") + 1);
            } else if (rate < 6.0) {
                turnoverRanges.put("4-6", turnoverRanges.get("4-6") + 1);
            } else {
                turnoverRanges.put("6+", turnoverRanges.get("6+") + 1);
            }
        }
        
        XYChart.Series<String, Number> turnoverSeries = new XYChart.Series<>();
        turnoverSeries.setName("Batches");
        
        for (Map.Entry<String, Integer> entry : turnoverRanges.entrySet()) {
            turnoverSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        turnoverChart.getData().add(turnoverSeries);
        
        // Create velocity vs age scatter chart
        NumberAxis velocityXAxis = new NumberAxis();
        NumberAxis ageYAxis = new NumberAxis();
        velocityXAxis.setLabel("Sales Velocity (units/day)");
        ageYAxis.setLabel("Batch Age (days)");
        
        @SuppressWarnings({ "rawtypes", "unchecked" })
        LineChart<Number, Number> velocityAgeChart = new LineChart(velocityXAxis, ageYAxis);
        velocityAgeChart.setTitle("Sales Velocity vs Batch Age");
        velocityAgeChart.setCreateSymbols(true);
        velocityAgeChart.setLegendVisible(false);
        
        XYChart.Series<Number, Number> velocitySeries = new XYChart.Series<>();
        velocitySeries.setName("Batches");
        
        for (ProductBatch batch : batches) {
            // Calculate sales velocity
            BigDecimal quantitySold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            long daysSincePurchase = ChronoUnit.DAYS.between(
                    batch.getPurchaseDate().toLocalDate(), 
                    LocalDate.now()
            );
            
            double salesVelocity = 0.0;
            if (daysSincePurchase > 0) {
                salesVelocity = quantitySold.divide(new BigDecimal(daysSincePurchase), 4, ROUNDING_MODE).doubleValue();
            }
            
            velocitySeries.getData().add(new XYChart.Data<>(salesVelocity, daysSincePurchase));
        }
        
        velocityAgeChart.getData().add(velocitySeries);
        
        // Create charts container
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(turnoverChart, velocityAgeChart);
        
        // Create turnover analysis table
        TableView<Map.Entry<ProductBatch, Double>> turnoverTable = new TableView<>();
        
        TableColumn<Map.Entry<ProductBatch, Double>, String> productColumn = 
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getProduct().getName()));
        
        TableColumn<Map.Entry<ProductBatch, Double>, String> batchColumn = 
                new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getKey().getBatchNumber()));
        
        TableColumn<Map.Entry<ProductBatch, Double>, BigDecimal> initialQtyColumn = 
                new TableColumn<>("Initial Qty");
        initialQtyColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getKey().getInitialQuantity()));
        
        TableColumn<Map.Entry<ProductBatch, Double>, BigDecimal> remainingQtyColumn = 
                new TableColumn<>("Remaining Qty");
        remainingQtyColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(data.getValue().getKey().getRemainingQuantity()));
        
        TableColumn<Map.Entry<ProductBatch, Double>, BigDecimal> soldQtyColumn = 
                new TableColumn<>("Sold Qty");
        soldQtyColumn.setCellValueFactory(data -> {
            ProductBatch batch = data.getValue().getKey();
            BigDecimal sold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            return new SimpleObjectProperty<>(sold);
        });
        
        TableColumn<Map.Entry<ProductBatch, Double>, Long> ageColumn = 
                new TableColumn<>("Age (Days)");
        ageColumn.setCellValueFactory(data -> {
            ProductBatch batch = data.getValue().getKey();
            long age = ChronoUnit.DAYS.between(
                    batch.getPurchaseDate().toLocalDate(), 
                    LocalDate.now()
            );
            return new SimpleObjectProperty<>(age);
        });
        
        TableColumn<Map.Entry<ProductBatch, Double>, String> velocityColumn = 
                new TableColumn<>("Sales Velocity");
        velocityColumn.setCellValueFactory(data -> {
            ProductBatch batch = data.getValue().getKey();
            BigDecimal sold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            long age = ChronoUnit.DAYS.between(
                    batch.getPurchaseDate().toLocalDate(), 
                    LocalDate.now()
            );
            
            double velocity = 0.0;
            if (age > 0) {
                velocity = sold.divide(new BigDecimal(age), 4, ROUNDING_MODE).doubleValue();
            }
            
            return new SimpleStringProperty(String.format("%.3f units/day", velocity));
        });
        
        TableColumn<Map.Entry<ProductBatch, Double>, String> turnoverRateColumn = 
                new TableColumn<>("Turnover Rate");
        turnoverRateColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%.2f x/year", data.getValue().getValue())));
        
        TableColumn<Map.Entry<ProductBatch, Double>, String> classificationColumn = 
                new TableColumn<>("Classification");
        classificationColumn.setCellValueFactory(data -> {
            double rate = data.getValue().getValue();
            String classification;
            
            if (rate > 4.0) {
                classification = "Fast Moving";
            } else if (rate > 2.0) {
                classification = "Medium Moving";
            } else if (rate > 1.0) {
                classification = "Slow Moving";
            } else {
                classification = "Very Slow";
            }
            
            return new SimpleStringProperty(classification);
        });
        
        // Color code classification column
        classificationColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Double>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    switch (item) {
                        case "Fast Moving":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "Medium Moving":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "Slow Moving":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Very Slow":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Format quantity columns
        initialQtyColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Double>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        remainingQtyColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Double>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        soldQtyColumn.setCellFactory(column -> new TableCell<Map.Entry<ProductBatch, Double>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(formatDecimal(item));
                }
            }
        });
        
        turnoverTable.getColumns().addAll(productColumn, batchColumn, initialQtyColumn, 
                remainingQtyColumn, soldQtyColumn, ageColumn, velocityColumn, 
                turnoverRateColumn, classificationColumn);
        
        // Sort by turnover rate (highest first)
        List<Map.Entry<ProductBatch, Double>> sortedTurnover = turnoverRates.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        
        turnoverTable.setItems(FXCollections.observableArrayList(sortedTurnover));
        
        // Store report data for export
        List<Map<String, Object>> exportData = new ArrayList<>();
        for (Map.Entry<ProductBatch, Double> entry : sortedTurnover) {
            ProductBatch batch = entry.getKey();
            Double turnoverRate = entry.getValue();
            
            Map<String, Object> data = new HashMap<>();
            data.put("product", batch.getProduct().getName());
            data.put("batchNumber", batch.getBatchNumber());
            data.put("initialQuantity", batch.getInitialQuantity());
            data.put("remainingQuantity", batch.getRemainingQuantity());
            data.put("soldQuantity", batch.getInitialQuantity().subtract(batch.getRemainingQuantity()));
            
            long age = ChronoUnit.DAYS.between(batch.getPurchaseDate().toLocalDate(), LocalDate.now());
            data.put("ageInDays", age);
            
            double velocity = 0.0;
            if (age > 0) {
                BigDecimal sold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
                velocity = sold.divide(new BigDecimal(age), 4, ROUNDING_MODE).doubleValue();
            }
            data.put("salesVelocity", velocity);
            data.put("turnoverRate", turnoverRate);
            
            String classification;
            if (turnoverRate > 4.0) {
                classification = "Fast Moving";
            } else if (turnoverRate > 2.0) {
                classification = "Medium Moving";
            } else if (turnoverRate > 1.0) {
                classification = "Slow Moving";
            } else {
                classification = "Very Slow";
            }
            data.put("classification", classification);
            
            exportData.add(data);
        }
        
        currentReportData = exportData;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, chartsBox, turnoverTable);
    }
    
    
    
    
    
  
    private void generateBatchCostAnalysis(Product selectedProduct, String batchStatus, String dateRange) {
        // Get filtered batches
        List<ProductBatch> batches = reportingService.getFilteredBatches(selectedProduct, batchStatus, dateRange);
        
        // Create report title
        Label titleLabel = new Label("Batch Cost Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle
        String subtitle = "Product: " + (selectedProduct != null ? selectedProduct.getName() : "All Products") +
                " | Status: " + batchStatus + " | Date Range: " + dateRange;
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate cost metrics
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalRealizedValue = BigDecimal.ZERO;
        BigDecimal minCost = null;
        BigDecimal maxCost = null;
        
        List<Map<String, Object>> costData = new ArrayList<>();
        
        for (ProductBatch batch : batches) {
            BigDecimal batchInvestment = batch.getBuyingPrice().multiply(batch.getInitialQuantity());
            BigDecimal batchCurrentValue = batch.getBuyingPrice().multiply(batch.getRemainingQuantity());
            BigDecimal quantitySold = batch.getInitialQuantity().subtract(batch.getRemainingQuantity());
            BigDecimal realizedValue = batch.getBuyingPrice().multiply(quantitySold);
            
            totalInvestment = totalInvestment.add(batchInvestment);
            totalCurrentValue = totalCurrentValue.add(batchCurrentValue);
            totalRealizedValue = totalRealizedValue.add(realizedValue);
            
            // Track min/max costs
            if (minCost == null || batch.getBuyingPrice().compareTo(minCost) < 0) {
                minCost = batch.getBuyingPrice();
            }
            if (maxCost == null || batch.getBuyingPrice().compareTo(maxCost) > 0) {
                maxCost = batch.getBuyingPrice();
            }
            
            // Prepare data for table
            Map<String, Object> data = new HashMap<>();
            data.put("batch", batch);
            data.put("investment", batchInvestment);
            data.put("currentValue", batchCurrentValue);
            data.put("realizedValue", realizedValue);
            data.put("quantitySold", quantitySold);
            
            costData.add(data);
        }
        
        BigDecimal totalUnrealizedValue = totalInvestment.subtract(totalRealizedValue);
        
        // Calculate average cost
        BigDecimal avgCost = BigDecimal.ZERO;
        if (!batches.isEmpty()) {
            BigDecimal totalCost = batches.stream()
                    .map(ProductBatch::getBuyingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgCost = totalCost.divide(new BigDecimal(batches.size()), DECIMAL_SCALE, ROUNDING_MODE);
        }
        
        // Create summary labels
        Label totalBatchesLabel = new Label("Total Batches: " + batches.size());
        totalBatchesLabel.getStyleClass().add("summary-item");
        
        Label totalInvestmentLabel = new Label("Total Investment: LKR " + formatDecimal(totalInvestment));
        totalInvestmentLabel.getStyleClass().add("summary-item");
        
        Label currentValueLabel = new Label("Current Inventory Value: LKR " + formatDecimal(totalCurrentValue));
        currentValueLabel.getStyleClass().add("summary-item");
        
        Label realizedValueLabel = new Label("Realized Value: LKR " + formatDecimal(totalRealizedValue));
        realizedValueLabel.getStyleClass().add("summary-item");
        
        Label unrealizedValueLabel = new Label("Unrealized Value: LKR " + formatDecimal(totalUnrealizedValue));
        unrealizedValueLabel.getStyleClass().add("summary-item");
        
        Label avgCostLabel = new Label("Average Cost per Unit: LKR " + formatDecimal(avgCost));
        avgCostLabel.getStyleClass().add("summary-item");
        
        Label costRangeLabel = new Label("Cost Range: LKR " + formatDecimal(minCost) + 
                " - LKR " + formatDecimal(maxCost));
        costRangeLabel.getStyleClass().add("summary-item");
        
        summaryBox.getChildren().addAll(
                totalBatchesLabel, totalInvestmentLabel, currentValueLabel, 
                realizedValueLabel, unrealizedValueLabel, avgCostLabel, costRangeLabel);
        
        // Create cost distribution chart
        CategoryAxis costXAxis = new CategoryAxis();
        NumberAxis costYAxis = new NumberAxis();
        costXAxis.setLabel("Cost Range (LKR)");
        costYAxis.setLabel("Number of Batches");
        
        BarChart<String, Number> costChart = new BarChart<>(costXAxis, costYAxis);
        costChart.setTitle("Batch Cost Distribution");
        
        // Group batches by cost ranges
        Map<String, Integer> costRanges = new TreeMap<>();
        if (minCost != null && maxCost != null) {
            BigDecimal range = maxCost.subtract(minCost);
            BigDecimal increment = range.divide(new BigDecimal("5"), DECIMAL_SCALE, ROUNDING_MODE);
            
            if (increment.compareTo(BigDecimal.ZERO) == 0) {
                // All batches have the same cost
                costRanges.put(formatDecimal(minCost), batches.size());
            } else {
                for (int i = 0; i < 5; i++) {
                    BigDecimal rangeStart = minCost.add(increment.multiply(new BigDecimal(i)));
                    BigDecimal rangeEnd = minCost.add(increment.multiply(new BigDecimal(i + 1)));
                    String rangeLabel = formatDecimal(rangeStart) + "-" + formatDecimal(rangeEnd);
                    costRanges.put(rangeLabel, 0);
                }
                
                // Count batches in each range
                for (ProductBatch batch : batches) {
                    BigDecimal cost = batch.getBuyingPrice();
                    for (int i = 0; i < 5; i++) {
                        BigDecimal rangeStart = minCost.add(increment.multiply(new BigDecimal(i)));
                        BigDecimal rangeEnd = minCost.add(increment.multiply(new BigDecimal(i + 1)));
                        
                        if ((i == 4 && cost.compareTo(rangeEnd) <= 0) || 
                            (cost.compareTo(rangeStart) >= 0 && cost.compareTo(rangeEnd) < 0)) {
                            String rangeLabel = formatDecimal(rangeStart) + "-" + formatDecimal(rangeEnd);
                            costRanges.put(rangeLabel, costRanges.get(rangeLabel) + 1);
                            break;
                        }
                    }
                }
            }
        }
        
        XYChart.Series<String, Number> costSeries = new XYChart.Series<>();
        costSeries.setName("Batches");
        
        for (Map.Entry<String, Integer> entry : costRanges.entrySet()) {
            if (entry.getValue() > 0) {
                costSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        
        costChart.getData().add(costSeries);
        
        // Create pie chart for value distribution
        PieChart valueChart = new PieChart();
        valueChart.setTitle("Investment Value Distribution");
        
        valueChart.getData().add(new PieChart.Data(
                "Realized (LKR " + formatDecimal(totalRealizedValue) + ")", 
                totalRealizedValue.doubleValue()));
        valueChart.getData().add(new PieChart.Data(
                "Unrealized (LKR " + formatDecimal(totalUnrealizedValue) + ")", 
                totalUnrealizedValue.doubleValue()));
        
        // Create HBox to hold charts side by side
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(costChart, valueChart);
        
        // Create cost analysis table
        TableView<Map<String, Object>> costTable = new TableView<>();
        
        TableColumn<Map<String, Object>, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            return new SimpleStringProperty(batch.getProduct().getName());
        });
        
        TableColumn<Map<String, Object>, String> batchColumn = new TableColumn<>("Batch #");
        batchColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            return new SimpleStringProperty(batch.getBatchNumber());
        });
        
        TableColumn<Map<String, Object>, BigDecimal> unitCostColumn = new TableColumn<>("Unit Cost");
        unitCostColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            return new SimpleObjectProperty<>(batch.getBuyingPrice());
        });
        
        TableColumn<Map<String, Object>, BigDecimal> initialQtyColumn = new TableColumn<>("Initial Qty");
        initialQtyColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            return new SimpleObjectProperty<>(batch.getInitialQuantity());
        });
        
        TableColumn<Map<String, Object>, BigDecimal> remainingQtyColumn = new TableColumn<>("Remaining Qty");
        remainingQtyColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            return new SimpleObjectProperty<>(batch.getRemainingQuantity());
        });
        
        TableColumn<Map<String, Object>, BigDecimal> investmentColumn = new TableColumn<>("Total Investment");
        investmentColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((BigDecimal) data.getValue().get("investment")));
        
        TableColumn<Map<String, Object>, BigDecimal> currentValueColumn = new TableColumn<>("Current Value");
        currentValueColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((BigDecimal) data.getValue().get("currentValue")));
        
        TableColumn<Map<String, Object>, BigDecimal> realizedColumn = new TableColumn<>("Realized Value");
        realizedColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((BigDecimal) data.getValue().get("realizedValue")));
        
        TableColumn<Map<String, Object>, String> utilizationColumn = new TableColumn<>("Utilization %");
        utilizationColumn.setCellValueFactory(data -> {
            ProductBatch batch = (ProductBatch) data.getValue().get("batch");
            BigDecimal utilization = batch.getInitialQuantity().subtract(batch.getRemainingQuantity())
                    .divide(batch.getInitialQuantity(), 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
            return new SimpleStringProperty(formatDecimal(utilization) + "%");
        });
        
        // Format currency columns
        unitCostColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        investmentColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        currentValueColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        realizedColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText("LKR " + formatDecimal(item));
                }
            }
        });
        
        costTable.getColumns().addAll(productColumn, batchColumn, unitCostColumn, 
                initialQtyColumn, remainingQtyColumn, investmentColumn, 
                currentValueColumn, realizedColumn, utilizationColumn);
        
        // Sort by total investment (descending)
        costData.sort((d1, d2) -> {
            BigDecimal inv1 = (BigDecimal) d1.get("investment");
            BigDecimal inv2 = (BigDecimal) d2.get("investment");
            return inv2.compareTo(inv1);
        });
        
        costTable.setItems(FXCollections.observableArrayList(costData));
        
        // Store report data for export
        currentReportData = costData;
        
        // Add all components to the container
        batchReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, chartsBox, costTable);
    }
    
    
    
    
    
    
    
    
  private void setDefaultDates() {
        // Set default dates to current month
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        
        salesStartDatePicker.setValue(firstDayOfMonth);
        salesEndDatePicker.setValue(today);
        
        financialStartDatePicker.setValue(firstDayOfMonth);
        financialEndDatePicker.setValue(today);
        
        // Initially disable date pickers (will be enabled only for Custom Range)
        salesStartDatePicker.setDisable(true);
        salesEndDatePicker.setDisable(true);
        financialStartDatePicker.setDisable(true);
        financialEndDatePicker.setDisable(true);
    }
    
    private void setupDateRangeListeners() {
        // Sales date range listener
        salesDateRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDateRange(newVal, salesStartDatePicker, salesEndDatePicker);
            }
        });
        
        // Financial date range listener
        financialDateRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDateRange(newVal, financialStartDatePicker, financialEndDatePicker);
            }
        });
    }
    
    private void updateDateRange(String range, DatePicker startPicker, DatePicker endPicker) {
        LocalDate today = LocalDate.now();
        
        switch (range) {
            case "Today":
                startPicker.setValue(today);
                endPicker.setValue(today);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Yesterday":
                LocalDate yesterday = today.minusDays(1);
                startPicker.setValue(yesterday);
                endPicker.setValue(yesterday);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "This Week":
                LocalDate firstDayOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                startPicker.setValue(firstDayOfWeek);
                endPicker.setValue(today);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Last Week":
                LocalDate lastWeekStart = today.minusDays(today.getDayOfWeek().getValue() + 6);
                LocalDate lastWeekEnd = today.minusDays(today.getDayOfWeek().getValue());
                startPicker.setValue(lastWeekStart);
                endPicker.setValue(lastWeekEnd);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "This Month":
                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                startPicker.setValue(firstDayOfMonth);
                endPicker.setValue(today);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Last Month":
                LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
                LocalDate lastDayOfLastMonth = today.withDayOfMonth(1).minusDays(1);
                startPicker.setValue(firstDayOfLastMonth);
                endPicker.setValue(lastDayOfLastMonth);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "This Year":
                LocalDate firstDayOfYear = today.withDayOfYear(1);
                startPicker.setValue(firstDayOfYear);
                endPicker.setValue(today);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Last Year":
                LocalDate firstDayOfLastYear = today.minusYears(1).withDayOfYear(1);
                LocalDate lastDayOfLastYear = today.withDayOfYear(1).minusDays(1);
                startPicker.setValue(firstDayOfLastYear);
                endPicker.setValue(lastDayOfLastYear);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "This Quarter":
                int quarter = (today.getMonthValue() - 1) / 3;
                LocalDate firstDayOfQuarter = LocalDate.of(today.getYear(), quarter * 3 + 1, 1);
                startPicker.setValue(firstDayOfQuarter);
                endPicker.setValue(today);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Last Quarter":
                LocalDate firstDayOfLastQuarter = today.minusMonths(3).withDayOfMonth(1);
                LocalDate lastDayOfLastQuarter = today.withDayOfMonth(1).minusDays(1);
                startPicker.setValue(firstDayOfLastQuarter);
                endPicker.setValue(lastDayOfLastQuarter);
                startPicker.setDisable(true);
                endPicker.setDisable(true);
                break;
                
            case "Custom Range":
                startPicker.setDisable(false);
                endPicker.setDisable(false);
                break;
        }
    }
    
    @FXML
    private void handleGenerateSalesReport(ActionEvent event) {
        // Clear previous report
        salesReportContainer.getChildren().clear();
        
        // Get date range
        LocalDate startDate = salesStartDatePicker.getValue();
        LocalDate endDate = salesEndDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            AlertHelper.showErrorAlert("Invalid Date Range", "Please select valid dates", 
                    "Both start and end dates must be selected.");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            AlertHelper.showErrorAlert("Invalid Date Range", "Start date is after end date", 
                    "Please ensure the start date is before or equal to the end date.");
            return;
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // Get selected report type
        String reportType = salesReportTypeCombo.getValue();
        currentReportType = reportType;
        
        switch (reportType) {
            case "Daily Sales Summary":
                generateDailySalesSummary(startDateTime, endDateTime);
                break;
                
            case "Sales by Product":
                generateSalesByProduct(startDateTime, endDateTime);
                break;
                
            case "Sales by Category":
                generateSalesByCategory(startDateTime, endDateTime);
                break;
                
            case "Sales Trends":
                generateSalesTrends(startDateTime, endDateTime);
                break;
                
            case "Payment Method Analysis":
                generatePaymentMethodAnalysis(startDateTime, endDateTime);
                break;
        }
        
        // Enable export button
        exportSalesReportBtn.setDisable(false);
    }
    
    private void generateDailySalesSummary(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get sales data for the date range
        List<Invoice> invoices = invoiceService.getInvoicesForDateRange(startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Daily Sales Summary");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle with date range
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
                " to " + endDateTime.format(dateFormatter));
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        // Calculate totals
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal creditSales = BigDecimal.ZERO;
        int totalTransactions = invoices.size();
        
        for (Invoice invoice : invoices) {
            totalSales = totalSales.add(invoice.getFinalAmount());
            
            if ("CASH".equals(invoice.getPaymentMethod())) {
                cashSales = cashSales.add(invoice.getFinalAmount());
            } else if ("CREDIT".equals(invoice.getPaymentMethod())) {
                creditSales = creditSales.add(invoice.getFinalAmount());
            }
        }
        
        // Create summary labels
        Label totalSalesLabel = new Label("Total Sales: LKR " + totalSales);
        totalSalesLabel.getStyleClass().add("summary-item");
        
        Label cashSalesLabel = new Label("Cash Sales: LKR " + cashSales + 
                " (" + getPercentage(cashSales, totalSales) + "%)");
        cashSalesLabel.getStyleClass().add("summary-item");
        
        Label creditSalesLabel = new Label("Credit Sales: LKR " + creditSales + 
                " (" + getPercentage(creditSales, totalSales) + "%)");
        creditSalesLabel.getStyleClass().add("summary-item");
        
        Label transactionsLabel = new Label("Total Transactions: " + totalTransactions);
        transactionsLabel.getStyleClass().add("summary-item");
        
        BigDecimal avgTransaction = totalTransactions > 0 
                ? totalSales.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
        
        Label avgTransactionLabel = new Label("Average Transaction: LKR " + avgTransaction);
        avgTransactionLabel.getStyleClass().add("summary-item");
        
        // Add summary items to box
        summaryBox.getChildren().addAll(
                totalSalesLabel, cashSalesLabel, creditSalesLabel, 
                transactionsLabel, avgTransactionLabel);
        
        // Create daily sales chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Sales (LKR)");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Daily Sales");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");
        
        // Group sales by date
        Map<LocalDate, BigDecimal> salesByDate = reportingService.getSalesByDate(startDateTime, endDateTime);
        
        for (Map.Entry<LocalDate, BigDecimal> entry : salesByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(dateFormatter), 
                    entry.getValue()));
        }
        
        barChart.getData().add(series);
        
        // Create table for detailed view
        TableView<Invoice> invoiceTable = new TableView<>();
        
        TableColumn<Invoice, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
        
        TableColumn<Invoice, String> invoiceColumn = new TableColumn<>("Invoice #");
        invoiceColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getInvoiceNumber()));
        
        TableColumn<Invoice, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCustomer() != null ? 
                        cellData.getValue().getCustomer().getName() : "Walk-in Customer"));
        
        TableColumn<Invoice, String> typeColumn = new TableColumn<>("Customer Type");
        typeColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCustomerType()));
        
        TableColumn<Invoice, String> paymentMethodColumn = new TableColumn<>("Payment Method");
        paymentMethodColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        
        TableColumn<Invoice, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getFinalAmount()));
        
        // Format amount column
        amountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
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
        
        invoiceTable.getColumns().addAll(dateColumn, invoiceColumn, customerColumn, 
                typeColumn, paymentMethodColumn, amountColumn);
        
        invoiceTable.setItems(FXCollections.observableArrayList(invoices));
        
        // Store report data for export
        currentReportData = invoices;
        
        // Add all components to the container
        salesReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, barChart, invoiceTable);
    }
    
    private void generateSalesByProduct(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get sales data grouped by product
        Map<Product, Map<String, Object>> productSales = 
                reportingService.getProductSales(startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Sales by Product");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle with date range
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
                " to " + endDateTime.format(dateFormatter));
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create PieChart for top products by sales value
        PieChart topProductsChart = new PieChart();
        topProductsChart.setTitle("Top 5 Products by Sales Value");
        
        // Add top 5 products to pie chart
        List<Map.Entry<Product, Map<String, Object>>> sortedProducts = 
                reportingService.getTopProductsBySalesValue(productSales, 5);
        
        for (Map.Entry<Product, Map<String, Object>> entry : sortedProducts) {
            Product product = entry.getKey();
            BigDecimal salesValue = (BigDecimal) entry.getValue().get("salesValue");
            
            topProductsChart.getData().add(
                    new PieChart.Data(product.getName() + " (LKR " + salesValue + ")", 
                            salesValue.doubleValue()));
        }
        
        // Create table for detailed product sales
        TableView<Map.Entry<Product, Map<String, Object>>> productTable = new TableView<>();
        
        TableColumn<Map.Entry<Product, Map<String, Object>>, String> productColumn = 
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getKey().getName()));
        
        TableColumn<Map.Entry<Product, Map<String, Object>>, String> categoryColumn = 
                new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getKey().getCategory() != null ? 
                        cellData.getValue().getKey().getCategory().getName() : "Uncategorized"));
        
        TableColumn<Map.Entry<Product, Map<String, Object>>, Number> quantityColumn = 
                new TableColumn<>("Quantity Sold");
        quantityColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>((Number)cellData.getValue().getValue().get("quantity")));
        
        TableColumn<Map.Entry<Product, Map<String, Object>>, BigDecimal> valueColumn = 
                new TableColumn<>("Sales Value");
        valueColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>((BigDecimal)cellData.getValue().getValue().get("salesValue")));
        
        // Format value column
        valueColumn.setCellFactory(column -> new TableCell<Map.Entry<Product, Map<String, Object>>, BigDecimal>() {
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
        
        productTable.getColumns().addAll(productColumn, categoryColumn, quantityColumn, valueColumn);
        
        // Sort products by sales value (descending) and add to table
        List<Map.Entry<Product, Map<String, Object>>> allProductsSorted = 
                reportingService.getAllProductsSortedBySalesValue(productSales);
        
        productTable.setItems(FXCollections.observableArrayList(allProductsSorted));
        
        // Store report data for export
        currentReportData = allProductsSorted;
        
        // Add all components to the container
        salesReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, topProductsChart, productTable);
    }
    
    private void generateSalesByCategory(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get sales data grouped by category
        Map<Category, BigDecimal> categorySales = 
                reportingService.getCategorySales(startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Sales by Category");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle with date range
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
                " to " + endDateTime.format(dateFormatter));
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create PieChart for category distribution
        PieChart categoryChart = new PieChart();
        categoryChart.setTitle("Sales Distribution by Category");
        
        // Add categories to pie chart
        for (Map.Entry<Category, BigDecimal> entry : categorySales.entrySet()) {
            String categoryName = entry.getKey() != null ? entry.getKey().getName() : "Uncategorized";
            BigDecimal salesValue = entry.getValue();
            
            categoryChart.getData().add(
                    new PieChart.Data(categoryName + " (LKR " + salesValue + ")", 
                            salesValue.doubleValue()));
        }
        
        // Create Bar Chart for category comparison
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Sales (LKR)");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Sales by Category");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales Value");
        
        // Add categories to bar chart
        for (Map.Entry<Category, BigDecimal> entry : categorySales.entrySet()) {
            String categoryName = entry.getKey() != null ? entry.getKey().getName() : "Uncategorized";
            series.getData().add(new XYChart.Data<>(categoryName, entry.getValue()));
        }
        
        barChart.getData().add(series);
        
        // Create table for detailed category sales
        TableView<Map.Entry<Category, BigDecimal>> categoryTable = new TableView<>();
        
        TableColumn<Map.Entry<Category, BigDecimal>, String> categoryColumn = 
                new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getKey() != null ? 
                        cellData.getValue().getKey().getName() : "Uncategorized"));
        
        TableColumn<Map.Entry<Category, BigDecimal>, BigDecimal> salesColumn = 
                new TableColumn<>("Sales Value");
        salesColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getValue()));
        
        // Format sales column
        salesColumn.setCellFactory(column -> new TableCell<Map.Entry<Category, BigDecimal>, BigDecimal>() {
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
        
        TableColumn<Map.Entry<Category, BigDecimal>, String> percentageColumn = 
                new TableColumn<>("Percentage");
        percentageColumn.setCellValueFactory(cellData -> {
            BigDecimal totalSales = categorySales.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal percentage = cellData.getValue().getValue()
                    .multiply(new BigDecimal("100"))
                    .divide(totalSales, 2, RoundingMode.HALF_UP);
            
            return new SimpleStringProperty(percentage + "%");
        });
        
        categoryTable.getColumns().addAll(categoryColumn, salesColumn, percentageColumn);
        
        // Sort categories by sales value (descending) and add to table
        List<Map.Entry<Category, BigDecimal>> sortedCategories = 
                reportingService.getCategoriesSortedBySalesValue(categorySales);
        
        categoryTable.setItems(FXCollections.observableArrayList(sortedCategories));
        
        // Store report data for export
        currentReportData = sortedCategories;
        
        // Add all components to the container
        salesReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, categoryChart, barChart, categoryTable);
    }
    
    private void generateSalesTrends(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get daily sales data for trend analysis
        Map<LocalDate, BigDecimal> dailySales = 
                reportingService.getSalesByDate(startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Sales Trends");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle with date range
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
                " to " + endDateTime.format(dateFormatter));
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Create Line Chart for sales trend
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Sales (LKR)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Daily Sales Trend");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");
        
        // Add daily sales to line chart
        for (Map.Entry<LocalDate, BigDecimal> entry : dailySales.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(dateFormatter), 
                    entry.getValue()));
        }
        
        lineChart.getData().add(series);
        
        // Create table for detailed daily sales
        TableView<Map.Entry<LocalDate, BigDecimal>> salesTable = new TableView<>();
        
        TableColumn<Map.Entry<LocalDate, BigDecimal>, String> dateColumn = 
                new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getKey().format(dateFormatter)));
        
        TableColumn<Map.Entry<LocalDate, BigDecimal>, BigDecimal> salesColumn = 
                new TableColumn<>("Sales Value");
        salesColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getValue()));
        
        // Format sales column
        salesColumn.setCellFactory(column -> new TableCell<Map.Entry<LocalDate, BigDecimal>, BigDecimal>() {
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
        
        salesTable.getColumns().addAll(dateColumn, salesColumn);
        
        // Sort dates chronologically and add to table
        List<Map.Entry<LocalDate, BigDecimal>> sortedDates = 
                reportingService.getDatesChronologically(dailySales);
        
        salesTable.setItems(FXCollections.observableArrayList(sortedDates));
        
        // Store report data for export
        currentReportData = sortedDates;
        
        // Add all components to the container
        salesReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, lineChart, salesTable);
    }
    
    private void generatePaymentMethodAnalysis(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Get payment method distribution
        Map<String, BigDecimal> paymentMethodSales = 
                reportingService.getSalesByPaymentMethod(startDateTime, endDateTime);
        
        // Create report title
        Label titleLabel = new Label("Payment Method Analysis");
        titleLabel.getStyleClass().add("report-title");
        
        // Create report subtitle with date range
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
                " to " + endDateTime.format(dateFormatter));
        subtitleLabel.getStyleClass().add("report-subtitle");
        
        // Calculate total sales
        BigDecimal totalSales = paymentMethodSales.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create summary box
        VBox summaryBox = new VBox(5);
        summaryBox.getStyleClass().add("summary-box");
        
        Label totalSalesLabel = new Label("Total Sales: LKR " + totalSales);
        totalSalesLabel.getStyleClass().add("summary-item");
        
        summaryBox.getChildren().add(totalSalesLabel);
        
        // Add payment method breakdown
        for (Map.Entry<String, BigDecimal> entry : paymentMethodSales.entrySet()) {
            String method = entry.getKey();
            BigDecimal value = entry.getValue();
            BigDecimal percentage = value.multiply(new BigDecimal("100"))
                    .divide(totalSales, 2, RoundingMode.HALF_UP);
            
            Label methodLabel = new Label(method + ": LKR " + value + 
                    " (" + percentage + "%)");
            methodLabel.getStyleClass().add("summary-item");
            
            summaryBox.getChildren().add(methodLabel);
        }
        
        // Create PieChart for payment method distribution
        PieChart paymentChart = new PieChart();
        paymentChart.setTitle("Payment Method Distribution");
        
        // Add payment methods to pie chart
        for (Map.Entry<String, BigDecimal> entry : paymentMethodSales.entrySet()) {
            paymentChart.getData().add(
                    new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }
        
        // Store report data for export
        currentReportData = paymentMethodSales;
        
        // Add all components to the container
        salesReportContainer.getChildren().addAll(
                titleLabel, subtitleLabel, summaryBox, paymentChart);
    }
    
    // Helper method to calculate percentage
    private String getPercentage(BigDecimal part, BigDecimal whole) {
        if (whole.equals(BigDecimal.ZERO)) {
            return "0";
        }
        
        return part.multiply(new BigDecimal("100"))
                .divide(whole, 2, RoundingMode.HALF_UP)
                .toString();
    }
    
    @FXML
    private void handleExportSalesReport(ActionEvent event) {
        exportReport(currentReportData, "sales_report_" + currentReportType.toLowerCase().replace(" ", "_"));
    }
    
  // Replace the existing exportReport method in ReportingController with this enhanced version

private void exportReport(Object reportData, String fileNamePrefix) {
    // Configure file chooser
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Report");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
    );
    
    // Set default file name
    DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    String defaultFileName = fileNamePrefix + "_" + LocalDateTime.now().format(fileFormatter);
    fileChooser.setInitialFileName(defaultFileName);
    
    // Show save dialog
    File file = fileChooser.showSaveDialog(reportsPane.getScene().getWindow());
    
    if (file != null) {
        String fileName = file.getAbsolutePath();
        
        try {
            if (fileName.endsWith(".xlsx")) {
                // Based on the report type, use appropriate export method
                if (currentReportType.equals("Daily Sales Summary") || 
                    currentReportType.equals("Sales Summary")) {
                    if (reportData instanceof Map) {
                        exportUtil.exportSalesSummaryReport((Map<String, Object>) reportData, fileName);
                    } else {
                        exportUtil.exportToExcel(reportData, fileName);
                    }
                } else if (currentReportType.equals("Stock Levels")) {
                    if (reportData instanceof List && !((List<?>)reportData).isEmpty() && 
                            ((List<?>)reportData).get(0) instanceof Product) {
                        exportUtil.exportInventoryStockReport((List<Product>) reportData, fileName);
                    } else {
                        exportUtil.exportToExcel(reportData, fileName);
                    }
                } else if (currentReportType.equals("Credit Status")) {
                    if (reportData instanceof List && !((List<?>)reportData).isEmpty() && 
                            ((List<?>)reportData).get(0) instanceof Customer) {
                        exportUtil.exportCustomerCreditReport((List<Customer>) reportData, fileName);
                    } else {
                        exportUtil.exportToExcel(reportData, fileName);
                    }
                } else if (currentReportType.equals("Profit & Loss")) {
                    if (reportData instanceof List && !((List<?>)reportData).isEmpty() && 
                            ((List<?>)reportData).get(0) instanceof Map) {
                        exportUtil.exportProfitLossReport((List<Map<String, Object>>) reportData, fileName);
                    } else {
                        exportUtil.exportToExcel(reportData, fileName);
                    }
                } else if (currentReportType.equals("Margin Analysis")) {
                    if (reportData instanceof List && !((List<?>)reportData).isEmpty() && 
                            ((List<?>)reportData).get(0) instanceof Map) {
                        exportUtil.exportProfitMarginReport((List<Map<String, Object>>) reportData, fileName);
                    } else {
                        exportUtil.exportToExcel(reportData, fileName);
                    }
                } else {
                    // Default export
                    exportUtil.exportToExcel(reportData, fileName);
                }
            } else if (fileName.endsWith(".csv")) {
                exportUtil.exportToCSV(reportData, fileName);
            }
            
            AlertHelper.showInformationAlert("Export Successful", "Report Exported", 
                    "The report has been exported successfully to " + fileName);
            
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Export Error", "Failed to export report", 
                    "An error occurred while exporting the report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
    
    @FXML
private void handleGenerateInventoryReport(ActionEvent event) {
    // Clear previous report
    inventoryReportContainer.getChildren().clear();
    
    // Get category filter
    Category selectedCategory = inventoryCategoryCombo.getValue();
    
    // Get selected report type
    String reportType = inventoryReportTypeCombo.getValue();
    currentReportType = reportType;
    
    switch (reportType) {
        case "Stock Levels":
            generateStockLevelsReport(selectedCategory);
            break;
            
        case "Low Stock Items":
            generateLowStockReport(selectedCategory);
            break;
            
        case "Stock Valuation":
            generateStockValuationReport(selectedCategory);
            break;
            
        case "Inventory Turnover":
            generateInventoryTurnoverReport(selectedCategory);
            break;
            
        case "Category Distribution":
            generateCategoryDistributionReport();
            break;
    }
    
    // Enable export button
    exportInventoryReportBtn.setDisable(false);
}

private void generateStockLevelsReport(Category selectedCategory) {
    // Get inventory data based on category filter
    List<Product> products;
    if (selectedCategory == null) {
        products = productService.getAllProducts();
    } else {
        products = productService.getProductsByCategory(selectedCategory.getId());
    }
    
    // Create report title
    Label titleLabel = new Label("Stock Levels Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    Label subtitleLabel = new Label(selectedCategory == null ? 
            "All Categories" : "Category: " + selectedCategory.getName());
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate summary metrics
    int totalProducts = products.size();
    BigDecimal totalItems = BigDecimal.ZERO;
    int lowStockItems = 0;
    int outOfStockItems = 0;
    
    for (Product product : products) {
        totalItems = totalItems.add(product.getCurrentStock());
        
        if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
            outOfStockItems++;
        } else if (product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0) {
            lowStockItems++;
        }
    }
    
    // Create summary labels
    Label totalProductsLabel = new Label("Total Products: " + totalProducts);
    totalProductsLabel.getStyleClass().add("summary-item");
    
    Label totalItemsLabel = new Label("Total Items in Stock: " + totalItems.setScale(2, RoundingMode.HALF_UP));
    totalItemsLabel.getStyleClass().add("summary-item");
    
    Label lowStockItemsLabel = new Label("Low Stock Items: " + lowStockItems);
    lowStockItemsLabel.getStyleClass().add("summary-item");
    
    Label outOfStockItemsLabel = new Label("Out of Stock Items: " + outOfStockItems);
    outOfStockItemsLabel.getStyleClass().add("summary-item");
    
    // Add summary items to box
    summaryBox.getChildren().addAll(
            totalProductsLabel, totalItemsLabel, lowStockItemsLabel, outOfStockItemsLabel);
    
    // Create PieChart for stock status
    PieChart stockStatusChart = new PieChart();
    stockStatusChart.setTitle("Stock Status");
    
    int normalStockItems = totalProducts - lowStockItems - outOfStockItems;
    
    stockStatusChart.getData().add(new PieChart.Data("Normal Stock (" + normalStockItems + ")", normalStockItems));
    stockStatusChart.getData().add(new PieChart.Data("Low Stock (" + lowStockItems + ")", lowStockItems));
    stockStatusChart.getData().add(new PieChart.Data("Out of Stock (" + outOfStockItems + ")", outOfStockItems));
    
    // Create table for detailed product view
    TableView<Product> productTable = new TableView<>();
    
    TableColumn<Product, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId()));
    
    TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
    
    TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
    categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory() != null ? 
                    cellData.getValue().getCategory().getName() : "Uncategorized"));
    
    TableColumn<Product, String> unitColumn = new TableColumn<>("Unit");
    unitColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUnit() != null ? 
                    cellData.getValue().getUnit().getName() : ""));
    
    TableColumn<Product, BigDecimal> currentStockColumn = new TableColumn<>("Current Stock");
    currentStockColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getCurrentStock()));
    
    // Format decimal values for current stock column
    currentStockColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, BigDecimal> reorderLevelColumn = new TableColumn<>("Reorder Level");
    reorderLevelColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getReorderLevel()));
    
    // Format decimal values for reorder level column
    reorderLevelColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        String status;
        
        if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
            status = "OUT OF STOCK";
        } else if (product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0) {
            status = "LOW STOCK";
        } else {
            status = "NORMAL";
        }
        
        return new SimpleStringProperty(status);
    });
    
    // Add color formatting to status column
    statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (item == null || empty) {
                setText(null);
                setStyle("");
            } else {
                setText(item);
                
                if (item.equals("OUT OF STOCK")) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red
                } else if (item.equals("LOW STOCK")) {
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Orange
                } else {
                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                }
            }
        }
    });
    
    productTable.getColumns().addAll(idColumn, nameColumn, categoryColumn, unitColumn, 
            currentStockColumn, reorderLevelColumn, statusColumn);
    
    // Sort by status (out of stock first, then low stock, then normal)
    products.sort((p1, p2) -> {
        int status1 = getStockStatusPriority(p1);
        int status2 = getStockStatusPriority(p2);
        
        return status1 - status2;
    });
    
    productTable.setItems(FXCollections.observableArrayList(products));
    
    // Store report data for export
    currentReportData = products;
    
    // Add all components to the container
    inventoryReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, stockStatusChart, productTable);
}

// Helper method for sorting by stock status
private int getStockStatusPriority(Product product) {
    if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
        return 0; // Out of stock (highest priority)
    } else if (product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0) {
        return 1; // Low stock
    } else {
        return 2; // Normal stock (lowest priority)
    }
}

private void generateLowStockReport(Category selectedCategory) {
    // Get low stock items based on category filter
    List<Product> allProducts;
    if (selectedCategory == null) {
        allProducts = productService.getAllProducts();
    } else {
        allProducts = productService.getProductsByCategory(selectedCategory.getId());
    }
    
    // Filter to get only low stock items - updated for BigDecimal
    List<Product> lowStockProducts = allProducts.stream()
            .filter(p -> p.getCurrentStock().compareTo(p.getReorderLevel()) <= 0)
            .sorted(Comparator.comparing(p -> 
                    p.getCurrentStock().divide(p.getReorderLevel(), 2, RoundingMode.HALF_UP).doubleValue()))
            .collect(Collectors.toList());
    
    // Create report title
    Label titleLabel = new Label("Low Stock Items Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    Label subtitleLabel = new Label(selectedCategory == null ? 
            "All Categories" : "Category: " + selectedCategory.getName());
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Count low stock and out of stock items - updated for BigDecimal
    int lowStockCount = (int) lowStockProducts.stream()
            .filter(p -> p.getCurrentStock().compareTo(BigDecimal.ZERO) > 0 
                    && p.getCurrentStock().compareTo(p.getReorderLevel()) <= 0)
            .count();
    
    int outOfStockCount = (int) lowStockProducts.stream()
            .filter(p -> p.getCurrentStock().compareTo(BigDecimal.ZERO) == 0)
            .count();
    
    // Create summary labels
    Label totalLowStockLabel = new Label("Total Low Stock Items: " + lowStockProducts.size());
    totalLowStockLabel.getStyleClass().add("summary-item");
    
    Label lowStockItemsLabel = new Label("Low Stock Items: " + lowStockCount);
    lowStockItemsLabel.getStyleClass().add("summary-item");
    
    Label outOfStockItemsLabel = new Label("Out of Stock Items: " + outOfStockCount);
    outOfStockItemsLabel.getStyleClass().add("summary-item");
    
    // Add summary items to box
    summaryBox.getChildren().addAll(
            totalLowStockLabel, lowStockItemsLabel, outOfStockItemsLabel);
    
    // Create table for low stock items
    TableView<Product> lowStockTable = new TableView<>();
    
    TableColumn<Product, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId()));
    
    TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
    
    TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
    categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory() != null ? 
                    cellData.getValue().getCategory().getName() : "Uncategorized"));
    
    TableColumn<Product, BigDecimal> currentStockColumn = new TableColumn<>("Current Stock");
    currentStockColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getCurrentStock()));
            
    // Format current stock column
    currentStockColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, BigDecimal> reorderLevelColumn = new TableColumn<>("Reorder Level");
    reorderLevelColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getReorderLevel()));
            
    // Format reorder level column
    reorderLevelColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, BigDecimal> deficitColumn = new TableColumn<>("Deficit");
    deficitColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        BigDecimal deficit = product.getReorderLevel().subtract(product.getCurrentStock());
        return new SimpleObjectProperty<>(deficit.compareTo(BigDecimal.ZERO) > 0 ? deficit : BigDecimal.ZERO);
    });
    
    // Format deficit column
    deficitColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        String status;
        
        if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
            status = "OUT OF STOCK";
        } else {
            status = "LOW STOCK";
        }
        
        return new SimpleStringProperty(status);
    });
    
    // Add color formatting to status column
    statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (item == null || empty) {
                setText(null);
                setStyle("");
            } else {
                setText(item);
                
                if (item.equals("OUT OF STOCK")) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red
                } else {
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Orange
                }
            }
        }
    });
    
    lowStockTable.getColumns().addAll(idColumn, nameColumn, categoryColumn, 
            currentStockColumn, reorderLevelColumn, deficitColumn, statusColumn);
    
    lowStockTable.setItems(FXCollections.observableArrayList(lowStockProducts));
    
    // Create Bar chart for reorder quantities
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Product");
    yAxis.setLabel("Quantity");
    
    BarChart<String, Number> reorderChart = new BarChart<>(xAxis, yAxis);
    reorderChart.setTitle("Recommended Reorder Quantities");
    
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Reorder Quantity");
    
    // Add top 10 items by deficit to chart
    List<Product> topDeficitProducts = lowStockProducts.stream()
            .sorted(Comparator.comparing(p -> 
                    p.getReorderLevel().subtract(p.getCurrentStock()), Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());
    
    for (Product product : topDeficitProducts) {
        BigDecimal deficit = product.getReorderLevel().subtract(product.getCurrentStock());
        if (deficit.compareTo(BigDecimal.ZERO) > 0) {
            series.getData().add(new XYChart.Data<>(
                    product.getName(), deficit));
        }
    }
    
    reorderChart.getData().add(series);
    
    // Store report data for export
    currentReportData = lowStockProducts;
    
    // Add all components to the container
    inventoryReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, reorderChart, lowStockTable);
}

private void generateStockValuationReport(Category selectedCategory) {
    // Get products based on category filter
    List<Product> products;
    if (selectedCategory == null) {
        products = productService.getAllProducts();
    } else {
        products = productService.getProductsByCategory(selectedCategory.getId());
    }
    
    // Create report title
    Label titleLabel = new Label("Stock Valuation Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    Label subtitleLabel = new Label(selectedCategory == null ? 
            "All Categories" : "Category: " + selectedCategory.getName());
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add pricing strategy info
    String pricingStrategy = systemConfigService.getPricingStrategy();
    Label strategyLabel = new Label("Valuation Method: " + pricingStrategy + " (using actual batch costs)");
    strategyLabel.getStyleClass().add("report-subtitle");
    
    // Calculate valuation using actual batch costs
    BigDecimal totalStockValue = BigDecimal.ZERO;
    Map<Category, BigDecimal> categoryValues = new HashMap<>();
    List<Map<String, Object>> productValuations = new ArrayList<>();
    
    for (Product product : products) {
        // Get actual inventory value from batches
        BigDecimal productValue = reportingService.getProductInventoryValue(product.getId());
        
        if (productValue.compareTo(BigDecimal.ZERO) > 0) {
            totalStockValue = totalStockValue.add(productValue);
            
            Category category = product.getCategory();
            categoryValues.merge(category, productValue, BigDecimal::add);
            
            // Calculate weighted average cost
            BigDecimal weightedAvgCost = BigDecimal.ZERO;
            if (product.getCurrentStock().compareTo(BigDecimal.ZERO) > 0) {
                weightedAvgCost = productValue.divide(product.getCurrentStock(), DECIMAL_SCALE, ROUNDING_MODE);
            }
            
            Map<String, Object> productValuation = new HashMap<>();
            productValuation.put("product", product);
            productValuation.put("currentStock", product.getCurrentStock());
            productValuation.put("weightedAvgCost", weightedAvgCost);
            productValuation.put("totalValue", productValue);
            productValuations.add(productValuation);
        }
    }
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    Label totalValueLabel = new Label("Total Stock Value: LKR " + formatDecimal(totalStockValue));
    totalValueLabel.getStyleClass().add("summary-item");
    
    Label productsCountLabel = new Label("Products with Stock: " + productValuations.size());
    productsCountLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(totalValueLabel, productsCountLabel);
    
    // Create PieChart for category valuation
    PieChart categoryValueChart = new PieChart();
    categoryValueChart.setTitle("Stock Value by Category");
    
    for (Map.Entry<Category, BigDecimal> entry : categoryValues.entrySet()) {
        String categoryName = entry.getKey() != null ? 
                entry.getKey().getName() : "Uncategorized";
        BigDecimal formattedValue = entry.getValue().setScale(DECIMAL_SCALE, ROUNDING_MODE);
        
        categoryValueChart.getData().add(new PieChart.Data(
                categoryName + " (LKR " + formatDecimal(formattedValue) + ")", 
                formattedValue.doubleValue()));
    }
    
    // Create table for product valuation
    TableView<Map<String, Object>> valuationTable = new TableView<>();
    
    TableColumn<Map<String, Object>, String> nameColumn = new TableColumn<>("Product Name");
    nameColumn.setCellValueFactory(data -> {
        Product product = (Product) data.getValue().get("product");
        return new SimpleStringProperty(product.getName());
    });
    
    TableColumn<Map<String, Object>, String> categoryColumn = new TableColumn<>("Category");
    categoryColumn.setCellValueFactory(data -> {
        Product product = (Product) data.getValue().get("product");
        return new SimpleStringProperty(product.getCategory() != null ? 
                product.getCategory().getName() : "Uncategorized");
    });
    
    TableColumn<Map<String, Object>, BigDecimal> stockColumn = new TableColumn<>("Current Stock");
    stockColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("currentStock")));
    stockColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(formatDecimal(item));
            }
        }
    });
    
    TableColumn<Map<String, Object>, BigDecimal> avgCostColumn = new TableColumn<>("Weighted Avg Cost");
    avgCostColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("weightedAvgCost")));
    avgCostColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    TableColumn<Map<String, Object>, BigDecimal> totalValueColumn = new TableColumn<>("Total Value");
    totalValueColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("totalValue")));
    totalValueColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    valuationTable.getColumns().addAll(nameColumn, categoryColumn, 
            stockColumn, avgCostColumn, totalValueColumn);
    
    // Sort by total value (descending)
    productValuations.sort((v1, v2) -> {
        BigDecimal value1 = (BigDecimal) v1.get("totalValue");
        BigDecimal value2 = (BigDecimal) v2.get("totalValue");
        return value2.compareTo(value1);
    });
    
    valuationTable.setItems(FXCollections.observableArrayList(productValuations));
    
    // Store report data for export
    currentReportData = productValuations;
    
    // Add all components to the container
    inventoryReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, strategyLabel, summaryBox, categoryValueChart, valuationTable);
}

private void generateInventoryTurnoverReport(Category selectedCategory) {
    // Define date range for turnover calculation (last 30 days)
    LocalDateTime endDate = LocalDateTime.now();
    LocalDateTime startDate = endDate.minusDays(30);
    
    // Get products based on category filter
    List<Product> products;
    if (selectedCategory == null) {
        products = productService.getAllProducts();
    } else {
        products = productService.getProductsByCategory(selectedCategory.getId());
    }
    
    // Get sales data for turnover calculation
    Map<Product, Map<String, Object>> productSales = 
            reportingService.getProductSales(startDate, endDate);
    
    // Calculate inventory turnover
    Map<Product, Double> turnoverRates = reportingService.getInventoryTurnoverRate(startDate, endDate);
    
    // Create report title
    Label titleLabel = new Label("Inventory Turnover Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDate.format(dateFormatter) + 
            " to " + endDate.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add category filter info if selected
    if (selectedCategory != null) {
        Label categoryLabel = new Label("Category: " + selectedCategory.getName());
        categoryLabel.getStyleClass().add("report-subtitle");
        inventoryReportContainer.getChildren().add(categoryLabel);
    }
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate average turnover
    double averageTurnover = turnoverRates.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    
    // Count products with turnover
    int productsWithTurnover = (int) turnoverRates.values().stream()
            .filter(rate -> rate > 0)
            .count();
    
    // Create summary labels
    Label averageTurnoverLabel = new Label("Average Inventory Turnover: " 
            + String.format("%.2f", averageTurnover));
    averageTurnoverLabel.getStyleClass().add("summary-item");
    
    Label productsWithTurnoverLabel = new Label("Products with Sales: " + productsWithTurnover 
            + " of " + products.size());
    productsWithTurnoverLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(averageTurnoverLabel, productsWithTurnoverLabel);
    
    // Create bar chart for top products by turnover
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Product");
    yAxis.setLabel("Turnover Rate");
    
    BarChart<String, Number> turnoverChart = new BarChart<>(xAxis, yAxis);
    turnoverChart.setTitle("Top 10 Products by Turnover Rate");
    
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Turnover Rate");
    
    // Get top 10 products by turnover
    List<Map.Entry<Product, Double>> topTurnoverProducts = turnoverRates.entrySet().stream()
            .sorted(Map.Entry.<Product, Double>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
    
    for (Map.Entry<Product, Double> entry : topTurnoverProducts) {
        series.getData().add(new XYChart.Data<>(
                entry.getKey().getName(), entry.getValue()));
    }
    
    turnoverChart.getData().add(series);
    
    // Create table for turnover details
    TableView<Product> turnoverTable = new TableView<>();
    
    TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
    
    TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
    categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory() != null ? 
                    cellData.getValue().getCategory().getName() : "Uncategorized"));
    
    TableColumn<Product, BigDecimal> stockColumn = new TableColumn<>("Current Stock");
stockColumn.setCellValueFactory(cellData -> 
        new SimpleObjectProperty<BigDecimal>(cellData.getValue().getCurrentStock()));
     stockColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(item.setScale(2, RoundingMode.HALF_UP).toString());
            }
        }
    });
    
    TableColumn<Product, BigDecimal> quantitySoldColumn = new TableColumn<>("Quantity Sold");
quantitySoldColumn.setCellValueFactory(cellData -> {
    Product currentProduct = cellData.getValue();
    BigDecimal quantity = BigDecimal.ZERO;
    
    // Find sales data for this product by ID
    for (Map.Entry<Product, Map<String, Object>> entry : productSales.entrySet()) {
        if (entry.getKey().getId().equals(currentProduct.getId())) {
            quantity = (BigDecimal) entry.getValue().get("quantity");
            break;
        }
    }
    
    return new SimpleObjectProperty<>(quantity);
});
    
   TableColumn<Product, Double> turnoverRateColumn = new TableColumn<>("Turnover Rate");
turnoverRateColumn.setCellValueFactory(cellData -> {
    Product currentProduct = cellData.getValue();
    Double rate = 0.0;
    
    // Find turnover rate for this product by ID
    for (Map.Entry<Product, Double> entry : turnoverRates.entrySet()) {
        if (entry.getKey().getId().equals(currentProduct.getId())) {
            rate = entry.getValue();
            break;
        }
    }
    
    return new SimpleObjectProperty<>(rate != null ? rate : 0.0);
});
    
    // Format turnover rate column
    turnoverRateColumn.setCellFactory(column -> new TableCell<Product, Double>() {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(String.format("%.2f", item));
            }
        }
    });
    
    turnoverTable.getColumns().addAll(nameColumn, categoryColumn, 
            stockColumn, quantitySoldColumn, turnoverRateColumn);
    
    // Sort products by turnover rate (descending)
    List<Product> sortedProducts = products.stream()
            .sorted(Comparator.comparing(p -> 
                    turnoverRates.getOrDefault(p, 0.0), Comparator.reverseOrder()))
            .collect(Collectors.toList());
    
    turnoverTable.setItems(FXCollections.observableArrayList(sortedProducts));
    
    // Store report data for export - combine product data with turnover data
    List<Map<String, Object>> exportData = new ArrayList<>();
    for (Product product : sortedProducts) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("product", product);
        productData.put("turnoverRate", turnoverRates.getOrDefault(product, 0.0));
        
        Map<String, Object> salesData = productSales.get(product);
        productData.put("quantitySold", salesData != null ? 
                salesData.get("quantity") : BigDecimal.ZERO);
        
        exportData.add(productData);
    }
    
    currentReportData = exportData;
    
    // Add all components to the container
    inventoryReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, turnoverChart, turnoverTable);
}

private void generateCategoryDistributionReport() {
    // Get all products
    List<Product> products = productService.getAllProducts();
    
    // Get all categories
    List<Category> categories = categoryService.getAllCategories();
    
    // Create report title
    Label titleLabel = new Label("Category Distribution Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Add pricing strategy info
    Label strategyLabel = createPricingStrategyLabel();
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Count products and calculate actual inventory values by category
    Map<Category, Integer> productCountByCategory = new HashMap<>();
    Map<Category, BigDecimal> stockCountByCategory = new HashMap<>();
    Map<Category, BigDecimal> inventoryValueByCategory = new HashMap<>();
    
    for (Product product : products) {
        Category category = product.getCategory();
        
        // Count products
        productCountByCategory.merge(category, 1, Integer::sum);
        
        // Count stock - updated for BigDecimal
        stockCountByCategory.merge(category, product.getCurrentStock(), BigDecimal::add);
        
        // Calculate actual inventory value from batches
        BigDecimal productValue = reportingService.getProductInventoryValue(product.getId());
        inventoryValueByCategory.merge(category, productValue, BigDecimal::add);
    }
    
    // Calculate total inventory value
    BigDecimal totalInventoryValue = inventoryValueByCategory.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Create summary labels
    Label totalProductsLabel = new Label("Total Products: " + products.size());
    totalProductsLabel.getStyleClass().add("summary-item");
    
    Label totalCategoriesLabel = new Label("Total Categories: " + categories.size());
    totalCategoriesLabel.getStyleClass().add("summary-item");
    
    Label totalValueLabel = new Label("Total Inventory Value: LKR " + formatDecimal(totalInventoryValue));
    totalValueLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(totalProductsLabel, totalCategoriesLabel, totalValueLabel);
    
    // Create pie chart for product distribution
    PieChart productDistributionChart = new PieChart();
    productDistributionChart.setTitle("Products Distribution by Category");
    
    for (Map.Entry<Category, Integer> entry : productCountByCategory.entrySet()) {
        String categoryName = entry.getKey() != null ? 
                entry.getKey().getName() : "Uncategorized";
        
        productDistributionChart.getData().add(new PieChart.Data(
                categoryName + " (" + entry.getValue() + " products)", 
                entry.getValue()));
    }
    
    // Create pie chart for stock distribution
    PieChart stockDistributionChart = new PieChart();
    stockDistributionChart.setTitle("Stock Distribution by Category");
    
    for (Map.Entry<Category, BigDecimal> entry : stockCountByCategory.entrySet()) {
        String categoryName = entry.getKey() != null ? 
                entry.getKey().getName() : "Uncategorized";
        BigDecimal formattedValue = entry.getValue().setScale(2, RoundingMode.HALF_UP);
        
        stockDistributionChart.getData().add(new PieChart.Data(
                categoryName + " (" + formatDecimal(formattedValue) + " items)", 
                formattedValue.doubleValue()));
    }
    
    // Create pie chart for inventory value distribution
    PieChart inventoryValueChart = new PieChart();
    inventoryValueChart.setTitle("Inventory Value Distribution by Category");
    
    for (Map.Entry<Category, BigDecimal> entry : inventoryValueByCategory.entrySet()) {
        String categoryName = entry.getKey() != null ? entry.getKey().getName() : "Uncategorized";
        BigDecimal value = entry.getValue();
        
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            inventoryValueChart.getData().add(new PieChart.Data(
                    categoryName + " (LKR " + formatDecimal(value) + ")", 
                    value.doubleValue()));
        }
    }
    
    // Create HBox to hold the charts
    HBox chartsBox1 = new HBox(20);
    chartsBox1.setAlignment(Pos.CENTER);
    chartsBox1.getChildren().addAll(productDistributionChart, stockDistributionChart);
    
    HBox chartsBox2 = new HBox(20);
    chartsBox2.setAlignment(Pos.CENTER);
    chartsBox2.getChildren().add(inventoryValueChart);
    
    // Create table for category details
    TableView<Category> categoryTable = new TableView<>();
    
    TableColumn<Category, String> nameColumn = new TableColumn<>("Category Name");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue() != null ? 
                    cellData.getValue().getName() : "Uncategorized"));
    
    TableColumn<Category, Integer> productCountColumn = new TableColumn<>("Product Count");
    productCountColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(productCountByCategory.getOrDefault(cellData.getValue(), 0)));
    
    TableColumn<Category, BigDecimal> stockCountColumn = new TableColumn<>("Stock Count");
    stockCountColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(stockCountByCategory.getOrDefault(cellData.getValue(), BigDecimal.ZERO)));
            
    // Format stock count column for decimal display
    stockCountColumn.setCellFactory(column -> new TableCell<Category, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(formatDecimal(item));
            }
        }
    });
    
    // Add inventory value column
    TableColumn<Category, BigDecimal> inventoryValueColumn = new TableColumn<>("Inventory Value");
    inventoryValueColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(inventoryValueByCategory.getOrDefault(cellData.getValue(), BigDecimal.ZERO)));
            
    // Format inventory value column for currency display
    inventoryValueColumn.setCellFactory(column -> new TableCell<Category, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    TableColumn<Category, String> percentageColumn = new TableColumn<>("% of Products");
    percentageColumn.setCellValueFactory(cellData -> {
        int categoryCount = productCountByCategory.getOrDefault(cellData.getValue(), 0);
        double percentage = products.isEmpty() ? 0 : 
                (double) categoryCount / products.size() * 100;
        return new SimpleStringProperty(String.format("%.2f%%", percentage));
    });
    
    TableColumn<Category, String> valuePercentageColumn = new TableColumn<>("% of Value");
    valuePercentageColumn.setCellValueFactory(cellData -> {
        BigDecimal categoryValue = inventoryValueByCategory.getOrDefault(cellData.getValue(), BigDecimal.ZERO);
        double percentage = totalInventoryValue.compareTo(BigDecimal.ZERO) == 0 ? 0 : 
                categoryValue.divide(totalInventoryValue, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        return new SimpleStringProperty(String.format("%.2f%%", percentage));
    });
    
    categoryTable.getColumns().addAll(nameColumn, productCountColumn, stockCountColumn, 
            inventoryValueColumn, percentageColumn, valuePercentageColumn);
    
    // Add null category for "Uncategorized" if it exists in the data
    List<Category> displayCategories = new ArrayList<>(categories);
    if (productCountByCategory.containsKey(null)) {
        displayCategories.add(null);
    }
    
    // Sort categories by inventory value (descending)
    displayCategories.sort((c1, c2) -> {
        BigDecimal value1 = inventoryValueByCategory.getOrDefault(c1, BigDecimal.ZERO);
        BigDecimal value2 = inventoryValueByCategory.getOrDefault(c2, BigDecimal.ZERO);
        return value2.compareTo(value1);
    });
    
    categoryTable.setItems(FXCollections.observableArrayList(displayCategories));
    
    // Store report data for export
    currentReportData = displayCategories;
    
    // Add all components to the container
    inventoryReportContainer.getChildren().addAll(
            titleLabel, strategyLabel, summaryBox, chartsBox1, chartsBox2, categoryTable);
}

@FXML
private void handleExportInventoryReport(ActionEvent event) {
    if (currentReportData == null) {
        AlertHelper.showErrorAlert("No Report Data", "No report data available to export", 
                "Please generate a report first before attempting to export.");
        return;
    }
    
    exportReport(currentReportData, "inventory_report_" + currentReportType.toLowerCase().replace(" ", "_"));
}


@FXML
private void handleGenerateCustomerReport(ActionEvent event) {
    // Clear previous report
    customerReportContainer.getChildren().clear();
    
    // Get selected customer
    Customer selectedCustomer = customerCombo.getValue();
    
    // Get selected report type
    String reportType = customerReportTypeCombo.getValue();
    currentReportType = reportType;
    
    switch (reportType) {
        case "Purchase History":
            generatePurchaseHistoryReport(selectedCustomer);
            break;
            
        case "Credit Status":
            generateCreditStatusReport(selectedCustomer);
            break;
            
        case "Top Customers":
            generateTopCustomersReport();
            break;
            
        case "Customer Segmentation":
            generateCustomerSegmentationReport();
            break;
    }
}

private void generatePurchaseHistoryReport(Customer selectedCustomer) {
    // Get invoices for the selected customer (last 90 days by default)
    LocalDateTime endDate = LocalDateTime.now();
    LocalDateTime startDate = endDate.minusDays(90);
    
    List<Invoice> invoices;
    
    if (selectedCustomer == null) {
        // Get all customer invoices for the period
        invoices = invoiceService.getInvoicesForDateRange(startDate, endDate);
    } else {
        // Get specific customer invoices
        invoices = invoiceService.getInvoicesForCustomerAndDateRange(
                selectedCustomer.getId(), startDate, endDate);
    }
    
    // Create report title
    Label titleLabel = new Label("Purchase History Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDate.format(dateFormatter) + 
            " to " + endDate.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add customer name if selected
    if (selectedCustomer != null) {
        Label customerLabel = new Label("Customer: " + selectedCustomer.getName());
        customerLabel.getStyleClass().add("report-subtitle");
        customerReportContainer.getChildren().add(customerLabel);
    }
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate totals
    BigDecimal totalPurchases = BigDecimal.ZERO;
    int totalTransactions = invoices.size();
    Map<String, BigDecimal> paymentMethodTotals = new HashMap<>();
    
    for (Invoice invoice : invoices) {
        totalPurchases = totalPurchases.add(invoice.getFinalAmount());
        
        String paymentMethod = invoice.getPaymentMethod();
        paymentMethodTotals.put(paymentMethod, 
                paymentMethodTotals.getOrDefault(paymentMethod, BigDecimal.ZERO)
                .add(invoice.getFinalAmount()));
    }
    
    // Create summary labels
    Label totalPurchasesLabel = new Label("Total Purchases: LKR " + totalPurchases);
    totalPurchasesLabel.getStyleClass().add("summary-item");
    
    Label totalTransactionsLabel = new Label("Total Transactions: " + totalTransactions);
    totalTransactionsLabel.getStyleClass().add("summary-item");
    
    BigDecimal avgTransaction = totalTransactions > 0 
            ? totalPurchases.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
    
    Label avgTransactionLabel = new Label("Average Transaction: LKR " + avgTransaction);
    avgTransactionLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(
            totalPurchasesLabel, totalTransactionsLabel, avgTransactionLabel);
    
    // Add payment method breakdown
    for (Map.Entry<String, BigDecimal> entry : paymentMethodTotals.entrySet()) {
        Label methodLabel = new Label(entry.getKey() + ": LKR " + entry.getValue());
        methodLabel.getStyleClass().add("summary-item");
        summaryBox.getChildren().add(methodLabel);
    }
    
    // Create purchase trend chart (if for a specific customer)
    if (selectedCustomer != null) {
        // Group invoices by month
        Map<String, BigDecimal> monthlyPurchases = new TreeMap<>();
        for (Invoice invoice : invoices) {
            String monthYear = invoice.getDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            monthlyPurchases.put(monthYear, 
                    monthlyPurchases.getOrDefault(monthYear, BigDecimal.ZERO)
                    .add(invoice.getFinalAmount()));
        }
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Purchase Amount (LKR)");
        
        LineChart<String, Number> trendChart = new LineChart<>(xAxis, yAxis);
        trendChart.setTitle("Purchase Trend - Last 3 Months");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Purchases");
        
        for (Map.Entry<String, BigDecimal> entry : monthlyPurchases.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey(), entry.getValue()));
        }
        
        trendChart.getData().add(series);
        customerReportContainer.getChildren().add(trendChart);
    }
    
    // Create table for purchase history
    TableView<Invoice> invoiceTable = new TableView<>();
    
    TableColumn<Invoice, String> dateColumn = new TableColumn<>("Date");
    dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().format(
                    DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));
    
    TableColumn<Invoice, String> invoiceColumn = new TableColumn<>("Invoice #");
    invoiceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getInvoiceNumber()));
    
    TableColumn<Invoice, String> customerColumn = new TableColumn<>("Customer");
    customerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer() != null ? 
                    cellData.getValue().getCustomer().getName() : "Walk-in Customer"));
    
    // Only show customer column if no specific customer is selected
    if (selectedCustomer == null) {
        invoiceTable.getColumns().add(customerColumn);
    }
    
    TableColumn<Invoice, String> paymentMethodColumn = new TableColumn<>("Payment Method");
    paymentMethodColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPaymentMethod()));
    
    TableColumn<Invoice, BigDecimal> totalAmountColumn = new TableColumn<>("Total Amount");
    totalAmountColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getFinalAmount()));
    
    // Format amount column
    totalAmountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
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
    
    invoiceTable.getColumns().addAll(dateColumn, invoiceColumn, 
            paymentMethodColumn, totalAmountColumn);
    
    // Sort by date (newest first)
    invoices.sort(Comparator.comparing(Invoice::getDate).reversed());
    
    invoiceTable.setItems(FXCollections.observableArrayList(invoices));
    
    // Store report data for export
    currentReportData = invoices;
    
    // Add all components to the container
    customerReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, invoiceTable);
}

private void generateCreditStatusReport(Customer selectedCustomer) {
    List<Customer> customers;
    
    if (selectedCustomer == null) {
        // Get all customers with credit accounts
        customers = customerService.getAllCustomers().stream()
                .filter(c -> c.getCreditAccount() != null)
                .collect(Collectors.toList());
    } else {
        // Use only the selected customer
        customers = Collections.singletonList(selectedCustomer);
    }
    
    // Create report title
    Label titleLabel = new Label("Credit Status Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create current date label
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label dateLabel = new Label("Report Date: " + LocalDate.now().format(dateFormatter));
    dateLabel.getStyleClass().add("report-subtitle");
    
    // Add customer name if selected
    if (selectedCustomer != null) {
        Label customerLabel = new Label("Customer: " + selectedCustomer.getName());
        customerLabel.getStyleClass().add("report-subtitle");
        customerReportContainer.getChildren().add(customerLabel);
    }
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate credit statistics
    BigDecimal totalOutstandingCredit = BigDecimal.ZERO;
    int customersWithCredit = 0;
    
    for (Customer customer : customers) {
        if (customer.getCreditAccount() != null && 
            customer.getCreditAccount().getBalance().compareTo(BigDecimal.ZERO) > 0) {
            totalOutstandingCredit = totalOutstandingCredit.add(
                    customer.getCreditAccount().getBalance());
            customersWithCredit++;
        }
    }
    
    // Create summary labels
    Label totalCreditLabel = new Label("Total Outstanding Credit: LKR " + totalOutstandingCredit);
    totalCreditLabel.getStyleClass().add("summary-item");
    
    Label creditCustomersLabel = new Label("Customers with Outstanding Credit: " + customersWithCredit);
    creditCustomersLabel.getStyleClass().add("summary-item");
    
    BigDecimal avgCredit = customersWithCredit > 0 
            ? totalOutstandingCredit.divide(new BigDecimal(customersWithCredit), 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
    
    Label avgCreditLabel = new Label("Average Credit Balance: LKR " + avgCredit);
    avgCreditLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(
            totalCreditLabel, creditCustomersLabel, avgCreditLabel);
    
    // Create table for credit status
    TableView<Customer> creditTable = new TableView<>();
    
    TableColumn<Customer, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId()));
    
    TableColumn<Customer, String> nameColumn = new TableColumn<>("Customer");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
    
    TableColumn<Customer, String> contactColumn = new TableColumn<>("Contact");
    contactColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContactNo()));
    
    TableColumn<Customer, BigDecimal> balanceColumn = new TableColumn<>("Credit Balance");
    balanceColumn.setCellValueFactory(cellData -> {
        if (cellData.getValue().getCreditAccount() != null) {
            return new SimpleObjectProperty<>(cellData.getValue().getCreditAccount().getBalance());
        } else {
            return new SimpleObjectProperty<>(BigDecimal.ZERO);
        }
    });
    
    TableColumn<Customer, BigDecimal> limitColumn = new TableColumn<>("Credit Limit");
    limitColumn.setCellValueFactory(cellData -> {
        if (cellData.getValue().getCreditAccount() != null && 
            cellData.getValue().getCreditAccount().getCreditLimit() != null) {
            return new SimpleObjectProperty<>(cellData.getValue().getCreditAccount().getCreditLimit());
        } else {
            return new SimpleObjectProperty<>(null);
        }
    });
    
    TableColumn<Customer, String> usageColumn = new TableColumn<>("Usage %");
    usageColumn.setCellValueFactory(cellData -> {
        if (cellData.getValue().getCreditAccount() != null && 
            cellData.getValue().getCreditAccount().getCreditLimit() != null &&
            cellData.getValue().getCreditAccount().getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal usage = cellData.getValue().getCreditAccount().getBalance()
                    .multiply(new BigDecimal("100"))
                    .divide(cellData.getValue().getCreditAccount().getCreditLimit(), 2, RoundingMode.HALF_UP);
            
            return new SimpleStringProperty(usage + "%");
        } else {
            return new SimpleStringProperty("N/A");
        }
    });
    
    TableColumn<Customer, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(cellData -> {
        if (cellData.getValue().getCreditAccount() == null || 
            cellData.getValue().getCreditAccount().getBalance().compareTo(BigDecimal.ZERO) == 0) {
            return new SimpleStringProperty("NO BALANCE");
        }
        
        if (cellData.getValue().getCreditAccount().getCreditLimit() == null) {
            return new SimpleStringProperty("ACTIVE");
        }
        
        BigDecimal balance = cellData.getValue().getCreditAccount().getBalance();
        BigDecimal limit = cellData.getValue().getCreditAccount().getCreditLimit();
        
        BigDecimal usagePercent = balance.multiply(new BigDecimal("100"))
                .divide(limit, 2, RoundingMode.HALF_UP);
        
        if (usagePercent.compareTo(new BigDecimal("90")) > 0) {
            return new SimpleStringProperty("CRITICAL");
        } else if (usagePercent.compareTo(new BigDecimal("75")) > 0) {
            return new SimpleStringProperty("HIGH");
        } else {
            return new SimpleStringProperty("NORMAL");
        }
    });
    
    // Add color formatting to status column
    statusColumn.setCellFactory(column -> new TableCell<Customer, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (item == null || empty) {
                setText(null);
                setStyle("");
            } else {
                setText(item);
                
                if (item.equals("CRITICAL")) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red
                } else if (item.equals("HIGH")) {
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Orange
                } else if (item.equals("NORMAL")) {
                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green
                } else if (item.equals("NO BALANCE")) {
                    setStyle("-fx-text-fill: #7f8c8d;"); // Gray
                } else {
                    setStyle("-fx-text-fill: #3498db;"); // Blue
                }
            }
        }
    });
    
    // Format currency columns
    balanceColumn.setCellFactory(column -> new TableCell<Customer, BigDecimal>() {
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
    
    limitColumn.setCellFactory(column -> new TableCell<Customer, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText("No Limit");
            } else {
                setText("LKR " + item.toString());
            }
        }
    });
    
    creditTable.getColumns().addAll(idColumn, nameColumn, contactColumn, 
            balanceColumn, limitColumn, usageColumn, statusColumn);
    
    // Filter out customers with no credit account
    List<Customer> filteredCustomers = customers.stream()
            .filter(c -> c.getCreditAccount() != null)
            .collect(Collectors.toList());
    
    // Sort by balance (highest first)
    filteredCustomers.sort((c1, c2) -> {
        BigDecimal balance1 = c1.getCreditAccount() != null ? 
                c1.getCreditAccount().getBalance() : BigDecimal.ZERO;
        BigDecimal balance2 = c2.getCreditAccount() != null ? 
                c2.getCreditAccount().getBalance() : BigDecimal.ZERO;
        
        return balance2.compareTo(balance1);
    });
    
    creditTable.setItems(FXCollections.observableArrayList(filteredCustomers));
    
    // Create pie chart for credit status distribution if showing all customers
    if (selectedCustomer == null) {
        PieChart statusChart = new PieChart();
        statusChart.setTitle("Credit Status Distribution");
        
        int normal = 0;
        int high = 0;
        int critical = 0;
        int noBalance = 0;
        
        for (Customer customer : filteredCustomers) {
            if (customer.getCreditAccount() == null || 
                customer.getCreditAccount().getBalance().compareTo(BigDecimal.ZERO) == 0) {
                noBalance++;
                continue;
            }
            
            if (customer.getCreditAccount().getCreditLimit() == null) {
                normal++; // No limit is treated as normal
                continue;
            }
            
            BigDecimal balance = customer.getCreditAccount().getBalance();
            BigDecimal limit = customer.getCreditAccount().getCreditLimit();
            
            BigDecimal usagePercent = balance.multiply(new BigDecimal("100"))
                    .divide(limit, 2, RoundingMode.HALF_UP);
            
            if (usagePercent.compareTo(new BigDecimal("90")) > 0) {
                critical++;
            } else if (usagePercent.compareTo(new BigDecimal("75")) > 0) {
                high++;
            } else {
                normal++;
            }
        }
        
        // Add data to chart
        if (normal > 0) {
            statusChart.getData().add(new PieChart.Data("Normal (" + normal + ")", normal));
        }
        if (high > 0) {
            statusChart.getData().add(new PieChart.Data("High (" + high + ")", high));
        }
        if (critical > 0) {
            statusChart.getData().add(new PieChart.Data("Critical (" + critical + ")", critical));
        }
        if (noBalance > 0) {
            statusChart.getData().add(new PieChart.Data("No Balance (" + noBalance + ")", noBalance));
        }
        
        customerReportContainer.getChildren().add(statusChart);
    }
    
    // Store report data for export
    currentReportData = filteredCustomers;
    
    // Add all components to the container
    customerReportContainer.getChildren().addAll(
            titleLabel, dateLabel, summaryBox, creditTable);
}

private void generateTopCustomersReport() {
    // Define date range for report (last 90 days by default)
    LocalDateTime endDate = LocalDateTime.now();
    LocalDateTime startDate = endDate.minusDays(90);
    
    // Create report title
    Label titleLabel = new Label("Top Customers Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDate.format(dateFormatter) + 
            " to " + endDate.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Get top customers by purchase value
    List<Map.Entry<Customer, BigDecimal>> topCustomers = 
            reportingService.getTopCustomersByPurchaseValue(startDate, endDate, 20);
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate totals
    BigDecimal totalSales = topCustomers.stream()
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Create summary labels
    Label totalCustomersLabel = new Label("Total Customers with Purchases: " + topCustomers.size());
    totalCustomersLabel.getStyleClass().add("summary-item");
    
    Label totalSalesLabel = new Label("Total Sales to These Customers: LKR " + totalSales);
    totalSalesLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(totalCustomersLabel, totalSalesLabel);
    
    // Create bar chart for top 10 customers
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Customer");
    yAxis.setLabel("Purchase Amount (LKR)");
    
    BarChart<String, Number> topCustomersChart = new BarChart<>(xAxis, yAxis);
    topCustomersChart.setTitle("Top 10 Customers by Purchase Amount");
    
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Purchase Amount");
    
    // Add top 10 customers to chart
    List<Map.Entry<Customer, BigDecimal>> top10 = topCustomers.stream()
            .limit(10)
            .collect(Collectors.toList());
    
    for (Map.Entry<Customer, BigDecimal> entry : top10) {
        series.getData().add(new XYChart.Data<>(
                entry.getKey().getName(), entry.getValue()));
    }
    
    topCustomersChart.getData().add(series);
    
    // Create table for all top customers
    TableView<Map.Entry<Customer, BigDecimal>> customersTable = new TableView<>();
    
    TableColumn<Map.Entry<Customer, BigDecimal>, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getKey().getId()));
    
    TableColumn<Map.Entry<Customer, BigDecimal>, String> nameColumn = new TableColumn<>("Customer");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getKey().getName()));
    
    TableColumn<Map.Entry<Customer, BigDecimal>, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getKey().getCustomerType()));
    
    TableColumn<Map.Entry<Customer, BigDecimal>, String> contactColumn = new TableColumn<>("Contact");
    contactColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getKey().getContactNo()));
    
    TableColumn<Map.Entry<Customer, BigDecimal>, BigDecimal> purchaseColumn = 
            new TableColumn<>("Purchase Amount");
    purchaseColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getValue()));
    
    TableColumn<Map.Entry<Customer, BigDecimal>, String> percentageColumn = 
            new TableColumn<>("% of Total");
    percentageColumn.setCellValueFactory(cellData -> {
        BigDecimal percentage = cellData.getValue().getValue()
                .multiply(new BigDecimal("100"))
                .divide(totalSales, 2, RoundingMode.HALF_UP);
        
        return new SimpleStringProperty(percentage + "%");
    });
    
    // Format amount column
    purchaseColumn.setCellFactory(column -> new TableCell<Map.Entry<Customer, BigDecimal>, BigDecimal>() {
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
    
    customersTable.getColumns().addAll(idColumn, nameColumn, typeColumn, 
            contactColumn, purchaseColumn, percentageColumn);
    
    customersTable.setItems(FXCollections.observableArrayList(topCustomers));
    
    // Store report data for export
    currentReportData = topCustomers;
    
    // Add all components to the container
    customerReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, topCustomersChart, customersTable);
}

private void generateCustomerSegmentationReport() {
    // Get all customers
    List<Customer> customers = customerService.getAllCustomers();
    
    // Create report title
    Label titleLabel = new Label("Customer Segmentation Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Count customer types
    long retailCount = customers.stream()
            .filter(c -> "RETAIL".equals(c.getCustomerType()))
            .count();
    
    long wholesaleCount = customers.stream()
            .filter(c -> "WHOLESALE".equals(c.getCustomerType()))
            .count();
    
    long creditCustomers = customers.stream()
            .filter(c -> c.getCreditAccount() != null && 
                c.getCreditAccount().getBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();
    
    // Create summary labels
    Label totalCustomersLabel = new Label("Total Customers: " + customers.size());
    totalCustomersLabel.getStyleClass().add("summary-item");
    
    Label retailCustomersLabel = new Label("Retail Customers: " + retailCount);
    retailCustomersLabel.getStyleClass().add("summary-item");
    
    Label wholesaleCustomersLabel = new Label("Wholesale Customers: " + wholesaleCount);
    wholesaleCustomersLabel.getStyleClass().add("summary-item");
    
    Label creditAccountsLabel = new Label("Customers with Credit Balance: " + creditCustomers);
    creditAccountsLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(
            totalCustomersLabel, retailCustomersLabel, wholesaleCustomersLabel, creditAccountsLabel);
    
    // Create pie chart for customer type distribution
    PieChart customerTypeChart = new PieChart();
    customerTypeChart.setTitle("Customer Type Distribution");
    
    customerTypeChart.getData().add(new PieChart.Data(
            "Retail (" + retailCount + ")", retailCount));
    customerTypeChart.getData().add(new PieChart.Data(
            "Wholesale (" + wholesaleCount + ")", wholesaleCount));
    
    // Create pie chart for credit status
    PieChart creditStatusChart = new PieChart();
    creditStatusChart.setTitle("Credit Account Status");
    
    creditStatusChart.getData().add(new PieChart.Data(
            "With Credit Balance (" + creditCustomers + ")", creditCustomers));
    creditStatusChart.getData().add(new PieChart.Data(
            "No Credit Balance (" + (customers.size() - creditCustomers) + ")", 
            customers.size() - creditCustomers));
    
    // Create HBox to hold the charts side by side
    HBox chartsBox = new HBox(20);
    chartsBox.setAlignment(Pos.CENTER);
    chartsBox.getChildren().addAll(customerTypeChart, creditStatusChart);
    
    // Create table for customer segmentation
    TableView<Customer> customerTable = new TableView<>();
    
    TableColumn<Customer, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId()));
    
    TableColumn<Customer, String> nameColumn = new TableColumn<>("Customer");
    nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
    
    TableColumn<Customer, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomerType()));
    
    TableColumn<Customer, String> contactColumn = new TableColumn<>("Contact");
    contactColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContactNo()));
    
    TableColumn<Customer, String> creditStatusColumn = new TableColumn<>("Credit Status");
    creditStatusColumn.setCellValueFactory(cellData -> {
        if (cellData.getValue().getCreditAccount() == null) {
            return new SimpleStringProperty("No Credit Account");
        }
        
        BigDecimal balance = cellData.getValue().getCreditAccount().getBalance();
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return new SimpleStringProperty("No Balance");
        } else {
            return new SimpleStringProperty("Active - LKR " + balance);
        }
    });
    
    customerTable.getColumns().addAll(idColumn, nameColumn, typeColumn, 
            contactColumn, creditStatusColumn);
    
    customerTable.setItems(FXCollections.observableArrayList(customers));
    
    // Store report data for export
    currentReportData = customers;
    
    // Add all components to the container
    customerReportContainer.getChildren().addAll(
            titleLabel, summaryBox, chartsBox, customerTable);
}


@FXML
private void handleGenerateFinancialReport(ActionEvent event) {
    // Clear previous report
    financialReportContainer.getChildren().clear();
    
    // Get date range
    LocalDate startDate = financialStartDatePicker.getValue();
    LocalDate endDate = financialEndDatePicker.getValue();
    
    if (startDate == null || endDate == null) {
        AlertHelper.showErrorAlert("Invalid Date Range", "Please select valid dates", 
                "Both start and end dates must be selected.");
        return;
    }
    
    if (startDate.isAfter(endDate)) {
        AlertHelper.showErrorAlert("Invalid Date Range", "Start date is after end date", 
                "Please ensure the start date is before or equal to the end date.");
        return;
    }
    
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
    
    // Get selected report type
    String reportType = financialReportTypeCombo.getValue();
    currentReportType = reportType;
    
    switch (reportType) {
        case "Sales Summary":
            generateSalesSummaryReport(startDateTime, endDateTime);
            break;
            
        case "Profit & Loss":
            generateProfitLossReport(startDateTime, endDateTime);
            break;
            
        case "Revenue vs Expenses":
            generateRevenueVsExpensesReport(startDateTime, endDateTime);
            break;
            
        case "Margin Analysis":
            generateMarginAnalysisReport(startDateTime, endDateTime);
            break;
    }
    
    // Enable export button
    exportFinancialReportBtn.setDisable(false);
}

private void generateSalesSummaryReport(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // Get sales data for the date range
    List<Invoice> invoices = invoiceService.getInvoicesForDateRange(startDateTime, endDateTime);
    
    // Create report title
    Label titleLabel = new Label("Sales Summary Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle with date range
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
            " to " + endDateTime.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate totals
    BigDecimal totalSales = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    BigDecimal netSales = BigDecimal.ZERO;
    int totalInvoices = invoices.size();
    
    for (Invoice invoice : invoices) {
        totalSales = totalSales.add(invoice.getTotalAmount());
        if (invoice.getDiscountAmount() != null) {
            totalDiscount = totalDiscount.add(invoice.getDiscountAmount());
        }
        netSales = netSales.add(invoice.getFinalAmount());
    }
    
    // Create summary labels
    Label totalSalesLabel = new Label("Gross Sales: LKR " + totalSales);
    totalSalesLabel.getStyleClass().add("summary-item");
    
    Label totalDiscountLabel = new Label("Total Discounts: LKR " + totalDiscount);
    totalDiscountLabel.getStyleClass().add("summary-item");
    
    Label netSalesLabel = new Label("Net Sales: LKR " + netSales);
    netSalesLabel.getStyleClass().add("summary-item");
    
    Label invoiceCountLabel = new Label("Total Invoices: " + totalInvoices);
    invoiceCountLabel.getStyleClass().add("summary-item");
    
    BigDecimal avgInvoice = totalInvoices > 0 
            ? netSales.divide(new BigDecimal(totalInvoices), 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
    
    Label avgInvoiceLabel = new Label("Average Invoice Value: LKR " + avgInvoice);
    avgInvoiceLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(
            totalSalesLabel, totalDiscountLabel, netSalesLabel, 
            invoiceCountLabel, avgInvoiceLabel);
    
    // Group sales by payment method
    Map<String, BigDecimal> paymentMethodSales = reportingService.getSalesByPaymentMethod(
            startDateTime, endDateTime);
    
    // Create payment method summary
    VBox paymentMethodBox = new VBox(5);
    paymentMethodBox.getStyleClass().add("summary-box");
    
    Label paymentMethodTitle = new Label("Sales by Payment Method");
    paymentMethodTitle.getStyleClass().add("section-header");
    paymentMethodBox.getChildren().add(paymentMethodTitle);
    
    for (Map.Entry<String, BigDecimal> entry : paymentMethodSales.entrySet()) {
        BigDecimal percentage = entry.getValue().multiply(new BigDecimal("100"))
                .divide(netSales.equals(BigDecimal.ZERO) ? BigDecimal.ONE : netSales, 
                        2, RoundingMode.HALF_UP);
        
        Label methodLabel = new Label(entry.getKey() + ": LKR " + entry.getValue() + 
                " (" + percentage + "%)");
        methodLabel.getStyleClass().add("summary-item");
        paymentMethodBox.getChildren().add(methodLabel);
    }
    
    // Create daily sales chart
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Date");
    yAxis.setLabel("Sales (LKR)");
    
    LineChart<String, Number> salesChart = new LineChart<>(xAxis, yAxis);
    salesChart.setTitle("Daily Sales");
    
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Daily Sales");
    
    // Group sales by date
    Map<LocalDate, BigDecimal> salesByDate = reportingService.getSalesByDate(
            startDateTime, endDateTime);
    
    for (Map.Entry<LocalDate, BigDecimal> entry : salesByDate.entrySet()) {
        series.getData().add(new XYChart.Data<>(
                entry.getKey().format(dateFormatter), 
                entry.getValue()));
    }
    
    salesChart.getData().add(series);
    
    // Create customer type sales pie chart
    PieChart customerTypeChart = new PieChart();
    customerTypeChart.setTitle("Sales by Customer Type");
    
    // Get sales by customer type
    Map<String, BigDecimal> customerTypeSales = reportingService.getSalesByCustomerType(
            startDateTime, endDateTime);
    
    for (Map.Entry<String, BigDecimal> entry : customerTypeSales.entrySet()) {
        customerTypeChart.getData().add(new PieChart.Data(
                entry.getKey() + " (LKR " + entry.getValue() + ")", 
                entry.getValue().doubleValue()));
    }
    
    // Create HBox to hold the charts side by side
    HBox chartsBox = new HBox(20);
    chartsBox.setAlignment(Pos.CENTER);
    chartsBox.getChildren().addAll(salesChart, customerTypeChart);
    
    // Create monthly sales summary table (if the period is more than a month)
    long daysBetween = ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    
    if (daysBetween > 30) {
        TableView<Map<String, Object>> monthlySummaryTable = new TableView<>();
        
        TableColumn<Map<String, Object>, String> monthColumn = new TableColumn<>("Month");
        monthColumn.setCellValueFactory(data -> 
                new SimpleStringProperty((String) data.getValue().get("month")));
        
        TableColumn<Map<String, Object>, BigDecimal> salesColumn = new TableColumn<>("Sales");
        salesColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((BigDecimal) data.getValue().get("sales")));
        
        TableColumn<Map<String, Object>, Integer> invoiceCountColumn = new TableColumn<>("Invoices");
        invoiceCountColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((Integer) data.getValue().get("invoiceCount")));
        
        TableColumn<Map<String, Object>, BigDecimal> avgSalesColumn = new TableColumn<>("Avg. Sale");
        avgSalesColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((BigDecimal) data.getValue().get("averageSale")));
        
        // Format currency columns
        salesColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
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
        
        avgSalesColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
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
        
        monthlySummaryTable.getColumns().addAll(monthColumn, salesColumn, 
                invoiceCountColumn, avgSalesColumn);
        
        // Group data by month
        Map<String, List<Invoice>> invoicesByMonth = new TreeMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (Invoice invoice : invoices) {
            String monthKey = invoice.getDate().format(monthFormatter);
            if (!invoicesByMonth.containsKey(monthKey)) {
                invoicesByMonth.put(monthKey, new ArrayList<>());
            }
            invoicesByMonth.get(monthKey).add(invoice);
        }
        
        // Calculate monthly summaries
        List<Map<String, Object>> monthlySummaries = new ArrayList<>();
        
        for (Map.Entry<String, List<Invoice>> entry : invoicesByMonth.entrySet()) {
            String month = YearMonth.parse(entry.getKey(), monthFormatter)
                    .format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            List<Invoice> monthInvoices = entry.getValue();
            
            BigDecimal monthlySales = BigDecimal.ZERO;
            for (Invoice invoice : monthInvoices) {
                monthlySales = monthlySales.add(invoice.getFinalAmount());
            }
            
            int monthInvoiceCount = monthInvoices.size();
            BigDecimal monthAvgSale = monthInvoiceCount > 0 ? 
                    monthlySales.divide(new BigDecimal(monthInvoiceCount), 2, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
            
            Map<String, Object> monthSummary = new HashMap<>();
            monthSummary.put("month", month);
            monthSummary.put("sales", monthlySales);
            monthSummary.put("invoiceCount", monthInvoiceCount);
            monthSummary.put("averageSale", monthAvgSale);
            
            monthlySummaries.add(monthSummary);
        }
        
        monthlySummaryTable.setItems(FXCollections.observableArrayList(monthlySummaries));
        
        Label monthlySummaryLabel = new Label("Monthly Sales Summary");
        monthlySummaryLabel.getStyleClass().add("section-header");
        
        financialReportContainer.getChildren().addAll(monthlySummaryLabel, monthlySummaryTable);
    }
    
    // Store report data for export
    Map<String, Object> reportData = new HashMap<>();
    reportData.put("invoices", invoices);
    reportData.put("salesByDate", salesByDate);
    reportData.put("paymentMethodSales", paymentMethodSales);
    reportData.put("customerTypeSales", customerTypeSales);
    
    currentReportData = reportData;
    
    // Add all components to the container
    financialReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, summaryBox, paymentMethodBox, chartsBox);
}

private void generateProfitLossReport(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // Create report title
    Label titleLabel = new Label("Profit & Loss Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle with date range
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
            " to " + endDateTime.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add pricing strategy info
    String pricingStrategy = systemConfigService.getPricingStrategy();
    Label strategyLabel = new Label("Cost Method: " + pricingStrategy + " (using actual batch costs)");
    strategyLabel.getStyleClass().add("report-subtitle");
    
    // Calculate gross profit using actual batch costs
    BigDecimal grossProfit = reportingService.calculateGrossProfit(startDateTime, endDateTime);
    
    // Get total revenue
    List<Invoice> invoices = invoiceService.getInvoicesForDateRange(startDateTime, endDateTime);
    BigDecimal totalRevenue = BigDecimal.ZERO;
    
    for (Invoice invoice : invoices) {
        totalRevenue = totalRevenue.add(invoice.getFinalAmount());
    }
    
    // Calculate COGS using actual batch costs (should match the gross profit calculation)
    BigDecimal totalCogs = totalRevenue.subtract(grossProfit);
    
    // Create summary box
    VBox summaryBox = new VBox(10);
    summaryBox.getStyleClass().add("summary-box");
    
    // Create summary labels
    Label revenueLabel = new Label("Total Revenue: LKR " + formatDecimal(totalRevenue));
    revenueLabel.getStyleClass().add("summary-item");
    revenueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    
    Label cogsLabel = new Label("Cost of Goods Sold (Actual Batch Costs): LKR " + formatDecimal(totalCogs));
    cogsLabel.getStyleClass().add("summary-item");
    cogsLabel.setStyle("-fx-font-size: 14px;");
    
    Separator separator1 = new Separator();
    separator1.setPrefWidth(200);
    
    Label grossProfitLabel = new Label("Gross Profit: LKR " + formatDecimal(grossProfit));
    grossProfitLabel.getStyleClass().add("summary-item");
    grossProfitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    
    // Calculate profit margin
    double profitMargin = reportingService.calculateProfitMargin(startDateTime, endDateTime);
    
    Label profitMarginLabel = new Label("Profit Margin: " + String.format("%.2f", profitMargin) + "%");
    profitMarginLabel.getStyleClass().add("summary-item");
    profitMarginLabel.setStyle("-fx-font-size: 14px;");
    
    summaryBox.getChildren().addAll(
            revenueLabel, cogsLabel, separator1, grossProfitLabel, profitMarginLabel);
    
    // Create line chart for revenue vs cogs vs profit
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Date");
    yAxis.setLabel("Amount (LKR)");
    
    LineChart<String, Number> profitLossChart = new LineChart<>(xAxis, yAxis);
    profitLossChart.setTitle("Daily Revenue vs COGS vs Profit");
    
    // Group data by date using actual batch costs
    Map<LocalDate, BigDecimal> revenueByDate = new TreeMap<>();
    Map<LocalDate, BigDecimal> cogsByDate = new TreeMap<>();
    Map<LocalDate, BigDecimal> profitByDate = new TreeMap<>();
    
    // Group invoices by date first
    Map<LocalDate, List<Invoice>> invoicesByDate = new HashMap<>();
    
    for (Invoice invoice : invoices) {
        LocalDate date = invoice.getDate().toLocalDate();
        invoicesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(invoice);
    }
    
    // Calculate daily revenue, COGS, and profit using actual batch costs
    for (Map.Entry<LocalDate, List<Invoice>> entry : invoicesByDate.entrySet()) {
        LocalDate date = entry.getKey();
        List<Invoice> dailyInvoices = entry.getValue();
        
        BigDecimal dailyRevenue = BigDecimal.ZERO;
        BigDecimal dailyCogs = BigDecimal.ZERO;
        
        for (Invoice invoice : dailyInvoices) {
            dailyRevenue = dailyRevenue.add(invoice.getFinalAmount());
            
            // Calculate COGS using actual batch costs for each invoice item
            for (InvoiceItem item : invoice.getItems()) {
                BigDecimal itemCost = reportingService.getActualCostForInvoiceItem(item);
                dailyCogs = dailyCogs.add(itemCost);
            }
        }
        
        BigDecimal dailyProfit = dailyRevenue.subtract(dailyCogs);
        
        revenueByDate.put(date, dailyRevenue);
        cogsByDate.put(date, dailyCogs);
        profitByDate.put(date, dailyProfit);
    }
    
    // Add data to chart
    XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
    revenueSeries.setName("Revenue");
    
    XYChart.Series<String, Number> cogsSeries = new XYChart.Series<>();
    cogsSeries.setName("COGS (Actual)");
    
    XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
    profitSeries.setName("Profit");
    
    for (Map.Entry<LocalDate, BigDecimal> entry : revenueByDate.entrySet()) {
        String formattedDate = entry.getKey().format(dateFormatter);
        revenueSeries.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
    }
    
    for (Map.Entry<LocalDate, BigDecimal> entry : cogsByDate.entrySet()) {
        String formattedDate = entry.getKey().format(dateFormatter);
        cogsSeries.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
    }
    
    for (Map.Entry<LocalDate, BigDecimal> entry : profitByDate.entrySet()) {
        String formattedDate = entry.getKey().format(dateFormatter);
        profitSeries.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
    }
    
    profitLossChart.getData().addAll(revenueSeries, cogsSeries, profitSeries);
    
    // Create profit margin analysis table
    TableView<Map<String, Object>> profitTable = new TableView<>();
    
    TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>("Date");
    dateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("date")));
    
    TableColumn<Map<String, Object>, BigDecimal> revColumn = new TableColumn<>("Revenue");
    revColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("revenue")));
    
    TableColumn<Map<String, Object>, BigDecimal> costColumn = new TableColumn<>("COGS (Actual)");
    costColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("cogs")));
    
    TableColumn<Map<String, Object>, BigDecimal> profitColumn = new TableColumn<>("Profit");
    profitColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("profit")));
    
    TableColumn<Map<String, Object>, String> marginColumn = new TableColumn<>("Margin %");
    marginColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("margin")));
    
    // Format currency columns
    revColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    costColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    profitColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
                
                // Color-code profit values
                if (item.compareTo(BigDecimal.ZERO) < 0) {
                    setTextFill(Color.RED);
                } else {
                    setTextFill(Color.GREEN);
                }
            }
        }
    });
    
    profitTable.getColumns().addAll(dateColumn, revColumn, costColumn, profitColumn, marginColumn);
    
    // Prepare table data
    List<Map<String, Object>> profitData = new ArrayList<>();
    
    for (LocalDate date : revenueByDate.keySet()) {
        Map<String, Object> rowData = new HashMap<>();
        
        BigDecimal revenue = revenueByDate.get(date);
        BigDecimal cogs = cogsByDate.get(date);
        BigDecimal profit = profitByDate.get(date);
        
        String marginPct = "N/A";
        if (revenue.compareTo(BigDecimal.ZERO) > 0) {
            double margin = profit.divide(revenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            marginPct = String.format("%.2f%%", margin);
        }
        
        rowData.put("date", date.format(dateFormatter));
        rowData.put("revenue", revenue);
        rowData.put("cogs", cogs);
        rowData.put("profit", profit);
        rowData.put("margin", marginPct);
        
        profitData.add(rowData);
    }
    
    // Sort by date (chronological order)
    profitData.sort((a, b) -> {
        String dateA = (String) a.get("date");
        String dateB = (String) b.get("date");
        LocalDate localDateA = LocalDate.parse(dateA, dateFormatter);
        LocalDate localDateB = LocalDate.parse(dateB, dateFormatter);
        return localDateA.compareTo(localDateB);
    });
    
    profitTable.setItems(FXCollections.observableArrayList(profitData));
    
    // Store report data for export
    currentReportData = profitData;
    
    // Add all components to the container
    financialReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, strategyLabel, summaryBox, profitLossChart, profitTable);
}
private void generateRevenueVsExpensesReport(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // Create report title
    Label titleLabel = new Label("Revenue vs Expenses Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle with date range
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
            " to " + endDateTime.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add pricing strategy info
    String pricingStrategy = systemConfigService.getPricingStrategy();
    Label strategyLabel = new Label("Cost Method: " + pricingStrategy + " (using actual batch costs)");
    strategyLabel.getStyleClass().add("report-subtitle");
    
    // Get revenue data
    List<Invoice> invoices = invoiceService.getInvoicesForDateRange(startDateTime, endDateTime);
    BigDecimal totalRevenue = BigDecimal.ZERO;
    
    for (Invoice invoice : invoices) {
        totalRevenue = totalRevenue.add(invoice.getFinalAmount());
    }
    
    // Get expense data using actual batch costs (same as COGS calculation in other reports)
    BigDecimal totalCOGS = BigDecimal.ZERO;
    
    for (Invoice invoice : invoices) {
        for (InvoiceItem item : invoice.getItems()) {
            BigDecimal itemCost = reportingService.getActualCostForInvoiceItem(item);
            totalCOGS = totalCOGS.add(itemCost);
        }
    }
    
    // Create summary box
    VBox summaryBox = new VBox(10);
    summaryBox.getStyleClass().add("summary-box");
    
    // Create summary labels
    Label revenueLabel = new Label("Total Revenue: LKR " + formatDecimal(totalRevenue));
    revenueLabel.getStyleClass().add("summary-item");
    revenueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    
    Label expensesLabel = new Label("Total Expenses (COGS - Actual Batch Costs): LKR " + formatDecimal(totalCOGS));
    expensesLabel.getStyleClass().add("summary-item");
    expensesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    
    Separator separator = new Separator();
    separator.setPrefWidth(200);
    
    BigDecimal netProfit = totalRevenue.subtract(totalCOGS);
    Label netProfitLabel = new Label("Net Profit: LKR " + formatDecimal(netProfit));
    netProfitLabel.getStyleClass().add("summary-item");
    netProfitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    
    // Color code the net profit
    if (netProfit.compareTo(BigDecimal.ZERO) < 0) {
        netProfitLabel.setTextFill(Color.RED);
    } else {
        netProfitLabel.setTextFill(Color.GREEN);
    }
    
    summaryBox.getChildren().addAll(
            revenueLabel, expensesLabel, separator, netProfitLabel);
    
    // Create pie chart for revenue vs expenses
    PieChart revVsExpChart = new PieChart();
    revVsExpChart.setTitle("Revenue vs Expenses (Actual Costs)");
    
    PieChart.Data revenueData = new PieChart.Data(
            "Revenue (LKR " + formatDecimal(totalRevenue) + ")", totalRevenue.doubleValue());
    PieChart.Data expensesData = new PieChart.Data(
            "Expenses (LKR " + formatDecimal(totalCOGS) + ")", totalCOGS.doubleValue());
    
    revVsExpChart.getData().addAll(revenueData, expensesData);
    
    // Create bar chart for monthly comparison
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Month");
    yAxis.setLabel("Amount (LKR)");
    
    BarChart<String, Number> monthlyChart = new BarChart<>(xAxis, yAxis);
    monthlyChart.setTitle("Monthly Revenue vs Expenses (Actual Costs)");
    
    // Group data by month using actual costs
    Map<String, BigDecimal> revenueByMonth = new TreeMap<>();
    Map<String, BigDecimal> expensesByMonth = new TreeMap<>();
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
    
    for (Invoice invoice : invoices) {
        String monthKey = invoice.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String monthDisplay = YearMonth.parse(monthKey, DateTimeFormatter.ofPattern("yyyy-MM"))
                .format(monthFormatter);
        
        // Add revenue
        BigDecimal monthRevenue = revenueByMonth.getOrDefault(monthDisplay, BigDecimal.ZERO);
        monthRevenue = monthRevenue.add(invoice.getFinalAmount());
        revenueByMonth.put(monthDisplay, monthRevenue);
        
        // Calculate expenses using actual batch costs
        BigDecimal monthExpenses = expensesByMonth.getOrDefault(monthDisplay, BigDecimal.ZERO);
        
        for (InvoiceItem item : invoice.getItems()) {
            BigDecimal itemCost = reportingService.getActualCostForInvoiceItem(item);
            monthExpenses = monthExpenses.add(itemCost);
        }
        
        expensesByMonth.put(monthDisplay, monthExpenses);
    }
    
    // Add data to chart
    XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
    revenueSeries.setName("Revenue");
    
    XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
    expensesSeries.setName("Expenses (Actual)");
    
    for (String month : revenueByMonth.keySet()) {
        revenueSeries.getData().add(new XYChart.Data<>(
                month, revenueByMonth.get(month)));
        
        expensesSeries.getData().add(new XYChart.Data<>(
                month, expensesByMonth.getOrDefault(month, BigDecimal.ZERO)));
    }
    
    monthlyChart.getData().addAll(revenueSeries, expensesSeries);
    
    // Create HBox to hold the charts side by side
    HBox chartsBox = new HBox(20);
    chartsBox.setAlignment(Pos.CENTER);
    chartsBox.getChildren().addAll(revVsExpChart, monthlyChart);
    
    // Create table for monthly breakdown
    TableView<Map<String, Object>> monthlyTable = new TableView<>();
    
    TableColumn<Map<String, Object>, String> monthColumn = new TableColumn<>("Month");
    monthColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("month")));
    
    TableColumn<Map<String, Object>, BigDecimal> revColumn = new TableColumn<>("Revenue");
    revColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("revenue")));
    
    TableColumn<Map<String, Object>, BigDecimal> expColumn = new TableColumn<>("Expenses (Actual)");
    expColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("expenses")));
    
    TableColumn<Map<String, Object>, BigDecimal> profitColumn = new TableColumn<>("Profit");
    profitColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("profit")));
    
    TableColumn<Map<String, Object>, String> marginColumn = new TableColumn<>("Margin %");
    marginColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("margin")));
    
    // Format currency columns
    revColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    expColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    profitColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
                
                // Color-code profit values
                if (item.compareTo(BigDecimal.ZERO) < 0) {
                    setTextFill(Color.RED);
                } else {
                    setTextFill(Color.GREEN);
                }
            }
        }
    });
    
    monthlyTable.getColumns().addAll(monthColumn, revColumn, expColumn, profitColumn, marginColumn);
    
    // Prepare table data
    List<Map<String, Object>> monthlyData = new ArrayList<>();
    
    for (String month : revenueByMonth.keySet()) {
        Map<String, Object> rowData = new HashMap<>();
        
        BigDecimal revenue = revenueByMonth.get(month);
        BigDecimal expenses = expensesByMonth.getOrDefault(month, BigDecimal.ZERO);
        BigDecimal profit = revenue.subtract(expenses);
        
        String marginPct = "N/A";
        if (revenue.compareTo(BigDecimal.ZERO) > 0) {
            double margin = profit.divide(revenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            marginPct = String.format("%.2f%%", margin);
        }
        
        rowData.put("month", month);
        rowData.put("revenue", revenue);
        rowData.put("expenses", expenses);
        rowData.put("profit", profit);
        rowData.put("margin", marginPct);
        
        monthlyData.add(rowData);
    }
    
    monthlyTable.setItems(FXCollections.observableArrayList(monthlyData));
    
    // Store report data for export
    currentReportData = monthlyData;
    
    // Add all components to the container
    financialReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, strategyLabel, summaryBox, chartsBox, monthlyTable);
}
private void generateMarginAnalysisReport(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    // Create report title
    Label titleLabel = new Label("Margin Analysis Report");
    titleLabel.getStyleClass().add("report-title");
    
    // Create report subtitle with date range
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    Label subtitleLabel = new Label("From " + startDateTime.format(dateFormatter) + 
            " to " + endDateTime.format(dateFormatter));
    subtitleLabel.getStyleClass().add("report-subtitle");
    
    // Add pricing strategy info
    String pricingStrategy = systemConfigService.getPricingStrategy();
    Label strategyLabel = new Label("Cost Calculation Method: " + pricingStrategy + " (using actual batch costs)");
    strategyLabel.getStyleClass().add("report-subtitle");
    
    // Get product sales with actual batch costs
    Map<Product, Map<String, Object>> productSales = 
            reportingService.getProductSales(startDateTime, endDateTime);
    
    Map<Product, Double> productMargins = 
            reportingService.getProductProfitMargins(startDateTime, endDateTime);
    
    // Create summary box
    VBox summaryBox = new VBox(5);
    summaryBox.getStyleClass().add("summary-box");
    
    // Calculate overall margin using actual costs
    BigDecimal totalSales = BigDecimal.ZERO;
    BigDecimal totalCost = BigDecimal.ZERO;
    
    for (Map.Entry<Product, Map<String, Object>> entry : productSales.entrySet()) {
        Map<String, Object> salesData = entry.getValue();
        
        BigDecimal salesValue = (BigDecimal) salesData.get("salesValue");
        BigDecimal costValue = (BigDecimal) salesData.getOrDefault("totalCost", BigDecimal.ZERO);
        
        totalSales = totalSales.add(salesValue);
        totalCost = totalCost.add(costValue);
    }
    
    BigDecimal totalProfit = totalSales.subtract(totalCost);
    double overallMargin = 0;
    
    if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
        overallMargin = totalProfit.divide(totalSales, 4, ROUNDING_MODE)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }
    
    // Create summary labels
    Label totalSalesLabel = new Label("Total Sales: LKR " + formatDecimal(totalSales));
    totalSalesLabel.getStyleClass().add("summary-item");
    
    Label totalCostLabel = new Label("Total Cost (Actual Batch Costs): LKR " + formatDecimal(totalCost));
    totalCostLabel.getStyleClass().add("summary-item");
    
    Label totalProfitLabel = new Label("Total Profit: LKR " + formatDecimal(totalProfit));
    totalProfitLabel.getStyleClass().add("summary-item");
    
    Label overallMarginLabel = new Label("Overall Profit Margin: " + 
            String.format("%.2f%%", overallMargin));
    overallMarginLabel.getStyleClass().add("summary-item");
    
    summaryBox.getChildren().addAll(
            totalSalesLabel, totalCostLabel, totalProfitLabel, overallMarginLabel);
    
    // Create margin distribution chart
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Margin Range (%)");
    yAxis.setLabel("Number of Products");
    
    BarChart<String, Number> marginDistributionChart = new BarChart<>(xAxis, yAxis);
    marginDistributionChart.setTitle("Product Margin Distribution");
    
    // Define margin ranges
    String[] marginRanges = {
        "Negative", "0-10%", "10-20%", "20-30%", "30-40%", "40-50%", "50%+"
    };
    
    int[] marginCounts = new int[marginRanges.length];
    
    // Count products in each margin range
    for (Double margin : productMargins.values()) {
        if (margin < 0) {
            marginCounts[0]++; // Negative
        } else if (margin < 10) {
            marginCounts[1]++; // 0-10%
        } else if (margin < 20) {
            marginCounts[2]++; // 10-20%
        } else if (margin < 30) {
            marginCounts[3]++; // 20-30%
        } else if (margin < 40) {
            marginCounts[4]++; // 30-40%
        } else if (margin < 50) {
            marginCounts[5]++; // 40-50%
        } else {
            marginCounts[6]++; // 50%+
        }
    }
    
    // Add data to chart
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Products");
    
    for (int i = 0; i < marginRanges.length; i++) {
        series.getData().add(new XYChart.Data<>(marginRanges[i], marginCounts[i]));
    }
    
    marginDistributionChart.getData().add(series);
    
    // Create table for product margins with actual costs
    TableView<Map<String, Object>> marginTable = new TableView<>();
    
    TableColumn<Map<String, Object>, String> productColumn = new TableColumn<>("Product");
    productColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("product")));
    
    TableColumn<Map<String, Object>, String> categoryColumn = new TableColumn<>("Category");
    categoryColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("category")));
    
    TableColumn<Map<String, Object>, BigDecimal> salesColumn = new TableColumn<>("Sales");
    salesColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("sales")));
    
    TableColumn<Map<String, Object>, BigDecimal> costColumn = new TableColumn<>("Actual Cost");
    costColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("cost")));
    
    TableColumn<Map<String, Object>, BigDecimal> profitColumn = new TableColumn<>("Profit");
    profitColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("profit")));
    
    TableColumn<Map<String, Object>, BigDecimal> marginPctColumn = new TableColumn<>("Margin %");
    marginPctColumn.setCellValueFactory(data -> 
            new SimpleObjectProperty<>((BigDecimal) data.getValue().get("marginPercentage")));
    
    // Format currency columns
    salesColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    costColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
            }
        }
    });
    
    profitColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText("LKR " + formatDecimal(item));
                
                // Color-code profit values
                if (item.compareTo(BigDecimal.ZERO) < 0) {
                    setTextFill(Color.RED);
                } else {
                    setTextFill(Color.GREEN);
                }
            }
        }
    });
    
    // Format margin percentage column
    marginPctColumn.setCellFactory(column -> new TableCell<Map<String, Object>, BigDecimal>() {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(String.format("%.2f%%", item));
                
                // Color-code margin values
                if (item.compareTo(BigDecimal.ZERO) < 0) {
                    setTextFill(Color.RED);
                } else if (item.compareTo(new BigDecimal("20")) < 0) {
                    setTextFill(Color.ORANGE);
                } else {
                    setTextFill(Color.GREEN);
                }
            }
        }
    });
    
    marginTable.getColumns().addAll(productColumn, categoryColumn, 
            salesColumn, costColumn, profitColumn, marginPctColumn);
    
    // Prepare table data with actual costs
    List<Map<String, Object>> marginData = new ArrayList<>();
    
    for (Map.Entry<Product, Map<String, Object>> entry : productSales.entrySet()) {
        Product product = entry.getKey();
        Map<String, Object> salesData = entry.getValue();
        
        BigDecimal salesValue = (BigDecimal) salesData.get("salesValue");
        BigDecimal costValue = (BigDecimal) salesData.getOrDefault("totalCost", BigDecimal.ZERO);
        BigDecimal profit = salesValue.subtract(costValue);
        
        BigDecimal marginPercentage = BigDecimal.ZERO;
        if (salesValue.compareTo(BigDecimal.ZERO) > 0) {
            marginPercentage = profit.divide(salesValue, 4, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
        }
        
        Map<String, Object> rowData = new HashMap<>();
        rowData.put("product", product.getName());
        rowData.put("category", product.getCategory() != null ? 
                product.getCategory().getName() : "Uncategorized");
        rowData.put("sales", salesValue);
        rowData.put("cost", costValue);
        rowData.put("profit", profit);
        rowData.put("marginPercentage", marginPercentage);
        
        marginData.add(rowData);
    }
    
    // Sort by margin (highest first)
    marginData.sort((m1, m2) -> {
        BigDecimal margin1 = (BigDecimal) m1.get("marginPercentage");
        BigDecimal margin2 = (BigDecimal) m2.get("marginPercentage");
        return margin2.compareTo(margin1);
    });
    
    marginTable.setItems(FXCollections.observableArrayList(marginData));
    
    // Store report data for export
    currentReportData = marginData;
    
    // Add all components to the container
    financialReportContainer.getChildren().addAll(
            titleLabel, subtitleLabel, strategyLabel, summaryBox, marginDistributionChart, marginTable);
}


// Add these helper methods at the end of ReportingController class

/**
 * Format decimal values consistently
 */
private String formatDecimal(BigDecimal value) {
    if (value == null) {
        return "0.00";
    }
    return value.setScale(DECIMAL_SCALE, ROUNDING_MODE).toString();
}

/**
 * Format currency values consistently
 */
private String formatCurrency(BigDecimal value) {
    if (value == null) {
        return "LKR 0.00";
    }
    return "LKR " + formatDecimal(value);
}

/**
 * Add pricing strategy label to reports that use batch costs
 */
private Label createPricingStrategyLabel() {
    String strategy = systemConfigService.getPricingStrategy();
    Label label = new Label("Cost Method: " + strategy + " (Actual Batch Costs)");
    label.getStyleClass().add("info-label");
    return label;
}



@FXML
private void handleExportFinancialReport(ActionEvent event) {
    if (currentReportData == null) {
        AlertHelper.showErrorAlert("No Report Data", "No report data available to export", 
                "Please generate a report first before attempting to export.");
        return;
    }
    
    exportReport(currentReportData, "financial_report_" + currentReportType.toLowerCase().replace(" ", "_"));
}


//private String formatDecimal(BigDecimal value) {
//    if (value == null) {
//        return "0.00";
//    }
//    return value.setScale(2, RoundingMode.HALF_UP).toString();
//}
//
//// Helper method for formatting BigDecimal as currency
//private String formatCurrency(BigDecimal value) {
//    if (value == null) {
//        return "LKR 0.00";
//    }
//    return "LKR " + value.setScale(2, RoundingMode.HALF_UP).toString();
//}




  
    
    // Add this method to ReportingController
public void selectTab(int tabIndex) {
    TabPane tabPane = (TabPane) reportsPane.getCenter();
    tabPane.getSelectionModel().select(tabIndex);
}
}