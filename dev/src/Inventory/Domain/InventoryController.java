package Inventory.Domain;

import Inventory.DTO.*;
import Inventory.Domain.Repository.*;
import java.time.LocalDate;
import java.util.*;

public class InventoryController {

    private final IProductRepository productRepo;
    private final IProductSpecRepository productSpecRepo;
    private final IStockItemRepository stockItemRepo;
    private final ICategoryRepository categoryRepo;
    private final IPromotionRepository promotionRepo;
    private final IDefectiveReportRepository defectiveRepo;

    public InventoryController(
            IProductSpecRepository productSpecRepo,
            IProductRepository productRepo,
            IStockItemRepository stockItemRepo,
            ICategoryRepository categoryRepo,
            IPromotionRepository promotionRepo,
            IDefectiveReportRepository defectiveRepo) {
        this.productSpecRepo = productSpecRepo;
        this.productRepo   = productRepo;
        this.stockItemRepo = stockItemRepo;
        this.categoryRepo  = categoryRepo;
        this.promotionRepo = promotionRepo;
        this.defectiveRepo = defectiveRepo;
    }

    public void reset() {
        productRepo.clear();
        productSpecRepo.clear();
        stockItemRepo.clear();
        categoryRepo.clear();
        promotionRepo.clear();
        defectiveRepo.clear();
    }

    // ── CATALOG ──────────────────────────────────────────────

    public int addProduct(ProductDTO dto) {
        Category category = categoryRepo.findById(dto.categoryId());
        if (category == null)
            throw new IllegalArgumentException("Category not found: " + dto.categoryId());
        ProductSpec spec = new ProductSpec(dto.name(), dto.manufacturer(), category,
                dto.costPrice(), dto.sellPrice(), dto.minStockThreshold());
        return productSpecRepo.add(spec);
    }

    public ProductDTO getProduct(int specId) {
        ProductSpec spec = productSpecRepo.findById(specId);
        if (spec == null)
            throw new IllegalArgumentException("Product spec ID " + specId + " not found");
        return toProductDTO(spec);
    }

    public List<ProductDTO> getLowStockProducts() {
        List<ProductDTO> result = new ArrayList<>();
        for (ProductSpec spec : productSpecRepo.findAll()) {
            if (spec.getTotalQuantity() < spec.getMinStockThreshold())
                result.add(toProductDTO(spec));
        }
        return result;
    }

    // ── STOCK ────────────────────────────────────────────────

    public void addStockItem(StockItemDTO dto) {
        ProductSpec spec = productSpecRepo.findById(dto.specId());
        if (spec == null)
            throw new IllegalArgumentException("No product with specId " + dto.specId());
        Area area = Area.valueOf(dto.area().toUpperCase());
        LocalDate expiry = (dto.expiryDate() != null && !dto.expiryDate().isEmpty())
                ? LocalDate.parse(dto.expiryDate()) : null;
        List<Integer> productIds = new ArrayList<>();
        for (int i = 0; i < dto.quantity(); i++) {
            int productId = productRepo.nextId();
            productRepo.add(new Product(productId, spec));
            productIds.add(productId);
        }
        StockItem item = new StockItem(spec, area, dto.shelf(), dto.row(), dto.quantity(), expiry, productIds);
        stockItemRepo.add(item);
        spec.adjustQuantity(dto.quantity());
    }

    public List<StockItemDTO> getStockForProduct(int specId) {
        ProductSpec spec = productSpecRepo.findById(specId);
        if (spec == null)
            throw new IllegalArgumentException("Product spec ID " + specId + " not found");
        List<StockItemDTO> result = new ArrayList<>();
        for (StockItem si : stockItemRepo.findBySpec(spec)) result.add(toStockItemDTO(si));
        return result;
    }

