package com.nimesh.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class StageManager {
    
    private final ApplicationContext context;
    private Stage primaryStage;
    
    @Value("classpath:/fxml/login.fxml")
    private Resource loginResource;
    
    @Value("classpath:/fxml/dashboard.fxml")
    private Resource dashboardResource;
    
    public StageManager(ApplicationContext context) {
        this.context = context;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Nimesh Store - Retail & Wholesale Management System");
        
        // Set minimum size for the application window
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(600);
    }
    
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(loginResource.getURL());
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Reset stage properties before showing login
            primaryStage.setMaximized(false);
            primaryStage.setWidth(800);
            primaryStage.setHeight(500);
            primaryStage.setResizable(false); // Prevent resizing of login window
            
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(dashboardResource.getURL());
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 768);
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            
            // Set dashboard stage properties
            primaryStage.setResizable(true); // Allow resizing for dashboard
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(768);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showPOSScreen() {
        try {
            // Choose appropriate POS screen based on user role
            String fxmlPath;
            
            if (SessionManager.getInstance().isAdmin()) {
                // Admin gets the normal POS screen
                fxmlPath = "/fxml/pos.fxml";
            } else {
                // Employee gets the restricted POS screen
                fxmlPath = "/fxml/employee_pos.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/pos.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }
    }
    
    /**
     * Show a dialog window and return the controller
     * @param fxmlPath Path to the FXML file
     * @param title Dialog title
     * @param modality Dialog modality
     * @return The controller instance
     */
    @SuppressWarnings("unchecked")
    public <T> T showDialog(String fxmlPath, String title, Modality modality) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(modality);
            dialogStage.initOwner(primaryStage);
            dialogStage.setResizable(false);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
            
            dialogStage.setScene(scene);
            dialogStage.centerOnScreen();
            dialogStage.show();
            
            return (T) loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Show a modal dialog and wait for it to close
     * @param fxmlPath Path to the FXML file
     * @param title Dialog title
     * @return The controller instance
     */
    @SuppressWarnings("unchecked")
    public <T> T showModalDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setResizable(false);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
            
            dialogStage.setScene(scene);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
            
            return (T) loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Show a dialog with custom size
     * @param fxmlPath Path to the FXML file
     * @param title Dialog title
     * @param modality Dialog modality
     * @param width Dialog width
     * @param height Dialog height
     * @return The controller instance
     */
    @SuppressWarnings("unchecked")
    public <T> T showDialog(String fxmlPath, String title, Modality modality, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(modality);
            dialogStage.initOwner(primaryStage);
            dialogStage.setResizable(false);
            dialogStage.setWidth(width);
            dialogStage.setHeight(height);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
            
            dialogStage.setScene(scene);
            dialogStage.centerOnScreen();
            dialogStage.show();
            
            return (T) loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}