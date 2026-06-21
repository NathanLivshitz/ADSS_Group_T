package Suppliers.Domain;

public class OrderItem {

    private final int productSpecId;   // links to ProductSpec.specId in Inventory
    private final int quantity;
    private final double unitPrice;

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public OrderItem(int productSpecId, int quantity, double unitPrice) {
        // TODO: validate productSpecId > 0
        // TODO: validate quantity > 0
        // TODO: validate unitPrice >= 0
        // TODO: assign fields
    }

    // ── PRICING ───────────────────────────────────────────────

    /**
     * Total cost for this line item: quantity * unitPrice.
     */
    public double calculateTotalPrice() {
        // TODO: return quantity * unitPrice
        return 0;
    }

    // ── GETTERS ───────────────────────────────────────────────

    public int getProductSpecId() {
        // TODO: return productSpecId
        return 0;
    }

    public int getQuantity() {
        // TODO: return quantity
        return 0;
    }

    public double getUnitPrice() {
        // TODO: return unitPrice
        return 0;
    }
}
