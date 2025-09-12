-- Sample data for FlowStep Spring Boot 3 example

-- Insert sample users
INSERT INTO users (username, email, full_name, is_active, created_at, updated_at) VALUES
('john_doe', 'john.doe@example.com', 'John Doe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane_smith', 'jane.smith@example.com', 'Jane Smith', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bob_wilson', 'bob.wilson@example.com', 'Bob Wilson', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('alice_brown', 'alice.brown@example.com', 'Alice Brown', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, is_active, created_at, updated_at) VALUES
('Laptop Pro 15"', 'High-performance laptop with 15-inch display', 1299.99, 50, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 29.99, 200, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USB-C Hub', 'Multi-port USB-C hub with HDMI and ethernet', 79.99, 75, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 149.99, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Monitor 27"', '4K monitor with USB-C connectivity', 399.99, 25, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Webcam HD', 'High-definition webcam with auto-focus', 89.99, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Headphones', 'Noise-cancelling wireless headphones', 199.99, 40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tablet Stand', 'Adjustable aluminum tablet stand', 39.99, 100, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Power Bank', '20000mAh portable power bank with fast charging', 49.99, 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cable Kit', 'USB cable kit with multiple connector types', 24.99, 150, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample orders
INSERT INTO orders (user_id, total_amount, status, order_date, created_at, updated_at) VALUES
(1, 1379.98, 'DELIVERED', DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -25, CURRENT_TIMESTAMP)),
(1, 229.98, 'DELIVERED', DATEADD('DAY', -20, CURRENT_TIMESTAMP), DATEADD('DAY', -20, CURRENT_TIMESTAMP), DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
(1, 89.99, 'SHIPPED', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(2, 599.98, 'DELIVERED', DATEADD('DAY', -15, CURRENT_TIMESTAMP), DATEADD('DAY', -15, CURRENT_TIMESTAMP), DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
(2, 149.99, 'PROCESSING', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(3, 79.99, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, created_at) VALUES
-- Order 1 (John Doe - $1379.98)
(1, 1, 1, 1299.99, DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
(1, 3, 1, 79.99, DATEADD('DAY', -30, CURRENT_TIMESTAMP)),

-- Order 2 (John Doe - $229.98)
(2, 4, 1, 149.99, DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
(2, 3, 1, 79.99, DATEADD('DAY', -20, CURRENT_TIMESTAMP)),

-- Order 3 (John Doe - $89.99)
(3, 6, 1, 89.99, DATEADD('DAY', -5, CURRENT_TIMESTAMP)),

-- Order 4 (Jane Smith - $599.98)
(4, 5, 1, 399.99, DATEADD('DAY', -15, CURRENT_TIMESTAMP)),
(4, 7, 1, 199.99, DATEADD('DAY', -15, CURRENT_TIMESTAMP)),

-- Order 5 (Jane Smith - $149.99)
(5, 4, 1, 149.99, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),

-- Order 6 (Bob Wilson - $79.99)
(6, 3, 1, 79.99, CURRENT_TIMESTAMP);