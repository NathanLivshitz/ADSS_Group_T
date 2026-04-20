package Inventory.Domain;

import java.time.LocalDate;

public class Promotion {
    private double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProductSpec targetProduct;
    private Category targetCategory;

    public Promotion(double discountPercent, LocalDate startDate, LocalDate endDate,
                     ProductSpec targetProduct, Category targetCategory) {
        if (discountPercent <= 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be greater than 0 and less than or equal to 100");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        boolean hasProduct = (targetProduct != null);
        boolean hasCategory = (targetCategory != null);

        if (!hasProduct && !hasCategory) {
            throw new IllegalArgumentException("Must specify either a product or a category as target");
        }
        if (hasProduct && hasCategory) {
            throw new IllegalArgumentException("Cannot specify both product and category - choose one");
        }

        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetProduct = targetProduct;
        this.targetCategory = targetCategory;
    }

    public double getDiscountPercent() { return discountPercent; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ProductSpec getTargetProduct() { return targetProduct; }
    public Category getTargetCategory() { return targetCategory; }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public double getEffectivePrice(ProductSpec p) {
        if (isActive()) {
            return p.getSellPrice() * (1 - discountPercent / 100);
        }
        return p.getSellPrice();
    }

    public boolean appliesTo(ProductSpec p) {
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
