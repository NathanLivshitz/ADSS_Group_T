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
                "(product_id, spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.productId());
            ps.setInt(2, dto.specId());
            ps.setString(3, dto.name());
            ps.setString(4, dto.manufacturer());
            ps.setInt(5, dto.categoryId());
            ps.setDouble(6, dto.costPrice());
            ps.setDouble(7, dto.sellPrice());
            ps.setInt(8, dto.minStockThreshold());
            ps.setInt(9, dto.totalQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.insert failed", e);
        }
    }

    @Override
    public ProductDTO findById(int productId) {
        String sql = "SELECT product_id, spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity " +
                "FROM product_specs WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
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
        String sql = "SELECT product_id, spec_id, name, manufacturer, category_id, " +
                "cost_price, sell_price, min_stock_threshold, total_quantity " +
                "FROM product_specs ORDER BY product_id ASC";
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
                "SET spec_id = ?, name = ?, manufacturer = ?, category_id = ?, " +
                "cost_price = ?, sell_price = ?, min_stock_threshold = ?, total_quantity = ? " +
                "WHERE product_id = ?";
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
            ps.setInt(9, dto.productId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteProductDAO.update failed", e);
        }
    }

    private ProductDTO mapRow(ResultSet rs) throws SQLException {
        return new ProductDTO(
            rs.getInt("product_id"),
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
