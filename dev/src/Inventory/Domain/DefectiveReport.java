package Inventory.Domain;

import java.time.LocalDate;

public class DefectiveReport {
    private int productId;
    private int quantity;
    private String reason;
    private LocalDate reportDate;

    public DefectiveReport(int productId, int quantity, String reason, LocalDate reportDate) {
        // Validate product ID - must be positive
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than zero");
        }
        
        // Validate quantity - must be positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Defective quantity must be greater than zero");
        }
        
        // Validate reason - cannot be null or empty
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
        
        // Validate reason value - must be DEFECTIVE or EXPIRED
        String validReason = reason.toUpperCase().trim();
        if (!validReason.equals("DEFECTIVE") && !validReason.equals("EXPIRED")) {
            throw new IllegalArgumentException("Reason must be 'DEFECTIVE' or 'EXPIRED'");
        }
        
        // Validate report date - cannot be null
        if (reportDate == null) {
            throw new IllegalArgumentException("Report date cannot be null");
        }
        
        this.productId = productId;
        this.quantity = quantity;
        this.reason = validReason;
        this.reportDate = reportDate;
    }

    public int getProductId() { return productId; }
    
    public int getQuantity() { return quantity; }
    
    public String getReason() { return reason; }
    
    public LocalDate getReportDate() { return reportDate; }
}

