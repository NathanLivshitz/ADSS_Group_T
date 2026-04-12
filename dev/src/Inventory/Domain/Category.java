package Inventory.Domain;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private Category parent;
    private List<Category> subCategories;
    private List<Product> products;

    public Category(String name) {
        this(name, null);
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
        this.subCategories = new ArrayList<>();
        this.products = new ArrayList<>();
        if (parent != null) {
            parent.subCategories.add(this);
        }
    }

    public String getName() { return name; }
    public Category getParent() { return parent; }
    public List<Category> getSubCategories() { return subCategories; }
    public List<Product> getProducts() { return products; }

    public void addProduct(Product product) {
        products.add(product);
    }

    public List<Product> getAllProducts() {
        List<Product> all = new ArrayList<>(products);
        for (Category sub : subCategories) {
            all.addAll(sub.getAllProducts());
        }
        return all;
    }
}
