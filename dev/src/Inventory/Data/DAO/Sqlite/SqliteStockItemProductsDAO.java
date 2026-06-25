package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IStockItemProductsDAO;
import Inventory.Data.DB.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteStockItemProductsDAO implements IStockItemProductsDAO {

    @Override
    public void insert(int specId, String area, int shelf, int row, int productId) {
        String sql = "INSERT OR REPLACE INTO stock_item_products (spec_id, area, shelf, row, product_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            ps.setString(2, area);
            ps.setInt(3, shelf);
            ps.setInt(4, row);
            ps.setInt(5, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemProductsDAO.insert failed", e);
        }
    }

    @Override
    public List<Integer> findByLocation(int specId, String area, int shelf, int row) {
        String sql = "SELECT product_id FROM stock_item_products WHERE spec_id = ? AND area = ? AND shelf = ? AND row = ? ORDER BY product_id ASC";
        List<Integer> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            ps.setString(2, area);
            ps.setInt(3, shelf);
            ps.setInt(4, row);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemProductsDAO.findByLocation failed", e);
        }
        return result;
    }

    @Override
    public void deleteByLocation(int specId, String area, int shelf, int row) {
        String sql = "DELETE FROM stock_item_products WHERE spec_id = ? AND area = ? AND shelf = ? AND row = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            ps.setString(2, area);
            ps.setInt(3, shelf);
            ps.setInt(4, row);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemProductsDAO.deleteByLocation failed", e);
        }
    }
}
