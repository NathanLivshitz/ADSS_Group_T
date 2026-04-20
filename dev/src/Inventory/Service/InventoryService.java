package Inventory.Service;

import Inventory.Domain.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Service layer — sits between the menu and the controller (INV-NF4).
// Basically a thin pass-through so the menu doesn't talk to the controller directly.
public class InventoryService {
    private final InventoryController controller;

    public InventoryService(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
    }

    // ── Catalog ────────────────────────────────────────────

    // Menu 1 — add product (INV-1)
    public void addProduct(Product product) {
        controller.addProduct(product);
    }

    public Product getProduct(int id) {
        return controller.getProduct(id);
    }

    // ── Stock ──────────────────────────────────────────────

    // Menu 2 — add stock (INV-2)
    public void addStockItem(StockItem item) {
        controller.addStockItem(item);
    }

    // Menu 3 — view product stock (INV-2)
    public List<StockItem> getStockForProduct(int productId) {
        return controller.getStockForProduct(productId);
    }

    // Menu 4 — update stock (INV-10)
    public void updateQuantity(int productId, Area area, int shelf, int row, int delta) {
        controller.updateQuantity(productId, area, shelf, row, delta);
    }

    // ── Alerts ─────────────────────────────────────────────

    // Menu 5 — low stock alerts (INV-3)
    public List<Product> getLowStockProducts() {
        return controller.getLowStockProducts();
    }

    // ── Categories ─────────────────────────────────────────

    // Menu 6 — add category (INV-4)
    public void addCategory(Category c) {
        controller.addCategory(c);
    }

    public List<Category> getRootCategories() {
        return controller.getRootCategories();
    }

    // ── Promotions ─────────────────────────────────────────

    // Menu 7 — add promotion (INV-5)
    public void addPromotion(Promotion p) {
        controller.addPromotion(p);
    }

    // Menu 8 — view active promotions (INV-5)
    public List<Promotion> getActivePromotions() {
        return controller.getActivePromotions();
    }

    // Menu 9 — effective price with discount (INV-5, INV-9)
    public double getEffectivePrice(int productId) {
        return controller.getEffectivePrice(productId);
    }

    // ── Defectives ─────────────────────────────────────────

    // Menu 10 — report defective (INV-7, INV-11)
    public void reportDefective(int productId, int quantity, String reason) {
        if (quantity <= 0)
            throw new IllegalArgumentException("quantity must be positive");
        if (reason == null || reason.trim().isEmpty())
            throw new IllegalArgumentException("reason cannot be empty");
        controller.reportDefective(productId, quantity, reason);
    }

    // Menu 11 — remove expired stock (INV-11)
    public int removeExpiredStock() {
        return controller.removeExpiredStock();
    }

    // Menu 12 — locate defective items (INV-7)
    public Map<Integer, List<StockItem>> getDefectiveItemsWithLocations() {
        return controller.getDefectiveItemsWithLocations();
    }

    // Menu 13 — defective report by dates (INV-8)
    public List<DefectiveReport> getDefectiveReports(LocalDate from, LocalDate to) {
        return controller.getDefectiveReports(from, to);
    }

    // ── Reports ────────────────────────────────────────────

    // Menu 14 — inventory report (INV-6)
    public InventoryReport generateInventoryReport(LocalDate reportDate, List<Category> categoriesFilter) {
        return controller.generateInventoryReport(reportDate, categoriesFilter);
    }

    // ── Reset ──────────────────────────────────────────────

    public void reset() {
        controller.reset();
    }
}
