package Inventory.Domain.Repository;

import Inventory.Domain.ProductSpec;
import java.util.List;

public interface IProductSpecRepository {
    int add(ProductSpec spec);
    ProductSpec findById(int specId);
    List<ProductSpec> findAll();
    void clear();
}
