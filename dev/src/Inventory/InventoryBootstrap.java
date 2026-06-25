package Inventory;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Sqlite.*;
import Inventory.Service.InventoryService;
import Inventory.Service.PeriodicOrderScheduler;
import Inventory.Service.SchedulerConfig;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;
import java.util.Scanner;

// Wires the inventory module against the shared SQLite database and returns a ready
// InventoryMenu. Used by the unified SystemMain so the inventory side runs with real
// persistence (the same wiring Inventory.Main uses standalone).
public class InventoryBootstrap {

    public static InventoryMenu buildMenu(Scanner scanner) {
        SqliteCategoryDAO categoryDAO = new SqliteCategoryDAO();
        SqliteProductDAO productDAO = new SqliteProductDAO();
        SqliteProductInstanceDAO productInstanceDAO = new SqliteProductInstanceDAO();
        SqliteStockItemDAO stockItemDAO = new SqliteStockItemDAO();
        SqliteStockItemProductsDAO stockItemProductsDAO = new SqliteStockItemProductsDAO();
        SqlitePromotionDAO promotionDAO = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defectiveReportDAO = new SqliteDefectiveReportDAO();

        ProductSpecRepository productSpecRepo = new ProductSpecRepository(productDAO);
        ProductRepository productRepo = new ProductRepository(productInstanceDAO, productDAO);
        StockItemRepository stockItemRepo = new StockItemRepository(stockItemDAO, stockItemProductsDAO);
        CategoryRepository categoryRepo = new CategoryRepository(categoryDAO);
        PromotionRepository promotionRepo = new PromotionRepository(promotionDAO);
        DefectiveReportRepository defectiveRepo = new DefectiveReportRepository(defectiveReportDAO);

        categoryRepo.hydrate();
        productSpecRepo.hydrate(categoryDAO, categoryRepo.getAllCategoriesMap());
        productRepo.hydrate(productSpecRepo);
        stockItemRepo.hydrate(productSpecRepo);
        promotionRepo.hydrate(productSpecRepo, categoryRepo);
        defectiveRepo.hydrate();

        InventoryController controller = new InventoryController(
                productSpecRepo, productRepo, stockItemRepo,
                categoryRepo, promotionRepo, defectiveRepo);
        InventoryService service = new InventoryService(controller);
        service.seedDefaultSuppliers();
        new PeriodicOrderScheduler(service, new SchedulerConfig()).start();

        PreloadData preloadData = new PreloadData(controller);
        if (productDAO.findAll().isEmpty()) {
            preloadData.loadFresh();
        }
        return new InventoryMenu(service, preloadData, scanner);
    }
}
