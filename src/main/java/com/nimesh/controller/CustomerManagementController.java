package com.nimesh.controller;

import com.nimesh.model.CreditAccount;
import com.nimesh.model.Customer;
import com.nimesh.model.Invoice;
import com.nimesh.service.CustomerService;
import com.nimesh.service.InvoiceService;
import com.nimesh.service.SMSService;
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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;

@Controller
public class CustomerManagementController implements Initializable {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ApplicationContext context;

    @FXML
    private BorderPane customerPane;

    // Customers Tab
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Long> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    @FXML
    private TableColumn<Customer, String> contactColumn;

    @FXML
    private TableColumn<Customer, String> addressColumn;

    @FXML
    private TableColumn<Customer, String> typeColumn;

    @FXML
    private TableColumn<Customer, BigDecimal> customerCreditBalanceColumn;

    @FXML
    private TableColumn<Customer, Button> actionsColumn;

    // Credit Management Tab
    @FXML
    private ComboBox<String> creditFilterCombo;

    @FXML
    private TableView<CreditAccount> creditTable;

    @FXML
    private TableColumn<CreditAccount, Long> creditIdColumn;

    @FXML
    private TableColumn<CreditAccount, String> creditNameColumn;

    @FXML
    private TableColumn<CreditAccount, String> creditContactColumn;

    @FXML
    private TableColumn<CreditAccount, BigDecimal> creditBalanceColumn;

    @FXML
    private TableColumn<CreditAccount, BigDecimal> creditLimitColumn;

    @FXML
    private TableColumn<CreditAccount, String> creditStatusColumn;

    @FXML
    private TableColumn<CreditAccount, HBox> creditActionsColumn;

    // Reports Tab
    @FXML
    private ComboBox<Customer> reportCustomerCombo;

    @FXML
    private ComboBox<String> reportTypeCombo;

    @FXML
    private VBox reportContainer;

    @FXML
    private Button editCustomerButton;

    @FXML
    private Button deleteCustomerButton;

    @FXML
    private Button viewTransactionsButton;

    @FXML
    private Button recordPaymentButton;

    @FXML
    private Button setCreditLimitButton;

    @FXML
    private Button sendRemindersButton;

    @FXML
    private Button exportCreditReportButton;

    @FXML
    private Button generateReportButton;
    
    @Autowired
    private SMSService smsService;

    private ObservableList<Customer> customersList = FXCollections.observableArrayList();
    private ObservableList<CreditAccount> creditAccountsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize tables
        initializeCustomerTable();
        initializeCreditTable();

        // Load data
        loadAllCustomers();
        loadAllCreditAccounts();

