package Suppliers.Domain;

import java.time.DayOfWeek;
import java.util.*;

public class SupplierController {
    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<SupplyAgreement> agreements = new ArrayList<>();
    private final Map<Integer, OrderProposal> proposals = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<DeliverySchedule> schedules = new ArrayList<>();
    private int nextProposalId = 1;
    private int nextOrderId = 1;

    public OrderProposal createOrderProposal(int productSpecId, int requiredQty) {
        List<SupplyAgreement> matching = findAgreements(productSpecId);
        if (matching.isEmpty())
            throw new IllegalArgumentException("No agreement found for specId " + productSpecId);
        SupplyAgreement best = matching.get(0);
        for (SupplyAgreement a : matching)
            if (a.priceFor(requiredQty) < best.priceFor(requiredQty)) best = a;
        OrderProposal p = new OrderProposal(nextProposalId++, productSpecId, requiredQty,
                best.getSupplier().getSupplierID(), best.priceFor(requiredQty));
        registerProposal(p);
        return p;
    }

    public List<SupplyAgreement> findAgreements(int productSpecId) {
        List<SupplyAgreement> result = new ArrayList<>();
        for (SupplyAgreement a : agreements)
            if (a.getProductSpecId() == productSpecId) result.add(a);
        return result;
    }

    public void registerProposal(OrderProposal p) { proposals.put(p.getOrderProposalId(), p); }

    public OrderProposal getOrderProposal(int id) {
        OrderProposal p = proposals.get(id);
        if (p == null) throw new IllegalArgumentException("Proposal not found: " + id);
        return p;
    }

    public Order registerAndTransmitOrder(Order o) {
        assignUniqueOrderId(o);
        recordOrder(o);
        for (Supplier s : suppliers)
            if (s.getSupplierID() == o.getSupplierId()) { s.receiveOrder(o); break; }
        o.changeStatus(OrderStatus.SENT);
        return o;
    }

    public void assignUniqueOrderId(Order o) { o.setOrderId(nextOrderId++); }
    public void recordOrder(Order o) { orders.add(o); }

    public List<Supplier> getSuppliers() { return Collections.unmodifiableList(suppliers); }

    public Supplier addSupplier(int id, String name) {
        Supplier s = new Supplier(id, name);
        suppliers.add(s);
        return s;
    }

    public void addAgreement(SupplyAgreement a) {
        if (a == null) throw new IllegalArgumentException("agreement cannot be null");
        agreements.add(a);
    }

    public List<DayOfWeek> getFixedDeliveryDays(int supplierId) {
        List<DayOfWeek> days = new ArrayList<>();
        for (DeliverySchedule d : schedules)
            if (d.getSupplier().getSupplierID() == supplierId) days.add(d.getDeliveryDay());
        return days;
    }

    public void addSchedule(DeliverySchedule d) {
        if (d == null) throw new IllegalArgumentException("schedule cannot be null");
        schedules.add(d);
    }

    public List<SupplyAgreement> getAgreementsForSupplier(int supplierId) {
        List<SupplyAgreement> result = new ArrayList<>();
        for (SupplyAgreement a : agreements)
            if (a.getSupplier().getSupplierID() == supplierId) result.add(a);
        return result;
    }

    public List<Supplier> getSuppliersWithSchedules() {
        Set<Integer> seen = new HashSet<>();
        List<Supplier> result = new ArrayList<>();
        for (DeliverySchedule d : schedules) {
            int id = d.getSupplier().getSupplierID();
            if (seen.add(id)) result.add(d.getSupplier());
        }
        return result;
    }
}
