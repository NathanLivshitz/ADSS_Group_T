package Shared.DTO;

/**
 * Data transfer object for supply agreements.
 * Carries agreement data between Suppliers and Inventory modules.
 */
public record SupplyAgreementDTO(
    int productSpecId,
    int supplierId,
    int minQuantity,
    double unitPrice
) {}