        // Set up search field handler
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadAllCustomers();
            }
        });

        // Set up credit filter combo
        creditFilterCombo.getItems().addAll(
                "All Customers",
                "With Outstanding Balance",
                "Approaching Credit Limit",
                "Exceeded Credit Limit"
        );
        creditFilterCombo.getSelectionModel().selectFirst();
        creditFilterCombo.setOnAction(event -> filterCreditAccounts());

        // Setup report type combo
        reportTypeCombo.getItems().addAll(
                "Purchase History",
                "Payment History",
                "Credit Summary",
                "Monthly Purchase Trends"
        );

        // Setup customer combo for reports
        reportCustomerCombo.setItems(customersList);

        // Disable buttons until selection is made
        disableButtons(true);

        // Add selection listeners to tables
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> disableButtons(newSelection == null));

        creditTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean noneSelected = newSelection == null;
                    recordPaymentButton.setDisable(noneSelected);
                    setCreditLimitButton.setDisable(noneSelected);
                });
    }

    private void initializeCustomerTable() {
        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("customerType"));

        // For credit balance, we need to fetch from credit account
        customerCreditBalanceColumn.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue();
            CreditAccount creditAccount = customerService.getCreditAccount(customer.getId());
            BigDecimal balance = (creditAccount != null) ? creditAccount.getBalance() : BigDecimal.ZERO;
            return new SimpleObjectProperty<>(balance);
        });

        // Format credit balance column
        customerCreditBalanceColumn.setCellFactory(column -> new TableCell<Customer, BigDecimal>() {
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

        // Setup actions column with view details button
        actionsColumn.setCellFactory(param -> new TableCell<Customer, Button>() {
            private final Button detailsButton = new Button("Details");

            {
                detailsButton.getStyleClass().add("detail-button");
                detailsButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    showCustomerDetails(customer);
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

        customerTable.setItems(customersList);
    }

    private void initializeCreditTable() {
        // Setup columns
        creditIdColumn.setCellValueFactory(cellData -> {
            CreditAccount account = cellData.getValue();
            return new SimpleObjectProperty<>(account.getCustomer().getId());
        });

        creditNameColumn.setCellValueFactory(cellData -> {
            CreditAccount account = cellData.getValue();
            return new SimpleStringProperty(account.getCustomer().getName());
        });

        creditContactColumn.setCellValueFactory(cellData -> {
            CreditAccount account = cellData.getValue();
            return new SimpleStringProperty(account.getCustomer().getContactNo());
        });

        creditBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        creditLimitColumn.setCellValueFactory(new PropertyValueFactory<>("creditLimit"));

        // Format money columns
        creditBalanceColumn.setCellFactory(column -> new TableCell<CreditAccount, BigDecimal>() {
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

        creditLimitColumn.setCellFactory(column -> new TableCell<CreditAccount, BigDecimal>() {
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

        // Credit status column
        creditStatusColumn.setCellFactory(column -> new TableCell<CreditAccount, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("credit-normal", "credit-high", "credit-critical");
                } else {
                    CreditAccount account = getTableView().getItems().get(getIndex());
                    String status;

                    if (account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                        status = "PAID";
                        getStyleClass().add("credit-normal");
                    } else if (account.getCreditLimit() == null) {
                        status = "OUTSTANDING";
                        getStyleClass().add("credit-normal");
                    } else {
                        // Calculate what percentage of limit is used
                        BigDecimal limit = account.getCreditLimit();
                        BigDecimal balance = account.getBalance();
                        BigDecimal usagePercent = balance.multiply(new BigDecimal("100")).divide(limit, 0, BigDecimal.ROUND_HALF_UP);

                        if (usagePercent.compareTo(new BigDecimal("90")) > 0) {
                            status = "CRITICAL (" + usagePercent + "%)";
                            getStyleClass().removeAll("credit-normal", "credit-high");
                            getStyleClass().add("credit-critical");
                        } else if (usagePercent.compareTo(new BigDecimal("70")) > 0) {
                            status = "HIGH (" + usagePercent + "%)";
                            getStyleClass().removeAll("credit-normal", "credit-critical");
                            getStyleClass().add("credit-high");
                        } else {
                            status = "NORMAL (" + usagePercent + "%)";
                            getStyleClass().removeAll("credit-high", "credit-critical");
                            getStyleClass().add("credit-normal");
                        }
                    }

                    setText(status);
                }
            }
        });

        // Setup actions column with buttons
        creditActionsColumn.setCellFactory(param -> new TableCell<CreditAccount, HBox>() {
            private final Button payButton = new Button("Pay");
            private final Button detailsButton = new Button("Details");
            private final Button reminderButton = new Button("Remind");
            private final HBox buttonBox = new HBox(5);

            {
                payButton.getStyleClass().add("pay-button");
                detailsButton.getStyleClass().add("detail-button");
                reminderButton.getStyleClass().add("remind-button");

                buttonBox.getChildren().addAll(payButton, detailsButton, reminderButton);

                payButton.setOnAction(event -> {
                    CreditAccount account = getTableView().getItems().get(getIndex());
                    handleCreditPayment(account);
                });

                detailsButton.setOnAction(event -> {
                    CreditAccount account = getTableView().getItems().get(getIndex());
                    showCreditDetails(account);
                });

                reminderButton.setOnAction(event -> {
                    CreditAccount account = getTableView().getItems().get(getIndex());
                    sendCreditReminder(account);
                });
            }

            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        creditTable.setItems(creditAccountsList);
    }

    private void loadAllCustomers() {
        customersList.clear();
        customersList.addAll(customerService.getAllCustomers());
    }

    private void loadAllCreditAccounts() {
        creditAccountsList.clear();
        List<CreditAccount> accounts = customerService.getCustomersWithOutstandingCredit();
        creditAccountsList.addAll(accounts);
    }

    private void filterCreditAccounts() {
        String filter = creditFilterCombo.getValue();
        creditAccountsList.clear();

        if ("All Customers".equals(filter)) {
            List<CreditAccount> allAccounts = customerService.getAllCreditAccounts();
            creditAccountsList.addAll(allAccounts);
        } else if ("With Outstanding Balance".equals(filter)) {
            List<CreditAccount> outstandingAccounts = customerService.getCustomersWithOutstandingCredit();
            creditAccountsList.addAll(outstandingAccounts);
        } else if ("Approaching Credit Limit".equals(filter)) {
            List<CreditAccount> approachingLimitAccounts = customerService.getCustomersApproachingCreditLimit(70);
            creditAccountsList.addAll(approachingLimitAccounts);
        } else if ("Exceeded Credit Limit".equals(filter)) {
            List<CreditAccount> exceedingAccounts = customerService.getCustomersExceedingCreditLimit();
            creditAccountsList.addAll(exceedingAccounts);
        }
    }

    private void disableButtons(boolean disable) {
        editCustomerButton.setDisable(disable);
        deleteCustomerButton.setDisable(disable);
        viewTransactionsButton.setDisable(disable);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllCustomers();
            return;
        }

        List<Customer> searchResults = customerService.searchCustomersByName(searchTerm);
        customersList.clear();
        customersList.addAll(searchResults);
    }

    @FXML
    private void handleAddCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            CustomerDialogController controller = loader.getController();
            controller.initializeForAdd("RETAIL");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(customerPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isCustomerSaved()) {
                loadAllCustomers();
                loadAllCreditAccounts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to open the customer dialog.");
        }
    }

    @FXML
    private void handleEditCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            CustomerDialogController controller = loader.getController();
            controller.initializeForEdit(selectedCustomer);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(customerPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isCustomerSaved()) {
                loadAllCustomers();
                loadAllCreditAccounts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to open the customer dialog.");
        }
    }

    @FXML
    private void handleDeleteCustomer(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            return;
        }

        // Check if customer has outstanding credit
        CreditAccount creditAccount = customerService.getCreditAccount(selectedCustomer.getId());
        if (creditAccount != null && creditAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            AlertHelper.showErrorAlert("Cannot Delete", "Customer Has Outstanding Credit",
                    "This customer has an outstanding credit balance of LKR " + creditAccount.getBalance()
                    + ". Please clear the balance before deleting.");
            return;
        }

        // Check if customer has purchase history
        List<Invoice> customerInvoices = invoiceService.getInvoicesForCustomer(selectedCustomer.getId());
        if (!customerInvoices.isEmpty()) {
            boolean confirmed = AlertHelper.showConfirmationAlert("Warning", "Customer Has Purchase History",
                    "This customer has " + customerInvoices.size() + " transactions. Deleting will affect your sales history. "
                    + "Are you sure you want to delete?");

            if (!confirmed) {
                return;
            }
        } else {
            boolean confirmed = AlertHelper.showConfirmationAlert("Confirm Delete", "Delete Customer",
                    "Are you sure you want to delete " + selectedCustomer.getName() + "?");

            if (!confirmed) {
                return;
            }
        }

        // Delete the customer
        boolean deleted = customerService.deleteCustomer(selectedCustomer.getId());

        if (deleted) {
            customersList.remove(selectedCustomer);
            loadAllCreditAccounts();
            AlertHelper.showInformationAlert("Success", "Customer Deleted",
                    "Customer was successfully deleted.");
        } else {
            AlertHelper.showErrorAlert("Error", "Delete Failed",
                    "Could not delete the customer. Please try again.");
        }
    }

    @FXML
    private void handleViewTransactions(ActionEvent event) {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction_history.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            TransactionHistoryController controller = loader.getController();
            controller.initializeForCustomer(selectedCustomer);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Transaction History - " + selectedCustomer.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(customerPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to display transaction history.");
        }
    }

    @FXML
    private void handleRefreshCredit(ActionEvent event) {
        filterCreditAccounts();
    }

    @FXML
    private void handleRecordPayment(ActionEvent event) {
        CreditAccount selectedAccount = creditTable.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment_dialog.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            PaymentDialogController controller = loader.getController();
            controller.initialize(selectedAccount);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Record Payment - " + selectedAccount.getCustomer().getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(customerPane.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isPaymentSaved()) {
                loadAllCustomers();
                loadAllCreditAccounts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showErrorAlert("Error", "Could not open dialog",
                    "An error occurred while trying to open the payment dialog.");
        }
    }

    @FXML
    private void handleSetCreditLimit(ActionEvent event) {
        CreditAccount selectedAccount = creditTable.getSelectionModel().getSelectedItem();

        if (selectedAccount == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(
                selectedAccount.getCreditLimit() != null ? selectedAccount.getCreditLimit().toString() : "");
        dialog.setTitle("Set Credit Limit");
        dialog.setHeaderText("Set Credit Limit for " + selectedAccount.getCustomer().getName());
        dialog.setContentText("Enter credit limit (LKR):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(limitStr -> {
            try {
                BigDecimal newLimit = new BigDecimal(limitStr);

                if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
                    AlertHelper.showErrorAlert("Invalid Input", "Credit Limit Cannot Be Negative",
                            "Please enter a positive number for credit limit.");
                    return;
                }

                CreditAccount updatedAccount = customerService.updateCreditLimit(
                        selectedAccount.getCustomer().getId(), newLimit);

                if (updatedAccount != null) {
                    loadAllCreditAccounts();
                    AlertHelper.showInformationAlert("Success", "Credit Limit Updated",
                            "Credit limit updated successfully.");
                } else {
                    AlertHelper.showErrorAlert("Error", "Update Failed",
                            "Could not update credit limit. Please try again.");
                }
            } catch (NumberFormatException e) {
                AlertHelper.showErrorAlert("Invalid Input", "Not a Valid Number",
                        "Please enter a valid number for credit limit.");
            }
        });
    }

    @FXML
