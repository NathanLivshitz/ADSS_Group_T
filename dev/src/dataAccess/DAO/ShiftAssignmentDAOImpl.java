package dataAccess.DAO;

import dataAccess.DTO.ShiftAssignmentDTO;
import dataAccess.Database.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShiftAssignmentDAOImpl {

    public void insert(ShiftAssignmentDTO dto) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO shift_assignments (employee_id, day, shift_type, role_name) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.employeeId);
            statement.setString(2, dto.day);
            statement.setString(3, dto.shiftType);
            statement.setString(4, dto.roleName);
            statement.executeUpdate();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void update(ShiftAssignmentDTO dto, String oldRoleName) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "UPDATE shift_assignments SET role_name = ? WHERE employee_id = ? AND day = ? AND shift_type = ? AND role_name = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.roleName);
            statement.setString(2, dto.employeeId);
            statement.setString(3, dto.day);
            statement.setString(4, dto.shiftType);
            statement.setString(5, oldRoleName);
            statement.executeUpdate();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public List<ShiftAssignmentDTO> findAll() throws SQLException {
        List<ShiftAssignmentDTO> list = new ArrayList<>();
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT * FROM shift_assignments";
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
                dto.employeeId = resultSet.getString("employee_id");
                dto.day = resultSet.getString("day");
                dto.shiftType = resultSet.getString("shift_type");
                dto.roleName = resultSet.getString("role_name");
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
        return list;
    }

    public void deleteAssignment(String employeeId, String day, String shiftType) throws SQLException {
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "DELETE FROM shift_assignments WHERE employee_id = ? AND day = ? AND shift_type = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, employeeId);
            statement.setString(2, day);
            statement.setString(3, shiftType);
            statement.executeUpdate();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void deleteAssignmentsByEmployee(Connection connection, String employeeId) throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql = "DELETE FROM shift_assignments WHERE employee_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, employeeId);
            statement.executeUpdate();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
}