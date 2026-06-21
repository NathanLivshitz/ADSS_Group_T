package Inventory.Data.DTO;

public record PromotionDTO(
    double discountPercent,
    String startDate,           // ISO-8601
    String endDate,             // ISO-8601
    int targetSpecId,           // 0 if category-targeted
    int targetCategoryId,       // 0 if product-targeted
    String targetProductName,   // null if category-targeted
    String targetCategoryName   // null if product-targeted
) {}