    public void updateQuantity(int specId, String area, int shelf, int row, int delta) {
        ProductSpec spec = productSpecRepo.findById(specId);
        if (spec == null)
            throw new IllegalArgumentException("Product spec ID " + specId + " not found");
        Area areaEnum = Area.valueOf(area.toUpperCase());
        StockItem found = null;
        for (StockItem si : stockItemRepo.findBySpec(spec)) {
            if (si.getArea() == areaEnum && si.getShelfNumber() == shelf && si.getRowNumber() == row) {
                found = si; break;
            }
        }
        if (found == null)
            throw new IllegalArgumentException("Location not found for product spec " + specId);
        int newQty = found.getQuantity() + delta;
        if (newQty < 0)
            throw new IllegalArgumentException("Not enough stock. Available: " + found.getQuantity());
        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                int productId = productRepo.nextId();
                productRepo.add(new Product(productId, spec));
                found.addProductId(productId);
            }
        } else if (delta < 0) {
            found.removeProductIds(-delta);
        }
        spec.adjustQuantity(delta);
    }

    // ── CATEGORIES ───────────────────────────────────────────

    public int addCategory(String name, int parentCategoryId) {
        Category parent = null;
        if (parentCategoryId != 0) {
            parent = categoryRepo.findById(parentCategoryId);
            if (parent == null)
                throw new IllegalArgumentException("Parent category not found: " + parentCategoryId);
        }
        Category cat = new Category(name, parent);
        return categoryRepo.add(cat);
    }

    public List<CategoryDTO> getRootCategories() {
        List<CategoryDTO> result = new ArrayList<>();
        for (Category c : categoryRepo.findAllRoots()) result.add(toCategoryDTO(c));
        return result;
    }

    public CategoryDTO findCategoryByName(String name) {
        Category c = categoryRepo.findByName(name);
        return c != null ? toCategoryDTO(c) : null;
    }

    // ── PROMOTIONS ───────────────────────────────────────────

    public void addPromotion(PromotionDTO dto) {
        ProductSpec targetSpec = null;
        Category targetCat = null;
        if (dto.targetSpecId() != 0) {
            ProductSpec spec = productSpecRepo.findById(dto.targetSpecId());
            if (spec == null)
                throw new IllegalArgumentException("No product with specId " + dto.targetSpecId());
            targetSpec = spec;
        } else if (dto.targetCategoryId() != 0) {
            targetCat = categoryRepo.findById(dto.targetCategoryId());
            if (targetCat == null)
                throw new IllegalArgumentException("Category not found: " + dto.targetCategoryId());
        } else {
            throw new IllegalArgumentException("Promotion must target a product spec or a category");
        }
        Promotion promo = new Promotion(dto.discountPercent(),
                LocalDate.parse(dto.startDate()), LocalDate.parse(dto.endDate()),
                targetSpec, targetCat);
        promotionRepo.add(promo);
    }

    public List<PromotionDTO> getActivePromotions() {
        List<PromotionDTO> result = new ArrayList<>();
        for (Promotion p : promotionRepo.findAll()) {
            if (p.isActive()) result.add(toPromotionDTO(p));
        }
        return result;
    }

    public double getEffectivePrice(int productId) {
        ProductSpec spec = productSpecRepo.findById(productId);
        if (spec == null)
            throw new IllegalArgumentException("Product spec ID " + productId + " not found");
        double bestPrice = spec.getSellPrice();
        for (Promotion promo : promotionRepo.findAll()) {
            if (promo.isActive() && promo.appliesTo(spec)) {
                double promoPrice = promo.getEffectivePrice(spec);
                if (promoPrice < bestPrice) bestPrice = promoPrice;
            }
        }
        return bestPrice;
    }

    // ── DEFECTIVES ───────────────────────────────────────────

    public void reportDefective(int productId, int quantity, String reason) {
        if (productSpecRepo.findById(productId) == null)
            throw new IllegalArgumentException("Product spec ID " + productId + " not found");
        defectiveRepo.add(new DefectiveReport(productId, quantity, reason, LocalDate.now()));
        removeStockInternal(productId, quantity);
    }

    public Map<Integer, List<StockItemDTO>> getDefectiveItemsWithLocations() {
        Map<Integer, List<StockItemDTO>> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (StockItem si : stockItemRepo.findAll()) {
            if (si.getExpiryDate() != null && si.getExpiryDate().isBefore(today) && si.getQuantity() > 0) {
                int pid = findProductIdBySpec(si.getSpec());
                if (pid != -1)
                    result.computeIfAbsent(pid, k -> new ArrayList<>()).add(toStockItemDTO(si));
            }
        }
        for (DefectiveReport r : defectiveRepo.findAll()) {
            int pid = r.getProductId();
            if (!result.containsKey(pid) && productSpecRepo.findById(pid) != null)
                result.put(pid, getStockForProduct(pid));
        }
        return result;
    }

    public List<DefectiveReportDTO> getDefectiveReports(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Date range cannot be null");
        if (from.isAfter(to))
            throw new IllegalArgumentException("From date must be before or equal to to date");
        List<DefectiveReportDTO> result = new ArrayList<>();
        for (DefectiveReport r : defectiveRepo.findByDateRange(from, to))
            result.add(toDefectiveReportDTO(r));
        return result;
    }

    // ── REPORTS ──────────────────────────────────────────────

    public List<ProductDTO> generateInventoryReport(List<Integer> categoryIds) {
        List<Product> items;
        if (categoryIds == null || categoryIds.isEmpty()) {
            items = new ArrayList<>();
            for (ProductSpec spec : productSpecRepo.findAll()) {
                items.add(new Product(spec.getSpecId(), spec));
            }
        } else {
            Set<ProductSpec> specSet = new HashSet<>();
            for (int catId : categoryIds) {
                Category cat = categoryRepo.findById(catId);
                if (cat == null)
                    throw new IllegalArgumentException("Category not found: " + catId);
                specSet.addAll(cat.getAllProducts());
            }
            items = new ArrayList<>();
            for (ProductSpec spec : productSpecRepo.findAll()) {
                if (specSet.contains(spec)) items.add(new Product(spec.getSpecId(), spec));
            }
        }
        List<ProductDTO> result = new ArrayList<>();
        for (Product p : items) result.add(toProductDTO(p));
        return result;
    }

    // ── STOCK MANAGEMENT ─────────────────────────────────────

    public int removeExpiredStock() {
        int totalRemoved = 0;
        LocalDate today = LocalDate.now();
        List<StockItem> toRemove = new ArrayList<>();
        for (StockItem si : stockItemRepo.findAll()) {
            if (si.getExpiryDate() != null && si.getExpiryDate().isBefore(today) && si.getQuantity() > 0) {
                int qty = si.getQuantity();
                int productId = findProductIdBySpec(si.getSpec());
                if (productId != -1) {
                    defectiveRepo.add(new DefectiveReport(productId, qty, "EXPIRED", today));
                    si.getSpec().adjustQuantity(-qty);
                    totalRemoved += qty;
                }
                toRemove.add(si);
            }
        }
        for (StockItem si : toRemove) si.setQuantity(0);
        return totalRemoved;
    }

    public void updateShortageReport(int specId, int orderedQty, double unitPrice) {
        ProductSpec spec = productSpecRepo.findById(specId);
        if (spec != null) {
                spec.setCostPrice(unitPrice);
                for (int i = 0; i < orderedQty; i++) {
                    int productId = productRepo.nextId();
                    productRepo.add(new Product(productId, spec));
                }
                spec.adjustQuantity(orderedQty);
                return;
        }
        throw new IllegalArgumentException("No product found with specId " + specId);
    }

    // ── CROSS-MODULE SUPPORT ─────────────────────────────────

    /**
     * Looks up a product by its specId. Used by InventoryService.selectProduct()
     * to resolve a specId (from the low-stock list) into a full ProductDTO
     * so the service can compute requiredQty and forward to SupplierService.
     * Returns null if no product with that specId exists.
     */
    public ProductDTO getProductBySpecId(int specId) {
        ProductSpec spec = productSpecRepo.findById(specId);
        return spec != null ? toProductDTO(spec) : null;
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private Product findProductBySpecId(int specId) {
        ProductSpec spec = productSpecRepo.findById(specId);
        return spec != null ? new Product(specId, spec) : null;
    }

    private int findProductIdBySpec(ProductSpec spec) {
        return spec != null ? spec.getSpecId() : -1;
    }

    private void removeStockInternal(int productId, int quantity) {
        ProductSpec spec = productSpecRepo.findById(productId);
        if (spec == null) return;
        List<StockItem> store = new ArrayList<>(), warehouse = new ArrayList<>();
        for (StockItem si : stockItemRepo.findBySpec(spec)) {
            if (si.getArea() == Area.STORE) store.add(si);
            else warehouse.add(si);
        }
        List<StockItem> sorted = new ArrayList<>(store);
        sorted.addAll(warehouse);
        int remaining = quantity;
        for (StockItem si : sorted) {
            if (remaining <= 0) break;
            int take = Math.min(si.getQuantity(), remaining);
            si.removeProductIds(take);
            si.getSpec().adjustQuantity(-take);
            remaining -= take;
        }
        if (remaining > 0)
            throw new IllegalArgumentException("Not enough total stock to remove " + quantity + " units");
    }

    private ProductDTO toProductDTO(Product p) {
        ProductSpec s = p.getSpec();
        return toProductDTO(s);
    }

    private ProductDTO toProductDTO(ProductSpec s) {
        int catId = s.getCategory() != null ? s.getCategory().getCategoryId() : 0;
        return new ProductDTO(0, s.getSpecId(), s.getName(), s.getManufacturer(),
                catId, s.getCostPrice(), s.getSellPrice(), s.getMinStockThreshold(), s.getTotalQuantity());
    }

    private StockItemDTO toStockItemDTO(StockItem si) {
        String expiry = si.getExpiryDate() != null ? si.getExpiryDate().toString() : null;
        return new StockItemDTO(si.getSpec().getSpecId(), si.getArea().name(),
                si.getShelfNumber(), si.getRowNumber(), si.getQuantity(), expiry);
    }

    private CategoryDTO toCategoryDTO(Category c) {
        int parentId = c.getParent() != null ? c.getParent().getCategoryId() : 0;
        return new CategoryDTO(c.getCategoryId(), c.getName(), parentId);
    }

    private PromotionDTO toPromotionDTO(Promotion p) {
        int specId   = p.getTargetProduct() != null ? p.getTargetProduct().getSpecId() : 0;
        int catId    = p.getTargetCategory() != null ? p.getTargetCategory().getCategoryId() : 0;
        String pName = p.getTargetProduct() != null ? p.getTargetProduct().getName() : null;
        String cName = p.getTargetCategory() != null ? p.getTargetCategory().getName() : null;
        return new PromotionDTO(p.getDiscountPercent(),
                p.getStartDate().toString(), p.getEndDate().toString(),
                specId, catId, pName, cName);
    }

    private DefectiveReportDTO toDefectiveReportDTO(DefectiveReport r) {
        return new DefectiveReportDTO(r.getProductId(), r.getQuantity(),
                r.getReason(), r.getReportDate().toString());
    }
}
