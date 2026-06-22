-- Inventory SQLite schema  (reference; authoritative DDL lives in DatabaseConnection.java)
-- Create order: categories -> product_specs -> stock_items -> promotions -> defective_reports

PRAGMA foreign_keys = ON;

-- categories: parent_category_id = 0 means root
CREATE TABLE IF NOT EXISTS categories (
    category_id        INTEGER PRIMARY KEY,
    category_name      TEXT    NOT NULL,
    parent_category_id INTEGER NOT NULL DEFAULT 0
);

-- product_specs: spec_id groups products under the same ProductSpec; total_quantity is a cached count
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

-- stock_items: area = 'STORE'|'WAREHOUSE'; expiry_date is YYYY-MM-DD or NULL
CREATE TABLE IF NOT EXISTS stock_items (
    spec_id     INTEGER NOT NULL,
    area        TEXT    NOT NULL,
    shelf       INTEGER NOT NULL,
    row         INTEGER NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 0,
    expiry_date TEXT,
    PRIMARY KEY (spec_id, area, shelf, row)
);

-- promotions: target_spec_id=0 means category-targeted; target_category_id=0 means product-targeted
CREATE TABLE IF NOT EXISTS promotions (
    discount_percent     REAL    NOT NULL,
    start_date           TEXT    NOT NULL,
    end_date             TEXT    NOT NULL,
    target_spec_id       INTEGER NOT NULL DEFAULT 0,
    target_category_id   INTEGER NOT NULL DEFAULT 0,
    target_product_name  TEXT,
    target_category_name TEXT
);

-- defective_reports: reason is 'DEFECTIVE' or 'EXPIRED'; multiple rows per product_id OK
CREATE TABLE IF NOT EXISTS defective_reports (
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL,
    reason      TEXT    NOT NULL,
    report_date TEXT    NOT NULL
);
