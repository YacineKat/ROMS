package com.restaurant.roms;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import com.restaurant.roms.MenuItem;
import com.restaurant.roms.MenuItemDAO;
import com.restaurant.roms.Feedback;
import com.restaurant.roms.FeedbackDAO;
import com.restaurant.roms.User;
import com.restaurant.roms.UserDAO;
import com.restaurant.roms.Order;
import com.restaurant.roms.OrderDAO;

public class AdminDashboardController implements Initializable {

    // Menu Items Table
    @FXML
    private TableView<MenuItem> menuItemsTable;
    @FXML
    private TableColumn<MenuItem, Integer> idColumn;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, Double> priceColumn;
    @FXML
    private TableColumn<MenuItem, String> categoryColumn;
    @FXML
    private TableColumn<MenuItem, String> imagePathColumn;

    // Form Fields
    @FXML
    private TextField menuNameField;
    @FXML
    private TextField menuPriceField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField imagePathField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button browseImageButton;

    // Buttons
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button refreshFeedbackButton;
    @FXML
    private Button addManagerButton;
    @FXML
    private Button deleteManagerButton;

    // Data
    private ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
    private MenuItem selectedMenuItem;
    private int nextId = 13; // Start from 13 as existing items are 1-12

    // Orders Table
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderTimeColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, String> orderItemsColumn;
    @FXML
    private TableColumn<Order, String> orderTotalColumn;

    // Feedback Table
    @FXML
    private TableView<Feedback> feedbackTable;
    @FXML
    private TableColumn<Feedback, Integer> feedbackIdColumn;
    @FXML
    private TableColumn<Feedback, String> customerNameColumn;
    @FXML
    private TableColumn<Feedback, Integer> ratingColumn;
    @FXML
    private TableColumn<Feedback, String> commentColumn;
    @FXML
    private TableColumn<Feedback, String> dateColumn;
    @FXML
    private Button deleteFeedbackButton;

    // Manager Table
    @FXML
    private TableView<User> managersTable;
    @FXML
    private TableColumn<User, Integer> managerIdColumn;
    @FXML
    private TableColumn<User, String> managerUsernameColumn;
    @FXML
    private TableColumn<User, String> managerNameColumn;
    @FXML
    private TableColumn<User, String> managerEmailColumn;

    private OrderDAO orderDAO = new OrderDAO();
    private FeedbackDAO feedbackDAO = new FeedbackDAO();
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize DAOs
            userDAO = new UserDAO();
            orderDAO = new OrderDAO();
            feedbackDAO = new FeedbackDAO();

            // Setup tables
            setupMenuItemsTable();
            setupFeedbackTable();
            setupManagerTable();
            
            // Load data
            loadCategories();
            loadMenuItems();
            loadFeedback();
            refreshManagers();

            // Test database connection
            boolean connected = DatabaseConnection.testConnection();
            if (!connected) {
                statusLabel.setText("Warning: Database connection failed. Using sample data.");
                statusLabel.setTextFill(Color.RED);
            } else {
                statusLabel.setText("Connected to database successfully");
                statusLabel.setTextFill(Color.GREEN);
            }

            setupCategoryComboBox();
            setupImagePicker();
            clearForm();