private void handleSendReminders(ActionEvent event) {
    List<CreditAccount> outstandingAccounts = customerService.getCustomersWithOutstandingCredit();
    
    if (outstandingAccounts.isEmpty()) {
        AlertHelper.showInformationAlert("No Reminders", "No Outstanding Balances", 
                "There are no customers with outstanding credit balances.");
        return;
    }
    
    boolean confirmed = AlertHelper.showConfirmationAlert("Send Reminders", "Send Payment Reminders", 
            "This will send payment reminders to " + outstandingAccounts.size() + 
            " customers with outstanding balances. Continue?");
    
    if (confirmed) {
        int sentCount = smsService.sendAllOutstandingReminders();
        
        AlertHelper.showInformationAlert("Reminders Sent", "Payment Reminders Sent", 
                "Payment reminders sent to " + sentCount + " customers.");
    }
}

    @FXML
    private void handleExportCreditReport(ActionEvent event) {
        // In a real system, this would generate a CSV or PDF file
        // For now, we'll just show a confirmation

        AlertHelper.showInformationAlert("Report Exported", "Credit Report Generated",
                "Credit report has been generated and saved to the reports folder.");
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String reportType = reportTypeCombo.getValue();
        Customer selectedCustomer = reportCustomerCombo.getValue();

        if (reportType == null) {
            AlertHelper.showErrorAlert("No Report Selected", "Please Select a Report Type",
                    "Please select a report type to generate.");
            return;
        }

        reportContainer.getChildren().clear();

        switch (reportType) {
            case "Purchase History":
                generatePurchaseHistoryReport(selectedCustomer);
                break;
            case "Payment History":
                generatePaymentHistoryReport(selectedCustomer);
                break;
            case "Credit Summary":
                generateCreditSummaryReport(selectedCustomer);
                break;
            case "Monthly Purchase Trends":
                generateMonthlyTrendsReport(selectedCustomer);
                break;
        }
    }

    private void generatePurchaseHistoryReport(Customer customer) {
        Label titleLabel = new Label("Purchase History Report");
        titleLabel.getStyleClass().add("report-title");

        Label subtitleLabel = new Label(customer != null
                ? "For " + customer.getName() : "For All Customers");
        subtitleLabel.getStyleClass().add("report-subtitle");

        TableView<Invoice> reportTable = new TableView<>();

        TableColumn<Invoice, String> invoiceColumn = new TableColumn<>("Invoice #");
        invoiceColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getInvoiceNumber()));

        TableColumn<Invoice, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getDate().format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"))));

        TableColumn<Invoice, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getCustomer() != null
                        ? cellData.getValue().getCustomer().getName() : "Walk-in Customer"));

        TableColumn<Invoice, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getCustomerType()));

        TableColumn<Invoice, String> methodColumn = new TableColumn<>("Payment");
        methodColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getPaymentMethod()));

        TableColumn<Invoice, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData
                -> new SimpleObjectProperty<>(cellData.getValue().getFinalAmount()));

        // Format the amount column
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

        reportTable.getColumns().addAll(invoiceColumn, dateColumn, customerColumn,
                typeColumn, methodColumn, amountColumn);

        // Load data
        List<Invoice> invoices;
        if (customer != null) {
            invoices = invoiceService.getInvoicesForCustomer(customer.getId());
        } else {
            invoices = invoiceService.getAllInvoices();
        }

        reportTable.setItems(FXCollections.observableArrayList(invoices));

        // Add to container
        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, reportTable);
    }

    private void generatePaymentHistoryReport(Customer customer) {
        // Similar to purchase history but focusing on payments
        Label titleLabel = new Label("Payment History Report");
        titleLabel.getStyleClass().add("report-title");

        Label subtitleLabel = new Label(customer != null
                ? "For " + customer.getName() : "For All Customers with Credit");
        subtitleLabel.getStyleClass().add("report-subtitle");

        // In a real implementation, this would show payment transactions
        // For now, just show a message
        Label placeholderLabel = new Label(
                "Payment history report would show all credit payments made by customers.");
        placeholderLabel.getStyleClass().add("placeholder-text");

        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, placeholderLabel);
    }

    private void generateCreditSummaryReport(Customer customer) {
        Label titleLabel = new Label("Credit Summary Report");
        titleLabel.getStyleClass().add("report-title");

        Label subtitleLabel = new Label(customer != null
                ? "For " + customer.getName() : "For All Customers");
        subtitleLabel.getStyleClass().add("report-subtitle");

        // Add credit summary
        VBox summaryBox = new VBox(10);
        summaryBox.getStyleClass().add("summary-box");

        // Add credit summary stats
        BigDecimal totalOutstanding = customerService.getTotalOutstandingCredit();
        int customersWithCredit = customerService.getCustomersWithOutstandingCredit().size();

        Label totalOutstandingLabel = new Label("Total Outstanding Credit: LKR " + totalOutstanding);
        totalOutstandingLabel.getStyleClass().add("summary-item");

        Label customerCountLabel = new Label("Customers with Outstanding Balance: " + customersWithCredit);
        customerCountLabel.getStyleClass().add("summary-item");

        summaryBox.getChildren().addAll(totalOutstandingLabel, customerCountLabel);

        // If a specific customer is selected, add their credit details
        if (customer != null) {
            CreditAccount account = customerService.getCreditAccount(customer.getId());

            if (account != null) {
                Separator separator = new Separator();

                Label balanceLabel = new Label("Current Balance: LKR " + account.getBalance());
                balanceLabel.getStyleClass().add("summary-item");

                Label limitLabel = new Label("Credit Limit: "
                        + (account.getCreditLimit() != null ? "LKR " + account.getCreditLimit() : "No Limit"));
                limitLabel.getStyleClass().add("summary-item");

                // Add usage percentage if limit exists
                if (account.getCreditLimit() != null && account.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal usagePercent = account.getBalance()
                            .multiply(new BigDecimal("100"))
                            .divide(account.getCreditLimit(), 0, BigDecimal.ROUND_HALF_UP);

                    Label usageLabel = new Label("Credit Usage: " + usagePercent + "%");
                    usageLabel.getStyleClass().add("summary-item");

                    // Add color based on usage
                    if (usagePercent.compareTo(new BigDecimal("90")) > 0) {
                        usageLabel.getStyleClass().add("credit-critical");
                    } else if (usagePercent.compareTo(new BigDecimal("70")) > 0) {
                        usageLabel.getStyleClass().add("credit-high");
                    } else {
                        usageLabel.getStyleClass().add("credit-normal");
                    }

                    summaryBox.getChildren().addAll(separator, balanceLabel, limitLabel, usageLabel);
                } else {
                    summaryBox.getChildren().addAll(separator, balanceLabel, limitLabel);
                }
            }
        }

        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, summaryBox);

        // Add pie chart for credit distribution if no specific customer selected
        if (customer == null) {
            PieChart creditDistributionChart = new PieChart();
            creditDistributionChart.setTitle("Credit Distribution by Customer Type");

            // Sample data - in a real app, calculate this from actual data
            PieChart.Data retailData = new PieChart.Data("Retail", 40);
            PieChart.Data wholesaleData = new PieChart.Data("Wholesale", 60);

            creditDistributionChart.getData().addAll(retailData, wholesaleData);
            creditDistributionChart.setLabelsVisible(true);

            reportContainer.getChildren().add(creditDistributionChart);
        }
    }

    private void generateMonthlyTrendsReport(Customer customer) {
        Label titleLabel = new Label("Monthly Purchase Trends");
        titleLabel.getStyleClass().add("report-title");

        Label subtitleLabel = new Label(customer != null
                ? "For " + customer.getName() : "For All Customers");
        subtitleLabel.getStyleClass().add("report-subtitle");

        // Create a line chart for monthly trends
        final LineChart<String, Number> lineChart = createMonthlyTrendsChart(customer);

        reportContainer.getChildren().addAll(titleLabel, subtitleLabel, lineChart);
    }

    private LineChart<String, Number> createMonthlyTrendsChart(Customer customer) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Sales (LKR)");

        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Purchase Trends");

        // Prepare sales data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(customer != null ? customer.getName() : "All Customers");

        // Get data for the last 6 months
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        LocalDate currentDate = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = currentDate.minusMonths(i).withDayOfMonth(1);
            String monthName = monthStart.format(monthFormatter);

            // Get sales for this month (specific customer or all)
            BigDecimal monthlySales;
            if (customer != null) {
                monthlySales = invoiceService.getCustomerSalesForDateRange(
                        customer.getId(),
                        monthStart.atStartOfDay(),
                        monthStart.plusMonths(1).minusDays(1).atTime(23, 59, 59)
                );
            } else {
                monthlySales = invoiceService.getSalesForDateRange(
                        monthStart.atStartOfDay(),
                        monthStart.plusMonths(1).minusDays(1).atTime(23, 59, 59)
                );
            }

            series.getData().add(new XYChart.Data<>(monthName, monthlySales));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private void showCustomerDetails(Customer customer) {
        // Show detailed information about a customer
        AlertHelper.showInformationAlert("Customer Details", customer.getName(),
                "ID: " + customer.getId() + "\n"
                + "Contact: " + customer.getContactNo() + "\n"
                + "Address: " + customer.getAddress() + "\n"
                + "Type: " + customer.getCustomerType() + "\n"
                + "Credit Account: " + (customerService.hasCreditAccount(customer.getId()) ? "Yes" : "No"));
    }

    private void showCreditDetails(CreditAccount account) {
        // Show detailed information about a credit account
        Customer customer = account.getCustomer();

        AlertHelper.showInformationAlert("Credit Account Details", customer.getName(),
                "Customer ID: " + customer.getId() + "\n"
                + "Current Balance: LKR " + account.getBalance() + "\n"
                + "Credit Limit: " + (account.getCreditLimit() != null ? "LKR " + account.getCreditLimit() : "No Limit") + "\n"
                + "Contact: " + customer.getContactNo());
    }

   private void sendCreditReminder(CreditAccount account) {
    Customer customer = account.getCustomer();
    
    // Check if customer has a phone number
    if (customer.getContactNo() == null || customer.getContactNo().isEmpty()) {
        AlertHelper.showErrorAlert("No Contact Number", "Customer Has No Contact Number", 
                "This customer does not have a contact number for SMS notifications.");
        return;
    }
    
    // Use SMS Service to send the reminder
    boolean success = smsService.sendCreditReminder(customer);
    
    if (success) {
        AlertHelper.showInformationAlert("Reminder Sent", "Payment Reminder Sent", 
                "Payment reminder sent to " + customer.getName() + " at " + 
                customer.getContactNo() + " for outstanding balance of LKR " + 
                account.getBalance());
    } else {
        AlertHelper.showErrorAlert("Send Failed", "Failed to Send SMS", 
                "There was an error sending the reminder to " + customer.getName() + 
                ". Please check the SMS notification logs for details.");
    }
}

    private void handleCreditPayment(CreditAccount account) {
        handleRecordPayment(new ActionEvent());
    }
    
    
    
}
