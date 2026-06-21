package Suppliers.Service;

import Suppliers.Domain.*;
import java.util.List;

public class SupplierService {

    private final SupplierController supplierSystem;

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public SupplierService(SupplierController supplierSystem) {
        // TODO: validate supplierSystem not null
        // TODO: assign field
    }

    // ── PROPOSALS ─────────────────────────────────────────────

    /**
     * Called by InventoryService.orderShortageFromSupplier().
     * Delegates to SupplierController to find the best agreement and register a proposal.
     */
    public OrderProposal createOrderProposal(int productSpecId, int requiredQty) {
        // TODO: return supplierSystem.createOrderProposal(productSpecId, requiredQty)
        return null;
    }

    public OrderProposal getOrderProposal(int id) {
        // TODO: return supplierSystem.getOrderProposal(id)
        return null;
    }

    // ── ORDERS ────────────────────────────────────────────────

    /**
     * Called by InventoryService.confirmOrder().
     * Builds and transmits the order; returns it so InventoryService can
     * call InventoryController.updateShortageReport() with the order details.
     */
    public Order createOrder(int proposalId) {
        // TODO: OrderProposal proposal = supplierSystem.getOrderProposal(proposalId)
        // TODO: Order order = Order.create(proposal, proposal.getPrice())
        // TODO: return supplierSystem.registerAndTransmitOrder(order)
        return null;
    }

    // ── SUPPLIERS ─────────────────────────────────────────────

    public List<Supplier> getSuppliers() {
        // TODO: return supplierSystem.getSuppliers()
        return null;
    }

    public Supplier addSupplier(int id, String name) {
        // TODO: return supplierSystem.addSupplier(id, name)
        return null;
    }

    public void addAgreement(SupplyAgreement a) {
        // TODO: supplierSystem.addAgreement(a)
    }
}
