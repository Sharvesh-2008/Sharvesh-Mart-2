-- =====================================================================
-- SharveshMart — Seed data
-- Applied automatically on first startup. NOT a migration (spec Section 14).
--
-- Passwords are bcrypt-hashed (cost 12). Plaintext passwords never stored.
--   admin@sharveshmart.com / Admin@123   (role ADMIN, no signup flow — F1)
--   seller@sharveshmart.com / Seller@123
--   buyer@sharveshmart.com  / Buyer@123
-- =====================================================================

INSERT INTO users (name, email, password_hash, role) VALUES
  ('Admin',  'admin@sharveshmart.com',  '$2a$12$6Y8gMKuyQJYVjxJeHelAXez6Wq8W8hCGT1qLx8h8jHE9Jgvf9Wobq', 'ADMIN'),
  ('Seller', 'seller@sharveshmart.com', '$2a$12$GYIBSn71rzv3ZshiY35L4ePq5XlOeoLC3ZiL5z5QNcw9ttXlJbIf.', 'SELLER'),
  ('Buyer',  'buyer@sharveshmart.com',  '$2a$12$ZkVXXw0HL8AnxpRhRUO7e.GnkkeBCh7y/gwXgI9k2aCHXewvi8x2S', 'BUYER');

INSERT INTO products (seller_id, name, description, price, stock_qty, category) VALUES
  ((SELECT id FROM users WHERE email = 'seller@sharveshmart.com'),
   'Mechanical Keyboard', 'Tactile blue-switch, RGB backlit, hot-swappable.', 89.99, 25, 'Electronics'),
  ((SELECT id FROM users WHERE email = 'seller@sharveshmart.com'),
   'Wireless Mouse', '2.4GHz + Bluetooth, 1600 DPI, rechargeable.', 24.50, 60, 'Electronics'),
  ((SELECT id FROM users WHERE email = 'seller@sharveshmart.com'),
   'Stainless Steel Bottle', '750 ml, vacuum insulated, leak-proof.', 15.00, 120, 'Home & Kitchen');
