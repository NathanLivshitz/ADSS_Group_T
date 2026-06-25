package Inventory.Data.DAO;

import Inventory.DTO.CategoryDTO;
import java.util.List;

public interface ICategoryDAO {
    void insert(CategoryDTO dto);
    List<CategoryDTO> findAll(); // ordered by categoryId ASC - parent always before child
}
