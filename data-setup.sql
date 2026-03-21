-- Drop tables if they exist (for easy re-running)
DROP TABLE IF EXISTS testimonials;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_favorites;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS support_queries;

-- Create users tableUser
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);

-- Create pets table
CREATE TABLE pets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    species ENUM('DOG', 'CAT', 'BIRD') NOT NULL,
    breed VARCHAR(100),
    age VARCHAR(50), -- Using VARCHAR for flexibility like "5 months", "2 years"
    image VARCHAR(255), -- Stores filename or relative path
    description TEXT,
    location VARCHAR(150),
    traits VARCHAR(255) -- Store as comma-separated string, e.g., "Friendly,Playful,Good with kids"
);

-- Create products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image VARCHAR(255), -- Stores filename or relative path
    category VARCHAR(100)
);

-- Create testimonials table
CREATE TABLE testimonials (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,               -- Link to the user who submitted
    name VARCHAR(100) NOT NULL,         -- Submitter's name (can be pre-filled from user profile)
    location VARCHAR(150),              -- Submitter's location (optional)
    image VARCHAR(255),                 -- Submitter's image (optional)
    petName VARCHAR(100),               -- Name of the adopted pet (optional)
    text TEXT NOT NULL,                 -- The testimonial content
    rating INT CHECK (rating >= 1 AND rating <= 5), -- Optional rating (1-5 stars)
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending', -- Approval status
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When it was submitted
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE -- Link to users table
);

-- Add indexes for faster lookups
CREATE INDEX idx_testimonial_user_id ON testimonials(user_id);
CREATE INDEX idx_testimonial_status ON testimonials(status);

-- Create the user_favorites table
CREATE TABLE user_favorites (
    user_id INT NOT NULL,
    pet_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, pet_id), -- Ensures a user can favorite a pet only once
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- If user is deleted, remove their favorites
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE    -- If pet is removed, remove related favorites
);

-- Optional: Add an index for faster lookups if needed later (especially on pet_id if querying all users who favorited a pet)
CREATE INDEX idx_favorite_pet_id ON user_favorites(pet_id);

-- Create cart_items table if it doesn't exist
CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- Create index for faster cart lookups by user
CREATE INDEX idx_cart_user ON cart_items(user_id);

-- Table for storing overall order information
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,                     -- Auto-incrementing order ID
    user_id INTEGER NOT NULL,                  -- Foreign key to the users table
    total_amount DECIMAL(10, 2) NOT NULL,     -- Total cost of the order
    shipping_street VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_phone VARCHAR(20),
    payment_upi_id VARCHAR(100),              -- UPI ID used for payment (example)
    status VARCHAR(50) NOT NULL DEFAULT 'Pending', -- Order status (e.g., Pending, Shipped)
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When the order was placed
    FOREIGN KEY (user_id) REFERENCES users(id) -- Assuming you have a 'users' table with an 'id' column
);

-- Table for storing individual items within an order
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,                     -- Auto-incrementing item ID
    order_id INTEGER NOT NULL,                 -- Foreign key to the orders table
    product_id INTEGER NOT NULL,               -- Foreign key to the products table
    quantity INTEGER NOT NULL,
    price_at_time_of_order DECIMAL(10, 2) NOT NULL, -- Price of the product when ordered
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, -- If order is deleted, delete items
    FOREIGN KEY (product_id) REFERENCES products(id) -- Assuming 'products' table with 'id'
    -- Consider adding ON DELETE SET NULL or ON DELETE RESTRICT for product_id if products can be deleted
);

-- Optional: Add indexes for performance, especially on foreign keys
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Create support_queries table
CREATE TABLE support_queries (
    query_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    query_text TEXT NOT NULL,
    query_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_reply TEXT NULL,
    reply_timestamp TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'Open' NOT NULL, -- e.g., 'Open', 'Answered', 'Closed'
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE -- Link to the user who asked
);

-- Add indexes for faster lookups
CREATE INDEX idx_support_user_id ON support_queries(user_id);
CREATE INDEX idx_support_status ON support_queries(status);


-- Insert Sample Data (adjust image paths as needed)

-- Sample Pets
INSERT INTO pets (name, species, breed, age, image, description, location, traits) VALUES
('Buddy', 'DOG', 'Golden Retriever', '2 years', 'buddy.jpg', 'Friendly and energetic, loves fetch.', 'City Shelter West', 'Friendly,Energetic,Good with kids'),
('Whiskers', 'CAT', 'Siamese', '1 year', 'whiskers.jpg', 'Curious and vocal, enjoys naps in sunny spots.', 'Maple Animal Rescue', 'Curious,Vocal,Affectionate'),
('Kiwi', 'BIRD', 'Parakeet', '6 months', 'kiwi.jpg', 'Cheerful and chirpy, learning to talk.', 'Avian Friends Sanctuary', 'Chirpy,Social,Intelligent'),
('Luna', 'DOG', 'German Shepherd', '3 years', 'luna.jpg', 'Loyal and protective, needs an active owner.', 'City Shelter East', 'Loyal,Protective,Active'),
('Max', 'DOG', 'Labrador Retriever', '4 years', 'max_labrador.jpg', 'Playful and loves water.', 'City Shelter North', 'Playful,Loyal,Swimmer'),
('Cleo', 'CAT', 'Tabby', '2 years', 'cleo_tabby.jpg', 'Independent but sweet, loves quiet corners.', 'Feline Friends', 'Independent,Sweet,Quiet'),
('Rocky', 'DOG', 'Boxer', '5 years', 'rocky_boxer.jpg', 'Energetic and goofy, great family dog.', 'Canine Corner', 'Energetic,Goofy,Strong');

-- Sample Products
INSERT INTO products (name, price, image, category) VALUES
('Premium Dog Food (5kg)', 25.99, 'dog_food.jpg', 'Food'),
('Interactive Cat Wand', 8.50, 'cat_wand.jpg', 'Toys'),
('Comfy Pet Bed (Medium)', 35.00, 'pet_bed.jpg', 'Accessories'),
('Bird Seed Mix (1kg)', 12.75, 'bird_seed.jpg', 'Food'),
('Durable Chew Toy', 10.99, 'chew_toy.jpg', 'Toys');

-- Sample Testimonials (Assuming user IDs 1, 2, 3 exist and submitted these)
-- Note: In a real app, user_id would come from the logged-in user. Status is initially 'pending'.
-- Let's make the existing ones 'approved' for display purposes.

-- INSERT INTO testimonials (user_id, name, location, image, petName, text, rating, status) VALUES
-- (1, 'Alice Smith', 'Springfield', 'alice.jpg', 'Buddy', 'Adopting Buddy was the best decision! He brings so much joy to our family. The process was smooth.', 5, 'approved'),
-- (1, 'Bob Johnson', 'Greenville', 'bob.jpg', 'Whiskers', 'Whiskers settled in right away. She''s the perfect companion. Thanks to the team for their help!', 4, 'approved'),
-- (1, 'Charlie Brown', 'Metropolis', NULL, 'Max', 'Found our perfect dog here. He was a bit shy at first but now he runs the house!', 5, 'approved');
