-- Add role-based system tables and modifications

-- Create Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'manager') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

-- Create User_Profile table
CREATE TABLE User_Profile (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create Permissions table
CREATE TABLE Permissions (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Create Role_Permissions table
CREATE TABLE Role_Permissions (
    role ENUM('admin', 'manager'),
    permission_id INT,
    PRIMARY KEY (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES Permissions(permission_id)
);

-- Insert default permissions
INSERT INTO Permissions (name, description) VALUES
('add_manager', 'Can add new managers'),
('delete_manager', 'Can delete managers'),
('add_item', 'Can add menu items'),
('delete_item', 'Can delete menu items'),
('delete_comment', 'Can delete feedback comments'),
('edit_profile', 'Can update own profile');

-- Assign permissions to roles
INSERT INTO Role_Permissions (role, permission_id) VALUES
-- Admin permissions
('admin', 1), -- add_manager
('admin', 2), -- delete_manager
('admin', 3), -- add_item
('admin', 4), -- delete_item
('admin', 5), -- delete_comment
('admin', 6), -- edit_profile
-- Manager permissions
('manager', 3), -- add_item
('manager', 4), -- delete_item
('manager', 5), -- delete_comment
('manager', 6); -- edit_profile

-- Create default admin user (password should be changed immediately)
INSERT INTO Users (username, password, role) VALUES
('admin', '$2a$10$default_hashed_password', 'admin');

-- Add user_id to feedback table
ALTER TABLE feedback
ADD COLUMN user_id INT,
ADD FOREIGN KEY (user_id) REFERENCES Users(user_id);

-- Add user_id to MenuItem table
ALTER TABLE MenuItem
ADD COLUMN created_by INT,
ADD FOREIGN KEY (created_by) REFERENCES Users(user_id); 