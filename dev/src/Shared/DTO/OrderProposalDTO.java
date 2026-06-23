package Shared.DTO;

public record OrderProposalDTO(int proposalId, int supplierId, int productSpecId, int requiredQty, double unitPrice) {}
