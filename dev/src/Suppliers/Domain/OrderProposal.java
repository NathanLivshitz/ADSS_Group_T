package Suppliers.Domain;

public class OrderProposal {
    private final int orderProposalId;
    private final int productSpecId;
    private final int requiredQty;
    private final int supplierId;
    private final double price;

    public OrderProposal(int orderProposalId, int productSpecId, int requiredQty, int supplierId, double price) {
        if (orderProposalId <= 0) throw new IllegalArgumentException("orderProposalId must be > 0");
        if (productSpecId <= 0) throw new IllegalArgumentException("productSpecId must be > 0");
        if (requiredQty <= 0) throw new IllegalArgumentException("requiredQty must be > 0");
        if (supplierId <= 0) throw new IllegalArgumentException("supplierId must be > 0");
        if (price < 0) throw new IllegalArgumentException("price cannot be negative");
        this.orderProposalId = orderProposalId;
        this.productSpecId = productSpecId;
        this.requiredQty = requiredQty;
        this.supplierId = supplierId;
        this.price = price;
    }

    public String getDetails() {
        return "Proposal#" + orderProposalId + " spec=" + productSpecId
                + " qty=" + requiredQty + " supplier=" + supplierId + " price=" + price;
    }

    public int getOrderProposalId() { return orderProposalId; }
    public int getProductSpecId() { return productSpecId; }
    public int getRequiredQty() { return requiredQty; }
    public int getSupplierId() { return supplierId; }
    public double getPrice() { return price; }
}
