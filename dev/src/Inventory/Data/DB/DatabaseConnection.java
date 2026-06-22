package Inventory.Data.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Single SQLite connection factory for the Inventory system.
// get() opens a fresh connection each call; caller owns close.
// Schema init is idempotent (CREATE TABLE IF NOT EXISTS) on first call.
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:inventory.db";
    private static boolean initialized = false;

    // Opens a JDBC connection. Caller must close it.
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

    // Deletes all rows in child-before-parent order inside one transaction.
    public static synchronized void truncateAll() throws SQLException {
        try (Connection conn = get()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM defective_reports");
                st.executeUpdate("DELETE FROM promotions");
                st.executeUpdate("DELETE FROM stock_items");
                st.executeUpdate("DELETE FROM product_specs");
                st.executeUpdate("DELETE FROM categories");
            }
            conn.commit();
        }
    }

    // ── Private ──────────────────────────────────────────────────────────────

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
                "    product_id          INTEGER PRIMARY KEY," +
                "    spec_id             INTEGER NOT NULL," +
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
