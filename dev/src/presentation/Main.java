package presentation;

import dataAccess.Database.DataBase;
import domain.*;

import java.util.Date;
import java.util.Scanner;

public class Main {
    private static boolean isMockDataLoaded = false;

    public static void main(String[] args) {

        try {
            DataBase.getConnection();
            System.out.println("Database connection established successfully.");
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }

        Manager manager = new Manager();

        try {
            if (manager.getEmployees().isEmpty()) {
                initMockData(manager);
                System.out.println("Initial mock data loaded automatically.");
            }
        } catch (Exception e) {
            System.out.println("Could not load initial mock data: " + e.getMessage());
        }

        ManagerView managerView = new ManagerView(manager);
        EmployeeView employeeView = new EmployeeView(manager);
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        System.out.println("Welcome to Super-Lee management system");
        while (running) {
            System.out.println("\nSelect user profile:");
            System.out.println("1. HR Manager");
            System.out.println("2. Employee");
            System.out.println("3. Load mock data");
            System.out.println("4. Reset system (Delete all data)");
            System.out.println("5. Exit");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    managerView.showMenu(scanner);
                    break;
                case "2":
                    employeeView.showMenu(scanner);
                    break;
                case "3":
                    try {
                        initMockData(manager);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Mock data was not loaded because the database already contains data.");
                        System.out.println("To reload the initial sample data, first choose 'Reset system' and then choose 'Load mock data'.");
                    }
                    break;
                case "4":
                    System.out.println(
                            "\nWARNING: This action will permanently delete all employees, shifts, and assignments!");
                    System.out.print("Are you sure you want to perform a hard reset? (yes/no): ");
                    String confirmation = scanner.nextLine().trim();
                    if (confirmation.equalsIgnoreCase("yes")) {
                        manager.resetSystem();
                        System.out.println("System reset successful. All tables have been cleared.");
                    } else {
                        System.out.println("Reset operation cancelled.");
                    }
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection, please try again");
            }
        }
        scanner.close();
    }

    public static void initMockData(Manager m) {

        // if (m.searchEmployee("111") != null) {
        // return;
        // }

        Date today = new Date();

        Employee e1 = new ShiftManager("Michael Brown", "111", "123456",
                new EmploymentConditions("Full-time", "Global", 12000, today, 10));
        e1.addRole(m.searchRole("Cashier"));
        m.addEmployee(e1);

        Employee e2 = new Employee("Bob Hill", "222", "333454",
                new EmploymentConditions("Part-time", "Hourly", 45, today, 8));
        e2.addRole(m.searchRole("Warehouse"));
        m.addEmployee(e2);

        Employee e3 = new ShiftManager("Lola White", "333", "789789",
                new EmploymentConditions("Full-time", "Global", 8500, today, 12));
        e3.addRole(m.searchRole("Warehouse"));
        e3.addRole(m.searchRole("Cashier"));
        m.addEmployee(e3);

        Employee e4 = new Employee("Diana walker", "444", "656747",
                new EmploymentConditions("Part-time", "Hourly", 50, today, 8));
        e4.addRole(m.searchRole("Cashier"));
        m.addEmployee(e4);

        Employee e5 = new ShiftManager("Ben Smith", "555", "012888",
                new EmploymentConditions("Full-time", "Hourly", 40, today, 7));
        e5.addRole(m.searchRole("Warehouse"));
        m.addEmployee(e5);

        Employee e6 = new Employee("Emily Sue", "666", "112244",
                new EmploymentConditions("Full-time", "Global", 11000, today, 5));
        e6.addRole(m.searchRole("Warehouse"));
        m.addEmployee(e6);

        Employee e7 = new Employee("Mia Wilson", "777", "909033",
                new EmploymentConditions("Part-time", "Global", 10000, today, 10));
        e7.addRole(m.searchRole("Cashier"));
        m.addEmployee(e7);

        Employee e8 = new Employee("Johny Lee", "888", "585877",
                new EmploymentConditions("part-time", "Hourly", 36, today, 16));
        e8.addRole(m.searchRole("Cashier"));
        m.addEmployee(e8);

        Employee e9 = new Employee("New worker", "999", "80009",
                new EmploymentConditions("part-time", "Hourly", 30, today, 9));
        e9.addRole(m.searchRole("Warehouse"));
        m.addEmployee(e9);

        Employee e10 = new Driver("Dan Driver", "1010", "456789",
                new EmploymentConditions("Full-time", "Hourly", 55, today, 10), "B");
        e10.addRole(m.searchRole("Driver"));
        m.addEmployee(e10);

        m.submitAvailability("111", new Availability("sunday", "morning"));
        m.submitAvailability("111", new Availability("monday", "morning"));
        m.submitAvailability("111", new Availability("monday", "evening"));
        m.submitAvailability("111", new Availability("tuesday", "morning"));
        m.submitAvailability("111", new Availability("tuesday", "evening"));
        m.submitAvailability("111", new Availability("wednesday", "morning"));
        m.submitAvailability("111", new Availability("wednesday", "evening"));

        m.submitAvailability("222", new Availability("sunday", "evening"));
        m.submitAvailability("222", new Availability("monday", "evening"));
        m.submitAvailability("222", new Availability("thursday", "morning"));
        m.submitAvailability("222", new Availability("friday", "evening"));

        m.submitAvailability("333", new Availability("sunday", "morning"));
        m.submitAvailability("333", new Availability("monday", "morning"));
        m.submitAvailability("333", new Availability("monday", "evening"));
        m.submitAvailability("333", new Availability("friday", "morning"));
        m.submitAvailability("333", new Availability("friday", "evening"));
        m.submitAvailability("333", new Availability("wednesday", "evening"));

        m.submitAvailability("444", new Availability("monday", "morning"));
        m.submitAvailability("444", new Availability("monday", "evening"));
        m.submitAvailability("444", new Availability("tuesday", "morning"));
        m.submitAvailability("444", new Availability("tuesday", "evening"));
        m.submitAvailability("444", new Availability("wednesday", "morning"));
        m.submitAvailability("444", new Availability("friday", "morning"));
        m.submitAvailability("444", new Availability("thursday", "morning"));
        m.submitAvailability("444", new Availability("thursday", "evening"));

        m.submitAvailability("555", new Availability("sunday", "morning"));
        m.submitAvailability("555", new Availability("sunday", "evening"));
        m.submitAvailability("555", new Availability("tuesday", "evening"));
        m.submitAvailability("555", new Availability("wednesday", "morning"));
        m.submitAvailability("555", new Availability("thursday", "morning"));
        m.submitAvailability("555", new Availability("friday", "morning"));

        m.submitAvailability("666", new Availability("sunday", "morning"));
        m.submitAvailability("666", new Availability("monday", "morning"));
        m.submitAvailability("666", new Availability("monday", "evening"));
        m.submitAvailability("666", new Availability("tuesday", "evening"));
        m.submitAvailability("666", new Availability("wednesday", "morning"));
        m.submitAvailability("666", new Availability("wednesday", "evening"));

        m.submitAvailability("777", new Availability("sunday", "morning"));
        m.submitAvailability("777", new Availability("tuesday", "morning"));
        m.submitAvailability("777", new Availability("wednesday", "morning"));
        m.submitAvailability("777", new Availability("thursday", "evening"));

        m.submitAvailability("888", new Availability("sunday", "evening"));
        m.submitAvailability("888", new Availability("monday", "morning"));
        m.submitAvailability("888", new Availability("tuesday", "morning"));
        m.submitAvailability("888", new Availability("tuesday", "evening"));
        m.submitAvailability("888", new Availability("friday", "morning"));
        m.submitAvailability("888", new Availability("friday", "evening"));

        m.submitAvailability("1010", new Availability("sunday", "morning"));
        m.submitAvailability("1010", new Availability("monday", "morning"));
        m.submitAvailability("1010", new Availability("tuesday", "morning"));

        // Shift s1 = new Shift("morning", "sunday");
        // s1.addRoleRequirement(m.searchRole("Cashier"), 2);
        // s1.addRoleRequirement(m.searchRole("Warehouse"), 2);
        // m.addShift(s1);

        Shift s1 = new Shift("morning", "sunday");
        s1.addRoleRequirement(m.searchRole("Cashier"), 2);
        s1.addRoleRequirement(m.searchRole("Warehouse"), 2);
        s1.addRoleRequirement(m.searchRole("Driver"), 1);
        m.addShift(s1);

        Shift s2morning = new Shift("morning", "monday");
        s2morning.addRoleRequirement(m.searchRole("Warehouse"), 1);
        s2morning.addRoleRequirement(m.searchRole("Cashier"), 2);
        m.addShift(s2morning);

        Shift s2evening = new Shift("evening", "monday");
        s2evening.addRoleRequirement(m.searchRole("Warehouse"), 2);
        s2evening.addRoleRequirement(m.searchRole("Cashier"), 2);
        m.addShift(s2evening);

        Shift s3morning = new Shift("morning", "tuesday");
        s3morning.addRoleRequirement(m.searchRole("Cashier"), 3);
        s3morning.addRoleRequirement(m.searchRole("Warehouse"), 1);
        m.addShift(s3morning);

        Shift s3evening = new Shift("evening", "tuesday");
        s3evening.addRoleRequirement(m.searchRole("Cashier"), 2);
        s3evening.addRoleRequirement(m.searchRole("Warehouse"), 1);
        m.addShift(s3evening);

        Shift s4 = new Shift("morning", "wednesday");
        s4.addRoleRequirement(m.searchRole("Cashier"), 2);
        s4.addRoleRequirement(m.searchRole("Warehouse"), 3);
        m.addShift(s4);

        Shift s5 = new Shift("morning", "thursday");
        s5.addRoleRequirement(m.searchRole("Warehouse"), 2);
        s5.addRoleRequirement(m.searchRole("Cashier"), 1);
        m.addShift(s5);

        Shift s6 = new Shift("morning", "friday");
        s6.addRoleRequirement(m.searchRole("Warehouse"), 2);
        s6.addRoleRequirement(m.searchRole("Cashier"), 2);
        m.addShift(s6);

        Shift s7 = new Shift("evening", "friday");
        s7.addRoleRequirement(m.searchRole("Warehouse"), 2);
        s7.addRoleRequirement(m.searchRole("Cashier"), 1);
        m.addShift(s7);

        m.assignShiftManagerToShift("111", m.searchRole("Cashier"), s1, false);
        m.assignEmployeeToShift("333", m.searchRole("Warehouse"), s1, false);
        m.assignEmployeeToShift("777", m.searchRole("Cashier"), s1, false);
        m.assignEmployeeToShift("555", m.searchRole("Warehouse"), s1, false);
        m.assignEmployeeToShift("1010", m.searchRole("Driver"), s1, false);

        m.assignShiftManagerToShift("333", m.searchRole("Warehouse"), s2morning, false);
        m.assignEmployeeToShift("111", m.searchRole("Cashier"), s2morning, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s2morning, false);

        m.assignShiftManagerToShift("333", m.searchRole("Warehouse"), s2evening, false);
        m.assignEmployeeToShift("111", m.searchRole("Cashier"), s2evening, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s2evening, false);
        m.assignEmployeeToShift("222", m.searchRole("Warehouse"), s2evening, false);

        m.assignShiftManagerToShift("111", m.searchRole("Cashier"), s3morning, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s3morning, false);
        m.assignEmployeeToShift("888", m.searchRole("Cashier"), s3morning, false);

        m.assignShiftManagerToShift("555", m.searchRole("Warehouse"), s3evening, false);
        m.assignEmployeeToShift("111", m.searchRole("Cashier"), s3evening, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s3evening, false);

        m.assignShiftManagerToShift("111", m.searchRole("Cashier"), s4, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s4, false);
        m.assignEmployeeToShift("555", m.searchRole("Warehouse"), s4, false);
        m.assignEmployeeToShift("666", m.searchRole("Warehouse"), s4, false);
        m.assignEmployeeToShift("999", m.searchRole("Warehouse"), s4, false);

        m.assignShiftManagerToShift("555", m.searchRole("Warehouse"), s5, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s5, false);
        m.assignEmployeeToShift("222", m.searchRole("Warehouse"), s5, false);

        m.assignShiftManagerToShift("333", m.searchRole("Warehouse"), s6, false);
        m.assignEmployeeToShift("444", m.searchRole("Cashier"), s6, false);
        m.assignEmployeeToShift("888", m.searchRole("Cashier"), s6, false);
        m.assignEmployeeToShift("555", m.searchRole("Warehouse"), s6, false);

        m.assignShiftManagerToShift("333", m.searchRole("Warehouse"), s7, false);
        m.assignEmployeeToShift("222", m.searchRole("Warehouse"), s7, false);
        m.assignEmployeeToShift("888", m.searchRole("Cashier"), s7, false);

        System.out.println("Mock data loaded successfully!");
        isMockDataLoaded = true;
    }
}