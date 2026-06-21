package Suppliers.Domain;

public class OrderItem {
    private final int productSpecId;
    private final int quantity;
    private final double unitPrice;

    public OrderItem(int productSpecId, int quantity, double unitPrice) {
        if (productSpecId <= 0) throw new IllegalArgumentException("productSpecId must be > 0");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (unitPrice < 0) throw new IllegalArgumentException("unitPrice cannot be negative");
        this.productSpecId = productSpecId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double calculateTotalPrice() { return quantity * unitPrice; }
    public int getProductSpecId() { return productSpecId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
}
