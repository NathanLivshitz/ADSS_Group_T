package Inventory.Data;

import Inventory.Data.DTO.*;
import Inventory.Domain.InventoryController;

public class PreloadData {
    private final InventoryController controller;

    public PreloadData(InventoryController controller) {
        if (controller == null)
            throw new IllegalArgumentException("Controller must not be null");
        this.controller = controller;
    }

    public void load() {
        controller.reset();

        // ── CATEGORIES ──────────────────────────────────────
        int dairyId        = controller.addCategory("Dairy Products", 0);
        int milkId         = controller.addCategory("Milk", dairyId);
        int milkBySizeId   = controller.addCategory("By Size", milkId);

        int toiletriesId   = controller.addCategory("Toiletries", 0);
        int shampooId      = controller.addCategory("Shampoo", toiletriesId);
        int shampooBySizeId = controller.addCategory("By Size", shampooId);

        int snacksId       = controller.addCategory("Snacks", 0);
        int chipsId        = controller.addCategory("Chips", snacksId);
        int chipsByBrandId = controller.addCategory("By Brand", chipsId);

        // ── PRODUCTS ────────────────────────────────────────
        int milk1LId  = controller.addProduct(new ProductDTO(0, 0, "Tnuva 3% Milk 1L",   "Tnuva", milkBySizeId,    4.5, 6.9,  15, 0));
        int milk500Id = controller.addProduct(new ProductDTO(0, 0, "Tnuva 3% Milk 500ml", "Tnuva", milkBySizeId,    3.0, 4.9,  10, 0));
        int shampooId2 = controller.addProduct(new ProductDTO(0, 0, "Pinuk Shampoo 250ml", "Pinuk", shampooBySizeId, 8.0, 14.9,  8, 0));
        int bambaId   = controller.addProduct(new ProductDTO(0, 0, "Bamba 80g",            "Osem",  chipsByBrandId,  2.5, 4.5,  20, 0));
        int bissliId  = controller.addProduct(new ProductDTO(0, 0, "Bissli 70g",           "Osem",  chipsByBrandId,  2.0, 3.9,  12, 0));

        // Retrieve assigned specIds for stock item creation
        int milk1LSpec  = controller.getProduct(milk1LId).specId();
        int milk500Spec = controller.getProduct(milk500Id).specId();
        int shampooSpec = controller.getProduct(shampooId2).specId();
        int bambaSpec   = controller.getProduct(bambaId).specId();
        int bissliSpec  = controller.getProduct(bissliId).specId();

        // ── STOCK ITEMS ─────────────────────────────────────
        controller.addStockItem(new StockItemDTO(milk1LSpec,  "STORE",     2, 1, 20,  "2026-07-01"));
        controller.addStockItem(new StockItemDTO(milk1LSpec,  "WAREHOUSE", 1, 3, 50,  "2026-07-15"));
        controller.addStockItem(new StockItemDTO(milk500Spec, "STORE",     2, 2, 15,  "2026-07-01"));
        controller.addStockItem(new StockItemDTO(milk500Spec, "WAREHOUSE", 1, 3, 30,  "2026-07-10"));
        controller.addStockItem(new StockItemDTO(shampooSpec, "STORE",     5, 1, 10,  null));
        controller.addStockItem(new StockItemDTO(shampooSpec, "WAREHOUSE", 3, 1, 25,  null));
        controller.addStockItem(new StockItemDTO(bambaSpec,   "STORE",     4, 3, 40,  "2026-09-01"));
        controller.addStockItem(new StockItemDTO(bambaSpec,   "WAREHOUSE", 2, 5, 100, "2026-10-01"));
        controller.addStockItem(new StockItemDTO(bissliSpec,  "STORE",     4, 4, 5,   "2026-08-01"));
        controller.addStockItem(new StockItemDTO(bissliSpec,  "WAREHOUSE", 2, 5, 3,   "2026-08-15"));

        // ── PROMOTIONS ──────────────────────────────────────
        // 10% off all Dairy products, April 1-30
        controller.addPromotion(new PromotionDTO(10.0, "2026-04-01", "2026-04-30", 0, dairyId, null, null));

        // ── DEFECTIVE REPORTS ───────────────────────────────
        controller.reportDefective(milk1LId, 3, "EXPIRED");
    }
}
