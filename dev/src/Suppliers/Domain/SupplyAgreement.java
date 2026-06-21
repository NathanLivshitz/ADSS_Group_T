package Suppliers.Domain;

public class SupplyAgreement {
    private final Supplier supplier;
    private final int productSpecId;
    private final int minQuantity;
    private final double unitPrice;

    public SupplyAgreement(Supplier supplier, int productSpecId, int minQuantity, double unitPrice) {
        if (supplier == null) throw new IllegalArgumentException("supplier cannot be null");
        if (productSpecId <= 0) throw new IllegalArgumentException("productSpecId must be > 0");
        if (minQuantity <= 0) throw new IllegalArgumentException("minQuantity must be > 0");
        if (unitPrice < 0) throw new IllegalArgumentException("unitPrice cannot be negative");
        this.supplier = supplier;
        this.productSpecId = productSpecId;
        this.minQuantity = minQuantity;
        this.unitPrice = unitPrice;
    }

    public double priceFor(int qty) { return unitPrice; }
    public Supplier getSupplier() { return supplier; }
    public int getProductSpecId() { return productSpecId; }
    public int getMinQuantity() { return minQuantity; }
    public double getUnitPrice() { return unitPrice; }
}
