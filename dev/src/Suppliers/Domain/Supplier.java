package Suppliers.Domain;

public class Supplier {
    private final int supplierID;
    private final String supplierName;

    public Supplier(int supplierID, String supplierName) {
        if (supplierID <= 0) throw new IllegalArgumentException("supplierID must be > 0");
        if (supplierName == null || supplierName.trim().isEmpty())
            throw new IllegalArgumentException("supplierName cannot be empty");
        this.supplierID = supplierID;
        this.supplierName = supplierName;
    }

    public int getSupplierID() { return supplierID; }
    public String getSupplierName() { return supplierName; }
    public void receiveOrder(Order o) {}
}
