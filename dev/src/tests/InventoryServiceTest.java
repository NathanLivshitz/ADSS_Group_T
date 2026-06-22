package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.DTO.*;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.Service.InventoryService;
import java.time.LocalDate;
import java.util.List;

class InventoryServiceTest {

    private InventoryController controller;
    private InventoryService service;
    private int dairyId;
    private int milkSpecId;

    private static InventoryController freshController() {
        return new InventoryController(
            new ProductSpecRepository(),
            new ProductRepository(new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
    }

    @BeforeEach
    void setUp() {
        controller = freshController();
        service = new InventoryService(controller);
        dairyId = service.addCategory("Dairy", 0);
        milkSpecId = service.addProduct(
            new ProductDTO(0, 0, "Tnuva 3% 1L", "Tnuva", dairyId, 4.5, 6.9, 10, 0));
    }

    @Test
    void constructorRejectsNullController() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryService(null));
    }

    @Test
    void addAndGetProduct() {
        ProductDTO p = service.getProduct(milkSpecId);
        assertEquals("Tnuva 3% 1L", p.name());
        assertEquals(milkSpecId, p.specId());
    }

    @Test
    void addStockAndGetForProduct() {
        service.addStockItem(new StockItemDTO(milkSpecId, "STORE", 2, 1, 20, null));
        List<StockItemDTO> stock = service.getStockForProduct(milkSpecId);
        assertEquals(1, stock.size());
        assertEquals(20, stock.get(0).quantity());
    }

    @Test
    void updateQuantityReducesStock() {
        service.addStockItem(new StockItemDTO(milkSpecId, "STORE", 2, 1, 20, null));
        service.updateQuantity(milkSpecId, "STORE", 2, 1, -5);
        assertEquals(15, service.getStockForProduct(milkSpecId).get(0).quantity());
    }

    @Test
    void lowStockAlertsShowUnderMin() {
        // min threshold = 10, stock = 5 -> should be low
        service.addStockItem(new StockItemDTO(milkSpecId, "STORE", 1, 1, 5, null));
        assertEquals(1, service.getLowStockProducts().size());
    }

    @Test
    void promotionReducesEffectivePrice() {
        service.addPromotion(new PromotionDTO(
            10.0,
            LocalDate.now().minusDays(1).toString(),
            LocalDate.now().plusDays(5).toString(),
            milkSpecId, 0, null, null));
        assertEquals(6.21, service.getEffectivePrice(milkSpecId), 0.001);
    }

    @Test
    void effectivePriceWithoutPromotionEqualsSellPrice() {
        assertEquals(6.9, service.getEffectivePrice(milkSpecId), 0.001);
    }

    @Test
    void reportDefectiveReducesStock() {
        service.addStockItem(new StockItemDTO(milkSpecId, "STORE", 1, 1, 10, null));
        service.reportDefective(milkSpecId, 3, "DEFECTIVE");
        assertEquals(7, service.getStockForProduct(milkSpecId).get(0).quantity());
    }

    @Test
    void reportDefectiveRejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> service.reportDefective(milkSpecId, -1, "DEFECTIVE"));
    }

    @Test
    void inventoryReportReturnsAllProducts() {
        List<ProductDTO> report = service.generateInventoryReport(null);
        assertEquals(1, report.size());
        assertEquals(milkSpecId, report.get(0).specId());
    }

    @Test
    void resetClearsCatalog() {
        service.reset();
        final int id = milkSpecId;
        assertThrows(IllegalArgumentException.class, () -> service.getProduct(id));
    }
}
