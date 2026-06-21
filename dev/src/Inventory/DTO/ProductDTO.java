package Inventory.DTO;

public record ProductDTO(
    int productId,
    int specId,
    String name,
    String manufacturer,
    int categoryId,
    double costPrice,
    double sellPrice,
    int minStockThreshold,
    int totalQuantity
) {}
