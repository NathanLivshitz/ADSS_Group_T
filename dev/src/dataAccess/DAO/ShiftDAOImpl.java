package dataAccess.DAO;

import dataAccess.DTO.ShiftDTO;
import dataAccess.Database.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAOImpl {

    public void insert(ShiftDTO dto) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO shifts (day, shift_type, manager_id, branch_id) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.day);
            statement.setString(2, dto.shiftType);
            statement.setString(3, dto.managerId);
            statement.setString(4, dto.branchId);
            statement.executeUpdate();

            saveShiftRequirements(dto, connection);
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void update(ShiftDTO dto) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "UPDATE shifts SET manager_id = ?, branch_id = ? WHERE day = ? AND shift_type = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.managerId);
            statement.setString(2, dto.branchId);
            statement.setString(3, dto.day);
            statement.setString(4, dto.shiftType);
            statement.executeUpdate();

            saveShiftRequirements(dto, connection);
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public ShiftDTO findByBusinessKey(String day, String shiftType) throws SQLException {
        if (day == null || shiftType == null) {
            return null;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT * FROM shifts WHERE day = ? AND shift_type = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, day);
            statement.setString(2, shiftType);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ShiftDTO dto = new ShiftDTO();
                dto.day = resultSet.getString("day");
                dto.shiftType = resultSet.getString("shift_type");
                dto.managerId = resultSet.getString("manager_id");
                dto.branchId = resultSet.getString("branch_id");

                loadShiftRequirements(dto, connection);
                return dto;
            }
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
        return null;
    }

    public List<ShiftDTO> findAll() throws SQLException {
        List<ShiftDTO> list = new ArrayList<>();
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT * FROM shifts";
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ShiftDTO dto = new ShiftDTO();
                dto.day = resultSet.getString("day");
                dto.shiftType = resultSet.getString("shift_type");
                dto.managerId = resultSet.getString("manager_id");
                dto.branchId = resultSet.getString("branch_id");
                list.add(dto);
            }
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
        for (ShiftDTO dto : list) {
            loadShiftRequirements(dto, connection);
        }
        return list;
    }

    private void saveShiftRequirements(ShiftDTO dto, Connection connection) throws SQLException {
        PreparedStatement deleteStatement = null;
        PreparedStatement insertStatement = null;
        try {
            deleteStatement = connection.prepareStatement("DELETE FROM shift_requirements WHERE day = ? AND shift_type = ?");
            deleteStatement.setString(1, dto.day);
            deleteStatement.setString(2, dto.shiftType);
            deleteStatement.executeUpdate();

            insertStatement = connection.prepareStatement("INSERT INTO shift_requirements (day, shift_type, role_name, amount) VALUES (?, ?, ?, ?)");
            for (int i = 0; i < dto.requirementRoles.size(); i++) {
                insertStatement.setString(1, dto.day);
                insertStatement.setString(2, dto.shiftType);
                insertStatement.setString(3, dto.requirementRoles.get(i));
                insertStatement.setInt(4, dto.requirementAmounts.get(i));
                insertStatement.executeUpdate();
            }
        }
        finally {
            if (deleteStatement != null) {
                deleteStatement.close();
            }
            if (insertStatement != null) {
                insertStatement.close();
            }
        }
    }

    private void loadShiftRequirements(ShiftDTO dto, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT role_name, amount FROM shift_requirements WHERE day = ? AND shift_type = ?");
            statement.setString(1, dto.day);
            statement.setString(2, dto.shiftType);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dto.requirementRoles.add(resultSet.getString("role_name"));
                dto.requirementAmounts.add(resultSet.getInt("amount"));
            }
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
}