package Suppliers.Domain;

import java.time.DayOfWeek;

public class DeliverySchedule {
    private final Supplier supplier;
    private final DayOfWeek deliveryDay;

    public DeliverySchedule(Supplier supplier, DayOfWeek deliveryDay) {
        if (supplier == null) throw new IllegalArgumentException("supplier cannot be null");
        if (deliveryDay == null) throw new IllegalArgumentException("deliveryDay cannot be null");
        this.supplier = supplier;
        this.deliveryDay = deliveryDay;
    }

    public Supplier getSupplier() { return supplier; }
    public DayOfWeek getDeliveryDay() { return deliveryDay; }
}
