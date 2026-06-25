package Inventory.Domain.Repository;

import Inventory.Domain.ProductSpec;
import Inventory.Domain.StockItem;
import java.util.List;

public interface IStockItemRepository {

    // Persists a new stock item.
    void add(StockItem item);

    // Persists the current quantity of an existing stock item.
    void updateQuantity(StockItem item);

    // Returns all StockItems whose spec reference matches the given spec.
    // Uses identity comparison (==), not equals().
    List<StockItem> findBySpec(ProductSpec spec);

    // Returns all stock items across all products.
    List<StockItem> findAll();

    // Clears all stock items.
    void clear();
}
