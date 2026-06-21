package Suppliers.Service;

import Suppliers.Domain.*;
import java.util.List;

public class SupplierService {
    private final SupplierController supplierSystem;

    public SupplierService(SupplierController supplierSystem) {
        if (supplierSystem == null)
            throw new IllegalArgumentException("supplierSystem cannot be null");
        this.supplierSystem = supplierSystem;
    }

    public OrderProposal createOrderProposal(int productSpecId, int requiredQty) {
        return supplierSystem.createOrderProposal(productSpecId, requiredQty);
    }

    public OrderProposal getOrderProposal(int id) {
        return supplierSystem.getOrderProposal(id);
    }

    public Order createOrder(int proposalId) {
        OrderProposal proposal = supplierSystem.getOrderProposal(proposalId);
        Order order = Order.create(proposal, proposal.getPrice());
        return supplierSystem.registerAndTransmitOrder(order);
    }

    public List<Supplier> getSuppliers() { return supplierSystem.getSuppliers(); }

    public Supplier addSupplier(int id, String name) { return supplierSystem.addSupplier(id, name); }

    public void addAgreement(SupplyAgreement a) { supplierSystem.addAgreement(a); }
}
