USE restaurant_db;

-- Add kitchen_id column
ALTER TABLE `Order` 
ADD COLUMN kitchen_id INT,
ADD FOREIGN KEY (kitchen_id) REFERENCES Staff(kitchen_id);

-- Add notes column
ALTER TABLE `Order`
ADD COLUMN notes TEXT; 