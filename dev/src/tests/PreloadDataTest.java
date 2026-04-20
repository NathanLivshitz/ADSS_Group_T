import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import Inventory.Data.PreloadData;
import java.time.LocalDate;
import java.util.List;

class PreloadDataTest {

    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController();
        new PreloadData(controller).load();
    }

    // ── CATALOG ─────────────────────────────────────────────

    @Test
    void loadCreates5Products() {
        assertNotNull(controller.getProduct(1));
        assertNotNull(controller.getProduct(2));
        assertNotNull(controller.getProduct(3));
        assertNotNull(controller.getProduct(4));
        assertNotNull(controller.getProduct(5));
    }

    @Test
    void product1IsTnuvaMilk1L() {
        Product p = controller.getProduct(1);
        ProductSpec spec = p.getSpec();
        assertEquals("Tnuva 3% Milk 1L", spec.getName());
        assertEquals("Tnuva", spec.getManufacturer());
        assertEquals(4.5, spec.getCostPrice(), 0.01);
        assertEquals(6.9, spec.getSellPrice(), 0.01);
        assertEquals(15, spec.getMinStockThreshold());
    }

    @Test
    void product4IsBamba() {
        Product p = controller.getProduct(4);
        ProductSpec spec = p.getSpec();
        assertEquals("Bamba 80g", spec.getName());
        assertEquals("Osem", spec.getManufacturer());
        assertEquals(2.5, spec.getCostPrice(), 0.01);
        assertEquals(4.5, spec.getSellPrice(), 0.01);
        assertEquals(20, spec.getMinStockThreshold());
    }

    // ── STOCK ───────────────────────────────────────────────

    @Test
    void eachProductHas2StockItems() {
        for (int id = 1; id <= 5; id++) {
            List<StockItem> stock = controller.getStockForProduct(id);
            assertEquals(2, stock.size(), "Product " + id + " should have 2 stock items");
        }
    }

    @Test
    void milk1LStockLocationsCorrect() {
        List<StockItem> stock = controller.getStockForProduct(1);
        StockItem store = null;
        StockItem warehouse = null;
        for (StockItem si : stock) {
            if (si.getArea() == Area.STORE) store = si;
            else warehouse = si;
        }
        assertNotNull(store);
        assertNotNull(warehouse);
        assertEquals(2, store.getShelfNumber());
        assertEquals(1, store.getRowNumber());
        assertEquals(1, warehouse.getShelfNumber());
        assertEquals(3, warehouse.getRowNumber());
    }

    @Test
    void milk1LStoreQuantityReducedByDefective() {
        // Original store qty=20, defective report removes 3 (store-first)
        List<StockItem> stock = controller.getStockForProduct(1);
        StockItem store = null;
        for (StockItem si : stock) {
            if (si.getArea() == Area.STORE) store = si;
        }
        assertNotNull(store);
        assertEquals(17, store.getQuantity());
    }

    // ── CATEGORIES ──────────────────────────────────────────

    @Test
    void loadCreates3RootCategories() {
        List<Category> roots = controller.getRootCategories();
        assertEquals(3, roots.size());
    }

    @Test
    void dairyCategoryHierarchyIsComplete() {
        Category dairy = findRootCategory("Dairy Products");
        assertNotNull(dairy);
        assertEquals(1, dairy.getSubCategories().size());

        Category milk = dairy.getSubCategories().get(0);
        assertEquals("Milk", milk.getName());
        assertEquals(1, milk.getSubCategories().size());

        Category bySize = milk.getSubCategories().get(0);
        assertEquals("By Size", bySize.getName());
        assertEquals(2, bySize.getProducts().size());
    }

    @Test
    void snacksCategoryContainsBambaAndBissli() {
        Category snacks = findRootCategory("Snacks");
        assertNotNull(snacks);
        List<ProductSpec> allProducts = snacks.getAllProducts();
        assertEquals(2, allProducts.size());

        boolean hasBamba = false;
        boolean hasBissli = false;
        for (ProductSpec spec : allProducts) {
            if (spec.getName().equals("Bamba 80g")) hasBamba = true;
            if (spec.getName().equals("Bissli 70g")) hasBissli = true;
        }
        assertTrue(hasBamba);
        assertTrue(hasBissli);
    }

    // ── PROMOTIONS ──────────────────────────────────────────

    @Test
    void dairyPromotionIsActive() {
        List<Promotion> active = controller.getActivePromotions();
        assertEquals(1, active.size());
        assertEquals(10.0, active.get(0).getDiscountPercent(), 0.01);
    }

    @Test
    void milk1LEffectivePriceReflectsDairyPromotion() {
        // sellPrice=6.9, 10% off = 6.21
        double price = controller.getEffectivePrice(1);
        assertEquals(6.21, price, 0.01);
    }

    @Test
    void shampooNotAffectedByDairyPromotion() {
        // Shampoo is Toiletries, not Dairy — full price 14.9
        double price = controller.getEffectivePrice(3);
        assertEquals(14.9, price, 0.01);
    }

    // ── LOW STOCK ───────────────────────────────────────────

    @Test
    void bissliIsLowStock() {
        List<Product> low = controller.getLowStockProducts();
        boolean bissliFound = false;
        for (Product p : low) {
            if (p.getSpec().getName().equals("Bissli 70g")) {
                bissliFound = true;
                break;
            }
        }
        assertTrue(bissliFound, "Bissli (total=8, min=12) should be in low stock");
    }

    @Test
    void bambaIsNotLowStock() {
        List<Product> low = controller.getLowStockProducts();
        for (Product p : low) {
            assertNotEquals("Bamba 80g", p.getSpec().getName(),
                    "Bamba (total=140, min=20) should not be low stock");
        }
    }

    // ── DEFECTIVE REPORTS ───────────────────────────────────

    @Test
    void defectiveReportExistsForMilk1L() {
        List<DefectiveReport> reports = controller.getDefectiveReports(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).getProductId());
        assertEquals(3, reports.get(0).getQuantity());
        assertEquals("EXPIRED", reports.get(0).getReason());
    }

    // ── DOUBLE LOAD (reset + reload) ───────────────────────

    @Test
    void doubleLoadProducesSameProductCount() {
        // setUp already called load() once
        new PreloadData(controller).load(); // second load (resets first)
        assertNotNull(controller.getProduct(1));
        assertNotNull(controller.getProduct(5));
        assertThrows(IllegalArgumentException.class, () -> controller.getProduct(6));
    }

    @Test
    void doubleLoadProducesSameCategoryCount() {
        new PreloadData(controller).load();
        assertEquals(3, controller.getRootCategories().size());
    }

    @Test
    void doubleLoadProducesSamePromotionCount() {
        new PreloadData(controller).load();
        assertEquals(1, controller.getActivePromotions().size());
    }

    @Test
    void doubleLoadStockIntact() {
        new PreloadData(controller).load();
        List<StockItem> stock = controller.getStockForProduct(1);
        assertEquals(2, stock.size());
    }

    // ── HELPERS ─────────────────────────────────────────────

    private Category findRootCategory(String name) {
        for (Category root : controller.getRootCategories()) {
            if (root.getName().equals(name)) return root;
        }
        return null;
    }
}
