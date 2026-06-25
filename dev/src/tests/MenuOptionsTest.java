package tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.Service.InventoryService;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;
import Suppliers.Domain.*;
import Suppliers.Service.SupplierService;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Scanner;

// Menu-driven tests covering all 17 options via simulated stdin.
class MenuOptionsTest {

    // test harness
    private static InventoryController freshController() {
        return new InventoryController(
            new ProductSpecRepository(new StubProductDAO()),
            new ProductRepository(new StubProductInstanceDAO(), new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO(), new StubStockItemProductsDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
    }

    private String run(String input) throws Exception {
        InventoryController ctrl = freshController();
        return run(input, ctrl, new InventoryService(ctrl));
    }

    private String run(String input, InventoryController ctrl, InventoryService svc) throws Exception {
        PreloadData pd = new PreloadData(ctrl);
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream orig = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
        try {
            new InventoryMenu(svc, pd, scanner).run();
        } finally {
            System.setOut(orig);
        }
        return buf.toString();
    }

    /** Supplier seeded with an agreement for the given specId on every day of the week. */
    private SupplierService seededSupplierService(int specId) {
        SupplierController sc = new SupplierController();
        Supplier acme = sc.addSupplier(1, "ACME");
        sc.addAgreement(new SupplyAgreement(acme, specId, 1, 5.00));
        for (DayOfWeek day : DayOfWeek.values())
            sc.addSchedule(new DeliverySchedule(acme, day));
        return new SupplierService(sc);
    }

    // helpers to build common prefix inputs
    /** Inputs to add product "Milk" (cost=3.0, sell=6.9, min=10), returns specId=1 with stub DAO. */
    private static final String ADD_MILK =
        "1\nMilk\nTnuva\nDairy\n3.0\n6.9\n10\n";

    /** Inputs to add 20 units of specId=1 in STORE shelf=1 row=1 (above min threshold). */
    private static final String ADD_STOCK_ABOVE_MIN =
        "2\n1\nSTORE\n1\n1\n20\n\n";

    /** Inputs to add 5 units of specId=1 in STORE shelf=1 row=1 (below min threshold). */
    private static final String ADD_STOCK_BELOW_MIN =
        "2\n1\nSTORE\n1\n1\n5\n\n";

    // Option 1: Add product
    @Test
    void menu1_write_addProductConfirmed() throws Exception {
        String out = run(ADD_MILK + "0\n");
        assertTrue(out.contains("Product spec added with ID: 1"), "spec ID assigned");
    }

    @Test
    void menu1_write_addProductLosslossWarning() throws Exception {
        // sell < cost -> warning
        String out = run("1\nCheapMilk\nTnuva\nDairy\n9.0\n5.0\n10\n0\n");
        assertTrue(out.contains("Warning: sell price"), "loss-leader warning shown");
    }

    @Test
    void menu1_write_addProductInvalidCategoryReturns() throws Exception {
        // non-existent parent category name path triggers failure
        // empty name segment returns "Invalid category path."
        String out = run("1\nMilk\nTnuva\n,bad\n0\n");
        assertTrue(out.contains("Invalid category path."), "invalid path rejected");
    }

    // Option 2: Add stock
    @Test
    void menu2_write_addStockConfirmed() throws Exception {
        String out = run(ADD_MILK + ADD_STOCK_ABOVE_MIN + "0\n");
        assertTrue(out.contains("Stock added"), "stock added confirmation");
    }

    @Test
    void menu2_write_addExpiredStockWarning() throws Exception {
        String out = run(ADD_MILK + "2\n1\nSTORE\n1\n1\n3\n2000-01-01\n0\n");
        assertTrue(out.contains("Stock added"),              "stock still added");
        assertTrue(out.contains("Warning: added 3 expired"), "expired warning shown");
    }

    // Option 3: View product stock
    @Test
    void menu3_read_stockLocationsAndTotals() throws Exception {
        String input = ADD_MILK
            + ADD_STOCK_ABOVE_MIN              // 20 in STORE shelf=1 row=1
            + "2\n1\nWAREHOUSE\n3\n2\n50\n\n" // 50 in WAREHOUSE shelf=3 row=2
            + "3\n1\n"                          // view stock for specId=1
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("STORE"),          "store location shown");
        assertTrue(out.contains("WAREHOUSE"),      "warehouse location shown");
        assertTrue(out.contains("qty=20"),         "store qty shown");
        assertTrue(out.contains("qty=50"),         "warehouse qty shown");
        assertTrue(out.contains("Store: 20"),      "store total shown");
        assertTrue(out.contains("Warehouse: 50"),  "warehouse total shown");
        assertTrue(out.contains("Total: 70"),      "grand total shown");
    }

    @Test
    void menu3_read_noStockShowsNotFound() throws Exception {
        String out = run(ADD_MILK + "3\n1\n0\n");
        assertTrue(out.contains("No stock found"), "no stock message shown");
    }

    // Option 4: Update stock
    @Test
    void menu4_write_positiveUpdateIncreasesQty() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units
            + "4\n1\nSTORE\n1\n1\n10\n"               // +10
            + "3\n1\n"                                 // verify
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Stock updated"), "update confirmed");
        assertTrue(out.contains("qty=30"),        "quantity increased to 30");
    }

