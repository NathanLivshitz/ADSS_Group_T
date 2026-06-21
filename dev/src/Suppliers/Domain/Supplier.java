package Suppliers.Domain;

public class Supplier {

    private final int supplierID;
    private final String supplierName;

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public Supplier(int supplierID, String supplierName) {
        // TODO: validate supplierID > 0
        // TODO: validate supplierName not null or empty
        // TODO: assign fields
    }

    // ── GETTERS ───────────────────────────────────────────────

    public int getSupplierID() {
        // TODO: return supplierID
        return 0;
    }

    public String getSupplierName() {
        // TODO: return supplierName
        return null;
    }

    // ── MOCK BEHAVIOUR ────────────────────────────────────────

    /**
     * Mock: called by SupplierController.registerAndTransmitOrder().
     * Simulates the supplier receiving and acknowledging the order.
     */
    public void receiveOrder(Order o) {
        // TODO: mock no-op — in a real system this would notify the supplier
    }
}
