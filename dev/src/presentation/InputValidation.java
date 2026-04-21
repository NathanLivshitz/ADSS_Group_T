package presentation;
import java.util.Scanner;

public class InputValidation {

    public static String getValidEmploymentType(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Full-time"))
                return "Full-time";
            if (input.equalsIgnoreCase("Part-time"))
                return "Part-time";
            System.out.print("Invalid input. Please enter exactly 'Full-time' or 'Part-time': ");
        }
    }

    public static String getValidSalaryType(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Hourly"))
                return "Hourly";
            if (input.equalsIgnoreCase("Global"))
                return "Global";
            System.out.print("Invalid input. Please enter exactly 'Hourly' or 'Global': ");
        }
    }

    public static double getValidDouble(Scanner scanner) {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= 0)
                    return value;
                System.out.print("Value cannot be negative. Try again: ");
            }
            catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }

    public static int getValidInt(Scanner scanner) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= 0)
                    return value;
                System.out.print("Value cannot be negative. Try again: ");
            }
            catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid whole number: ");
            }
        }
    }

    public static String getValidNumericString(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.matches("\\d+"))
                return input;
            System.out.print("Invalid input. Please enter numbers only: ");
        }
    }

    public static String getValidShiftType(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("morning") || input.equals("evening") || input.equals("double"))
                return input;
            System.out.print("Invalid input. Please enter exactly 'Morning', 'Evening', or 'Double': ");
        }
    }

    public static String getValidShiftDay(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("sunday") || input.equals("monday") || input.equals("tuesday") || input.equals("wednesday") || input.equals("thursday") || input.equals("friday") || input.equals("saturday")) {
                return input;
            }
            System.out.print("Invalid input. Please enter a valid day: ");
        }
    }
}