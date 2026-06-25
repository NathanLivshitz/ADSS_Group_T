package dataAccess;

import dataAccess.DAO.EmployeeDAOImpl;
import dataAccess.DAO.ShiftAssignmentDAOImpl;
import dataAccess.DAO.ShiftDAOImpl;
import dataAccess.DTO.EmployeeDTO;
import dataAccess.DTO.ShiftAssignmentDTO;
import dataAccess.DTO.ShiftDTO;
import dataAccess.Database.DataBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DAOTest {

    private EmployeeDAOImpl employeeDAO;
    private ShiftDAOImpl shiftDAO;
    private ShiftAssignmentDAOImpl shiftAssignmentDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        DataBase.closeConnection();
        DataBase.dbName = "superLee_test.db";
        DataBase.getConnection();
        DataBase.clearAllTables();

        this.employeeDAO = new EmployeeDAOImpl();
        this.shiftDAO = new ShiftDAOImpl();
        this.shiftAssignmentDAO = new ShiftAssignmentDAOImpl();
    }

    private EmployeeDTO createDefaultEmployeeDTO(String id, String name) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = id;
        dto.name = name;
        dto.bankAccount = "12345";
        dto.active = 1;
        dto.isManager = 0;
        dto.employmentType = "Full-time";
        dto.salaryType = "Global";
        dto.salary = 10000.0;
        dto.startDate = System.currentTimeMillis();
        dto.vacationDays = 10;
        return dto;
    }

    private ShiftDTO createDefaultShiftDTO(String day, String shiftType) {
        ShiftDTO dto = new ShiftDTO();
        dto.day = day;
        dto.shiftType = shiftType;
        dto.managerId = null;
        dto.branchId = "B1";
        return dto;
    }

    @Test
    public void testInsertAndFindEmployeeSuccess() throws SQLException {
        EmployeeDTO dto = createDefaultEmployeeDTO("555", "Test Employee");
        dto.roles.add("Cashier");
        dto.availabilities.add(new EmployeeDTO.AvailabilityInfo("monday", "morning"));

        employeeDAO.insert(dto);

        EmployeeDTO retrieved = employeeDAO.findById("555");
        assertNotNull(retrieved);
        assertEquals("Test Employee", retrieved.name);
        assertEquals(1, retrieved.roles.size());
        assertEquals("Cashier", retrieved.roles.get(0));
        assertEquals(1, retrieved.availabilities.size());
        assertEquals("monday", retrieved.availabilities.get(0).day);
    }

    @Test
    public void testUpdateEmployeeFieldsSuccess() throws SQLException {
        EmployeeDTO dto = createDefaultEmployeeDTO("555", "Old Name");
        employeeDAO.insert(dto);

        dto.name = "New Name";
        dto.salary = 12000.0;
        dto.bankAccount = "99999";
        employeeDAO.update(dto);

        EmployeeDTO retrieved = employeeDAO.findById("555");
        assertNotNull(retrieved);
        assertEquals("New Name", retrieved.name);
        assertEquals(12000.0, retrieved.salary);
        assertEquals("99999", retrieved.bankAccount);
    }

    @Test
    public void testInsertAndFindDriverWithLicenseSuccess() throws SQLException {
        EmployeeDTO dto = createDefaultEmployeeDTO("1010", "Driver Employee");
        dto.roles.add("Driver");
        dto.licenseType = "B";

        employeeDAO.insert(dto);

        EmployeeDTO retrieved = employeeDAO.findById("1010");
        assertNotNull(retrieved);
        assertEquals("B", retrieved.licenseType);
        assertTrue(retrieved.roles.contains("Driver"));
    }

    @Test
    public void testFindAllEmployeesReturnsCorrectCount() throws SQLException {
        employeeDAO.insert(createDefaultEmployeeDTO("111", "Worker 1"));
        employeeDAO.insert(createDefaultEmployeeDTO("222", "Worker 2"));

        List<EmployeeDTO> list = employeeDAO.findAll();
        assertEquals(2, list.size());
    }

    @Test
    public void testFindNonExistentEmployeeReturnsNull() throws SQLException {
        EmployeeDTO retrieved = employeeDAO.findById("99999");
        assertNull(retrieved);
    }

    @Test
    public void testInsertAndFindShiftWithRequirementsSuccess() throws SQLException {
        ShiftDTO dto = createDefaultShiftDTO("sunday", "morning");
        dto.requirementRoles.add("Warehouse");
        dto.requirementAmounts.add(2);

        shiftDAO.insert(dto);

        ShiftDTO retrieved = shiftDAO.findByBusinessKey("sunday", "morning");
        assertNotNull(retrieved);
        assertEquals("B1", retrieved.branchId);
        assertEquals(1, retrieved.requirementRoles.size());
        assertEquals("Warehouse", retrieved.requirementRoles.get(0));
        assertEquals(2, retrieved.requirementAmounts.get(0));
    }

    @Test
    public void testUpdateShiftManagerSuccess() throws SQLException {
        EmployeeDTO employeeDTO = createDefaultEmployeeDTO("111", "Manager Guy");
        employeeDAO.insert(employeeDTO);

        ShiftDTO shiftDTO = createDefaultShiftDTO("sunday", "morning");
        shiftDAO.insert(shiftDTO);

        shiftDTO.managerId = "111";
        shiftDAO.update(shiftDTO);

        ShiftDTO retrieved = shiftDAO.findByBusinessKey("sunday", "morning");
        assertNotNull(retrieved);
        assertEquals("111", retrieved.managerId);
    }

    @Test
    public void testFindAllShiftsReturnsCorrectCount() throws SQLException {
        shiftDAO.insert(createDefaultShiftDTO("monday", "morning"));
        shiftDAO.insert(createDefaultShiftDTO("tuesday", "evening"));

        List<ShiftDTO> list = shiftDAO.findAll();
        assertEquals(2, list.size());
    }

    @Test
    public void testInsertFindAllAndDeleteAssignmentSuccess() throws SQLException {
        EmployeeDTO employeeDTO = createDefaultEmployeeDTO("777", "Worker");
        employeeDAO.insert(employeeDTO);

        ShiftDTO shiftDTO = createDefaultShiftDTO("monday", "evening");
        shiftDAO.insert(shiftDTO);

        ShiftAssignmentDTO assignmentDTO = new ShiftAssignmentDTO();
        assignmentDTO.employeeId = "777";
        assignmentDTO.day = "monday";
        assignmentDTO.shiftType = "evening";
        assignmentDTO.roleName = "Cashier";

        shiftAssignmentDAO.insert(assignmentDTO);

        List<ShiftAssignmentDTO> allAssignments = shiftAssignmentDAO.findAll();
        assertEquals(1, allAssignments.size());
        assertEquals("777", allAssignments.get(0).employeeId);
        assertEquals("Cashier", allAssignments.get(0).roleName);

        shiftAssignmentDAO.deleteAssignment("777", "monday", "evening");
        assertTrue(shiftAssignmentDAO.findAll().isEmpty());
    }

    @Test
    public void testUpdateAssignmentRoleSuccess() throws SQLException {
        EmployeeDTO employeeDTO = createDefaultEmployeeDTO("777", "Worker");
        employeeDAO.insert(employeeDTO);

        ShiftDTO shiftDTO = createDefaultShiftDTO("monday", "evening");
        shiftDAO.insert(shiftDTO);

        ShiftAssignmentDTO assignmentDTO = new ShiftAssignmentDTO();
        assignmentDTO.employeeId = "777";
        assignmentDTO.day = "monday";
        assignmentDTO.shiftType = "evening";
        assignmentDTO.roleName = "Cashier";
        shiftAssignmentDAO.insert(assignmentDTO);

        assignmentDTO.roleName = "Warehouse";
        shiftAssignmentDAO.update(assignmentDTO, "Cashier");

        List<ShiftAssignmentDTO> allAssignments = shiftAssignmentDAO.findAll();
        assertEquals(1, allAssignments.size());
        assertEquals("Warehouse", allAssignments.get(0).roleName);
    }

    @Test
    public void testDeleteNonExistentAssignmentDoesNotThrowException() {
        assertDoesNotThrow(() -> shiftAssignmentDAO.deleteAssignment("999", "friday", "morning"));
    }
}