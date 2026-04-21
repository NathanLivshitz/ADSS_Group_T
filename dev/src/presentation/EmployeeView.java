package presentation;

import domain.*;
import java.util.List;
import java.util.Scanner;
import static presentation.InputValidation.*;

public class EmployeeView {
    private Manager manager;

    public EmployeeView(Manager manager) {
        this.manager = manager;
    }

    public void showMenu(Scanner scanner) {
        System.out.println("Employee login page:");
        System.out.print("Enter id: ");
        String id = getValidNumericString(scanner);

        if (id == null || id.trim().isEmpty()) {
            System.out.println("Invalid ID");
            return;
        }
        if (id.equals("0"))
            return;

        Employee employee = manager.searchEmployee(id);
        if (employee == null || !employee.isActive()) {
            System.out.println("Employee not found or inactive.");
            return;
        }

        System.out.println("\nWelcome, " + employee.getName() + "!");
        boolean employeeMenu = true;
        while (employeeMenu) {
            System.out.println("1. My availabilities");
            System.out.println("2. View my shifts");
            System.out.println("3. View full shift board");
            System.out.println("4. Shift manager menu");
            System.out.println("5. Logout");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    availabilitiesMenu(scanner, id);
                    break;
                case "2":
                    viewMyShifts(employee);
                    break;
                case "3":
                    viewFullShiftBoard();
                    break;
                case "4":
                    if (employee.isShiftManager()) {
                        shiftManagerMenu(scanner, employee);
                    }
                    else
                        System.out.println("Invalid selection. Must be a shift manager to do that");
                    break;
                case "5":
                    System.out.println("Logging out...");
                    employeeMenu = false;
                    break;
                default:
                    System.out.println("Invalid selection, try again");
            }
        }
    }

    private void availabilitiesMenu(Scanner scanner, String id) {
        boolean inSubMenu = true;
        while (inSubMenu) {
            System.out.println("\nManage Availabilities:");
            System.out.println("1. Add new availability");
            System.out.println("2. Delete existing availability");
            System.out.println("3. Edit existing availability");
            System.out.println("4. View my availabilities");
            System.out.println("5. Back");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1":
                        addAvailability(scanner, id);
                        break;
                    case "2":
                        deleteAvailability(scanner, id);
                        break;
                    case "3":
                        editAvailability(scanner, id);
                        break;
                    case "4":
                        viewMyAvailabilities(id);
                        break;
                    case "5":
                        inSubMenu = false;
                        break;
                    default:
                        System.out.println("Invalid selection");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void addAvailability(Scanner scanner, String id) {
        System.out.print("Enter day: ");
        String day = getValidShiftDay(scanner);
        System.out.print("Enter shift type (Morning/Evening/Double): ");
        String type = getValidShiftType(scanner).toLowerCase();

        if (type.equals("double")) {
            manager.submitAvailability(id, new Availability(day, "morning"));
            manager.submitAvailability(id, new Availability(day, "evening"));
            System.out.println("Availability added for Double shift (Morning & Evening).");
        }
        else {
            manager.submitAvailability(id, new Availability(day, type));
            System.out.println("Availability added.");
        }
    }

    private void deleteAvailability(Scanner scanner, String id) {
        System.out.print("Enter day to delete: ");
        String day = getValidShiftDay(scanner);
        System.out.print("Enter shift type to delete: ");
        String type = getValidShiftType(scanner).toLowerCase();

        try {
            if (type.equals("double")) {
                manager.removeAvailability(id, new Availability(day, "morning"));
                manager.removeAvailability(id, new Availability(day, "evening"));
                System.out.println("Double availability removed (Morning & Evening).");
            }
            else {
                manager.removeAvailability(id, new Availability(day, type));
                System.out.println("Availability removed.");
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println("Failed to remove: " + e.getMessage());
        }
    }

    private void editAvailability(Scanner scanner, String id) {
        Employee employee = manager.searchEmployee(id);
        List<Availability> availabilities = employee.getAvailabilities();

        System.out.println("\n--- Edit Availability ---");
        if (availabilities.isEmpty()) {
            System.out.println("You have no availabilities to edit");
            return;
        }

        System.out.println("Select an availability to edit:");
        System.out.println("0. Cancel");
        for (int i = 0; i < availabilities.size(); i++) {
            Availability a = availabilities.get(i);
            System.out.println((i + 1) + ". Day: " + a.getDay() + " | Shift: " + a.getShiftType());
        }
        int choice = getValidInt(scanner);
        if (choice == 0)
            return;
        if (choice < 1 || choice > availabilities.size()) {
            System.out.println("Invalid selection");
            return;
        }
        Availability selected = availabilities.get(choice - 1);

        System.out.println("Enter new details:");
        System.out.print("New day: ");
        String newDay = getValidShiftDay(scanner);
        System.out.print("New type (Morning / Evening / Double): ");
        String newType = getValidShiftType(scanner).toLowerCase();

        try {
            if (newType.equals("double")) {
                manager.removeAvailability(id, selected);
                manager.submitAvailability(id, new Availability(newDay, "morning"));
                manager.submitAvailability(id, new Availability(newDay, "evening"));
                System.out.println("Availability updated to Double shift successfully.");
            }
            else {
                manager.editAvailability(id, selected, new Availability(newDay, newType));
                System.out.println("Availability updated successfully");
            }
        }
        catch (IllegalArgumentException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    private void viewMyAvailabilities(String id) {
        Employee employee = manager.searchEmployee(id);
        List<Availability> availabilitiesList = employee.getAvailabilities();

        System.out.println("\nMy availabilities:");
        if (availabilitiesList.isEmpty()) {
            System.out.println("No availabilities submitted yet");
            return;
        }

        java.util.List<String> printedDays = new java.util.ArrayList<>();

        for (Availability a : availabilitiesList) {
            String day = a.getDay();
            if (printedDays.contains(day)) continue;

            boolean hasMorning = false;
            boolean hasEvening = false;

            for (Availability check : availabilitiesList) {
                if (check.getDay().equals(day)) {
                    if (check.getShiftType().equals("morning")) hasMorning = true;
                    if (check.getShiftType().equals("evening")) hasEvening = true;
                }
            }

            if (hasMorning && hasEvening) {
                System.out.println("- Day: " + day + " | Shift: double");
            } else {
                System.out.println("- Day: " + day + " | Shift: " + a.getShiftType());
            }
            printedDays.add(day);
        }
    }

    private void viewMyShifts(Employee employee) {
        System.out.println("\nMy assigned shifts:\n");
        List<ShiftAssignment> assignments = manager.getAssignments();

        boolean found = false;
        java.util.List<String> printedDays = new java.util.ArrayList<>();

        for (ShiftAssignment sa : assignments) {
            if (sa.getEmployee().getId().equals(employee.getId())) {
                found = true;
                String day = sa.getShift().getDay();
                if (printedDays.contains(day)) continue;

                boolean hasMorning = false;
                boolean hasEvening = false;
                String roleName = "";

                for (ShiftAssignment check : assignments) {
                    if (check.getEmployee().getId().equals(employee.getId()) && check.getShift().getDay().equals(day)) {
                        if (check.getShift().getShiftType().equals("morning")) {
                            hasMorning = true;
                            roleName = check.getRole().getRoleName();
                        }
                        if (check.getShift().getShiftType().equals("evening")) {
                            hasEvening = true;
                            if (roleName.isEmpty()) roleName = check.getRole().getRoleName();
                        }
                    }
                }

                if (hasMorning && hasEvening) {
                    System.out.println("Day: " + day + " | Type: double | Role: " + roleName);
                } else {
                    System.out.println("Day: " + day + " | Type: " + sa.getShift().getShiftType() + " | Role: " + sa.getRole().getRoleName());
                }
                printedDays.add(day);
            }
        }
        if (!found) {
            System.out.println("No shifts assigned yet.");
        }
    }

    private void viewFullShiftBoard() {
        System.out.println("\nFull shift board");
        List<Shift> Shifts = manager.getShifts();

        if (Shifts.isEmpty()) {
            System.out.println("No shifts have been published yet.");
            return;
        }

        for (Shift s : Shifts) {
            System.out
                    .println("\nDay: " + s.getDay().toUpperCase() + " | Shift type: " + s.getShiftType().toUpperCase());

            Employee manager = s.getShiftManager();
            if (manager != null) {
                System.out.println("Shift Manager: " + manager.getName());
            }

            System.out.println("Team:");
            boolean hasAssignments = false;
            for (ShiftAssignment sa : this.manager.getAssignments()) {
                if (sa.getShift().equals(s)) {
                    System.out.println(" - " + sa.getEmployee().getName() + " (" + sa.getRole().getRoleName() + ")");
                    hasAssignments = true;
                }
            }
            if (!hasAssignments) {
                System.out.println("No employees assigned yet.");
            }
        }
        System.out.println("------------------------------\n");
    }

    private void shiftManagerMenu(Scanner scanner, Employee shiftManager) {

        boolean shiftManagerPage = true;
        while (shiftManagerPage) {
            System.out.println("Shift manager actions:");
            System.out.println("1. Swipe manager card");
            System.out.println("2. View employees details");
            System.out.println("3. Back");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Cancellation approved by shift manager: " + shiftManager.getName());
                    break;
                case "2":
                    showEmployeesDetailsSM(scanner);
                    break;
                case "3":
                    shiftManagerPage = false;
                    break;
                default:
                    System.out.println("Invalid selection, try again.");
            }
        }
    }

    private void showEmployeesDetailsSM(Scanner scanner) {
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
                    showEmployeeDetailsSM(id);
                    break;
                case "2":
                    System.out.println("\n--- Store employees details ---");
                    List<Employee> allEmployees = manager.getEmployees();

                    if (allEmployees.isEmpty()) {
                        System.out.println("No employees in the system.");
                        return;
                    }
                    for (Employee employee : allEmployees) {
                        showEmployeeDetailsSM(employee.getId());
                    }
                    break;
                case "3":
                    view = false;
                    break;
            }
        }
    }

    private void showEmployeeDetailsSM(String id) {
        Employee employee = manager.searchEmployee(id);
        if (employee == null) {
            System.out.println("Employee not found");
            return;
        }
        System.out.println("\n--- Employee Details ---");
        System.out.println("Name: " + employee.getName());
        System.out.println("ID: " + employee.getId());
        System.out.println("Is shift manager: " + employee.isShiftManager());
        System.out.println("Roles: " + employee.getRoles());
}

}
