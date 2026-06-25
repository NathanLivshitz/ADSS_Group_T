package Inventory.Data.DAO;

import java.util.List;

/**
 * DAO for the stock_item_products mapping table - links product instances to stock locations.
 */
public interface IStockItemProductsDAO {
    void insert(int specId, String area, int shelf, int row, int productId);
    List<Integer> findByLocation(int specId, String area, int shelf, int row);
    void deleteByLocation(int specId, String area, int shelf, int row);
}
