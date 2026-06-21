package Suppliers.Domain;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class Order {

    private int orderId;                        // assigned by SupplierController
    private final int supplierId;
    private final List<OrderItem> items;
    private final OrderStatus status_initial = OrderStatus.DRAFT;
    private OrderStatus status;
    private final OrderType orderType;
    private final LocalDate creationDate;
    private final LocalDate expectedDeliveryDate;

    // ── CONSTRUCTOR (package-private — use static factory) ────

    Order(int orderId, int supplierId, List<OrderItem> items,
          OrderStatus status, OrderType orderType,
          LocalDate creationDate, LocalDate expectedDeliveryDate) {
        // TODO: validate supplierId > 0
        // TODO: validate items not null or empty
        // TODO: validate status, orderType, creationDate, expectedDeliveryDate not null
        // TODO: assign fields
    }

    // ── FACTORY ───────────────────────────────────────────────

    /**
     * Creates a SHORTAGE order from an approved proposal.
     * Called by SupplierController.registerAndTransmitOrder().
     * orderId starts at 0 — SupplierController assigns the real ID via assignUniqueOrderId().
     */
    public static Order create(OrderProposal proposal, double unitPrice) {
        // TODO: build OrderItem from proposal.getProductSpecId(), proposal.getRequiredQty(), unitPrice
        // TODO: return new Order(0, proposal.getSupplierId(), items,
        //           OrderStatus.DRAFT, OrderType.SHORTAGE,
        //           LocalDate.now(), LocalDate.now().plusDays(7))
        return null;
    }

    // ── STATE ─────────────────────────────────────────────────

    /**
     * Transitions order to a new status.
     * Called by SupplierController after transmitting to supplier → SENT.
     */
    public void changeStatus(OrderStatus s) {
        // TODO: validate s not null
        // TODO: this.status = s
    }

    public void setOrderId(int orderId) {
        // TODO: validate orderId > 0
        // TODO: this.orderId = orderId
    }

    // ── PRICING ───────────────────────────────────────────────

    /**
     * Sums calculateTotalPrice() across all OrderItems.
     */
    public double calculateTotalPrice() {
        // TODO: sum item.calculateTotalPrice() for each item in items
        return 0;
    }

    // ── GETTERS ───────────────────────────────────────────────

    public int getOrderId() {
        // TODO: return orderId
        return 0;
    }

    public int getSupplierId() {
        // TODO: return supplierId
        return 0;
    }

    public List<OrderItem> getItems() {
        // TODO: return Collections.unmodifiableList(items)
        return null;
    }

    public double getTotalPrice() {
        // TODO: return calculateTotalPrice()
        return 0;
    }

    public OrderStatus getStatus() {
        // TODO: return status
        return null;
    }

    public OrderType getOrderType() {
        // TODO: return orderType
        return null;
    }

    public LocalDate getCreationDate() {
        // TODO: return creationDate
        return null;
    }

    public LocalDate getExpectedDeliveryDate() {
        // TODO: return expectedDeliveryDate
        return null;
    }
}
