package Suppliers.Domain;

public class SupplyAgreement {

    private final Supplier supplier;
    private final int productSpecId;   // links to ProductSpec.specId in Inventory
    private final int minQuantity;
    private final double unitPrice;

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public SupplyAgreement(Supplier supplier, int productSpecId,
                           int minQuantity, double unitPrice) {
        // TODO: validate supplier not null
        // TODO: validate productSpecId > 0
        // TODO: validate minQuantity > 0
        // TODO: validate unitPrice >= 0
        // TODO: assign fields
    }

    // ── PRICING ───────────────────────────────────────────────

    /**
     * Returns the unit price for the given quantity.
     * Mock: flat price — no tiered pricing in this implementation.
     * INV-9: cost price is sourced from priceFor() when an order is confirmed.
     */
    public double priceFor(int qty) {
        // TODO: return unitPrice (flat mock — no tiers)
        return 0;
    }

    // ── GETTERS ───────────────────────────────────────────────

    public Supplier getSupplier() {
        // TODO: return supplier
        return null;
    }

    public int getProductSpecId() {
        // TODO: return productSpecId
        return 0;
    }

    public int getMinQuantity() {
        // TODO: return minQuantity
        return 0;
    }

    public double getUnitPrice() {
        // TODO: return unitPrice
        return 0;
    }
}
