package Inventory.Domain;

import java.time.LocalDate;
import java.util.List;

public class InventoryReport {
    private LocalDate reportDate;
    private List<Category> categoriesFilter;
    private List<Product> items;

    public InventoryReport(LocalDate reportDate, List<Category> categoriesFilter,
                           List<Product> items) {
        this.reportDate = reportDate;
        this.categoriesFilter = categoriesFilter;
        this.items = items;
    }

    public LocalDate getReportDate() { return reportDate; }
    public List<Category> getCategoriesFilter() { return categoriesFilter; }
    public List<Product> getItems() { return items; }
}
