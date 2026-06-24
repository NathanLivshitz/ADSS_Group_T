package Inventory.Data.DAO;

import java.util.List;

/**
 * DAO for the products table - physical product instances linked to a spec.
 */
public interface IProductInstanceDAO {
    void insert(int productId, int specId);
    List<Integer> findAllBySpecId(int specId);
    List<Integer> findAll();
}
