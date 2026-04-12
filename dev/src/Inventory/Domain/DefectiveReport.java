package Inventory.Domain;

import java.time.LocalDate;

public class DefectiveReport {
    private int productId;
    private int quantity;
    private String reason;
    private LocalDate reportDate;

    public DefectiveReport(int productId, int quantity, String reason, LocalDate reportDate) {
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
        this.reportDate = reportDate;
    }

    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public String getReason() { return reason; }
    public LocalDate getReportDate() { return reportDate; }
}
