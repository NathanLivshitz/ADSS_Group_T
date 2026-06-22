package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IStockItemDAO;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.DTO.StockItemDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SqliteStockItemDAO implements IStockItemDAO {

    @Override
    public void insert(StockItemDTO dto) {
        String sql = "INSERT OR REPLACE INTO stock_items (spec_id, area, shelf, row, quantity, expiry_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.specId());
            ps.setString(2, dto.area());
            ps.setInt(3, dto.shelf());
            ps.setInt(4, dto.row());
            ps.setInt(5, dto.quantity());
            if (dto.expiryDate() != null) {
                ps.setString(6, dto.expiryDate());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemDAO.insert failed", e);
        }
    }

    @Override
    public List<StockItemDTO> findBySpecId(int specId) {
        String sql = "SELECT spec_id, area, shelf, row, quantity, expiry_date FROM stock_items WHERE spec_id = ?";
        List<StockItemDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemDAO.findBySpecId failed", e);
        }
        return result;
    }

    @Override
    public List<StockItemDTO> findAll() {
        String sql = "SELECT spec_id, area, shelf, row, quantity, expiry_date FROM stock_items";
        List<StockItemDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemDAO.findAll failed", e);
        }
        return result;
    }

    @Override
    public void updateQuantity(int specId, String area, int shelf, int row, int newQty) {
        String sql = "UPDATE stock_items SET quantity = ? WHERE spec_id = ? AND area = ? AND shelf = ? AND row = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setInt(2, specId);
            ps.setString(3, area);
            ps.setInt(4, shelf);
            ps.setInt(5, row);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteStockItemDAO.updateQuantity failed", e);
        }
    }

    private StockItemDTO mapRow(ResultSet rs) throws SQLException {
        String expiry = rs.getString("expiry_date");
        return new StockItemDTO(
            rs.getInt("spec_id"),
            rs.getString("area"),
            rs.getInt("shelf"),
            rs.getInt("row"),
            rs.getInt("quantity"),
            expiry
        );
    }
}
