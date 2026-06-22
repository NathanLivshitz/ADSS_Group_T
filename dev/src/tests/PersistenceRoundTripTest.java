package tests;

import Inventory.Data.DAO.Sqlite.*;
import Inventory.Data.DB.DatabaseConnection;
import Inventory.Domain.*;
import Inventory.Domain.Repository.*;
import Inventory.DTO.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Sqlite persistence round-trip tests; truncateAll() in @BeforeEach for isolation.
public class PersistenceRoundTripTest {

    @BeforeEach
    void clean() throws SQLException {
        DatabaseConnection.truncateAll();
    }

    // ── 1. CategoryDAO: insert + findAll round-trip ───────────────────────────

    @Test
    void categoryDaoRoundTrip() {
        SqliteCategoryDAO dao = new SqliteCategoryDAO();
        CategoryDTO root = new CategoryDTO(10, "Food", 0);
        CategoryDTO child = new CategoryDTO(20, "Dairy", 10);
        dao.insert(root);
        dao.insert(child);

        List<CategoryDTO> all = dao.findAll();
        assertEquals(2, all.size());
        // ordered by category_id ASC - parent comes first
        CategoryDTO first = all.get(0);
        assertEquals(10, first.categoryId());
        assertEquals("Food", first.categoryName());
        assertEquals(0, first.parentCategoryId());

        CategoryDTO second = all.get(1);
        assertEquals(20, second.categoryId());
        assertEquals("Dairy", second.categoryName());
        assertEquals(10, second.parentCategoryId());
    }

    // ── 2. ProductDAO: insert + findById + findAll round-trip ────────────────

    @Test
    void productDaoRoundTrip() {
        // category row required for FK sanity (not enforced by sqlite in this schema but good practice)
        SqliteCategoryDAO catDao = new SqliteCategoryDAO();
        catDao.insert(new CategoryDTO(1, "Root", 0));

        SqliteProductDAO dao = new SqliteProductDAO();
        ProductDTO dto = new ProductDTO(0, 5, "Milk 3%", "Tnuva", 1, 4.50, 6.90, 15, 0);
        dao.insert(dto);

        ProductDTO found = dao.findById(0);
        assertNotNull(found);
        assertEquals(5, found.specId());
        assertEquals("Milk 3%", found.name());
        assertEquals("Tnuva", found.manufacturer());
        assertEquals(1, found.categoryId());
        assertEquals(4.50, found.costPrice(), 0.001);
        assertEquals(6.90, found.sellPrice(), 0.001);
        assertEquals(15, found.minStockThreshold());

        List<ProductDTO> all = dao.findAll();
        assertEquals(1, all.size());
    }

    // ── 3. StockItemDAO: insert + findBySpecId round-trip (with null expiry) ─

    @Test
    void stockItemNullExpiryRoundTrip() {
        SqliteStockItemDAO dao = new SqliteStockItemDAO();
        StockItemDTO dto = new StockItemDTO(7, "STORE", 2, 3, 10, null);
        dao.insert(dto);

        List<StockItemDTO> items = dao.findBySpecId(7);
        assertEquals(1, items.size());
        StockItemDTO back = items.get(0);
        assertEquals(7, back.specId());
        assertEquals("STORE", back.area());
        assertEquals(2, back.shelf());
        assertEquals(3, back.row());
        assertEquals(10, back.quantity());
        assertNull(back.expiryDate(), "expiry should round-trip as null");
    }

    // ── 4. PromotionDAO: insert + findAll round-trip ─────────────────────────

    @Test
    void promotionDaoRoundTrip() {
        SqlitePromotionDAO dao = new SqlitePromotionDAO();
        PromotionDTO dto = new PromotionDTO(
            15.0, "2025-01-01", "2025-12-31",
            5, 0, "Milk 3%", null
        );
        dao.insert(dto);

        List<PromotionDTO> all = dao.findAll();
        assertEquals(1, all.size());
        PromotionDTO back = all.get(0);
        assertEquals(15.0, back.discountPercent(), 0.001);
        assertEquals("2025-01-01", back.startDate());
        assertEquals("2025-12-31", back.endDate());
        assertEquals(5, back.targetSpecId());
        assertEquals(0, back.targetCategoryId());
        assertEquals("Milk 3%", back.targetProductName());
        assertNull(back.targetCategoryName());
    }

    // ── 5. DefectiveReportDAO: insert + findAll round-trip ───────────────────

    @Test
    void defectiveReportDaoRoundTrip() {
        SqliteDefectiveReportDAO dao = new SqliteDefectiveReportDAO();
        DefectiveReportDTO dto = new DefectiveReportDTO(3, 2, "DEFECTIVE", "2025-06-01");
        dao.insert(dto);

        List<DefectiveReportDTO> all = dao.findAll();
        assertEquals(1, all.size());
        DefectiveReportDTO back = all.get(0);
        assertEquals(3, back.productId());
        assertEquals(2, back.quantity());
        assertEquals("DEFECTIVE", back.reason());
        assertEquals("2025-06-01", back.reportDate());
    }

    // ── 6. Categories ordered parent-before-child after hydrate ──────────────

