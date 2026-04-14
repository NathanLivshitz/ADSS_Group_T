package Inventory.Data;

import Inventory.Domain.*;
import java.time.LocalDate;

public class PreloadData {
    private final InventoryController controller;

    public PreloadData(InventoryController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller must not be null");
        }
        this.controller = controller;
    }

    public void load() {
        controller.reset();

        // ── CATEGORIES ──────────────────────────────────────
        // Dairy Products > Milk > By Size
        Category dairy = new Category("Dairy Products");
        controller.addCategory(dairy);
        Category milk = new Category("Milk", dairy);
        Category milkBySize = new Category("By Size", milk);

        // Toiletries > Shampoo > By Size
        Category toiletries = new Category("Toiletries");
        controller.addCategory(toiletries);
        Category shampoo = new Category("Shampoo", toiletries);
        Category shampooBySize = new Category("By Size", shampoo);

        // Snacks > Chips > By Brand
        Category snacks = new Category("Snacks");
        controller.addCategory(snacks);
        Category chips = new Category("Chips", snacks);
        Category chipsByBrand = new Category("By Brand", chips);

        // ── PRODUCTS ────────────────────────────────────────
        // 1. Tnuva 3% Milk 1L
        ProductSpec milkSpec1L = new ProductSpec("Tnuva 3% Milk 1L", "Tnuva",
                milkBySize, 4.5, 6.9, 15);
        Product milk1L = new Product(1, milkSpec1L);
        controller.addProduct(milk1L);

        // 2. Tnuva 3% Milk 500ml
        ProductSpec milkSpec500 = new ProductSpec("Tnuva 3% Milk 500ml", "Tnuva",
                milkBySize, 3.0, 4.9, 10);
        Product milk500 = new Product(2, milkSpec500);
        controller.addProduct(milk500);

        // 3. Pinuk Shampoo 250ml
        ProductSpec shampooSpec = new ProductSpec("Pinuk Shampoo 250ml", "Pinuk",
                shampooBySize, 8.0, 14.9, 8);
        Product shampoo250 = new Product(3, shampooSpec);
        controller.addProduct(shampoo250);

        // 4. Bamba 80g
        ProductSpec bambaSpec = new ProductSpec("Bamba 80g", "Osem",
                chipsByBrand, 2.5, 4.5, 20);
        Product bamba = new Product(4, bambaSpec);
        controller.addProduct(bamba);

        // 5. Bissli 70g
        ProductSpec bissliSpec = new ProductSpec("Bissli 70g", "Osem",
                chipsByBrand, 2.0, 3.9, 12);
        Product bissli = new Product(5, bissliSpec);
        controller.addProduct(bissli);

        // ── STOCK ITEMS ─────────────────────────────────────
        // Tnuva 3% Milk 1L
        controller.addStockItem(new StockItem(milkSpec1L, Area.STORE,
                2, 1, 20, LocalDate.of(2026, 7, 1)));
        controller.addStockItem(new StockItem(milkSpec1L, Area.WAREHOUSE,
                1, 3, 50, LocalDate.of(2026, 7, 15)));

        // Tnuva 3% Milk 500ml
        controller.addStockItem(new StockItem(milkSpec500, Area.STORE,
                2, 2, 15, LocalDate.of(2026, 7, 1)));
        controller.addStockItem(new StockItem(milkSpec500, Area.WAREHOUSE,
                1, 3, 30, LocalDate.of(2026, 7, 10)));

        // Pinuk Shampoo 250ml
        controller.addStockItem(new StockItem(shampooSpec, Area.STORE,
                5, 1, 10, null));
        controller.addStockItem(new StockItem(shampooSpec, Area.WAREHOUSE,
                3, 1, 25, null));

        // Bamba 80g
        controller.addStockItem(new StockItem(bambaSpec, Area.STORE,
                4, 3, 40, LocalDate.of(2026, 9, 1)));
        controller.addStockItem(new StockItem(bambaSpec, Area.WAREHOUSE,
                2, 5, 100, LocalDate.of(2026, 10, 1)));

        // Bissli 70g (LOW STOCK: total=8 < min=12)
        controller.addStockItem(new StockItem(bissliSpec, Area.STORE,
                4, 4, 5, LocalDate.of(2026, 8, 1)));
        controller.addStockItem(new StockItem(bissliSpec, Area.WAREHOUSE,
                2, 5, 3, LocalDate.of(2026, 8, 15)));

        // ── PROMOTIONS ──────────────────────────────────────
        // 10% off all Dairy products, April 1-30
        controller.addPromotion(new Promotion(10.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                null, dairy));

        // ── DEFECTIVE REPORTS ───────────────────────────────
        // 3 units of Tnuva 3% Milk 1L reported expired on April 10
        controller.reportDefective(1, 3, "EXPIRED");
    }
}
