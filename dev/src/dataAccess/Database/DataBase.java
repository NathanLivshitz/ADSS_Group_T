package dataAccess.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private static Connection connection = null;
    public static String dbName = "superLee.db";

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
                createTables();
            }
            catch (ClassNotFoundException exception) {
                throw new SQLException(exception);
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = null;
    }

    private static void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS employees " +
                "(id TEXT PRIMARY KEY, name TEXT, bank_account TEXT, active INTEGER, is_manager INTEGER, branch_id TEXT, employment_type TEXT, salary_type TEXT, salary REAL, start_date INTEGER, vacation_days INTEGER, license_type TEXT)");
        statement.execute("CREATE TABLE IF NOT EXISTS employee_roles" +
                " (employee_id TEXT, role_name TEXT, PRIMARY KEY(employee_id, role_name))");
        statement.execute("CREATE TABLE IF NOT EXISTS employee_availabilities" +
                " (employee_id TEXT, day TEXT, shift_type TEXT, PRIMARY KEY(employee_id, day, shift_type))");
        statement.execute("CREATE TABLE IF NOT EXISTS shifts" +
                " (day TEXT, shift_type TEXT, manager_id TEXT, branch_id TEXT, PRIMARY KEY(day, shift_type))");
        statement.execute("CREATE TABLE IF NOT EXISTS shift_requirements" +
                " (day TEXT, shift_type TEXT, role_name TEXT, amount INTEGER, PRIMARY KEY(day, shift_type, role_name))");
        statement.execute("CREATE TABLE IF NOT EXISTS shift_assignments" +
                " (employee_id TEXT, day TEXT, shift_type TEXT, role_name TEXT, PRIMARY KEY(employee_id, day, shift_type))");
        statement.close();
    }

    public static void clearAllTables() throws SQLException {
        Connection connection = getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("DELETE FROM employee_roles");
            statement.execute("DELETE FROM employee_availabilities");
            statement.execute("DELETE FROM shift_assignments");
            statement.execute("DELETE FROM shift_requirements");
            statement.execute("DELETE FROM shifts");
            statement.execute("DELETE FROM employees");
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


}