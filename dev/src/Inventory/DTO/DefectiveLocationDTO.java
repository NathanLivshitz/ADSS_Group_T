package Inventory.DTO;

import java.util.List;

public record DefectiveLocationDTO(int specId, List<StockItemDTO> locations) {}
