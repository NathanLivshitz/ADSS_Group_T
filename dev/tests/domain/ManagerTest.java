package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerTest {

    private Manager manager;

    @BeforeEach
    public void setUp() throws SQLException {
        dataAccess.Database.DataBase.closeConnection();
        dataAccess.Database.DataBase.dbName = "superLee_test.db";
        this.manager = new Manager();
        this.manager.resetSystem();
    }

    private Employee createDefaultEmployee(String id, String name) {
        EmploymentConditions conditions = new EmploymentConditions("Full-time", "Global", 10000, new Date(), 10);
        Employee employee = new Employee(name, id, "9999", conditions);
        employee.addRole(manager.searchRole("Cashier"));
        return employee;
    }

    private ShiftManager createDefaultShiftManager(String id, String name) {
        EmploymentConditions conditions = new EmploymentConditions("Full-time", "Global", 12000, new Date(), 10);
        ShiftManager shiftManager = new ShiftManager(name, id, "9999", conditions);
        shiftManager.addRole(manager.searchRole("Cashier"));
        return shiftManager;
    }

    private Driver createDefaultDriver(String id, String name, String license) {
        EmploymentConditions conditions = new EmploymentConditions("Full-time", "Hourly", 60, new Date(), 10);
        Driver driver = new Driver(name, id, "456789", conditions, license);
        driver.addRole(manager.searchRole("Driver"));
        return driver;
    }

    private Shift createDefaultShift(String day, String shiftType) {
        Shift shift = new Shift(shiftType, day);
        shift.addRoleRequirement(manager.searchRole("Cashier"), 1);
        shift.addRoleRequirement(manager.searchRole("Warehouse"), 1);
        shift.addRoleRequirement(manager.searchRole("Driver"), 1);
        return shift;
    }

    @Test
    public void testAddEmployeeSuccess() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Employee retrieved = manager.searchEmployee("123");
        assertNotNull(retrieved);
        assertEquals("John Doe", retrieved.getName());
        assertTrue(retrieved.isActive());
    }

    @Test
    public void testAddDuplicateEmployeeThrowsException() {
        Employee employee1 = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee1);

        Employee employee2 = createDefaultEmployee("123", "Jane Doe");
        assertThrows(IllegalArgumentException.class, () -> manager.addEmployee(employee2));
    }

    @Test
    public void testFireEmployeeSetsInactiveButKeepsData() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        manager.removeEmployee("123");

        Employee retrieved = manager.searchEmployee("123");
        assertNotNull(retrieved);
        assertFalse(retrieved.isActive());
    }

    @Test
    public void testUpdateEmployeeFields() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        manager.updateBankAccount("123", "77777");
        manager.updateEmployeeEmploymentType("123", "Part-time");
        manager.updateEmployeeSalaryType("123", "Hourly");
        manager.updateEmployeeSalary("123", 60.0);
        manager.updateEmployeeVacationDays("123", 15);

        Employee retrieved = manager.searchEmployee("123");
        assertEquals("77777", retrieved.getBankAccount());
        assertEquals("Part-time", retrieved.getEmploymentConditions().getEmploymentType());
        assertEquals("Hourly", retrieved.getEmploymentConditions().getSalaryType());
        assertEquals(60.0, retrieved.getEmploymentConditions().getSalary());
        assertEquals(15, retrieved.getEmploymentConditions().getVacationDays());
    }

    @Test
    public void testUpdateDriverLicenseType() {
        Driver driver = createDefaultDriver("1010", "Dan Driver", "B");
        manager.addEmployee(driver);

        manager.updateLicenseType("1010", "C");
        Employee retrieved = manager.searchEmployee("1010");
        assertTrue(retrieved instanceof Driver);
        assertEquals("C", ((Driver) retrieved).getLicenseType());
    }

    @Test
    public void testUpdateLicenseTypeForEmployeePromotedToDriver() {
        Employee employee = createDefaultEmployee("555", "Promoted Employee");
        manager.addEmployee(employee);

        manager.addRoleToEmployee(manager.searchRole("Driver"), "555");
        manager.updateLicenseType("555", "B");

        Employee retrieved = manager.searchEmployee("555");
        assertNotNull(retrieved);
        assertTrue(retrieved.containsRole(manager.searchRole("Driver")));
    }

    @Test
    public void testGrantAndRevokeShiftManagerCertification() {
        Employee employee = createDefaultEmployee("111", "Michael Brown");
        manager.addEmployee(employee);

        manager.updateShiftManagerStatus("111", true);
        Employee managerRetrieved = manager.searchEmployee("111");
        assertTrue(managerRetrieved instanceof ShiftManager);

        manager.updateShiftManagerStatus("111", false);
        Employee regularRetrieved = manager.searchEmployee("111");
        assertFalse(regularRetrieved instanceof ShiftManager);
    }

    @Test
    public void testAddAndRemoveRolesToEmployee() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Role warehouseRole = manager.searchRole("Warehouse");
        manager.addRoleToEmployee(warehouseRole, "123");
        assertTrue(manager.searchEmployee("123").containsRole(warehouseRole));

        manager.removeRoleToEmployee(warehouseRole, "123");
        assertFalse(manager.searchEmployee("123").containsRole(warehouseRole));
    }

    @Test
    public void testSubmitAvailabilitySuccess() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Availability availability = new Availability("monday", "morning");
        manager.submitAvailability("123", availability);

        Employee retrieved = manager.searchEmployee("123");
        assertTrue(retrieved.containsAvailability(availability));
    }

    @Test
    public void testSubmitAvailabilityOnClosedDayThrowsException() {
        manager.addClosedDay("wednesday");

        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Availability availability = new Availability("wednesday", "morning");
        assertThrows(IllegalArgumentException.class, () -> manager.submitAvailability("123", availability));
    }

    @Test
    public void testSubmitAvailabilityAfterDeadlineThrowsException() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Date futureDeadline = new Date(System.currentTimeMillis() + 86400000);
        manager.updateDeadline(futureDeadline);

        manager.updateDeadline(new Date(System.currentTimeMillis() + 1000));
        try {
            Thread.sleep(1005);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Availability availability = new Availability("monday", "morning");
        assertThrows(IllegalArgumentException.class, () -> manager.submitAvailability("123", availability));
    }

    @Test
    public void testEditAndRemoveAvailabilityScenarios() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Availability oldAvailability = new Availability("monday", "morning");
        Availability newAvailability = new Availability("tuesday", "evening");
        manager.submitAvailability("123", oldAvailability);

        manager.editAvailability("123", oldAvailability, newAvailability);
        Employee retrieved = manager.searchEmployee("123");
        assertFalse(retrieved.containsAvailability(oldAvailability));
        assertTrue(retrieved.containsAvailability(newAvailability));

        manager.removeAvailability("123", newAvailability);
        assertFalse(manager.searchEmployee("123").containsAvailability(newAvailability));
    }

    @Test
    public void testEditAvailabilityAfterDeadlineThrowsException() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Availability oldAvailability = new Availability("monday", "morning");
        Availability newAvailability = new Availability("tuesday", "evening");
        manager.submitAvailability("123", oldAvailability);

        manager.updateDeadline(new Date(System.currentTimeMillis() + 1000));
        try {
            Thread.sleep(1005);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThrows(IllegalArgumentException.class, () -> manager.editAvailability("123", oldAvailability, newAvailability));
    }

    @Test
    public void testEditAvailabilityWhenEmployeeAlreadyAssignedThrowsException() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Michael Brown");
        manager.addEmployee(shiftManager);

        Availability availability = new Availability("sunday", "morning");
        manager.submitAvailability("111", availability);

        Shift shift = createDefaultShift("sunday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", manager.searchRole("Cashier"), shift, false);

        Availability newAvailability = new Availability("monday", "evening");
        assertThrows(IllegalArgumentException.class, () -> manager.editAvailability("111", availability, newAvailability));
    }

    @Test
    public void testFireNonExistentEmployeeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> manager.removeEmployee("99999"));
    }

    @Test
    public void testEmployeeWithNoSubmittedAvailabilityIsAvailableByDefault() {
        Employee employee = createDefaultEmployee("9999", "Guy");
        manager.addEmployee(employee);

        Shift shift = createDefaultShift("thursday", "evening");
        assertTrue(manager.isEmployeeAvailableForShift(employee, shift));
    }

    @Test
    public void testRemoveAvailabilitySuccess() {
        Employee employee = createDefaultEmployee("123", "John Doe");
        manager.addEmployee(employee);

        Availability availability = new Availability("monday", "morning");
        manager.submitAvailability("123", availability);
        manager.removeAvailability("123", availability);

        assertFalse(manager.searchEmployee("123").containsAvailability(availability));
    }

    @Test
    public void testAssignShiftManagerSuccess() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);
        manager.assignShiftManagerToShift("111", null, shift, true);

        assertEquals("111", manager.searchShift("monday", "morning").getShiftManager().getId());
    }

    @Test
    public void testAssignShiftManagerNotCertifiedThrowsException() {
        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        assertThrows(IllegalArgumentException.class, () -> manager.assignShiftManagerToShift("222", null, shift, true));
    }

    @Test
    public void testAssignEmployeeToShiftSuccess() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), shift, true);

        assertEquals(1, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), shift));
    }

    @Test
    public void testAssignEmployeeToShiftNoManagerThrowsException() {
        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        assertThrows(IllegalArgumentException.class, () -> manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), shift, true));
    }

    @Test
    public void testAssignEmployeeNoAvailabilityNoOverrideThrowsException() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);
        manager.submitAvailability(employee.getId(), new Availability("tuesday", "evening"));

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);
        manager.assignShiftManagerToShift("111", null, shift, true);

        assertThrows(IllegalArgumentException.class, () -> manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), shift, false));
    }

    @Test
    public void testChangeRoleToAssignmentSuccess() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);
        manager.addRoleToEmployee(manager.searchRole("Warehouse"), employee.getId());

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), shift, true);
        manager.changeRoleToAssignment("222", shift, manager.searchRole("Warehouse"));

        assertEquals(0, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), shift));
        assertEquals(1, manager.countAssignmentsForRoleInShift(manager.searchRole("Warehouse"), shift));
    }

    @Test
    public void testChangeShiftToAssignmentSuccess() {
        ShiftManager shiftManager1 = createDefaultShiftManager("111", "Manager1");
        manager.addEmployee(shiftManager1);

        ShiftManager shiftManager2 = createDefaultShiftManager("333", "Manager2");
        manager.addEmployee(shiftManager2);

        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);

        Shift oldShift = createDefaultShift("monday", "morning");
        manager.addShift(oldShift);

        Shift newShift = createDefaultShift("tuesday", "evening");
        manager.addShift(newShift);

        manager.assignShiftManagerToShift("111", null, oldShift, true);
        manager.assignShiftManagerToShift("333", null, newShift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), oldShift, true);
        manager.changeShiftToAssignment("222", oldShift, newShift);

        assertEquals(0, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), oldShift));
        assertEquals(1, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), newShift));
    }

    @Test
    public void testAddRoleToExistingShiftManagerSuccess() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.addRoleToExistingShiftManager("111", manager.searchRole("Cashier"), shift);

        assertEquals(1, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), shift));
    }

    @Test
    public void testRemoveAssignmentSuccess() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Employee employee = createDefaultEmployee("222", "Worker");
        manager.addEmployee(employee);

        Shift shift = createDefaultShift("monday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Cashier"), shift, true);
        manager.removeAssignment("222", shift);

        assertEquals(0, manager.countAssignmentsForRoleInShift(manager.searchRole("Cashier"), shift));
    }

    @Test
    public void testDeliveryValidationMissingWarehouseAndDriverReturnsFalse() {
        Shift shift = createDefaultShift("sunday", "morning");
        manager.addShift(shift);

        assertTrue(manager.hasDeliveryInShift(shift));
        assertFalse(manager.hasAssignedDriverForDelivery(shift));
        assertFalse(manager.isWarehouseAssignedToShift(shift));
    }

    @Test
    public void testDeliveryValidationWithDriverButNoWarehouseWorker() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Driver driver = createDefaultDriver("1010", "Dan Driver", "B");
        manager.addEmployee(driver);

        Shift shift = createDefaultShift("sunday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("1010", manager.searchRole("Driver"), shift, true);

        assertTrue(manager.hasDeliveryInShift(shift));
        assertTrue(manager.hasAssignedDriverForDelivery(shift));
        assertFalse(manager.isWarehouseAssignedToShift(shift));
    }

    @Test
    public void testDeliveryValidationWithWarehouseWorkerButNoDriver() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Employee warehouseWorker = createDefaultEmployee("222", "Warehouse Guy");
        manager.addEmployee(warehouseWorker);
        manager.addRoleToEmployee(manager.searchRole("Warehouse"), "222");

        Shift shift = createDefaultShift("sunday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Warehouse"), shift, true);

        assertTrue(manager.hasDeliveryInShift(shift));
        assertFalse(manager.hasAssignedDriverForDelivery(shift));
        assertTrue(manager.isWarehouseAssignedToShift(shift));
    }

    @Test
    public void testDeliveryValidationWithDriverWrongLicenseType() {
        ShiftManager shiftManager = createDefaultShiftManager("111", "Manager");
        manager.addEmployee(shiftManager);

        Driver driver = createDefaultDriver("1010", "Dan Driver", "A");
        manager.addEmployee(driver);

        Employee warehouseWorker = createDefaultEmployee("222", "Warehouse Guy");
        manager.addEmployee(warehouseWorker);
        manager.addRoleToEmployee(manager.searchRole("Warehouse"), "222");

        Shift shift = createDefaultShift("sunday", "morning");
        manager.addShift(shift);

        manager.assignShiftManagerToShift("111", null, shift, true);
        manager.assignEmployeeToShift("1010", manager.searchRole("Driver"), shift, true);
        manager.assignEmployeeToShift("222", manager.searchRole("Warehouse"), shift, true);

        assertTrue(manager.hasDeliveryInShift(shift));
        assertFalse(manager.hasAssignedDriverForDelivery(shift));
        assertTrue(manager.isWarehouseAssignedToShift(shift));
    }
}