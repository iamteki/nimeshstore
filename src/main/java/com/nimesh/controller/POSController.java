package com.nimesh.controller;

import com.nimesh.model.*;
import com.nimesh.service.CustomerService;
import com.nimesh.service.InvoiceService;
import com.nimesh.service.ProductBatchService;
import com.nimesh.service.ProductService;
import com.nimesh.service.SystemConfigService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.DecimalFormatter;
import com.nimesh.util.ReceiptPrinter;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Window;


import com.nimesh.util.SessionManager;
import com.nimesh.util.StageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

@Controller
public class POSController implements Initializable {

    
     @Autowired
private ProductBatchService productBatchService;
    
    
    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ApplicationContext context;

    @FXML
    private BorderPane posPane;

    @FXML
    private TextField barcodeField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Product> searchResultsTable;

    @FXML
    private TableColumn<Product, Long> resultIdColumn;

    @FXML
    private TableColumn<Product, String> resultNameColumn;

    @FXML
    private TableColumn<Product, BigDecimal> resultPriceColumn;

    @FXML
    private Button addToCartButton;

    @FXML
    private RadioButton retailRadio;

    @FXML
    private RadioButton wholesaleRadio;

    @FXML
    private ToggleGroup customerType;

    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private Button newCustomerButton;

    @FXML
    private Label subTotalLabel;

    @FXML
    private TextField discountField;

    @FXML
    private Label totalLabel;

    @FXML
    private RadioButton cashRadio;

    @FXML
    private RadioButton creditRadio;

    @FXML
    private ToggleGroup paymentMethod;

    @FXML
    private Button clearButton;

    @FXML
    private Button checkoutButton;

    @FXML
    private Label dateLabel;

    @FXML
    private Label invoiceLabel;

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, Integer> cartIndexColumn;

    @FXML
    private TableColumn<CartItem, String> cartNameColumn;

    @FXML
    private TableColumn<CartItem, String> cartUnitColumn;

    @FXML
    private TableColumn<CartItem, BigDecimal> cartPriceColumn;

    @FXML
    private TableColumn<CartItem, BigDecimal> cartQuantityColumn;

    @FXML
    private TableColumn<CartItem, BigDecimal> cartTotalColumn;

    @FXML
    private TableColumn<CartItem, Button> cartActionColumn;

    private BarcodeScannerHandler barcodeScannerHandler;

    @FXML
    private Button scanBarcodeButton;

    // New fields
    @FXML
    private TextField cashReceivedField;

    @FXML
    private Label cashReceivedLabel;

    @FXML
    private Label changeLabel;

    @FXML
    private Label changeAmountLabel;

    @FXML
    private TableColumn<CartItem, BigDecimal> cartDiscountColumn;

    @FXML
    private Label itemDiscountsLabel;

    @FXML
    private Button helpButton;

    private ObservableList<Product> searchResults = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    
    @FXML
private Label userLabel;

@FXML
private Label dateTimeLabel;

@FXML
private Button logoutButton;

