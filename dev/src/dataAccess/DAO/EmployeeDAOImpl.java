package dataAccess.DAO;

import dataAccess.DTO.EmployeeDTO;
import dataAccess.Database.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl {

    public void insert(EmployeeDTO dto) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO employees (id, name, bank_account, active, is_manager, branch_id, employment_type, salary_type, salary, start_date, vacation_days, license_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.id);
            statement.setString(2, dto.name);
            statement.setString(3, dto.bankAccount);
            statement.setInt(4, dto.active);
            statement.setInt(5, dto.isManager);
            statement.setString(6, dto.branchId);
            statement.setString(7, dto.employmentType);
            statement.setString(8, dto.salaryType);
            statement.setDouble(9, dto.salary);
            statement.setLong(10, dto.startDate);
            statement.setInt(11, dto.vacationDays);
            statement.setString(12, dto.licenseType);
            statement.executeUpdate();

            saveEmployeeRoles(dto, connection);
            saveEmployeeAvailabilities(dto, connection);
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void update(EmployeeDTO dto) throws SQLException {
        if (dto == null) {
            return;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "UPDATE employees SET name = ?, bank_account = ?, active = ?, is_manager = ?, branch_id = ?, employment_type = ?, salary_type = ?, salary = ?, start_date = ?, vacation_days = ?, license_type = ? WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, dto.name);
            statement.setString(2, dto.bankAccount);
            statement.setInt(3, dto.active);
            statement.setInt(4, dto.isManager);
            statement.setString(5, dto.branchId);
            statement.setString(6, dto.employmentType);
            statement.setString(7, dto.salaryType);
            statement.setDouble(8, dto.salary);
            statement.setLong(9, dto.startDate);
            statement.setInt(10, dto.vacationDays);
            statement.setString(11, dto.licenseType);
            statement.setString(12, dto.id);
            statement.executeUpdate();

            saveEmployeeRoles(dto, connection);
            saveEmployeeAvailabilities(dto, connection);
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public EmployeeDTO findById(String id) throws SQLException {
        if (id == null) {
            return null;
        }
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT * FROM employees WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                EmployeeDTO dto = new EmployeeDTO();
                dto.id = resultSet.getString("id");
                dto.name = resultSet.getString("name");
                dto.bankAccount = resultSet.getString("bank_account");
                dto.active = resultSet.getInt("active");
                dto.isManager = resultSet.getInt("is_manager");
                dto.branchId = resultSet.getString("branch_id");
                dto.employmentType = resultSet.getString("employment_type");
                dto.salaryType = resultSet.getString("salary_type");
                dto.salary = resultSet.getDouble("salary");
                dto.startDate = resultSet.getLong("start_date");
                dto.vacationDays = resultSet.getInt("vacation_days");
                dto.licenseType = resultSet.getString("license_type");

                loadEmployeeRoles(dto, connection);
                loadEmployeeAvailabilities(dto, connection);
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

    public List<EmployeeDTO> findAll() throws SQLException {
        List<String> employeeIds = new ArrayList<>();
        Connection connection = DataBase.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT id FROM employees";
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                employeeIds.add(resultSet.getString("id"));
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
        List<EmployeeDTO> list = new ArrayList<>();
        for (String id : employeeIds) {
            EmployeeDTO dto = findById(id);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    private void saveEmployeeRoles(EmployeeDTO dto, Connection connection) throws SQLException {
        PreparedStatement deleteStatement = null;
        PreparedStatement insertStatement = null;
        try {
            deleteStatement = connection.prepareStatement("DELETE FROM employee_roles WHERE employee_id = ?");
            deleteStatement.setString(1, dto.id);
            deleteStatement.executeUpdate();

            insertStatement = connection.prepareStatement("INSERT INTO employee_roles (employee_id, role_name) VALUES (?, ?)");
            for (String roleName : dto.roles) {
                insertStatement.setString(1, dto.id);
                insertStatement.setString(2, roleName);
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

    private void loadEmployeeRoles(EmployeeDTO dto, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT role_name FROM employee_roles WHERE employee_id = ?");
            statement.setString(1, dto.id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dto.roles.add(resultSet.getString("role_name"));
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    private void saveEmployeeAvailabilities(EmployeeDTO dto, Connection connection) throws SQLException {
        PreparedStatement deleteStatement = null;
        PreparedStatement insertStatement = null;
        try {
            deleteStatement = connection.prepareStatement("DELETE FROM employee_availabilities WHERE employee_id = ?");
            deleteStatement.setString(1, dto.id);
            deleteStatement.executeUpdate();

            insertStatement = connection.prepareStatement("INSERT INTO employee_availabilities (employee_id, day, shift_type) VALUES (?, ?, ?)");
            for (EmployeeDTO.AvailabilityInfo av : dto.availabilities) {
                insertStatement.setString(1, dto.id);
                insertStatement.setString(2, av.day);
                insertStatement.setString(3, av.shiftType);
                insertStatement.executeUpdate();
            }
        } finally {
            if (deleteStatement != null) {
                deleteStatement.close();
            }
            if (insertStatement != null) {
                insertStatement.close();
            }
        }
    }

    private void loadEmployeeAvailabilities(EmployeeDTO dto, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT day, shift_type FROM employee_availabilities WHERE employee_id = ?");
            statement.setString(1, dto.id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dto.availabilities.add(new EmployeeDTO.AvailabilityInfo(resultSet.getString("day"), resultSet.getString("shift_type")));
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