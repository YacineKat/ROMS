package com.restaurant.roms;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import com.restaurant.roms.MenuItem;
import com.restaurant.roms.MenuItemDAO;
import com.restaurant.roms.Feedback;
import com.restaurant.roms.FeedbackDAO;
import javafx.geometry.Insets;

public class ManagerDashboardController {
    @FXML private TextField menuNameField;
    @FXML private TextField menuPriceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField imagePathField;
    @FXML private Label statusLabel;
    
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
    
    private MenuItemDAO menuItemDAO;
    private FeedbackDAO feedbackDAO;
    
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
        categoryComboBox.getItems().addAll("Appetizers", "Main Course", "Desserts", "Beverages");
        
        // Load initial data
        refreshMenuItems();
        refreshFeedback();
    }
    
    @FXML
    private void handleAddItem(ActionEvent event) {
        try {
            String name = menuNameField.getText();
            double price = Double.parseDouble(menuPriceField.getText());
            String category = categoryComboBox.getValue();
            String imagePath = imagePathField.getText();
            
            MenuItem item = new MenuItem();
            item.setName(name);
            item.setPrice(price);
            item.setCategory(category);
            item.setImagePath(imagePath);
            
            menuItemDAO.addMenuItem(item);
            refreshMenuItems();
            clearForm();
            statusLabel.setText("Item added successfully!");
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid price!");
        }
    }
    
    @FXML
    private void handleUpdateItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            statusLabel.setText("Please select an item to update!");
            return;
        }
        
        try {
            selectedItem.setName(menuNameField.getText());
            selectedItem.setPrice(Double.parseDouble(menuPriceField.getText()));
            selectedItem.setCategory(categoryComboBox.getValue());
            selectedItem.setImagePath(imagePathField.getText());
            
            menuItemDAO.updateMenuItem(selectedItem);
            refreshMenuItems();
            clearForm();
            statusLabel.setText("Item updated successfully!");
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid price!");
        } catch (SQLException e) {
            statusLabel.setText("Error updating item: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            statusLabel.setText("Please select an item to delete!");
            return;
        }
        
        try {
            menuItemDAO.deleteMenuItem(selectedItem.getId());
            refreshMenuItems();
            clearForm();
            statusLabel.setText("Item deleted successfully!");
        } catch (SQLException e) {
            statusLabel.setText("Error deleting item: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearForm(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        menuNameField.clear();
        menuPriceField.clear();
        categoryComboBox.setValue(null);
        imagePathField.clear();
        menuItemsTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void handleRefreshFeedback(ActionEvent event) {
        refreshFeedback();
    }
    
    private void refreshMenuItems() {
        try {
            List<MenuItem> items = menuItemDAO.getAllMenuItems();
            menuItemsTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            statusLabel.setText("Error loading menu items: " + e.getMessage());
        }
    }
    
    private void refreshFeedback() {
        try {
            List<Feedback> feedbacks = feedbackDAO.getAllFeedback();
            feedbackTable.setItems(FXCollections.observableArrayList(feedbacks));
        } catch (SQLException e) {
            statusLabel.setText("Error loading feedback: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleKitchenDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("kitchen_dashboard.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) menuNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Kitchen Dashboard");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Error loading kitchen dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToCustomerView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) menuNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Error loading customer view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin_login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) menuNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Staff Login");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Error logging out: " + e.getMessage());
        }
    }
} 