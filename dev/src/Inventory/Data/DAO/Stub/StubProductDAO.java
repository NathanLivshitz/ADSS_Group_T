package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IProductDAO;
import Inventory.Data.DTO.ProductDTO;
import java.util.*;

public class StubProductDAO implements IProductDAO {
    @Override public void insert(ProductDTO dto) {}
    @Override public ProductDTO findById(int productId) { return null; }
    @Override public List<ProductDTO> findAll() { return Collections.emptyList(); }
    @Override public void update(ProductDTO dto) {}
}
