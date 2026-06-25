package tests;

import Inventory.Data.DAO.Sqlite.*;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.DTO.ProductDTO;
import Inventory.DTO.StockItemDTO;
import Inventory.Service.InventoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for the new schema - verifies that products table,
// stock_item_products mapping table, and product_specs table are all
// correctly written to and read from SQLite.
class SchemaPersistenceTest {

    private InventoryController controller;
    private InventoryService service;

    // DAOs - kept for direct DB assertions
    private SqliteProductDAO specDAO;
    private SqliteProductInstanceDAO instanceDAO;
    private SqliteStockItemDAO stockDAO;
    private SqliteStockItemProductsDAO stockProductsDAO;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection.truncateAll();

        SqliteCategoryDAO categoryDAO = new SqliteCategoryDAO();
        specDAO = new SqliteProductDAO();
        instanceDAO = new SqliteProductInstanceDAO();
        stockDAO = new SqliteStockItemDAO();
        stockProductsDAO = new SqliteStockItemProductsDAO();
        SqlitePromotionDAO promotionDAO = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defectiveReportDAO = new SqliteDefectiveReportDAO();

        ProductSpecRepository specRepo = new ProductSpecRepository(specDAO);
        ProductRepository productRepo = new ProductRepository(instanceDAO, specDAO);
        StockItemRepository stockRepo = new StockItemRepository(stockDAO, stockProductsDAO);
        CategoryRepository categoryRepo = new CategoryRepository(categoryDAO);
        PromotionRepository promotionRepo = new PromotionRepository(promotionDAO);
        DefectiveReportRepository defectiveRepo = new DefectiveReportRepository(defectiveReportDAO);

        controller = new InventoryController(
            specRepo, productRepo, stockRepo,
            categoryRepo, promotionRepo, defectiveRepo);

