package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.IDefectiveReportDAO;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.DTO.DefectiveReportDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteDefectiveReportDAO implements IDefectiveReportDAO {

    @Override
    public void deleteAll() {
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM defective_reports")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteDefectiveReportDAO.deleteAll failed", e);
        }
    }

    @Override
    public void insert(DefectiveReportDTO dto) {
        String sql = "INSERT INTO defective_reports (product_id, quantity, reason, report_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.specId());
            ps.setInt(2, dto.quantity());
            ps.setString(3, dto.reason());
            ps.setString(4, dto.reportDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteDefectiveReportDAO.insert failed", e);
        }
    }

    @Override
    public List<DefectiveReportDTO> findAll() {
        String sql = "SELECT product_id, quantity, reason, report_date FROM defective_reports";
        List<DefectiveReportDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new DefectiveReportDTO(
                    rs.getInt("product_id"),
                    rs.getInt("quantity"),
                    rs.getString("reason"),
                    rs.getString("report_date")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteDefectiveReportDAO.findAll failed", e);
        }
        return result;
    }
}
