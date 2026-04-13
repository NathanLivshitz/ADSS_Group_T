package Inventory.Domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryReport {
    private LocalDate reportDate;
    private List<Category> categoriesFilter;
    private List<Product> items;

    public InventoryReport(LocalDate reportDate, List<Category> categoriesFilter, List<Product> items) {
        if (reportDate == null) {
            throw new IllegalArgumentException("Report date cannot be null");
        }
        
        this.reportDate = reportDate;
        this.categoriesFilter = categoriesFilter != null ? new ArrayList<>(categoriesFilter) : new ArrayList<>();
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public LocalDate getReportDate() { 
        return reportDate; 
    }
    
    public List<Category> getCategoriesFilter() { 
        return categoriesFilter; 
    }
    
    public List<Product> getItems() { 
        return items; 
    }
}
