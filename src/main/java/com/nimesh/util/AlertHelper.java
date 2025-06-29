package com.nimesh.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Helper class for showing alerts in JavaFX
 */
public class AlertHelper {

    /**
     * Shows an error alert with the specified title, header, and content
     * 
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert with the specified title, header, and content
     * 
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    public static void showInformationAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning alert with the specified title, header, and content
     * 
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     */
    public static void showWarningAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation alert with the specified title, header, and content
     * 
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}