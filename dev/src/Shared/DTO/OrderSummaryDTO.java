package Shared.DTO;

public record OrderSummaryDTO(int orderId, int supplierId, int itemCount,
                              double totalPrice, String orderType, String expectedDeliveryDate) {}
