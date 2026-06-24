package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IProductDAO;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.DTO.ProductDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteProductDAO implements IProductDAO {

    @Override
    public void insert(ProductDTO dto) {
        String sql = "INSERT OR REPLACE INTO product_specs " +
                "(spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.specId());
            ps.setString(2, dto.name());
            ps.setString(3, dto.manufacturer());
            ps.setInt(4, dto.categoryId());
            ps.setDouble(5, dto.costPrice());
            ps.setDouble(6, dto.sellPrice());
            ps.setInt(7, dto.minStockThreshold());
            ps.setInt(8, dto.totalQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.insert failed", e);
        }
    }

    @Override
    public ProductDTO findById(int specId) {
        String sql = "SELECT spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity " +
                "FROM product_specs WHERE spec_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.findById failed", e);
        }
        return null;
    }

    @Override
    public List<ProductDTO> findAll() {
        String sql = "SELECT spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity " +
                "FROM product_specs ORDER BY spec_id ASC";
        List<ProductDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.findAll failed", e);
        }
        return result;
    }

    @Override
    public void update(ProductDTO dto) {
        String sql = "UPDATE product_specs " +
                "SET name = ?, manufacturer = ?, category_id = ?, " +
                "cost_price = ?, sell_price = ?, min_stock_threshold = ?, total_quantity = ? " +
                "WHERE spec_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.name());
            ps.setString(2, dto.manufacturer());
            ps.setInt(3, dto.categoryId());
            ps.setDouble(4, dto.costPrice());
            ps.setDouble(5, dto.sellPrice());
            ps.setInt(6, dto.minStockThreshold());
            ps.setInt(7, dto.totalQuantity());
            ps.setInt(8, dto.specId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.update failed", e);
        }
    }

    @Override
    public void updateSpec(int specId, double costPrice, int totalQuantity) {
        String sql = "UPDATE product_specs SET cost_price = ?, total_quantity = ? WHERE spec_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, costPrice);
            ps.setInt(2, totalQuantity);
            ps.setInt(3, specId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.updateSpec failed", e);
        }
    }

    private ProductDTO mapRow(ResultSet rs) throws SQLException {
        return new ProductDTO(
            0,
            rs.getInt("spec_id"),
            rs.getString("name"),
            rs.getString("manufacturer"),
            rs.getInt("category_id"),
            rs.getDouble("cost_price"),
            rs.getDouble("sell_price"),
            rs.getInt("min_stock_threshold"),
            rs.getInt("total_quantity")
        );
    }
}
