package Inventory.Service;

import Inventory.Domain.InventoryController;
import Inventory.DTO.*;
import Shared.DTO.*;
import Suppliers.Service.SupplierService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Inventory.
 *
 * Acts as the integration point between Presentation and the Inventory Domain.
 * All public methods accept and return only DTOs.
 *
 * Uses SupplierService (imported) for cross-module calls.
 * SupplierService returns DTOs — InventoryService never touches Suppliers domain types.
 */
public class InventoryService {

    private final InventoryController controller;
    private final SupplierService supplierService;

    public InventoryService(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
        this.supplierService = new SupplierService();
    }

    /** Two-arg constructor for testing — allows injecting a pre-seeded SupplierService. */
    public InventoryService(InventoryController controller, SupplierService supplierService) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
        this.supplierService = supplierService != null ? supplierService : new SupplierService();
    }

    // Seeds the in-memory supplier mock with demo data so the shortage (UC-f) and periodic
    // (UC-e) order use cases are demonstrable. Called from Main. Suppliers are a mock with no
    // DB, so this demo data is wired here rather than loaded from the database.
    // Agreements assume the standard preloaded catalog (specIds 1-5); spec 5 (Bissli) is the
    // low-stock item and is supplied by two suppliers at different prices.
    public void seedDefaultSuppliers() {
        supplierService.addSupplier(1, "ACME Supplies");
        supplierService.addSupplier(2, "Global Foods");
        supplierService.addAgreement(new SupplyAgreementDTO(1, 1, 10, 5.00));
        supplierService.addAgreement(new SupplyAgreementDTO(2, 1, 5, 8.50));
        supplierService.addAgreement(new SupplyAgreementDTO(3, 1, 20, 3.00));
        supplierService.addAgreement(new SupplyAgreementDTO(4, 2, 30, 2.40));
        supplierService.addAgreement(new SupplyAgreementDTO(5, 1, 12, 3.50));
        supplierService.addAgreement(new SupplyAgreementDTO(5, 2, 12, 3.20));
        supplierService.addSchedule(1, "WEDNESDAY");
        supplierService.addSchedule(2, "MONDAY");
    }

    // ── Catalog ──────────────────────────────────────────────

    public int addProduct(ProductDTO dto) {
        return controller.addProduct(dto);
    }

    public ProductDTO getProduct(int specId) {
        return controller.getProduct(specId);
    }

    public List<ProductDTO> getLowStockProducts() {
        return controller.getLowStockProducts();
    }

    // ── Stock ────────────────────────────────────────────────

    public void addStockItem(StockItemDTO dto) {
        controller.addStockItem(dto);
    }

    public List<StockItemDTO> getStockForProduct(int specId) {
        return controller.getStockForProduct(specId);
    }

    public void updateQuantity(int specId, String area, int shelf, int row, int delta) {
        controller.updateQuantity(specId, area, shelf, row, delta);
    }

    // ── Categories ───────────────────────────────────────────

    public int addCategory(String name, int parentCategoryId) {
        return controller.addCategory(name, parentCategoryId);
    }

    public List<CategoryDTO> getRootCategories() {
        return controller.getRootCategories();
    }

    public CategoryDTO findCategoryByName(String name) {
        return controller.findCategoryByName(name);
    }

    // ── Promotions ───────────────────────────────────────────

    public void addPromotion(PromotionDTO dto) {
        controller.addPromotion(dto);
    }

    public List<PromotionDTO> getActivePromotions() {
        return controller.getActivePromotions();
    }

    public double getEffectivePrice(int productId) {
        return controller.getEffectivePrice(productId);
    }

    // ── Defectives ───────────────────────────────────────────

    public void reportDefective(int productId, int quantity, String reason) {
        controller.reportDefective(productId, quantity, reason);
    }

    public int removeExpiredStock() {
        return controller.removeExpiredStock();
    }

    /**
     * Returns defective items grouped by product as DTOs.
     * The controller returns a raw Map; we wrap it into DefectiveLocationDTOs.
     */
    public List<DefectiveLocationDTO> getDefectiveItemsWithLocations() {
        java.util.Map<Integer, List<StockItemDTO>> raw = controller.getDefectiveItemsWithLocations();
        List<DefectiveLocationDTO> result = new ArrayList<>();
        for (java.util.Map.Entry<Integer, List<StockItemDTO>> e : raw.entrySet()) {
            result.add(new DefectiveLocationDTO(e.getKey(), e.getValue()));
        }
        return result;
    }

    // ── Reports ──────────────────────────────────────────────

    public List<DefectiveReportDTO> getDefectiveReports(LocalDate from, LocalDate to) {
        return controller.getDefectiveReports(from, to);
    }

    public List<ProductDTO> generateInventoryReport(List<Integer> categoryIds) {
        return controller.generateInventoryReport(categoryIds);
    }

    /** Reset in-memory state. */
    public void reset() {
        controller.reset();
    }

    /** Pass-through for shortage report update. */
    public void updateShortageReport(int specId, int orderedQty, double unitPrice) {
        controller.updateShortageReport(specId, orderedQty, unitPrice);
    }

    // ── Cross-module: Ordering ───────────────────────────────

    /**
     * Order shortage items from the best available supplier.
     * Uses SupplierService which returns DTOs — no domain types touch this layer.
     */
    public List<ProductDTO> orderShortageFromSupplier() {
        List<ProductDTO> lowStock = controller.getLowStockProducts();
        if (lowStock.isEmpty()) return lowStock;

        List<ProductDTO> ordered = new ArrayList<>();
        for (ProductDTO product : lowStock) {
            int requiredQty = product.minStockThreshold() - product.totalQuantity();
            if (requiredQty <= 0) continue;

            OrderProposalDTO proposal = supplierService.createOrderProposal(product.specId(), requiredQty);
            if (proposal != null) {
                OrderSummaryDTO order = supplierService.createOrder(proposal.proposalId());
                if (order != null) {
                    controller.updateShortageReport(product.specId(), requiredQty, proposal.unitPrice());
                    ordered.add(product);
                }
            }
        }
        return ordered;
    }

    /**
     * Create an order proposal for a specific low-stock product.
     * specId must identify a product that is currently below its minimum threshold.
     */
    public OrderProposalDTO selectProduct(int specId) {
        ProductDTO product = controller.getProductBySpecId(specId);
        if (product == null)
            throw new IllegalArgumentException("Product not found: " + specId);
        int requiredQty = product.minStockThreshold() - product.totalQuantity();
        if (requiredQty <= 0)
            throw new IllegalStateException("Product " + specId + " is not below threshold");
        return supplierService.createOrderProposal(specId, requiredQty);
    }

    /**
     * Confirm a selected order proposal.
     * SupplierService returns OrderSummaryDTO directly.
     */
    public OrderSummaryDTO confirmOrder(int proposalId) {
        OrderSummaryDTO order = supplierService.createOrder(proposalId);
        if (order == null)
            throw new IllegalArgumentException("Order proposal not found: " + proposalId);

        // resolve the spec from the proposal to update inventory
        OrderProposalDTO proposal = supplierService.getOrderProposal(proposalId);
        if (proposal != null) {
            ProductDTO product = controller.getProductBySpecId(proposal.productSpecId());
            if (product != null) {
                int requiredQty = product.minStockThreshold() - product.totalQuantity();
                controller.updateShortageReport(product.specId(), requiredQty, order.totalPrice() / Math.max(requiredQty, 1));
            }
        }
        return order;
    }

    /**
     * Get suppliers with their delivery schedules.
     * SupplierService returns DTOs directly.
     */
    public List<SupplierScheduleDTO> getSuppliersWithSchedules() {
        return supplierService.getSuppliersWithSchedules();
    }

    // ── Periodic Ordering ────────────────────────────────────

    /**
     * Describe periodic orders that would be placed today.
     * Iterates supplier schedules (DTOs), matches agreements (DTOs), builds preview.
     */
    public List<String> describePeriodicOrders() {
        List<String> result = new ArrayList<>();
        List<SupplierScheduleDTO> suppliers = supplierService.getSuppliersWithSchedules();

        for (SupplierScheduleDTO supplier : suppliers) {
            LocalDate deliveryDate = nextDeliveryDate(supplier.deliveryDays());
            if (deliveryDate == null) continue;

            List<SupplyAgreementDTO> agreements = supplierService.getAgreementsForSupplier(supplier.supplierId());

            for (SupplyAgreementDTO agreement : agreements) {
                ProductDTO product = controller.getProductBySpecId(agreement.productSpecId());
                if (product == null) continue;

                int requiredQty = product.minStockThreshold() - product.totalQuantity();
                if (requiredQty > 0) {
                    result.add(String.format(
                        "[%s] %s: %d units, delivery %s, price %.2f/unit",
                        supplier.name(), product.name(), requiredQty, deliveryDate, agreement.unitPrice()
                    ));
                }
            }
        }
        return result;
    }

    /**
     * Submit all periodic orders for today.
     * Builds OrderItemDTO lists per supplier, delegates to SupplierService.
     * Returns OrderSummaryDTOs.
     */
    public List<OrderSummaryDTO> submitAllPeriodicOrders() {
        List<OrderSummaryDTO> result = new ArrayList<>();
        List<SupplierScheduleDTO> suppliers = supplierService.getSuppliersWithSchedules();

        for (SupplierScheduleDTO supplier : suppliers) {
            LocalDate deliveryDate = nextDeliveryDate(supplier.deliveryDays());
            if (deliveryDate == null) continue;

            List<SupplyAgreementDTO> agreements = supplierService.getAgreementsForSupplier(supplier.supplierId());

            List<OrderItemDTO> items = new ArrayList<>();
            for (SupplyAgreementDTO agreement : agreements) {
                ProductDTO product = controller.getProductBySpecId(agreement.productSpecId());
                if (product == null) continue;

                int requiredQty = product.minStockThreshold() - product.totalQuantity();
                if (requiredQty > 0) {
                    items.add(new OrderItemDTO(agreement.productSpecId(), requiredQty, agreement.unitPrice()));
                    controller.updateShortageReport(product.specId(), requiredQty, agreement.unitPrice());
                }
            }

            if (!items.isEmpty()) {
                OrderSummaryDTO order = supplierService.createPeriodicOrder(supplier.supplierId(), items, deliveryDate);
                if (order != null) result.add(order);
            }
        }
        return result;
    }

    /**
     * Check and place periodic orders for a specific supplier.
     * Uses next delivery date from the supplier's schedule.
     * Uses SupplierService for all Suppliers interactions (returns DTOs).
     */
    public List<OrderSummaryDTO> checkAndPlacePeriodicOrders(int supplierId) {
        List<OrderSummaryDTO> result = new ArrayList<>();

        List<SupplyAgreementDTO> agreements = supplierService.getAgreementsForSupplier(supplierId);
        for (SupplyAgreementDTO agreement : agreements) {
            ProductDTO product = controller.getProductBySpecId(agreement.productSpecId());
            if (product == null) continue;

            int requiredQty = product.minStockThreshold() - product.totalQuantity();
            if (requiredQty > 0) {
                List<OrderItemDTO> items = new ArrayList<>();
                items.add(new OrderItemDTO(agreement.productSpecId(), requiredQty, agreement.unitPrice()));

                LocalDate deliveryDate = LocalDate.now().plusDays(7);
                OrderSummaryDTO order = supplierService.createPeriodicOrder(supplierId, items, deliveryDate);
                if (order != null) {
                    result.add(order);
                    controller.updateShortageReport(product.specId(), requiredQty, agreement.unitPrice());
                }
            }
        }
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────

    /**
     * Calculate the next delivery date from a list of day-of-week names.
     */
    private LocalDate nextDeliveryDate(List<String> dayNames) {
        List<DayOfWeek> days = new ArrayList<>();
        for (String name : dayNames) {
            try { days.add(DayOfWeek.valueOf(name)); } catch (IllegalArgumentException ignored) {}
        }
        if (days.isEmpty()) return null;

        LocalDate candidate = LocalDate.now();
        while (true) {
            for (DayOfWeek dow : days) {
                if (candidate.getDayOfWeek() == dow) return candidate;
            }
            candidate = candidate.plusDays(1);
            if (candidate.isAfter(LocalDate.now().plusDays(365))) return null;
        }
    }
}
