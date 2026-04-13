package Inventory.Domain;

public class Product {
    private final int id;
    private final ProductSpec spec;

    public Product(int id, ProductSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Spec must not be null");
        }
        this.id = id;
        this.spec = spec;
    }

    public int getId() { return id; }
    public ProductSpec getSpec() { return spec; }
}
