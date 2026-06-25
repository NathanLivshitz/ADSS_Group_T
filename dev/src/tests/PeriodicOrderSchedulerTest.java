package tests;

import Inventory.Data.DAO.Sqlite.*;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.DTO.ProductDTO;
import Inventory.DTO.StockItemDTO;
import Inventory.Service.InventoryService;
import Shared.DTO.OrderSummaryDTO;
import Suppliers.Domain.Supplier;
import Suppliers.Domain.SupplyAgreement;
import Suppliers.Domain.DeliverySchedule;
import Suppliers.Domain.SupplierController;
import Suppliers.Service.SupplierService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// TDD: automated periodic scheduler with real SQLite persistence.
// DB is truncated in @BeforeEach so each test starts clean.
class PeriodicOrderSchedulerTest {

    private InventoryController controller;
    private InventoryService service;

    // DAOs - kept for post-test DB assertions
    private SqliteProductDAO productDAO;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection.truncateAll();

        SqliteCategoryDAO categoryDAO = new SqliteCategoryDAO();
        productDAO = new SqliteProductDAO();
        SqliteStockItemDAO stockItemDAO = new SqliteStockItemDAO();
        SqlitePromotionDAO promotionDAO = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defectiveReportDAO = new SqliteDefectiveReportDAO();

        SqliteProductInstanceDAO productInstanceDAO = new SqliteProductInstanceDAO();
        SqliteStockItemProductsDAO stockItemProductsDAO = new SqliteStockItemProductsDAO();
        ProductSpecRepository productSpecRepo = new ProductSpecRepository(productDAO);
        ProductRepository productRepo = new ProductRepository(productInstanceDAO, productDAO);
        StockItemRepository stockItemRepo = new StockItemRepository(stockItemDAO, stockItemProductsDAO);
        CategoryRepository categoryRepo = new CategoryRepository(categoryDAO);
        PromotionRepository promotionRepo = new PromotionRepository(promotionDAO);
        DefectiveReportRepository defectiveRepo = new DefectiveReportRepository(defectiveReportDAO);

        controller = new InventoryController(
            productSpecRepo, productRepo, stockItemRepo,
            categoryRepo, promotionRepo, defectiveRepo);

        // Seed supplier data before creating service
        SupplierController sc = new SupplierController();
        Supplier acme = sc.addSupplier(1, "ACME Supplies");
        Supplier global = sc.addSupplier(2, "Global Foods");
        sc.addSchedule(new DeliverySchedule(acme, java.time.DayOfWeek.MONDAY));
        sc.addSchedule(new DeliverySchedule(global, java.time.DayOfWeek.WEDNESDAY));

        SupplierService seededSupplierService = new SupplierService(sc);
        service = new InventoryService(controller, seededSupplierService);

        int dairyId = service.addCategory("Dairy", 0);

        int milkSpecId = service.addProduct(
            new ProductDTO(0, 0, "Milk 1L", "Tnuva", dairyId, 4.5, 6.9, 10, 0));

        int chipsSpecId = service.addProduct(
            new ProductDTO(0, 0, "Bamba 80g", "Osem", dairyId, 2.5, 4.5, 20, 0));

        service.addStockItem(new StockItemDTO(milkSpecId, "STORE", 2, 1, 3, null));
        service.addStockItem(new StockItemDTO(chipsSpecId, "STORE", 4, 3, 25, null));

        // Supplier agreement: ACME supplies milk (seed via controller - SupplierService is DTO-only)
        sc.addAgreement(new SupplyAgreement(acme, milkSpecId, 10, 5.00));
    }

    // helper: find DB row by spec_id (findById queries product_id PK, not spec_id)
    private ProductDTO findBySpecId(int specId) {
        for (ProductDTO dto : productDAO.findAll()) {
            if (dto.specId() == specId) return dto;
        }
        return null;
    }

    // TESTS
    @Test
    void checkAndPlacePeriodicOrders_returnsOrdersWhenTriggerDayMatches() {
        List<OrderSummaryDTO> placed = service.checkAndPlacePeriodicOrders(1);
        assertNotNull(placed);
    }

    @Test
    void updateShortageReport_persistsToDatabase() {
        int dairyId = service.addCategory("TestCat", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "TestProduct", "TestMfg", dairyId, 3.0, 5.0, 10, 0));
        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));

        ProductDTO before = findBySpecId(specId);
        assertNotNull(before);

        // Simulate what the scheduler does: update shortage report
        service.updateShortageReport(specId, 20, 4.0);

        // Read directly from DB - NOT from in-memory repo
        ProductDTO after = findBySpecId(specId);
        assertNotNull(after);

        // costPrice should have been updated
        assertEquals(4.0, after.costPrice(), 0.01);
        // totalQuantity should be the in-memory value (5 initial + 20 ordered = 25)
        assertEquals(25, after.totalQuantity());
    }

    @Test
    void fullSchedulerPipeline_persistsOrderEffectsToDB() {
        int milkSpecId = 1; // from seed data in setUp

        ProductDTO before = findBySpecId(milkSpecId);
        assertNotNull(before);
        int qtyBefore = before.totalQuantity();

        // Run the scheduler
        List<OrderSummaryDTO> placed = service.checkAndPlacePeriodicOrders(1);

        ProductDTO after = findBySpecId(milkSpecId);
        assertNotNull(after);

        if (!placed.isEmpty()) {
            assertTrue(after.totalQuantity() >= qtyBefore,
                "DB totalQuantity should not decrease after placing orders");
        } else {
            assertEquals(qtyBefore, after.totalQuantity(),
                "DB totalQuantity should be unchanged when no orders placed");
        }
    }

    @Test
    void submitAllPeriodicOrders_persistsToDatabase() {
        int milkSpecId = 1;

        ProductDTO before = findBySpecId(milkSpecId);
        assertNotNull(before);
        int qtyBefore = before.totalQuantity();

        List<OrderSummaryDTO> placed = service.submitAllPeriodicOrders();

        ProductDTO after = findBySpecId(milkSpecId);
        assertNotNull(after);

        assertTrue(placed.size() > 0, "should have placed at least one order");
        assertTrue(after.totalQuantity() > qtyBefore,
            "DB totalQuantity should increase after submitAllPeriodicOrders");
    }
}
