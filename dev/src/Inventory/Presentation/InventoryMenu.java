package Inventory.Presentation;

import Inventory.DTO.*;
import Inventory.Data.PreloadData;
import Inventory.Service.InventoryService;
import Shared.DTO.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class InventoryMenu {
    private final InventoryService service;
    private final PreloadData preloadData;
    private final Scanner scanner;

    public InventoryMenu(InventoryService service, PreloadData preloadData, Scanner scanner) {
        this.service = service;
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
                    case "16": orderDueToShortage(); break;
                    case "17": orderOnTimePeriod(); break;
                    case "0":  running = false; break;
                    default:   System.out.println("Invalid option."); break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Error: invalid date format, expected YYYY-MM-DD.");
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
        System.out.println("16. Order due to shortage");
        System.out.println("17. Periodic order from supplier");
        System.out.println("0.  Exit");
        System.out.print("Choose: ");
    }

    // INV-1
    private void addProduct() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Manufacturer: ");
        String manufacturer = scanner.nextLine().trim();
        System.out.print("Category path (c1,c2,c3): ");
        String catPath = scanner.nextLine().trim();
        int catId = findOrCreateCategoryPath(catPath);
        if (catId == -1) {
            System.out.println("Invalid category path.");
            return;
        }
        System.out.print("Cost price: ");
        double costPrice = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Sell price: ");
        double sellPrice = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Min stock threshold: ");
        int minStock = Integer.parseInt(scanner.nextLine().trim());

        int assignedId = service.addProduct(new ProductDTO(0, 0, name, manufacturer, catId, costPrice, sellPrice, minStock, 0));
        System.out.println("Product spec added with ID: " + assignedId);
        if (sellPrice < costPrice)
            System.out.printf("Warning: sell price %.2f is below cost price %.2f.%n", sellPrice, costPrice);
    }

    // INV-2
    private void addStockItem() {
        System.out.print("Product spec ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        ProductDTO product = findProduct(productId);
        if (product == null) return;

        System.out.print("Area (STORE/WAREHOUSE): ");
        String area = scanner.nextLine().trim().toUpperCase();
        System.out.print("Shelf number: ");
        int shelf = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Row number: ");
        int row = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Expiry date (YYYY-MM-DD or empty): ");
        String expiryStr = scanner.nextLine().trim();
        String expiry = expiryStr.isEmpty() ? null : expiryStr;

        service.addStockItem(new StockItemDTO(product.specId(), area, shelf, row, qty, expiry));
        System.out.println("Stock added.");
        if (expiry != null && LocalDate.parse(expiry).isBefore(LocalDate.now())) {
            System.out.printf("Warning: added %d expired items for %s. They will appear in defective item location reports.%n",
                    qty, product.name());
        }
    }

    // INV-2
    private void viewProductStock() {
        System.out.print("Product spec ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());

        List<StockItemDTO> stock = service.getStockForProduct(productId);
        if (stock.isEmpty()) {
            System.out.println("No stock found.");
            return;
        }

        int storeQty = 0, warehouseQty = 0;
        for (StockItemDTO si : stock) {
            System.out.printf("  %s shelf=%d row=%d qty=%d expiry=%s%n",
                    si.area(), si.shelf(), si.row(), si.quantity(),
                    si.expiryDate() != null ? si.expiryDate() : "N/A");
            if ("STORE".equals(si.area())) storeQty += si.quantity();
            else warehouseQty += si.quantity();
        }
        System.out.printf("Store: %d | Warehouse: %d | Total: %d%n",
                storeQty, warehouseQty, storeQty + warehouseQty);
    }

    // INV-10
    private void updateStock() {
        System.out.print("Product spec ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Area (STORE/WAREHOUSE): ");
        String area = scanner.nextLine().trim().toUpperCase();
        System.out.print("Shelf number: ");
        int shelf = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Row number: ");
        int row = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Delta (positive to add, negative to remove): ");
        int delta = Integer.parseInt(scanner.nextLine().trim());

        service.updateQuantity(productId, area, shelf, row, delta);
        System.out.println("Stock updated.");
    }

    // INV-3
    private void lowStockAlerts() {
        List<ProductDTO> low = service.getLowStockProducts();
        if (low.isEmpty()) {
            System.out.println("No low stock products.");
            return;
        }
        for (ProductDTO p : low) {
            System.out.printf("  [specId=%d] %s (%s) - total=%d, min=%d%n",
                    p.specId(), p.name(), p.manufacturer(),
                    p.totalQuantity(), p.minStockThreshold());
        }
    }

    // INV-4
    private void addCategory() {
        System.out.print("Category name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Parent category name (or empty for root): ");
        String parentName = scanner.nextLine().trim();

        int parentId = 0;
        if (!parentName.isEmpty()) {
            CategoryDTO parent = service.findCategoryByName(parentName);
            if (parent == null) {
                System.out.println("Parent category not found.");
                return;
            }
            parentId = parent.categoryId();
        }

        int assignedId = service.addCategory(name, parentId);
        System.out.println("Category added with ID: " + assignedId);
    }

    // INV-5
    private void addPromotion() {
        System.out.print("Discount percent: ");
        double discount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Start date (YYYY-MM-DD): ");
        String start = scanner.nextLine().trim();
        System.out.print("End date (YYYY-MM-DD): ");
        String end = scanner.nextLine().trim();
        System.out.print("Target type (PRODUCT/CATEGORY): ");
        String targetType = scanner.nextLine().trim().toUpperCase();

        int targetSpecId = 0;
        int targetCatId = 0;

        if (targetType.equals("PRODUCT")) {
            System.out.print("Product spec ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());
            ProductDTO product = findProduct(productId);
            if (product == null) return;
            targetSpecId = product.specId();
        } else if (targetType.equals("CATEGORY")) {
            System.out.print("Category name: ");
            String catName = scanner.nextLine().trim();
            CategoryDTO cat = service.findCategoryByName(catName);
            if (cat == null) {
                System.out.println("Category not found.");
                return;
            }
            targetCatId = cat.categoryId();
        } else {
            System.out.println("Invalid target type.");
            return;
        }

        service.addPromotion(new PromotionDTO(discount, start, end, targetSpecId, targetCatId, null, null));
        System.out.println("Promotion added.");
    }

    // INV-5
    private void viewActivePromotions() {
        List<PromotionDTO> active = service.getActivePromotions();
        if (active.isEmpty()) {
            System.out.println("No active promotions.");
            return;
        }
        for (PromotionDTO p : active) {
            String target = p.targetSpecId() != 0
                    ? "Product: " + p.targetProductName()
                    : "Category: " + p.targetCategoryName();
            System.out.printf("  %.0f%% off - %s to %s - %s%n",
                    p.discountPercent(), p.startDate(), p.endDate(), target);
        }
    }

    // INV-5, INV-9
    private void checkEffectivePrice() {
        System.out.print("Product spec ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        double price = service.getEffectivePrice(productId);
        System.out.printf("Effective price: %.2f%n", price);
    }

    // INV-7, INV-11
    private void reportDefective() {
        System.out.print("Product spec ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        service.reportDefective(productId, 1, "DEFECTIVE");
        System.out.println("Defective item reported and stock reduced by 1.");
    }

    // INV-11
    private void removeExpiredStock() {
        int removed = service.removeExpiredStock();
        if (removed == 0) System.out.println("No expired stock found.");
        else System.out.println(removed + " expired items removed from stock.");
    }

    // INV-7
    private void locateDefectiveItems() {
        List<DefectiveLocationDTO> defectives = service.getDefectiveItemsWithLocations();
        if (defectives.isEmpty()) {
            System.out.println("No defective items found.");
            return;
        }
        for (DefectiveLocationDTO entry : defectives) {
            System.out.printf("  Product spec ID %d:%n", entry.productId());
            for (StockItemDTO si : entry.locations()) {
                System.out.printf("    %s shelf=%d row=%d qty=%d%n",
                        si.area(), si.shelf(), si.row(), si.quantity());
            }
        }
    }

    // INV-8
    private void defectiveReportByDates() {
        System.out.print("From date (YYYY-MM-DD): ");
        LocalDate from = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("To date (YYYY-MM-DD): ");
        LocalDate to = LocalDate.parse(scanner.nextLine().trim());

        List<DefectiveReportDTO> reports = service.getDefectiveReports(from, to);
        if (reports.isEmpty()) {
            System.out.println("No defective reports in this period.");
            return;
        }
        for (DefectiveReportDTO r : reports) {
            System.out.printf("  Product spec ID=%d qty=%d reason=%s date=%s%n",
                    r.productId(), r.quantity(), r.reason(), r.reportDate());
        }
    }

    // INV-6
    private void generateInventoryReport() {
        System.out.print("Filter by categories? (y/n): ");
        String filterChoice = scanner.nextLine().trim().toLowerCase();

        List<Integer> categoryIds = null;
        if (filterChoice.equals("y")) {
            categoryIds = new ArrayList<>();
            System.out.println("Enter category names (empty line to finish):");
            while (true) {
                String catName = scanner.nextLine().trim();
                if (catName.isEmpty()) break;
                CategoryDTO cat = service.findCategoryByName(catName);
                if (cat == null) System.out.println("Category '" + catName + "' not found, skipping.");
                else categoryIds.add(cat.categoryId());
            }
        }

        List<ProductDTO> items = service.generateInventoryReport(categoryIds);
        System.out.printf("Inventory Report - %s (%d items)%n", LocalDate.now(), items.size());
        for (ProductDTO p : items) {
            List<StockItemDTO> stock = service.getStockForProduct(p.specId());
            int storeQty = 0, warehouseQty = 0;
            for (StockItemDTO si : stock) {
                if ("STORE".equals(si.area())) storeQty += si.quantity();
                else warehouseQty += si.quantity();
            }
            int total = storeQty + warehouseQty;
            String status = total == 0 ? "OUT" : total < p.minStockThreshold() ? "LOW" : "OK";
            System.out.printf("  [specId=%d] %s (%s) - store=%d warehouse=%d total=%d [%s]%n",
                    p.specId(), p.name(), p.manufacturer(), storeQty, warehouseQty, total, status);
        }
    }

    private void loadTestData() {
        preloadData.load();
        System.out.println("Test data loaded (previous data cleared).");
    }

    // UC-f
    private void orderDueToShortage() {
        List<ProductDTO> shortage = service.getLowStockProducts();
        if (shortage.isEmpty()) { System.out.println("No low-stock products."); return; }
        System.out.println("Low-stock products:");
        for (ProductDTO p : shortage)
            System.out.printf("  [specId=%d] %s - qty=%d, min=%d%n",
                    p.specId(), p.name(), p.totalQuantity(), p.minStockThreshold());
        System.out.print("Enter specId to order: ");
        int specId = Integer.parseInt(scanner.nextLine().trim());
        OrderProposalDTO proposal = service.selectProduct(specId);
        System.out.printf("Proposal #%d: supplier=%d, qty=%d, price/unit=%.2f%n",
                proposal.proposalId(), proposal.supplierId(),
                proposal.requiredQty(), proposal.unitPrice());
        System.out.print("Confirm? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) { System.out.println("Cancelled."); return; }
        OrderSummaryDTO order = service.confirmOrder(proposal.proposalId());
        System.out.printf("Order #%d sent. Total: %.2f%n", order.orderId(), order.totalPrice());
    }

    // UC-e
    private void orderOnTimePeriod() {
        List<SupplierScheduleDTO> scheduled = service.getSuppliersWithSchedules();
        if (scheduled.isEmpty()) { System.out.println("No suppliers with fixed delivery schedules."); return; }
        List<String> preview = service.describePeriodicOrders();
        if (preview.isEmpty()) { System.out.println("No periodic orders needed right now."); return; }
        System.out.println("Proposed periodic orders:");
        for (String line : preview) System.out.println("  " + line);
        System.out.print("Approve and send all? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) { System.out.println("Cancelled."); return; }
        List<OrderSummaryDTO> placed = service.submitAllPeriodicOrders();
        System.out.printf("%d periodic order(s) sent.%n", placed.size());
        for (OrderSummaryDTO o : placed)
            System.out.printf("  Order #%d -> supplier %d, delivery %s, total %.2f%n",
                    o.orderId(), o.supplierId(), o.expectedDeliveryDate(), o.totalPrice());
    }

    // ── HELPERS ──────────────────────────────────────────────

    private ProductDTO findProduct(int productId) {
        try {
            return service.getProduct(productId);
        } catch (IllegalArgumentException e) {
            System.out.println("Product spec not found: " + productId);
            return null;
        }
    }

    private int findOrCreateCategoryPath(String path) {
        String[] parts = path.split(",");
        if (parts.length == 0) return -1;

        int parentId = 0;
        for (String part : parts) {
            String name = part.trim();
            if (name.isEmpty()) return -1;
            CategoryDTO existing = service.findCategoryByName(name);
            if (existing != null) {
                parentId = existing.categoryId();
            } else {
                parentId = service.addCategory(name, parentId);
            }
        }
        return parentId;
    }
}
