package Inventory.Service;

import Shared.DTO.OrderSummaryDTO;
import Shared.DTO.SupplierScheduleDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Daemon thread that places periodic supplier orders automatically.
 *
 * On every tick it checks each supplier with a fixed delivery schedule:
 * if the next delivery falls within {@code config.leadDays} from today,
 * it calls {@link InventoryService#checkAndPlacePeriodicOrders} for all
 * low-stock products covered by that supplier's agreements.
 *
 * Wire-up in Main:
 * <pre>
 *   SchedulerConfig config = new SchedulerConfig()
 *       .intervalSeconds(10)   // demo; use 86400 for production
 *       .leadDays(2);
 *
 *   PeriodicOrderScheduler scheduler = new PeriodicOrderScheduler(service, config);
 *   scheduler.start();
 * </pre>
 */
public class PeriodicOrderScheduler implements Runnable {

    private final InventoryService service;
    private final SchedulerConfig  config;
    private volatile boolean running = true;

    // supplierId → delivery date for which we already placed an order this window.
    // Cleared implicitly when nextDeliveryDate() advances past the recorded date.
    private final Map<Integer, LocalDate> lastOrderedDelivery = new HashMap<>();

    public PeriodicOrderScheduler(InventoryService service, SchedulerConfig config) {
        if (service == null) throw new IllegalArgumentException("service cannot be null");
        if (config == null)  throw new IllegalArgumentException("config cannot be null");
        this.service = service;
        this.config  = config;
    }

    public void start() {
        Thread t = new Thread(this, "periodic-order-scheduler");
        t.setDaemon(true);
        t.start();
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            checkAndPlace();
            try {
                TimeUnit.SECONDS.sleep(config.getIntervalSeconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    void checkAndPlace() {
        LocalDate cutoff = LocalDate.now().plusDays(config.getLeadDays());

        for (SupplierScheduleDTO supplier : service.getSuppliersWithSchedules()) {
            LocalDate next = nextDeliveryDate(supplier.deliveryDays());
            if (next == null || next.isAfter(cutoff)) continue;

            // Skip if we already placed an order for this exact delivery date.
            // When the delivery date advances, nextDeliveryDate() returns a new date
            // and the guard clears naturally.
            if (next.equals(lastOrderedDelivery.get(supplier.supplierId()))) continue;

            List<OrderSummaryDTO> placed =
                service.checkAndPlacePeriodicOrders(supplier.supplierId());

            if (!placed.isEmpty()) {
                lastOrderedDelivery.put(supplier.supplierId(), next);
                for (OrderSummaryDTO o : placed) {
                    System.out.printf(
                        "[Scheduler] Auto-order #%d → supplier %d (%s), delivery %s, total %.2f%n",
                        o.orderId(), o.supplierId(), supplier.name(),
                        o.expectedDeliveryDate(), o.totalPrice());
                }
            }
        }
    }

    private LocalDate nextDeliveryDate(List<String> dayNames) {
        List<DayOfWeek> days = new ArrayList<>();
        for (String name : dayNames) {
            try { days.add(DayOfWeek.valueOf(name)); }
            catch (IllegalArgumentException ignored) {}
        }
        if (days.isEmpty()) return null;
        LocalDate candidate = LocalDate.now();
        for (int i = 0; i <= 7; i++, candidate = candidate.plusDays(1))
            for (DayOfWeek dow : days)
                if (candidate.getDayOfWeek() == dow) return candidate;
        return null;
    }
}