        service = new InventoryService(controller);
    }

    // 1. addStockItem persists to products table
    @Test
    void addStockItem_createsProductRowsInProductsTable() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        // Before stock: products table should be empty
        List<Integer> before = instanceDAO.findAll();
        assertTrue(before.isEmpty(), "products table empty before adding stock");

        // Add 5 units of stock
        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));

        // After: products table should have 5 rows
        List<Integer> after = instanceDAO.findAll();
        assertEquals(5, after.size(), "products table has one row per unit");

        // All 5 should reference specId=1
        for (Integer productId : after) {
            List<Integer> bySpec = instanceDAO.findAllBySpecId(specId);
            assertEquals(5, bySpec.size(), "all products linked to spec 1");
            break;
        }
    }

    // 2. addStockItem persists to stock_item_products mapping
    @Test
    void addStockItem_createsMappingRowsInStockItemProductsTable() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));

        // Mapping table should have 5 rows for this location
        List<Integer> mappedIds = stockProductsDAO.findByLocation(specId, "STORE", 1, 1);
        assertEquals(5, mappedIds.size(), "mapping table has one row per product-in-location");
    }

    // 3. addStockItem persists spec to product_specs table
    @Test
    void addProduct_persistsSpecToProductSpecsTable() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        ProductDTO fromDb = specDAO.findById(specId);
        assertNotNull(fromDb, "spec persisted to product_specs");
        assertEquals("Milk", fromDb.name());
        assertEquals("Tnuva", fromDb.manufacturer());
        assertEquals(3.0, fromDb.costPrice(), 0.01);
        assertEquals(6.0, fromDb.sellPrice(), 0.01);
        assertEquals(10, fromDb.minStockThreshold());
    }

    // 4. updateQuantity(+delta) adds new product rows
    @Test
    void updateQuantityPositive_addsProductRowsAndMapping() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 3, null));
        assertEquals(3, instanceDAO.findAll().size(), "3 product rows initially");
        assertEquals(3, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size());

        // Add 2 more units
        service.updateQuantity(specId, "STORE", 1, 1, 2);

        assertEquals(5, instanceDAO.findAll().size(), "5 product rows after +2");
        assertEquals(5, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size(),
            "mapping also grows to 5");
    }

    // 5. updateQuantity(-delta) removes product IDs from mapping
    @Test
    void updateQuantityNegative_removesProductIdsFromMapping() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));
        assertEquals(5, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size());

        // Remove 2 units
        service.updateQuantity(specId, "STORE", 1, 1, -2);

        assertEquals(3, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size(),
            "mapping shrinks to 3 after -2");
    }

    // 6. reportDefective removes product IDs from mapping
    @Test
    void reportDefective_removesProductIdsFromMapping() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));
        assertEquals(5, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size());

        // Report 2 defective
        service.reportDefective(specId, 2, "DEFECTIVE");

        assertEquals(3, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size(),
            "mapping shrinks after defective removal");
    }

    // 7. updateShortageReport persists spec update + product rows
    @Test
    void updateShortageReport_persistsSpecAndProductRows() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 3, null));
        int initialProducts = instanceDAO.findAll().size();

        // Simulate order arrival: 10 units at cost 4.0
        service.updateShortageReport(specId, 10, 4.0);

        // Spec should be updated in DB
        ProductDTO fromDb = specDAO.findById(specId);
        assertEquals(4.0, fromDb.costPrice(), 0.01, "costPrice persisted");
        assertEquals(13, fromDb.totalQuantity(), "totalQuantity = 3 + 10");

        // Product rows should have grown by 10
        int afterProducts = instanceDAO.findAll().size();
        assertEquals(initialProducts + 10, afterProducts,
            "products table grew by ordered quantity");
    }

    // 8. Multiple stock locations each get their own mapping
    @Test
    void multipleLocations_separateMappingEntries() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 3, null));
        service.addStockItem(new StockItemDTO(specId, "WAREHOUSE", 2, 2, 5, null));

        assertEquals(3, stockProductsDAO.findByLocation(specId, "STORE", 1, 1).size(),
            "STORE mapping has 3");
        assertEquals(5, stockProductsDAO.findByLocation(specId, "WAREHOUSE", 2, 2).size(),
            "WAREHOUSE mapping has 5");

        // Product IDs should be disjoint between locations
        List<Integer> storeIds = stockProductsDAO.findByLocation(specId, "STORE", 1, 1);
        List<Integer> warehouseIds = stockProductsDAO.findByLocation(specId, "WAREHOUSE", 2, 2);
        storeIds.removeAll(warehouseIds);
        assertEquals(3, storeIds.size(), "no overlap between location mappings");
    }

    // 9. stock_items table has correct quantity
    @Test
    void stockItemsTable_hasCorrectQuantity() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 20, null));

        List<Inventory.DTO.StockItemDTO> rows = stockDAO.findBySpecId(specId);
        assertEquals(1, rows.size());
        assertEquals(20, rows.get(0).quantity());
        assertEquals("STORE", rows.get(0).area());
        assertEquals(1, rows.get(0).shelf());
        assertEquals(1, rows.get(0).row());
    }

    // 10. stock_items quantity updated after updateQuantity
    @Test
    void stockItemsTable_quantityUpdatedAfterDelta() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 20, null));
        service.updateQuantity(specId, "STORE", 1, 1, -5);

        List<Inventory.DTO.StockItemDTO> rows = stockDAO.findBySpecId(specId);
        assertEquals(15, rows.get(0).quantity(), "stock_items quantity reflects -5 delta");
    }

    // 11. No spec duplication - one row per spec in product_specs
    @Test
    void noSpecDuplication_oneRowPerSpec() {
        int catId = service.addCategory("Dairy", 0);
        int spec1 = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));
        int spec2 = service.addProduct(
            new ProductDTO(0, 0, "Cheese", "Gal", catId, 5.0, 10.0, 5, 0));

        // Add stock for both specs
        service.addStockItem(new StockItemDTO(spec1, "STORE", 1, 1, 3, null));
        service.addStockItem(new StockItemDTO(spec2, "STORE", 2, 1, 4, null));

        List<ProductDTO> allSpecs = specDAO.findAll();
        assertEquals(2, allSpecs.size(), "product_specs has exactly 2 rows - no duplication");
    }

    // 12. product_specs totalQuantity updated via updateShortageReport
    @Test
    void productSpecs_totalQuantityUpdated() {
        int catId = service.addCategory("Dairy", 0);
        int specId = service.addProduct(
            new ProductDTO(0, 0, "Milk", "Tnuva", catId, 3.0, 6.0, 10, 0));

        service.addStockItem(new StockItemDTO(specId, "STORE", 1, 1, 5, null));

        // After addStockItem, totalQuantity is persisted to DB
        ProductDTO afterAdd = specDAO.findById(specId);
        assertEquals(5, afterAdd.totalQuantity(), "totalQuantity = 5 after addStockItem");

        // Order 10 more - updateShortageReport also persists
        service.updateShortageReport(specId, 10, 3.5);

        ProductDTO afterOrder = specDAO.findById(specId);
        assertEquals(15, afterOrder.totalQuantity(), "totalQuantity = 5 + 10");
        assertEquals(3.5, afterOrder.costPrice(), 0.01, "costPrice also updated");
    }
}
