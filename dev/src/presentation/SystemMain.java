package presentation;

import domain.*;
import dataAccess.Database.DataBase;

import Inventory.InventoryBootstrap;
import Inventory.Presentation.InventoryMenu;

import java.util.Scanner;

// Unified entry point for the whole Super-Lee system.
// Logs in by employee ID and routes by role: every employee gets the employee menu,
// shift managers also get the management menu, and warehouse workers (or managers) get
// the inventory menu. Both modules share one SQLite database (superLee.db).
public class SystemMain {

    public static void main(String[] args) {
        try {
            DataBase.getConnection();
        } catch (Exception e) {
            System.out.println("Employee database initialization failed: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);

        Manager employeeManager = new Manager();
        try {
            if (employeeManager.getEmployees().isEmpty()) {
                Main.initMockData(employeeManager);
            }
        } catch (Exception e) {
            System.out.println("Could not load employee sample data: " + e.getMessage());
        }
        ManagerView managerView = new ManagerView(employeeManager);
        EmployeeView employeeView = new EmployeeView(employeeManager);

        // inventory module wired against the shared database
        InventoryMenu inventoryMenu = InventoryBootstrap.buildMenu(scanner);

        boolean running = true;
        while (running) {
            System.out.println("\n=== Super-Lee System ===");
            System.out.println("1. Login by employee ID");
            System.out.println("2. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    login(scanner, employeeManager, managerView, employeeView, inventoryMenu);
                    break;
                case "2":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection, please try again.");
            }
        }
        scanner.close();
    }

    private static void login(Scanner scanner, Manager employeeManager,
                              ManagerView managerView, EmployeeView employeeView,
                              InventoryMenu inventoryMenu) {
        System.out.print("Enter employee ID: ");
        String id = scanner.nextLine().trim();

        Employee employee = employeeManager.searchEmployee(id);
        if (employee == null) {
            System.out.println("Employee does not exist.");
            return;
        }
        if (!employee.isActive()) {
            System.out.println("Employee is inactive and cannot access the system.");
            return;
        }

        System.out.println("Welcome, " + employee.getName());
        showMenuByEmployeeType(scanner, employee, managerView, employeeView, inventoryMenu);
    }

    private static void showMenuByEmployeeType(Scanner scanner, Employee employee,
                                               ManagerView managerView, EmployeeView employeeView,
                                               InventoryMenu inventoryMenu) {
        boolean back = false;
        while (!back) {
            boolean isManager = employee instanceof ShiftManager;
            boolean isWarehouse = hasRole(employee, "Warehouse");

            System.out.println("\n=== Available Menus ===");
            System.out.println("1. Employee menu");

            int managerOption = -1;
            int inventoryOption = -1;
            int next = 2;
            if (isManager) {
                managerOption = next;
                System.out.println(managerOption + ". Employee and shift management menu");
                next++;
            }
            if (isWarehouse || isManager) {
                inventoryOption = next;
                System.out.println(inventoryOption + ". Inventory management menu");
                next++;
            }
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            int selected;
            try {
                selected = Integer.parseInt(choice);
            } catch (Exception e) {
                System.out.println("Invalid selection.");
                continue;
            }

            if (selected == 0) {
                back = true;
            } else if (selected == 1) {
                employeeView.showMenu(scanner);
            } else if (selected == managerOption) {
                managerView.showMenu(scanner);
            } else if (selected == inventoryOption) {
                inventoryMenu.run();
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private static boolean hasRole(Employee employee, String roleName) {
        if (employee == null || roleName == null) {
            return false;
        }
        for (Role role : employee.getRoles()) {
            if (role.getRoleName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
}