    @Test
    void categoriesParentBeforeChildAfterHydrate() {
        SqliteCategoryDAO dao = new SqliteCategoryDAO();
        // insert in ascending id order - DAOs return ordered by id ASC
        dao.insert(new CategoryDTO(1, "Food", 0));
        dao.insert(new CategoryDTO(2, "Dairy", 1));
        dao.insert(new CategoryDTO(3, "Milk", 2));

        CategoryRepository repo = new CategoryRepository(dao);
        repo.hydrate();

        Category food = repo.findById(1);
        Category dairy = repo.findById(2);
        Category milk = repo.findById(3);

        assertNotNull(food);
        assertNull(food.getParent(), "root has no parent");
        assertNotNull(dairy);
        assertEquals(food, dairy.getParent());
        assertNotNull(milk);
        assertEquals(dairy, milk.getParent());
    }

    // ── 7. Full cycle: write via controller, hydrate fresh repos, assert state ─

    @Test
    void fullCycleControllerHydrate() throws SQLException {
        // -- first set: create via Sqlite DAOs / repositories -----------------
        SqliteCategoryDAO catDao1 = new SqliteCategoryDAO();
        SqliteProductDAO prodDao1 = new SqliteProductDAO();
        SqliteStockItemDAO stockDao1 = new SqliteStockItemDAO();
        SqlitePromotionDAO promoDao1 = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defDao1 = new SqliteDefectiveReportDAO();

        CategoryRepository catRepo1 = new CategoryRepository(catDao1);
        ProductSpecRepository specRepo1 = new ProductSpecRepository();
        ProductRepository productRepo1 = new ProductRepository(prodDao1);
        StockItemRepository stockRepo1 = new StockItemRepository(stockDao1);
        PromotionRepository promoRepo1 = new PromotionRepository(promoDao1);
        DefectiveReportRepository defRepo1 = new DefectiveReportRepository(defDao1);

        InventoryController ctrl1 = new InventoryController(
            specRepo1, productRepo1, stockRepo1, catRepo1, promoRepo1, defRepo1);

        // add 2 categories (parent + child)
        int rootId = ctrl1.addCategory("Food", 0);
        int subId  = ctrl1.addCategory("Dairy", rootId);

        // add 1 product spec in Dairy
        int specId = ctrl1.addProduct(new ProductDTO(0, 0, "Milk 3%", "Tnuva", subId, 4.50, 6.90, 15, 0));

        // add stock with expiry
        ctrl1.addStockItem(new StockItemDTO(specId, "WAREHOUSE", 1, 1, 20, "2026-12-31"));

        // add promotion targeting the spec
        ctrl1.addPromotion(new PromotionDTO(10.0, "2025-01-01", "2099-12-31", specId, 0, "Milk 3%", null));

        // add a defective report
        ctrl1.reportDefective(specId, 2, "DEFECTIVE");

        // -- second set: fresh repos hydrated from DB -------------------------
        SqliteCategoryDAO catDao2 = new SqliteCategoryDAO();
        SqliteProductDAO prodDao2 = new SqliteProductDAO();
        SqliteStockItemDAO stockDao2 = new SqliteStockItemDAO();
        SqlitePromotionDAO promoDao2 = new SqlitePromotionDAO();
        SqliteDefectiveReportDAO defDao2 = new SqliteDefectiveReportDAO();

        CategoryRepository catRepo2 = new CategoryRepository(catDao2);
        catRepo2.hydrate();

        ProductSpecRepository specRepo2 = new ProductSpecRepository();
        specRepo2.hydrate(prodDao2, catDao2, catRepo2.getAllCategoriesMap());

        StockItemRepository stockRepo2 = new StockItemRepository(stockDao2);
        stockRepo2.hydrate(specRepo2);

        PromotionRepository promoRepo2 = new PromotionRepository(promoDao2);
        promoRepo2.hydrate(specRepo2, catRepo2);

        DefectiveReportRepository defRepo2 = new DefectiveReportRepository(defDao2);
        defRepo2.hydrate();

        // -- assertions -------------------------------------------------------

        // 2 categories present
        List<CategoryDTO> cats = catDao2.findAll();
        assertEquals(2, cats.size(), "should have 2 categories after hydrate");

        // same spec exists with same name and prices
        ProductSpec rehydratedSpec = specRepo2.findById(specId);
        assertNotNull(rehydratedSpec, "spec must exist after hydrate");
        assertEquals("Milk 3%", rehydratedSpec.getName());
        assertEquals(6.90, rehydratedSpec.getSellPrice(), 0.001);

        // persisted row reflects the defective removal (write-through on update)
        List<StockItemDTO> persistedStock = stockDao2.findBySpecId(specId);
        assertEquals(1, persistedStock.size(), "one stock_items row exists");
        assertEquals(18, persistedStock.get(0).quantity(), "quantity reflects the persisted removal (20 - 2)");

        // promotion round-tripped
        List<Promotion> promos = promoRepo2.findAll();
        assertEquals(1, promos.size());
        assertEquals(10.0, promos.get(0).getDiscountPercent(), 0.001);

        // defective report round-tripped
        List<DefectiveReport> defects = defRepo2.findAll();
        assertEquals(1, defects.size());
        assertEquals(specId, defects.get(0).getProductId());
        assertEquals(2, defects.get(0).getQuantity());
    }

    // ── 8. StockItemDAO: item with explicit expiry date round-trips correctly ─

    @Test
    void stockItemWithExpiryRoundTrip() {
        SqliteStockItemDAO dao = new SqliteStockItemDAO();
        StockItemDTO dto = new StockItemDTO(9, "WAREHOUSE", 3, 1, 5, "2027-06-15");
        dao.insert(dto);

        List<StockItemDTO> all = dao.findAll();
        assertEquals(1, all.size());
        assertEquals("2027-06-15", all.get(0).expiryDate());
        assertEquals(5, all.get(0).quantity());
    }
}
