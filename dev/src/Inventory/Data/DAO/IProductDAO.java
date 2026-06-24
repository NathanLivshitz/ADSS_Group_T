package Inventory.Data.DAO;

import Inventory.DTO.ProductDTO;
import java.util.List;

/**
 * DAO for the product_specs table — one row per spec.
 * ProductDTO is still used as the transfer object but productId is ignored
 * (product instances live in the products table, managed by IProductInstanceDAO).
 */
public interface IProductDAO {
    void insert(ProductDTO dto);
    ProductDTO findById(int specId);
    List<ProductDTO> findAll();
    void update(ProductDTO dto);
    void updateSpec(int specId, double costPrice, int totalQuantity);
}
