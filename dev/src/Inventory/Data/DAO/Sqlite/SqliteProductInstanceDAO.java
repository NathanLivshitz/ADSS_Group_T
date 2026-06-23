package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IProductInstanceDAO;
import Inventory.Data.DB.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteProductInstanceDAO implements IProductInstanceDAO {

    @Override
    public void insert(int productId, int specId) {
        String sql = "INSERT OR REPLACE INTO products (product_id, spec_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, specId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductInstanceDAO.insert failed", e);
        }
    }

    @Override
    public List<Integer> findAllBySpecId(int specId) {
        String sql = "SELECT product_id FROM products WHERE spec_id = ? ORDER BY product_id ASC";
        List<Integer> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductInstanceDAO.findAllBySpecId failed", e);
        }
        return result;
    }

    @Override
    public List<Integer> findAll() {
        String sql = "SELECT product_id FROM products ORDER BY product_id ASC";
        List<Integer> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getInt("product_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductInstanceDAO.findAll failed", e);
        }
        return result;
    }
}
