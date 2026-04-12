package Inventory.Domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private String manufacturer;
    private Category category;
    private double costPrice;
    private double sellPrice;
    private int minStockThreshold;
    private LocalDate expiryDate;
    private List<ProductLocation> locations;

    public Product(int id, String name, String manufacturer, Category category,
                   double costPrice, double sellPrice, int minStockThreshold,
                   LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
        this.costPrice = costPrice;
        this.sellPrice = sellPrice;
        this.minStockThreshold = minStockThreshold;
        this.expiryDate = expiryDate;
        this.locations = new ArrayList<>();
        category.addProduct(this);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getManufacturer() { return manufacturer; }
    public Category getCategory() { return category; }
    public double getCostPrice() { return costPrice; }
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
    public int getMinStockThreshold() { return minStockThreshold; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public List<ProductLocation> getLocations() { return locations; }

    public void addLocation(ProductLocation location) {
        locations.add(location);
    }

    public int getTotalQuantity() {
        return locations.stream().mapToInt(ProductLocation::getQuantity).sum();
    }

    public int getStoreQuantity() {
        return locations.stream()
                .filter(l -> l.getArea() == Area.STORE)
                .mapToInt(ProductLocation::getQuantity).sum();
    }

    public int getWarehouseQuantity() {
        return locations.stream()
                .filter(l -> l.getArea() == Area.WAREHOUSE)
                .mapToInt(ProductLocation::getQuantity).sum();
    }

    public boolean isBelowMinStock() {
        return getTotalQuantity() < minStockThreshold;
    }
}
