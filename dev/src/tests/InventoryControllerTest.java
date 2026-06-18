import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class InventoryControllerTest {

    private InventoryController controller;
    private Category dairy;
    private Category milk;
    private Category bySize;
    private ProductSpec milkSpec;
    private Product milkProduct;

    @BeforeEach
    void setUp() {
        controller = new InventoryController();
        dairy = new Category("Dairy");
        milk = new Category("Milk", dairy);
        bySize = new Category("By Size", milk);
        milkSpec = new ProductSpec("Tnuva 3% 1L", "Tnuva", bySize, 4.5, 6.9, 15);
        milkProduct = new Product(1, milkSpec);
    }

    // ── INV-1: Maintain product catalog ──────────────────────

    @Test
    void addProductToController() {
        controller.addProduct(milkProduct);
        // verify via report — no direct getProduct
        InventoryReport report = controller.generateInventoryReport(LocalDate.now(), null);
        assertEquals(1, report.getItems().size());
        assertEquals(1, report.getItems().get(0).getId());
    }

    @Test
    void addProductDuplicateIdThrows() {
        controller.addProduct(milkProduct);
        Product dup = new Product(1, new ProductSpec("Other", "Mfg", bySize, 1.0, 2.0, 5));
        assertThrows(IllegalArgumentException.class, () -> controller.addProduct(dup));
    }

    // ── INV-2: Track quantities by exact location ────────────

    @Test
    void addStockItemAndRetrieveForProduct() {
        controller.addProduct(milkProduct);
        StockItem store = new StockItem(milkSpec, Area.STORE, 2, 1, 20, null);
        StockItem warehouse = new StockItem(milkSpec, Area.WAREHOUSE, 1, 3, 50, null);
        controller.addStockItem(store);
        controller.addStockItem(warehouse);

        List<StockItem> stock = controller.getStockForProduct(1);
        assertEquals(2, stock.size());
        assertTrue(stock.contains(store));
        assertTrue(stock.contains(warehouse));
    }

    @Test
    void getStockForProductShowsLocationDetails() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));
        controller.addStockItem(new StockItem(milkSpec, Area.WAREHOUSE, 1, 3, 50, null));

        List<StockItem> stock = controller.getStockForProduct(1);

        // verify store quantity
        int storeQty = stock.stream()
                .filter(si -> si.getArea() == Area.STORE)
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(20, storeQty);

        // verify warehouse quantity
        int warehouseQty = stock.stream()
                .filter(si -> si.getArea() == Area.WAREHOUSE)
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(50, warehouseQty);
    }

    // ── INV-10: Update stock (sale/delivery) ─────────────────

    @Test
    void updateQuantityPositiveDelta() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));

        controller.updateQuantity(1, Area.STORE, 2, 1, 10);

        int total = controller.getStockForProduct(1).stream()
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(30, total);
    }

    @Test
    void updateQuantityNegativeDelta() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));

        controller.updateQuantity(1, Area.STORE, 2, 1, -5);

        int total = controller.getStockForProduct(1).stream()
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(15, total);
    }

    @Test
    void updateQuantityGoingNegativeThrows() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 10, null));

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateQuantity(1, Area.STORE, 2, 1, -15));
    }

    @Test
    void updateQuantityLocationNotFoundThrows() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateQuantity(1, Area.WAREHOUSE, 9, 9, 5));
    }

    // ── INV-3: Proactive stock alerts ────────────────────────

    @Test
    void getLowStockProductsBelowThreshold() {
        controller.addProduct(milkProduct); // minStock = 15
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 5, null));

        List<Product> low = controller.getLowStockProducts();
        assertEquals(1, low.size());
        assertSame(milkProduct, low.get(0));
    }

    @Test
    void getLowStockProductsAboveThresholdNotIncluded() {
        controller.addProduct(milkProduct); // minStock = 15
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));

        List<Product> low = controller.getLowStockProducts();
        assertTrue(low.isEmpty());
    }

    @Test
    void getLowStockAggregatesAcrossLocations() {
        controller.addProduct(milkProduct); // minStock = 15
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 5, null));
        controller.addStockItem(new StockItem(milkSpec, Area.WAREHOUSE, 1, 3, 12, null));
        // total = 17, above threshold 15

        List<Product> low = controller.getLowStockProducts();
        assertTrue(low.isEmpty());
    }

    // ── INV-4: Hierarchical categories ───────────────────────

    @Test
    void addCategoryAndRetrieveRoots() {
        controller.addCategory(dairy);

        List<Category> roots = controller.getRootCategories();
        assertEquals(1, roots.size());
        assertSame(dairy, roots.get(0));
    }

    // ── INV-5: Promotions ────────────────────────────────────

    @Test
    void addPromotionAndGetActive() {
        Promotion active = new Promotion(10.0,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), milkSpec, null);
        controller.addPromotion(active);

        List<Promotion> actives = controller.getActivePromotions();
        assertEquals(1, actives.size());
        assertSame(active, actives.get(0));
    }

    @Test
    void getActivePromotionsExcludesExpired() {
        Promotion expired = new Promotion(10.0,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), milkSpec, null);
        controller.addPromotion(expired);

        List<Promotion> actives = controller.getActivePromotions();
        assertTrue(actives.isEmpty());
    }

    @Test
    void getEffectivePriceWithActivePromotion() {
        controller.addProduct(milkProduct); // sellPrice = 6.9
        Promotion promo = new Promotion(10.0,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), milkSpec, null);
        controller.addPromotion(promo);

        double price = controller.getEffectivePrice(1);
        assertEquals(6.21, price, 0.01); // 6.9 * 0.9
    }

    @Test
    void getEffectivePriceNoActivePromotionReturnsSellPrice() {
        controller.addProduct(milkProduct); // sellPrice = 6.9

        double price = controller.getEffectivePrice(1);
        assertEquals(6.9, price, 0.01);
    }

    @Test
    void getEffectivePriceCategoryPromotion() {
        controller.addProduct(milkProduct);
        Promotion promo = new Promotion(20.0,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, dairy);
        controller.addPromotion(promo);

        double price = controller.getEffectivePrice(1);
        assertEquals(5.52, price, 0.01); // 6.9 * 0.8
    }

    // ── INV-7,11: Report defective + auto reduce ─────────────

    @Test
    void reportDefectiveCreatesReportAndReducesStock() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));

        controller.reportDefective(1, 3, "EXPIRED");

        LocalDate today = LocalDate.now();
        List<DefectiveReport> reports = controller.getDefectiveReports(
                today.minusDays(1), today.plusDays(1));
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).getProductId());
        assertEquals(3, reports.get(0).getQuantity());
        assertEquals("EXPIRED", reports.get(0).getReason());

        int total = controller.getStockForProduct(1).stream()
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(17, total);
    }

    @Test
    void reportDefectiveReducesStoreFirstThenWarehouse() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 5, null));
        controller.addStockItem(new StockItem(milkSpec, Area.WAREHOUSE, 1, 3, 50, null));

        controller.reportDefective(1, 8, "DEFECTIVE");

        List<StockItem> stock = controller.getStockForProduct(1);
        int storeQty = stock.stream()
                .filter(si -> si.getArea() == Area.STORE)
                .mapToInt(StockItem::getQuantity).sum();
        int warehouseQty = stock.stream()
                .filter(si -> si.getArea() == Area.WAREHOUSE)
                .mapToInt(StockItem::getQuantity).sum();
        assertEquals(0, storeQty);      // drained 5 from store
        assertEquals(47, warehouseQty); // drained 3 from warehouse
    }

    @Test
    void reportDefectiveInsufficientStockThrows() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 5, null));

        assertThrows(IllegalArgumentException.class,
                () -> controller.reportDefective(1, 10, "DEFECTIVE"));
    }

    // ── INV-8: Periodic defect reports ───────────────────────

    @Test
    void getDefectiveReportsFilteredByDateRange() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 100, null));
        controller.reportDefective(1, 2, "EXPIRED");

        LocalDate today = LocalDate.now();
        List<DefectiveReport> filtered = controller.getDefectiveReports(
                today.minusDays(1), today.plusDays(1));
        assertEquals(1, filtered.size());

        List<DefectiveReport> outOfRange = controller.getDefectiveReports(
                today.plusDays(10), today.plusDays(20));
        assertTrue(outOfRange.isEmpty());
    }

    // ── INV-7: Locate defective items ────────────────────────

    @Test
    void getDefectiveItemsWithLocationsReturnsStockForDefectiveProducts() {
        controller.addProduct(milkProduct);
        StockItem storeItem = new StockItem(milkSpec, Area.STORE, 2, 1, 20, null);
        controller.addStockItem(storeItem);
        controller.reportDefective(1, 3, "DEFECTIVE");

        Map<Integer, List<StockItem>> result = controller.getDefectiveItemsWithLocations();
        assertTrue(result.containsKey(1));
        assertEquals(1, result.get(1).size());
        assertSame(storeItem, result.get(1).get(0));
    }

    // ── INV-6: Inventory reports by categories ───────────────

    @Test
    void generateInventoryReportNoFilterReturnsAll() {
        controller.addProduct(milkProduct);
        ProductSpec bambaSpec = new ProductSpec("Bamba", "Osem", bySize, 2.5, 4.5, 20);
        controller.addProduct(new Product(2, bambaSpec));

        InventoryReport report = controller.generateInventoryReport(LocalDate.now(), null);
        assertEquals(2, report.getItems().size());
    }

    @Test
    void generateInventoryReportFilterByCategory() {
        controller.addCategory(dairy);
        controller.addProduct(milkProduct); // in dairy > milk > bySize

        Category snacks = new Category("Snacks");
        controller.addCategory(snacks);
        Category chips = new Category("Chips", snacks);
        ProductSpec bambaSpec = new ProductSpec("Bamba", "Osem", chips, 2.5, 4.5, 20);
        controller.addProduct(new Product(2, bambaSpec));

        InventoryReport report = controller.generateInventoryReport(
                LocalDate.now(), List.of(dairy));
        assertEquals(1, report.getItems().size());
        assertEquals(1, report.getItems().get(0).getId());
    }

    @Test
    void generateInventoryReportFilterMultipleCategories() {
        controller.addCategory(dairy);
        controller.addProduct(milkProduct);

        Category snacks = new Category("Snacks");
        controller.addCategory(snacks);
        Category chips = new Category("Chips", snacks);
        ProductSpec bambaSpec = new ProductSpec("Bamba", "Osem", chips, 2.5, 4.5, 20);
        controller.addProduct(new Product(2, bambaSpec));

        InventoryReport report = controller.generateInventoryReport(
                LocalDate.now(), List.of(dairy, snacks));
        assertEquals(2, report.getItems().size());
    }

    // ── RESET ───────────────────────────────────────────────

    @Test
    void resetClearsCatalog() {
        controller.addProduct(milkProduct);
        controller.reset();
        assertThrows(IllegalArgumentException.class, () -> controller.getProduct(1));
    }

    @Test
    void resetClearsStockItems() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, null));
        controller.reset();
        // after reset, product doesn't exist so getStockForProduct throws
        assertThrows(IllegalArgumentException.class, () -> controller.getStockForProduct(1));
    }

    @Test
    void resetClearsCategories() {
        controller.addCategory(dairy);
        controller.reset();
        assertTrue(controller.getRootCategories().isEmpty());
    }

    @Test
    void resetClearsPromotions() {
        controller.addPromotion(new Promotion(10, LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1), milkSpec, null));
        controller.reset();
        assertTrue(controller.getActivePromotions().isEmpty());
    }

    @Test
    void resetClearsDefectiveReports() {
        controller.addProduct(milkProduct);
        controller.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, null));
        controller.reportDefective(1, 1, "DEFECTIVE");
        controller.reset();
        List<DefectiveReport> reports = controller.getDefectiveReports(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertTrue(reports.isEmpty());
    }

    @Test
    void resetAllowsReaddingSameProductId() {
        controller.addProduct(milkProduct);
        controller.reset();
        controller.addProduct(milkProduct);
        assertEquals("Tnuva 3% 1L", controller.getProduct(1).getSpec().getName());
    }

    // ── EMPTY DATA OPERATIONS ───────────────────────────────

    @Test
    void lowStockOnEmptyCatalogReturnsEmpty() {
        assertTrue(controller.getLowStockProducts().isEmpty());
    }

    @Test
    void inventoryReportOnEmptyCatalogReturnsEmpty() {
        InventoryReport report = controller.generateInventoryReport(LocalDate.now(), null);
        assertTrue(report.getItems().isEmpty());
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
        List<DefectiveReport> reports = controller.getDefectiveReports(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertTrue(reports.isEmpty());
    }

    @Test
    void defectiveItemsWithLocationsOnEmptyReturnsEmpty() {
        assertTrue(controller.getDefectiveItemsWithLocations().isEmpty());
    }

    @Test
    void getEffectivePriceOnEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.getEffectivePrice(1));
    }

    @Test
    void getStockForProductOnEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.getStockForProduct(1));
    }

    @Test
    void reportDefectiveOnEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.reportDefective(1, 1, "DEFECTIVE"));
    }

    @Test
    void updateQuantityOnEmptyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.updateQuantity(1, Area.STORE, 1, 1, 5));
    }

    @Test
    void rootCategoriesOnEmptyReturnsEmpty() {
        assertTrue(controller.getRootCategories().isEmpty());
    }
}
