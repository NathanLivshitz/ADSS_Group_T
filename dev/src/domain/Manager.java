package domain;

import dataAccess.DAO.EmployeeDAOImpl;
import dataAccess.DAO.ShiftAssignmentDAOImpl;
import dataAccess.DAO.ShiftDAOImpl;
import dataAccess.DTO.*;
import mock.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    // private List<Employee> employees;
    private List<Shift> shifts;
    private List<ShiftAssignment> assignments;
    private Date deadLine;
    private List<String> closedDays;
    private List<Role> storeRoles = new LinkedList<>(java.util.Arrays.asList(
            new Role("Cashier"),
            new Role("Warehouse"),
            new Role("Driver")));
    private DeliveryServiceMock deliveryService;
    private EmployeeDAOImpl employeeDAO;
    private ShiftDAOImpl shiftDAO;
    private ShiftAssignmentDAOImpl shiftAssignmentDAO;

    public Manager() {
        // this.employees = new LinkedList<>();
        this.shifts = new LinkedList<>();
        this.assignments = new LinkedList<>();
        this.deadLine = null;
        this.closedDays = new LinkedList<>();
        this.deliveryService = new DeliveryServiceMock();
        this.employeeDAO = new EmployeeDAOImpl();
        this.shiftDAO = new ShiftDAOImpl();
        this.shiftAssignmentDAO = new ShiftAssignmentDAOImpl();
    }

    public Manager(DeliveryServiceMock deliveryService) {
        this();
        if (deliveryService == null) {
            throw new IllegalArgumentException("delivery service cannot be null");
        }
        this.deliveryService = deliveryService;
    }

    public void addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (searchEmployee(employee.getId()) != null) {
            throw new IllegalArgumentException("Employee with ID: " + employee.getId() + " is already exists");
        }
        try {
            this.employeeDAO.insert(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeEmployee(String id) {
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        employee.setActive(false);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateBankAccount(String id, String bankAccount) {
        if (id == null || bankAccount == null) {
            throw new IllegalArgumentException("invalid input");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        employee.setBankAccount(bankAccount);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Employee> getEmployees() {
        try {
            List<Employee> list = new LinkedList<>();
            for (EmployeeDTO dto : this.employeeDAO.findAll()) {
                list.add(buildEmployeeFromDTO(dto));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Shift> getShifts() {
        try {
            shifts = new LinkedList<>();
            for (ShiftDTO dto : this.shiftDAO.findAll()) {
                shifts.add(buildShiftFromDTO(dto));
            }
            return shifts;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<ShiftAssignment> getAssignments() {
        try {
            assignments = new LinkedList<>();
            for (ShiftAssignmentDTO dto : this.shiftAssignmentDAO.findAll()) {
                Employee employee = searchEmployee(dto.employeeId);
                Shift shift = searchShift(dto.day, dto.shiftType);
                if (employee != null && shift != null) {
                    Role role = new Role(dto.roleName);
                    ShiftAssignment shiftAssignment = new ShiftAssignment(employee, shift, role);
                    assignments.add(shiftAssignment);
                }
            }
            return assignments;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Role> getStoreRoles() {
        return this.storeRoles;
    }

    public void addStoreRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (searchRole(role.getRoleName()) != null) {
            throw new IllegalArgumentException("Role already exists");
        }
        storeRoles.add(role);
    }

    public Role searchRole(String name) {
        for (Role role : storeRoles) {
            if (role.getRoleName().equalsIgnoreCase(name))
                return role;
        }
        return null;
    }

    public void addShift(Shift shift) {
        if (shift == null)
            throw new IllegalArgumentException("Shift cannot be null");
        if (searchShift(shift.getDay(), shift.getShiftType()) != null) {
            throw new IllegalArgumentException("Shift already exists");
        }
        shifts.add(shift);
        try {
            this.shiftDAO.insert(mapToShiftDTO(shift));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public DeliveryServiceMock getDeliveryService() {
        return deliveryService;
    }

    public Shift searchShift(String day, String type) {
        if (day == null || type == null) {
            return null;
        }
        for (Shift shift : shifts) {
            if (shift.getDay().equalsIgnoreCase(day) && shift.getShiftType().equalsIgnoreCase(type))
                return shift;
        }
        try {
            ShiftDTO dto = this.shiftDAO.findByBusinessKey(day, type);
            if (dto != null) {
                Shift shift = buildShiftFromDTO(dto);
                shifts.add(shift);
                return shift;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public void updateShiftManagerStatus(String id, boolean isManager) {
        if (id == null) {
            throw new IllegalArgumentException("invalid input");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }

        if (isManager) {
            if (employee instanceof ShiftManager) {
                throw new IllegalArgumentException("employee is already a shift manager");
            }
            ShiftManager shiftManager = new ShiftManager(employee);

            try {
                this.employeeDAO.update(mapToEmployeeDTO(shiftManager));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            if (!(employee instanceof ShiftManager)) {
                throw new IllegalArgumentException("employee is not a shift manager");
            }
            Employee regularEmployee = new Employee(employee.getName(), employee.getId(), employee.getBankAccount(),
                    employee.getEmploymentConditions());
            regularEmployee.setActive(employee.isActive());

            if (employee.getBranch() != null) {
                regularEmployee.setBranch(employee.getBranch());
            }

            for (Role role : employee.getRoles()) {
                regularEmployee.addRole(role);
            }
            for (Availability availability : employee.getAvailabilities()) {
                regularEmployee.addAvailability(availability);
            }
            try {
                this.employeeDAO.update(mapToEmployeeDTO(regularEmployee));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public void updateEmployeeEmploymentType(String id, String employmentType) {
        if (id == null || employmentType == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setEmploymentType(employmentType);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateEmployeeSalaryType(String id, String salaryType) {
        if (id == null || salaryType == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setSalaryType(salaryType);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateEmployeeSalary(String id, double salary) {
        if (id == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setSalary(salary);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateEmployeeVacationDays(String id, int vacationDays) {
        if (id == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setVacationDays(vacationDays);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Employee searchEmployee(String id) {
        if (id != null) {
            try {
                EmployeeDTO dto = this.employeeDAO.findById(id);
                return buildEmployeeFromDTO(dto);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return null;
    }

    public void addRoleToEmployee(Role role, String id) {
        if (role == null || id == null) {
            throw new IllegalArgumentException("invalid input");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee doesn't exist");
        }
        employee.addRole(role);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeRoleToEmployee(Role role, String id) {
        if (role == null || id == null) {
            throw new IllegalArgumentException("invalid input");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        if (!employee.containsRole(role)) {
            throw new IllegalArgumentException("role does not exist");
        }
        employee.removeRole(role);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void loadAssignmentsFromDB() {
        try {
            getAssignments();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ShiftAssignment findAssignment(String id, Shift shift) {
        loadAssignmentsFromDB();

        for (ShiftAssignment assignment : assignments) {
            if (assignment.getEmployee().getId().equals(id) && assignment.getShift().equals(shift)) {
                return assignment;
            }
        }
        return null;
    }

    public void changeRoleToAssignment(String id, Shift shift, Role newRole) {
        if (id == null || newRole == null || shift == null) {
            throw new IllegalArgumentException("invalid input");
        }
        ShiftAssignment assignment = findAssignment(id, shift);
        if (assignment == null) {
            throw new IllegalArgumentException("assignment does not exist");
        }
        if (searchRole(newRole.getRoleName()) == null) {
            throw new IllegalArgumentException("Role does not exist in the store definitions");
        }

        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        if (!employee.containsRole(newRole)) {
            throw new IllegalArgumentException("employee is not qualified for this role");
        }
        if (!employee.isActive()) {
            throw new IllegalArgumentException("employee is not active");
        }
        Role currentRole = assignment.getRole();
        if (currentRole.equals(newRole)) {
            return;
        }
        if (!canAssignRoleToShift(newRole, shift)) {
            throw new IllegalArgumentException("role is not required or already full in this shift");
        }
        assignment.setRole(newRole);
        try {
            ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
            dto.employeeId = id;
            dto.day = shift.getDay();
            dto.shiftType = shift.getShiftType();
            dto.roleName = newRole.getRoleName();
            this.shiftAssignmentDAO.update(dto, currentRole.getRoleName());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void changeShiftToAssignment(String id, Shift oldShift, Shift newShift) {
        if (id == null || oldShift == null || newShift == null) {
            throw new IllegalArgumentException("invalid input");
        }
        ShiftAssignment assignment = findAssignment(id, oldShift);
        if (assignment == null) {
            throw new IllegalArgumentException("assignment does not exist");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        if (oldShift.equals(newShift)) {
            return;
        }
        if (!shifts.contains(newShift)) {
            throw new IllegalArgumentException("shift does not exist");
        }
        if (!isShiftValid(newShift)) {
            throw new IllegalArgumentException("cannot move employee to a shift without a shift manager");
        }
        if (isClosedDay(newShift.getDay())) {
            throw new IllegalArgumentException("cannot assign shift on closed day");
        }
        if (!employee.isActive()) {
            throw new IllegalArgumentException("employee is not active");
        }
        if (isEmployeeAlreadyAssignedToShift(employee, newShift)) {
            throw new IllegalArgumentException("employee is already assigned to this shift");
        }
        if (!isEmployeeAvailableForShift(employee, newShift)) {
            throw new IllegalArgumentException("employee can't work at this shift");
        }
        Role currentRole = assignment.getRole();
        if (!canAssignRoleToShift(currentRole, newShift)) {
            throw new IllegalArgumentException("role is not required or already full in this shift");
        }
        if (oldShift.getShiftManager() != null && oldShift.getShiftManager().equals(employee)) {
            oldShift.setDesignatedManager(null);
            try {
                this.shiftDAO.update(mapToShiftDTO(oldShift));
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage());
            }
        }
        assignment.setShift(newShift);
        try {
            this.shiftAssignmentDAO.deleteAssignment(id, oldShift.getDay(), oldShift.getShiftType());
            ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
            dto.employeeId = id;
            dto.day = newShift.getDay();
            dto.shiftType = newShift.getShiftType();
            dto.roleName = currentRole.getRoleName();
            this.shiftAssignmentDAO.insert(dto);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isShiftValid(Shift shift) {
        return shift.getShiftManager() != null;
    }

    public void assignEmployeeToShift(String id, Role role, Shift shift, boolean isOverride) {
        if (id == null || role == null || shift == null)
            throw new IllegalArgumentException("invalid input");

        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        if (!shifts.contains(shift))
            throw new IllegalArgumentException("shift does not exist");
        if (isClosedDay(shift.getDay()))
            throw new IllegalArgumentException("cannot assign shift on a closed day");
        if (!employee.containsRole(role))
            throw new IllegalArgumentException("employee is not qualified for this role");
        if (!employee.isActive())
            throw new IllegalArgumentException("employee is not active");
        if (!isShiftValid(shift))
            throw new IllegalArgumentException("cannot assign employees before selecting a shift manager");

        if (isEmployeeAlreadyAssignedToShift(employee, shift))
            throw new IllegalArgumentException("employee is already assigned to this shift");
        if (!canAssignRoleToShift(role, shift))
            throw new IllegalArgumentException("role is not required or already full in this shift");
        if (!isOverride && !isEmployeeAvailableForShift(employee, shift)) {
            throw new IllegalArgumentException("employee can't work at this shift");
        }
        if (!isShiftValid(shift)) {
            throw new IllegalArgumentException("cannot assign employees before selecting a shift manager");
        }
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
        assignments.add(assignment);
        try {
            ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
            dto.employeeId = id;
            dto.day = shift.getDay();
            dto.shiftType = shift.getShiftType();
            dto.roleName = role.getRoleName();
            this.shiftAssignmentDAO.insert(dto);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public int countAssignmentsForRoleInShift(Role role, Shift shift) {
        if (role == null || shift == null) {
            throw new IllegalArgumentException("invalid input");
        }
        loadAssignmentsFromDB();

        int count = 0;
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getShift().equals(shift) && assignment.getRole().equals(role))
                count++;
        }
        return count;
    }

    public void addRoleToExistingShiftManager(String id, Role role, Shift shift) {
        if (id == null || role == null || shift == null) {
            throw new IllegalArgumentException("invalid input");
        }

        Employee employee = searchEmployee(id);

        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }

        if (!shifts.contains(shift)) {
            throw new IllegalArgumentException("shift does not exist");
        }

        if (!employee.isActive()) {
            throw new IllegalArgumentException("employee is not active");
        }

        if (!(employee instanceof ShiftManager)) {
            throw new IllegalArgumentException("employee is not a shift manager");
        }

        if (shift.getShiftManager() == null || !shift.getShiftManager().equals(employee)) {
            throw new IllegalArgumentException("employee is not the shift manager of this shift");
        }

        if (!employee.containsRole(role)) {
            throw new IllegalArgumentException("employee is not qualified for this role");
        }

        if (isEmployeeAlreadyAssignedToShift(employee, shift)) {
            throw new IllegalArgumentException("shift manager already has a work role in this shift");
        }

        if (!canAssignRoleToShift(role, shift)) {
            throw new IllegalArgumentException("role is not required or already full in this shift");
        }

        ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
        assignments.add(assignment);
        try {
            ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
            dto.employeeId = id;
            dto.day = shift.getDay();
            dto.shiftType = shift.getShiftType();
            dto.roleName = role.getRoleName();
            this.shiftAssignmentDAO.insert(dto);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean canAssignRoleToShift(Role role, Shift shift) {
        int required = shift.numOfRoles(role);
        int assigned = countAssignmentsForRoleInShift(role, shift);
        return required > assigned;
    }

    private boolean isEmployeeAlreadyAssignedToShift(Employee employee, Shift shift) {
        loadAssignmentsFromDB();

        for (ShiftAssignment assignment : assignments) {
            if (assignment.getEmployee().equals(employee) && assignment.getShift().equals(shift)) {
                return true;
            }
        }
        return false;
    }

    public List<Employee> getAvailableEmployeesForShift(Shift shift) {
        List<Employee> available = new LinkedList<>();

        try {
            for (Employee employee : getEmployees()) {
                if (employee.isActive() && !isEmployeeAlreadyAssignedToShift(employee, shift)) {
                    if (isEmployeeAvailableForShift(employee, shift)) {
                        available.add(employee);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return available;
    }

    public List<Employee> getAvailableManagersForShift(Shift shift) {
        List<Employee> availableManagers = new LinkedList<>();
        for (Employee employee : getAvailableEmployeesForShift(shift)) {
            if (employee instanceof ShiftManager) {
                availableManagers.add(employee);
            }
        }
        return availableManagers;
    }

    public int getRemainingSpotsForRole(Shift shift, Role role) {
        int required = shift.numOfRoles(role);
        int assigned = countAssignmentsForRoleInShift(role, shift);
        return required - assigned;
    }

    public void assignShiftManagerToShift(String id, Role role, Shift shift, boolean isOverride) {
        if (id == null || shift == null)
            throw new IllegalArgumentException("invalid input");

        Employee employee = searchEmployee(id);

        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");

        if (!shifts.contains(shift))
            throw new IllegalArgumentException("shift does not exist");

        if (isClosedDay(shift.getDay()))
            throw new IllegalArgumentException("cannot assign shift on a closed day");

        if (!employee.isActive())
            throw new IllegalArgumentException("employee is not active");

        if (!(employee instanceof ShiftManager))
            throw new IllegalArgumentException("employee is not certified as a shift manager");

        if (!isOverride && !isEmployeeAvailableForShift(employee, shift))
            throw new IllegalArgumentException("employee can't work at this shift");

        ShiftManager shiftManager = (ShiftManager) employee;
        shift.setDesignatedManager(shiftManager);
        try {
            this.shiftDAO.update(mapToShiftDTO(shift));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (role != null) {
            if (!employee.containsRole(role))
                throw new IllegalArgumentException("employee is not qualified for this role");
            if (isEmployeeAlreadyAssignedToShift(employee, shift))
                throw new IllegalArgumentException("employee is already assigned to this shift");
            if (!canAssignRoleToShift(role, shift))
                throw new IllegalArgumentException("role is not required or already full in this shift");

            ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
            assignments.add(assignment);
            try {
                ShiftAssignmentDTO dto = new ShiftAssignmentDTO();
                dto.employeeId = id;
                dto.day = shift.getDay();
                dto.shiftType = shift.getShiftType();
                dto.roleName = role.getRoleName();
                this.shiftAssignmentDAO.insert(dto);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public boolean isEmployeeAvailableForShift(Employee employee, Shift shift) {
        if (employee.getAvailabilities().isEmpty()) {
            return true;
        }
        for (Availability availability : employee.getAvailabilities()) {
            if (availability.getDay().equalsIgnoreCase(shift.getDay())
                    && availability.getShiftType().equalsIgnoreCase(shift.getShiftType())) {
                return true;
            }
        }
        return false;
    }

    public void editAvailability(String id, Availability availability, Availability newAvailability) {
        if (id == null || availability == null || newAvailability == null)
            throw new IllegalArgumentException("Invalid input");
        if (!isBeforeDeadline()) {
            throw new IllegalArgumentException("Deadline has passed, cannot edit availability.");
        }
        if (isClosedDay(newAvailability.getDay())) {
            throw new IllegalArgumentException("cannot submit availability for a closed day");
        }
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("Employee does not exist");

        Shift oldShift = searchShift(availability.getDay(), availability.getShiftType());
        if (oldShift != null && isEmployeeAlreadyAssignedToShift(employee, oldShift)) {
            throw new IllegalArgumentException(
                    "Cannot edit availability: Employee is already assigned to this shift. Please ask HR to remove your assignment first.");
        }
        if (!employee.getAvailabilities().remove(availability)) {
            throw new IllegalArgumentException("The original availability was not found.");
        }
        try {
            employee.addAvailability(newAvailability);
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            employee.getAvailabilities().add(availability);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeAvailability(String id, Availability availability) {
        if (availability == null || id == null)
            throw new IllegalArgumentException("Invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("Employee does not exist");
        if (!isBeforeDeadline()) {
            throw new IllegalArgumentException("Deadline has passed, cannot modify availability.");
        }
        Shift shift = searchShift(availability.getDay(), availability.getShiftType());
        if (shift != null && isEmployeeAlreadyAssignedToShift(employee, shift)) {
            throw new IllegalArgumentException(
                    "Cannot remove availability: Employee is already assigned to this shift. Please ask HR to remove your assignment first.");
        }
        employee.getAvailabilities().remove(availability);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateDeadline(Date deadLine) {
        if (deadLine == null) {
            throw new IllegalArgumentException("deadline cannot be null");
        }
        if (deadLine.before(new Date())) {
            throw new IllegalArgumentException("deadline must be in the future");
        }
        this.deadLine = deadLine;
    }

    public void submitAvailability(String id, Availability availability) {
        if (availability == null || id == null) {
            throw new IllegalArgumentException("invalid input");
        }
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        if (!employee.isActive()) {
            throw new IllegalArgumentException("employee is not active");
        }
        if (!isBeforeDeadline()) {
            throw new IllegalArgumentException("deadline has passed");
        }
        if (isClosedDay(availability.getDay())) {
            throw new IllegalArgumentException("cannot submit availability for a closed day");
        }
        employee.addAvailability(availability);
        try {
            this.employeeDAO.update(mapToEmployeeDTO(employee));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeAssignment(String id, Shift shift) {
        if (id == null || shift == null)
            throw new IllegalArgumentException("Invalid input");
        ShiftAssignment assignment = findAssignment(id, shift);
        if (assignment == null) {
            throw new IllegalArgumentException("Employee is not assigned to this shift.");
        }
        if (shift.getShiftManager() != null && shift.getShiftManager().getId().equals(id)) {
            shift.setDesignatedManager(null);

            try {
                this.shiftDAO.update(mapToShiftDTO(shift));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        assignments.remove(assignment);
        try {
            this.shiftAssignmentDAO.deleteAssignment(id, shift.getDay(), shift.getShiftType());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isBeforeDeadline() {
        if (deadLine == null) {
            return true;
        }
        Date now = new Date();
        return now.before(deadLine);
    }

    public void addClosedDay(String day) {
        if (day == null) {
            throw new IllegalArgumentException("invalid input");
        }
        String normalizedDay = day.trim().toLowerCase();
        if (isClosedDay(normalizedDay))
            throw new IllegalArgumentException("day already exists");
        closedDays.add(day);
    }

    private boolean isClosedDay(String day) {
        if (day == null)
            return false;

        String normalizedDay = day.trim().toLowerCase();
        for (String closedDay : closedDays) {
            if (closedDay.equalsIgnoreCase(normalizedDay)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDriverAssignedToShift(Driver driver, Shift shift) {
        if (driver == null || shift == null) {
            throw new IllegalArgumentException("invalid input");
        }
        if (!deliveryService.hasDeliveryInShift(shift)) {
            return true;
        }
        String requiredLicense = deliveryService.getTruckLicenseForDelivery(shift);
        if (requiredLicense != null && !requiredLicense.isEmpty() && !driver.hasLicenseFor(requiredLicense)) {
            return false;
        }

        loadAssignmentsFromDB();
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getEmployee().equals(driver) && assignment.getShift().equals(shift)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAssignedDriverForDelivery(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("shift cannot be null");
        }
        if (!deliveryService.hasDeliveryInShift(shift))
            return true;

        String driverId = deliveryService.getDriverIdForDelivery(shift);
        if (driverId == null || driverId.isEmpty()) {
            return false;
        }

        Employee employee = searchEmployee(driverId);
        if (!(employee instanceof Driver)) {
            return false;
        }

        Driver driver = (Driver) employee;
        return isDriverAssignedToShift(driver, shift);
    }

    public boolean isWarehouseAssignedToShift(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("shift cannot be null");
        }
        if (!deliveryService.hasDeliveryInShift(shift)) {
            return true;
        }

        loadAssignmentsFromDB();
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getShift().equals(shift)
                    && assignment.getRole().getRoleName().equalsIgnoreCase("warehouse")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDeliveryInShift(Shift shift) {
        return this.deliveryService.hasDeliveryInShift(shift);
    }

    private EmployeeDTO mapToEmployeeDTO(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = emp.getId();
        dto.name = emp.getName();
        dto.bankAccount = emp.getBankAccount();
        dto.active = emp.isActive() ? 1 : 0;
        dto.isManager = (emp instanceof ShiftManager) ? 1 : 0;
        dto.branchId = emp.getBranch() != null ? emp.getBranch().getBranchId() : null;
        dto.employmentType = emp.getEmploymentConditions().getEmploymentType();
        dto.salaryType = emp.getEmploymentConditions().getSalaryType();
        dto.salary = emp.getEmploymentConditions().getSalary();
        dto.startDate = emp.getEmploymentConditions().getStartDate() != null
                ? emp.getEmploymentConditions().getStartDate().getTime()
                : 0;
        dto.vacationDays = emp.getEmploymentConditions().getVacationDays();
        dto.licenseType = (emp instanceof Driver) ? ((Driver) emp).getLicenseType() : null;

        for (Role role : emp.getRoles()) {
            dto.roles.add(role.getRoleName());
        }
        for (Availability availability : emp.getAvailabilities()) {
            dto.availabilities
                    .add(new EmployeeDTO.AvailabilityInfo(availability.getDay(), availability.getShiftType()));
        }
        return dto;
    }

    private ShiftDTO mapToShiftDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.day = shift.getDay();
        dto.shiftType = shift.getShiftType();
        dto.managerId = shift.getShiftManager() != null ? shift.getShiftManager().getId() : null;
        dto.branchId = shift.getBranch() != null ? shift.getBranch().getBranchId() : null;

        for (RoleRequirement roleRequirement : shift.getRoleRequirements()) {
            dto.requirementRoles.add(roleRequirement.getRole().getRoleName());
            dto.requirementAmounts.add(roleRequirement.getAmount());
        }
        return dto;
    }

    private Employee buildEmployeeFromDTO(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }
        Date startDate = new Date(dto.startDate);
        EmploymentConditions cond = new EmploymentConditions(
                dto.employmentType, dto.salaryType, dto.salary, startDate, dto.vacationDays);

        Employee employee;
        if (dto.licenseType != null && !dto.licenseType.isEmpty()) {
            employee = new Driver(dto.name, dto.id, dto.bankAccount, cond, dto.licenseType);
        } else if (dto.isManager == 1) {
            employee = new ShiftManager(dto.name, dto.id, dto.bankAccount, cond);
        } else {
            employee = new Employee(dto.name, dto.id, dto.bankAccount, cond);
        }
        employee.setActive(dto.active == 1);
        if (dto.branchId != null) {
            employee.setBranch(new Branch(dto.branchId));
        }

        for (String roleName : dto.roles) {
            employee.addRole(new Role(roleName));
        }
        for (EmployeeDTO.AvailabilityInfo av : dto.availabilities) {
            employee.addAvailability(new Availability(av.day, av.shiftType));
        }
        return employee;
    }

    private Shift buildShiftFromDTO(ShiftDTO dto) {
        if (dto == null) {
            return null;
        }
        Shift shift = new Shift(dto.shiftType, dto.day);
        if (dto.managerId != null) {
            Employee mgr = searchEmployee(dto.managerId);
            if (mgr instanceof ShiftManager) {
                shift.setDesignatedManager((ShiftManager) mgr);
            }
        }
        if (dto.branchId != null) {
            shift.setBranch(new Branch(dto.branchId));
        }
        for (int i = 0; i < dto.requirementRoles.size(); i++) {
            shift.addRoleRequirement(new Role(dto.requirementRoles.get(i)), dto.requirementAmounts.get(i));
        }
        return shift;
    }

    public void updateLicenseType(String id, String licenseType) {
        if (id == null || licenseType == null || licenseType.trim().isEmpty()) {
            throw new IllegalArgumentException("invalid input");
        }
        try {
            EmployeeDTO dto = this.employeeDAO.findById(id);
            if (dto == null) {
                throw new IllegalArgumentException("employee does not exist");
            }

            boolean hasDriverRole = false;

            for (String roleName : dto.roles) {
                if (roleName.equalsIgnoreCase("Driver")) {
                    hasDriverRole = true;
                    break;
                }
            }
            if (!hasDriverRole && (dto.licenseType == null || dto.licenseType.trim().isEmpty()))
                throw new IllegalArgumentException("employee is not assigned to driver role");

            dto.licenseType = licenseType.trim();
            this.employeeDAO.update(dto);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void resetSystem() {
        try {
            dataAccess.Database.DataBase.clearAllTables();

            this.shifts.clear();
            this.assignments.clear();
        }
        catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}