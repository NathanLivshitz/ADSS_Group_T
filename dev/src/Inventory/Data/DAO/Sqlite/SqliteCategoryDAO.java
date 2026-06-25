package Inventory.Data.DAO.Sqlite;

import Inventory.Data.DAO.ICategoryDAO;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.DTO.CategoryDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteCategoryDAO implements ICategoryDAO {

    @Override
    public void insert(CategoryDTO dto) {
        String sql = "INSERT OR REPLACE INTO categories (category_id, category_name, parent_category_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.categoryId());
            ps.setString(2, dto.categoryName());
            ps.setInt(3, dto.parentCategoryId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SqliteCategoryDAO.insert failed", e);
        }
    }

    @Override
    public List<CategoryDTO> findAll() {
        String sql = "SELECT category_id, category_name, parent_category_id FROM categories ORDER BY category_id ASC";
        List<CategoryDTO> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new CategoryDTO(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getInt("parent_category_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SqliteCategoryDAO.findAll failed", e);
        }
        return result;
    }
}
