package tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Data.DAO.Stub.*;
import Inventory.DTO.ProductDTO;
import Inventory.DTO.StockItemDTO;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;

public class ProductSpecStockModelTest {

    @Test
    void addProductRegistersSpecAndAddStockCreatesRealProducts() {
        ProductRepository productRepo = new ProductRepository(new StubProductInstanceDAO(), new StubProductDAO());
        StockItemRepository stockRepo = new StockItemRepository(new StubStockItemDAO(), new StubStockItemProductsDAO());
        CategoryRepository categoryRepo = new CategoryRepository(new StubCategoryDAO());

        InventoryController controller = new InventoryController(
                new ProductSpecRepository(new StubProductDAO()),
                productRepo,
                stockRepo,
                categoryRepo,
                new PromotionRepository(new StubPromotionDAO()),
                new DefectiveReportRepository(new StubDefectiveReportDAO())
        );

        int categoryId = controller.addCategory("Dairy", 0);
        int specId = controller.addProduct(new ProductDTO(0, 0, "Milk", "Tnuva",
                categoryId, 3.0, 6.0, 10, 0));

        assertEquals(1, specId);
        assertTrue(productRepo.findAll().isEmpty(), "Adding a spec must not create real products");

        controller.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 3, null));

        assertEquals(3, productRepo.findAll().size(), "Adding stock creates one product entity per unit");
        assertEquals(3, stockRepo.findAll().get(0).getProductIds().size());
        assertEquals(3, controller.getProduct(specId).totalQuantity());
    }
}
