package Inventory.Domain.Repository;

import Inventory.Domain.Category;
import java.util.List;

public interface ICategoryRepository {

    // Persists a category (root or sub), assigns its categoryId, returns the assigned ID.
    int add(Category category);

    // Returns all root categories (parent == null).
    List<Category> findAllRoots();

    // Finds a category by its categoryId. Returns null if not found.
    Category findById(int categoryId);

    // Finds a category by name (case-insensitive). Returns first match or null.
    Category findByName(String name);

    // Clears all categories.
    void clear();
}
