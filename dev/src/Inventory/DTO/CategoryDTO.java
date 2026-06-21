package Inventory.DTO;

public record CategoryDTO(
    int categoryId,
    String categoryName,
    int parentCategoryId  // -1 for root categories
) {}
