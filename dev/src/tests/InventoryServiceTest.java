import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import Inventory.Service.InventoryService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class InventoryServiceTest {

    private InventoryController controller;
    private InventoryService service;
    private Category dairy;
    private Category milk;
    private ProductSpec milkSpec;
    private Product milkProduct;

    @BeforeEach
    void setUp() {
        controller = new InventoryController();
        service = new InventoryService(controller);
        dairy = new Category("Dairy");
        milk = new Category("Milk", dairy);
        milkSpec = new ProductSpec("Tnuva 3% 1L", "Tnuva", milk, 4.5, 6.9, 10);
        milkProduct = new Product(1, milkSpec);
    }

    // ── Constructor ────────────────────────────────────────

    @Test
    void constructorRejectsNullController() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryService(null));
    }

    // ── Catalog ────────────────────────────────────────────

    @Test
    void addProductThroughService() {
        service.addProduct(milkProduct);
        Product retrieved = service.getProduct(1);
        assertEquals(1, retrieved.getId());
        assertEquals("Tnuva 3% 1L", retrieved.getSpec().getName());
    }

    @Test
    void getProductNotFoundThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.getProduct(999));
    }

    // ── Stock ──────────────────────────────────────────────

    @Test
    void addStockAndViewThroughService() {
        service.addProduct(milkProduct);
        StockItem store = new StockItem(milkSpec, Area.STORE, 2, 1, 20, null);
        service.addStockItem(store);

        List<StockItem> stock = service.getStockForProduct(1);
        assertEquals(1, stock.size());
        assertEquals(20, stock.get(0).getQuantity());
    }

    @Test
    void updateQuantityThroughService() {
        service.addProduct(milkProduct);
        StockItem store = new StockItem(milkSpec, Area.STORE, 2, 1, 20, null);
        service.addStockItem(store);

        service.updateQuantity(1, Area.STORE, 2, 1, -5);
        assertEquals(15, service.getStockForProduct(1).get(0).getQuantity());
    }

    // ── Alerts ─────────────────────────────────────────────

    @Test
    void lowStockAlertsThroughService() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 5, null));

        List<Product> low = service.getLowStockProducts();
        assertEquals(1, low.size());
        assertEquals(1, low.get(0).getId());
    }

    // ── Categories ─────────────────────────────────────────

    @Test
    void addCategoryThroughService() {
        Category c = new Category("Bread");
        service.addCategory(c);
        assertTrue(service.getRootCategories().contains(c));
    }

    // ── Promotions ─────────────────────────────────────────

    @Test
    void addAndListActivePromotions() {
        service.addProduct(milkProduct);
        Promotion promo = new Promotion(10.0,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                milkSpec, null);
        service.addPromotion(promo);

        List<Promotion> active = service.getActivePromotions();
        assertEquals(1, active.size());
    }

    @Test
    void effectivePriceWithDiscount() {
        service.addProduct(milkProduct);
        Promotion promo = new Promotion(10.0,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                milkSpec, null);
        service.addPromotion(promo);

        double price = service.getEffectivePrice(1);
        // 6.9 * 0.9 = 6.21
        assertEquals(6.21, price, 0.001);
    }

    @Test
    void effectivePriceWithoutPromotion() {
        service.addProduct(milkProduct);
        assertEquals(6.9, service.getEffectivePrice(1), 0.001);
    }

    // ── Defectives ─────────────────────────────────────────

    @Test
    void reportDefectiveThroughService() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, null));

        service.reportDefective(1, 3, "DEFECTIVE");
        assertEquals(7, service.getStockForProduct(1).get(0).getQuantity());
    }

    @Test
    void reportDefectiveRejectsNegativeQuantity() {
        service.addProduct(milkProduct);
        assertThrows(IllegalArgumentException.class,
                () -> service.reportDefective(1, -1, "DEFECTIVE"));
    }

    @Test
    void reportDefectiveRejectsEmptyReason() {
        service.addProduct(milkProduct);
        assertThrows(IllegalArgumentException.class,
                () -> service.reportDefective(1, 1, ""));
    }

    @Test
    void locateDefectiveItemsThroughService() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, null));
        service.reportDefective(1, 2, "DEFECTIVE");

        Map<Integer, List<StockItem>> defectives = service.getDefectiveItemsWithLocations();
        assertTrue(defectives.containsKey(1));
    }

    @Test
    void defectiveReportsByDateRange() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, null));
        service.reportDefective(1, 1, "DEFECTIVE");

        LocalDate today = LocalDate.now();
        List<DefectiveReport> reports = service.getDefectiveReports(today.minusDays(1), today.plusDays(1));
        assertEquals(1, reports.size());
    }

    // ── Reports ────────────────────────────────────────────

    @Test
    void generateInventoryReportThroughService() {
        service.addProduct(milkProduct);
        InventoryReport report = service.generateInventoryReport(LocalDate.now(), null);
        assertEquals(1, report.getItems().size());
    }

    @Test
    void generateFilteredReport() {
        service.addProduct(milkProduct);
        List<Category> filter = new ArrayList<>();
        filter.add(dairy);
        InventoryReport report = service.generateInventoryReport(LocalDate.now(), filter);
        assertEquals(1, report.getItems().size());
    }

    // ── Reset ──────────────────────────────────────────────

    @Test
    void resetClearsEverything() {
        service.addProduct(milkProduct);
        service.reset();
        assertThrows(IllegalArgumentException.class, () -> service.getProduct(1));
    }

    // ── Remove expired ─────────────────────────────────────

    @Test
    void removeExpiredStockThroughService() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 10, LocalDate.now().minusDays(1)));

        int removed = service.removeExpiredStock();
        assertEquals(10, removed);
    }
}
