import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.*;
import Inventory.Service.InventoryService;
import java.time.LocalDate;
import java.util.List;

class InventoryServiceTest {

    private InventoryController controller;
    private InventoryService service;
    private Category dairy;
    private ProductSpec milkSpec;
    private Product milkProduct;

    @BeforeEach
    void setUp() {
        controller = new InventoryController();
        service = new InventoryService(controller);
        dairy = new Category("Dairy");
        milkSpec = new ProductSpec("Tnuva 3% 1L", "Tnuva", dairy, 4.5, 6.9, 10);
        milkProduct = new Product(1, milkSpec);
    }

    @Test
    void constructorRejectsNullController() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryService(null));
    }

    @Test
    void addAndGetProduct() {
        service.addProduct(milkProduct);
        assertEquals(1, service.getProduct(1).getId());
    }

    @Test
    void addStockAndGetForProduct() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));
        List<StockItem> stock = service.getStockForProduct(1);
        assertEquals(1, stock.size());
        assertEquals(20, stock.get(0).getQuantity());
    }

    @Test
    void updateQuantityReducesStock() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 2, 1, 20, null));
        service.updateQuantity(1, Area.STORE, 2, 1, -5);
        assertEquals(15, service.getStockForProduct(1).get(0).getQuantity());
    }

    @Test
    void lowStockAlertsShowUnderMin() {
        service.addProduct(milkProduct);
        service.addStockItem(new StockItem(milkSpec, Area.STORE, 1, 1, 5, null));
        assertEquals(1, service.getLowStockProducts().size());
    }

    @Test
    void promotionReducesEffectivePrice() {
        service.addProduct(milkProduct);
        Promotion promo = new Promotion(10.0,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                milkSpec, null);
        service.addPromotion(promo);
        assertEquals(6.21, service.getEffectivePrice(1), 0.001);
    }

    @Test
    void effectivePriceWithoutPromotionEqualsSellPrice() {
        service.addProduct(milkProduct);
        assertEquals(6.9, service.getEffectivePrice(1), 0.001);
    }

    @Test
    void reportDefectiveReducesStock() {
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
    void inventoryReportReturnsAllProducts() {
        service.addProduct(milkProduct);
        InventoryReport report = service.generateInventoryReport(LocalDate.now(), null);
        assertEquals(1, report.getItems().size());
    }

    @Test
    void resetClearsCatalog() {
        service.addProduct(milkProduct);
        service.reset();
        assertThrows(IllegalArgumentException.class, () -> service.getProduct(1));
    }
}
