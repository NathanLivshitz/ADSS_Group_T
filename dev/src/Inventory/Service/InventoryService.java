package Inventory.Service;

import Inventory.Data.DTO.*;
import Inventory.Domain.InventoryController;
import Suppliers.Domain.OrderProposal;
import Suppliers.Domain.Order;
import Suppliers.Domain.OrderItem;
import Suppliers.Service.SupplierService;
import java.time.LocalDate;
import java.util.*;

public class InventoryService {
    private final InventoryController controller;
    private SupplierService supplierService;

    public InventoryService(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
    }

    public InventoryService(InventoryController controller, SupplierService supplierService) {
        this(controller);
        this.supplierService = supplierService;
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

    public Map<Integer, List<StockItemDTO>> getDefectiveItemsWithLocations() {
        return controller.getDefectiveItemsWithLocations();
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
    public OrderProposal selectProduct(int specId) {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        ProductDTO product = controller.getProductBySpecId(specId);
        if (product == null)
            throw new IllegalArgumentException("No product with specId " + specId);
        int requiredQty = product.minStockThreshold() - product.totalQuantity();
        if (requiredQty <= 0)
            throw new IllegalArgumentException("Product specId=" + specId + " is not below threshold");
        return supplierService.createOrderProposal(specId, requiredQty);
    }

    /**
     * INV-9 / Contract 3: User confirmed the order proposal.
     * Creates and transmits the order via SupplierService, then updates
     * cost price and total quantity in the inventory (updateShortageReport).
     */
    public Order confirmOrder(int orderProposalId) {
        if (supplierService == null)
            throw new IllegalStateException("SupplierService not wired");
        Order order = supplierService.createOrder(orderProposalId);
        for (OrderItem item : order.getItems()) {
            controller.updateShortageReport(
                    item.getProductSpecId(), item.getQuantity(), item.getUnitPrice());
        }
        return order;
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
