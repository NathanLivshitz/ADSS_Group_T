package Inventory.Domain.Repository;

import Inventory.Domain.Product;
import java.util.List;

public interface IProductRepository {

    // Returns the next available product ID (peek - does not consume).
    int nextId();

    // Persists a new product. The product must already carry the ID returned by nextId().
    void add(Product product);

    // Looks up a product by its ID. Returns null if not found.
    Product findById(int id);

    // Returns all products.
    List<Product> findAll();

    // Clears all products from the repository.
    void clear();

    // Persists updated costPrice and totalQuantity for a product spec to the database.
    void persistUpdate(int specId, double costPrice, int totalQuantity);
}
