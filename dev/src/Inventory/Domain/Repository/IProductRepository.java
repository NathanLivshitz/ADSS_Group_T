package Inventory.Domain.Repository;

import Inventory.Domain.Product;
import java.util.List;

public interface IProductRepository {

    /**
     * Returns the next available product ID without consuming it.
     * Called by InventoryController before constructing a Product (id is final).
     */
    int nextId();

    /**
     * Persists a new product. The product must already carry the ID returned by nextId().
     * Called by InventoryController.addProduct().
     */
    void add(Product product);

    /**
     * Looks up a product by its ID.
     * Returns null if not found - caller is responsible for null check.
     */
    Product findById(int id);

    /**
     * GAP-4: Returns all products.
     * Required by InventoryController.generateInventoryReport().
     */
    List<Product> findAll();

    /**
     * Clears all products from the repository.
     * Called by InventoryController.reset().
     */
    void clear();
}
