package tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import Inventory.Domain.InventoryController;
import Inventory.Domain.Repository.*;
import Inventory.Data.DAO.Stub.*;
import Inventory.Service.InventoryService;
import Inventory.Data.PreloadData;
import Inventory.Presentation.InventoryMenu;

import java.io.*;
import java.util.Scanner;

public class MenuIntegrationTest {

    private String run(String input) throws Exception {
        InventoryController controller = new InventoryController(
            new ProductRepository(new StubProductDAO()),
            new StockItemRepository(new StubStockItemDAO()),
            new CategoryRepository(new StubCategoryDAO()),
            new PromotionRepository(new StubPromotionDAO()),
            new DefectiveReportRepository(new StubDefectiveReportDAO())
        );
        InventoryService service = new InventoryService(controller);
        PreloadData preloadData = new PreloadData(controller);
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        PrintStream original = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            new InventoryMenu(service, preloadData, scanner).run();
        } finally {
            System.setOut(original);
        }
        return capture.toString();
    }

    // ── Add product + stock + view ─────────────────────────────────────────

    @Test
    void testAddProductAndViewStock() throws Exception {
        String input = String.join("\n",
            "1",          // add product
            "Apple Juice", "FarmFresh", "Beverages", "2.5", "5.0", "10",
            "2",          // add stock to product 1
            "1", "STORE", "1", "1", "20", "",   // qty=20, no expiry
            "3",          // view stock for product 1
            "1",
            "0"           // exit
        );
        String out = run(input);
        assertTrue(out.contains("Product added with ID: 1"), "product added");
        assertTrue(out.contains("Stock added"),               "stock added");
        assertTrue(out.contains("qty=20"),                    "stock qty shown");
        assertTrue(out.contains("Total: 20"),                 "total shown");
    }

    // ── Low stock alert ────────────────────────────────────────────────────

    @Test
    void testLowStockAlert() throws Exception {
        String input = String.join("\n",
            "1",          // add product with min=10
            "Milk", "Tnuva", "Dairy", "3.0", "6.0", "10",
            "2",          // add stock qty=5 (below min)
            "1", "STORE", "1", "1", "5", "",
            "5",          // low stock alerts
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("Milk"),       "product name shown");
        assertTrue(out.contains("total=5"),    "current qty shown");
        assertTrue(out.contains("min=10"),     "min threshold shown");
    }

    // ── Order due to shortage (UC-f) ───────────────────────────────────────

    @Test
    void testOrderDueToShortage() throws Exception {
        // Product gets specId=1; supplier has agreement for specId=1
        String input = String.join("\n",
            "1",          // add product specId=1, min=10
            "Milk", "Tnuva", "Dairy", "3.0", "6.0", "10",
            "2",          // add stock qty=5 (low stock)
            "1", "STORE", "1", "1", "5", "",
            "16",         // order due to shortage
            "1",          // pick specId=1
            "y",          // confirm
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("Proposal"),        "proposal shown");
        assertTrue(out.contains("supplier="),       "supplier shown");
        assertTrue(out.contains("Order #"),         "order placed");
        assertTrue(out.contains("sent"),            "order sent");
    }

    // ── Periodic order (UC-e) ──────────────────────────────────────────────

    @Test
    void testPeriodicOrder() throws Exception {
        // Product gets specId=1; ACME (supplier 1) has agreement for specId=1
        String input = String.join("\n",
            "1",          // add product specId=1, min=10
            "Milk", "Tnuva", "Dairy", "3.0", "6.0", "10",
            "2",          // add stock qty=5 (low stock)
            "1", "STORE", "1", "1", "5", "",
            "17",         // periodic order
            "y",          // confirm
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("Proposed periodic orders"), "preview shown");
        assertTrue(out.contains("periodic order(s) sent"),   "order(s) sent");
    }

    // ── Expired stock auto-detected in option 12 ───────────────────────────

    @Test
    void testExpiredStockDetectedWithoutOption11() throws Exception {
        String input = String.join("\n",
            "1",          // add product
            "OldMilk", "Farm", "Dairy", "1.0", "2.0", "5",
            "2",          // add stock with past expiry
            "1", "STORE", "1", "1", "3", "2000-01-01",
            "12",         // locate defective items (no option 11 run first)
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("Product ID 1"), "expired product detected");
        assertTrue(out.contains("shelf=1"),      "location shown");
    }

    // ── Category add ──────────────────────────────────────────────────────

    @Test
    void testAddCategory() throws Exception {
        String input = String.join("\n",
            "6",          // add category
            "Frozen", "",  // root category
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("Category added with ID"), "category added");
    }

    // ── No low stock when stock is sufficient ─────────────────────────────

    @Test
    void testNoLowStockWhenSufficient() throws Exception {
        String input = String.join("\n",
            "1",
            "Water", "Aqua", "Beverages", "1.0", "2.0", "5",
            "2",
            "1", "STORE", "1", "1", "50", "",
            "5",          // low stock alerts
            "0"
        );
        String out = run(input);
        assertTrue(out.contains("No low stock products"), "no alerts when stock ok");
    }
}
