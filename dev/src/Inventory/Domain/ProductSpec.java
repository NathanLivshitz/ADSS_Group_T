package Inventory.Domain;

public class ProductSpec {
    private final String name;
    private final String manufacturer;
    private Category category;
    private double costPrice;
    private double sellPrice;
    private final int minStockThreshold;

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
}
