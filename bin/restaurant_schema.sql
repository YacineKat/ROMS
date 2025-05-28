-- Create the database
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Modified Staff table to include Admins and authentication fields
CREATE TABLE Staff (
    staff_id INT PRIMARY KEY AUTO_INCREMENT, -- Changed to auto-incrementing integer for simplicity
    username VARCHAR(50) UNIQUE NOT NULL,   -- Unique username for login
    password VARCHAR(255) NOT NULL,         -- Password (hashed in production)
    name VARCHAR(100) NOT NULL,             -- Staff name
    role ENUM('admin', 'manager', 'kitchen_staff') NOT NULL, -- Added 'admin' role
    kitchen_id INT UNIQUE,                  -- For kitchen staff, nullable for admins/managers
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

-- Create Purchase Order table
CREATE TABLE Purchase_Order (
    po_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('draft', 'ordered', 'completed', 'cancelled') DEFAULT 'draft',
    total_amount DECIMAL(10,2),
    created_by INT,
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

-- Create Order table
CREATE TABLE `Order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    customer_id INT,
    staff_id INT,
    kitchen_id INT,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),
    FOREIGN KEY (kitchen_id) REFERENCES Staff(kitchen_id)
);

-- Create Order_Items table
CREATE TABLE Order_Items (
    order_id INT,
    item_id INT,
    quantity INT DEFAULT 1,
    PRIMARY KEY (order_id, item_id),
    FOREIGN KEY (order_id) REFERENCES `Order`(order_id),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id)
);

-- Create feedback table
CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    comment TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    submission_date TIMESTAMP NOT NULL
);

-- Create MenuItem_Ingredient table
CREATE TABLE MenuItem_Ingredient (
    item_id INT,
    ingredient_title VARCHAR(100),
    quantity DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id, ingredient_title),
    FOREIGN KEY (item_id) REFERENCES MenuItem(item_id),
    FOREIGN KEY (ingredient_title) REFERENCES Ingredient(title)
);

-- Trigger to update ingredient quantities
DELIMITER //
CREATE TRIGGER update_ingredients_after_order
AFTER INSERT ON Order_Items
FOR EACH ROW
BEGIN
    UPDATE Ingredient i
    JOIN MenuItem_Ingredient mi ON mi.ingredient_title = i.title
    SET i.current_quantity = i.current_quantity - (NEW.quantity * mi.quantity),
        i.last_updated = CURRENT_TIMESTAMP
    WHERE mi.item_id = NEW.item_id;
END //
DELIMITER ;

-- Insert default Admin user (password should be hashed in production)
INSERT INTO Staff (username, password, name, role)
VALUES ('admin', 'admin', 'Administrator', 'admin');