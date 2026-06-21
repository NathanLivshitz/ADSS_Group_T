package Inventory.Domain.Repository;

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
}
