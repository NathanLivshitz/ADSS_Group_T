package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IProductDAO;
import Inventory.DTO.ProductDTO;
import java.util.*;

public class StubProductDAO implements IProductDAO {
    @Override public void insert(ProductDTO dto) {}
    @Override public ProductDTO findById(int specId) { return null; }
    @Override public List<ProductDTO> findAll() { return Collections.emptyList(); }
    @Override public void update(ProductDTO dto) {}
    @Override public void updateSpec(int specId, double costPrice, int totalQuantity) {}
}
