package com.nimesh.controller;

import com.nimesh.model.User;
import com.nimesh.service.LoginService;
import com.nimesh.util.SessionManager;
import com.nimesh.util.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @Autowired
    private LoginService loginService;
    
    @Autowired
    private StageManager stageManager;
    
    @FXML
    public void initialize() {
        // Setup enter key handling for the password field
        passwordField.setOnKeyPressed(this::handleEnterKey);
        usernameField.setOnKeyPressed(this::handleEnterKey);
    }
    
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLoginAction(new ActionEvent());
        }
    }
    
    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password");
            return;
        }
        
        boolean isAuthenticated = loginService.authenticate(username, password);
        
        if (isAuthenticated) {
            // Get user and store in session
            User user = loginService.getUserByUsername(username);
            SessionManager.getInstance().setCurrentUser(user);
            
            // Redirect based on user role
            if (user.getUserType().equals("ADMIN")) {
                stageManager.showDashboard();
            } else if (user.getUserType().equals("EMPLOYEE")) {
                stageManager.showPOSScreen();
            } else {
                // Handle unknown role
                showAlert(Alert.AlertType.WARNING, "Access Error", 
                         "Unknown user role. Please contact your administrator.");
                // Clear session on error
                SessionManager.getInstance().clearSession();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}