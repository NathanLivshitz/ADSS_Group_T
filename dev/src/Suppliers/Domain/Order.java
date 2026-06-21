package Suppliers.Domain;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class Order {
    private int orderId;
    private final int supplierId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final OrderType orderType;
    private final LocalDate creationDate;
    private final LocalDate expectedDeliveryDate;

    Order(int orderId, int supplierId, List<OrderItem> items,
          OrderStatus status, OrderType orderType,
          LocalDate creationDate, LocalDate expectedDeliveryDate) {
        if (supplierId <= 0) throw new IllegalArgumentException("supplierId must be > 0");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("items cannot be empty");
        if (status == null || orderType == null || creationDate == null || expectedDeliveryDate == null)
            throw new IllegalArgumentException("status/orderType/dates cannot be null");
        this.orderId = orderId;
        this.supplierId = supplierId;
        this.items = items;
        this.status = status;
        this.orderType = orderType;
        this.creationDate = creationDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public static Order create(OrderProposal proposal, double unitPrice) {
        List<OrderItem> items = Collections.singletonList(
                new OrderItem(proposal.getProductSpecId(), proposal.getRequiredQty(), unitPrice));
        return new Order(0, proposal.getSupplierId(), items,
                OrderStatus.DRAFT, OrderType.SHORTAGE,
                LocalDate.now(), LocalDate.now().plusDays(7));
    }

    public static Order createPeriodic(int supplierId, List<OrderItem> items, LocalDate deliveryDate) {
        return new Order(0, supplierId, new java.util.ArrayList<>(items),
                OrderStatus.DRAFT, OrderType.PERIODIC,
                LocalDate.now(), deliveryDate);
    }

    public void changeStatus(OrderStatus s) {
        if (s == null) throw new IllegalArgumentException("status cannot be null");
        this.status = s;
    }

    public void setOrderId(int orderId) {
        if (orderId <= 0) throw new IllegalArgumentException("orderId must be > 0");
        this.orderId = orderId;
    }

    public double calculateTotalPrice() {
        double total = 0;
        for (OrderItem i : items) total += i.calculateTotalPrice();
        return total;
    }

    public int getOrderId() { return orderId; }
    public int getSupplierId() { return supplierId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public double getTotalPrice() { return calculateTotalPrice(); }
    public OrderStatus getStatus() { return status; }
    public OrderType getOrderType() { return orderType; }
    public LocalDate getCreationDate() { return creationDate; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
}
