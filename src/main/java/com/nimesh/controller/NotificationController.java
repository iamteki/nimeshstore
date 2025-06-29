package com.nimesh.controller;

import com.nimesh.model.CreditAccount;
import com.nimesh.model.Customer;
import com.nimesh.model.SMSNotification;
import com.nimesh.service.CustomerService;
import com.nimesh.service.SMSNotificationService;
import com.nimesh.service.SMSService;
import com.nimesh.util.AlertHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class NotificationController implements Initializable {
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private SMSNotificationService notificationService;
    
    @Value("${sms.enabled}")
    private boolean smsEnabled;
    
    @FXML
    private BorderPane notificationPane;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private TableView<SMSNotification> notificationTable;
    
    @FXML
    private TableColumn<SMSNotification, String> phoneColumn;
    
    @FXML
    private TableColumn<SMSNotification, String> messageColumn;
    
    @FXML
    private TableColumn<SMSNotification, String> dateColumn;
    
    @FXML
    private TableColumn<SMSNotification, String> statusColumn;
    
    @FXML
    private Button sendAllRemindersButton;
    
    @FXML
    private Button sendLimitWarningsButton;
    
    @FXML
    private Button sendCustomMessageButton;
    
    @FXML
    private Button refreshButton;
    
    private ObservableList<SMSNotification> notificationsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set SMS status
        if (smsEnabled) {
            statusLabel.setText("SMS Service: ENABLED");
            statusLabel.getStyleClass().add("status-enabled");
        } else {
            statusLabel.setText("SMS Service: DISABLED (Development Mode)");
            statusLabel.getStyleClass().add("status-disabled");
        }
        
        // Setup filter combo
        filterComboBox.getItems().addAll(
                "All Notifications",
                "Today's Notifications",
                "Successful Notifications",
                "Failed Notifications"
        );
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setOnAction(event -> loadNotifications());
        
        // Initialize table
        initializeTable();
        
        // Load notifications
        loadNotifications();
    }
    
    private void initializeTable() {
        phoneColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        
        messageColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getMessage()));
        
        dateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSentDate()
                        .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"))));
        
        statusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Style status column based on success/failure
        statusColumn.setCellFactory(column -> new TableCell<SMSNotification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("FAILED".equals(item)) {
                        getStyleClass().add("status-failed");
                    } else if ("SIMULATED".equals(item)) {
                        getStyleClass().add("status-simulated");
                    } else if ("DELIVERED".equals(item) || "SENT".equals(item)) {
                        getStyleClass().add("status-success");
                    }
                }
            }
        });
        
        notificationTable.setItems(notificationsList);
    }
    
    private void loadNotifications() {
        String filter = filterComboBox.getValue();
        List<SMSNotification> notifications;
        
        if ("Today's Notifications".equals(filter)) {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
            notifications = notificationService.getNotificationsBetween(startOfDay, endOfDay);
        } else if ("Successful Notifications".equals(filter)) {
            notifications = notificationService.getNotificationsByStatus("DELIVERED");
            notifications.addAll(notificationService.getNotificationsByStatus("SENT"));
            notifications.addAll(notificationService.getNotificationsByStatus("SIMULATED"));
        } else if ("Failed Notifications".equals(filter)) {
            notifications = notificationService.getNotificationsByStatus("FAILED");
        } else {
            notifications = notificationService.getRecentNotifications();
        }
        
        notificationsList.clear();
        notificationsList.addAll(notifications);
    }
    
    @FXML
    private void handleSendAllReminders(ActionEvent event) {
        // Check if there are customers with outstanding balances
        List<CreditAccount> outstandingAccounts = customerService.getCustomersWithOutstandingCredit();
        
        if (outstandingAccounts.isEmpty()) {
            AlertHelper.showInformationAlert("No Reminders Needed", "No Outstanding Balances", 
                    "There are no customers with outstanding credit balances.");
            return;
        }
        
        // Confirm sending
        boolean confirmed = AlertHelper.showConfirmationAlert("Send Reminders", 
                "Send Payment Reminders", 
                "This will send payment reminders to " + outstandingAccounts.size() + 
                " customers with outstanding balances. Continue?");
        
        if (confirmed) {
            int sentCount = smsService.sendAllOutstandingReminders();
            
            AlertHelper.showInformationAlert("Reminders Sent", "Payment Reminders Sent", 
                    "Reminders sent to " + sentCount + " customers.");
            
            // Refresh the table
            loadNotifications();
        }
    }
    
    @FXML
    private void handleSendLimitWarnings(ActionEvent event) {
        // Check if there are customers approaching their credit limit
        List<CreditAccount> approachingLimitAccounts = 
                customerService.getCustomersApproachingCreditLimit(80);
        
        if (approachingLimitAccounts.isEmpty()) {
            AlertHelper.showInformationAlert("No Warnings Needed", "No Credit Limits Approaching", 
                    "There are no customers approaching their credit limits.");
            return;
        }
        
        // Confirm sending
        boolean confirmed = AlertHelper.showConfirmationAlert("Send Warnings", 
                "Send Credit Limit Warnings", 
                "This will send credit limit warnings to " + approachingLimitAccounts.size() + 
                " customers who are approaching their credit limits. Continue?");
        
        if (confirmed) {
            int sentCount = smsService.sendCreditLimitWarnings(80);
            
            AlertHelper.showInformationAlert("Warnings Sent", "Credit Limit Warnings Sent", 
                    "Warnings sent to " + sentCount + " customers.");
            
            // Refresh the table
            loadNotifications();
        }
    }
    
    @FXML
    private void handleSendCustomMessage(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send Custom Message");
        dialog.setHeaderText("Send a custom SMS to a customer");
        
        // Setup dialog content
        ComboBox<Customer> customerCombo = new ComboBox<>();
        customerCombo.setPromptText("Select Customer");
        customerCombo.setPrefWidth(300);
        customerCombo.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
        
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter message");
        messageArea.setPrefRowCount(5);
        messageArea.setPrefWidth(300);
        
        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, cancelButtonType);
        
        // Create a grid for the form
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Message:"), 0, 1);
        grid.add(messageArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus for customer combo box
        customerCombo.requestFocus();
        
        // Show dialog and handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == sendButtonType) {
                Customer selectedCustomer = customerCombo.getValue();
                String message = messageArea.getText().trim();
                
                if (selectedCustomer == null) {
                    AlertHelper.showErrorAlert("No Customer Selected", "Please Select a Customer", 
                            "You must select a customer to send the message to.");
                    return;
                }
                
                if (message.isEmpty()) {
                    AlertHelper.showErrorAlert("Empty Message", "Please Enter a Message", 
                            "You must enter a message to send.");
                    return;
                }
                
                if (selectedCustomer.getContactNo() == null || selectedCustomer.getContactNo().isEmpty()) {
                    AlertHelper.showErrorAlert("No Contact Number", "Customer Has No Contact Number", 
                            "The selected customer does not have a contact number.");
                    return;
                }
                
                // Send the message
                boolean success = smsService.sendSMS(selectedCustomer.getContactNo(), message);
                
                if (success) {
                    AlertHelper.showInformationAlert("Message Sent", "SMS Sent Successfully", 
                            "The message was sent to " + selectedCustomer.getName() + ".");
                    
                    // Refresh the table
                    loadNotifications();
                } else {
                    AlertHelper.showErrorAlert("Send Failed", "Failed to Send SMS", 
                            "There was an error sending the message. Please try again.");
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadNotifications();
    }
}