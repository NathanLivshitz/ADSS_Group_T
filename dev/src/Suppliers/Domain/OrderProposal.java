package Suppliers.Domain;

public class OrderProposal {

    private final int orderProposalId;
    private final int productSpecId;   // links to ProductSpec.specId in Inventory
    private final int requiredQty;
    private final int supplierId;
    private final double price;        // unit price from the winning SupplyAgreement

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public OrderProposal(int orderProposalId, int productSpecId,
                         int requiredQty, int supplierId, double price) {
        // TODO: validate orderProposalId > 0
        // TODO: validate productSpecId > 0
        // TODO: validate requiredQty > 0
        // TODO: validate supplierId > 0
        // TODO: validate price >= 0
        // TODO: assign fields
    }

    // ── SUMMARY ───────────────────────────────────────────────

    /**
     * Human-readable summary used by the sequence: OrderProposal.getDetails().
     * Called by SupplierController before transmitting an order.
     */
    public String getDetails() {
        // TODO: return formatted string — proposalId, specId, qty, supplierId, price
        return null;
    }

    // ── GETTERS ───────────────────────────────────────────────

    public int getOrderProposalId() {
        // TODO: return orderProposalId
        return 0;
    }

    public int getProductSpecId() {
        // TODO: return productSpecId
        return 0;
    }

    public int getRequiredQty() {
        // TODO: return requiredQty
        return 0;
    }

    public int getSupplierId() {
        // TODO: return supplierId
        return 0;
    }

    public double getPrice() {
        // TODO: return price
        return 0;
    }
}
