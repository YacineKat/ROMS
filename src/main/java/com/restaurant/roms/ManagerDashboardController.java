package com.restaurant.roms;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Optional;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

public class ManagerDashboardController {
    @FXML private VBox dashboardView;
    @FXML private TableView<MenuItem> menuItemsTable;
    @FXML private TableColumn<MenuItem, Integer> idColumn;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private TableColumn<MenuItem, String> categoryColumn;
    @FXML private TableColumn<MenuItem, String> imagePathColumn;
    
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, Integer> feedbackIdColumn;
    @FXML private TableColumn<Feedback, String> customerNameColumn;
    @FXML private TableColumn<Feedback, Integer> ratingColumn;
    @FXML private TableColumn<Feedback, String> commentColumn;
    @FXML private TableColumn<Feedback, String> dateColumn;
    
    @FXML private TextField menuNameField;
    @FXML private TextField menuPriceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField imagePathField;
    @FXML private Label statusLabel;
    
    private MenuItemDAO menuItemDAO;
    private FeedbackDAO feedbackDAO;
    private User currentUser;
    
    public void initialize() {
        menuItemDAO = new MenuItemDAO();
        feedbackDAO = new FeedbackDAO();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        imagePathColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        
        feedbackIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Initialize category combo box
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Appetizers", "Main Course", "Desserts", "Beverages"
        ));
        
        // Load initial data
        loadMenuItems();
        loadFeedback();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Enable/disable features based on permissions
        if (!user.hasPermission("add_item")) {
            menuNameField.setDisable(true);
            menuPriceField.setDisable(true);
            categoryComboBox.setDisable(true);
            imagePathField.setDisable(true);
        }
    }
    
    private void loadMenuItems() {
        List<MenuItem> items = menuItemDAO.getAllMenuItems();
        menuItemsTable.setItems(FXCollections.observableArrayList(items));
    }
    
    private void loadFeedback() {
        try {
            List<Feedback> feedbackList = feedbackDAO.getAllFeedback();
            if (feedbackList.isEmpty()) {
                statusLabel.setText("No feedback entries found");
                statusLabel.setTextFill(Color.ORANGE);
            } else {
                feedbackTable.setItems(FXCollections.observableArrayList(feedbackList));
                statusLabel.setText("Feedback loaded successfully (" + feedbackList.size() + " entries)");
                statusLabel.setTextFill(Color.GREEN);
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading feedback: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddItem() {
        if (!currentUser.hasPermission("add_item")) {
            showAlert("Permission Denied", "You don't have permission to add menu items.");
            return;
        }
        
        try {
            String name = menuNameField.getText();
            double price = Double.parseDouble(menuPriceField.getText());
            String category = categoryComboBox.getValue();
            String imagePath = imagePathField.getText();
            
            MenuItem item = new MenuItem(0, name, price, category, imagePath, 0);
            menuItemDAO.addMenuItem(item);
            
            loadMenuItems();
            clearForm();
            statusLabel.setText("Item added successfully!");
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid price format!");
        } catch (Exception e) {
            statusLabel.setText("Error adding item: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteItem() {
        if (!currentUser.hasPermission("delete_item")) {
            showAlert("Permission Denied", "You don't have permission to delete menu items.");
            return;
        }
        
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select an item to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Menu Item");
        alert.setContentText("Are you sure you want to delete " + selectedItem.getName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            menuItemDAO.deleteMenuItem(selectedItem.getId());
            loadMenuItems();
            statusLabel.setText("Item deleted successfully!");
        }
    }
    
    @FXML
    private void handleDeleteFeedback(ActionEvent event) {
        Feedback selectedFeedback = feedbackTable.getSelectionModel().getSelectedItem();
        if (selectedFeedback != null) {
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Feedback");
            confirmAlert.setContentText(
                    "Are you sure you want to delete this feedback from " + selectedFeedback.getName() + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleted = feedbackDAO.deleteFeedback(selectedFeedback.getId());
                if (deleted) {
                    loadFeedback(); // Refresh the table
                    statusLabel.setText("Feedback deleted successfully");
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    statusLabel.setText("Failed to delete feedback");
                    statusLabel.setTextFill(Color.RED);
                }
            }
        } else {
            statusLabel.setText("Please select a feedback to delete");
            statusLabel.setTextFill(Color.ORANGE);
        }
    }
    
    @FXML
    private void handleUpdateProfile() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Update Profile");
        dialog.setHeaderText("Update your profile information");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField(currentUser.getUsername());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave blank to keep current)");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newUsername = usernameField.getText();
                String newPassword = passwordField.getText();
                
                if (newUsername.isEmpty()) {
                    showAlert("Error", "Username cannot be empty.");
                    return null;
                }
                
                UserDAO userDAO = new UserDAO();
                if (userDAO.updateUser(currentUser.getId(), newUsername, 
                    newPassword.isEmpty() ? null : newPassword)) {
                    currentUser.setUsername(newUsername);
                    return currentUser;
                }
            }
            return null;
        });
        
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            showAlert("Success", "Profile updated successfully!");
        });
    }
    
    @FXML
    private void handleRefreshFeedback() {
        loadFeedback();
    }
    
    @FXML
    private void handleClearForm() {
        menuNameField.clear();
        menuPriceField.clear();
        categoryComboBox.setValue(null);
        imagePathField.clear();
        statusLabel.setText("");
    }
    
    @FXML
    private void handleLogout() {
        try {
            StageManager.getInstance().loadScene("welcome.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load welcome screen: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void clearForm() {
        // Clear all form fields
        menuNameField.clear();
        menuPriceField.clear();
        categoryComboBox.setValue(null);
        imagePathField.clear();
    }
} 