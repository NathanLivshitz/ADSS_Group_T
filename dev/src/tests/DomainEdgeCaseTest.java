import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import java.time.LocalDate;

class DomainEdgeCaseTest {

    // ── ProductSpec ─────────────────────────────────────────

    @Test
    void productSpecNullNameThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec(null, "Mfg", cat, 5.0, 10.0, 10));
    }

    @Test
    void productSpecEmptyNameThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("", "Mfg", cat, 5.0, 10.0, 10));
    }

    @Test
    void productSpecBlankNameThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("   ", "Mfg", cat, 5.0, 10.0, 10));
    }

    @Test
    void productSpecNullManufacturerThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", null, cat, 5.0, 10.0, 10));
    }

    @Test
    void productSpecEmptyManufacturerThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", "", cat, 5.0, 10.0, 10));
    }

    @Test
    void productSpecNegativeCostPriceThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", "Mfg", cat, -1.0, 10.0, 10));
    }

    @Test
    void productSpecNegativeSellPriceThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", "Mfg", cat, 5.0, -1.0, 10));
    }

    @Test
    void productSpecZeroMinThresholdThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 0));
    }

    @Test
    void productSpecNegativeMinThresholdThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class,
                () -> new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, -5));
    }

    @Test
    void productSpecZeroCostPriceAllowed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Free", "Mfg", cat, 0.0, 10.0, 1);
        assertEquals(0.0, spec.getCostPrice(), 0.01);
    }

    @Test
    void productSpecZeroSellPriceAllowed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Free", "Mfg", cat, 5.0, 0.0, 1);
        assertEquals(0.0, spec.getSellPrice(), 0.01);
    }

    @Test
    void productSpecNullCategoryAllowed() {
        ProductSpec spec = new ProductSpec("Item", "Mfg", null, 5.0, 10.0, 1);
        assertNull(spec.getCategory());
    }

    @Test
    void productSpecNameIsTrimmed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("  Milk  ", "  Tnuva  ", cat, 5.0, 10.0, 1);
        assertEquals("Milk", spec.getName());
        assertEquals("Tnuva", spec.getManufacturer());
    }

    @Test
    void productSpecSetCostPriceNegativeThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class, () -> spec.setCostPrice(-1.0));
    }

    @Test
    void productSpecSetSellPriceNegativeThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class, () -> spec.setSellPrice(-1.0));
    }

    @Test
    void productSpecSetCostPriceUpdatesValue() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        spec.setCostPrice(3.5);
        assertEquals(3.5, spec.getCostPrice(), 0.01);
    }

    @Test
    void productSpecSetSellPriceUpdatesValue() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        spec.setSellPrice(8.0);
        assertEquals(8.0, spec.getSellPrice(), 0.01);
    }

    @Test
    void productSpecAutoRegistersToCategory() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertTrue(cat.getProducts().contains(spec));
    }

    @Test
    void productSpecNullCategoryDoesNotRegister() {
        ProductSpec spec = new ProductSpec("Item", "Mfg", null, 5.0, 10.0, 1);
        assertNull(spec.getCategory());
    }

    // ── Product ─────────────────────────────────────────────

    @Test
    void productNullSpecThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Product(1, null));
    }

    // ── StockItem ───────────────────────────────────────────

    @Test
    void stockItemNullSpecThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(null, Area.STORE, 1, 1, 10, null));
    }

    @Test
    void stockItemNullAreaThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(spec, null, 1, 1, 10, null));
    }

    @Test
    void stockItemZeroShelfThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(spec, Area.STORE, 0, 1, 10, null));
    }

    @Test
    void stockItemNegativeShelfThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(spec, Area.STORE, -1, 1, 10, null));
    }

    @Test
    void stockItemZeroRowThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(spec, Area.STORE, 1, 0, 10, null));
    }

    @Test
    void stockItemNegativeQuantityThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new StockItem(spec, Area.STORE, 1, 1, -5, null));
    }

    @Test
    void stockItemZeroQuantityAllowed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        StockItem item = new StockItem(spec, Area.STORE, 1, 1, 0, null);
        assertEquals(0, item.getQuantity());
    }

    @Test
    void stockItemSetQuantityNegativeThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        StockItem item = new StockItem(spec, Area.STORE, 1, 1, 10, null);
        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(-1));
    }

    @Test
    void stockItemSetQuantityToZeroAllowed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        StockItem item = new StockItem(spec, Area.STORE, 1, 1, 10, null);
        item.setQuantity(0);
        assertEquals(0, item.getQuantity());
    }

    // ── Category ────────────────────────────────────────────

    @Test
    void categoryNullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Category(null));
    }

    @Test
    void categoryEmptyNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Category(""));
    }

    @Test
    void categoryBlankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Category("   "));
    }

    @Test
    void categoryAddNullProductThrows() {
        Category cat = new Category("Test");
        assertThrows(IllegalArgumentException.class, () -> cat.addProduct(null));
    }

    @Test
    void categoryGetAllProductsEmptyTree() {
        Category root = new Category("Root");
        Category sub = new Category("Sub", root);
        assertTrue(root.getAllProducts().isEmpty());
    }

    // ── Promotion ───────────────────────────────────────────

    @Test
    void promotionZeroDiscountThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(0, LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), spec, null));
    }

    @Test
    void promotionNegativeDiscountThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(-10, LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), spec, null));
    }

    @Test
    void promotionOver100DiscountThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(101, LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), spec, null));
    }

    @Test
    void promotion100DiscountAllowed() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        Promotion promo = new Promotion(100, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), spec, null);
        assertEquals(0.0, promo.getEffectivePrice(spec), 0.01);
    }

    @Test
    void promotionNullStartDateThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(10, null, LocalDate.of(2026, 12, 31), spec, null));
    }

    @Test
    void promotionNullEndDateThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(10, LocalDate.of(2026, 1, 1), null, spec, null));
    }

    @Test
    void promotionStartAfterEndThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(10, LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1), spec, null));
    }

    @Test
    void promotionNoTargetThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(10, LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), null, null));
    }

    @Test
    void promotionBothTargetsThrows() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new Promotion(10, LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), spec, cat));
    }

    @Test
    void promotionSameDayStartEndIsActive() {
        Category cat = new Category("Test");
        ProductSpec spec = new ProductSpec("Item", "Mfg", cat, 5.0, 10.0, 1);
        LocalDate today = LocalDate.now();
        Promotion promo = new Promotion(10, today, today, spec, null);
        assertTrue(promo.isActive());
    }

    @Test
    void promotionAppliesToProductInTargetCategory() {
        Category dairy = new Category("Dairy");
        Category milk = new Category("Milk", dairy);
        ProductSpec spec = new ProductSpec("Milk 1L", "Tnuva", milk, 4.5, 6.9, 15);
        Promotion promo = new Promotion(10, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null, dairy);
        assertTrue(promo.appliesTo(spec));
    }

    @Test
    void promotionDoesNotApplyToProductOutsideCategory() {
        Category dairy = new Category("Dairy");
        Category snacks = new Category("Snacks");
        ProductSpec spec = new ProductSpec("Bamba", "Osem", snacks, 2.5, 4.5, 20);
        Promotion promo = new Promotion(10, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null, dairy);
        assertFalse(promo.appliesTo(spec));
    }

    // ── DefectiveReport ─────────────────────────────────────

    @Test
    void defectiveReportZeroProductIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(0, 5, "DEFECTIVE", LocalDate.now()));
    }

    @Test
    void defectiveReportNegativeProductIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(-1, 5, "DEFECTIVE", LocalDate.now()));
    }

    @Test
    void defectiveReportZeroQuantityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, 0, "DEFECTIVE", LocalDate.now()));
    }

    @Test
    void defectiveReportNegativeQuantityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, -3, "DEFECTIVE", LocalDate.now()));
    }

    @Test
    void defectiveReportNullReasonThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, 5, null, LocalDate.now()));
    }

    @Test
    void defectiveReportEmptyReasonThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, 5, "", LocalDate.now()));
    }

    @Test
    void defectiveReportInvalidReasonThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, 5, "BROKEN", LocalDate.now()));
    }

    @Test
    void defectiveReportNullDateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new DefectiveReport(1, 5, "DEFECTIVE", null));
    }

    @Test
    void defectiveReportReasonIsCaseInsensitive() {
        DefectiveReport r = new DefectiveReport(1, 5, "expired", LocalDate.now());
        assertEquals("EXPIRED", r.getReason());
    }

    @Test
    void defectiveReportDefectiveReasonAccepted() {
        DefectiveReport r = new DefectiveReport(1, 5, "defective", LocalDate.now());
        assertEquals("DEFECTIVE", r.getReason());
    }

    // ── InventoryReport ─────────────────────────────────────

    @Test
    void inventoryReportNullDateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new InventoryReport(null, null, null));
    }

    @Test
    void inventoryReportNullListsDefaultToEmpty() {
        InventoryReport report = new InventoryReport(LocalDate.now(), null, null);
        assertNotNull(report.getCategoriesFilter());
        assertTrue(report.getCategoriesFilter().isEmpty());
        assertNotNull(report.getItems());
        assertTrue(report.getItems().isEmpty());
    }
}
