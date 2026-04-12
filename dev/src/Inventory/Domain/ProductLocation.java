package Inventory.Domain;

public class ProductLocation {
    private Area area;
    private int shelfNumber;
    private int rowNumber;
    private int quantity;

    public ProductLocation(Area area, int shelfNumber, int rowNumber, int quantity) {
        this.area = area;
        this.shelfNumber = shelfNumber;
        this.rowNumber = rowNumber;
        this.quantity = quantity;
    }

    public Area getArea() { return area; }
    public int getShelfNumber() { return shelfNumber; }
    public int getRowNumber() { return rowNumber; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
