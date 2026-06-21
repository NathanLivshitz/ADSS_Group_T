package Inventory.Domain.Repository;

import Inventory.Data.DAO.ICategoryDAO;
import Inventory.Data.DTO.CategoryDTO;
import Inventory.Domain.Category;
import java.util.*;

public class CategoryRepository implements ICategoryRepository {

    private final Map<Integer, Category> categories = new LinkedHashMap<>();
    private int nextCategoryId = 1;
    private final ICategoryDAO dao;

    public CategoryRepository(ICategoryDAO dao) {
        this.dao = dao;
    }

    @Override
    public int add(Category category) {
        int id = nextCategoryId++;
        category.setCategoryId(id);
        categories.put(id, category);
        dao.insert(toDTO(category));
        return id;
    }

    @Override
    public List<Category> findAllRoots() {
        List<Category> roots = new ArrayList<>();
        for (Category c : categories.values()) {
            if (c.getParent() == null) roots.add(c);
        }
        return roots;
    }

    @Override
    public Category findById(int categoryId) {
        return categories.get(categoryId);
    }

    @Override
    public Category findByName(String name) {
        for (Category c : categories.values()) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    @Override
    public void clear() {
        categories.clear();
        nextCategoryId = 1;
    }

    private CategoryDTO toDTO(Category c) {
        int parentId = c.getParent() != null ? c.getParent().getCategoryId() : 0;
        return new CategoryDTO(c.getCategoryId(), c.getName(), parentId);
    }
}
