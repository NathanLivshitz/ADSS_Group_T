-- Inventory SQLite schema  (reference copy; authoritative DDL is in DatabaseConnection.java)
-- All tables use CREATE TABLE IF NOT EXISTS for idempotent startup initialisation.
-- Run order: categories -> product_specs -> stock_items -> promotions -> defective_reports
-- Truncate order (child before parent): defective_reports -> promotions -> stock_items -> product_specs -> categories

PRAGMA foreign_keys = ON;

-- 1. categories
--    parent_category_id = 0 means root (no parent).
CREATE TABLE IF NOT EXISTS categories (
    category_id        INTEGER PRIMARY KEY,
    category_name      TEXT    NOT NULL,
    parent_category_id INTEGER NOT NULL DEFAULT 0
);

-- 2. product_specs
--    One row per Product (product_id PK).  spec_id groups products sharing the same ProductSpec.
--    category_id is a logical FK to categories.category_id.
--    total_quantity is a cached aggregate kept current by InventoryController.
CREATE TABLE IF NOT EXISTS product_specs (
    product_id          INTEGER PRIMARY KEY,
    spec_id             INTEGER NOT NULL,
    name                TEXT    NOT NULL,
    manufacturer        TEXT    NOT NULL,
    category_id         INTEGER NOT NULL,
    cost_price          REAL    NOT NULL,
    sell_price          REAL    NOT NULL,
    min_stock_threshold INTEGER NOT NULL,
    total_quantity      INTEGER NOT NULL DEFAULT 0
);

-- 3. stock_items
--    Composite natural PK: (spec_id, area, shelf, row).
--    area is 'STORE' or 'WAREHOUSE'.
--    expiry_date is ISO-8601 'YYYY-MM-DD' or NULL when no expiry applies.
CREATE TABLE IF NOT EXISTS stock_items (
    spec_id     INTEGER NOT NULL,
    area        TEXT    NOT NULL,
    shelf       INTEGER NOT NULL,
    row         INTEGER NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 0,
    expiry_date TEXT,
    PRIMARY KEY (spec_id, area, shelf, row)
);

-- 4. promotions
--    No surrogate key; promotions are immutable once created.
--    target_spec_id = 0 means category-targeted.
--    target_category_id = 0 means product-targeted.
CREATE TABLE IF NOT EXISTS promotions (
    discount_percent     REAL    NOT NULL,
    start_date           TEXT    NOT NULL,
    end_date             TEXT    NOT NULL,
    target_spec_id       INTEGER NOT NULL DEFAULT 0,
    target_category_id   INTEGER NOT NULL DEFAULT 0,
    target_product_name  TEXT,
    target_category_name TEXT
);

-- 5. defective_reports
--    No surrogate key; multiple reports for the same product_id are valid.
--    reason is 'DEFECTIVE' or 'EXPIRED'.
CREATE TABLE IF NOT EXISTS defective_reports (
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL,
    reason      TEXT    NOT NULL,
    report_date TEXT    NOT NULL
);
