package com.nimesh.controller;

import com.nimesh.model.ActivityLog;
import com.nimesh.model.Category;
import com.nimesh.model.CreditAccount;
import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.model.Product;
import com.nimesh.service.ActivityLogService;
import com.nimesh.service.CategoryService;
import com.nimesh.service.CustomerService;
import com.nimesh.service.InvoiceService;
import com.nimesh.service.ProductService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.SessionManager;
import com.nimesh.util.SidebarStateManager;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import com.nimesh.util.StageManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.Alert;

@Controller
public class DashboardController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox dashboardContent;
    
    @FXML
    private VBox sidebarVBox;
    
    @FXML
    private VBox menuContainer;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label userLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label todaySalesLabel;

    @FXML
    private Label salesChangeLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label stockChangeLabel;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private Label customersChangeLabel;

    @FXML
    private Label pendingCreditsLabel;

    @FXML
    private Label creditsChangeLabel;

    @FXML
    private BarChart<String, Number> monthlySalesChart;

    @FXML
    private PieChart categoriesChart;

    @FXML
    private VBox recentActivitiesContainer;
    
    @FXML
    private Button toggleSidebarBtn;
    
    @FXML
    private Button expandSidebarBtn;
    
    @FXML
    private Label logoText;
    
    @FXML
    private Label menuSectionLabel;
    
    @FXML
    private Label reportsSectionLabel;
    
    @FXML
    private Label settingsSectionLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button inventoryBtn;

    @FXML
    private Button posBtn;

    @FXML
    private Button customersBtn;

    @FXML
    private Button suppliersBtn;

    @FXML
    private Button salesReportBtn;

    @FXML
    private Button inventoryReportBtn;

    @FXML
    private Button customerReportBtn;

    @FXML
    private Button financialReportBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button logoutBtn;

    @Autowired
    private StageManager stageManager;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private SidebarStateManager sidebarStateManager;

    @FXML
    private Button notificationsBtn;
    
    @FXML
private Button batchManagementBtn;
    
    @FXML
public void handleBatchManagementAction(ActionEvent event) {
    // Switch to batch management screen
    setActiveButton(batchManagementBtn);
    loadScreen("/fxml/batch_management.fxml");
    pageTitleLabel.setText("Product Batch Management");
}

@FXML
public void handleSettingsAction(ActionEvent event) {
    // Switch to settings screen
    setActiveButton(settingsBtn);
    loadScreen("/fxml/settings.fxml");
    pageTitleLabel.setText("Settings");
}



    @FXML
    public void handleToggleSidebar(ActionEvent event) {
        // Set sidebar state to collapsed in the manager
        sidebarStateManager.setSidebarCollapsed(true);
        
        // Apply collapsed style to sidebar
        sidebarVBox.getStyleClass().add("collapsed");
        
        // Update button visibility
        toggleSidebarBtn.setVisible(false);
        expandSidebarBtn.setVisible(true);
        
        // Update toggle button icon (left arrow becomes right arrow)
        Label toggleIcon = (Label) toggleSidebarBtn.getGraphic();
        toggleIcon.setText("\uf054"); // FontAwesome right arrow
        
        // Update tooltip
        Tooltip.install(toggleSidebarBtn, new Tooltip("Expand Sidebar"));
        
        // Update menu buttons state
        updateSidebarButtonsState();
    }
    
    @FXML
    public void handleExpandSidebar(ActionEvent event) {
        // Set sidebar state to expanded in the manager
        sidebarStateManager.setSidebarCollapsed(false);
        
        // Remove collapsed style from sidebar
        sidebarVBox.getStyleClass().remove("collapsed");
        
        // Update button visibility
        toggleSidebarBtn.setVisible(true);
        expandSidebarBtn.setVisible(false);
        
        // Update toggle button icon (right arrow becomes left arrow)
        Label toggleIcon = (Label) toggleSidebarBtn.getGraphic();
        toggleIcon.setText("\uf053"); // FontAwesome left arrow
        
        // Update tooltip
        Tooltip.install(toggleSidebarBtn, new Tooltip("Collapse Sidebar"));
        
        // Update menu buttons state
        updateSidebarButtonsState();
    }

    @FXML
    public void handleNotificationsAction(ActionEvent event) {
        // Switch to notifications screen
        setActiveButton(notificationsBtn);
        loadScreen("/fxml/notifications.fxml");
        pageTitleLabel.setText("SMS Notifications");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        
        
         // Check if user is authorized to access dashboard
    if (!SessionManager.getInstance().isAdmin()) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Unauthorized Access");
            alert.setContentText("You do not have permission to access the dashboard.");
            alert.showAndWait();
            
            // Return to login screen
            stageManager.showLoginScreen();
        });
        return;
    }
    
    // Set current user
    String username = SessionManager.getInstance().getCurrentUsername();
    userLabel.setText(username);
        
        
        
        

