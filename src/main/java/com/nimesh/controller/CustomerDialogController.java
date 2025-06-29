package com.nimesh.controller;

import com.nimesh.model.Customer;
import com.nimesh.service.CustomerService;
import com.nimesh.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class CustomerDialogController {
    
    @Autowired
    private CustomerService customerService;
    
    @FXML
    private Label dialogTitleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField contactField;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private RadioButton retailRadio;
    
    @FXML
    private RadioButton wholesaleRadio;
    
    @FXML
    private ToggleGroup customerTypeGroup;
    
    @FXML
    private CheckBox creditAccountCheckbox;
    
    @FXML
    private TextField creditLimitField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private Customer customer;
    private boolean customerSaved = false;
    private boolean isEditMode = false;
    private Customer createdCustomer;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isCustomerSaved() {
        return customerSaved;
    }
    
    public Customer getCreatedCustomer() {
        return createdCustomer;
    }
    
    public void initializeForAdd(String customerType) {
        this.customer = new Customer();
        this.isEditMode = false;
        this.dialogTitleLabel.setText("Add New Customer");
        
        // Set customer type based on selection
        if ("WHOLESALE".equals(customerType)) {
            wholesaleRadio.setSelected(true);
        } else {
            retailRadio.setSelected(true);
        }
        
        // Set default credit limit
        creditLimitField.setText("10000");
        
        // Initialize credit limit field visibility
        setupCreditAccountOption();
    }
    
    public void initializeForEdit(Customer customer) {
        this.customer = customer;
        this.isEditMode = true;
        this.dialogTitleLabel.setText("Edit Customer");
        
        // Populate fields with customer data
        nameField.setText(customer.getName());
        contactField.setText(customer.getContactNo());
        addressField.setText(customer.getAddress());
        
        if ("WHOLESALE".equals(customer.getCustomerType())) {
            wholesaleRadio.setSelected(true);
        } else {
            retailRadio.setSelected(true);
        }
        
        // Check if customer has a credit account
        creditAccountCheckbox.setSelected(customerService.hasCreditAccount(customer.getId()));
        if (customerService.hasCreditAccount(customer.getId())) {
            BigDecimal creditLimit = customerService.getCreditAccount(customer.getId()).getCreditLimit();
            creditLimitField.setText(creditLimit != null ? creditLimit.toString() : "");
        }
        
        // Initialize credit limit field visibility
        setupCreditAccountOption();
    }
    
    private void setupCreditAccountOption() {
        // Add listener to credit account checkbox
        creditAccountCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            creditLimitField.setDisable(!newVal);
        });
        
        // Initialize credit limit field state
        creditLimitField.setDisable(!creditAccountCheckbox.isSelected());
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        
        customer.setName(nameField.getText().trim());
        customer.setContactNo(contactField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        customer.setCustomerType(wholesaleRadio.isSelected() ? "WHOLESALE" : "RETAIL");
        
        // Handle credit account
        BigDecimal creditLimit = null;
        if (creditAccountCheckbox.isSelected()) {
            try {
                creditLimit = new BigDecimal(creditLimitField.getText().trim());
            } catch (NumberFormatException e) {
                AlertHelper.showErrorAlert("Validation Error", "Invalid Credit Limit", 
                        "Please enter a valid number for credit limit.");
                return;
            }
        }
        
        if (isEditMode) {
            // Update existing customer
            Customer updatedCustomer = customerService.updateCustomer(customer);
            
            // Update credit limit if needed
            if (creditAccountCheckbox.isSelected()) {
                customerService.updateCreditLimit(customer.getId(), creditLimit);
            }
            
            if (updatedCustomer != null) {
                customerSaved = true;
                createdCustomer = updatedCustomer;
                dialogStage.close();
            } else {
                AlertHelper.showErrorAlert("Save Error", "Could Not Save Customer", 
                        "An error occurred while saving the customer. Please try again.");
            }
        } else {
            // Create new customer
            Customer savedCustomer = customerService.createCustomer(customer, creditLimit);
            
            if (savedCustomer != null && savedCustomer.getId() != null) {
                customerSaved = true;
                createdCustomer = savedCustomer;
                dialogStage.close();
            } else {
                AlertHelper.showErrorAlert("Save Error", "Could Not Save Customer", 
                        "An error occurred while saving the customer. Please try again.");
            }
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
    
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        // Required fields validation
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage.append("Customer name is required.\n");
        }
        
        // Contact number validation
    String contactNumber = contactField.getText().trim();
    if (!contactNumber.isEmpty()) {
        // Check if contact number already exists for another customer
        Customer existingCustomer = customerService.findCustomerByContactNo(contactNumber);
        if (existingCustomer != null && (isEditMode == false || !existingCustomer.getId().equals(customer.getId()))) {
            errorMessage.append("Contact number already exists for another customer: " + existingCustomer.getName() + ".\n");
        }
    }
        
        // If credit account is enabled, validate credit limit
        if (creditAccountCheckbox.isSelected()) {
            if (creditLimitField.getText() == null || creditLimitField.getText().trim().isEmpty()) {
                errorMessage.append("Credit limit is required when credit account is enabled.\n");
            } else {
                try {
                    BigDecimal creditLimit = new BigDecimal(creditLimitField.getText().trim());
                    if (creditLimit.compareTo(BigDecimal.ZERO) < 0) {
                        errorMessage.append("Credit limit cannot be negative.\n");
                    }
                } catch (NumberFormatException e) {
                    errorMessage.append("Credit limit must be a valid number.\n");
                }
            }
        }
        
        if (errorMessage.length() > 0) {
            AlertHelper.showErrorAlert("Validation Error", "Please correct the following errors:", 
                    errorMessage.toString());
            return false;
        }
        
        return true;
    }
}