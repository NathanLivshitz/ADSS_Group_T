package domain;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Manager {

    private List<Employee> employees;
    private List<Shift> shifts;
    private List<ShiftAssignment> assignments;
    private Date deadLine;
    private List<String> closedDays;
    private List<Role> storeRoles = new LinkedList<>(java.util.Arrays.asList(
            new Role("Cashier"),
            new Role("Warehouse")));

    public Manager() {
        this.employees = new LinkedList<>();
        this.shifts = new LinkedList<>();
        this.assignments = new LinkedList<>();
        this.deadLine = null;
        this.closedDays = new LinkedList<>();
    }

    public void addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (searchEmployee(employee.getId()) != null) {
            throw new IllegalArgumentException("Employee with ID: " + employee.getId() + " is already exists");
        }
        employees.add(employee);
    }

    public void removeEmployee(String id) {
        Employee employee = searchEmployee(id);
        if (employee == null) {
            throw new IllegalArgumentException("employee does not exist");
        }
        employee.setActive(false);
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
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public List<ShiftAssignment> getAssignments() {
        return assignments;
    }

    public List<Role> getStoreRoles() {
        return storeRoles;
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
        if (shifts.contains(shift)) {
            throw new IllegalArgumentException("Shift already exists");
        }
        shifts.add(shift);
    }

    public Shift searchShift(String day, String type) {
        for (Shift shift : shifts) {
            if (shift.getDay().equalsIgnoreCase(day) && shift.getShiftType().equalsIgnoreCase(type)) {
                return shift;
            }
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
        employee.setShiftManager(isManager);
    }

    public void updateEmployeeEmploymentType(String id, String employmentType) {
        if (id == null || employmentType == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setEmploymentType(employmentType);
    }

    public void updateEmployeeSalaryType(String id, String salaryType) {
        if (id == null || salaryType == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setSalaryType(salaryType);
    }

    public void updateEmployeeSalary(String id, double salary) {
        if (id == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setSalary(salary);
    }

    public void updateEmployeeVacationDays(String id, int vacationDays) {
        if (id == null)
            throw new IllegalArgumentException("invalid input");
        Employee employee = searchEmployee(id);
        if (employee == null)
            throw new IllegalArgumentException("employee does not exist");
        employee.getEmploymentConditions().setVacationDays(vacationDays);
    }

    public Employee searchEmployee(String id) {
        if (id != null) {
            for (Employee employee : employees) {
                if (employee.getId().equals(id)) {
                    return employee;
                }
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
    }

    private ShiftAssignment findAssignment(String id, Shift shift) {
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getEmployee().getId().equals(id) &&
                    assignment.getShift().equals(shift)) {
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
        }
        assignment.setShift(newShift);
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
    }

    private int countAssignmentsForRoleInShift(Role role, Shift shift) {
        int count = 0;
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getShift().equals(shift) &&
                    assignment.getRole().equals(role)) {
                count++;
            }
        }
        return count;
    }

    private boolean canAssignRoleToShift(Role role, Shift shift) {
        int required = shift.numOfRoles(role);
        int assigned = countAssignmentsForRoleInShift(role, shift);
        return required > assigned;
    }

    private boolean isEmployeeAlreadyAssignedToShift(Employee employee, Shift shift) {
        for (ShiftAssignment assignment : assignments) {
            if (assignment.getEmployee().equals(employee) &&
                    assignment.getShift().equals(shift)) {
                return true;
            }
        }
        return false;
    }
    public List<Employee> getAvailableEmployeesForShift(Shift shift) {
        List<Employee> available = new LinkedList<>();
        for (Employee employee : employees) {
            if (employee.isActive() && !isEmployeeAlreadyAssignedToShift(employee, shift)) {
                if (isEmployeeAvailableForShift(employee, shift)) {
                    available.add(employee);
                }
            }
        }
        return available;
    }

    public List<Employee> getAvailableManagersForShift(Shift shift) {
        List<Employee> available = new LinkedList<>();
        for (Employee employee : getAvailableEmployeesForShift(shift)) {
            if (employee.isShiftManager()) {
                available.add(employee);
            }
        }
        return available;
    }

    public int getRemainingSpotsForRole(Shift shift, Role role) {
        int required = shift.numOfRoles(role);
        int assigned = countAssignmentsForRoleInShift(role, shift);
        return required - assigned;
    }

    public void assignShiftManagerToShift(String id, Role role, Shift shift, boolean isOverride) {
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
        if (!employee.isShiftManager())
            throw new IllegalArgumentException("employee is not certified as a shift manager");
        if (isEmployeeAlreadyAssignedToShift(employee, shift))
            throw new IllegalArgumentException("employee is already assigned to this shift");
        if (!canAssignRoleToShift(role, shift))
            throw new IllegalArgumentException("role is not required or already full in this shift");
        if (!isOverride && !isEmployeeAvailableForShift(employee, shift)) {
            throw new IllegalArgumentException("employee can't work at this shift");
        }
        ShiftAssignment assignment = new ShiftAssignment(employee, shift, role);
        assignments.add(assignment);
        shift.setDesignatedManager(employee);
    }

    public boolean isEmployeeAvailableForShift(Employee employee, Shift shift) {
        if (employee.getAvailabilities().isEmpty()) {
            return true;
        }
        for (Availability availability : employee.getAvailabilities()) {
            if (availability.getDay().equalsIgnoreCase(shift.getDay()) &&
                    availability.getShiftType().equalsIgnoreCase(shift.getShiftType())) {
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
            throw new IllegalArgumentException("Cannot edit availability: Employee is already assigned to this shift. Please ask HR to remove your assignment first.");
        }
        if (!employee.getAvailabilities().remove(availability)) {
            throw new IllegalArgumentException("The original availability was not found.");
        }
        try {
            employee.addAvailability(newAvailability);
        }
        catch (IllegalArgumentException e) {
            employee.addAvailability(availability);
            throw e;
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
            throw new IllegalArgumentException("Cannot remove availability: Employee is already assigned to this shift. Please ask HR to remove your assignment first.");
        }
        employee.getAvailabilities().remove(availability);
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
        }
        assignments.remove(assignment);
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
        if (day == null) {
            return false;
        }

        String normalizedDay = day.trim().toLowerCase();
        for (String closedDay : closedDays) {
            if (closedDay.equalsIgnoreCase(normalizedDay)) {
                return true;
            }
        }
        return false;
    }
}
