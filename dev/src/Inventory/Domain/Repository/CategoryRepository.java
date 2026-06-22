package Inventory.Domain.Repository;

import Inventory.Data.DAO.ICategoryDAO;
import Inventory.DTO.CategoryDTO;
import Inventory.Domain.Category;
import java.util.*;

public class CategoryRepository implements ICategoryRepository {

    private final Map<Integer, Category> categories = new LinkedHashMap<>();
    private int nextCategoryId = 1;
    private final ICategoryDAO dao;

    public CategoryRepository(ICategoryDAO dao) {
        this.dao = dao;
    }

    // Rebuilds in-memory state from the database.
    // findAll() orders by category_id ASC so parents always arrive before children.
    public void hydrate() {
        List<CategoryDTO> dtos = dao.findAll();
        int maxId = 0;
        for (CategoryDTO dto : dtos) {
            Category parent = (dto.parentCategoryId() > 0)
                ? categories.get(dto.parentCategoryId())
                : null;
            Category cat = new Category(dto.categoryName(), parent);
            cat.setCategoryId(dto.categoryId());
            categories.put(dto.categoryId(), cat);
            if (dto.categoryId() > maxId) maxId = dto.categoryId();
        }
        if (maxId > 0) nextCategoryId = maxId + 1;
    }

    // Unmodifiable view of the category map; used during hydration wiring.
    public Map<Integer, Category> getAllCategoriesMap() {
        return Collections.unmodifiableMap(categories);
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
