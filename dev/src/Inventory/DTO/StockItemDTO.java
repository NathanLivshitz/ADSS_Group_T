package Inventory.DTO;

public record StockItemDTO(
    int specId,
    String area,       // "STORE" or "WAREHOUSE"
    int shelf,
    int row,
    int quantity,
    String expiryDate  // ISO-8601 "YYYY-MM-DD", null if no expiry
) {}