            // Initialize orders table if it exists
            if (orderIdColumn != null && orderTimeColumn != null && orderStatusColumn != null &&
                    orderItemsColumn != null && orderTotalColumn != null) {
                initializeOrdersTable();
            }
        } catch (Exception e) {
            statusLabel.setText("Error initializing dashboard: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void setupMenuItemsTable() {
        try {
            // Initialize columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            imagePathColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));

            // Format price column
            priceColumn.setCellFactory(column -> new TableCell<MenuItem, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", price));
                    }
                }
            });

            // Add row factory for better visual feedback
            menuItemsTable.setRowFactory(tv -> {
                TableRow<MenuItem> row = new TableRow<>();
                row.setOnMouseEntered(event -> row.setStyle("-fx-background-color: #f0f0f0;"));
                row.setOnMouseExited(event -> row.setStyle(""));
                return row;
            });

            // Set placeholder text
            menuItemsTable.setPlaceholder(new Label("No menu items found"));

            // Add selection listener
            menuItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedMenuItem = newSelection;
                    populateForm(selectedMenuItem);
                }
            });
        } catch (Exception e) {
            statusLabel.setText("Error setting up menu items table: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setEditable(true);
        categoryComboBox.setOnAction(event -> {
            String newCategory = categoryComboBox.getValue();
            if (newCategory != null && !newCategory.isEmpty() && !categoryComboBox.getItems().contains(newCategory)) {
                // Add the new category to the database
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    String sql = "INSERT IGNORE INTO Category (title) VALUES (?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, newCategory);
                        pstmt.executeUpdate();
                    }

                    // Add to the ComboBox items
                    categoryComboBox.getItems().add(newCategory);
                } catch (SQLException e) {
                    statusLabel.setText("Error adding category: " + e.getMessage());
                    statusLabel.setTextFill(Color.RED);
                }
            }
        });
    }

    private void setupImagePicker() {
        browseImageButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            File selectedFile = fileChooser.showOpenDialog(browseImageButton.getScene().getWindow());
            if (selectedFile != null) {
                // Copy the image to the images directory
                try {
                    String imagesDir = "bin/images";
                    File imagesDirectory = new File(imagesDir);
                    if (!imagesDirectory.exists()) {
                        imagesDirectory.mkdirs();
                    }

                    String fileName = selectedFile.getName();
                    File destFile = new File(imagesDirectory, fileName);
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Set the relative path in the text field
                    imagePathField.setText("images/" + fileName);
                } catch (IOException e) {
                    statusLabel.setText("Error copying image: " + e.getMessage());
                    statusLabel.setTextFill(Color.RED);
                }
            }
        });
    }

    private void loadCategories() {
        try {
            // Load categories from database
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            List<String> categories = menuItemDAO.getAllCategories();

            // If database returned categories, use them
            if (!categories.isEmpty()) {
                categoryComboBox.getItems().clear();
                categoryComboBox.getItems().addAll(categories);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error loading categories from database: " + e.getMessage());
        }

        // Fall back to sample categories if database fails or returns empty list
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll("Burger", "Coffee", "Drinks", "Italian", "Mexican", "Chinese", "Hotdog",
                "Snack");
    }

    private void loadMenuItems() {
        try {
            // Clear existing items
            menuItems.clear();

            // Test database connection first
            if (!DatabaseConnection.testConnection()) {
                statusLabel.setText("Warning: Database connection failed. Please check your connection.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // Load menu items from database
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            List<MenuItem> dbMenuItems = menuItemDAO.getAllMenuItems();

            if (dbMenuItems != null && !dbMenuItems.isEmpty()) {
                menuItems.addAll(dbMenuItems);
                menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                menuItemsTable.refresh();
                statusLabel.setText("Menu items loaded successfully (" + dbMenuItems.size() + " items)");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("No menu items found in database");
                statusLabel.setTextFill(Color.ORANGE);
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading menu items: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void populateForm(MenuItem item) {
        menuNameField.setText(item.getName());
        menuPriceField.setText(String.valueOf(item.getPrice()));
        categoryComboBox.setValue(item.getCategory());
        imagePathField.setText(item.getImagePath());

        addButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    @FXML
    private void handleAddItem(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            // Test database connection first
            if (!DatabaseConnection.testConnection()) {
                statusLabel.setText("Error: Database connection failed. Please check your connection.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // Create new menu item from form data
            String name = menuNameField.getText().trim();
            double price = Double.parseDouble(menuPriceField.getText().trim());
            String category = categoryComboBox.getValue();
            String imagePath = imagePathField.getText().trim();

            // Validate image path
            if (imagePath.isEmpty()) {
                imagePath = "images/default-food.png"; // Set default image if none provided
            }

            // First ensure the category exists
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "INSERT IGNORE INTO Category (title) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, category);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                statusLabel.setText("Error ensuring category exists: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // Get or create a kitchen staff member
            int kitchenId = 1; // Default kitchen ID
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                // First check if kitchen staff exists
                String checkSql = "SELECT kitchen_id FROM Staff WHERE role = 'kitchen_staff' LIMIT 1";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkSql)) {
                    if (rs.next()) {
                        kitchenId = rs.getInt("kitchen_id");
                    } else {
                        // If no kitchen staff exists, create one
                        String staffId = "KITCHEN_" + System.currentTimeMillis();
                        String insertSql = "INSERT INTO Staff (staff_id, name, role, kitchen_id) VALUES (?, ?, 'kitchen_staff', ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                            pstmt.setString(1, staffId);
                            pstmt.setString(2, "Default Kitchen Staff");
                            pstmt.setInt(3, kitchenId);
                            pstmt.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                statusLabel.setText("Warning: Using default kitchen ID. " + e.getMessage());
                statusLabel.setTextFill(Color.ORANGE);
            }

            // Create new menu item
            MenuItem newItem = new MenuItem();
            newItem.setName(name);
            newItem.setTitle(name);
            newItem.setPrice(price);
            newItem.setCategory(category);
            newItem.setCategoryTitle(category);
            newItem.setImagePath(imagePath);
            newItem.setKitchenId(kitchenId);
            newItem.setQuantity(0);

            // Add to database
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            int newId = menuItemDAO.insertMenuItem(newItem);

            if (newId > 0) {
                // Database operation successful
                newItem.setId(newId);
                menuItems.add(newItem);
                menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));
                menuItemsTable.refresh();

                statusLabel.setText("Menu item added successfully with ID: " + newId);
                statusLabel.setTextFill(Color.GREEN);
                clearForm();
            } else {
                statusLabel.setText("Failed to add item to database. Please try again.");
                statusLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            statusLabel.setText("Error adding item: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        // Validate name
        if (menuNameField.getText().trim().isEmpty()) {
            statusLabel.setText("Please enter a menu item name");
            statusLabel.setTextFill(Color.RED);
            return false;
        }

        // Validate price
        try {
            double price = Double.parseDouble(menuPriceField.getText().trim());
            if (price <= 0) {
                statusLabel.setText("Please enter a valid price (greater than 0)");
                statusLabel.setTextFill(Color.RED);
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid price");
            statusLabel.setTextFill(Color.RED);
            return false;
        }

        // Validate category
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            statusLabel.setText("Please select a category");
            statusLabel.setTextFill(Color.RED);
            return false;
        }

        return true;
    }

    @FXML
    private void handleUpdateItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && validateInputs()) {
            try {
                // Update menu item with form data
                String name = menuNameField.getText();
                double price = Double.parseDouble(menuPriceField.getText());
                String category = categoryComboBox.getValue();
                String imagePath = imagePathField.getText();

                // Update the selected item
                selectedItem.setName(name);
                selectedItem.setPrice(price);
                selectedItem.setCategory(category);
                selectedItem.setImagePath(imagePath);

                // Update in database
                MenuItemDAO menuItemDAO = new MenuItemDAO();
                boolean updated = menuItemDAO.updateMenuItem(selectedItem);

                if (updated) {
                    // Database operation successful
                    menuItemsTable.refresh();
                    statusLabel.setText("Menu item updated successfully");
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    // Database operation failed
                    menuItemsTable.refresh();
                    statusLabel.setText("Failed to update item in database. Updated in local list only.");
                    statusLabel.setTextFill(Color.ORANGE);
                }

                clearForm();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        } else {
            statusLabel.setText("Please select an item to update and provide valid inputs");
            statusLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleDeleteItem(ActionEvent event) {
        MenuItem selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                // Delete from database first
                MenuItemDAO menuItemDAO = new MenuItemDAO();
                boolean deleted = menuItemDAO.deleteMenuItem(selectedItem.getId());

                if (deleted) {
                    // Database operation successful
                    menuItems.remove(selectedItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));

                    statusLabel.setText("Menu item deleted successfully");
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    // Database operation failed
                    menuItems.remove(selectedItem);
                    menuItemsTable.setItems(FXCollections.observableArrayList(menuItems));

                    statusLabel.setText("Failed to delete from database. Removed from local list only.");
                    statusLabel.setTextFill(Color.ORANGE);
                }

                clearForm();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        } else {
            statusLabel.setText("Please select an item to delete");
            statusLabel.setTextFill(Color.RED);
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
        statusLabel.setText("");
        selectedMenuItem = null;

        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) addButton.getScene().getWindow();
            
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("admin_login.fxml"));
            Parent root = loader.load();
            
            // Create new stage for login
            Stage loginStage = new Stage();
            Scene scene = new Scene(root);
            loginStage.setScene(scene);
            loginStage.setTitle("Admin Login");
            StageManager.applyStageSettings(loginStage);
            loginStage.show();
            
            // Close the current stage
            currentStage.close();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToCustomerView(ActionEvent event) {
        try {
            // Load the customer view
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("customer_view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Restaurant Customer View");
            StageManager.applyStageSettings(stage);
            stage.setWidth(1100);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error loading customer view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKitchenDashboard(ActionEvent event) {
        try {
            // Load the kitchen dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("kitchen_dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) addButton.getScene().getWindow();
            StageManager.initializeStage(stage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Kitchen Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error loading kitchen dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to get the next available ID for a new menu item
    private int getNextAvailableId() {
        return menuItems.stream()
                .mapToInt(MenuItem::getId)
                .max()
                .orElse(0) + 1;
    }

    private void initializeOrdersTable() {
        orderIdColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        orderTimeColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        orderStatusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        orderItemsColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemsSummary()));
        orderTotalColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));

        refreshOrdersTable();
    }

    private void refreshOrdersTable() {
        try {
            List<Order> orders = orderDAO.getOrdersByStatus(Order.OrderStatus.QUEUED);
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.IN_PROGRESS));
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.READY));
            orders.addAll(orderDAO.getOrdersByStatus(Order.OrderStatus.DELIVERED));
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            statusLabel.setText("Error loading orders: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void handleOrderStatusChange(Order order) {
        switch (order.getStatus()) {
            case QUEUED:
                updateOrderStatus(order, Order.OrderStatus.IN_PROGRESS);
                break;
            case IN_PROGRESS:
                updateOrderStatus(order, Order.OrderStatus.READY);
                break;
            case READY:
                updateOrderStatus(order, Order.OrderStatus.DELIVERED);
                break;
            case DELIVERED:
                updateOrderStatus(order, Order.OrderStatus.CANCELLED);
                break;
            default:
                break;
        }
    }

    private void updateOrderStatus(Order order, Order.OrderStatus newStatus) {
        order.setStatus(newStatus);
        orderDAO.updateOrderStatus(order.getOrderId(), newStatus);
        refreshOrdersTable();
    }

    private void handleOrderItemClick(Order.OrderItem orderItem) {
        MenuItem menuItem = orderItem.getMenuItem();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(menuItem.getTitle());
        alert.setContentText(String.format(
                "Price: $%.2f\nQuantity: %d\nCategory: %s\nKitchen: %d",
                menuItem.getPrice(),
                orderItem.getQuantity(),
                menuItem.getCategoryTitle(),
                menuItem.getKitchenId()));
        alert.showAndWait();
    }

    private void setupFeedbackTable() {
        try {
            // Initialize columns
            feedbackIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
            commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

            // Custom cell factory for rating to show stars
            ratingColumn.setCellFactory(column -> new TableCell<Feedback, Integer>() {
                @Override
                protected void updateItem(Integer rating, boolean empty) {
                    super.updateItem(rating, empty);
                    if (empty || rating == null) {
                        setText(null);
                    } else {
                        setText("★".repeat(rating));
                        setStyle("-fx-text-fill: gold;");
                    }
                }
            });

            // Custom cell factory for comment to show truncated text
            commentColumn.setCellFactory(column -> new TableCell<Feedback, String>() {
                @Override
                protected void updateItem(String comment, boolean empty) {
                    super.updateItem(comment, empty);
                    if (empty || comment == null) {
                        setText(null);
                    } else {
                        setText(comment.length() > 50 ? comment.substring(0, 47) + "..." : comment);
                    }
                }
            });

            // Format date column
            dateColumn.setCellFactory(column -> new TableCell<Feedback, String>() {
                @Override
                protected void updateItem(String date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        try {
                            String formattedDate = date.substring(0, 16).replace('T', ' ');
                            setText(formattedDate);
                        } catch (Exception e) {
                            setText(date);
                        }
                    }
                }
            });

            // Add row factory for better visual feedback
            feedbackTable.setRowFactory(tv -> {
                TableRow<Feedback> row = new TableRow<>();
                row.setOnMouseEntered(event -> row.setStyle("-fx-background-color: #f0f0f0;"));
                row.setOnMouseExited(event -> row.setStyle(""));
                return row;
            });

            // Set placeholder text
            feedbackTable.setPlaceholder(new Label("No feedback available"));
        } catch (Exception e) {
            statusLabel.setText("Error setting up feedback table: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefreshFeedback(ActionEvent event) {
        loadFeedback();
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
        } catch (SQLException e) {
            statusLabel.setText("Error loading feedback: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void showFeedbackDetails(Feedback feedback) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Feedback Details");
        alert.setHeaderText("Customer Feedback from " + feedback.getCustomerName());

        String stars = "★".repeat(feedback.getRating());
        TextArea textArea = new TextArea(
                "Rating: " + stars + " (" + feedback.getRating() + "/5)\n" +
                        "Date: " + feedback.getDate() + "\n\n" +
                        "Comment:\n" + feedback.getComment());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(40);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteFeedback(ActionEvent event) {
        Feedback selectedFeedback = feedbackTable.getSelectionModel().getSelectedItem();
        if (selectedFeedback != null) {
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Feedback");
            confirmAlert.setContentText(
                    "Are you sure you want to delete this feedback from " + selectedFeedback.getCustomerName() + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean deleted = feedbackDAO.deleteFeedback(selectedFeedback.getId());
                    if (deleted) {
                        loadFeedback(); // Refresh the table
                        statusLabel.setText("Feedback deleted successfully");
                        statusLabel.setTextFill(Color.GREEN);
                    } else {
                        statusLabel.setText("Failed to delete feedback");
                        statusLabel.setTextFill(Color.RED);
                    }
                } catch (SQLException e) {
                    statusLabel.setText("Error deleting feedback: " + e.getMessage());
                    statusLabel.setTextFill(Color.RED);
                    e.printStackTrace();
                }
            }
        } else {
            statusLabel.setText("Please select a feedback to delete");
            statusLabel.setTextFill(Color.ORANGE);
        }
    }

    @FXML
    private void handleAddManager(ActionEvent event) {
        try {
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Add Manager");
            dialog.setHeaderText("Enter Manager Details");
            
            // Create the custom dialog content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField usernameField = new TextField();
            TextField passwordField = new TextField();
            TextField nameField = new TextField();
            TextField emailField = new TextField();
            
            grid.add(new Label("Username:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Password:"), 0, 1);
            grid.add(passwordField, 1, 1);
            grid.add(new Label("Name:"), 0, 2);
            grid.add(nameField, 1, 2);
            grid.add(new Label("Email:"), 0, 3);
            grid.add(emailField, 1, 3);
            
            dialog.getDialogPane().setContent(grid);
            
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    User manager = new User();
                    manager.setUsername(usernameField.getText());
                    manager.setPassword(passwordField.getText());
                    manager.setName(nameField.getText());
                    manager.setEmail(emailField.getText());
                    manager.setManager(true);
                    return manager;
                }
                return null;
            });
            
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(manager -> {
                try {
                    if (userDAO == null) {
                        userDAO = new UserDAO();
                    }
                    userDAO.addManager(manager);
                    refreshManagers();
                    statusLabel.setText("Manager added successfully!");
                    statusLabel.setTextFill(Color.GREEN);
                } catch (SQLException e) {
                    statusLabel.setText("Error adding manager: " + e.getMessage());
                    statusLabel.setTextFill(Color.RED);
                }
            });
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }
    
    @FXML
    private void handleDeleteManager(ActionEvent event) {
        User selectedManager = managersTable.getSelectionModel().getSelectedItem();
        if (selectedManager == null) {
            statusLabel.setText("Please select a manager to delete!");
            statusLabel.setTextFill(Color.ORANGE);
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Manager");
        alert.setContentText("Are you sure you want to delete manager " + selectedManager.getName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (userDAO == null) {
                    userDAO = new UserDAO();
                }
                userDAO.deleteManager(selectedManager.getId());
                refreshManagers();
                statusLabel.setText("Manager deleted successfully!");
                statusLabel.setTextFill(Color.GREEN);
            } catch (SQLException e) {
                statusLabel.setText("Error deleting manager: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            }
        }
    }
    
    private void setupManagerTable() {
        try {
            // Initialize columns
            managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            managerUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            managerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            managerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

            // Add row factory for better visual feedback
            managersTable.setRowFactory(tv -> {
                TableRow<User> row = new TableRow<>();
                row.setOnMouseEntered(event -> row.setStyle("-fx-background-color: #f0f0f0;"));
                row.setOnMouseExited(event -> row.setStyle(""));
                return row;
            });

            // Set placeholder text
            managersTable.setPlaceholder(new Label("No managers found"));

            // Add selection listener
            managersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Enable/disable delete button based on selection
                    deleteManagerButton.setDisable(false);
                } else {
                    deleteManagerButton.setDisable(true);
                }
            });
        } catch (Exception e) {
            statusLabel.setText("Error setting up manager table: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void refreshManagers() {
        try {
            // Test database connection first
            if (!DatabaseConnection.testConnection()) {
                statusLabel.setText("Warning: Database connection failed. Please check your connection.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            if (userDAO == null) {
                userDAO = new UserDAO();
            }
            
            List<User> managers = userDAO.getAllManagers();
            if (managers != null && !managers.isEmpty()) {
                // Clear existing items and add new ones
                managersTable.getItems().clear();
                managersTable.setItems(FXCollections.observableArrayList(managers));
                managersTable.refresh();
                statusLabel.setText("Managers loaded successfully (" + managers.size() + " managers)");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                managersTable.getItems().clear();
                statusLabel.setText("No managers found in database");
                statusLabel.setTextFill(Color.ORANGE);
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading managers: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
}