package Inventory.Domain.Repository;

import Inventory.Domain.ProductSpec;
import Inventory.Domain.StockItem;
import java.util.List;

public interface IStockItemRepository {

    /**
     * Persists a new stock item.
     * Called by InventoryController.addStockItem().
     */
    void add(StockItem item);

    /**
     * Returns all StockItems whose spec reference matches the given spec.
     * Identity comparison (==), not equals() - one spec object per product.
     * Called by InventoryController.getStockForProduct() and removeStock().
     */
    List<StockItem> findBySpec(ProductSpec spec);

    /**
     * Returns all stock items across all products.
     * Called by InventoryController.removeExpiredStock().
     */
    List<StockItem> findAll();

    /**
     * Clears all stock items.
     * Called by InventoryController.reset().
     */
    void clear();
}
