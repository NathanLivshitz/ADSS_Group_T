package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.DTO.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class InventoryControllerTest {

    private InventoryController controller;
    // category IDs assigned by controller.addCategory()
    private int dairyId;
    private int milkId;
    private int bySizeId;

    private InventoryController freshController() {
        return new InventoryController(
            new ProductSpecRepository(new StubProductDAO()),
            new ProductRepository(new StubProductInstanceDAO(), new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO(), new StubStockItemProductsDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
    }

    @BeforeEach
    void setUp() {
        controller = freshController();
        dairyId  = controller.addCategory("Dairy", 0);
        milkId   = controller.addCategory("Milk", dairyId);
        bySizeId = controller.addCategory("By Size", milkId);
    }

    private int addMilk() {
        return controller.addProduct(
            new ProductDTO(0, 0, "Tnuva 3% 1L", "Tnuva", bySizeId, 4.5, 6.9, 15, 0));
    }

    // Maintain product catalog
    @Test
    void addProductToController() {
        int specId = addMilk();
        List<ProductDTO> report = controller.generateInventoryReport(null);
        assertEquals(1, report.size());
        assertEquals(specId, report.get(0).specId());
    }

    @Test
    void addProductDuplicateIdThrows() {
        // non-existent categoryId must throw
        assertThrows(IllegalArgumentException.class, () ->
            controller.addProduct(new ProductDTO(0, 0, "Other", "Mfg", 9999, 1.0, 2.0, 5, 0)));
    }

    // Track quantities by exact location
    @Test
    void addStockItemAndRetrieveForProduct() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE",     2, 1, 20, null));
        controller.addStockItem(new StockItemDTO(specId, "WAREHOUSE", 1, 3, 50, null));

        List<StockItemDTO> stock = controller.getStockForProduct(specId);
        assertEquals(2, stock.size());
        boolean hasStore     = stock.stream().anyMatch(s -> "STORE".equals(s.area()));
        boolean hasWarehouse = stock.stream().anyMatch(s -> "WAREHOUSE".equals(s.area()));
        assertTrue(hasStore);
        assertTrue(hasWarehouse);
    }

    @Test
    void getStockForProductShowsLocationDetails() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE",     2, 1, 20, null));
        controller.addStockItem(new StockItemDTO(specId, "WAREHOUSE", 1, 3, 50, null));

        List<StockItemDTO> stock = controller.getStockForProduct(specId);

        int storeQty = stock.stream()
            .filter(si -> "STORE".equals(si.area()))
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(20, storeQty);

        int warehouseQty = stock.stream()
            .filter(si -> "WAREHOUSE".equals(si.area()))
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(50, warehouseQty);
    }

    // Update stock (sale/delivery)
    @Test
    void updateQuantityPositiveDelta() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));

        controller.updateQuantity(specId, "STORE", 2, 1, 10);

        int total = controller.getStockForProduct(specId).stream()
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(30, total);
    }

    @Test
    void updateQuantityNegativeDelta() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));

        controller.updateQuantity(specId, "STORE", 2, 1, -5);

        int total = controller.getStockForProduct(specId).stream()
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(15, total);
    }

    @Test
    void updateQuantityGoingNegativeThrows() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 10, null));

        assertThrows(IllegalArgumentException.class,
            () -> controller.updateQuantity(specId, "STORE", 2, 1, -15));
    }

    @Test
    void updateQuantityLocationNotFoundThrows() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));

        // WAREHOUSE row 9,9 was never added
        assertThrows(IllegalArgumentException.class,
            () -> controller.updateQuantity(specId, "WAREHOUSE", 9, 9, 5));
    }

    // Proactive stock alerts
    @Test
    void getLowStockProductsBelowThreshold() {
        int specId = addMilk(); // minStock = 15
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 5, null));

        List<ProductDTO> low = controller.getLowStockProducts();
        assertEquals(1, low.size());
        assertEquals(specId, low.get(0).specId());
    }

    @Test
    void getLowStockProductsAboveThresholdNotIncluded() {
        int specId = addMilk(); // minStock = 15
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));

        assertTrue(controller.getLowStockProducts().isEmpty());
    }

    @Test
    void getLowStockAggregatesAcrossLocations() {
        int specId = addMilk(); // minStock = 15
        controller.addStockItem(new StockItemDTO(specId, "STORE",     2, 1,  5, null));
        controller.addStockItem(new StockItemDTO(specId, "WAREHOUSE", 1, 3, 12, null));
        // total = 17, above threshold 15

        assertTrue(controller.getLowStockProducts().isEmpty());
    }

    // Hierarchical categories
    @Test
    void addCategoryAndRetrieveRoots() {
        // dairyId already added in setUp; verify it appears as a root
        List<CategoryDTO> roots = controller.getRootCategories();
        assertEquals(1, roots.size());
        assertEquals(dairyId, roots.get(0).categoryId());
        assertEquals("Dairy", roots.get(0).categoryName());
    }

    @Test
    void addMultipleRootCategories() {
        int snacksId = controller.addCategory("Snacks", 0);
        List<CategoryDTO> roots = controller.getRootCategories();
        assertEquals(2, roots.size());
        boolean hasDairy  = roots.stream().anyMatch(c -> "Dairy".equals(c.categoryName()));
        boolean hasSnacks = roots.stream().anyMatch(c -> "Snacks".equals(c.categoryName()));
        assertTrue(hasDairy);
        assertTrue(hasSnacks);
    }

    // Promotions
    @Test
    void addPromotionAndGetActive() {
        int specId = addMilk();
        controller.addPromotion(new PromotionDTO(10.0,
            "2026-01-01", "2026-12-31",
            specId, 0, null, null));

        List<PromotionDTO> actives = controller.getActivePromotions();
        assertEquals(1, actives.size());
        assertEquals(10.0, actives.get(0).discountPercent(), 0.01);
        assertEquals(specId, actives.get(0).targetSpecId());
    }

    @Test
    void getActivePromotionsExcludesExpired() {
        int specId = addMilk();
        controller.addPromotion(new PromotionDTO(10.0,
            "2025-01-01", "2025-06-30",
            specId, 0, null, null));

        assertTrue(controller.getActivePromotions().isEmpty());
    }

    @Test
    void getEffectivePriceWithActivePromotion() {
        int specId = addMilk(); // sellPrice = 6.9
        controller.addPromotion(new PromotionDTO(10.0,
            "2026-01-01", "2026-12-31",
            specId, 0, null, null));

        double price = controller.getEffectivePrice(specId);
        assertEquals(6.21, price, 0.01); // 9 * 0.9
    }

    @Test
    void getEffectivePriceNoActivePromotionReturnsSellPrice() {
        int specId = addMilk(); // sellPrice = 6.9

        double price = controller.getEffectivePrice(specId);
        assertEquals(6.9, price, 0.01);
    }

    @Test
    void getEffectivePriceCategoryPromotion() {
        int specId = addMilk(); // in bySize -> milk -> dairy
        // promo targets the dairy root; milk spec is a descendant so it applies
        controller.addPromotion(new PromotionDTO(20.0,
            "2026-01-01", "2026-12-31",
            0, dairyId, null, null));

        double price = controller.getEffectivePrice(specId);
        assertEquals(5.52, price, 0.01); // 9 * 0.8
    }

    // Report defective + auto reduce
    @Test
    void reportDefectiveCreatesReportAndReducesStock() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));

        controller.reportDefective(specId, 3, "EXPIRED");

        LocalDate today = LocalDate.now();
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
            today.minusDays(1), today.plusDays(1));
        assertEquals(1, reports.size());
        assertEquals(specId, reports.get(0).specId());
        assertEquals(3, reports.get(0).quantity());
        assertEquals("EXPIRED", reports.get(0).reason());

        int total = controller.getStockForProduct(specId).stream()
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(17, total);
    }

    @Test
    void reportDefectiveReducesStoreFirstThenWarehouse() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE",      2, 1,  5, null));
        controller.addStockItem(new StockItemDTO(specId, "WAREHOUSE",  1, 3, 50, null));

        controller.reportDefective(specId, 8, "DEFECTIVE");

        List<StockItemDTO> stock = controller.getStockForProduct(specId);
        int storeQty = stock.stream()
            .filter(si -> "STORE".equals(si.area()))
            .mapToInt(StockItemDTO::quantity).sum();
        int warehouseQty = stock.stream()
            .filter(si -> "WAREHOUSE".equals(si.area()))
            .mapToInt(StockItemDTO::quantity).sum();
        assertEquals(0,  storeQty);     // drained 5 from store
        assertEquals(47, warehouseQty); // drained 3 from warehouse
    }

    @Test
    void reportDefectiveInsufficientStockThrows() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 5, null));

        assertThrows(IllegalArgumentException.class,
            () -> controller.reportDefective(specId, 10, "DEFECTIVE"));
    }

    // Periodic defect reports
    @Test
    void getDefectiveReportsFilteredByDateRange() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 100, null));
        controller.reportDefective(specId, 2, "EXPIRED");

        LocalDate today = LocalDate.now();
        List<DefectiveReportDTO> filtered = controller.getDefectiveReports(
            today.minusDays(1), today.plusDays(1));
        assertEquals(1, filtered.size());

        List<DefectiveReportDTO> outOfRange = controller.getDefectiveReports(
            today.plusDays(10), today.plusDays(20));
        assertTrue(outOfRange.isEmpty());
    }

    // Locate defective items
    @Test
    void getDefectiveItemsWithLocationsReturnsStockForDefectiveProducts() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 2, 1, 20, null));
        controller.reportDefective(specId, 3, "DEFECTIVE");

        Map<Integer, List<StockItemDTO>> result = controller.getDefectiveItemsWithLocations();
        assertTrue(result.containsKey(specId));
        // After reporting 3 defective from 20, 17 remain in STORE
        assertEquals(1, result.get(specId).size());
        assertEquals("STORE", result.get(specId).get(0).area());
        assertEquals(17, result.get(specId).get(0).quantity());
    }

    // Inventory reports by categories
    @Test
    void generateInventoryReportNoFilterReturnsAll() {
        int milkSpecId = addMilk();
        int bambaSpecId = controller.addProduct(
            new ProductDTO(0, 0, "Bamba", "Osem", bySizeId, 2.5, 4.5, 20, 0));

        List<ProductDTO> report = controller.generateInventoryReport(null);
        assertEquals(2, report.size());
    }

    @Test
    void generateInventoryReportFilterByCategory() {
        int milkSpecId = addMilk(); // in dairy > milk > bySize

        int snacksId = controller.addCategory("Snacks", 0);
        int chipsId  = controller.addCategory("Chips", snacksId);
        int bambaSpecId = controller.addProduct(
            new ProductDTO(0, 0, "Bamba", "Osem", chipsId, 2.5, 4.5, 20, 0));

        // filter by dairy root - should include milk (descendant) but not bamba
        List<ProductDTO> report = controller.generateInventoryReport(List.of(dairyId));
        assertEquals(1, report.size());
        assertEquals(milkSpecId, report.get(0).specId());
    }

    @Test
    void generateInventoryReportFilterMultipleCategories() {
        int milkSpecId = addMilk();

        int snacksId = controller.addCategory("Snacks", 0);
        int chipsId  = controller.addCategory("Chips", snacksId);
        int bambaSpecId = controller.addProduct(
            new ProductDTO(0, 0, "Bamba", "Osem", chipsId, 2.5, 4.5, 20, 0));

        List<ProductDTO> report = controller.generateInventoryReport(List.of(dairyId, snacksId));
        assertEquals(2, report.size());
    }

    // RESET
    @Test
    void resetClearsCatalog() {
        int specId = addMilk();
        controller.reset();
        assertThrows(IllegalArgumentException.class, () -> controller.getProduct(specId));
    }

    @Test
    void resetClearsStockItems() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 10, null));
        controller.reset();
        // product gone after reset, so getStockForProduct must throw
        assertThrows(IllegalArgumentException.class, () -> controller.getStockForProduct(specId));
    }

    @Test
    void resetClearsCategories() {
        controller.reset();
        assertTrue(controller.getRootCategories().isEmpty());
    }

    @Test
    void resetClearsPromotions() {
        int specId = addMilk();
        controller.addPromotion(new PromotionDTO(10.0,
            LocalDate.now().minusDays(1).toString(),
            LocalDate.now().plusDays(1).toString(),
            specId, 0, null, null));
        controller.reset();
        assertTrue(controller.getActivePromotions().isEmpty());
    }

    @Test
    void resetClearsDefectiveReports() {
        int specId = addMilk();
        controller.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 10, null));
        controller.reportDefective(specId, 1, "DEFECTIVE");
        controller.reset();
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertTrue(reports.isEmpty());
    }

    @Test
    void resetAllowsReaddingProduct() {
        int specId = addMilk();
        controller.reset();
        // re-register categories after reset
        int newDairyId  = controller.addCategory("Dairy", 0);
        int newMilkId   = controller.addCategory("Milk", newDairyId);
        int newBySizeId = controller.addCategory("By Size", newMilkId);
        int newSpecId = controller.addProduct(
            new ProductDTO(0, 0, "Tnuva 3% 1L", "Tnuva", newBySizeId, 4.5, 6.9, 15, 0));
        assertEquals("Tnuva 3% 1L", controller.getProduct(newSpecId).name());
    }

    // EMPTY DATA OPERATIONS
    @Test
    void lowStockOnEmptyCatalogReturnsEmpty() {
        controller.reset();
        assertTrue(controller.getLowStockProducts().isEmpty());
    }

    @Test
    void inventoryReportOnEmptyCatalogReturnsEmpty() {
        controller.reset();
        List<ProductDTO> report = controller.generateInventoryReport(null);
        assertTrue(report.isEmpty());
    }

    @Test
    void activePromotionsOnEmptyReturnsEmpty() {
        assertTrue(controller.getActivePromotions().isEmpty());
    }

    @Test
    void removeExpiredStockOnEmptyReturnsZero() {
        assertEquals(0, controller.removeExpiredStock());
    }

    @Test
    void defectiveReportsOnEmptyReturnsEmpty() {
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertTrue(reports.isEmpty());
    }

    @Test
    void defectiveItemsWithLocationsOnEmptyReturnsEmpty() {
        assertTrue(controller.getDefectiveItemsWithLocations().isEmpty());
    }

    @Test
    void getEffectivePriceOnEmptyThrows() {
        controller.reset();
        assertThrows(IllegalArgumentException.class, () -> controller.getEffectivePrice(1));
    }

    @Test
    void getStockForProductOnEmptyThrows() {
        controller.reset();
        assertThrows(IllegalArgumentException.class, () -> controller.getStockForProduct(1));
    }

    @Test
    void reportDefectiveOnEmptyThrows() {
        controller.reset();
        assertThrows(IllegalArgumentException.class,
            () -> controller.reportDefective(1, 1, "DEFECTIVE"));
    }

    @Test
    void updateQuantityOnEmptyThrows() {
        controller.reset();
        assertThrows(IllegalArgumentException.class,
            () -> controller.updateQuantity(1, "STORE", 1, 1, 5));
    }

    @Test
    void rootCategoriesOnEmptyReturnsEmpty() {
        controller.reset();
        assertTrue(controller.getRootCategories().isEmpty());
    }
}
