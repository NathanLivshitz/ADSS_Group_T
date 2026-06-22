package Inventory.Domain.Repository;

import Inventory.Data.DAO.ICategoryDAO;
import Inventory.Data.DAO.IProductDAO;
import Inventory.DTO.ProductDTO;
import Inventory.Domain.Category;
import Inventory.Domain.ProductSpec;
import java.util.*;

public class ProductSpecRepository implements IProductSpecRepository {
    private final Map<Integer, ProductSpec> specs = new LinkedHashMap<>();
    private int nextSpecId = 1;

    @Override
    public int add(ProductSpec spec) {
        int id = nextSpecId++;
        spec.setSpecId(id);
        specs.put(id, spec);
        return id;
    }

    @Override
    public ProductSpec findById(int specId) {
        return specs.get(specId);
    }

    @Override
    public List<ProductSpec> findAll() {
        return new ArrayList<>(specs.values());
    }

    @Override
    public void clear() {
        specs.clear();
        nextSpecId = 1;
    }

    // categoryDao unused here; kept for wiring symmetry with Main.
    // skips specIds already loaded (dedup guard).
    public void hydrate(IProductDAO productDao, ICategoryDAO categoryDao,
                        Map<Integer, Category> categoryMap) {
        List<ProductDTO> dtos = productDao.findAll();
        int maxSpecId = 0;
        for (ProductDTO dto : dtos) {
            if (specs.containsKey(dto.specId())) continue;
            Category cat = categoryMap.get(dto.categoryId());
            ProductSpec spec = new ProductSpec(
                dto.name(), dto.manufacturer(), cat,
                dto.costPrice(), dto.sellPrice(), dto.minStockThreshold()
            );
            spec.setSpecId(dto.specId());
            spec.adjustQuantity(dto.totalQuantity());
            specs.put(dto.specId(), spec);
            if (dto.specId() > maxSpecId) maxSpecId = dto.specId();
        }
        if (maxSpecId > 0) nextSpecId = maxSpecId + 1;
    }
}
