package Inventory.Service;

import Inventory.Data.DTO.*;
import Inventory.Domain.InventoryController;
import java.time.LocalDate;
import java.util.*;

public class InventoryService {
    private final InventoryController controller;

    public InventoryService(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("controller cannot be null");
        this.controller = controller;
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

    // ── Reset ──────────────────────────────────────────────

    public void reset() {
        controller.reset();
    }

    // ── Cross-module (called by SupplierService) ───────────

    public void updateShortageReport(int specId, int orderedQty, double unitPrice) {
        controller.updateShortageReport(specId, orderedQty, unitPrice);
    }
}
