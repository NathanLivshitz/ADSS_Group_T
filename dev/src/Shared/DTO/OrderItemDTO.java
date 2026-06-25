package Shared.DTO;

/**
 * Data transfer object for order line items.
 * Carries order item data between Suppliers and Inventory modules.
 */
public record OrderItemDTO(
    int productSpecId,
    int quantity,
    double unitPrice
) {}
