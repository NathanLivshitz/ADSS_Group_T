import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import java.time.LocalDate;
import java.util.List;

class DomainEntityTest {

    // --- ProductLocation ---

    @Test
    void productLocationStoresAreaShelfRowQty() {
        ProductLocation loc = new ProductLocation(Area.STORE, 2, 3, 50);
        assertEquals(Area.STORE, loc.getArea());
        assertEquals(2, loc.getShelfNumber());
        assertEquals(3, loc.getRowNumber());
        assertEquals(50, loc.getQuantity());
    }

    @Test
    void productLocationQuantityCanBeUpdated() {
        ProductLocation loc = new ProductLocation(Area.WAREHOUSE, 1, 1, 10);
        loc.setQuantity(25);
        assertEquals(25, loc.getQuantity());
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
    void categoryGetAllProductsRecursive() {
        Category root = new Category("Dairy");
        Category milk = new Category("Milk", root);
        Category bySize = new Category("By Size", milk);

        Product p1 = new Product(1, "Milk 1L", "Tnuva", bySize, 4.5, 6.9, 15,
                LocalDate.of(2026, 6, 1));
        Product p2 = new Product(2, "Milk 500ml", "Tnuva", bySize, 3.0, 4.9, 10,
                LocalDate.of(2026, 6, 1));

        List<Product> all = root.getAllProducts();
        assertEquals(2, all.size());
        assertTrue(all.contains(p1));
        assertTrue(all.contains(p2));
    }

    // --- Product ---

    @Test
    void productTotalQuantityAcrossLocations() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 10,
                LocalDate.of(2026, 12, 1));
        p.addLocation(new ProductLocation(Area.STORE, 1, 1, 20));
        p.addLocation(new ProductLocation(Area.WAREHOUSE, 1, 1, 30));
        assertEquals(50, p.getTotalQuantity());
    }

    @Test
    void productStoreAndWarehouseQuantitySeparate() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 10,
                LocalDate.of(2026, 12, 1));
        p.addLocation(new ProductLocation(Area.STORE, 1, 1, 15));
        p.addLocation(new ProductLocation(Area.WAREHOUSE, 2, 1, 40));
        assertEquals(15, p.getStoreQuantity());
        assertEquals(40, p.getWarehouseQuantity());
    }

    @Test
    void productBelowMinStock() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 20,
                LocalDate.of(2026, 12, 1));
        p.addLocation(new ProductLocation(Area.STORE, 1, 1, 5));
        assertTrue(p.isBelowMinStock());
    }

    @Test
    void productAboveMinStockNotFlagged() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 10,
                LocalDate.of(2026, 12, 1));
        p.addLocation(new ProductLocation(Area.STORE, 1, 1, 15));
        assertFalse(p.isBelowMinStock());
    }

    // --- Promotion ---

    @Test
    void promotionIsActiveWithinDateRange() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 10,
                LocalDate.of(2026, 12, 1));
        Promotion promo = new Promotion(10.0, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), p, null);
        assertTrue(promo.isActive());
    }

    @Test
    void promotionExpiredIsNotActive() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 10.0, 10,
                LocalDate.of(2026, 12, 1));
        Promotion promo = new Promotion(10.0, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30), p, null);
        assertFalse(promo.isActive());
    }

    @Test
    void promotionEffectivePriceWhenActive() {
        Category cat = new Category("Test");
        Product p = new Product(1, "Item", "Mfg", cat, 5.0, 100.0, 10,
                LocalDate.of(2026, 12, 1));
        Promotion promo = new Promotion(10.0, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), p, null);
        assertEquals(90.0, promo.getEffectivePrice(p), 0.01);
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
