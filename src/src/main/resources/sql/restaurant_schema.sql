-- MySQL Schema for Restaurant Management System

-- Create the database
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Create the Staff table (combines Manager and Kitchen staff)
CREATE TABLE Staff (
    staff_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role ENUM('manager', 'kitchen_staff') NOT NULL,
    kitchen_id INT UNIQUE
);

-- Create Category table
CREATE TABLE Category (
    title VARCHAR(50) PRIMARY KEY
);

-- Create the MenuItem table
CREATE TABLE MenuItem (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT DEFAULT 1,
    category_title VARCHAR(50),
    image_path VARCHAR(255),
    kitchen_id INT,
    FOREIGN KEY (category_title) REFERENCES Category(title),
    FOREIGN KEY (kitchen_id) REFERENCES Staff(kitchen_id)
);

-- Create Ingredient table
CREATE TABLE Ingredient (
    title VARCHAR(100) PRIMARY KEY,
    current_quantity DECIMAL(10,2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    min_threshold DECIMAL(10,2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Supplier table
CREATE TABLE Supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255)
);

-- Create Purchase Order table (simplified)
CREATE TABLE Purchase_Order (
    po_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('draft', 'ordered', 'completed', 'cancelled') DEFAULT 'draft',
    total_amount DECIMAL(10,2),
    created_by VARCHAR(50),
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id),
    FOREIGN KEY (created_by) REFERENCES Staff(staff_id)
);

-- Create Customer table
CREATE TABLE Customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    address VARCHAR(255),
    table_number INT,
    to_deliver BOOLEAN DEFAULT FALSE
);

-- Create Order table (Updated to include kitchen_id and notes)
CREATE TABLE `Order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    customer_id INT,
    staff_id VARCHAR(50),
    kitchen_id INT,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),
    FOREIGN KEY (kitchen_id) REFERENCES Staff(kitchen_id)
);

-- Create MenuItem_Ingredient table to track ingredients used in menu items
CREATE TABLE MenuItem_Ingredient (
    item_id INT,
    ingredient_title VARCHAR(100),
    quantity DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id, ingredient_title),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id),
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title)
);

-- Add role-based system tables and modifications

-- Create Users table with improved security
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role ENUM('admin', 'manager') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Create User_Profiles table
CREATE TABLE User_Profiles (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create Permissions table
CREATE TABLE Permissions (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Create Role_Permissions table
CREATE TABLE Role_Permissions (
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (role, permission),
    FOREIGN KEY (permission) REFERENCES Permissions(permission_name)
);

-- Insert default permissions
INSERT INTO Permissions (permission_name, description) VALUES
('add_manager', 'Can add new managers'),
('delete_manager', 'Can delete managers'),
('add_item', 'Can add menu items'),
('delete_item', 'Can delete menu items'),
('delete_comment', 'Can delete customer comments');

-- Assign permissions to roles
INSERT INTO Role_Permissions (role, permission) VALUES
-- Admin permissions
('admin', 'add_manager'),
('admin', 'delete_manager'),
('admin', 'add_item'),
('admin', 'delete_item'),
('admin', 'delete_comment'),
-- Manager permissions
('manager', 'add_item'),
('manager', 'delete_item'),
('manager', 'delete_comment');

-- Create default admin user with hashed password
INSERT INTO Users (username, password, salt, role) VALUES
('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'admin_salt', 'admin');

-- Create admin profile
INSERT INTO User_Profiles (user_id, full_name, email, phone) VALUES
(1, 'System Administrator', 'admin@restaurant.com', '123-456-7890');

-- Menu Items table
CREATE TABLE Menu_Items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Ingredients table
CREATE TABLE Ingredients (
    ingredient_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL,
    reorder_level DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Menu Item Ingredients table
CREATE TABLE Menu_Item_Ingredients (
    item_id INT,
    ingredient_id INT,
    quantity DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id, ingredient_id),
    FOREIGN KEY (item_id) REFERENCES Menu_Items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    table_number INT NOT NULL,
    status ENUM('pending', 'preparing', 'ready', 'served', 'cancelled') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Order Items table
CREATE TABLE Order_Items (
    order_id INT,
    item_id INT,
    quantity INT NOT NULL,
    notes TEXT,
    PRIMARY KEY (order_id, item_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Menu_Items(item_id)
);

-- Feedback table
CREATE TABLE Customer_Feedback (
    feedback_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Create trigger to update ingredient quantities when orders are placed
DELIMITER //
CREATE TRIGGER update_ingredients_after_order
AFTER INSERT ON Order_Items
FOR EACH ROW
BEGIN
    UPDATE Ingredient i
    JOIN MenuItem mi ON mi.item_id = NEW.item_id
    SET i.current_quantity = i.current_quantity - (NEW.quantity * mi.quantity),
        i.last_updated = CURRENT_TIMESTAMP
    WHERE i.title IN (
        SELECT ingredient_title 
        FROM MenuItem_Ingredient 
        WHERE item_id = NEW.item_id
    );
END //
DELIMITER ;