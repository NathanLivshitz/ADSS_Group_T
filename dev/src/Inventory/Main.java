package Inventory;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.Service.InventoryService;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InventoryController controller = new InventoryController(
            new ProductSpecRepository(),
            new ProductRepository(new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
        InventoryService service = new InventoryService(controller);
        PreloadData preloadData = new PreloadData(controller);

        System.out.print("Load preloaded test data? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (choice.equals("y")) {
            preloadData.load();
            System.out.println("Preloaded data ready.\n");
        } else {
            System.out.println("Starting with empty system.\n");
        }

        new InventoryMenu(service, preloadData, scanner).run();
    }
}
