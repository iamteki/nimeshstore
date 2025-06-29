package com.nimesh.controller;

import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.model.InvoiceItem;
import com.nimesh.service.InvoiceService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.ReceiptPrinter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class TransactionHistoryController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @FXML
    private Label customerNameLabel;
    
    @FXML
    private Label totalPurchasesLabel;
    
    @FXML
    private TableView<Invoice> invoiceTable;
    
    @FXML
    private TableColumn<Invoice, String> invoiceNumberColumn;
    
    @FXML
    private TableColumn<Invoice, String> dateColumn;
    
    @FXML
    private TableColumn<Invoice, String> paymentMethodColumn;
    
    @FXML
    private TableColumn<Invoice, Integer> itemCountColumn;
    
    @FXML
    private TableColumn<Invoice, BigDecimal> totalAmountColumn;
    
    @FXML
    private TableView<InvoiceItem> itemsTable;
    
    @FXML
    private TableColumn<InvoiceItem, String> productNameColumn;
    
    @FXML
    private TableColumn<InvoiceItem, BigDecimal> quantityColumn;
    
    @FXML
    private TableColumn<InvoiceItem, BigDecimal> unitPriceColumn;
    
    @FXML
    private TableColumn<InvoiceItem, BigDecimal> itemTotalColumn;
    
    @FXML
    private Button reprintButton;
    
    private Stage dialogStage;
    private Customer customer;
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> itemsList = FXCollections.observableArrayList();
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void initializeForCustomer(Customer customer) {
        this.customer = customer;
        customerNameLabel.setText(customer.getName());
        
        // Initialize tables
        initializeInvoiceTable();
        initializeItemsTable();
        
        // Load invoices
        loadInvoices();
        
        // Calculate total purchases
        BigDecimal totalPurchases = calculateTotalPurchases();
        totalPurchasesLabel.setText("LKR " + totalPurchases.toString());
        
        // Initially disable the reprint button until an invoice is selected
        reprintButton.setDisable(true);
        
        // Add listener to enable/disable button based on selection
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    reprintButton.setDisable(newSelection == null);
                });
    }
    
    @FXML
    private void handleReprintInvoice() {
        Invoice selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        
        if (selectedInvoice == null) {
            AlertHelper.showWarningAlert("No Invoice Selected", 
                    "Please Select an Invoice", 
                    "You must select an invoice to reprint.");
            return;
        }
        
        // Confirm before printing
        boolean confirmPrint = AlertHelper.showConfirmationAlert("Reprint Invoice", 
                "Confirm Reprint", 
                "Are you sure you want to reprint Invoice #" + selectedInvoice.getInvoiceNumber() + "?");
        
        if (confirmPrint) {
            // Print the receipt
            ReceiptPrinter.printReceipt(selectedInvoice);
        }
    }
    
    private void initializeInvoiceTable() {
        invoiceNumberColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getInvoiceNumber()));
        
        dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"))));
        
        paymentMethodColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        
        itemCountColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getItems().size()));
        
        totalAmountColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getFinalAmount()));
        
        // Format the total amount column
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
        
        // Set selection handler to load items
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadInvoiceItems(newSelection);
                    } else {
                        itemsList.clear();
                    }
                });
        
        invoiceTable.setItems(invoiceList);
    }
    
    private void initializeItemsTable() {
        productNameColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        
        quantityColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        
        unitPriceColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getUnitPrice()));
        
        itemTotalColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>(cellData.getValue().getTotal()));
        
        // Format the price columns
        unitPriceColumn.setCellFactory(column -> new TableCell<InvoiceItem, BigDecimal>() {
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
        
        itemTotalColumn.setCellFactory(column -> new TableCell<InvoiceItem, BigDecimal>() {
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
        
        itemsTable.setItems(itemsList);
    }
    
    private void loadInvoices() {
        List<Invoice> invoices = invoiceService.getInvoicesForCustomer(customer.getId());
        invoiceList.clear();
        invoiceList.addAll(invoices);
    }
    
    private void loadInvoiceItems(Invoice invoice) {
        itemsList.clear();
        itemsList.addAll(invoice.getItems());
    }
    
    private BigDecimal calculateTotalPurchases() {
        BigDecimal total = BigDecimal.ZERO;
        
        for (Invoice invoice : invoiceList) {
            total = total.add(invoice.getFinalAmount());
        }
        
        return total;
    }
}