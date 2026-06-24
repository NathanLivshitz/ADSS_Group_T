package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IPromotionDAO;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.DTO.PromotionDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SqlitePromotionDAO implements IPromotionDAO {

    @Override
    public void deleteAll() {
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM promotions")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqlitePromotionDAO.deleteAll failed", e);
        }
    }

    @Override
    public void insert(PromotionDTO dto) {
        String sql = "INSERT INTO promotions " +
                "(discount_percent, start_date, end_date, " +
                "target_spec_id, target_category_id, " +
                "target_product_name, target_category_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, dto.discountPercent());
            ps.setString(2, dto.startDate());
            ps.setString(3, dto.endDate());
            ps.setInt(4, dto.targetSpecId());
            ps.setInt(5, dto.targetCategoryId());
            if (dto.targetProductName() != null) {
                ps.setString(6, dto.targetProductName());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            if (dto.targetCategoryName() != null) {
                ps.setString(7, dto.targetCategoryName());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqlitePromotionDAO.insert failed", e);
        }
    }

    @Override
    public List<PromotionDTO> findAll() {
        String sql = "SELECT discount_percent, start_date, end_date, " +
                "target_spec_id, target_category_id, " +
                "target_product_name, target_category_name " +
                "FROM promotions";
        List<PromotionDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new PromotionDTO(
                    rs.getDouble("discount_percent"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("target_spec_id"),
                    rs.getInt("target_category_id"),
                    rs.getString("target_product_name"),
                    rs.getString("target_category_name")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqlitePromotionDAO.findAll failed", e);
        }
        return result;
    }
}