 @Autowired
    private StageManager stageManager; // Yo
 
 
 @Autowired
private SystemConfigService configService;

/**
     * Handles the logout action
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        // Clear the session
        SessionManager.getInstance().clearSession();
        
        // Return to login screen
        stageManager.showLoginScreen();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        if (userLabel != null) {
        userLabel.setText(SessionManager.getInstance().getCurrentUsername());
    }
        
          // Start clock if label exists (for employee POS view)
    if (dateTimeLabel != null) {
        startClock();
    }
    
     if (!SessionManager.getInstance().isAdmin() && !SessionManager.getInstance().isEmployee()) {
        Platform.runLater(() -> {
            AlertHelper.showErrorAlert("Access Denied", 
                "Unauthorized Access", 
                "You do not have permission to access this screen.");
            handleLogout(new ActionEvent());
        });
        return;
    }
     
      // Load FontAwesome font explicitly - this ensures it works regardless of how pos.css is loaded
    Font.loadFont(getClass().getResource("/fonts/fontawesome-webfont.ttf").toExternalForm(), 10);
    
        
        setupCashFields();

        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        dateLabel.setText("Date: " + LocalDate.now().format(dateFormatter));

        // Setup credit payment option
        setupCreditPaymentOption();

        // Generate invoice number
        Invoice dummyInvoice = new Invoice();
        dummyInvoice.setInvoiceNumber(invoiceService.generateInvoiceNumber());
        invoiceLabel.setText("Invoice #: " + dummyInvoice.getInvoiceNumber());

        // Initialize tables
        initializeSearchResultsTable();
        initializeCartTable();

        // Set up customer selection
        loadCustomers();
        customerType.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isWholesale = wholesaleRadio.isSelected();
                loadCustomers(isWholesale ? "WHOLESALE" : "RETAIL");
                updatePrices(); // Update prices based on customer type
            }
        });

        // Load default customers
        customerService.initializeDefaultCustomers();
        loadCustomers();

        // Add change listener to discount field
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                // Allow empty field
                if (newVal.isEmpty()) {
                    discountField.setText("0");
                    return;
                }

                // Only allow numbers and decimal point
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    discountField.setText(oldVal);
                    return;
                }

                // Update total when discount changes
                updateTotals();
            } catch (Exception e) {
                discountField.setText("0");
            }
        });

        // Initialize search
        barcodeField.setOnAction(this::handleBarcodeEnter);

        // Disable checkout button until items are added
        checkoutButton.setDisable(true);

        // Disable "Add to Cart" button until a product is selected
        addToCartButton.setDisable(true);
        searchResultsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> addToCartButton.setDisable(newSelection == null));

        // Initialize barcode scanner handler
        barcodeScannerHandler = new BarcodeScannerHandler();
        barcodeScannerHandler.setPosController(this);

        // Set up scene-wide key event handler for hardware barcode scanner
        Platform.runLater(() -> {
            barcodeField.getScene().addEventFilter(KeyEvent.KEY_TYPED, event
                    -> barcodeScannerHandler.processKeyEvent(event, barcodeField));
        });

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup tab order
        setupTabOrder();

        // Add shortcut hints
        addShortcutHints();

        // Setup help button
        if (helpButton != null) {
            helpButton.setOnAction(event -> showKeyboardShortcutsDialog());
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

    
    
   
    

    private void setupCashFields() {
        // Initially set visibility based on payment method
        cashReceivedField.setVisible(cashRadio.isSelected());
        cashReceivedLabel.setVisible(cashRadio.isSelected());
        changeLabel.setVisible(cashRadio.isSelected());
        changeAmountLabel.setVisible(cashRadio.isSelected());

        // Add listeners to payment method toggles
        cashRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cashReceivedField.setVisible(newVal);
            cashReceivedLabel.setVisible(newVal);
            changeLabel.setVisible(newVal);
            changeAmountLabel.setVisible(newVal);

            // Clear cash received and change when switching to credit
            if (!newVal) {
                cashReceivedField.clear();
                changeAmountLabel.setText("LKR 0.00");
            }
        });

        // Add listener to cash received field
        cashReceivedField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                // Allow empty field
                if (newVal.isEmpty()) {
                    changeAmountLabel.setText("LKR 0.00");
                    return;
                }

                // Only allow numbers and decimal point
                if (!newVal.matches("\\d*\\.?\\d*")) {
                    cashReceivedField.setText(oldVal);
                    return;
                }

                // Calculate change
                BigDecimal cashReceived = new BigDecimal(newVal);
                BigDecimal total = extractAmountFromLabel(totalLabel.getText());

                BigDecimal change = cashReceived.subtract(total);

                // Update change label (negative change is not allowed)
                if (change.compareTo(BigDecimal.ZERO) < 0) {
                    changeAmountLabel.setText("INSUFFICIENT");
                    changeAmountLabel.setStyle("-fx-text-fill: red;");
                } else {
                    changeAmountLabel.setText(DecimalFormatter.formatCurrency(change));
                    changeAmountLabel.setStyle("-fx-text-fill: green;");
                }
            } catch (Exception e) {
                changeAmountLabel.setText("LKR 0.00");
            }
        });
    }

    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            Scene scene = posPane.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown()) {
                    switch (event.getCode()) {
                        case F:  // Ctrl+F for searching
                            barcodeField.requestFocus();
                            event.consume();
                            break;
                        case A:  // Ctrl+A for adding to cart
                            if (!addToCartButton.isDisabled()) {
                                addToCart();
                                event.consume();
                            }
                            break;
                        case C:  // Ctrl+C for checkout
                            if (!checkoutButton.isDisabled()) {
                                handleCheckout(new ActionEvent());
                                event.consume();
                            }
                            break;
                        case X:  // Ctrl+X for clearing cart
                            handleClearCart(new ActionEvent());
                            event.consume();
                            break;
                        case N:  // Ctrl+N for new customer
                            handleNewCustomer(new ActionEvent());
                            event.consume();
                            break;
                        case H:  // Ctrl+H for help
                            showKeyboardShortcutsDialog();
                            event.consume();
                            break;
                            case R:  // Ctrl+R for Retail customer type
            retailRadio.setSelected(true);
            event.consume();
            break;
        case W:  // Ctrl+W for Wholesale customer type
            wholesaleRadio.setSelected(true);
            event.consume();
            break;
                    }
                } else {
                    // Standalone shortcuts
                    switch (event.getCode()) {
                        case F1:  // F1 for help
                            showKeyboardShortcutsDialog();
                            event.consume();
                            break;
                        case F2:  // F2 to focus on customer combo box
                            customerComboBox.requestFocus();
                            event.consume();
                            break;
                        case F3:  // F3 to focus on discount field
                            discountField.requestFocus();
                            event.consume();
                            break;
                        case F4:  // F4 to switch to cash payment
                            cashRadio.setSelected(true);
                            cashReceivedField.requestFocus();
                            event.consume();
                            break;
                        case F5:  // F5 to switch to credit payment
                            creditRadio.setSelected(true);
                            event.consume();
                            break;
                        case F6:  // F6 to focus on cash received field
                            if (cashRadio.isSelected()) {
                                cashReceivedField.requestFocus();
                                event.consume();
                            }
                            break;
                        case F7:  // F7 to focus on shopping cart table
                            if (!cartItems.isEmpty()) {
                                cartTable.requestFocus();
                                if (cartTable.getSelectionModel().isEmpty()) {
                                    cartTable.getSelectionModel().select(0);
                                }
                                event.consume();
                            }
                            break;
                        case F8:  // F8 to focus on barcode field
                            barcodeField.requestFocus();
                            event.consume();
                            break;
                        case F12: // F12 for checkout
                            if (!checkoutButton.isDisabled()) {
                                handleCheckout(new ActionEvent());
                                event.consume();
                            }
                            break;
                    }
                }
            });
        });
    }

    private void setupTabOrder() {
        Platform.runLater(() -> {
            barcodeField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.TAB) {
                    if (!searchResultsTable.getItems().isEmpty()) {
                        searchResultsTable.requestFocus();
                        searchResultsTable.getSelectionModel().selectFirst();
                        event.consume();
                    }
                }
            });

            searchResultsTable.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    addToCart();
                    barcodeField.requestFocus();
                    event.consume();
                }
            });

            // Allow using arrow keys to navigate the cart table
            cartTable.setOnKeyPressed(event -> {
                CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }

                switch (event.getCode()) {
                    case DELETE:
                        cartItems.remove(selectedItem);
                        updateTotals();
                        checkoutButton.setDisable(cartItems.isEmpty());
                        event.consume();
                        break;
                    case ENTER:
                        editItemPrice(selectedItem);
                        event.consume();
                        break;
                    case D:
                        if (event.isControlDown()) {
                            editItemDiscount(selectedItem);
                            event.consume();
                        }
                        break;
                    case Q:
                        if (event.isControlDown()) {
                            // Focus on quantity field
                            int selectedIndex = cartTable.getSelectionModel().getSelectedIndex();
                            Platform.runLater(() -> {
                                cartTable.scrollTo(selectedIndex);
                                cartTable.layout();
                                editItemQuantity(selectedItem);
                            });
                            event.consume();
                        }
                        break;
                    case UP:
                    case DOWN:
                        // Default handling for navigation
                        break;
                }
            });

            customerComboBox.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    customerComboBox.show();
                    event.consume();
                }
            });

            // Add keyboard navigation between radio buttons
            retailRadio.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.RIGHT) {
                    wholesaleRadio.setSelected(true);
                    wholesaleRadio.requestFocus();
                    event.consume();
                }
            });

            wholesaleRadio.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.LEFT) {
                    retailRadio.setSelected(true);
                    retailRadio.requestFocus();
                    event.consume();
                }
            });

            cashRadio.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.RIGHT) {
                    creditRadio.setSelected(true);
                    creditRadio.requestFocus();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER && cashRadio.isSelected()) {
                    cashReceivedField.requestFocus();
                    event.consume();
                }
            });

            creditRadio.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.LEFT) {
                    cashRadio.setSelected(true);
                    cashRadio.requestFocus();
                    event.consume();
                }
            });
        });
    }
// Updated editItemQuantity method in POSController.java

private void editItemQuantity(CartItem item) {
    final Product product = item.getProduct();
    List<ProductBatchService.BatchInfo> batchInfo = productBatchService.getBatchAvailabilityInfo(product.getId());
    
    BigDecimal currentQuantity = item.getQuantity();
    String pricingStrategy = configService.getPricingStrategy();
    
    // Create a custom dialog for quantity entry with strategy-aware batch information
    Dialog<BigDecimal> dialog = new Dialog<>();
    dialog.setTitle("Edit Quantity");
    dialog.setHeaderText("Edit quantity for " + item.getName() + " (Strategy: " + pricingStrategy + ")");
    
    // Add batch information to dialog
    VBox content = new VBox(10);
    content.setPadding(new Insets(10));
    
    if (!batchInfo.isEmpty()) {
        // Show current pricing strategy
        Label strategyLabel = new Label("Current Pricing Strategy: " + pricingStrategy);
        strategyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        content.getChildren().add(strategyLabel);
        
        // Get primary batch based on strategy
        ProductBatchService.BatchInfo primaryBatch = getPrimaryBatch(batchInfo);
        if (primaryBatch != null) {
            Label primaryLabel = new Label("Primary Batch #" + primaryBatch.getBatchNumber() + 
                    ": " + primaryBatch.getAvailableQuantity() + " units available");
            primaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            content.getChildren().add(primaryLabel);
        }
        
        content.getChildren().add(new Separator());
        content.getChildren().add(new Label("All Available Batches:"));
        
        // Create a visual representation of available batches
        for (ProductBatchService.BatchInfo batch : batchInfo) {
            HBox batchRow = new HBox(10);
            batchRow.setAlignment(Pos.CENTER_LEFT);
            
            String batchText = "Batch #" + batch.getBatchNumber() + ": " + 
                    batch.getAvailableQuantity() + " units";
            
            // Add pricing information
            boolean isWholesale = wholesaleRadio.isSelected();
            BigDecimal displayPrice = isWholesale && batch.getWholesalePrice() != null 
                    ? batch.getWholesalePrice() 
                    : batch.getSellingPrice();
            batchText += " (Price: " + DecimalFormatter.format(displayPrice) + ")";
            
            Label batchLabel = new Label(batchText);
            
            // Highlight the primary batch
            if (primaryBatch != null && batch.getBatchNumber().equals(primaryBatch.getBatchNumber())) {
                batchLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
            
            batchRow.getChildren().add(batchLabel);
            content.getChildren().add(batchRow);
        }
        
        content.getChildren().add(new Separator());
    }
    
    HBox quantityBox = new HBox(10);
    quantityBox.setAlignment(Pos.CENTER_LEFT);
    Label quantityLabel = new Label("Enter quantity:");
    TextField quantityField = new TextField(currentQuantity.toString());
    quantityField.setPrefWidth(100);
    quantityBox.getChildren().addAll(quantityLabel, quantityField);
    
    content.getChildren().add(quantityBox);
    
    // Calculate total available across all batches
    final BigDecimal totalAvailable = batchInfo.stream()
            .map(ProductBatchService.BatchInfo::getAvailableQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    Label availableLabel = new Label("Total available: " + totalAvailable);
    content.getChildren().add(availableLabel);
    
    // Add strategy explanation
    String explanation = getStrategyExplanation(pricingStrategy);
    Label explanationLabel = new Label(explanation);
    explanationLabel.setWrapText(true);
    explanationLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");
    content.getChildren().add(explanationLabel);
    
    dialog.getDialogPane().setContent(content);
    
    ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    
    // Setup focus and key handling
    Platform.runLater(() -> {
        quantityField.requestFocus();
        quantityField.selectAll();
    });
    
    // Add Enter key handling to the text field
    quantityField.setOnKeyPressed(event -> {
        if (event.getCode() == KeyCode.ENTER) {
            Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
            confirmButton.fire();
            event.consume();
        }
    });
    
    // Improved dialog key handling
    dialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        if (event.getCode() == KeyCode.ENTER && !event.isConsumed()) {
            Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
            if (confirmButton != null && !confirmButton.isDisabled()) {
                confirmButton.fire();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.fire();
            }
            event.consume();
        }
    });
    
    // Convert the result when the confirm button is clicked
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == confirmButtonType) {
            try {
                return new BigDecimal(quantityField.getText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    });
    
    final String productName = product.getName();
    final ProductBatchService.BatchInfo primaryBatch = batchInfo.isEmpty() ? null : getPrimaryBatch(batchInfo);
    
    // Show the dialog and process the result
    Optional<BigDecimal> result = dialog.showAndWait();
    result.ifPresent(newQuantity -> {
        try {
            // Validate the new quantity
            if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showErrorAlert("Invalid Quantity",
                        "Quantity must be greater than zero",
                        "Please enter a positive value");
                return;
            }
            
            // Check if total quantity is available across all batches
            if (newQuantity.compareTo(totalAvailable) > 0) {
                AlertHelper.showWarningAlert("Insufficient Stock",
                        "Cannot Set Quantity",
                        "Only " + totalAvailable.setScale(2, RoundingMode.HALF_UP)
                        + " units of " + productName + " are available in stock.");
                return;
            }
            
            // Check if quantity exceeds primary batch and show strategy-aware alert
            if (primaryBatch != null && 
                newQuantity.compareTo(primaryBatch.getAvailableQuantity()) > 0 && 
                newQuantity.compareTo(totalAvailable) <= 0) {
                showMultipleBatchAlert(batchInfo, primaryBatch);
            }
            
            // Set the quantity
            if (!item.setQuantity(newQuantity.toString())) {
                AlertHelper.showErrorAlert("Invalid Quantity",
                        "Could not set quantity",
                        "Please enter a valid number");
                return;
            }
            
            updateTotals();
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Invalid Input",
                    "Please enter a valid number",
                    "Input is not a valid quantity");
        }
    });
}

    private void addShortcutHints() {
        // Add shortcut hints to buttons
        addToCartButton.setTooltip(new Tooltip("Add to Cart (Ctrl+A)"));
        checkoutButton.setTooltip(new Tooltip("Checkout (F12 or Ctrl+C)"));
        clearButton.setTooltip(new Tooltip("Clear Cart (Ctrl+X)"));
        newCustomerButton.setTooltip(new Tooltip("New Customer (Ctrl+N)"));
        searchButton.setTooltip(new Tooltip("Search (Ctrl+F)"));
        scanBarcodeButton.setTooltip(new Tooltip("Scan Barcode (F8)"));
        barcodeField.setPromptText("Barcode/Search (F8)");

        // Add tooltips to radio buttons
        retailRadio.setTooltip(new Tooltip("Retail Customer Type (Ctrl+R)"));
        wholesaleRadio.setTooltip(new Tooltip("Wholesale Customer Type (Ctrl+W)"));
        cashRadio.setTooltip(new Tooltip("Cash Payment (F4)"));
        creditRadio.setTooltip(new Tooltip("Credit Payment (F5)"));

        // Add tooltip to cart table
        cartTable.setTooltip(new Tooltip("Shopping Cart (F7)"));
    }

    private void showKeyboardShortcutsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Available Keyboard Shortcuts");

        // Create a formatted list of shortcuts
        String shortcutsContent
                = "F1: Show Keyboard Shortcuts Help\n"
                + "F2: Focus customer selection\n"
                + "F3: Focus discount field\n"
                + "F4: Switch to cash payment\n"
                + "F5: Switch to credit payment\n"
                + "F6: Focus cash received field\n"
                + "F7: Focus shopping cart\n"
                + // Added line
                "F8: Focus barcode/search field\n"
                + "F12: Checkout\n\n"
                + "Ctrl+F: Focus search field\n"
                + "Ctrl+A: Add to cart\n"
                + "Ctrl+C: Checkout\n"
                + "Ctrl+X: Clear cart\n"
                + "Ctrl+N: New customer\n"
                + "Ctrl+H: Show this help\n\n"
                + "Ctrl+R: Select Retail customer type\n"
                + "Ctrl+W: Select Wholesale customer type\n\n"
                +
                "In shopping cart:\n"
                + // Added section
                "Ctrl+D: Edit item discount\n"
                + // Added line
                "Ctrl+Q: Edit item quantity\n"
                + // Added line
                "Enter: Edit item price\n"
                + "Delete: Remove selected item\n\n"
                + "Tab: Navigate between fields";

        alert.setContentText(shortcutsContent);
        alert.showAndWait();
    }

    private void initializeSearchResultsTable() {
        resultIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        resultNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        resultPriceColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            BigDecimal price = wholesaleRadio.isSelected() && product.getWholesalePrice() != null
                    ? product.getWholesalePrice()
                    : product.getSellingPrice();
            return new SimpleObjectProperty<>(price);
        });

        // Format price column
        resultPriceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
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

        searchResultsTable.setItems(searchResults);
    }

    private void initializeCartTable() {
        // Setup index column (row numbers)
        cartIndexColumn.setCellFactory(col -> new TableCell<CartItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        // Setup other columns
        cartNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        cartUnitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        cartPriceColumn.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty());
        cartQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        cartDiscountColumn.setCellValueFactory(cellData -> cellData.getValue().discountAmountProperty());
        cartTotalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        cartActionColumn.setCellValueFactory(cellData -> cellData.getValue().actionButtonProperty());

        // Format price and total columns
        // In your initializeCartTable() method in POSController.java, add this for the price column
        cartPriceColumn.setCellFactory(column -> {
            TableCell<CartItem, BigDecimal> cell = new TableCell<CartItem, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Format to 2 decimal places
                        setText(item.setScale(2, java.math.RoundingMode.HALF_UP).toString());
                    }
                }
            };

            // Create context menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editPrice = new MenuItem("Edit Price");
            contextMenu.getItems().add(editPrice);

            // Set the action for the menu item
            editPrice.setOnAction(event -> {
                CartItem item = cell.getTableRow().getItem();
                if (item != null) {
                    editItemPrice(item);
                }
            });

            // Add double-click handler
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    CartItem item = cell.getTableRow().getItem();
                    if (item != null) {
                        editItemPrice(item);
                    }
                }
            });

            // Show context menu on right-click
            cell.setContextMenu(contextMenu);

            return cell;
        });

        cartDiscountColumn.setCellFactory(column -> new TableCell<CartItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DecimalFormatter.formatCurrency(item));
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) {
                            editItemDiscount(getTableRow().getItem());
                        }
                    });
                }
            }
        });

        cartTotalColumn.setCellFactory(column -> new TableCell<CartItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    // Format to 2 decimal places
                    setText(item.setScale(2, java.math.RoundingMode.HALF_UP).toString());
                }
            }
        });

        // Setup quantify column to be editable
      // Setup quantity column to be editable
cartQuantityColumn.setCellFactory(column -> new TableCell<CartItem, BigDecimal>() {
    private final TextField textField = new TextField();
    private final HBox container = new HBox(5);
    private final Button minusBtn = new Button("-");
    private final Button plusBtn = new Button("+");
    private final Button infoBtn = new Button("i");

    {
        // Style buttons
        minusBtn.getStyleClass().add("qty-button");
        plusBtn.getStyleClass().add("qty-button");
        infoBtn.getStyleClass().add("info-button");
        minusBtn.setMinWidth(25);
        plusBtn.setMinWidth(25);
        infoBtn.setMinWidth(25);

        // Style text field
        textField.setMaxWidth(60);
        textField.setAlignment(javafx.geometry.Pos.CENTER);

        // Setup container
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.getChildren().addAll(minusBtn, textField, plusBtn, infoBtn);

        // Button actions
        minusBtn.setOnAction(e -> {
            CartItem item = getTableRow().getItem();
            if (item != null) {
                item.decrementQuantity();
                // Format to 2 decimal places
                textField.setText(item.getQuantity().setScale(2, java.math.RoundingMode.HALF_UP).toString());
                updateTotals();
            }
        });

       // Updated plus button action in cartQuantityColumn.setCellFactory
plusBtn.setOnAction(e -> {
    CartItem item = getTableRow().getItem();
    if (item != null) {
        Product product = item.getProduct();
        BigDecimal currentQuantity = item.getQuantity();
        
        // Get batch information
        List<ProductBatchService.BatchInfo> batchInfo = 
            productBatchService.getBatchAvailabilityInfo(product.getId());
        
        if (!batchInfo.isEmpty()) {
            // Get primary batch based on pricing strategy
            ProductBatchService.BatchInfo primaryBatch = getPrimaryBatch(batchInfo);
            BigDecimal primaryBatchQuantity = primaryBatch.getAvailableQuantity();
            
            // Calculate total available across all batches
            BigDecimal totalAvailable = batchInfo.stream()
                .map(ProductBatchService.BatchInfo::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal updatedQuantity = currentQuantity.add(BigDecimal.ONE);
            
            // Check if updated quantity exceeds primary batch but is within total available
            if (updatedQuantity.compareTo(primaryBatchQuantity) > 0 && 
                updatedQuantity.compareTo(totalAvailable) <= 0) {
                
                // Show strategy-specific alert
                showMultipleBatchAlert(batchInfo, primaryBatch);
            }
            
            // Check if there's enough stock available
            if (updatedQuantity.compareTo(totalAvailable) > 0) {
                AlertHelper.showWarningAlert("Insufficient Stock",
                    "Cannot Add More Items",
                    "Only " + totalAvailable.setScale(2, java.math.RoundingMode.HALF_UP)
                    + " units of " + product.getName() + " are available in stock.");
                return;
            }
        }
        
        // If we get here, we can increment
        item.incrementQuantity();
        textField.setText(item.getQuantity().setScale(2, java.math.RoundingMode.HALF_UP).toString());
        updateTotals();
    }
});
        
        // Add info button action to show batch details
        infoBtn.setOnAction(e -> {
            CartItem item = getTableRow().getItem();
            if (item != null) {
                Product product = item.getProduct();
                List<ProductBatchService.BatchInfo> batchInfo = 
                    productBatchService.getBatchAvailabilityInfo(product.getId());
                    
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Batch Information");
                alert.setHeaderText("Available Batches for " + product.getName());
                
                VBox content = new VBox(10);
                if (batchInfo.isEmpty()) {
                    content.getChildren().add(new Label("No batch information available."));
                } else {
                    for (ProductBatchService.BatchInfo batch : batchInfo) {
                        HBox batchRow = new HBox(10);
                        batchRow.setAlignment(Pos.CENTER_LEFT);
                        Label batchLabel = new Label("• Batch #" + batch.getBatchNumber() + ": " + 
                            batch.getAvailableQuantity() + " units (Price: " + 
                            DecimalFormatter.format(batch.getSellingPrice()) + ")");
                        batchRow.getChildren().add(batchLabel);
                        content.getChildren().add(batchRow);
                    }
                }
                
                alert.getDialogPane().setContent(content);
                alert.showAndWait();
            }
        });

        // Text field actions
        textField.setOnAction(event -> commitEdit());
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEdit();
            }
        });

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*\\.?\\d*")) {
                textField.setText(oldText);
            }
        });
    }
// Updated commitEdit method in cartQuantityColumn.setCellFactory

private void commitEdit() {
    CartItem item = getTableRow().getItem();
    if (item != null) {
        try {
            BigDecimal newQuantity = new BigDecimal(textField.getText());
            Product product = item.getProduct();
            
            // Get batch information
            List<ProductBatchService.BatchInfo> batchInfo = 
                productBatchService.getBatchAvailabilityInfo(product.getId());
            
            // Calculate total available across all batches
            BigDecimal totalAvailable = batchInfo.stream()
                .map(ProductBatchService.BatchInfo::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // If there's batch info, do strategy-aware checking
            if (!batchInfo.isEmpty()) {
                // Get primary batch based on pricing strategy
                ProductBatchService.BatchInfo primaryBatch = getPrimaryBatch(batchInfo);
                BigDecimal primaryBatchQuantity = primaryBatch.getAvailableQuantity();
                
                // Check if new quantity exceeds primary batch but is within total available
                if (newQuantity.compareTo(primaryBatchQuantity) > 0 && 
                    newQuantity.compareTo(totalAvailable) <= 0) {
                    
                    // Show strategy-specific alert
                    showMultipleBatchAlert(batchInfo, primaryBatch);
                }
                
                // Check if there's enough stock available
                if (newQuantity.compareTo(totalAvailable) > 0) {
                    AlertHelper.showWarningAlert("Insufficient Stock",
                        "Cannot Set Quantity",
                        "Only " + totalAvailable.setScale(2, java.math.RoundingMode.HALF_UP)
                        + " units of " + product.getName() + " are available in stock.");
                    // Reset to previous quantity
                    textField.setText(item.getQuantity().setScale(2, java.math.RoundingMode.HALF_UP).toString());
                    return;
                }
            }
            
            if (!item.setQuantity(textField.getText())) {
                // Format to 2 decimal places
                textField.setText(item.getQuantity().setScale(2, java.math.RoundingMode.HALF_UP).toString());
            }
            updateTotals();
        } catch (NumberFormatException e) {
            // Format to 2 decimal places
            textField.setText(item.getQuantity().setScale(2, java.math.RoundingMode.HALF_UP).toString());
        }
    }
}

    @Override
    protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            // Format to 2 decimal places
            textField.setText(item.setScale(2, java.math.RoundingMode.HALF_UP).toString());
            setGraphic(container);
        }
    }
});

        cartTable.setItems(cartItems);
    }

    private void editItemDiscount(CartItem item) {
        TextInputDialog dialog = new TextInputDialog(item.getDiscountAmount().toString());
        dialog.setTitle("Edit Discount");
        dialog.setHeaderText("Edit discount for " + item.getName());
        dialog.setContentText("Enter discount amount:");
        
        setupDialogKeyHandling(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(discountText -> {
            try {
                BigDecimal discount = new BigDecimal(discountText);
                BigDecimal maxDiscount = item.getUnitPrice().multiply(item.getQuantity());

                if (discount.compareTo(maxDiscount) > 0) {
                    AlertHelper.showErrorAlert("Invalid Discount",
                            "Discount cannot exceed item total",
                            "Maximum allowed discount: " + DecimalFormatter.formatCurrency(maxDiscount));
                    return;
                }

                if (discount.compareTo(BigDecimal.ZERO) < 0) {
                    AlertHelper.showErrorAlert("Invalid Discount",
                            "Negative discount not allowed",
                            "Please enter a positive value");
                    return;
                }

                item.setDiscountAmount(discount);
                updateTotals();
            } catch (NumberFormatException e) {
                AlertHelper.showErrorAlert("Invalid Input",
                        "Please enter a valid number",
                        discountText + " is not a valid discount amount");
            }
        });
    }

    private void editItemPrice(CartItem item) {
        TextInputDialog dialog = new TextInputDialog(item.getUnitPrice().toString());
        dialog.setTitle("Edit Price");
        dialog.setHeaderText("Edit price for " + item.getName());
        dialog.setContentText("Enter new unit price:");
        
        setupDialogKeyHandling(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(priceText -> {
            try {
                BigDecimal newPrice = new BigDecimal(priceText);

                if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    AlertHelper.showErrorAlert("Invalid Price",
                            "Price must be greater than zero",
                            "Please enter a positive value");
                    return;
                }

                item.setUnitPrice(newPrice);
                updateTotals();
            } catch (NumberFormatException e) {
                AlertHelper.showErrorAlert("Invalid Input",
                        "Please enter a valid number",
                        priceText + " is not a valid price amount");
            }
        });
    }

    private void loadCustomers() {
        customers.clear();
        customers.addAll(customerService.getAllCustomers());
        customerComboBox.setItems(customers);

        // Select default walk-in customer
        customerComboBox.getSelectionModel().selectFirst();
    }

    private void loadCustomers(String customerType) {
        customers.clear();
        customers.addAll(customerService.getCustomersByType(customerType));
        customerComboBox.setItems(customers);

        // Select first customer if available
        if (!customers.isEmpty()) {
            customerComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void handleBarcodeEnter(ActionEvent event) {
        String barcode = barcodeField.getText().trim();
        if (!barcode.isEmpty()) {
            Product product = productService.getProductByBarcode(barcode);
            if (product != null) {
                searchResults.clear();
                searchResults.add(product);
                searchResultsTable.getSelectionModel().select(0);
                addToCart();
            } else {
                // If barcode not found, try searching by name
                handleProductSearch(event);
            }
        }
    }

    @FXML
    private void handleProductSearch(ActionEvent event) {
        String searchTerm = barcodeField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Product> products = productService.searchProductsByName(searchTerm);
            searchResults.clear();
            searchResults.addAll(products);

            if (searchResults.isEmpty()) {
                AlertHelper.showInformationAlert("No Results", "No Products Found",
                        "No products match your search criteria.");
            }
        } else {
            searchResults.clear();
        }
    }

    @FXML
    private void handleAdvancedSearch(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product_search_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            ProductSearchDialogController controller = loader.getController();
            controller.setCustomerType(wholesaleRadio.isSelected() ? "WHOLESALE" : "RETAIL");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Product Search");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(posPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isProductSelected()) {
                Product selectedProduct = controller.getSelectedProduct();
                searchResults.clear();
                searchResults.add(selectedProduct);
                searchResultsTable.getSelectionModel().select(0);
                addToCart();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to open the product search dialog.");
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        addToCart();
    }

  

private void addToCart() {
    Product selectedProduct = searchResultsTable.getSelectionModel().getSelectedItem();
    if (selectedProduct != null) {
        // Check if we have stock available
        if (selectedProduct.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0) {
            AlertHelper.showWarningAlert("Out of Stock",
                    "Cannot Add to Cart",
                    "The selected product is currently out of stock.");
            return;
        }

        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(selectedProduct.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Check if there's enough stock before incrementing quantity
            CartItem item = existingItem.get();
            BigDecimal updatedQuantity = item.getQuantity().add(BigDecimal.ONE);

            if (updatedQuantity.compareTo(selectedProduct.getCurrentStock()) > 0) {
                AlertHelper.showWarningAlert("Insufficient Stock",
                        "Cannot Add More Items",
                        "Only " + selectedProduct.getCurrentStock().setScale(2, java.math.RoundingMode.HALF_UP)
                        + " units of " + selectedProduct.getName() + " are available in stock.");
                return;
            }

            // Increment quantity if product already in cart
            item.setQuantity(updatedQuantity);
            item.updateTotal();
        } else {
            // Get price based on the configured strategy
            boolean isWholesale = wholesaleRadio.isSelected();
            BigDecimal price = productBatchService.getStrategyBasedSellingPrice(
                selectedProduct.getId(), isWholesale);
            
            CartItem newItem = new CartItem(selectedProduct, BigDecimal.ONE, price);

            // Set action for remove button
            newItem.getActionButton().setOnAction(e -> {
                cartItems.remove(newItem);
                updateTotals();
                checkoutButton.setDisable(cartItems.isEmpty());
            });

            cartItems.add(newItem);
        }

        // Clear search after adding
        barcodeField.clear();
        searchResults.clear();

        // Update totals
        updateTotals();

        // Enable checkout if cart has items
        checkoutButton.setDisable(cartItems.isEmpty());
    }
}

    private void updateTotals() {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalItemDiscounts = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            subTotal = subTotal.add(item.getUnitPrice().multiply(item.getQuantity()));
            totalItemDiscounts = totalItemDiscounts.add(item.getDiscountAmount());
        }

        subTotalLabel.setText(DecimalFormatter.formatCurrency(subTotal));

        BigDecimal percentageDiscountAmount = BigDecimal.ZERO;
        try {
            String discountText = discountField.getText();
            if (discountText != null && !discountText.isEmpty()) {
                BigDecimal discountPercentage = new BigDecimal(discountText);
                percentageDiscountAmount = subTotal.subtract(totalItemDiscounts)
                        .multiply(discountPercentage)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        } catch (NumberFormatException e) {
            discountField.setText("0");
        }

        BigDecimal total = subTotal.subtract(totalItemDiscounts).subtract(percentageDiscountAmount);
        totalLabel.setText(DecimalFormatter.formatCurrency(total));

        // Update item discounts label if you added one
        if (itemDiscountsLabel != null) {
            itemDiscountsLabel.setText(DecimalFormatter.formatCurrency(totalItemDiscounts));
        }
    }

   private void updatePrices() {
    boolean isWholesale = wholesaleRadio.isSelected();

    // Update prices in search results table
    resultPriceColumn.setCellValueFactory(cellData -> {
        Product product = cellData.getValue();
        // Get price based on the configured strategy
        BigDecimal price = productBatchService.getStrategyBasedSellingPrice(
            product.getId(), isWholesale);
        return new SimpleObjectProperty<>(price);
    });

    // Update prices in cart
    for (CartItem item : cartItems) {
        Product product = item.getProduct();
        // Get price based on the configured strategy
        BigDecimal price = productBatchService.getStrategyBasedSellingPrice(
            product.getId(), isWholesale);
        item.unitPriceProperty().set(price);
        item.updateTotal();
    }

    // Update totals
    updateTotals();
}

    @FXML
    private void handleNewCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            CustomerDialogController controller = loader.getController();
            controller.initializeForAdd(wholesaleRadio.isSelected() ? "WHOLESALE" : "RETAIL");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(posPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isCustomerSaved()) {
                // Reload customers and select the new one
                loadCustomers(wholesaleRadio.isSelected() ? "WHOLESALE" : "RETAIL");
                customerComboBox.getSelectionModel().select(controller.getCreatedCustomer());
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to open the customer dialog.");
        }
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        if (!cartItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear Cart");
            alert.setHeaderText("Clear Shopping Cart");
            alert.setContentText("Are you sure you want to clear the shopping cart?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cartItems.clear();
                updateTotals();
                checkoutButton.setDisable(true);
            }
        }
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            AlertHelper.showErrorAlert("Empty Cart", "Cannot Checkout",
                    "Please add items to the cart before checkout.");
            return;
        }

        // Get selected customer
        Customer selectedCustomer = customerComboBox.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            AlertHelper.showErrorAlert("No Customer Selected", "Cannot Checkout",
                    "Please select a customer before checkout.");
            return;
        }

        // Validate cash payment
        if (cashRadio.isSelected()) {
            String cashReceivedText = cashReceivedField.getText();
            if (cashReceivedText == null || cashReceivedText.isEmpty()) {
                AlertHelper.showErrorAlert("Cash Amount Required", "Cannot Checkout",
                        "Please enter the cash amount received from customer.");
                return;
            }

            try {
                BigDecimal cashReceived = new BigDecimal(cashReceivedText);
                BigDecimal total = extractAmountFromLabel(totalLabel.getText());

                if (cashReceived.compareTo(total) < 0) {
                    AlertHelper.showErrorAlert("Insufficient Cash", "Cannot Checkout",
                            "The cash amount received is less than the total amount.");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertHelper.showErrorAlert("Invalid Cash Amount", "Cannot Checkout",
                        "Please enter a valid cash amount.");
                return;
            }
        }

        // Check if credit payment is allowed for this customer
        if (creditRadio.isSelected()) {
            if (!customerService.hasCreditAccount(selectedCustomer.getId())) {
                boolean createAccount = AlertHelper.showConfirmationAlert("Credit Account Required",
                        "Create Credit Account", "This customer does not have a credit account. "
                        + "Would you like to create one?");

                if (createAccount) {
                    // Create credit account with default limit
                    customerService.updateCreditLimit(selectedCustomer.getId(), new BigDecimal("10000"));
                } else {
                    return; // Cancel checkout if user doesn't want to create credit account
                }
            }

            // Check credit limit
            BigDecimal total = extractAmountFromLabel(totalLabel.getText());
            CreditAccount creditAccount = customerService.getCreditAccount(selectedCustomer.getId());

            if (creditAccount != null && !creditAccount.hasAvailableCredit(total)) {
                AlertHelper.showErrorAlert("Credit Limit Exceeded", "Cannot Process on Credit",
                        "This transaction would exceed the customer's credit limit. "
                        + "Please choose a different payment method.");
                return;
            }
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(selectedCustomer);
        invoice.setCustomerType(wholesaleRadio.isSelected() ? "WHOLESALE" : "RETAIL");
        invoice.setPaymentMethod(creditRadio.isSelected() ? "CREDIT" : "CASH");

        // Get discount
        try {
            String discountText = discountField.getText();
            if (discountText != null && !discountText.isEmpty()) {
                invoice.setDiscountPercentage(new BigDecimal(discountText));
            } else {
                invoice.setDiscountPercentage(BigDecimal.ZERO);
            }
        } catch (NumberFormatException e) {
            invoice.setDiscountPercentage(BigDecimal.ZERO);
        }

        // Add items to invoice
        for (CartItem cartItem : cartItems) {
            invoice.addItem(cartItem.toInvoiceItem());
        }

        // Handle cash payment details
        if (cashRadio.isSelected()) {
            try {
                BigDecimal cashReceived = new BigDecimal(cashReceivedField.getText().trim());
                invoice.setCashReceived(cashReceived);
                // Note: changeAmount is automatically calculated in the Invoice.setCashReceived method
            } catch (NumberFormatException e) {
                // This shouldn't happen due to previous validation, but handle it just in case
                invoice.setCashReceived(BigDecimal.ZERO);
                invoice.setChangeAmount(BigDecimal.ZERO);
            }
        } else {
            // For credit payments, set cash received and change to zero
            invoice.setCashReceived(BigDecimal.ZERO);
            invoice.setChangeAmount(BigDecimal.ZERO);
        }

        // Calculate totals
        invoice.calculateTotals();

        // Save invoice
        Invoice savedInvoice = invoiceService.saveInvoice(invoice);

        if (savedInvoice != null && savedInvoice.getId() != null) {
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout Complete");
            alert.setHeaderText("Transaction Successful");
            alert.setContentText("Invoice #" + savedInvoice.getInvoiceNumber() + " has been created successfully.");

            // Add Print button to alert with styling to make it prominent
            ButtonType printButtonType = new ButtonType("Print", ButtonBar.ButtonData.LEFT);
            ButtonType okButtonType = ButtonType.OK;

            alert.getButtonTypes().setAll(printButtonType, okButtonType);
            

            // Apply custom styling to highlight print button
            Window dialog = alert.getDialogPane().getScene().getWindow();
            if (dialog instanceof Stage) {
                Stage stage = (Stage) dialog;

                // Apply custom styling to highlight print button
                Platform.runLater(() -> {
                    Button printButton = (Button) alert.getDialogPane().lookupButton(printButtonType);
                    if (printButton != null) {
                        printButton.getStyleClass().add("highlighted-button");
                        printButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #2E7D32; -fx-border-width: 1;");
                        printButton.requestFocus(); // Auto-focus on print button
                    }
                });
            }

            // Show alert and handle button clicks
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == printButtonType) {
                // Print receipt
                ReceiptPrinter.printReceipt(savedInvoice);
            }

            // Reset cart
            resetPOS();
        } else {
            AlertHelper.showErrorAlert("Checkout Failed", "Transaction Error",
                    "There was an error processing your transaction. Please try again.");
        }
    }

    // Helper method to extract amount from formatted label text
    private BigDecimal extractAmountFromLabel(String labelText) {
        // Remove currency symbol and spaces
        String amountString = labelText.replace("LKR", "").replace(" ", "").trim();
        try {
            return new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void printReceipt(Invoice invoice) {
        // In a real application, this would integrate with a receipt printer
        // For now, we'll just show a message
        System.out.println("Printing receipt for invoice #" + invoice.getInvoiceNumber());

        // TODO: Implement receipt printing functionality
        // This could open a print dialog or send to a receipt printer
    }

    private void resetPOS() {
        // Clear cart
        cartItems.clear();
        updateTotals();

        // Reset cash fields with proper values instead of just clearing
        cashReceivedField.setText("0.00");
        changeAmountLabel.setText("LKR 0.00");
        changeAmountLabel.setStyle("-fx-text-fill: black;"); // Reset color

        // Reset discount
        discountField.setText("0");

        // Reset search
        barcodeField.clear();
        searchResults.clear();

        // Reset payment method
        cashRadio.setSelected(true);

        // Make sure cash fields are visible since we're resetting to cash payment
        cashReceivedField.setVisible(true);
        cashReceivedLabel.setVisible(true);
        changeLabel.setVisible(true);
        changeAmountLabel.setVisible(true);

        // Disable checkout button
        checkoutButton.setDisable(true);

        // Generate new invoice number
        Invoice dummyInvoice = new Invoice();
        dummyInvoice.setInvoiceNumber(invoiceService.generateInvoiceNumber());
        invoiceLabel.setText("Invoice #: " + dummyInvoice.getInvoiceNumber());

        // Reset the barcode scanner handler if it exists
        if (barcodeScannerHandler != null) {
            barcodeScannerHandler.reset();
        }

        // Set focus back to barcode field for the next transaction
        Platform.runLater(() -> barcodeField.requestFocus());
    }

    /**
     * Handles credit radio button selection changes
     */
    private void setupCreditPaymentOption() {
        // Add listener to credit radio button
        creditRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // When credit is selected, check if customer has a credit account
                Customer selectedCustomer = customerComboBox.getSelectionModel().getSelectedItem();
                if (selectedCustomer != null && !customerService.hasCreditAccount(selectedCustomer.getId())) {
                    AlertHelper.showWarningAlert("No Credit Account", "Credit Account Required",
                            "This customer does not have a credit account. Please select a different payment method or create a credit account.");
                    cashRadio.setSelected(true); // Switch back to cash
                }
            }
        });

        // Add listener to customer selection changes
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCustomer, newCustomer) -> {
            if (newCustomer != null && creditRadio.isSelected() && !customerService.hasCreditAccount(newCustomer.getId())) {
                AlertHelper.showWarningAlert("No Credit Account", "Credit Account Required",
                        "This customer does not have a credit account. Please select a different payment method or create a credit account.");
                cashRadio.setSelected(true); // Switch to cash
            }
        });
    }

    @FXML
    private void handleScanBarcode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/barcode_scanner_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            BarcodeScannerDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Scan Barcode");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(posPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            String barcode = controller.getScannedBarcode();
            if (barcode != null && !barcode.isEmpty()) {
                barcodeField.setText(barcode);
                handleBarcodeEnter(new ActionEvent());
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open barcode scanner",
                    "An error occurred while trying to open the barcode scanner dialog.");
        }
    }
    
