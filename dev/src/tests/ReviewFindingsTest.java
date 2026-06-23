package tests;

import Inventory.Data.DAO.Stub.*;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.DTO.*;
import Inventory.Service.InventoryService;
import Shared.DTO.SupplyAgreementDTO;
import Suppliers.Service.SupplierService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for each finding from the architectural review.
 *
 * Each test is named finding<N>_<description> and targets exactly one finding.
 * Tests are written to FAIL against current code, driving fixes.
 *
 * Findings not covered here (hard to unit-test):
 *   Finding 1 — Scheduler not wired in Main.java (composition-root concern)
 *   Finding 7 — generateInventoryReport creates throwaway Product objects (logic correctness, not behavioral regression)
 *   Finding 8 — Interface missing hydrate() method (design, no behavioral assertion possible)
 */
class ReviewFindingsTest {

    private InventoryController controller;
    private int dairyId;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(
            new ProductSpecRepository(new StubProductDAO()),
            new ProductRepository(new StubProductInstanceDAO(), new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO(), new StubStockItemProductsDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
        dairyId = controller.addCategory("Dairy", 0);
    }

    private int addMilkBelowThreshold() {
        int specId = controller.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", dairyId, 3.0, 6.0, 10, 0));
        controller.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 4, null));
        return specId;
    }

    // ── Finding 2: updateShortageReport does not create a StockItem ────────────
    //
    // After an order is confirmed, the ordered quantity should appear in
    // getStockForProduct() with a real location. Currently updateShortageReport
    // only adjusts totalQuantity on the spec; it never creates a StockItem,
    // so the physical location of the incoming goods is invisible.

    @Test
    void finding2_updateShortageReport_newStockAppearsInGetStockForProduct() {
        int specId = addMilkBelowThreshold(); // qty=4, min=10
        int orderedQty = 6;

        // Simulate what confirmOrder calls internally
        controller.updateShortageReport(specId, orderedQty, 3.0);

        List<StockItemDTO> stock = controller.getStockForProduct(specId);
        int total = stock.stream().mapToInt(StockItemDTO::quantity).sum();

        // The spec-level totalQuantity should be 4+6=10
        ProductDTO dto = controller.getProduct(specId);
        assertEquals(10, dto.totalQuantity(), "spec totalQuantity should include ordered qty");

        // AND the new units must be visible via getStockForProduct
        assertEquals(10, total,
            "getStockForProduct total should match spec totalQuantity after shortage restock");
    }

    @Test
    void finding2_updateShortageReport_stockItemCountNotZero() {
        int specId = addMilkBelowThreshold();

        controller.updateShortageReport(specId, 6, 3.0);

        List<StockItemDTO> stock = controller.getStockForProduct(specId);
        // There should be at least one StockItem carrying the restocked units
        assertFalse(stock.isEmpty(),
            "getStockForProduct must return at least one location after updateShortageReport");
    }

    // ── Finding 3: SupplierService public API accepts Suppliers domain types ───
    //
    // addAgreement(SupplyAgreement) and addSchedule(DeliverySchedule) are public
    // methods on SupplierService but accept Suppliers.Domain.* types, breaking the
    // DTO-only cross-module boundary.  The fix is to replace them with DTO-accepting
    // overloads (or remove them in favour of the DTO-based path already used by
    // InventoryService).

    @Test
    void finding3_supplierService_addAgreementAcceptsDTO() throws Exception {
        // SupplierService should expose a DTO-based addAgreement(SupplyAgreementDTO)
        // so callers never need to import Suppliers.Domain types.
        Method dtoMethod = null;
        for (Method m : SupplierService.class.getMethods()) {
            if (m.getName().equals("addAgreement")
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].equals(SupplyAgreementDTO.class)) {
                dtoMethod = m;
                break;
            }
        }
        assertNotNull(dtoMethod,
            "SupplierService must expose addAgreement(SupplyAgreementDTO) — DTO-only boundary");
    }

    @Test
    void finding3_supplierService_noPublicMethodAcceptsDomainType() {
        for (Method m : SupplierService.class.getMethods()) {
            // skip Object methods
            if (m.getDeclaringClass().equals(Object.class)) continue;
            for (Class<?> paramType : m.getParameterTypes()) {
                String pkg = paramType.getPackageName();
                assertFalse(pkg.startsWith("Suppliers.Domain"),
                    "SupplierService public method '" + m.getName()
                    + "' accepts Suppliers.Domain type '" + paramType.getSimpleName()
                    + "' — violates DTO-only boundary");
            }
        }
    }

    // ── Finding 4: DefectiveReportDTO / DefectiveLocationDTO use 'productId' ──
    //   for a value that is always a specId.
    //
    // The field name misleads callers.  After the fix, the accessor on both DTOs
    // should be named specId(), and the old productId() accessor must not exist.

    @Test
    void finding4_defectiveReportDTO_hasSpecIdAccessor() throws Exception {
        // After rename, the record accessor should be specId(), not productId()
        Method specIdMethod = null;
        try { specIdMethod = DefectiveReportDTO.class.getMethod("specId"); }
        catch (NoSuchMethodException ignored) {}
        assertNotNull(specIdMethod,
            "DefectiveReportDTO should expose specId() — the stored value is always a specId");
    }

    @Test
    void finding4_defectiveReportDTO_noProductIdAccessor() throws Exception {
        // Once renamed, productId() should no longer exist
        Method productIdMethod = null;
        try { productIdMethod = DefectiveReportDTO.class.getMethod("productId"); }
        catch (NoSuchMethodException ignored) {}
        assertNull(productIdMethod,
            "DefectiveReportDTO.productId() should not exist after rename to specId()");
    }

    @Test
    void finding4_defectiveLocationDTO_hasSpecIdAccessor() throws Exception {
        Method specIdMethod = null;
        try { specIdMethod = DefectiveLocationDTO.class.getMethod("specId"); }
        catch (NoSuchMethodException ignored) {}
        assertNotNull(specIdMethod,
            "DefectiveLocationDTO should expose specId() — the stored value is always a specId");
    }

    @Test
    void finding4_reportDefective_returnedReportSpecIdMatchesInput() {
        int specId = addMilkBelowThreshold();
        controller.reportDefective(specId, 1, "DEFECTIVE");
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertEquals(1, reports.size());
        // Verify the stored ID is the specId we passed in, whatever the field is named
        // (This test documents the invariant and will stay green through the rename)
        assertEquals(specId, reports.get(0).specId(),
            "stored ID must equal the specId passed to reportDefective");
    }

    // ── Finding 5: ProductDTO.productId is always 0 ────────────────────────────
    //
    // toProductDTO(Product p) delegates to toProductDTO(ProductSpec s) and discards
    // p.getId(). Any route that returns a ProductDTO from a real Product instance
    // (e.g. generateInventoryReport) should carry the physical product ID.

    @Test
    void finding5_generateInventoryReport_productDTOHasNonZeroProductId() {
        int specId = addMilkBelowThreshold(); // adds 4 physical Product instances

        List<ProductDTO> report = controller.generateInventoryReport(null);
        assertEquals(1, report.size());

        ProductDTO dto = report.get(0);
        // The DTO came from a real Product; its productId must not be zero
        assertNotEquals(0, dto.productId(),
            "ProductDTO.productId must be the physical product instance ID, not 0");
    }

    @Test
    void finding5_getProduct_productDTOHasNonZeroProductId() {
        int specId = addMilkBelowThreshold(); // adds 4 physical Product instances

        ProductDTO dto = controller.getProduct(specId);
        // Spec lookup should carry the first associated product instance ID
        assertNotEquals(0, dto.productId(),
            "ProductDTO.productId must not be hardcoded 0 when product instances exist");
    }

    // ── Finding 6: IProductRepository.nextId() consumes the ID ────────────────
    //
    // The interface Javadoc says "without consuming it", but ProductRepository
    // does nextProductId++.  Two consecutive nextId() calls should return the
    // same value (peek semantics); only add() should advance the counter.

    @Test
    void finding6_nextId_peekSemantics_consecutiveCallsReturnSameId() {
        // We can access ProductRepository directly since it is the concrete class
        // used in freshController()
        ProductRepository repo = new ProductRepository(
            new StubProductInstanceDAO(), new StubProductDAO());

        int first  = repo.nextId();
        int second = repo.nextId();

        assertEquals(first, second,
            "nextId() must be a non-consuming peek — two calls without add() must return the same ID");
    }
}
