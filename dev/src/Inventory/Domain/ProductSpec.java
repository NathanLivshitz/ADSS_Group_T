package Inventory.Domain;

public class ProductSpec {
    private final String name;
    private final String manufacturer;
    private Category category;
    private double costPrice;
    private double sellPrice;
    private final int minStockThreshold;

    // ── IDENTITY ───────────────────
    private int specId;          // surrogate key - assigned by repository after add()

    // ── QUANTITY CACHE ────────────────────────────────
    private int totalQuantity;   // kept current by InventoryController after every stock mutation

    public ProductSpec(String name, String manufacturer,
                       Category category, double costPrice, double sellPrice,
                       int minStockThreshold) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            throw new IllegalArgumentException("Manufacturer cannot be null or empty");
        }
        if (costPrice < 0) {
            throw new IllegalArgumentException("Cost price cannot be negative");
        }
        if (sellPrice < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }
        if (minStockThreshold <= 0) {
            throw new IllegalArgumentException("Minimum stock threshold must be greater than zero");
        }

        this.name = name.trim();
        this.manufacturer = manufacturer.trim();
        this.category = category;
        this.costPrice = costPrice;
        this.sellPrice = sellPrice;
        this.minStockThreshold = minStockThreshold;

        if (category != null) {
            category.addProduct(this);
        }
    }

    public String getName() { return name; }
    public String getManufacturer() { return manufacturer; }
    public Category getCategory() { return category; }
    public double getCostPrice() { return costPrice; }
    public double getSellPrice() { return sellPrice; }
    public int getMinStockThreshold() { return minStockThreshold; }

    public void setSellPrice(double sellPrice) {
        if (sellPrice < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }
        this.sellPrice = sellPrice;
    }

    public void setCostPrice(double costPrice) {
        if (costPrice < 0) {
            throw new IllegalArgumentException("Cost price cannot be negative");
        }
        this.costPrice = costPrice;
    }

    // ── IDENTITY ──────────────────────────────────────────────

    /**
     * Surrogate key for ProductSpec.
     * Called by repository immediately after the spec is registered.
     */
    public int getSpecId() {
        return specId;
    }

    public void setSpecId(int specId) {
        if (specId <= 0) throw new IllegalArgumentException("specId must be > 0");
        this.specId = specId;
    }

    // ── QUANTITY CACHE ────────────────────────────────────────

    /**
     * Total units across all StockItems for this spec.
     * InventoryController calls adjustQuantity() after every stock mutation
     * so this value is always current - no repo lookup needed.
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Called by InventoryController after: addStockItem, updateQuantity,
     * reportDefective, removeExpiredStock.
     * delta positive = stock added, negative = stock removed.
     * Throws if the result would go below zero.
     */
    public void adjustQuantity(int delta) {
        if (totalQuantity + delta < 0)
            throw new IllegalArgumentException("Stock would go negative: current=" + totalQuantity + " delta=" + delta);
        totalQuantity += delta;
    }
}
