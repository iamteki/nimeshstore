package com.nimesh.controller;

import com.nimesh.util.WebcamBarcodeScanner;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class BarcodeScannerDialogController implements Initializable {
    
    @FXML
    private BorderPane rootPane;
    
    @FXML
    private ImageView cameraView;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label barcodeLabel;
    
    @FXML
    private Button closeButton;
    
    private Stage dialogStage;
    private WebcamBarcodeScanner barcodeScanner;
    private String scannedBarcode;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        barcodeScanner = new WebcamBarcodeScanner();
        
        // Bind the camera view to the scanner's image property
        cameraView.imageProperty().bind(barcodeScanner.imageProperty());
        
        // Listen for barcodes
        barcodeScanner.barcodeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                scannedBarcode = newValue;
                barcodeLabel.setText("Barcode: " + newValue);
                // Automatically close after scanning
                dialogStage.close();
            }
        });
        
        // Start with a status message
        statusLabel.setText("Point camera at barcode");
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        // When dialog is shown, start the scanner
        dialogStage.setOnShown(event -> barcodeScanner.start());
        
        // When dialog is hidden, stop the scanner
        dialogStage.setOnHidden(event -> barcodeScanner.stop());
    }
    
    public String getScannedBarcode() {
        return scannedBarcode;
    }
    
    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}