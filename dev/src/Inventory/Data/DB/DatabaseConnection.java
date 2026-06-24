package Inventory.Data.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// SQLite connection factory. get() opens a fresh connection; caller must close.
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";
    private static boolean initialized = false;

    public static synchronized Connection get() throws SQLException {
        if (!initialized) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found on classpath", e);
            }
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        if (!initialized) {
            initSchema(conn);
            initialized = true;
        }
        return conn;
    }

    // removes all rows child-before-parent so FK constraints don't fire
    public static synchronized void truncateAll() throws SQLException {
        try (Connection conn = get()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM defective_reports");
                st.executeUpdate("DELETE FROM promotions");
                st.executeUpdate("DELETE FROM stock_item_products");
                st.executeUpdate("DELETE FROM stock_items");
                st.executeUpdate("DELETE FROM products");
                st.executeUpdate("DELETE FROM product_specs");
                st.executeUpdate("DELETE FROM categories");
            }
            conn.commit();
        }
    }

    private static void initSchema(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("PRAGMA foreign_keys = ON");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "    category_id        INTEGER PRIMARY KEY," +
                "    category_name      TEXT    NOT NULL," +
                "    parent_category_id INTEGER NOT NULL DEFAULT 0" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS product_specs (" +
                "    spec_id             INTEGER PRIMARY KEY," +
                "    name                TEXT    NOT NULL," +
                "    manufacturer        TEXT    NOT NULL," +
                "    category_id         INTEGER NOT NULL," +
                "    cost_price          REAL    NOT NULL," +
                "    sell_price          REAL    NOT NULL," +
                "    min_stock_threshold INTEGER NOT NULL," +
                "    total_quantity      INTEGER NOT NULL DEFAULT 0" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products (" +
                "    product_id INTEGER PRIMARY KEY," +
                "    spec_id    INTEGER NOT NULL REFERENCES product_specs(spec_id)" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stock_items (" +
                "    spec_id     INTEGER NOT NULL," +
                "    area        TEXT    NOT NULL," +
                "    shelf       INTEGER NOT NULL," +
                "    row         INTEGER NOT NULL," +
                "    quantity    INTEGER NOT NULL DEFAULT 0," +
                "    expiry_date TEXT," +
                "    PRIMARY KEY (spec_id, area, shelf, row)" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS stock_item_products (" +
                "    spec_id     INTEGER NOT NULL," +
                "    area        TEXT    NOT NULL," +
                "    shelf       INTEGER NOT NULL," +
                "    row         INTEGER NOT NULL," +
                "    product_id  INTEGER NOT NULL REFERENCES products(product_id)," +
                "    PRIMARY KEY (spec_id, area, shelf, row, product_id)," +
                "    FOREIGN KEY (spec_id, area, shelf, row) REFERENCES stock_items(spec_id, area, shelf, row)" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS promotions (" +
                "    discount_percent     REAL    NOT NULL," +
                "    start_date           TEXT    NOT NULL," +
                "    end_date             TEXT    NOT NULL," +
                "    target_spec_id       INTEGER NOT NULL DEFAULT 0," +
                "    target_category_id   INTEGER NOT NULL DEFAULT 0," +
                "    target_product_name  TEXT," +
                "    target_category_name TEXT" +
                ")"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS defective_reports (" +
                "    product_id  INTEGER NOT NULL," +
                "    quantity    INTEGER NOT NULL," +
                "    reason      TEXT    NOT NULL," +
                "    report_date TEXT    NOT NULL" +
                ")"
            );
        }
        conn.commit();
        conn.setAutoCommit(true);
    }
}
