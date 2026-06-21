package Inventory.Domain.Repository;

import Inventory.Data.DAO.IProductDAO;
import Inventory.Data.DTO.ProductDTO;
import Inventory.Domain.Category;
import Inventory.Domain.Product;
import Inventory.Domain.ProductSpec;
import java.util.*;

public class ProductRepository implements IProductRepository {

    private final Map<Integer, Product> catalog = new HashMap<>();
    private int nextProductId = 1;
    private final IProductDAO dao;

    public ProductRepository(IProductDAO dao) {
        this.dao = dao;
    }

    @Override
    public int nextId() {
        return nextProductId++;
    }

    @Override
    public void add(Product product) {
        catalog.put(product.getId(), product);
        dao.insert(toDTO(product));
    }

    @Override
    public Product findById(int id) {
        return catalog.get(id);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(catalog.values());
    }

    @Override
    public void clear() {
        catalog.clear();
        nextProductId = 1;
    }

    private ProductDTO toDTO(Product p) {
        ProductSpec s = p.getSpec();
        int catId = s.getCategory() != null ? s.getCategory().getCategoryId() : 0;
        return new ProductDTO(p.getId(), s.getSpecId(), s.getName(), s.getManufacturer(),
                catId, s.getCostPrice(), s.getSellPrice(), s.getMinStockThreshold(), s.getTotalQuantity());
    }
}
