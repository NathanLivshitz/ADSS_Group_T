package Inventory.Presentation;

import Inventory.Domain.*;
import Inventory.Data.PreloadData;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InventoryMenu {
    private final InventoryController controller;
    private final PreloadData preloadData;
    private final Scanner scanner;

    public InventoryMenu(InventoryController controller, PreloadData preloadData, Scanner scanner) {
        this.controller = controller;
        this.preloadData = preloadData;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":  addProduct(); break;
                    case "2":  addStockItem(); break;
                    case "3":  viewProductStock(); break;
                    case "4":  updateStock(); break;
                    case "5":  lowStockAlerts(); break;
                    case "6":  addCategory(); break;
                    case "7":  addPromotion(); break;
                    case "8":  viewActivePromotions(); break;
                    case "9":  checkEffectivePrice(); break;
                    case "10": reportDefective(); break;
                    case "11": removeExpiredStock(); break;
                    case "12": locateDefectiveItems(); break;
                    case "13": defectiveReportByDates(); break;
                    case "14": generateInventoryReport(); break;
                    case "15": loadTestData(); break;
                    case "0":  running = false; break;
                    default:   System.out.println("Invalid option."); break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Inventory Management ===");
        System.out.println("1.  Add product");
        System.out.println("2.  Add stock to product");
        System.out.println("3.  View product locations & quantities");
        System.out.println("4.  Update stock");
        System.out.println("5.  Low stock alerts");
        System.out.println("6.  Add category");
        System.out.println("7.  Add promotion");
        System.out.println("8.  View active promotions");
        System.out.println("9.  Check effective price");
        System.out.println("10. Report defective item");
        System.out.println("11. Remove expired stock");
        System.out.println("12. Locate defective items");
        System.out.println("13. Defective report by dates");
        System.out.println("14. Generate inventory report");
        System.out.println("15. Load test data");
        System.out.println("0.  Exit");
        System.out.print("Choose: ");
    }

    // INV-1
    private void addProduct() {
        System.out.print("Product ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Manufacturer: ");
        String manufacturer = scanner.nextLine().trim();
        System.out.print("Category path (c1,c2,c3): ");
        String catPath = scanner.nextLine().trim();
        Category category = findOrCreateCategoryPath(catPath);
        if (category == null) {
            System.out.println("Invalid category path.");
            return;
        }
        System.out.print("Cost price: ");
        double costPrice = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Sell price: ");
        double sellPrice = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Min stock threshold: ");
        int minStock = Integer.parseInt(scanner.nextLine().trim());

        ProductSpec spec = new ProductSpec(name, manufacturer, category, costPrice, sellPrice, minStock);
        Product product = new Product(id, spec);
        controller.addProduct(product);
        System.out.println("Product added.");
    }

    // INV-2
    private void addStockItem() {
        System.out.print("Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        Product product = findProduct(productId);
        if (product == null) return;

        System.out.print("Area (STORE/WAREHOUSE): ");
        Area area = Area.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Shelf number: ");
        int shelf = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Row number: ");
        int row = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Expiry date (YYYY-MM-DD or empty): ");
        String expiryStr = scanner.nextLine().trim();
        LocalDate expiry = expiryStr.isEmpty() ? null : LocalDate.parse(expiryStr);

        StockItem item = new StockItem(product.getSpec(), area, shelf, row, qty, expiry);
        controller.addStockItem(item);
        System.out.println("Stock added.");
    }

    // INV-2
    private void viewProductStock() {
        System.out.print("Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());

        List<StockItem> stock = controller.getStockForProduct(productId);
        if (stock.isEmpty()) {
            System.out.println("No stock found.");
            return;
        }

        int storeQty = 0, warehouseQty = 0;
        for (StockItem si : stock) {
            System.out.printf("  %s shelf=%d row=%d qty=%d expiry=%s%n",
                    si.getArea(), si.getShelfNumber(), si.getRowNumber(),
                    si.getQuantity(), si.getExpiryDate() != null ? si.getExpiryDate() : "N/A");
            if (si.getArea() == Area.STORE) storeQty += si.getQuantity();
            else warehouseQty += si.getQuantity();
        }
        System.out.printf("Store: %d | Warehouse: %d | Total: %d%n",
                storeQty, warehouseQty, storeQty + warehouseQty);
    }

    // INV-10
    private void updateStock() {
        System.out.print("Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Area (STORE/WAREHOUSE): ");
        Area area = Area.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Shelf number: ");
        int shelf = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Row number: ");
        int row = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Delta (positive to add, negative to remove): ");
        int delta = Integer.parseInt(scanner.nextLine().trim());

        controller.updateQuantity(productId, area, shelf, row, delta);
        System.out.println("Stock updated.");
    }

    // INV-3
    private void lowStockAlerts() {
        List<Product> low = controller.getLowStockProducts();
        if (low.isEmpty()) {
            System.out.println("No low stock products.");
            return;
        }
        for (Product p : low) {
            ProductSpec spec = p.getSpec();
            List<StockItem> stock = controller.getStockForProduct(p.getId());
            int total = 0;
            for (StockItem si : stock) { total += si.getQuantity(); }
            System.out.printf("  [ID=%d] %s (%s) — total=%d, min=%d%n",
                    p.getId(), spec.getName(), spec.getManufacturer(),
                    total, spec.getMinStockThreshold());
        }
    }

    // INV-4
    private void addCategory() {
        System.out.print("Category name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Parent category name (or empty for root): ");
        String parentName = scanner.nextLine().trim();

        Category parent = null;
        if (!parentName.isEmpty()) {
            parent = findCategory(parentName);
            if (parent == null) {
                System.out.println("Parent category not found.");
                return;
            }
        }

        Category category = new Category(name, parent);
        if (parent == null) {
            controller.addCategory(category);
        }
        System.out.println("Category added.");
    }

    // INV-5
    private void addPromotion() {
        System.out.print("Discount percent: ");
        double discount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate start = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("End date (YYYY-MM-DD): ");
        LocalDate end = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Target type (PRODUCT/CATEGORY): ");
        String targetType = scanner.nextLine().trim().toUpperCase();

        ProductSpec targetSpec = null;
        Category targetCat = null;

        if (targetType.equals("PRODUCT")) {
            System.out.print("Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());
            Product product = findProduct(productId);
            if (product == null) return;
            targetSpec = product.getSpec();
        } else if (targetType.equals("CATEGORY")) {
            System.out.print("Category name: ");
            String catName = scanner.nextLine().trim();
            targetCat = findCategory(catName);
            if (targetCat == null) {
                System.out.println("Category not found.");
                return;
            }
        } else {
            System.out.println("Invalid target type.");
            return;
        }

        Promotion promo = new Promotion(discount, start, end, targetSpec, targetCat);
        controller.addPromotion(promo);
        System.out.println("Promotion added.");
    }

    // INV-5
    private void viewActivePromotions() {
        List<Promotion> active = controller.getActivePromotions();
        if (active.isEmpty()) {
            System.out.println("No active promotions.");
            return;
        }
        for (Promotion p : active) {
            String target = p.getTargetProduct() != null
                    ? "Product: " + p.getTargetProduct().getName()
                    : "Category: " + p.getTargetCategory().getName();
            System.out.printf("  %.0f%% off — %s to %s — %s%n",
                    p.getDiscountPercent(), p.getStartDate(), p.getEndDate(), target);
        }
    }

    // INV-5, INV-9
    private void checkEffectivePrice() {
        System.out.print("Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        double price = controller.getEffectivePrice(productId);
        System.out.printf("Effective price: %.2f%n", price);
    }

    // INV-7, INV-11
    private void reportDefective() {
        System.out.print("Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());

        controller.reportDefective(productId, 1, "DEFECTIVE");
        System.out.println("Defective item reported and stock reduced by 1.");
    }

    // INV-11 — auto-remove expired stock
    private void removeExpiredStock() {
        int removed = controller.removeExpiredStock();
        if (removed == 0) {
            System.out.println("No expired stock found.");
        } else {
            System.out.println(removed + " expired items removed from stock.");
        }
    }

    // INV-7
    private void locateDefectiveItems() {
        Map<Integer, List<StockItem>> defectives = controller.getDefectiveItemsWithLocations();
        if (defectives.isEmpty()) {
            System.out.println("No defective items found.");
            return;
        }
        for (Map.Entry<Integer, List<StockItem>> entry : defectives.entrySet()) {
            System.out.printf("  Product ID %d:%n", entry.getKey());
            for (StockItem si : entry.getValue()) {
                System.out.printf("    %s shelf=%d row=%d qty=%d%n",
                        si.getArea(), si.getShelfNumber(), si.getRowNumber(), si.getQuantity());
            }
        }
    }

    // INV-8
    private void defectiveReportByDates() {
        System.out.print("From date (YYYY-MM-DD): ");
        LocalDate from = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("To date (YYYY-MM-DD): ");
        LocalDate to = LocalDate.parse(scanner.nextLine().trim());

        List<DefectiveReport> reports = controller.getDefectiveReports(from, to);
        if (reports.isEmpty()) {
            System.out.println("No defective reports in this period.");
            return;
        }
        for (DefectiveReport r : reports) {
            System.out.printf("  Product ID=%d qty=%d reason=%s date=%s%n",
                    r.getProductId(), r.getQuantity(), r.getReason(), r.getReportDate());
        }
    }

    // INV-6
    private void generateInventoryReport() {
        System.out.print("Filter by categories? (y/n): ");
        String filterChoice = scanner.nextLine().trim().toLowerCase();

        List<Category> filter = null;
        if (filterChoice.equals("y")) {
            filter = new ArrayList<>();
            System.out.println("Enter category names (empty line to finish):");
            while (true) {
                String catName = scanner.nextLine().trim();
                if (catName.isEmpty()) break;
                Category cat = findCategory(catName);
                if (cat == null) {
                    System.out.println("Category '" + catName + "' not found, skipping.");
                } else {
                    filter.add(cat);
                }
            }
        }

        InventoryReport report = controller.generateInventoryReport(LocalDate.now(), filter);
        System.out.printf("Inventory Report — %s (%d items)%n",
                report.getReportDate(), report.getItems().size());
        for (Product p : report.getItems()) {
            ProductSpec spec = p.getSpec();
            List<StockItem> stock = controller.getStockForProduct(p.getId());
            int storeQty = 0;
            int warehouseQty = 0;
            for (StockItem si : stock) {
                if (si.getArea() == Area.STORE) storeQty += si.getQuantity();
                else warehouseQty += si.getQuantity();
            }
            int total = storeQty + warehouseQty;
            String status = total == 0 ? "OUT" : total < spec.getMinStockThreshold() ? "LOW" : "OK";
            System.out.printf("  [ID=%d] %s (%s) — store=%d warehouse=%d total=%d [%s]%n",
                    p.getId(), spec.getName(), spec.getManufacturer(),
                    storeQty, warehouseQty, total, status);
        }
    }

    private void loadTestData() {
        preloadData.load();
        System.out.println("Test data loaded (previous data cleared).");
    }

    // ── HELPERS ──────────────────────────────────────────────

    private Product findProduct(int productId) {
        try {
            return controller.getProduct(productId);
        } catch (IllegalArgumentException e) {
            System.out.println("Product not found: " + productId);
            return null;
        }
    }

    private Category findCategory(String name) {
        for (Category root : controller.getRootCategories()) {
            Category found = findCategoryRecursive(root, name);
            if (found != null) return found;
        }
        return null;
    }

    private Category findOrCreateCategoryPath(String path) {
        String[] parts = path.split(",");
        if (parts.length == 0) return null;

        Category current = null;
        for (int i = 0; i < parts.length; i++) {
            String name = parts[i].trim();
            if (name.isEmpty()) return null;

            if (i == 0) {
                // Find or create root category
                current = findCategory(name);
                if (current == null) {
                    current = new Category(name);
                    controller.addCategory(current);
                }
            } else {
                // Find or create sub-category under current
                Category child = null;
                for (Category sub : current.getSubCategories()) {
                    if (sub.getName().equalsIgnoreCase(name)) {
                        child = sub;
                        break;
                    }
                }
                if (child == null) {
                    child = new Category(name, current);
                }
                current = child;
            }
        }
        return current;
    }

    private Category findCategoryRecursive(Category current, String name) {
        if (current.getName().equalsIgnoreCase(name)) return current;
        for (Category sub : current.getSubCategories()) {
            Category found = findCategoryRecursive(sub, name);
            if (found != null) return found;
        }
        return null;
    }
}
