package Suppliers.Domain;

public class SupplierOffer {
    private final Supplier supplier;
    private final double totalPrice;

    public SupplierOffer(Supplier supplier, double totalPrice) {
        if (supplier == null) throw new IllegalArgumentException("supplier cannot be null");
        if (totalPrice < 0) throw new IllegalArgumentException("totalPrice cannot be negative");
        this.supplier = supplier;
        this.totalPrice = totalPrice;
    }

    public Supplier getSupplier() { return supplier; }
    public double getTotalPrice() { return totalPrice; }
}
