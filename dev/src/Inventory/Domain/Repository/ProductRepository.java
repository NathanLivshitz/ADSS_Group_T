package Inventory.Domain.Repository;

import Inventory.Data.DAO.IProductDAO;
import Inventory.Data.DAO.IProductInstanceDAO;
import Inventory.Domain.Product;
import Inventory.Domain.ProductSpec;
import java.util.*;

public class ProductRepository implements IProductRepository {

    private final Map<Integer, Product> catalog = new HashMap<>();
    private int nextProductId = 1;
    private final IProductInstanceDAO instanceDao;
    private final IProductDAO specDao;

    public ProductRepository(IProductInstanceDAO instanceDao, IProductDAO specDao) {
        this.instanceDao = instanceDao;
        this.specDao = specDao;
    }

    @Override
    public int nextId() {
        return nextProductId;
    }

    @Override
    public void add(Product product) {
        catalog.put(product.getId(), product);
        instanceDao.insert(product.getId(), product.getSpec().getSpecId());
        if (product.getId() >= nextProductId) nextProductId = product.getId() + 1;
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

    @Override
    public void persistUpdate(int specId, double costPrice, int totalQuantity) {
        specDao.updateSpec(specId, costPrice, totalQuantity);
    }

    // specRepo must be hydrated first
    public void hydrate(IProductSpecRepository specRepo) {
        for (ProductSpec spec : specRepo.findAll()) {
            List<Integer> productIds = instanceDao.findAllBySpecId(spec.getSpecId());
            for (Integer productId : productIds) {
                Product product = new Product(productId, spec);
                catalog.put(productId, product);
                if (productId >= nextProductId) {
                    nextProductId = productId + 1;
                }
            }
        }
    }
}
