package Inventory.Data.DAO;

import Inventory.DTO.ProductDTO;
import java.util.List;

public interface IProductDAO {
    void insert(ProductDTO dto);
    ProductDTO findById(int productId);
    List<ProductDTO> findAll();
    void update(ProductDTO dto);
}
