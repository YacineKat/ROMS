package com.restaurant.roms;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class AdminLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private RadioButton adminRadio;
    @FXML private RadioButton managerRadio;
    @FXML private ToggleGroup loginType;
    
    private UserDAO userDAO;
    
    public AdminLoginController() {
        this.userDAO = new UserDAO();
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        User user = userDAO.authenticate(username, password);
        
        if (user != null) {
            if (adminRadio.isSelected() && user.isAdmin()) {
                loadDashboard("admin_dashboard.fxml", "Restaurant Admin Dashboard");
            } else if (managerRadio.isSelected() && user.isManager()) {
                loadDashboard("kitchen_dashboard.fxml", "Kitchen Dashboard");
            } else {
                errorLabel.setText("Invalid role selection for this user");
                errorLabel.setVisible(true);
            }
        } else {
            errorLabel.setText("Invalid username or password");
            errorLabel.setVisible(true);
        }
    }
    
    private void loadDashboard(String fxmlFile, String title) {
        try {
            // Load the dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setWidth(1500);
            StageManager.applyStageSettings(stage);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading dashboard: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToCustomerView(ActionEvent event) {
        try {
            // Load the customer view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customer_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            StageManager.applyStageSettings(stage);
            stage.setWidth(1100);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 