package com.nimesh.controller;

import com.nimesh.model.CreditAccount;
import com.nimesh.model.Customer;
import com.nimesh.service.CustomerService;
import com.nimesh.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class PaymentDialogController {
    
    @Autowired
    private CustomerService customerService;
    
    @FXML
    private Label customerNameLabel;
    
    @FXML
    private Label currentBalanceLabel;
    
    @FXML
    private TextField paymentAmountField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private Stage dialogStage;
    private CreditAccount creditAccount;
    private boolean paymentSaved = false;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isPaymentSaved() {
        return paymentSaved;
    }
    
    public void initialize(CreditAccount creditAccount) {
        this.creditAccount = creditAccount;
        Customer customer = creditAccount.getCustomer();
        
        customerNameLabel.setText(customer.getName());
        currentBalanceLabel.setText("LKR " + creditAccount.getBalance().toString());
        
        // Default to full payment amount
        paymentAmountField.setText(creditAccount.getBalance().toString());
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        
        try {
            BigDecimal paymentAmount = new BigDecimal(paymentAmountField.getText().trim());
            
            CreditAccount updatedAccount = customerService.applyCreditPayment(
                    creditAccount.getCustomer().getId(), paymentAmount);
            
            if (updatedAccount != null) {
                paymentSaved = true;
                dialogStage.close();
            } else {
                AlertHelper.showErrorAlert("Payment Error", "Could Not Process Payment", 
                        "An error occurred while processing the payment. Please try again.");
            }
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Input", "Not a Valid Number", 
                    "Please enter a valid number for payment amount.");
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }
    
    private boolean validateInput() {
        String paymentText = paymentAmountField.getText().trim();
        
        if (paymentText.isEmpty()) {
            AlertHelper.showErrorAlert("Empty Amount", "Payment Amount Required", 
                    "Please enter an amount to pay.");
            return false;
        }
        
        try {
            BigDecimal paymentAmount = new BigDecimal(paymentText);
            
            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showErrorAlert("Invalid Amount", "Payment Must Be Positive", 
                        "Please enter a positive amount for payment.");
                return false;
            }
            
            if (paymentAmount.compareTo(creditAccount.getBalance()) > 0) {
                AlertHelper.showErrorAlert("Invalid Amount", "Payment Exceeds Balance", 
                        "Payment amount cannot be more than the current balance.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Input", "Not a Valid Number", 
                    "Please enter a valid number for payment amount.");
            return false;
        }
        
        return true;
    }
}