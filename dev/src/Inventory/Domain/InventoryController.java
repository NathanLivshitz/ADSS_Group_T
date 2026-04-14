package Inventory.Domain;

import java.time.LocalDate;
import java.util.*;

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

    /**
     * Clears all data — catalog, stock, categories, promotions, defective reports.
     * Used before reloading preloaded data to avoid duplicates.
     */
    public void reset() {
        catalog.clear();
        stockItems.clear();
        rootCategories.clear();
        promotions.clear();
        defectiveReports.clear();
    }

    // ── CATALOG ──────────────────────────────────────────────

    /**
     * Menu 1: Add product
     * INV-1: Maintain product catalog.
     * Input: Product(id, ProductSpec(name, manufacturer, category, costPrice, sellPrice, minStock))
     * Throws: if product is null or ID already exists.
     */
    public void addProduct(Product product) {
        if (product == null)
            throw new IllegalArgumentException("Product must not be null");
        if (catalog.containsKey(product.getId()))
            throw new IllegalArgumentException("Product ID " + product.getId() + " already exists");
        catalog.put(product.getId(), product);
    }

    /**
     * Used by: Menu 2 (add stock), Menu 7 (add promotion), Menu 9 (check price)
     * Looks up a product by ID. Not a standalone menu option.
     * Throws: if product ID not found.
     */
    public Product getProduct(int id) {
        Product p = catalog.get(id);
        if (p == null)
            throw new IllegalArgumentException("Product ID " + id + " not found");
        return p;
    }

    // ── STOCK ────────────────────────────────────────────────

    /**
     * Menu 2: Add stock to product
     * INV-2: Track quantities by exact location.
     * Input: StockItem(spec, area, shelf, row, quantity, expiryDate)
     * Throws: if item is null or spec not in catalog.
     */
    public void addStockItem(StockItem item) {
        if (item == null)
            throw new IllegalArgumentException("StockItem must not be null");
        boolean specInCatalog = false;
        for (Product p : catalog.values()) {
            if (p.getSpec() == item.getSpec()) {
                specInCatalog = true;
                break;
            }
        }
        if (!specInCatalog)
            throw new IllegalArgumentException("StockItem's spec does not belong to any product in the catalog");
        stockItems.add(item);
    }

    /**
     * Menu 3: View product locations & quantities
     * INV-2: Where is each product, store qty, warehouse qty, total.
     * Input: productId
     * Returns: list of StockItems for this product (filter by area for store/warehouse breakdown).
     */
    public List<StockItem> getStockForProduct(int productId) {
        ProductSpec spec = getProduct(productId).getSpec();
        List<StockItem> result = new ArrayList<>();
        for (StockItem si : stockItems) {
            if (si.getSpec() == spec) {
                result.add(si);
            }
        }
        return result;
    }

    /**
     * Menu 4: Update stock
     * INV-10: Update stock when received, sold, or expired.
     * Input: productId, area, shelf, row, delta (positive=add, negative=remove)
     * Throws: if area null, product not found, location not found, or quantity would go negative.
     */
    public void updateQuantity(int productId, Area area, int shelf, int row, int delta) {
        if (area == null)
            throw new IllegalArgumentException("Area must not be null");
        ProductSpec spec = getProduct(productId).getSpec();
        StockItem found = null;
        for (StockItem si : stockItems) {
            if (si.getSpec() == spec && si.getArea() == area
                    && si.getShelfNumber() == shelf && si.getRowNumber() == row) {
                found = si;
                break;
            }
        }
        if (found == null)
            throw new IllegalArgumentException("Location not found for product " + productId);
        int newQty = found.getQuantity() + delta;
        if (newQty < 0)
            throw new IllegalArgumentException("Not enough stock. Available: " + found.getQuantity());
        found.setQuantity(newQty);
    }

    // ── ALERTS ───────────────────────────────────────────────

    /**
     * Menu 5: Low stock alerts
     * INV-3: Proactive stock alerts.
     * Returns: products where total quantity across all StockItems < minStockThreshold.
     */
    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : catalog.values()) {
            if (getTotalQuantity(p.getId()) < p.getSpec().getMinStockThreshold()) {
                result.add(p);
            }
        }
        return result;
    }

    // ── CATEGORIES ───────────────────────────────────────────

    /**
     * Menu 6: Add category
     * INV-4: Hierarchical categories.
     * Input: Category (with optional parent). Only root categories are added here;
     *        sub-categories auto-register via Category constructor.
     */
    public void addCategory(Category c) {
        if (c == null)
            throw new IllegalArgumentException("Category must not be null");
        rootCategories.add(c);
    }

    /**
     * Used by: Menu 6 (find parent), Menu 7 (category promotion), Menu 13 (report filter)
     * INV-4: Returns root categories for tree traversal.
     */
    public List<Category> getRootCategories() {
        return Collections.unmodifiableList(rootCategories);
    }

    // ── PROMOTIONS ───────────────────────────────────────────

    /**
     * Menu 7: Add promotion
     * INV-5: Promotions with discounts on products/categories with date ranges.
     * Input: Promotion(discountPercent, startDate, endDate, targetSpec OR targetCategory)
     */
    public void addPromotion(Promotion p) {
        if (p == null)
            throw new IllegalArgumentException("Promotion must not be null");
        promotions.add(p);
    }

    /**
     * Menu 8: View active promotions
     * INV-5: Returns all promotions where today is within [startDate, endDate].
     */
    public List<Promotion> getActivePromotions() {
        List<Promotion> active = new ArrayList<>();
        for (Promotion p : promotions) {
            if (p.isActive()) {
                active.add(p);
            }
        }
        return active;
    }

    /**
     * Menu 9: Check effective price
     * INV-5, INV-9: Returns sell price after first matching active promotion discount.
     * Input: productId
     * Returns: discounted price if active promotion applies, otherwise sell price.
     */
    public double getEffectivePrice(int productId) {
        ProductSpec spec = getProduct(productId).getSpec();
        for (Promotion p : promotions) {
            if (p.isActive() && p.appliesTo(spec)) {
                return p.getEffectivePrice(spec);
            }
        }
        return spec.getSellPrice();
    }

    // ── DEFECTIVES ───────────────────────────────────────────

    /**
     * Menu 10: Report defective/expired
     * INV-7: Employees report defective/expired items.
     * INV-11: Auto reduce quantity (drains store-first, then warehouse).
     * Input: productId, quantity, reason ("DEFECTIVE" or "EXPIRED")
     * Throws: if product not found or insufficient stock.
     */
    public void reportDefective(int productId, int quantity, String reason) {
        getProduct(productId);
        defectiveReports.add(new DefectiveReport(productId, quantity, reason, LocalDate.now()));
        removeStock(productId, quantity);
    }

    /**
     * Menu 11: Locate defective items
     * INV-7: Maps each defective product ID to its current stock locations.
     * Returns: productId to List of StockItem for products that have been reported defective.
     */
    public Map<Integer, List<StockItem>> getDefectiveItemsWithLocations() {
        Map<Integer, List<StockItem>> result = new HashMap<>();
        for (DefectiveReport r : defectiveReports) {
            if (!result.containsKey(r.getProductId()) && catalog.containsKey(r.getProductId())) {
                result.put(r.getProductId(), getStockForProduct(r.getProductId()));
            }
        }
        return result;
    }

    /**
     * Menu 12: Defective report by dates
     * INV-8: Periodic defect reports filtered by date range.
     * Input: from date, to date (inclusive)
     * Throws: if dates null or from after to.
     */
    public List<DefectiveReport> getDefectiveReports(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Date range cannot be null");
        if (from.isAfter(to))
            throw new IllegalArgumentException("From date must be before or equal to to date");
        List<DefectiveReport> result = new ArrayList<>();
        for (DefectiveReport r : defectiveReports) {
            if (!r.getReportDate().isBefore(from) && !r.getReportDate().isAfter(to)) {
                result.add(r);
            }
        }
        return result;
    }

    // ── REPORTS ──────────────────────────────────────────────

    /**
     * Menu 13: Generate inventory report
     * INV-6: Inventory reports filterable by categories.
     * Input: reportDate, categoriesFilter (null or empty = all products)
     * Returns: InventoryReport with products matching the category filter.
     */
    public InventoryReport generateInventoryReport(LocalDate reportDate, List<Category> categoriesFilter) {
        List<Product> items;
        if (categoriesFilter == null || categoriesFilter.isEmpty()) {
            items = new ArrayList<>(catalog.values());
        } else {
            Set<ProductSpec> specSet = new HashSet<>();
            for (Category c : categoriesFilter) {
                specSet.addAll(c.getAllProducts());
            }
            items = new ArrayList<>();
            for (Product p : catalog.values()) {
                if (specSet.contains(p.getSpec())) {
                    items.add(p);
                }
            }
        }
        return new InventoryReport(reportDate,
                categoriesFilter != null ? categoriesFilter : new ArrayList<>(), items);
    }

    /**
     * Auto-remove expired stock items.
     * INV-11: Automatically reduces quantity when items are expired.
     * Scans all StockItems, sets expired ones to qty=0, creates defective reports.
     * Returns: number of items removed.
     */
    public int removeExpiredStock() {
        int totalRemoved = 0;
        LocalDate today = LocalDate.now();
        for (StockItem si : stockItems) {
            if (si.getExpiryDate() != null && si.getExpiryDate().isBefore(today) && si.getQuantity() > 0) {
                int qty = si.getQuantity();
                int productId = findProductIdBySpec(si.getSpec());
                if (productId != -1) {
                    defectiveReports.add(new DefectiveReport(productId, qty, "EXPIRED", today));
                    totalRemoved += qty;
                }
                si.setQuantity(0);
            }
        }
        return totalRemoved;
    }

    private int findProductIdBySpec(ProductSpec spec) {
        for (Map.Entry<Integer, Product> entry : catalog.entrySet()) {
            if (entry.getValue().getSpec() == spec) {
                return entry.getKey();
            }
        }
        return -1;
    }

    // ── INTERNAL ─────────────────────────────────────────────

    private int getTotalQuantity(int productId) {
        int total = 0;
        for (StockItem si : getStockForProduct(productId)) {
            total += si.getQuantity();
        }
        return total;
    }

    private void removeStock(int productId, int quantity) {
        List<StockItem> storeItems = new ArrayList<>();
        List<StockItem> warehouseItems = new ArrayList<>();
        for (StockItem si : getStockForProduct(productId)) {
            if (si.getArea() == Area.STORE) {
                storeItems.add(si);
            } else {
                warehouseItems.add(si);
            }
        }

        List<StockItem> sorted = new ArrayList<>();
        sorted.addAll(storeItems);
        sorted.addAll(warehouseItems);

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
