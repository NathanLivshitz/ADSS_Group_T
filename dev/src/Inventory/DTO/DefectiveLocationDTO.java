package Inventory.DTO;

import java.util.List;

public record DefectiveLocationDTO(int productId, List<StockItemDTO> locations) {}
