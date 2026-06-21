package Inventory.DTO;

public record DefectiveReportDTO(
    int productId,
    int quantity,
    String reason,     // "DEFECTIVE" or "EXPIRED"
    String reportDate  // ISO-8601
) {}
