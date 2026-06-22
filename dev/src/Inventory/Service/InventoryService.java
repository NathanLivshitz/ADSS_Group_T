package Inventory.Service;

import Inventory.DTO.*;
import Inventory.Domain.InventoryController;
import Shared.DTO.*;
import Suppliers.Domain.*;
import Suppliers.Service.SupplierService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class InventoryService {
    private final InventoryController controller;
    private SupplierService supplierService;

    public InventoryService(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
        this.supplierService = defaultSupplierService();
    }

    public InventoryService(InventoryController controller, SupplierService supplierService) {
        this(controller);
        this.supplierService = supplierService;
    }

    private static SupplierService defaultSupplierService() {
        SupplierController sc = new SupplierController();
        Supplier s1 = sc.addSupplier(1, "ACME Supplies");
        Supplier s2 = sc.addSupplier(2, "Global Foods");
        sc.addSchedule(new DeliverySchedule(s1, DayOfWeek.MONDAY));
        sc.addSchedule(new DeliverySchedule(s2, DayOfWeek.WEDNESDAY));
        sc.addAgreement(new SupplyAgreement(s1, 1, 10, 5.00));
        sc.addAgreement(new SupplyAgreement(s1, 2, 5,  8.50));
        sc.addAgreement(new SupplyAgreement(s2, 1, 10, 4.80));
        sc.addAgreement(new SupplyAgreement(s2, 3, 20, 3.00));
        return new SupplierService(sc);
    }

    // ── Catalog ────────────────────────────────────────────

    public int addProduct(ProductDTO dto) {
        return controller.addProduct(dto);
    }

    public ProductDTO getProduct(int id) {
        return controller.getProduct(id);
    }

    public List<ProductDTO> getLowStockProducts() {
        return controller.getLowStockProducts();
    }

    // ── Stock ──────────────────────────────────────────────

    public void addStockItem(StockItemDTO dto) {
        controller.addStockItem(dto);
    }

    public List<StockItemDTO> getStockForProduct(int productId) {
        return controller.getStockForProduct(productId);
    }

    public void updateQuantity(int productId, String area, int shelf, int row, int delta) {
        controller.updateQuantity(productId, area, shelf, row, delta);
    }

    // ── Categories ─────────────────────────────────────────

    public int addCategory(String name, int parentCategoryId) {
        return controller.addCategory(name, parentCategoryId);
    }

    public List<CategoryDTO> getRootCategories() {
        return controller.getRootCategories();
    }

    public CategoryDTO findCategoryByName(String name) {
        return controller.findCategoryByName(name);
    }

    // ── Promotions ─────────────────────────────────────────

    public void addPromotion(PromotionDTO dto) {
        controller.addPromotion(dto);
    }

    public List<PromotionDTO> getActivePromotions() {
        return controller.getActivePromotions();
    }

    public double getEffectivePrice(int productId) {
        return controller.getEffectivePrice(productId);
    }

    // ── Defectives ─────────────────────────────────────────

    public void reportDefective(int productId, int quantity, String reason) {
        if (quantity <= 0)
            throw new IllegalArgumentException("quantity must be positive");
        if (reason == null || reason.trim().isEmpty())
            throw new IllegalArgumentException("reason cannot be empty");
        controller.reportDefective(productId, quantity, reason);
    }

    public int removeExpiredStock() {
        return controller.removeExpiredStock();
    }

    public List<DefectiveLocationDTO> getDefectiveItemsWithLocations() {
        List<DefectiveLocationDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<StockItemDTO>> e : controller.getDefectiveItemsWithLocations().entrySet())
            result.add(new DefectiveLocationDTO(e.getKey(), e.getValue()));
        return result;
    }

    public List<DefectiveReportDTO> getDefectiveReports(LocalDate from, LocalDate to) {
        return controller.getDefectiveReports(from, to);
    }

    // ── Reports ────────────────────────────────────────────

    public List<ProductDTO> generateInventoryReport(List<Integer> categoryIds) {
        return controller.generateInventoryReport(categoryIds);
    }

    // ── Cross-module (INV-3 trigger + INV-9 confirm) ───────

    /**
     * INV-3: Returns the current shortage list (products below min threshold).
     * User then picks one via selectProduct(specId).
     */
    public List<ProductDTO> orderShortageFromSupplier() {
        return controller.getLowStockProducts();
    }

    /**
     * INV-3 / Contract 2: User selected a product from the shortage list.
     * Computes requiredQty, forwards to SupplierService to find cheapest supplier
     * and create an OrderProposal. Returns the proposal for user approval.
     */
    public OrderProposalDTO selectProduct(int specId) {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        ProductDTO product = controller.getProductBySpecId(specId);
        if (product == null)
            throw new IllegalArgumentException("No product with specId " + specId);
        int requiredQty = product.minStockThreshold() - product.totalQuantity();
        if (requiredQty <= 0)
            throw new IllegalArgumentException("Product specId=" + specId + " is not below threshold");
        OrderProposal p = supplierService.createOrderProposal(specId, requiredQty);
        return new OrderProposalDTO(p.getOrderProposalId(), p.getSupplierId(), p.getRequiredQty(), p.getPrice());
    }

    /**
     * INV-9 / Contract 3: User confirmed the order proposal.
     * Creates and transmits the order via SupplierService, then updates
     * cost price and total quantity in the inventory (updateShortageReport).
     */
    public OrderSummaryDTO confirmOrder(int orderProposalId) {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        Order order = supplierService.createOrder(orderProposalId);
        for (OrderItem item : order.getItems())
            controller.updateShortageReport(item.getProductSpecId(), item.getQuantity(), item.getUnitPrice());
        return new OrderSummaryDTO(order.getOrderId(), order.getSupplierId(), order.getItems().size(),
                order.getTotalPrice(), order.getOrderType().toString(),
                order.getExpectedDeliveryDate().toString());
    }

    /** Returns one SupplierScheduleDTO per supplier that has fixed delivery days. */
    public List<SupplierScheduleDTO> getSuppliersWithSchedules() {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        List<SupplierScheduleDTO> result = new ArrayList<>();
        for (Supplier s : supplierService.getSuppliersWithSchedules()) {
            List<String> days = new ArrayList<>();
            for (DayOfWeek d : supplierService.getDeliveryDays(s.getSupplierID()))
                days.add(d.toString());
            result.add(new SupplierScheduleDTO(s.getSupplierID(), s.getSupplierName(), days));
        }
        return result;
    }

    /**
     * UC-e preview: returns a human-readable summary of what periodic orders would be
     * generated right now (no side effects). Used to show the user before confirming.
     */
    public List<String> describePeriodicOrders() {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        List<String> lines = new ArrayList<>();
        List<ProductDTO> lowStock = controller.getLowStockProducts();
        for (Supplier s : supplierService.getSuppliersWithSchedules()) {
            LocalDate nextDelivery = nextDeliveryDate(s.getSupplierID());
            List<SupplyAgreement> agreements = supplierService.getAgreementsForSupplier(s.getSupplierID());
            List<String> items = new ArrayList<>();
            for (ProductDTO p : lowStock) {
                for (SupplyAgreement a : agreements) {
                    if (a.getProductSpecId() == p.specId()) {
                        int qty = p.minStockThreshold() - p.totalQuantity() + 1;
                        items.add(String.format("specId=%d qty=%d price=%.2f", p.specId(), qty, a.priceFor(qty)));
                        break;
                    }
                }
            }
            if (!items.isEmpty())
                lines.add(String.format("Supplier [%d] %s - delivery %s - items: %s",
                        s.getSupplierID(), s.getSupplierName(), nextDelivery, items));
        }
        return lines;
    }

    /**
     * UC-e submit: creates and transmits periodic orders for all scheduled suppliers
     * whose agreements cover at least one low-stock product.
     */
    public List<OrderSummaryDTO> submitAllPeriodicOrders() {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        List<OrderSummaryDTO> placed = new ArrayList<>();
        List<ProductDTO> lowStock = controller.getLowStockProducts();
        for (Supplier s : supplierService.getSuppliersWithSchedules()) {
            LocalDate nextDelivery = nextDeliveryDate(s.getSupplierID());
            List<SupplyAgreement> agreements = supplierService.getAgreementsForSupplier(s.getSupplierID());
            List<OrderItem> items = new ArrayList<>();
            for (ProductDTO p : lowStock) {
                for (SupplyAgreement a : agreements) {
                    if (a.getProductSpecId() == p.specId()) {
                        int qty = p.minStockThreshold() - p.totalQuantity() + 1;
                        items.add(new OrderItem(p.specId(), qty, a.priceFor(qty)));
                        break;
                    }
                }
            }
            if (items.isEmpty()) continue;
            Order order = supplierService.createPeriodicOrder(s.getSupplierID(), items, nextDelivery);
            for (OrderItem item : order.getItems())
                controller.updateShortageReport(item.getProductSpecId(), item.getQuantity(), item.getUnitPrice());
            placed.add(new OrderSummaryDTO(order.getOrderId(), order.getSupplierId(), order.getItems().size(),
                    order.getTotalPrice(), order.getOrderType().toString(),
                    order.getExpectedDeliveryDate().toString()));
        }
        return placed;
    }

    private LocalDate nextDeliveryDate(int supplierId) {
        List<DayOfWeek> days = supplierService.getDeliveryDays(supplierId);
        if (days.isEmpty())
            throw new IllegalStateException("Supplier " + supplierId + " has no delivery days");
        LocalDate today = LocalDate.now();
        LocalDate earliest = null;
        for (DayOfWeek day : days) {
            LocalDate next = today.with(TemporalAdjusters.next(day)); // always tomorrow+
            if (earliest == null || next.isBefore(earliest)) earliest = next;
        }
        return earliest;
    }

    // ── Reset ──────────────────────────────────────────────

    public void reset() {
        controller.reset();
    }

    // ── Cross-module (called by SupplierService) ───────────

    public void updateShortageReport(int specId, int orderedQty, double unitPrice) {
        controller.updateShortageReport(specId, orderedQty, unitPrice);
    }
}
