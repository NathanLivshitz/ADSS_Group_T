-- inventory.db schema
--
-- product_specs: one row per spec (name, prices, thresholds, aggregated quantity)
-- products:      one row per physical product instance, linked to a spec via spec_id
-- stock_items:   location batches linked to a spec; holds area/shelf/row/quantity/expiry
-- stock_item_products: mapping table linking stock locations to individual product instances
-- promotions:    active promotions targeting specs or categories
-- defective_reports: reports of defective/expired product instances
-- categories:    hierarchical category tree

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    category_id        INTEGER PRIMARY KEY,
    category_name      TEXT    NOT NULL,
    parent_category_id INTEGER NOT NULL DEFAULT 0
);

-- Product specs
CREATE TABLE IF NOT EXISTS product_specs (
    spec_id             INTEGER PRIMARY KEY,
    name                TEXT    NOT NULL,
    manufacturer        TEXT    NOT NULL,
    category_id         INTEGER NOT NULL,
    cost_price          REAL    NOT NULL,
    sell_price          REAL    NOT NULL,
    min_stock_threshold INTEGER NOT NULL,
    total_quantity      INTEGER NOT NULL DEFAULT 0
);

-- Products (physical instances)
CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY,
    spec_id    INTEGER NOT NULL REFERENCES product_specs(spec_id)
);

-- Stock items (location batches)
CREATE TABLE IF NOT EXISTS stock_items (
    spec_id     INTEGER NOT NULL REFERENCES product_specs(spec_id),
    area        TEXT    NOT NULL,
    shelf       INTEGER NOT NULL,
    row         INTEGER NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 0,
    expiry_date TEXT,
    PRIMARY KEY (spec_id, area, shelf, row)
);

-- Stock item -> product instance mapping
CREATE TABLE IF NOT EXISTS stock_item_products (
    spec_id  INTEGER NOT NULL,
    area     TEXT    NOT NULL,
    shelf    INTEGER NOT NULL,
    row      INTEGER NOT NULL,
    product_id INTEGER NOT NULL REFERENCES products(product_id),
    PRIMARY KEY (spec_id, area, shelf, row, product_id),
    FOREIGN KEY (spec_id, area, shelf, row) REFERENCES stock_items(spec_id, area, shelf, row)
);

-- Promotions
CREATE TABLE IF NOT EXISTS promotions (
    discount_percent     REAL    NOT NULL,
    start_date           TEXT    NOT NULL,
    end_date             TEXT    NOT NULL,
    target_spec_id       INTEGER NOT NULL DEFAULT 0,
    target_category_id   INTEGER NOT NULL DEFAULT 0,
    target_product_name  TEXT,
    target_category_name TEXT
);

-- Defective reports
CREATE TABLE IF NOT EXISTS defective_reports (
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL,
    reason      TEXT    NOT NULL,
    report_date TEXT    NOT NULL
);
