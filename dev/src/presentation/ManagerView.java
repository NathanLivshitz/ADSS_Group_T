package presentation;

import domain.*;
import static presentation.InputValidation.*;
import java.util.List;
import java.util.Scanner;

public class ManagerView {
    private Manager manager;
    private final String password = "aadoss";

    public ManagerView(Manager manager) {
        this.manager = manager;
    }

    public void showMenu(Scanner scanner) {
        System.out.println("\nEnter password: ");
        String inputPassword = scanner.nextLine();
        while (!inputPassword.equals(this.password)){
            System.out.println("Incorrect password. Try again:");
            inputPassword = scanner.nextLine();
        }
        System.out.println("Access Granted. Welcome to the HR Management Menu!");
        boolean managerMenu = true;
        while (managerMenu) {
            System.out.println("\nSelect action:");
            System.out.println("1. Employee management");
            System.out.println("2. Shift & assignment management");
            System.out.println("3. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    manageEmployeesMenu(scanner);
                    break;
                case "2":
                    manageShiftsMenu(scanner);
                    break;
                case "3":
                    managerMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection, try again.");
            }
        }
    }

    private void manageEmployeesMenu(Scanner scanner) {
        boolean employeesMenu = true;
        while (employeesMenu) {
            System.out.println("\nEmployee management:");
            System.out.println("1. Add new employee");
            System.out.println("2. Update employee details");
            System.out.println("3. View employees details");
            System.out.println("4. Fire employee");
            System.out.println("5. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    addNewEmployee(scanner);
                    break;
                case "2":
                    updateEmployee(scanner);
                    break;
                case "3":
                    showEmployeesDetails(scanner);
                    break;
                case "4":
                    fireEmployee(scanner);
                    break;
                case "5":
                    employeesMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection, try again.");
            }
        }
    }

