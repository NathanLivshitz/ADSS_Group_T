package Shared.DTO;

import java.util.List;

public record SupplierScheduleDTO(int supplierId, String name, List<String> deliveryDays) {}