//    /**
// * Prevents dialog event propagation
// * @param dialog The dialog to apply this fix to
// */
//private void setupDialogKeyHandling(Dialog<?> dialog) {
//    // Get the dialog pane
//    DialogPane dialogPane = dialog.getDialogPane();
//    
//    // Add event filter to consume Enter key events at dialog level
//    dialogPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//        if (event.getCode() == KeyCode.ENTER) {
//            event.consume(); // Consume the event to prevent it from propagating
//            
//            // Handle the Enter key press manually
//            Button defaultButton = (Button) dialogPane.lookupButton(ButtonType.OK);
//            if (defaultButton != null) {
//                defaultButton.fire();
//            }
//        }
//    });
//}
    
    /**
 * Prevents dialog event propagation and sets up proper key handling
 * @param dialog The dialog to apply this fix to
 */
private void setupDialogKeyHandling(Dialog<?> dialog) {
    // Get the dialog pane
    DialogPane dialogPane = dialog.getDialogPane();
    
    // Add event filter to handle Enter and Escape keys at dialog level
    dialogPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        if (event.getCode() == KeyCode.ENTER && !event.isConsumed()) {
            // Find and fire the default/OK button
            Button defaultButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (defaultButton == null) {
                // Look for other positive buttons
                for (ButtonType buttonType : dialogPane.getButtonTypes()) {
                    if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        defaultButton = (Button) dialogPane.lookupButton(buttonType);
                        break;
                    }
                }
            }
            
            if (defaultButton != null && !defaultButton.isDisabled()) {
                defaultButton.fire();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            // Find and fire the cancel button
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.fire();
            }
            event.consume();
        }
    });
    
    // If it's a TextInputDialog, set up the text field properly
    if (dialog instanceof TextInputDialog) {
        TextInputDialog textDialog = (TextInputDialog) dialog;
        TextField textField = textDialog.getEditor();
        
        // Ensure proper focus
        Platform.runLater(() -> {
            textField.requestFocus();
            textField.selectAll();
        });
    }
}




    /**
     * Getter for ProductService to be used by BarcodeScannerHandler
     *
     * @return The ProductService instance
     */
    public ProductService getProductService() {
        return this.productService;
    }

    /**
     * Triggers barcode enter action from the barcode scanner handler
     */
    public void triggerBarcodeEnter() {
        handleBarcodeEnter(new ActionEvent());
    }
    
    
    
    
    
    
    
    //new codes 5/19 evening 
    
    
    // Updated quantity increase handling in POSController.java

