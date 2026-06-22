package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.DTO.*;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.Data.PreloadData;
import java.time.LocalDate;
import java.util.List;

class PreloadDataTest {

    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(
            new ProductSpecRepository(),
            new ProductRepository(new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
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
        ProductDTO p = controller.getProduct(1);
        assertEquals("Tnuva 3% Milk 1L", p.name());
        assertEquals("Tnuva", p.manufacturer());
        assertEquals(4.5, p.costPrice(), 0.01);
        assertEquals(6.9, p.sellPrice(), 0.01);
        assertEquals(15, p.minStockThreshold());
    }

    @Test
    void product4IsBamba() {
        ProductDTO p = controller.getProduct(4);
        assertEquals("Bamba 80g", p.name());
        assertEquals("Osem", p.manufacturer());
        assertEquals(2.5, p.costPrice(), 0.01);
        assertEquals(4.5, p.sellPrice(), 0.01);
        assertEquals(20, p.minStockThreshold());
    }

    // ── STOCK ───────────────────────────────────────────────

    @Test
    void eachProductHas2StockItems() {
        for (int id = 1; id <= 5; id++) {
            List<StockItemDTO> stock = controller.getStockForProduct(id);
            assertEquals(2, stock.size(), "Product " + id + " should have 2 stock items");
        }
    }

    @Test
    void milk1LStockLocationsCorrect() {
        List<StockItemDTO> stock = controller.getStockForProduct(1);
        StockItemDTO store = null;
        StockItemDTO warehouse = null;
        for (StockItemDTO si : stock) {
            if ("STORE".equals(si.area())) store = si;
            else warehouse = si;
        }
        assertNotNull(store);
        assertNotNull(warehouse);
        assertEquals(2, store.shelf());
        assertEquals(1, store.row());
        assertEquals(1, warehouse.shelf());
        assertEquals(3, warehouse.row());
    }

    @Test
    void milk1LStoreQuantityReducedByDefective() {
        // Original store qty=20, defective report removes 3 (store-first)
        List<StockItemDTO> stock = controller.getStockForProduct(1);
        StockItemDTO store = null;
        for (StockItemDTO si : stock) {
            if ("STORE".equals(si.area())) store = si;
        }
        assertNotNull(store);
        assertEquals(17, store.quantity());
    }

    // ── CATEGORIES ──────────────────────────────────────────

    @Test
    void loadCreates3RootCategories() {
        List<CategoryDTO> roots = controller.getRootCategories();
        assertEquals(3, roots.size());
    }

    @Test
    void dairyCategoryHierarchyIsComplete() {
        CategoryDTO dairy = findRootCategory("Dairy Products");
        assertNotNull(dairy);
        // Dairy -> Milk -> By Size holds the 2 milk products
        List<ProductDTO> dairyProducts = controller.generateInventoryReport(List.of(dairy.categoryId()));
        assertEquals(2, dairyProducts.size());
        // Check "Milk" sub-category exists
        CategoryDTO milk = controller.findCategoryByName("Milk");
        assertNotNull(milk);
        assertEquals(dairy.categoryId(), milk.parentCategoryId());
        // Check "By Size" (under Milk) exists
        CategoryDTO bySize = controller.findCategoryByName("By Size");
        assertNotNull(bySize);
    }

    @Test
    void snacksCategoryContainsBambaAndBissli() {
        CategoryDTO snacks = findRootCategory("Snacks");
        assertNotNull(snacks);
        List<ProductDTO> snackProducts = controller.generateInventoryReport(List.of(snacks.categoryId()));
        assertEquals(2, snackProducts.size());
        boolean hasBamba  = snackProducts.stream().anyMatch(p -> p.name().equals("Bamba 80g"));
        boolean hasBissli = snackProducts.stream().anyMatch(p -> p.name().equals("Bissli 70g"));
        assertTrue(hasBamba);
        assertTrue(hasBissli);
    }

    // ── PROMOTIONS ──────────────────────────────────────────

    @Test
    void dairyPromotionIsActive() {
        List<PromotionDTO> active = controller.getActivePromotions();
        assertEquals(1, active.size());
        assertEquals(10.0, active.get(0).discountPercent(), 0.01);
    }

    @Test
    void milk1LEffectivePriceReflectsDairyPromotion() {
        // sellPrice=6.9, 10% off = 6.21
        double price = controller.getEffectivePrice(1);
        assertEquals(6.21, price, 0.01);
    }

    @Test
    void shampooNotAffectedByDairyPromotion() {
        // Shampoo is Toiletries, not Dairy - full price 14.9
        double price = controller.getEffectivePrice(3);
        assertEquals(14.9, price, 0.01);
    }

    // ── LOW STOCK ───────────────────────────────────────────

    @Test
    void bissliIsLowStock() {
        List<ProductDTO> low = controller.getLowStockProducts();
        boolean bissliFound = false;
        for (ProductDTO p : low) {
            if (p.name().equals("Bissli 70g")) {
                bissliFound = true;
                break;
            }
        }
        assertTrue(bissliFound, "Bissli (total=8, min=12) should be in low stock");
    }

    @Test
    void bambaIsNotLowStock() {
        List<ProductDTO> low = controller.getLowStockProducts();
        for (ProductDTO p : low) {
            assertNotEquals("Bamba 80g", p.name(),
                    "Bamba (total=140, min=20) should not be low stock");
        }
    }

    // ── DEFECTIVE REPORTS ───────────────────────────────────

    @Test
    void defectiveReportExistsForMilk1L() {
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).productId());
        assertEquals(3, reports.get(0).quantity());
        assertEquals("EXPIRED", reports.get(0).reason());
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
        List<StockItemDTO> stock = controller.getStockForProduct(1);
        assertEquals(2, stock.size());
    }

    // ── HELPERS ─────────────────────────────────────────────

    private CategoryDTO findRootCategory(String name) {
        for (CategoryDTO root : controller.getRootCategories()) {
            if (root.categoryName().equals(name)) return root;
        }
        return null;
    }
}
