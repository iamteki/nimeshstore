package com.nimesh.controller;

import com.nimesh.model.User;
import com.nimesh.service.ProductBatchService;
import com.nimesh.service.SystemConfigService;
import com.nimesh.service.UserService;
import com.nimesh.util.AlertHelper;
import com.nimesh.util.StageManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class SettingsController implements Initializable {

    // User Management components
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userIdColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> userTypeColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private Button addUserBtn;
    @FXML
    private Button refreshUsersBtn;

    // Pricing Strategy components
    @FXML
    private RadioButton fifoRadio;
    @FXML
    private RadioButton lifoRadio;
    @FXML
    private RadioButton averageRadio;
    @FXML
    private ToggleGroup pricingStrategyGroup;

    // Batch Management components
    @FXML
    private Button synchronizeBatchesBtn;

    // Services
    @Autowired
    private SystemConfigService configService;
    @Autowired
    private ProductBatchService productBatchService;
    @Autowired
    private UserService userService;
    @Autowired
    private StageManager stageManager;

    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize pricing strategy
        initializePricingStrategy();
        
        // Initialize user management
        initializeUserManagement();
        
        // Load users
        loadUsers();
    }

    private void initializePricingStrategy() {
        // Load current pricing strategy
        String currentStrategy = configService.getPricingStrategy();
        
        switch (currentStrategy) {
            case SystemConfigService.FIFO_STRATEGY:
                fifoRadio.setSelected(true);
                break;
            case SystemConfigService.LIFO_STRATEGY:
                lifoRadio.setSelected(true);
                break;
            case SystemConfigService.AVERAGE_STRATEGY:
                averageRadio.setSelected(true);
                break;
            default:
                fifoRadio.setSelected(true);
        }
    }

    private void initializeUserManagement() {
        // Set up table columns
        userIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getId().toString()));
        
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        
        userTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUserType()));

        // Set up actions column
        actionsColumn.setCellFactory(col -> new TableCell<User, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button resetPasswordBtn = new Button("Reset Password");

            {
                editBtn.getStyleClass().addAll("user-action-button", "edit-user-button");
                deleteBtn.getStyleClass().addAll("user-action-button", "delete-user-button");
                resetPasswordBtn.getStyleClass().addAll("user-action-button", "reset-password-button");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });

                resetPasswordBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleResetPassword(user);
                });

                actionBox.getChildren().addAll(editBtn, resetPasswordBtn, deleteBtn);
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Hide delete button for the last admin
                    if (user.isAdmin() && userService.countByUserType("ADMIN") <= 1) {
                        deleteBtn.setVisible(false);
                    } else {
                        deleteBtn.setVisible(true);
                    }
                    setGraphic(actionBox);
                }
            }
        });

        // Set table data
        usersTable.setItems(usersList);
    }

    private void loadUsers() {
        try {
            List<User> users = userService.findAllUsers();
            usersList.clear();
            usersList.addAll(users);
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Error", "Failed to load users", e.getMessage());
        }
    }

    // User Management Event Handlers
    @FXML
    public void handleAddUser(ActionEvent event) {
        try {
            UserDialogController controller = stageManager.showDialog(
                "/fxml/user_dialog.fxml",
                "Add New User",
                Modality.APPLICATION_MODAL
            );
            
            if (controller != null) {
                controller.setUserService(userService);
                controller.setOnUserSaved(this::loadUsers);
            }
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Error", "Failed to open user dialog", e.getMessage());
        }
    }

    @FXML
    public void handleRefreshUsers(ActionEvent event) {
        loadUsers();
        AlertHelper.showInformationAlert("Refresh Complete", "Users Refreshed", 
            "User list has been refreshed successfully.");
    }

    private void handleEditUser(User user) {
        try {
            UserDialogController controller = stageManager.showDialog(
                "/fxml/user_dialog.fxml",
                "Edit User",
                Modality.APPLICATION_MODAL
            );
            
            if (controller != null) {
                controller.setUserService(userService);
                controller.setUser(user);
                controller.setOnUserSaved(this::loadUsers);
            }
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Error", "Failed to open user dialog", e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        // Prevent deletion of the current user
        if (user.getUsername().equals(getCurrentUsername())) {
            AlertHelper.showWarningAlert("Cannot Delete User", "Self-deletion Not Allowed", 
                "You cannot delete your own user account.");
            return;
        }

        // Prevent deletion of the last admin user
        if (user.isAdmin() && userService.countByUserType("ADMIN") <= 1) {
            AlertHelper.showWarningAlert("Cannot Delete User", "Last Admin User", 
                "Cannot delete the last admin user. At least one admin user must exist.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmationAlert(
            "Delete User",
            "Confirm User Deletion",
            "Are you sure you want to delete user '" + user.getUsername() + "'? This action cannot be undone.");

        if (confirmed) {
            try {
                userService.deleteUser(user.getId());
                loadUsers();
                AlertHelper.showInformationAlert("User Deleted", "Success", 
                    "User '" + user.getUsername() + "' has been deleted successfully.");
            } catch (Exception e) {
                AlertHelper.showErrorAlert("Delete Failed", "Failed to delete user", e.getMessage());
            }
        }
    }

    private void handleResetPassword(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for user: " + user.getUsername());
        dialog.setContentText("Enter new password:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newPassword = result.get().trim();
            
            if (newPassword.length() < 6) {
                AlertHelper.showWarningAlert("Invalid Password", "Password Too Short", 
                    "Password must be at least 6 characters long.");
                return;
            }

            try {
                userService.resetPassword(user.getId(), newPassword);
                AlertHelper.showInformationAlert("Password Reset", "Success", 
                    "Password for user '" + user.getUsername() + "' has been reset successfully.");
            } catch (Exception e) {
                AlertHelper.showErrorAlert("Reset Failed", "Failed to reset password", e.getMessage());
            }
        }
    }

    private String getCurrentUsername() {
        // This should return the currently logged-in user's username
        // You might need to implement this based on your session management
        // For now, returning a placeholder - replace with actual implementation
        return "admin"; 
    }

    // Pricing Strategy Event Handlers
    @FXML
    public void handleSavePricingStrategy(ActionEvent event) {
        String strategy;
        
        if (fifoRadio.isSelected()) {
            strategy = SystemConfigService.FIFO_STRATEGY;
        } else if (lifoRadio.isSelected()) {
            strategy = SystemConfigService.LIFO_STRATEGY;
        } else if (averageRadio.isSelected()) {
            strategy = SystemConfigService.AVERAGE_STRATEGY;
        } else {
            strategy = SystemConfigService.FIFO_STRATEGY;
        }
        
        configService.setPricingStrategy(strategy);
        AlertHelper.showInformationAlert("Settings Saved", 
            "Pricing Strategy Updated", 
            "Product pricing strategy has been updated to: " + strategy);
    }

    // Batch Management Event Handlers
    @FXML
    public void handleSynchronizeBatches(ActionEvent event) {
        // Show confirmation dialog
        boolean confirm = AlertHelper.showConfirmationAlert(
            "Synchronize Batches", 
            "Synchronize Inventory Batches",
            "This will create batches for any unbatched inventory and update product stock levels. Continue?"
        );
        
        if (confirm) {
            try {
                int batchesCreated = productBatchService.synchronizeBatchesWithProductStock();
                
                AlertHelper.showInformationAlert(
                    "Synchronization Complete", 
                    "Batch Synchronization Successful",
                    "Created " + batchesCreated + " new batches for unbatched inventory."
                );
            } catch (Exception e) {
                AlertHelper.showErrorAlert(
                    "Synchronization Failed", 
                    "Batch Synchronization Failed",
                    "Error: " + e.getMessage()
                );
            }
        }
    }
}