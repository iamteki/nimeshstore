package com.nimesh.controller;

import com.nimesh.model.Product;
import com.nimesh.service.ProductService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller for handling barcode scanning in the POS screen
 */
public class BarcodeScannerHandler {
    
    private ProductService productService;
    private POSController posController;
    
    // Flag to track if we're processing a barcode scan
    private boolean processingBarcode = false;
    
    // Buffer to hold incoming barcode data
    private StringBuilder barcodeBuffer = new StringBuilder();
    
    /**
     * Set the parent POS controller and get the ProductService from it
     */
    public void setPosController(POSController posController) {
        this.posController = posController;
        // Get ProductService from the POSController instead of autowiring
        this.productService = posController.getProductService();
    }
    
    /**
     * Process a key event that could be from a hardware barcode scanner
     */
    public void processKeyEvent(KeyEvent event, TextField barcodeField) {
        // Skip if ProductService wasn't properly initialized
        if (productService == null || posController == null) {
            return;
        }
        
        if (barcodeField.isFocused()) {
            return; // Let the TextField handle the event normally
        }
        
        // Most barcode scanners end with Enter/Return
        if (event.getCode() == KeyCode.ENTER) {
            if (processingBarcode && barcodeBuffer.length() > 0) {
                // Process the complete barcode
                String barcode = barcodeBuffer.toString();
                barcodeBuffer.setLength(0); // Clear buffer
                
                // Set the barcode in the field and trigger search
                Platform.runLater(() -> {
                    barcodeField.setText(barcode);
                    posController.triggerBarcodeEnter(); // Use the new method
                });
                
                processingBarcode = false;
                event.consume();
            }
        } else if (event.getCode() == KeyCode.TAB) {
            // Some scanners use Tab as terminator
            if (processingBarcode && barcodeBuffer.length() > 0) {
                String barcode = barcodeBuffer.toString();
                barcodeBuffer.setLength(0);
                
                Platform.runLater(() -> {
                    barcodeField.setText(barcode);
                    posController.triggerBarcodeEnter(); // Use the new method
                });
                
                processingBarcode = false;
                event.consume();
            }
        } else {
            // Collect the character
            if (event.getCharacter() != null && !event.getCharacter().isEmpty()) {
                char c = event.getCharacter().charAt(0);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                    if (!processingBarcode) {
                        processingBarcode = true;
                        barcodeBuffer.setLength(0);
                    }
                    
                    barcodeBuffer.append(c);
                    
                    // If barcode is reasonable length (most barcodes are 8-14 chars)
                    if (barcodeBuffer.length() >= 8) {
                        try {
                            // Check if the product exists for quick handling
                            Product product = productService.getProductByBarcode(barcodeBuffer.toString());
                            if (product != null) {
                                String barcode = barcodeBuffer.toString();
                                barcodeBuffer.setLength(0);
                                
                                Platform.runLater(() -> {
                                    barcodeField.setText(barcode);
                                    posController.triggerBarcodeEnter(); // Use the new method
                                });
                                
                                processingBarcode = false;
                                event.consume();
                            }
                        } catch (Exception e) {
                            // If any error occurs, just reset and continue
                            processingBarcode = false;
                            barcodeBuffer.setLength(0);
                        }
                    }
                } else {
                    // Non-barcode character, reset
                    processingBarcode = false;
                    barcodeBuffer.setLength(0);
                }
            }
        }
    }
    
    /**
     * Reset the barcode buffer and state
     */
    public void reset() {
        processingBarcode = false;
        barcodeBuffer.setLength(0);
    }
}