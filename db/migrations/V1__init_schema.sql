-- =====================================================================
-- SaranyaMart — Database schema v1
-- Anna University R2025 Sem3 JAVA Capstone (Week 1, Aug 2 2026)
--
-- Design rules (spec Section 4):
--   1. Every foreign key is indexed.
--   2. users.email is unique-constrained.
--   3. Monetary values are DECIMAL(10,2). FLOAT is prohibited for currency.
--   4. created_at timestamp is present on every table.
--   5. This file is checked into the repository (migration V1).
--
-- H2 2.x embedded / server mode. Idempotent re-runnable.
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER      AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,
    role          ENUM('BUYER', 'SELLER', 'ADMIN') NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id          INTEGER        AUTO_INCREMENT PRIMARY KEY,
    seller_id   INTEGER        NOT NULL,
    name        VARCHAR(200)   NOT NULL,
    description CLOB,
    price       DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_qty   INTEGER        NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category    VARCHAR(100),
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS orders (
    id           INTEGER        AUTO_INCREMENT PRIMARY KEY,
    buyer_id     INTEGER        NOT NULL,
    status       ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id         INTEGER        AUTO_INCREMENT PRIMARY KEY,
    order_id   INTEGER        NOT NULL,
    product_id INTEGER        NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    user_id    INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_cart_items_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    product_id INTEGER NOT NULL,
    user_id    INTEGER NOT NULL,
    rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users (id)
);

-- --- Indexes on every foreign key (design rule 1) ---
CREATE INDEX IF NOT EXISTS idx_products_seller   ON products (seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
CREATE INDEX IF NOT EXISTS idx_orders_buyer      ON orders (buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status     ON orders (status);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_prod  ON order_items (product_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_user   ON cart_items (user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_prod   ON cart_items (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_product   ON reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user      ON reviews (user_id);
