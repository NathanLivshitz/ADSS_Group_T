package Inventory.Domain.Repository;

import Inventory.Domain.Product;
import java.util.List;

public interface IProductRepository {

    /**
     * Returns the next available product ID (peek - does not consume).
     * Call add() to advance the counter.
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
     * Returns all products.
     * Required by InventoryController.generateInventoryReport().
     */
    List<Product> findAll();

    /**
     * Clears all products from the repository.
     * Called by InventoryController.reset().
     */
    void clear();

    /**
     * Persists updated costPrice and totalQuantity for a product spec to the database.
     * Called after updateShortageReport to ensure DB reflects the in-memory change.
     */
    void persistUpdate(int specId, double costPrice, int totalQuantity);
}
