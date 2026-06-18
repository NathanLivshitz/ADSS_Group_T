package Inventory.Domain;

import java.time.LocalDate;

public class StockItem {
    private final ProductSpec spec;
    private final Area area;
    private final int shelfNumber;
    private final int rowNumber;
    private int quantity;
    private final LocalDate expiryDate;

    public StockItem(ProductSpec spec, Area area, int shelfNumber,
                     int rowNumber, int quantity, LocalDate expiryDate) {
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
        this.spec = spec;
        this.area = area;
        this.shelfNumber = shelfNumber;
        this.rowNumber = rowNumber;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    public ProductSpec getSpec() { return spec; }
    public Area getArea() { return area; }
    public int getShelfNumber() { return shelfNumber; }
    public int getRowNumber() { return rowNumber; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        this.quantity = quantity;
    }
}