// Load FontAwesome
        Font.loadFont(getClass().getResource("/fonts/fontawesome-webfont.ttf").toExternalForm(), 10);

        // Set current user
        userLabel.setText("Admin User");

        // Start clock for date and time
        startClock();
        
        // Initialize sidebar toggle
        initSidebarToggle();

        // Initialize dashboard metrics
        initializeMetrics();

        // Set up charts
        initializeCharts();

        // Create sample recent activities
        createRecentActivities();

        // Set the dashboard button as selected
        setActiveButton(dashboardBtn);
    }
    
    private void initSidebarToggle() {
        // Apply the persisted sidebar state
        boolean isCollapsed = sidebarStateManager.isSidebarCollapsed();
        
        // Configure UI based on persisted state
        if (isCollapsed) {
            // Apply collapsed style to sidebar
            sidebarVBox.getStyleClass().add("collapsed");
            
            // Update button visibility
            toggleSidebarBtn.setVisible(false);
            expandSidebarBtn.setVisible(true);
            
            // Update tooltip
            Tooltip.install(toggleSidebarBtn, new Tooltip("Expand Sidebar"));
        } else {
            // Ensure sidebar is expanded
            sidebarVBox.getStyleClass().remove("collapsed");
            
            // Update button visibility
            toggleSidebarBtn.setVisible(true);
            expandSidebarBtn.setVisible(false);
            
            // Update tooltip
            Tooltip.install(toggleSidebarBtn, new Tooltip("Collapse Sidebar"));
        }
        
        // Configure toggles
        toggleSidebarBtn.setTooltip(new Tooltip("Collapse Sidebar"));
        expandSidebarBtn.setTooltip(new Tooltip("Expand Sidebar"));
        
        // Update menu buttons based on current state
        updateSidebarButtonsState();
    }
    
    /**
     * Utility method to update button states and menu items when sidebar is collapsed/expanded
     * This ensures proper display of menu items in both states
     */
    private void updateSidebarButtonsState() {
        // Collect all menu buttons
        List<Button> menuButtons = List.of(
            dashboardBtn, inventoryBtn, posBtn, customersBtn, suppliersBtn,
            salesReportBtn, inventoryReportBtn, customerReportBtn, financialReportBtn,
            notificationsBtn, settingsBtn, logoutBtn
        );
        
        // Update tooltip visibility and text display based on sidebar state
        for (Button button : menuButtons) {
            if (sidebarStateManager.isSidebarCollapsed()) {
                // When collapsed, ensure tooltips show the button name
                String buttonText = ((Label)button.getGraphic()).getText();
                String tooltipText = button.getText();
                
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(button, tooltip);
            } else {
                // When expanded, tooltips aren't needed for text buttons
                Tooltip.uninstall(button, button.getTooltip());
            }
        }
    }
    
