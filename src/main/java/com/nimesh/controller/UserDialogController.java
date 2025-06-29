package com.nimesh.controller;

import com.nimesh.model.User;
import com.nimesh.service.UserService;
import com.nimesh.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class UserDialogController implements Initializable {

    @FXML
    private Label dialogTitle;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> userTypeCombo;
    @FXML
    private CheckBox changePasswordOnNextLoginCheck;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private User user; // For editing existing user
    private UserService userService;
    private Runnable onUserSaved;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user type combo box with the roles from your existing system
        userTypeCombo.setItems(FXCollections.observableArrayList(
            "ADMIN", 
            "EMPLOYEE"
            // Add more roles as needed: "MANAGER", "CASHIER", etc.
        ));
        
        // Set default selection
        userTypeCombo.setValue("EMPLOYEE");
        
        // Add validation listeners
        setupValidation();
    }

    private void setupValidation() {
        // Username validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // Password validation
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // Confirm password validation
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // User type validation
        userTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // Initial validation
        validateForm();
    }

    private void validateForm() {
        boolean isValid = true;
        String errorMessage = "";

        // Check username
        if (usernameField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage = "Username is required";
        } else if (usernameField.getText().trim().length() < 3) {
            isValid = false;
            errorMessage = "Username must be at least 3 characters";
        }

        // Check password (only if it's a new user or password is being changed)
        if (user == null || !passwordField.getText().isEmpty()) {
            if (passwordField.getText().isEmpty()) {
                isValid = false;
                errorMessage = "Password is required";
            } else if (passwordField.getText().length() < 6) {
                isValid = false;
                errorMessage = "Password must be at least 6 characters";
            } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                isValid = false;
                errorMessage = "Passwords do not match";
            }
        }

        // Check user type
        if (userTypeCombo.getValue() == null) {
            isValid = false;
            errorMessage = "User role is required";
        }

        saveButton.setDisable(!isValid);
        
        // Update button tooltip with error message
        if (!isValid && !errorMessage.isEmpty()) {
            saveButton.setTooltip(new Tooltip(errorMessage));
        } else {
            saveButton.setTooltip(null);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            // Editing existing user
            dialogTitle.setText("Edit User");
            saveButton.setText("Update User");
            
            usernameField.setText(user.getUsername());
            usernameField.setDisable(true); // Don't allow username changes
            userTypeCombo.setValue(user.getUserType());
            
            // Make password fields optional for editing
            passwordField.setPromptText("Leave empty to keep current password");
            confirmPasswordField.setPromptText("Leave empty to keep current password");
            
            // Don't show change password option for editing
            changePasswordOnNextLoginCheck.setVisible(false);
        } else {
            // Adding new user
            dialogTitle.setText("Add New User");
            saveButton.setText("Save User");
            usernameField.setDisable(false);
            changePasswordOnNextLoginCheck.setVisible(true);
        }
        
        validateForm();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setOnUserSaved(Runnable onUserSaved) {
        this.onUserSaved = onUserSaved;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String userType = userTypeCombo.getValue();

            if (user == null) {
                // Creating new user
                
                // Check if username already exists
                if (userService.existsByUsername(username)) {
                    AlertHelper.showWarningAlert("Username Exists", "Username Already Taken", 
                        "A user with username '" + username + "' already exists. Please choose a different username.");
                    return;
                }

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(passwordEncoder.encode(password));
                newUser.setUserType(userType);
                
                userService.saveUser(newUser);
                
                AlertHelper.showInformationAlert("User Created", "Success", 
                    "User '" + username + "' has been created successfully.");
            } else {
                // Updating existing user
                user.setUserType(userType);
                
                // Only update password if provided
                if (!password.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                }
                
                userService.updateUser(user);
                
                AlertHelper.showInformationAlert("User Updated", "Success", 
                    "User '" + username + "' has been updated successfully.");
            }

            // Call the callback if provided
            if (onUserSaved != null) {
                onUserSaved.run();
            }

            // Close the dialog
            handleCancel(event);

        } catch (Exception e) {
            AlertHelper.showErrorAlert("Save Failed", "Failed to save user", 
                "An error occurred while saving the user: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}