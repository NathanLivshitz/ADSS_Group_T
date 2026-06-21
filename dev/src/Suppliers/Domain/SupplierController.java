package Suppliers.Domain;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Mock controller — all data is held in-memory.
 * No persistence layer for Suppliers in this phase.
 */
public class SupplierController {

    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<SupplyAgreement> agreements = new ArrayList<>();
    private final Map<Integer, OrderProposal> proposals = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<DeliverySchedule> schedules = new ArrayList<>();

    private int nextProposalId = 1;
    private int nextOrderId = 1;

    // ── PROPOSALS ─────────────────────────────────────────────

    /**
     * Finds all agreements for a specId, picks cheapest, builds and registers a proposal.
     * Called by SupplierService.createOrderProposal().
     * Throws if no agreement exists for the given specId.
     */
    public OrderProposal createOrderProposal(int productSpecId, int requiredQty) {
        // TODO: call findAgreements(productSpecId) — throw if empty
        // TODO: pick agreement with lowest priceFor(requiredQty)
        // TODO: build OrderProposal(nextProposalId++, productSpecId, requiredQty, supplierId, price)
        // TODO: registerProposal(proposal)
        // TODO: return proposal
        return null;
    }

    /**
     * Returns all agreements whose productSpecId matches.
     */
    public List<SupplyAgreement> findAgreements(int productSpecId) {
        // TODO: filter agreements list by productSpecId
        return null;
    }

    public void registerProposal(OrderProposal p) {
        // TODO: proposals.put(p.getOrderProposalId(), p)
    }

    /**
     * Retrieves a previously registered proposal by ID.
     * Throws if not found.
     */
    public OrderProposal getOrderProposal(int id) {
        // TODO: return proposals.get(id) — throw if null
        return null;
    }

    // ── ORDERS ────────────────────────────────────────────────

    /**
     * Sequence: assignUniqueOrderId → recordOrder → supplier.receiveOrder → status=SENT.
     * Called by SupplierService.createOrder().
     */
    public Order registerAndTransmitOrder(Order o) {
        // TODO: assignUniqueOrderId(o)
        // TODO: recordOrder(o)
        // TODO: find supplier by o.getSupplierId(), call supplier.receiveOrder(o)
        // TODO: o.changeStatus(OrderStatus.SENT)
        // TODO: return o
        return null;
    }

    public void assignUniqueOrderId(Order o) {
        // TODO: o.setOrderId(nextOrderId++)
    }

    public void recordOrder(Order o) {
        // TODO: orders.add(o)
    }

    // ── SUPPLIERS ─────────────────────────────────────────────

    public List<Supplier> getSuppliers() {
        // TODO: return Collections.unmodifiableList(suppliers)
        return null;
    }

    /**
     * Creates and registers a new supplier.
     * Returns the created Supplier so the caller can link agreements to it.
     */
    public Supplier addSupplier(int id, String name) {
        // TODO: new Supplier(id, name), suppliers.add(s), return s
        return null;
    }

    public void addAgreement(SupplyAgreement a) {
        // TODO: validate a not null
        // TODO: agreements.add(a)
    }

    // ── DELIVERY SCHEDULES ────────────────────────────────────

    /**
     * Returns delivery days for a given supplier.
     * Used by periodic order flow.
     */
    public List<DayOfWeek> getFixedDeliveryDays(int supplierId) {
        // TODO: filter schedules by supplierId, collect deliveryDay values
        return null;
    }

    public void addSchedule(DeliverySchedule d) {
        // TODO: validate d not null
        // TODO: schedules.add(d)
    }
}
