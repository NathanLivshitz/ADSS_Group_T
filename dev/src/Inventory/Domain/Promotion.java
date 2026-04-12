package Inventory.Domain;

import java.time.LocalDate;

public class Promotion {
    private double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Product targetProduct;
    private Category targetCategory;

    public Promotion(double discountPercent, LocalDate startDate, LocalDate endDate,
                     Product targetProduct, Category targetCategory) {
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetProduct = targetProduct;
        this.targetCategory = targetCategory;
    }

    public double getDiscountPercent() { return discountPercent; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Product getTargetProduct() { return targetProduct; }
    public Category getTargetCategory() { return targetCategory; }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public double getEffectivePrice(Product p) {
        if (isActive()) {
            return p.getSellPrice() * (1 - discountPercent / 100);
        }
        return p.getSellPrice();
    }

    public boolean appliesTo(Product p) {
        if (targetProduct != null && targetProduct.equals(p)) {
            return true;
        }
        if (targetCategory != null) {
            return isInCategory(p.getCategory(), targetCategory);
        }
        return false;
    }

    private boolean isInCategory(Category productCat, Category target) {
        Category current = productCat;
        while (current != null) {
            if (current.equals(target)) return true;
            current = current.getParent();
        }
        return false;
    }
}