// First, add a helper method to get the primary batch based on strategy
private ProductBatchService.BatchInfo getPrimaryBatch(List<ProductBatchService.BatchInfo> batchInfo) {
    if (batchInfo.isEmpty()) {
        return null;
    }
    
    String pricingStrategy = configService.getPricingStrategy();
    
    switch (pricingStrategy) {
        case SystemConfigService.FIFO_STRATEGY:
            // For FIFO, primary batch is the oldest (first in the list)
            return batchInfo.get(0);
            
        case SystemConfigService.LIFO_STRATEGY:
            // For LIFO, primary batch is the newest (last in the list by purchase date)
            return batchInfo.stream()
                    .max((b1, b2) -> b1.getPurchaseDate().compareTo(b2.getPurchaseDate()))
                    .orElse(batchInfo.get(0));
                    
        case SystemConfigService.AVERAGE_STRATEGY:
            // For AVERAGE, we could use the largest batch or first batch
            // Let's use the batch with the most quantity as "primary"
            return batchInfo.stream()
                    .max((b1, b2) -> b1.getAvailableQuantity().compareTo(b2.getAvailableQuantity()))
                    .orElse(batchInfo.get(0));
                    
        default:
            return batchInfo.get(0);
    }
}
    


// Add this new method to show strategy-specific batch alerts
private void showMultipleBatchAlert(List<ProductBatchService.BatchInfo> batchInfo, 
                                   ProductBatchService.BatchInfo primaryBatch) {
    String pricingStrategy = configService.getPricingStrategy();
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    
    // Set strategy-specific title and header
    switch (pricingStrategy) {
        case SystemConfigService.FIFO_STRATEGY:
            alert.setTitle("FIFO - Multiple Batches Required");
            alert.setHeaderText("Quantity exceeds oldest batch (FIFO strategy)");
            break;
        case SystemConfigService.LIFO_STRATEGY:
            alert.setTitle("LIFO - Multiple Batches Required");
            alert.setHeaderText("Quantity exceeds newest batch (LIFO strategy)");
            break;
        case SystemConfigService.AVERAGE_STRATEGY:
            alert.setTitle("AVERAGE - Multiple Batches Required");
            alert.setHeaderText("Multiple batches will be used (Average Cost strategy)");
            break;
        default:
            alert.setTitle("Multiple Batches Required");
            alert.setHeaderText("Multiple batches with different prices will be used");
            break;
    }
    
    VBox content = new VBox(10);
    
    // Add strategy explanation
    Label strategyLabel = new Label("Current Pricing Strategy: " + pricingStrategy);
    strategyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
    content.getChildren().add(strategyLabel);
    
    // Add primary batch info
    HBox primaryBatchRow = new HBox(10);
    primaryBatchRow.setAlignment(Pos.CENTER_LEFT);
    Label primaryLabel = new Label("Primary Batch #" + primaryBatch.getBatchNumber() + ": " + 
            primaryBatch.getAvailableQuantity() + " units available");
    primaryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
    primaryBatchRow.getChildren().add(primaryLabel);
    content.getChildren().add(primaryBatchRow);
    
    content.getChildren().add(new Separator());
    
    // Show all available batches
    Label batchesLabel = new Label("All Available Batches:");
    batchesLabel.setStyle("-fx-font-weight: bold;");
    content.getChildren().add(batchesLabel);
    
    for (ProductBatchService.BatchInfo batch : batchInfo) {
        HBox batchRow = new HBox(10);
        batchRow.setAlignment(Pos.CENTER_LEFT);
        
        String batchText = "• Batch #" + batch.getBatchNumber() + ": " + 
                batch.getAvailableQuantity() + " units";
        
        // Add pricing information based on strategy
        boolean isWholesale = wholesaleRadio.isSelected();
        BigDecimal displayPrice = isWholesale && batch.getWholesalePrice() != null 
                ? batch.getWholesalePrice() 
                : batch.getSellingPrice();
        batchText += " (Price: " + DecimalFormatter.format(displayPrice) + ")";
        
        Label batchLabel = new Label(batchText);
        
        // Highlight the primary batch
        if (batch.getBatchNumber().equals(primaryBatch.getBatchNumber())) {
            batchLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }
        
        batchRow.getChildren().add(batchLabel);
        content.getChildren().add(batchRow);
    }
    
    // Add strategy-specific explanation
    content.getChildren().add(new Separator());
    String explanation = getStrategyExplanation(pricingStrategy);
    Label explanationLabel = new Label(explanation);
    explanationLabel.setWrapText(true);
    explanationLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");
    content.getChildren().add(explanationLabel);
    
    alert.getDialogPane().setContent(content);
    alert.showAndWait();
}

// Add method to provide strategy-specific explanations
private String getStrategyExplanation(String pricingStrategy) {
    switch (pricingStrategy) {
        case SystemConfigService.FIFO_STRATEGY:
            return "FIFO (First In, First Out): Items are sold from the oldest batches first. " +
                   "The price shown will be from the oldest batch until it's exhausted.";
        case SystemConfigService.LIFO_STRATEGY:
            return "LIFO (Last In, First Out): Items are sold from the newest batches first. " +
                   "The price shown will be from the newest batch until it's exhausted.";
        case SystemConfigService.AVERAGE_STRATEGY:
            return "AVERAGE COST: The price shown is a weighted average across all batches. " +
                   "Items will be physically taken from oldest batches first (FIFO), but priced at average cost.";
        default:
            return "The price shown is based on your current pricing strategy settings.";
    }
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
