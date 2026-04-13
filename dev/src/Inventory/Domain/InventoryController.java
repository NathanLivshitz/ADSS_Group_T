package Inventory.Domain;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryController {
    private final Map<Integer, Product> catalog;
    private final List<StockItem> stockItems;
    private final List<Category> rootCategories;
    private final List<Promotion> promotions;
    private final List<DefectiveReport> defectiveReports;

    public InventoryController() {
        this.catalog = new HashMap<>();
        this.stockItems = new ArrayList<>();
        this.rootCategories = new ArrayList<>();
        this.promotions = new ArrayList<>();
        this.defectiveReports = new ArrayList<>();
    }

    // ── CATALOG ──────────────────────────────────────────────
    // INV-1: Maintain product catalog

    public void addProduct(Product product) {
        if (product == null)
            throw new IllegalArgumentException("Product must not be null");
        if (catalog.containsKey(product.getId()))
            throw new IllegalArgumentException("Product ID " + product.getId() + " already exists");
        catalog.put(product.getId(), product);
    }

    // ── STOCK ────────────────────────────────────────────────
    // INV-2: Track quantities by exact location

    public void addStockItem(StockItem item) {
        if (item == null)
            throw new IllegalArgumentException("StockItem must not be null");
        stockItems.add(item);
    }

    /** INV-2: Where is each product located + quantities per location. */
    public List<StockItem> getStockForProduct(int productId) {
        ProductSpec spec = getProductInternal(productId).getSpec();
        return stockItems.stream()
                .filter(si -> si.getSpec() == spec)
                .collect(Collectors.toList());
    }

    /** INV-10: Update stock when received, sold, or expired. */
    public void updateQuantity(int productId, Area area, int shelf, int row, int delta) {
        ProductSpec spec = getProductInternal(productId).getSpec();
        StockItem item = stockItems.stream()
                .filter(si -> si.getSpec() == spec
                        && si.getArea() == area
                        && si.getShelfNumber() == shelf
                        && si.getRowNumber() == row)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Location not found for product " + productId));
        int newQty = item.getQuantity() + delta;
        if (newQty < 0)
            throw new IllegalArgumentException("Not enough stock. Available: " + item.getQuantity());
        item.setQuantity(newQty);
    }

    // ── ALERTS ───────────────────────────────────────────────
    // INV-3: Proactive stock alerts

    public List<Product> getLowStockProducts() {
        return catalog.values().stream()
                .filter(p -> getTotalQuantity(p.getId()) < p.getSpec().getMinStockThreshold())
                .collect(Collectors.toList());
    }

    // ── CATEGORIES ───────────────────────────────────────────
    // INV-4: Hierarchical categories

    public void addCategory(Category c) {
        if (c == null)
            throw new IllegalArgumentException("Category must not be null");
        rootCategories.add(c);
    }

    public List<Category> getRootCategories() {
        return Collections.unmodifiableList(rootCategories);
    }

    // ── PROMOTIONS ───────────────────────────────────────────
    // INV-5: Promotions with discounts on products/categories with date ranges
    // INV-9: Track cost/sell price (via ProductSpec)

    public void addPromotion(Promotion p) {
        if (p == null)
            throw new IllegalArgumentException("Promotion must not be null");
        promotions.add(p);
    }

    public List<Promotion> getActivePromotions() {
        return promotions.stream()
                .filter(Promotion::isActive)
                .collect(Collectors.toList());
    }

    public double getEffectivePrice(int productId) {
        ProductSpec spec = getProductInternal(productId).getSpec();
        return promotions.stream()
                .filter(p -> p.isActive() && p.appliesTo(spec))
                .findFirst()
                .map(p -> p.getEffectivePrice(spec))
                .orElse(spec.getSellPrice());
    }

    // ── DEFECTIVES ───────────────────────────────────────────
    // INV-7: Employees report defective/expired
    // INV-11: Auto reduce quantity on report

    public void reportDefective(int productId, int quantity, String reason) {
        getProductInternal(productId);
        defectiveReports.add(new DefectiveReport(productId, quantity, reason, LocalDate.now()));
        removeStock(productId, quantity);
    }

    /** INV-8: Periodic defect reports filtered by date range. */
    public List<DefectiveReport> getDefectiveReports(LocalDate from, LocalDate to) {
        return defectiveReports.stream()
                .filter(r -> !r.getReportDate().isBefore(from) && !r.getReportDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /** INV-7: Locate defective items — maps product ID to current stock locations. */
    public Map<Integer, List<StockItem>> getDefectiveItemsWithLocations() {
        Map<Integer, List<StockItem>> result = new HashMap<>();
        for (DefectiveReport r : defectiveReports) {
            if (!result.containsKey(r.getProductId()) && catalog.containsKey(r.getProductId())) {
                result.put(r.getProductId(), getStockForProduct(r.getProductId()));
            }
        }
        return result;
    }

    // ── REPORTS ──────────────────────────────────────────────
    // INV-6: Inventory reports filterable by categories

    public InventoryReport generateInventoryReport(LocalDate reportDate, List<Category> categoriesFilter) {
        List<Product> items;
        if (categoriesFilter == null || categoriesFilter.isEmpty()) {
            items = new ArrayList<>(catalog.values());
        } else {
            Set<ProductSpec> specSet = categoriesFilter.stream()
                    .flatMap(c -> c.getAllProducts().stream())
                    .collect(Collectors.toSet());
            items = catalog.values().stream()
                    .filter(p -> specSet.contains(p.getSpec()))
                    .collect(Collectors.toList());
        }
        return new InventoryReport(reportDate,
                categoriesFilter != null ? categoriesFilter : List.of(), items);
    }

    // ── INTERNAL ─────────────────────────────────────────────

    private Product getProductInternal(int id) {
        Product p = catalog.get(id);
        if (p == null)
            throw new IllegalArgumentException("Product ID " + id + " not found");
        return p;
    }

    private int getTotalQuantity(int productId) {
        return getStockForProduct(productId).stream()
                .mapToInt(StockItem::getQuantity).sum();
    }

    private void removeStock(int productId, int quantity) {
        List<StockItem> sorted = getStockForProduct(productId).stream()
                .sorted(Comparator.comparing(si -> si.getArea() == Area.STORE ? 0 : 1))
                .collect(Collectors.toList());

        int remaining = quantity;
        for (StockItem item : sorted) {
            if (remaining <= 0) break;
            int take = Math.min(item.getQuantity(), remaining);
            item.setQuantity(item.getQuantity() - take);
            remaining -= take;
        }
        if (remaining > 0)
            throw new IllegalArgumentException(
                    "Not enough total stock to remove " + quantity + " units");
    }
}