private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy | hh:mm a");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void initializeMetrics() {
        // Get real data from services

        // Today's sales
        BigDecimal todaySales = invoiceService.getTodaysSales();
        todaySalesLabel.setText("LKR " + todaySales.toString());

        // Sales change compared to yesterday
        BigDecimal yesterdaySales = invoiceService.getSalesForDate(LocalDate.now().minusDays(1));
        String salesChangeText;

        if (yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate percentage change
            BigDecimal difference = todaySales.subtract(yesterdaySales);
            BigDecimal percentChange = difference.multiply(new BigDecimal("100")).divide(yesterdaySales, 1, BigDecimal.ROUND_HALF_UP);

            if (percentChange.compareTo(BigDecimal.ZERO) >= 0) {
                salesChangeText = "+" + percentChange + "% from yesterday";
            } else {
                salesChangeText = percentChange + "% from yesterday";
            }
        } else {
            salesChangeText = "No sales yesterday";
        }
        salesChangeLabel.setText(salesChangeText);

        // Low stock items count
        List<Product> lowStockItems = productService.getLowStockProducts();
        lowStockLabel.setText(String.valueOf(lowStockItems.size()));

        // Change in low stock items
        // We're assuming you could track this through a record of previous counts
        // For simplicity, we'll just use a placeholder
        stockChangeLabel.setText("Items need attention");

        // Total customers count
        List<Customer> allCustomers = customerService.getAllCustomers();
        totalCustomersLabel.setText(String.valueOf(allCustomers.size()));

        // Customers added today
        // This would require tracking customer creation dates
        // For now, we'll use a placeholder
        customersChangeLabel.setText("Active customers");

        // Pending credits
        BigDecimal totalCredits = customerService.getTotalOutstandingCredit();
        pendingCreditsLabel.setText("LKR " + totalCredits.toString());

        // Customers with outstanding balance
        List<CreditAccount> creditAccounts = customerService.getCustomersWithOutstandingCredit();
        creditsChangeLabel.setText(creditAccounts.size() + " customers");
    }

    private void initializeCharts() {
        // Monthly Sales Chart - Using real data from past 6 months
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        LocalDate currentDate = LocalDate.now();

        // Get data for last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = currentDate.minusMonths(i).withDayOfMonth(1);
            String monthName = monthStart.format(monthFormatter);

            BigDecimal monthlySales = invoiceService.getSalesForDateRange(
                    monthStart.atStartOfDay(),
                    monthStart.plusMonths(1).minusDays(1).atTime(23, 59, 59)
            );

            series.getData().add(new XYChart.Data<>(monthName, monthlySales));
        }

        monthlySalesChart.getData().clear();
        monthlySalesChart.getData().add(series);

        // Product Categories Chart - Using real category distribution
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Get all products grouped by category
        Map<Category, Long> productsByCategory = productService.getProductCountByCategory();

        // If no products with categories exist, add a placeholder
        if (productsByCategory.isEmpty()) {
            pieChartData.add(new PieChart.Data("No Categories", 1));
        } else {
            // Add an "Uncategorized" slice for products without a category
            long uncategorizedCount = productService.getProductsWithoutCategory().size();
            if (uncategorizedCount > 0) {
                pieChartData.add(new PieChart.Data("Uncategorized", uncategorizedCount));
            }

            // Add all categories with products
            for (Map.Entry<Category, Long> entry : productsByCategory.entrySet()) {
                if (entry.getKey() != null) {
                    pieChartData.add(new PieChart.Data(entry.getKey().getName(), entry.getValue()));
                }
            }
        }

        categoriesChart.setData(pieChartData);

        // Add legend to the chart
        categoriesChart.setLegendVisible(true);
    }

    private void createRecentActivities() {
        // Try to get activities from activity_logs table first
        List<ActivityLog> recentLogs = null;
        boolean activityLogsAvailable = false;

        try {
            // Check if activityLogService is available and can retrieve logs
            if (activityLogService != null) {
                recentLogs = activityLogService.getRecentActivities(10);
                activityLogsAvailable = (recentLogs != null && !recentLogs.isEmpty());
            }
        } catch (Exception e) {
            // If any error occurs, log it but continue with fallback options
            System.err.println("Error retrieving activity logs: " + e.getMessage());
            activityLogsAvailable = false;
        }

        recentActivitiesContainer.getChildren().clear();

        if (activityLogsAvailable) {
            // Use activity logs if available
            for (ActivityLog log : recentLogs) {
                addActivityItem(log.getDescription(), formatTimeAgo(log.getTimestamp()));
            }
        } else {
            // Fall back to other sources of activity
            List<Invoice> recentInvoices = invoiceService.getRecentInvoices(5);
            List<Product> lowStockProducts = productService.getLowStockProducts();

            // If we have real activities from these sources, show them
            if (!recentInvoices.isEmpty() || !lowStockProducts.isEmpty()) {
                // Add recent invoices
                for (Invoice invoice : recentInvoices) {
                    addActivityItem(
                            "Invoice #" + invoice.getInvoiceNumber() + " - "
                            + (invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Walk-in Customer")
                            + " - LKR " + invoice.getFinalAmount(),
                            formatTimeAgo(invoice.getDate())
                    );
                }

                // Add low stock alerts
                for (Product product : lowStockProducts) {
                    if (product.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0) {
                        addActivityItem(
                                "Out of Stock Alert: " + product.getName() + " - Restock needed!",
                                "Urgent"
                        );
                    } else if (product.getCurrentStock().compareTo(product.getReorderLevel().divide(new BigDecimal("2"))) <= 0) {
                        addActivityItem(
                                "Critical Stock Alert: " + product.getName() + " - Only "
                                + product.getCurrentStock() + " units left",
                                "Important"
                        );
                    } else {
                        addActivityItem(
                                "Low Stock Alert: " + product.getName() + " - Only "
                                + product.getCurrentStock() + " units left (Reorder level: "
                                + product.getReorderLevel() + ")",
                                "Attention needed"
                        );
                    }
                }
            } else {
                // Last resort: Use sample activities if no real data available
                String[] activities = {
                    "New wholesale order #1256 from Kumar Enterprises - LKR12,500",
                    "Low stock alert: Rice Basmati 5kg - Only 3 units left",
                    "New customer registered: Priya Sharma",
                    "Credit payment received from Rajesh Stores - LKR5,000",
                    "Inventory updated: 50 units of Soap added",
                    "Order #1255 delivered to customer",
                    "Low stock alert: Cooking Oil 1L - Only 5 units left",
                    "New supplier added: Global Imports Ltd."
                };

                String[] times = {
                    "10 minutes ago",
                    "35 minutes ago",
                    "2 hours ago",
                    "5 hours ago",
                    "Yesterday, 4:30 PM",
                    "Yesterday, 2:15 PM",
                    "Yesterday, 11:20 AM",
                    "2 days ago"
                };

                for (int i = 0; i < activities.length; i++) {
                    addActivityItem(activities[i], times[i]);
                }
            }
        }
    }

    private void addActivityItem(String activity, String time) {
        HBox activityBox = new HBox();
        activityBox.getStyleClass().add("activity-item");
        activityBox.setSpacing(10);

        Label activityText = new Label(activity);
        activityText.getStyleClass().add("activity-text");
        HBox.setHgrow(activityText, Priority.ALWAYS);

        Label activityTime = new Label(time);
        activityTime.getStyleClass().add("activity-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        activityBox.getChildren().addAll(activityText, spacer, activityTime);

        recentActivitiesContainer.getChildren().add(activityBox);
        VBox.setMargin(activityBox, new Insets(0, 0, 5, 0));
    }

    private String formatTimeAgo(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "Unknown time";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(timestamp, now);

        if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 24 * 60) {
            long hours = java.time.temporal.ChronoUnit.HOURS.between(timestamp, now);
            return hours + " hours ago";
        } else if (minutes < 48 * 60) {
            return "Yesterday, " + timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else {
            long days = java.time.temporal.ChronoUnit.DAYS.between(timestamp, now);
            return days + " days ago";
        }
    }

    /**
     * Refreshes all data displayed on the dashboard
     */
    private void refreshDashboardData() {
        initializeMetrics();
        initializeCharts();
        createRecentActivities();
    }

    @FXML
    public void handleDashboardAction(ActionEvent event) {
        // Show dashboard content
        setActiveButton(dashboardBtn);
        showDashboardContent();
        pageTitleLabel.setText("Dashboard");

        // Refresh all dashboard data
        refreshDashboardData();
    }

    @FXML
    public void handleInventoryAction(ActionEvent event) {
        // Switch to inventory screen
        setActiveButton(inventoryBtn);
        loadScreen("/fxml/inventory.fxml");
        pageTitleLabel.setText("Inventory Management");
    }

    @FXML
    public void handlePOSAction(ActionEvent event) {
        // Switch to POS screen
        setActiveButton(posBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pos.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            // Replace the center content with the POS screen
            mainBorderPane.setCenter(root);
            pageTitleLabel.setText("Point of Sale");
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not load POS screen",
                    "An error occurred while trying to load the Point of Sale screen.");
        }
    }

    @FXML
    public void handleCustomersAction(ActionEvent event) {
        // Switch to customers screen
        setActiveButton(customersBtn);
        loadScreen("/fxml/customer_management.fxml");
        pageTitleLabel.setText("Customer Management");
    }

    @FXML
    public void handleSuppliersAction(ActionEvent event) {
        // Switch to suppliers screen
        setActiveButton(suppliersBtn);
        loadScreen("/fxml/supplier_management.fxml");
        pageTitleLabel.setText("Supplier Management");
    }

    @FXML
    public void handleSalesReportAction(ActionEvent event) {
        // Switch to Sales Reports screen
        setActiveButton(salesReportBtn);
        loadScreen("/fxml/reporting.fxml");
        pageTitleLabel.setText("Reports & Analytics");
    }

    @FXML
    public void handleInventoryReportAction(ActionEvent event) {
        // Switch to Inventory Reports screen (use the same reporting fxml, just select the tab)
        setActiveButton(inventoryReportBtn);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reporting.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            ReportingController controller = loader.getController();
            controller.selectTab(1); // 1 is the index for Inventory Reports tab

            // Replace the center content with the reporting screen
            mainBorderPane.setCenter(root);
            pageTitleLabel.setText("Inventory Reports");

        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not load reporting screen",
                    "An error occurred while trying to load the reporting screen.");
        }
    }

    @FXML
    private void handleCustomerReportAction(ActionEvent event) {
        // Switch to Customer Reports screen (use the same reporting fxml, just select the tab)
        setActiveButton(customerReportBtn);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reporting.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            ReportingController controller = loader.getController();
            controller.selectTab(2); // 2 is the index for Customer Reports tab

            // Replace the center content with the reporting screen
            mainBorderPane.setCenter(root);
            pageTitleLabel.setText("Customer Reports");

        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not load reporting screen",
                    "An error occurred while trying to load the reporting screen.");
        }
    }

    @FXML
    private void handleFinancialReportAction(ActionEvent event) {
        // Switch to Financial Reports screen (use the same reporting fxml, just select the tab)
        setActiveButton(financialReportBtn);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reporting.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            ReportingController controller = loader.getController();
            controller.selectTab(3); // 3 is the index for Financial Reports tab

            // Replace the center content with the reporting screen
            mainBorderPane.setCenter(root);
            pageTitleLabel.setText("Financial Reports");

        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not load reporting screen",
                    "An error occurred while trying to load the reporting screen.");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        stageManager.showLoginScreen();
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("active-menu-button");
        inventoryBtn.getStyleClass().remove("active-menu-button");
        batchManagementBtn.getStyleClass().remove("active-menu-button");
        posBtn.getStyleClass().remove("active-menu-button");
        customersBtn.getStyleClass().remove("active-menu-button");
        suppliersBtn.getStyleClass().remove("active-menu-button");
        salesReportBtn.getStyleClass().remove("active-menu-button");
        inventoryReportBtn.getStyleClass().remove("active-menu-button");
        settingsBtn.getStyleClass().remove("active-menu-button");
        customerReportBtn.getStyleClass().remove("active-menu-button");
        financialReportBtn.getStyleClass().remove("active-menu-button");
        notificationsBtn.getStyleClass().remove("active-menu-button");
        
        // Add active class to current button
        activeButton.getStyleClass().add("active-menu-button");
    }

    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent screenRoot = loader.load();

            // Replace the center content with the new screen
            mainBorderPane.setCenter(screenRoot);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error dialog in real application
            AlertHelper.showErrorAlert("Error", "Could not load screen",
                    "An error occurred while trying to load the requested screen.");
        }
    }

    private void showDashboardContent() {
        // Show the dashboard content (restore it)
        mainBorderPane.setCenter(dashboardContent);

        // Refresh all dashboard data
        refreshDashboardData();
    }
    
    
}