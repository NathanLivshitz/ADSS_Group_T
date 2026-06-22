package Inventory.DTO;

public record CategoryDTO(
    int categoryId,
    String categoryName,
    int parentCategoryId  // 0 for root categories (no parent)
) {}
