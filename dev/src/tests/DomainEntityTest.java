import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import java.time.LocalDate;
import java.util.List;

class DomainEntityTest {

    // --- ProductSpec ---

    @Test
    void productSpecStoresTypeFields() {
        Category cat = new Category("Dairy");
        ProductSpec spec = new ProductSpec("Milk 1L", "Tnuva", cat, 4.5, 6.9, 15);
        assertEquals("Milk 1L", spec.getName());
        assertEquals("Tnuva", spec.getManufacturer());
        assertEquals(cat, spec.getCategory());
        assertEquals(4.5, spec.getCostPrice(), 0.01);
        assertEquals(6.9, spec.getSellPrice(), 0.01);
        assertEquals(15, spec.getMinStockThreshold());
    }

    // --- Product ---

    @Test
    void productHasIdAndSpec() {
        Category cat = new Category("Dairy");
        ProductSpec spec = new ProductSpec("Milk 1L", "Tnuva", cat, 4.5, 6.9, 15);
        Product product = new Product(1, spec);
        assertEquals(1, product.getId());
        assertSame(spec, product.getSpec());
    }

    // --- StockItem ---

    @Test
    void stockItemStoresSpecAndLocation() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 10);
        StockItem item = new StockItem(spec, Area.STORE, 2, 3, 50, null);
        assertSame(spec, item.getSpec());
        assertEquals(Area.STORE, item.getArea());
        assertEquals(2, item.getShelfNumber());
        assertEquals(3, item.getRowNumber());
        assertEquals(50, item.getQuantity());
        assertNull(item.getExpiryDate());
    }

    @Test
    void stockItemTracksExpiryPerBatch() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Milk", "Tnuva", cat, 4.5, 6.9, 15);
        LocalDate expiry = LocalDate.of(2026, 7, 1);
        StockItem item = new StockItem(spec, Area.WAREHOUSE, 1, 3, 50, expiry);
        assertEquals(expiry, item.getExpiryDate());
    }

    @Test
    void stockItemQuantityCanBeUpdated() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 10);
        StockItem item = new StockItem(spec, Area.WAREHOUSE, 1, 1, 10, null);
        item.setQuantity(25);
        assertEquals(25, item.getQuantity());
    }

    @Test
    void stockItemsForSameSpecAtDifferentLocations() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 10);
        StockItem s1 = new StockItem(spec, Area.STORE, 1, 1, 20, null);
        StockItem s2 = new StockItem(spec, Area.WAREHOUSE, 1, 1, 30, null);
        assertSame(s1.getSpec(), s2.getSpec());
        int total = s1.getQuantity() + s2.getQuantity();
        assertEquals(50, total);
    }

    // --- Category ---

    @Test
    void categoryWithNoParentIsRoot() {
        Category root = new Category("Dairy");
        assertNull(root.getParent());
        assertEquals("Dairy", root.getName());
    }

    @Test
    void categoryHierarchyParentChild() {
        Category root = new Category("Dairy");
        Category sub = new Category("Milk", root);
        assertEquals(root, sub.getParent());
        assertTrue(root.getSubCategories().contains(sub));
    }

    @Test
    void categoryGetAllProductSpecsRecursive() {
        Category root = new Category("Dairy");
        Category milk = new Category("Milk", root);
        Category bySize = new Category("By Size", milk);

        ProductSpec s1 = new ProductSpec("Milk 1L", "Tnuva", bySize, 4.5, 6.9, 15);
        ProductSpec s2 = new ProductSpec("Milk 500ml", "Tnuva", bySize, 3.0, 4.9, 10);

        List<ProductSpec> all = root.getAllProducts();
        assertEquals(2, all.size());
        assertTrue(all.contains(s1));
        assertTrue(all.contains(s2));
    }

    // --- Promotion ---

    @Test
    void promotionIsActiveWithinDateRange() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 10);
        Promotion promo = new Promotion(10.0, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), spec, null);
        assertTrue(promo.isActive());
    }

    @Test
    void promotionExpiredIsNotActive() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 10);
        Promotion promo = new Promotion(10.0, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30), spec, null);
        assertFalse(promo.isActive());
    }

    @Test
    void promotionEffectivePriceWhenActive() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 100.0, 10);
        Promotion promo = new Promotion(10.0, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), spec, null);
        assertEquals(90.0, promo.getEffectivePrice(spec), 0.01);
    }

    // --- DefectiveReport ---

    @Test
    void defectiveReportStoresFields() {
        DefectiveReport report = new DefectiveReport(1, 5, "EXPIRED",
                LocalDate.of(2026, 4, 10));
        assertEquals(1, report.getProductId());
        assertEquals(5, report.getQuantity());
        assertEquals("EXPIRED", report.getReason());
        assertEquals(LocalDate.of(2026, 4, 10), report.getReportDate());
    }
}