    private void addNewEmployee(Scanner scanner) {
        System.out.println("\nAdd new employee:");
        boolean success = false;

        while (!success) {
            try {
                System.out.print("Employee name: ");
                String name = scanner.nextLine();

                System.out.print("ID: ");
                String id = getValidNumericString(scanner);

                System.out.print("Bank account number: ");
                String bankAccount = getValidNumericString(scanner);

                System.out.print("Employment type (Full-time / Part-time):");
                String employmentType = getValidEmploymentType(scanner);

                System.out.print("Salary type (Hourly / Global): ");
                String salaryType = getValidSalaryType(scanner);

                System.out.print("Salary amount: ");
                double salary = getValidDouble(scanner);

                System.out.print("Vacation days: ");
                int vacationDays = getValidInt(scanner);

                EmploymentConditions conditions = new EmploymentConditions(employmentType, salaryType, salary,
                        new java.util.Date(), vacationDays);

                Employee newEmployee = new Employee(name, id, bankAccount, conditions);
                manager.addEmployee(newEmployee);

                System.out.println("Employee " + name + " added successfully");
                success = true;
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void updateEmployee(Scanner scanner) {
        System.out.print("\nEnter ID of the employee to update: ");
        String id = scanner.nextLine();
        Employee employee = manager.searchEmployee(id);

        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }

        boolean editing = true;
        while (editing) {
            System.out.println("\nSelect what to update for " + employee.getName() + ":");
            System.out.println("1. Update bank account");
            System.out.println("2. Update employment conditions");
            System.out.println("3. Update employee roles");
            System.out.println("4. Grant/revoke shift manager certification");
            System.out.println("5. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.print("New bank account: ");
                    String newBankAccount = getValidNumericString(scanner);
                    manager.updateBankAccount(id, newBankAccount);
                    System.out.println("Bank account updated.");
                    break;
                case "2":
                    updateEmploymentConditions(scanner, id);
                    break;
                case "3":
                    updateEmployeeRoles(scanner, id);
                    break;
                case "4":
                    updateShiftManagementStatus(scanner, id);
                    break;
                case "5":
                    editing = false;
                    break;
                default:
                    System.out.println("Invalid selection, try again.");
            }
        }
    }

    private void updateEmploymentConditions(Scanner scanner, String id) {
        Employee employee = manager.searchEmployee(id);

        boolean editing = true;
        while (editing) {
            System.out.println("\nWhat would you like to update for " + employee.getName() + "?");
            System.out.println("1. Employment type");
            System.out.println("2. Salary type");
            System.out.println("3. Salary amount");
            System.out.println("4. Vacation days");
            System.out.println("5. Cancel and go back");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1":
                        System.out.print("New employment type (Full-time / Part-time): ");
                        String newEmploymentType = getValidEmploymentType(scanner);
                        manager.updateEmployeeEmploymentType(id, newEmploymentType);
                        System.out.println("Updated successfully.");
                        break;
                    case "2":
                        System.out.print("New salary type (Hourly / Global): ");
                        String newSalaryType = getValidSalaryType(scanner);
                        manager.updateEmployeeSalaryType(id, newSalaryType);
                        System.out.println("Updated successfully.");
                        break;
                    case "3":
                        System.out.print("New salary amount: ");
                        double newSalary = getValidDouble(scanner);
                        manager.updateEmployeeSalary(id, newSalary);
                        System.out.println("Updated successfully.");
                        break;
                    case "4":
                        System.out.print("New vacation days: ");
                        int newVacationDays = getValidInt(scanner);
                        manager.updateEmployeeVacationDays(id, newVacationDays);
                        System.out.println("Updated successfully");
                        break;
                    case "5":
                        editing = false;
                        break;
                    default:
                        System.out.println("Invalid selection, try again.");
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input");
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private void updateEmployeeRoles(Scanner scanner, String id) {
        boolean editingRoles = true;
        while (editingRoles) {
            System.out.println("\n1. Add role to employee");
            System.out.println("2. Remove role from employee");
            System.out.println("3. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    addRoleToEmployee(scanner, id);
                    break;
                case "2":
                    removeRoleFromEmployee(scanner, id);
                    break;
                case "3":
                    editingRoles = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    private void addRoleToEmployee(Scanner scanner, String id) {
        System.out.println("Enter role name to add:");
        String roleName = scanner.nextLine();
        Role role = manager.searchRole(roleName);

        if (role == null) {
            System.out.println("Role '" + roleName + "' is not defined in the store.");
            return;
        }
        try {
            manager.addRoleToEmployee(role, id);
            System.out.println("Role added successfully");
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void removeRoleFromEmployee(Scanner scanner, String id) {
        System.out.println("Enter role name to remove: ");
        String roleName = scanner.nextLine();
        Role role = manager.searchRole(roleName);

        if (role == null) {
            System.out.println("Role not exist");
            return;
        }
        try {
            manager.removeRoleToEmployee(role, id);
            System.out.println("Role removed successfully");
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateShiftManagementStatus(Scanner scanner, String id) {
        Employee employee = manager.searchEmployee(id);
        boolean isShiftManager = employee.isShiftManager();
        System.out.println("The current status is:" + isShiftManager);
        System.out.println("Would you like to change it?");
        System.out.println("1. Yes");
        System.out.println("2. No");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            manager.updateShiftManagerStatus(employee.getId(), !isShiftManager);
            System.out.println("Status changed successfully");
        }
        else {
            System.out.println("Status didn't change");
        }
    }

    public void showEmployeesDetails(Scanner scanner) {
        boolean view = true;
        while (view) {
            System.out.println("\n1. View specific employee details");
            System.out.println("2. View all employees details");
            System.out.println("3. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Enter employee ID");
                    String id = scanner.nextLine();
                    showEmployeeDetails(id);
                    break;
                case "2":
                    List<Employee> allEmployees = manager.getEmployees();
                    if (allEmployees.isEmpty()) {
                        System.out.println("No employees in the system.");
                    }
                    else {
                        for (Employee employee : allEmployees) {
                            showEmployeeDetails(employee.getId());
                            System.out.println("---------------------------");
                        }
                    }
                    break;
                case "3":
                    view = false;
                    break;
            }
        }
    }

    private void showEmployeeDetails(String id) {
        Employee employee = manager.searchEmployee(id);
        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }
        System.out.println("\n--- Employee Details ---");
        System.out.println("Name: " + employee.getName());
        System.out.println("ID: " + employee.getId());
        System.out.println("Bank account: " + employee.getBankAccount());
        System.out.println("Employment type: " + employee.getEmploymentConditions().getEmploymentType());
        System.out.println("Salary type: " + employee.getEmploymentConditions().getSalaryType());
        System.out.println("Salary: " + employee.getEmploymentConditions().getSalary());
        System.out.println("Vacation days: " + employee.getEmploymentConditions().getVacationDays());
        System.out.println("Is shift manager: " + employee.isShiftManager());
        System.out.println("Roles: " + employee.getRoles());
    }

    private void fireEmployee(Scanner scanner) {
        System.out.print("Enter the ID of the employee you want to fire: ");
        String id = scanner.nextLine();
        try {
            manager.removeEmployee(id);
            System.out.println("Employee with ID " + id + " is fired");
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void manageShiftsMenu(Scanner scanner) {
        boolean shiftsMenu = true;
        while (shiftsMenu) {
            System.out.println("\n--- Shift & assignment management ---");
            System.out.println("1. Shift schedule (create, view history)");
            System.out.println("2. Assignment (assign, edit, remove, replace manager)");
            System.out.println("3. System settings (closed days, weekly deadline)");
            System.out.println("4. View employee availability");
            System.out.println("5. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    shiftScheduleMenu(scanner);
                    break;
                case "2":
                    assignmentsMenu(scanner);
                    break;
                case "3":
                    systemSettingsMenu(scanner);
                    break;
                case "4":
                    EmployeesAvailability(scanner);
                    break;
                case "5":
                    shiftsMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    private void shiftScheduleMenu(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n-- Shift schedule --");
            System.out.println("1. Create new shift");
            System.out.println("2. View shift history");
            System.out.println("3. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    createNewShift(scanner);
                    break;
                case "2":
                    showShiftHistory(scanner);
                    break;
                case "3":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    private void assignmentsMenu(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n-- Assignment Management --");
            System.out.println("1. Assign employee to Shift");
            System.out.println("2. Replace shift manager");
            System.out.println("3. Edit existing assignment");
            System.out.println("4. Remove employee from shift");
            System.out.println("5. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    assignEmployeeToShift(scanner);
                    break;
                case "2":
                    replaceShiftManagerUI(scanner);
                    break;
                case "3":
                    editAssignment(scanner);
                    break;
                case "4":
                    removeEmployeeFromShift(scanner);
                    break;
                case "5":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    private void assignEmployeeToShift(Scanner scanner) {
        System.out.println("\n--- Assign Employee to Shift ---");
        System.out.print("Shift day: ");
        String day = getValidShiftDay(scanner);

        System.out.print("Shift type (Morning / Evening / Double): ");
        String type = getValidShiftType(scanner).toLowerCase();

        if (type.equals("double")) {
            Shift morningShift = manager.searchShift(day, "morning");
            Shift eveningShift = manager.searchShift(day, "evening");
            if (morningShift == null || eveningShift == null) {
                System.out.println("Both morning and evening shifts must exist.");
                return;
            }
            handleShiftAssignment(morningShift, scanner);
            handleShiftAssignment(eveningShift, scanner);
        }
        else {
            Shift shift = manager.searchShift(day, type);
            if (shift == null) {
                System.out.println("Shift not found");
                return;
            }
            handleShiftAssignment(shift, scanner);
        }
    }

    private void handleShiftAssignment(Shift shift, Scanner scanner) {
        System.out.println("\n========================================");
        System.out.println("  CURRENT SHIFT: " + shift.getDay().toUpperCase() + " | " + shift.getShiftType().toUpperCase());
        System.out.println("========================================");

        boolean needsManager = (shift.getShiftManager() == null);
        displayShiftStatus(shift, needsManager);

        if (!needsManager && isShiftFull(shift)) {
            System.out.println("Shift is completely full! No further assignments needed.");
            return;
        }

        System.out.print("\nEnter Employee ID to assign: ");
        String id = getValidNumericString(scanner);

        Employee employee = manager.searchEmployee(id);
        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }
        if (needsManager && !employee.isShiftManager()) {
            System.out.println("The first person assigned MUST be a shift manager.");
            return;
        }

        Role selectedRole = selectRoleForShift(shift, scanner);
        if (selectedRole == null)
            return;
        handleAssignmentWithOverride(employee, selectedRole, shift, needsManager, scanner);
    }

    private void displayShiftStatus(Shift shift, boolean needsManager) {
        if (needsManager) {
            System.out.println("MANAGER REQUIRED: This shift has no Shift Manager.");
            List<Employee> availableManagers = manager.getAvailableManagersForShift(shift);
            if (availableManagers.isEmpty())
                System.out.println("Notice: No managers are available. You can override and force-assign any certified manager.");
            else {
                System.out.println("Available Shift Managers:");
                for (Employee employee : availableManagers)
                    System.out.println("- " + employee.getName() + " (ID: " + employee.getId() + ")");
            }
        }
        else {
            System.out.println("Shift Manager: " + shift.getShiftManager().getName());
            System.out.println("--- Remaining Roles ---");
            for (Role role : manager.getStoreRoles()) {
                int remainingSpots = manager.getRemainingSpotsForRole(shift, role);
                if (remainingSpots > 0)
                    System.out.println("- " + role.getRoleName() + ": " + remainingSpots + " spots left");
            }
            List<Employee> availableEmployees = manager.getAvailableEmployeesForShift(shift);
            System.out.println("\n--- Available Employees ---");

            if (availableEmployees.isEmpty())
                System.out.println("No available employees found");
            else {
                for (Employee employee : availableEmployees)
                    System.out.println("- " + employee.getName() + " (ID: " + employee.getId() + ")");
            }
        }
    }

    private Role selectRoleForShift(Shift shift, Scanner scanner) {
        System.out.println("Select Role:");
        List<Role> roles = manager.getStoreRoles();
        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            int remainingSpots = manager.getRemainingSpotsForRole(shift, role);
            System.out.println((i + 1) + ". " + role.getRoleName() + " (" + remainingSpots + " spots left)");
        }
        int choice = getValidInt(scanner);
        if (choice <= 0 || choice > roles.size()) {
            System.out.println("Invalid choice");
            return null;
        }
        return roles.get(choice - 1);
    }

    private void handleAssignmentWithOverride(Employee emp, Role role, Shift shift, boolean makeManager, Scanner scanner) {
        boolean isOverride = false;
        if (!manager.isEmployeeAvailableForShift(emp, shift)) {
            System.out.println("Warning: Employee is NOT available for this shift.");
            System.out.println("Do you want to perform an OVERRIDE assignment?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            if (!scanner.nextLine().equals("1")) {
                System.out.println("Assignment cancelled");
                return;
            }
            isOverride = true;
        }
        try {
            if (makeManager) {
                manager.assignShiftManagerToShift(emp.getId(), role, shift, isOverride);
                System.out.println("Shift Manager assigned successfully!");
            }
            else {
                manager.assignEmployeeToShift(emp.getId(), role, shift, isOverride);
                System.out.println("Employee assigned successfully!");
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isShiftFull(Shift shift) {
        for (Role role : manager.getStoreRoles())
            if (manager.getRemainingSpotsForRole(shift, role) > 0)
                return false;
        return true;
    }

    private void removeEmployeeFromShift(Scanner scanner) {
        System.out.println("\n--- Remove Employee from Shift ---");
        System.out.print("Enter employee ID: ");
        String id = getValidNumericString(scanner);
        System.out.print("Enter shift day: ");
        String day = getValidShiftDay(scanner);
        System.out.print("Enter shift type (Morning / Evening / Double): ");
        String type = getValidShiftType(scanner).toLowerCase();

        try {
            if (type.equals("double")) {
                Shift morningShift = manager.searchShift(day, "morning");
                Shift eveningShift = manager.searchShift(day, "evening");

                boolean removed = false;
                if (morningShift != null) {
                    manager.removeAssignment(id, morningShift);
                    removed = true;
                }
                if (eveningShift != null) {
                    manager.removeAssignment(id, eveningShift);
                    removed = true;
                }

                if (removed) {
                    System.out.println("Employee removed from Double shift (Morning & Evening).");
                } else {
                    System.out.println("Shifts not found.");
                }
            } else {
                Shift shift = manager.searchShift(day, type);
                if (shift == null) {
                    System.out.println("Shift not found.");
                    return;
                }
                manager.removeAssignment(id, shift);
                System.out.println("Employee removed from the shift.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to remove: " + e.getMessage());
        }
    }

    private void systemSettingsMenu(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n-- System Settings --");
            System.out.println("1. Set weekly deadline");
            System.out.println("2. Add closed day");
            System.out.println("0. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    setDeadline(scanner);
                    break;
                case "2":
                    addClosedDay(scanner);
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    private void createNewShift(Scanner scanner) {
        System.out.println("\nCreate new shift");
        System.out.print("Day: ");
        String day = getValidShiftDay(scanner);
        System.out.print("Type (Morning / Evening / Double): ");
        String type = getValidShiftType(scanner).toLowerCase();

        try {
            if (type.equals("double")) {
                if (manager.searchShift(day, "morning") != null || manager.searchShift(day, "evening") != null) {
                    System.out.println("Error: One or both shifts already exist for this day.");
                    return;
                }
                Shift morning = new Shift("morning", day);
                Shift evening = new Shift("evening", day);
                List<Role> roles = manager.getStoreRoles();

                for (Role role : roles) {
                    morning.addRoleRequirement(role, 1);
                    evening.addRoleRequirement(role, 1);
                }

                boolean addingRoles = true;
                while (addingRoles) {
                    System.out.println("Select a role to update its required number:");
                    System.out.println("0. Finish and save shift");
                    for (int i = 0; i < roles.size(); i++) {
                        System.out.println((i + 1) + ". " + roles.get(i).getRoleName());
                    }
                    int choice = getValidInt(scanner);
                    if (choice == 0) {
                        addingRoles = false;
                    }
                    else
                        if (choice > 0 && choice <= roles.size()) {
                            Role selectedRole = roles.get(choice - 1);
                            System.out.print("Enter required number of employees for role '" + selectedRole.getRoleName() + "': ");
                            int count = getValidInt(scanner);

                            morning.addRoleRequirement(selectedRole, count);
                            evening.addRoleRequirement(selectedRole, count);
                            System.out.println("Requirement added");
                        }
                    else {
                        System.out.println("Invalid selection");
                    }
                }
                manager.addShift(morning);
                manager.addShift(evening);
                System.out.println("Double shift created and saved successfully! (Morning & Evening)");

            }
            else {
                if (manager.searchShift(day, type) != null) {
                    System.out.println("Error: Shift already exists for this day.");
                    return;
                }
                Shift newShift = new Shift(type, day);
                List<Role> roles = manager.getStoreRoles();

                for (Role role : roles) {
                    newShift.addRoleRequirement(role, 1);
                }
                boolean addingRoles = true;
                while (addingRoles) {
                    System.out.println("Select a role to update its required number:");
                    System.out.println("0. Finish and save shift");
                    for (int i = 0; i < roles.size(); i++) {
                        System.out.println((i + 1) + ". " + roles.get(i).getRoleName());
                    }
                    int choice = getValidInt(scanner);
                    if (choice == 0) {
                        addingRoles = false;
                    } else if (choice > 0 && choice <= roles.size()) {
                        Role selectedRole = roles.get(choice - 1);
                        System.out.print("Enter required number of employees for role '" + selectedRole.getRoleName() + "': ");
                        int count = getValidInt(scanner);

                        newShift.addRoleRequirement(selectedRole, count);
                        System.out.println("Requirement added");
                    } else {
                        System.out.println("Invalid selection");
                    }
                }
                manager.addShift(newShift);
                System.out.println("Shift created and saved successfully!");
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void replaceShiftManagerUI(Scanner scanner) {
        System.out.println("\n--- Replace Shift Manager ---");
        System.out.print("Shift day: ");
        String day = getValidShiftDay(scanner);
        System.out.print("Shift type (Morning / Evening): ");
        String type = getValidShiftType(scanner).toLowerCase();

        if (type.equals("double")) {
            System.out.println("Error: Edit morning and evening shifts separately.");
            return;
        }
        Shift shift = manager.searchShift(day, type);
        if (shift == null) {
            System.out.println("Shift not found");
            return;
        }
        List<Employee> availableManagers = manager.getAvailableManagersForShift(shift);
        if (availableManagers.isEmpty()) {
            System.out.println("No available managers found for this shift.");
            return;
        }
        System.out.println("Available Shift Managers:");
        for (Employee employee : availableManagers) {
            System.out.println("- " + employee.getName() + " (ID: " + employee.getId() + ")");
        }

        System.out.print("Enter Employee ID to set as NEW shift manager: ");
        String id = scanner.nextLine();

        Employee emp = manager.searchEmployee(id);
        if (emp == null || !emp.isShiftManager()) {
            System.out.println("Employee not found or is not a shift manager.");
            return;
        }

        System.out.println("Select Role for the Manager:");
        List<Role> roles = manager.getStoreRoles();
        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            int rem = manager.getRemainingSpotsForRole(shift, role);
            System.out.println((i + 1) + ". " + role.getRoleName() + " (" + rem + " spots left)");
        }
        int choice = getValidInt(scanner);
        if (choice <= 0 || choice > roles.size()) {
            System.out.println("Invalid choice");
            return;
        }
        Role selectedRole = roles.get(choice - 1);

        boolean isOverride = false;
        if (!manager.isEmployeeAvailableForShift(emp, shift)) {
            System.out.println("Warning: Manager did NOT submit availability for this shift.");
            System.out.println("Do you want to perform an OVERRIDE assignment? (1. Yes / 2. No)");
            if (!scanner.nextLine().equals("1")) {
                System.out.println("Assignment cancelled");
                return;
            }
            isOverride = true;
        }
        try {
            if (shift.getShiftManager() != null) {
                manager.removeAssignment(shift.getShiftManager().getId(), shift);
            }
            manager.assignShiftManagerToShift(id, selectedRole, shift, isOverride);
            System.out.println("New Shift Manager selected and assigned successfully.");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Replacement failed: " + e.getMessage());
        }
    }

    private void showShiftHistory(Scanner scanner) {
        System.out.println("\nShift History:");
        List<Shift> allShifts = manager.getShifts();

        if (allShifts.isEmpty()) {
            System.out.println("No shifts have been defined in the system yet");
            return;
        }
        for (Shift shift : allShifts) {
            System.out.println("\n------------------------------------------------");
            System.out.println("\n     -----Shift-----");
            System.out.println("Day: " + shift.getDay() + ", Shift Type: " + shift.getShiftType());

            Employee shiftManager = shift.getShiftManager();
            if (shiftManager != null)
                System.out.println("Shift Manager: " + shiftManager.getName());
            else
                System.out.println("Shift manager wasn't selected yet");
            System.out.println("Assignments:");
            boolean Assignments = false;
            for (ShiftAssignment shiftAssignment : manager.getAssignments()) {
                if (shiftAssignment.getShift().equals(shift)) {
                    System.out.println(shiftAssignment.getRole().getRoleName() + ": " + shiftAssignment.getEmployee().getName()+ " (ID: " + shiftAssignment.getEmployee().getId() + ")");
                    Assignments = true;
                }
            }
            if (!Assignments) {
                System.out.println("No employees assigned to this shift");
            }
        }
    }

    private void EmployeesAvailability(Scanner scanner) {
        System.out.print("Enter employee ID to view availability: ");
        String id = scanner.nextLine();
        Employee employee = manager.searchEmployee(id);

        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }
        List<Availability> availabilities = employee.getAvailabilities();
        if (availabilities.isEmpty()) {
            System.out.println("No availability submitted for " + employee.getName());
        }
        else {
            System.out.println("Availability for " + employee.getName() + ":");
            for (Availability availability : availabilities) {
                System.out.println("- Day: " + availability.getDay() + " | Shift: " + availability.getShiftType());
            }
        }
    }

    private void setDeadline(Scanner scanner) {
        System.out.println("Set Weekly Deadline:");
        System.out.print("Enter deadline date (Format: yyyy-MM-dd): ");
        String dateString = scanner.nextLine();

        try {
            java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date deadlineDate = date.parse(dateString);

            manager.updateDeadline(deadlineDate);
            System.out.println("Deadline updated successfully to: " + dateString);
        }
        catch (java.text.ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd");
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void editAssignment(Scanner scanner) {
        System.out.println("\n--- Edit Existing Assignment ---");
        System.out.print("Enter employee ID: ");
        String id = getValidNumericString(scanner);
        Employee employee = manager.searchEmployee(id);

        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }
        List<ShiftAssignment> empAssignments = new java.util.ArrayList<>();
        for (ShiftAssignment shiftAssignment : manager.getAssignments()) {
            if (shiftAssignment.getEmployee().getId().equals(id)) {
                empAssignments.add(shiftAssignment);
            }
        }
        if (empAssignments.isEmpty()) {
            System.out.println("This employee has no assignments to edit");
            return;
        }
        System.out.println("Select an assignment to edit for " + employee.getName() + ":");
        for (int i = 0; i < empAssignments.size(); i++) {
            ShiftAssignment shiftAssignment = empAssignments.get(i);
            System.out.println((i + 1) + ". Day: " + shiftAssignment.getShift().getDay() + " | Shift: " + shiftAssignment.getShift().getShiftType() + " | Role: " + shiftAssignment.getRole().getRoleName());
        }
        System.out.println("0. Cancel");

        int choice = getValidInt(scanner);
        if (choice == 0) return;
        if (choice < 1 || choice > empAssignments.size()) {
            System.out.println("Invalid selection");
            return;
        }

        ShiftAssignment selectedAssignment = empAssignments.get(choice - 1);
        Shift oldShift = selectedAssignment.getShift();

        System.out.println("What would you like to change?");
        System.out.println("1. Change Role in current shift");
        System.out.println("2. Move employee to a different shift");
        String action = scanner.nextLine();

        try {
            if (action.equals("1")) {
                System.out.println("Select New Role:");
                List<Role> roles = manager.getStoreRoles();
                for (int i = 0; i < roles.size(); i++) {
                    Role role = roles.get(i);
                    int rem = manager.getRemainingSpotsForRole(oldShift, role);
                    System.out.println((i + 1) + ". " + role.getRoleName() + " (" + rem + " spots left)");
                }
                int roleChoice = getValidInt(scanner);
                if (roleChoice <= 0 || roleChoice > roles.size()) {
                    System.out.println("Invalid choice");
                    return;
                }
                manager.changeRoleToAssignment(id, oldShift, roles.get(roleChoice - 1));
                System.out.println("Role updated successfully");

            }
            else
                if (action.equals("2")) {
                    System.out.print("Enter NEW shift day: ");
                    String newDay = getValidShiftDay(scanner);
                    System.out.print("Enter NEW shift type (Morning / Evening): ");
                    String newType = getValidShiftType(scanner);

                    if (newType.equals("double")) {
                        System.out.println("Error: Move to morning or evening specifically.");
                        return;
                    }

                    Shift newShift = manager.searchShift(newDay, newType);
                    if (newShift == null) {
                        System.out.println("New shift does not exist. Please create it first.");
                        return;
                    }
                    manager.changeShiftToAssignment(id, oldShift, newShift);
                    System.out.println("Shift updated successfully");
                }
                else {
                    System.out.println("Invalid selection");
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addClosedDay(Scanner scanner) {
        System.out.print("Enter closed day: ");
        String day = getValidShiftDay(scanner);
        try {
            manager.addClosedDay(day);
            System.out.println("Closed day added successfully");
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}