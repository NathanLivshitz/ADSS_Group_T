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

// Regression tests for inventory behavior and the service/DTO boundary.
class InventoryRegressionTest {

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

    @Test
    void updateShortageReport_newStockAppearsInStockView() {
        int specId = addMilkBelowThreshold(); // qty=4, min=10
        int orderedQty = 6;

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
    void updateShortageReport_stockItemExistsAfterRestock() {
        int specId = addMilkBelowThreshold();

        controller.updateShortageReport(specId, 6, 3.0);

        List<StockItemDTO> stock = controller.getStockForProduct(specId);
        // There should be at least one StockItem carrying the restocked units
        assertFalse(stock.isEmpty(),
            "getStockForProduct must return at least one location after updateShortageReport");
    }

    @Test
    void supplierService_addAgreementAcceptsDTO() throws Exception {
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
            "SupplierService must expose addAgreement(SupplyAgreementDTO) - DTO-only boundary");
    }

    @Test
    void supplierService_noPublicMethodAcceptsDomainType() {
        for (Method m : SupplierService.class.getMethods()) {
            // skip Object methods
            if (m.getDeclaringClass().equals(Object.class)) continue;
            for (Class<?> paramType : m.getParameterTypes()) {
                String pkg = paramType.getPackageName();
                assertFalse(pkg.startsWith("Suppliers.Domain"),
                    "SupplierService public method '" + m.getName()
                    + "' accepts Suppliers.Domain type '" + paramType.getSimpleName()
                    + "' - violates DTO-only boundary");
            }
        }
    }

    @Test
    void defectiveReportDto_exposesSpecId() throws Exception {
        // The record accessor should be specId(), not productId()
        Method specIdMethod = null;
        try { specIdMethod = DefectiveReportDTO.class.getMethod("specId"); }
        catch (NoSuchMethodException ignored) {}
        assertNotNull(specIdMethod,
            "DefectiveReportDTO should expose specId() - the stored value is always a specId");
    }

    @Test
    void defectiveReportDto_lacksProductIdAccessor() throws Exception {
        // Once renamed, productId() should no longer exist
        Method productIdMethod = null;
        try { productIdMethod = DefectiveReportDTO.class.getMethod("productId"); }
        catch (NoSuchMethodException ignored) {}
        assertNull(productIdMethod,
            "DefectiveReportDTO.productId() should not exist after rename to specId()");
    }

    @Test
    void defectiveLocationDto_exposesSpecId() throws Exception {
        Method specIdMethod = null;
        try { specIdMethod = DefectiveLocationDTO.class.getMethod("specId"); }
        catch (NoSuchMethodException ignored) {}
        assertNotNull(specIdMethod,
            "DefectiveLocationDTO should expose specId() - the stored value is always a specId");
    }

    @Test
    void reportDefective_storedIdMatchesSpecId() {
        int specId = addMilkBelowThreshold();
        controller.reportDefective(specId, 1, "DEFECTIVE");
        List<DefectiveReportDTO> reports = controller.getDefectiveReports(
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertEquals(1, reports.size());
        // Verify the stored ID is the specId we passed in, whatever the field is named
        assertEquals(specId, reports.get(0).specId(),
            "stored ID must equal the specId passed to reportDefective");
    }

    @Test
    void generateInventoryReport_productDtoHasNonZeroProductId() {
        int specId = addMilkBelowThreshold(); // adds 4 physical Product instances

        List<ProductDTO> report = controller.generateInventoryReport(null);
        assertEquals(1, report.size());

        ProductDTO dto = report.get(0);
        // The DTO came from a real Product; its productId must not be zero
        assertNotEquals(0, dto.productId(),
            "ProductDTO.productId must be the physical product instance ID, not 0");
    }

    @Test
    void getProduct_productDtoHasNonZeroProductId() {
        int specId = addMilkBelowThreshold(); // adds 4 physical Product instances

        ProductDTO dto = controller.getProduct(specId);
        // Spec lookup should carry the first associated product instance ID
        assertNotEquals(0, dto.productId(),
            "ProductDTO.productId must not be hardcoded 0 when product instances exist");
    }

    @Test
    void nextId_consecutiveCallsReturnSameValue() {
        // ProductRepository.nextId() should be a non-consuming peek
        ProductRepository repo = new ProductRepository(
            new StubProductInstanceDAO(), new StubProductDAO());

        int first  = repo.nextId();
        int second = repo.nextId();

        assertEquals(first, second,
            "nextId() must be a non-consuming peek - two calls without add() must return the same ID");
    }
}
