package Inventory;

import Inventory.Domain.InventoryController;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InventoryController controller = new InventoryController();
        PreloadData preloadData = new PreloadData(controller);

        System.out.print("Load preloaded test data? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (choice.equals("y")) {
            preloadData.load();
            System.out.println("Preloaded data ready.\n");
        } else {
            System.out.println("Starting with empty system.\n");
        }

        new InventoryMenu(controller, preloadData, scanner).run();
    }
}
