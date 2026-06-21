package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.ICategoryDAO;
import Inventory.DTO.CategoryDTO;
import java.util.*;

public class StubCategoryDAO implements ICategoryDAO {
    @Override public void insert(CategoryDTO dto) {}
    @Override public List<CategoryDTO> findAll() { return Collections.emptyList(); }
}
