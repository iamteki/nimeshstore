package com.nimesh.controller;

import com.nimesh.model.Supplier;
import com.nimesh.service.SupplierService;
import com.nimesh.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SupplierDialogController {
    
    @Autowired
    private SupplierService supplierService;
    
    @FXML
    private Label dialogTitleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField contactPersonField;
    
    @FXML
    private TextField contactNoField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextArea addressArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private Supplier supplier;
    private boolean supplierSaved = false;
    private boolean isEditMode = false;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isSupplierSaved() {
        return supplierSaved;
    }
    
    public void initializeForAdd() {
        this.supplier = new Supplier();
        this.isEditMode = false;
        this.dialogTitleLabel.setText("Add New Supplier");
    }
    
    public void initializeForEdit(Supplier supplier) {
        this.supplier = supplier;
        this.isEditMode = true;
        this.dialogTitleLabel.setText("Edit Supplier");
        
        // Populate fields with supplier data
        nameField.setText(supplier.getName());
        contactPersonField.setText(supplier.getContactPerson());
        contactNoField.setText(supplier.getContactNo());
        emailField.setText(supplier.getEmail());
        addressArea.setText(supplier.getAddress());
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        
        supplier.setName(nameField.getText().trim());
        supplier.setContactPerson(contactPersonField.getText().trim());
        supplier.setContactNo(contactNoField.getText().trim());
        supplier.setEmail(emailField.getText().trim());
        supplier.setAddress(addressArea.getText().trim());
        
        Supplier savedSupplier = supplierService.saveSupplier(supplier);
        
        if (savedSupplier != null && savedSupplier.getId() != null) {
            supplierSaved = true;
            dialogStage.close();
        } else {
            AlertHelper.showErrorAlert("Save Error", "Could Not Save Supplier", 
                    "An error occurred while saving the supplier. Please try again.");
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
            errorMessage.append("Supplier name is required.\n");
        }
        
        // Contact number validation - optional but must be valid if provided
        String contactNo = contactNoField.getText().trim();
        if (!contactNo.isEmpty() && !contactNo.matches("\\d{10}")) {
            errorMessage.append("Contact number must be a valid 10-digit number.\n");
        }
        
        // Email validation - optional but must be valid if provided
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            errorMessage.append("Email must be a valid email address.\n");
        }
        
        // Check for duplicate contact number or email
        if (!isEditMode) {
            // Only check for duplicates when adding a new supplier
            if (!contactNo.isEmpty() && supplierService.existsByContactNo(contactNo)) {
                errorMessage.append("Contact number is already used by another supplier.\n");
            }
            
            if (!email.isEmpty() && supplierService.existsByEmail(email)) {
                errorMessage.append("Email address is already used by another supplier.\n");
            }
        } else {
            // When editing, check if the contact number or email is used by a different supplier
            String originalContactNo = supplier.getContactNo();
            String originalEmail = supplier.getEmail();
            
            if (!contactNo.isEmpty() && !contactNo.equals(originalContactNo) && 
                    supplierService.existsByContactNo(contactNo)) {
                errorMessage.append("Contact number is already used by another supplier.\n");
            }
            
            if (!email.isEmpty() && !email.equals(originalEmail) && 
                    supplierService.existsByEmail(email)) {
                errorMessage.append("Email address is already used by another supplier.\n");
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