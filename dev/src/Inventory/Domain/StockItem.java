package Inventory.Domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockItem {
    private final ProductSpec spec;
    private final Area area;
    private final int shelfNumber;
    private final int rowNumber;
    private int quantity;
    private final LocalDate expiryDate;
    private final List<Integer> productIds;

    public StockItem(ProductSpec spec, Area area, int shelfNumber,
                     int rowNumber, int quantity, LocalDate expiryDate) {
        this(spec, area, shelfNumber, rowNumber, quantity, expiryDate, new ArrayList<>());
    }

    public StockItem(ProductSpec spec, Area area, int shelfNumber,
                     int rowNumber, int quantity, LocalDate expiryDate,
                     List<Integer> productIds) {
        if (spec == null) {
            throw new IllegalArgumentException("Spec must not be null");
        }
        if (area == null) {
            throw new IllegalArgumentException("Area must not be null");
        }
        if (shelfNumber < 1) {
            throw new IllegalArgumentException("Shelf number must be >= 1");
        }
        if (rowNumber < 1) {
            throw new IllegalArgumentException("Row number must be >= 1");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        if (productIds == null) {
            throw new IllegalArgumentException("Product IDs must not be null");
        }
        if (!productIds.isEmpty() && productIds.size() != quantity) {
            throw new IllegalArgumentException("Product ID count must match quantity");
        }
        this.spec = spec;
        this.area = area;
        this.shelfNumber = shelfNumber;
        this.rowNumber = rowNumber;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.productIds = new ArrayList<>(productIds);
    }

    public ProductSpec getSpec() { return spec; }
    public Area getArea() { return area; }
    public int getShelfNumber() { return shelfNumber; }
    public int getRowNumber() { return rowNumber; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public List<Integer> getProductIds() { return Collections.unmodifiableList(productIds); }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        this.quantity = quantity;
        if (!productIds.isEmpty() && productIds.size() > quantity) {
            productIds.subList(quantity, productIds.size()).clear();
        }
    }

    public void addProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be > 0");
        }
        productIds.add(productId);
        quantity = productIds.size();
    }

    public List<Integer> removeProductIds(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        if (count > quantity) {
            throw new IllegalArgumentException("Not enough stock. Available: " + quantity);
        }
        if (productIds.isEmpty()) {
            quantity -= count;
            return Collections.emptyList();
        }
        List<Integer> removed = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            removed.add(productIds.remove(productIds.size() - 1));
        }
        quantity = productIds.size();
        return removed;
    }
}