    @Test
    void menu4_write_negativeUpdateDecreasesQty() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units
            + "4\n1\nSTORE\n1\n1\n-5\n"               // -5
            + "3\n1\n"                                 // verify
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Stock updated"), "update confirmed");
        assertTrue(out.contains("qty=15"),        "quantity decreased to 15");
    }

    @Test
    void menu4_write_overdraftShowsError() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units
            + "4\n1\nSTORE\n1\n1\n-100\n"             // try to remove 100
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Error:"), "overdraft rejected with error");
    }

    // Option 5: Low stock alerts
    @Test
    void menu5_read_lowStockAlertShown() throws Exception {
        String input = ADD_MILK + ADD_STOCK_BELOW_MIN + "5\n0\n"; // 5 units, min=10
        String out = run(input);
        assertTrue(out.contains("Milk"),    "product name shown");
        assertTrue(out.contains("total=5"), "current qty shown");
        assertTrue(out.contains("min=10"),  "min threshold shown");
    }

    @Test
    void menu5_read_noAlertWhenAboveMin() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN + "5\n0\n"; // 20 units, min=10
        String out = run(input);
        assertTrue(out.contains("No low stock products"), "no alerts shown");
    }

    // Option 6: Add category
    @Test
    void menu6_write_rootCategoryAdded() throws Exception {
        String out = run("6\nFrozen\n\n0\n");
        assertTrue(out.contains("Category added with ID"), "category ID assigned");
    }

    @Test
    void menu6_write_childCategoryAdded() throws Exception {
        String out = run("6\nFood\n\n6\nDairy\nFood\n0\n");
        assertTrue(out.contains("Category added with ID: 1"), "root ID=1");
        assertTrue(out.contains("Category added with ID: 2"), "child ID=2");
    }

    @Test
    void menu6_write_childWithUnknownParentRejects() throws Exception {
        String out = run("6\nDairy\nNonexistent\n0\n");
        assertTrue(out.contains("Parent category not found"), "unknown parent rejected");
    }

    // Option 7: Add promotion
    @Test
    void menu7_write_productPromotionAdded() throws Exception {
        String today = LocalDate.now().minusDays(1).toString();
        String future = LocalDate.now().plusDays(30).toString();
        String input = ADD_MILK
            + "7\n10.0\n" + today + "\n" + future + "\nPRODUCT\n1\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Promotion added"), "promotion confirmed");
    }

    @Test
    void menu7_write_categoryPromotionAdded() throws Exception {
        String today = LocalDate.now().minusDays(1).toString();
        String future = LocalDate.now().plusDays(30).toString();
        String input = ADD_MILK  // creates category "Dairy" as side effect
            + "7\n15.0\n" + today + "\n" + future + "\nCATEGORY\nDairy\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Promotion added"), "category promotion confirmed");
    }

    // Option 8: View active promotions
    @Test
    void menu8_read_activePromotionShown() throws Exception {
        String today = LocalDate.now().minusDays(1).toString();
        String future = LocalDate.now().plusDays(30).toString();
        String input = ADD_MILK
            + "7\n20.0\n" + today + "\n" + future + "\nPRODUCT\n1\n"
            + "8\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("20% off"),  "discount shown");
        assertTrue(out.contains("Milk"),     "product name shown");
    }

    @Test
    void menu8_read_expiredPromotionNotShown() throws Exception {
        String input = ADD_MILK
            + "7\n10.0\n2020-01-01\n2020-12-31\nPRODUCT\n1\n"
            + "8\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("No active promotions"), "expired promo excluded");
    }

    // Option 9: Check effective price
    @Test
    void menu9_read_priceWithoutPromoEqualsSellPrice() throws Exception {
        String out = run(ADD_MILK + "9\n1\n0\n");
        assertTrue(out.contains("Effective price: 6.90"), "sell price returned");
    }

    @Test
    void menu9_read_priceWithPromoIsDiscounted() throws Exception {
        String today = LocalDate.now().minusDays(1).toString();
        String future = LocalDate.now().plusDays(30).toString();
        String input = ADD_MILK
            + "7\n10.0\n" + today + "\n" + future + "\nPRODUCT\n1\n"
            + "9\n1\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Effective price: 6.21"), "10% discount applied: 6.9*0.9=6.21");
    }

    // Option 10: Report defective
    @Test
    void menu10_write_defectiveReducesStock() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units
            + "10\n1\n"                                 // report 1 defective for specId=1
            + "3\n1\n"                                  // verify stock
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Defective item reported"), "report confirmed");
        assertTrue(out.contains("qty=19"),                  "stock reduced by 1");
    }

    @Test
    void menu10_write_defectiveWithNoStockShowsError() throws Exception {
        // reporting defective with no stock -> should error (not enough stock to remove)
        String out = run(ADD_MILK + "10\n1\n0\n");
        assertTrue(out.contains("Error:"), "error shown when no stock to remove");
    }

    // Option 11: Remove expired stock
    @Test
    void menu11_write_expiredStockRemoved() throws Exception {
        String input = ADD_MILK
            + "2\n1\nSTORE\n1\n1\n5\n2000-01-01\n"  // 5 expired units
            + "11\n"                                   // remove expired
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("5 expired items removed"), "expired count shown");
    }

    @Test
    void menu11_write_noExpiredStockReportsZero() throws Exception {
        String out = run(ADD_MILK + ADD_STOCK_ABOVE_MIN + "11\n0\n");
        assertTrue(out.contains("No expired stock found"), "zero expired message");
    }

    // Option 12: Locate defective items
    @Test
    void menu12_read_defectiveItemLocationShown() throws Exception {
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN
            + "10\n1\n"    // report defective -> creates report for specId=1
            + "12\n"       // locate
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Product spec ID 1"), "product listed");
        assertTrue(out.contains("STORE"),             "location area shown");
        assertTrue(out.contains("shelf=1"),           "shelf shown");
    }

    @Test
    void menu12_read_expiredStockAppears() throws Exception {
        // expired items show up in locate-defective without running option 11 first
        String input = ADD_MILK
            + "2\n1\nSTORE\n1\n1\n3\n2000-01-01\n"
            + "12\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Product spec ID 1"), "expired product listed");
    }

    @Test
    void menu12_read_noDefectivesShowsEmpty() throws Exception {
        String out = run(ADD_MILK + ADD_STOCK_ABOVE_MIN + "12\n0\n");
        assertTrue(out.contains("No defective items found"), "empty message shown");
    }

    // Option 13: Defective report by dates
    @Test
    void menu13_read_reportInRange() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow  = LocalDate.now().plusDays(1);
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN
            + "10\n1\n"    // report defective
            + "13\n" + yesterday + "\n" + tomorrow + "\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("spec ID=1"),   "spec shown");
        assertTrue(out.contains("qty=1"),       "qty shown");
        assertTrue(out.contains("DEFECTIVE"),   "reason shown");
    }

    @Test
    void menu13_read_reportOutOfRangeIsEmpty() throws Exception {
        LocalDate past1 = LocalDate.now().minusDays(10);
        LocalDate past2 = LocalDate.now().minusDays(5);
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN
            + "10\n1\n"
            + "13\n" + past1 + "\n" + past2 + "\n"  // range before today
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("No defective reports"), "out-of-range returns empty");
    }

    // Option 14: Generate inventory report
    @Test
    void menu14_read_allProductsNoFilter() throws Exception {
        String input = ADD_MILK
            + "1\nBamba\nOsem\nSnacks\n2.0\n4.0\n5\n"  // second product
            + "14\nn\n"                                  // no filter
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Milk"),         "first product shown");
        assertTrue(out.contains("Bamba"),        "second product shown");
        assertTrue(out.contains("(2 items)"),    "count correct");
    }

    @Test
    void menu14_read_filterByCategory() throws Exception {
        String input = ADD_MILK
            + "1\nBamba\nOsem\nSnacks\n2.0\n4.0\n5\n"
            + "14\ny\nDairy\n\n"                        // filter by Dairy
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("Milk"),          "dairy product shown");
        assertFalse(out.contains("[specId=2]"),   "snacks product excluded");
        assertTrue(out.contains("(1 items)"),     "one item after filter");
    }

    @Test
    void menu14_read_statusLabels() throws Exception {
        // Stock at threshold -> OK; below -> LOW; none -> OUT
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units, min=10 -> OK
            + "1\nBamba\nOsem\nSnacks\n2.0\n4.0\n5\n"  // Bamba min=5
            + "2\n2\nSTORE\n1\n1\n3\n\n"               // 3 units, min=5 -> LOW
            + "14\nn\n"
            + "0\n";
        String out = run(input);
        assertTrue(out.contains("[OK]"),  "OK status shown");
        assertTrue(out.contains("[LOW]"), "LOW status shown");
    }

    // Option 15: Load test data
    @Test
    void menu15_write_testDataLoaded() throws Exception {
        String out = run("15\n14\nn\n0\n");
        assertTrue(out.contains("Test data loaded"),  "load confirmed");
        // preload creates 5 products
        assertTrue(out.contains("(5 items)"), "5 preloaded products shown");
    }

    @Test
    void menu15_write_doubleLoadDoesNotDuplicate() throws Exception {
        // loading twice should still yield 5 products (reset before reload)
        String out = run("15\n15\n14\nn\n0\n");
        assertTrue(out.contains("(5 items)"), "still 5 products after double load");
    }

    // Option 16: Order due to shortage
    @Test
    void menu16_write_orderPlacedAndConfirmed() throws Exception {
        InventoryController ctrl = freshController();
        InventoryService svc = new InventoryService(ctrl, seededSupplierService(1));
        String input = ADD_MILK + ADD_STOCK_BELOW_MIN  // specId=1, 5 units < min=10
            + "16\n1\ny\n"                             // enter specId=1, confirm
            + "0\n";
        String out = run(input, ctrl, svc);
        assertTrue(out.contains("Proposal"),  "proposal shown");
        assertTrue(out.contains("supplier="), "supplier shown");
        assertTrue(out.contains("Order #"),   "order number shown");
        assertTrue(out.contains("sent"),      "order placed");
    }

    @Test
    void menu16_write_cancelledDoesNotPlace() throws Exception {
        InventoryController ctrl = freshController();
        InventoryService svc = new InventoryService(ctrl, seededSupplierService(1));
        String input = ADD_MILK + ADD_STOCK_BELOW_MIN
            + "16\n1\nn\n"  // cancel
            + "0\n";
        String out = run(input, ctrl, svc);
        assertTrue(out.contains("Proposal"),   "proposal still shown");
        assertTrue(out.contains("Cancelled"),  "cancellation confirmed");
        assertFalse(out.contains("Order #"),   "no order placed");
    }

    @Test
    void menu16_read_noLowStockReturnsEarly() throws Exception {
        String out = run(ADD_MILK + ADD_STOCK_ABOVE_MIN + "16\n0\n");
        assertTrue(out.contains("No low-stock products"), "early return when stock ok");
    }

    // Option 17: Periodic order from supplier
    @Test
    void menu17_write_periodicOrderPreviewAndSend() throws Exception {
        InventoryController ctrl = freshController();
        InventoryService svc = new InventoryService(ctrl, seededSupplierService(1));
        String input = ADD_MILK + ADD_STOCK_BELOW_MIN  // specId=1 low stock
            + "17\ny\n"                                // approve periodic order
            + "0\n";
        String out = run(input, ctrl, svc);
        assertTrue(out.contains("Proposed periodic orders"), "preview shown");
        assertTrue(out.contains("periodic order(s) sent"),   "orders sent");
    }

    @Test
    void menu17_write_periodicOrderCancelled() throws Exception {
        InventoryController ctrl = freshController();
        InventoryService svc = new InventoryService(ctrl, seededSupplierService(1));
        String input = ADD_MILK + ADD_STOCK_BELOW_MIN
            + "17\nn\n"   // cancel
            + "0\n";
        String out = run(input, ctrl, svc);
        assertTrue(out.contains("Proposed periodic orders"), "preview still shown");
        assertTrue(out.contains("Cancelled"),                "cancellation confirmed");
        assertFalse(out.contains("periodic order(s) sent"),  "no order placed");
    }

    @Test
    void menu17_read_noSuppliersShowsMessage() throws Exception {
        // default InventoryService has no suppliers with schedules
        String out = run(ADD_MILK + ADD_STOCK_BELOW_MIN + "17\n0\n");
        assertTrue(out.contains("No suppliers with fixed delivery schedules"),
            "message when no suppliers configured");
    }

    @Test
    void menu17_read_stockAboveMinNoOrderNeeded() throws Exception {
        InventoryController ctrl = freshController();
        InventoryService svc = new InventoryService(ctrl, seededSupplierService(1));
        // stock is above min -> nothing to order
        String input = ADD_MILK + ADD_STOCK_ABOVE_MIN  // 20 units, min=10
            + "17\n0\n";
        String out = run(input, ctrl, svc);
        assertTrue(out.contains("No periodic orders needed right now"),
            "nothing to order when above threshold");
    }
}
