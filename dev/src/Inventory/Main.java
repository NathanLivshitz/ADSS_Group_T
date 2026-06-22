package Inventory;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Sqlite.*;
import Inventory.Service.InventoryService;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ── Wire Sqlite DAOs ─────────────────────────────────
        SqliteCategoryDAO categoryDAO = new SqliteCategoryDAO();
        SqliteProductDAO productDAO = new SqliteProductDAO();
        SqliteStockItemDAO stockItemDAO = new SqliteStockItemDAO();
        SqlitePromotionDAO promotionDAO = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defectiveReportDAO = new SqliteDefectiveReportDAO();

        // ── Build repositories ───────────────────────────────
        ProductSpecRepository productSpecRepo = new ProductSpecRepository();
        ProductRepository productRepo = new ProductRepository(productDAO);
        StockItemRepository stockItemRepo = new StockItemRepository(stockItemDAO);
        CategoryRepository categoryRepo = new CategoryRepository(categoryDAO);
        PromotionRepository promotionRepo = new PromotionRepository(promotionDAO);
        DefectiveReportRepository defectiveRepo = new DefectiveReportRepository(defectiveReportDAO);

        // ── Hydrate in dependency order ──────────────────────
        categoryRepo.hydrate();
        productSpecRepo.hydrate(productDAO, categoryDAO, categoryRepo.getAllCategoriesMap());
        productRepo.hydrate(productSpecRepo);
        stockItemRepo.hydrate(productSpecRepo);
        promotionRepo.hydrate(productSpecRepo, categoryRepo);
        defectiveRepo.hydrate();

        // ── Controller + Service ─────────────────────────────
        InventoryController controller = new InventoryController(
            productSpecRepo,
            productRepo,
            stockItemRepo,
            categoryRepo,
            promotionRepo,
            defectiveRepo
        );
        InventoryService service = new InventoryService(controller);
        PreloadData preloadData  = new PreloadData(controller);

        // ── First-run seed check ─────────────────────────────
        boolean dbEmpty = productDAO.findAll().isEmpty();
        if (dbEmpty) {
            System.out.print("No data found. Load preloaded test data? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("y")) {
                preloadData.loadFresh();
                System.out.println("Preloaded data ready.\n");
            } else {
                System.out.println("Starting with empty system.\n");
            }
        } else {
            System.out.println("Existing data loaded from database.\n");
        }

        new InventoryMenu(service, preloadData, scanner).run();
    }
}
