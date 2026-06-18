package Inventory.Domain;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private Category parent;
    private List<Category> subCategories;
    private List<ProductSpec> products;

    public Category(String name) {
        this(name, null);
    }

    public Category(String name, Category parent) {
        // Validate category name - cannot be null or empty
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name;
        this.parent = parent;
        this.subCategories = new ArrayList<>();
        this.products = new ArrayList<>();
        
        // Automatically add to parent's subcategories if parent exists
        if (parent != null) {
            parent.subCategories.add(this);
        }
    }

    public String getName() { return name; }
    public Category getParent() { return parent; }
    public List<Category> getSubCategories() { return subCategories; }
    public List<ProductSpec> getProducts() { return products; }

    public void addProduct(ProductSpec product) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot add null product to category");
        }
        products.add(product);
    }

    public List<ProductSpec> getAllProducts() {
        List<ProductSpec> all = new ArrayList<>(products);
        for (Category sub : subCategories) {
            all.addAll(sub.getAllProducts());
        }
        return all;
    }
}

