# Inventory Module (מלאי) — Deep Dive Guide
> Assignment: Practical Assignment #1 — Super-Li Supermarket System
> Course: Analysis and Design of Software Systems S2 (372-1-3401)
> Due: 2026-04-19, 23:59
> Module: Inventory (מחסן/מלאי)
> Generated: 2026-04-12
> Sources: ProjectDescription.pdf, PracticalAssignment1_2026.pdf, Moodle forum "שאלות ללקוח [מחסן/מלאי]", general Q&A forum

---

## 1. The Inventory Module — What the Client Wants

From the project description (סיפור מערכת), the inventory story:

**Current situation:** On Mondays and Thursdays, a store employee walks through the shelves and the small back warehouse, recording shortages from the week's sales. Customers are complaining about frequent shortages and long restocking times.

**What the client wants built:**
1. Proactive stock alerts (not reactive manual checks)
2. Full product tracking — name, location (shelf vs warehouse), manufacturer, quantities
3. Pricing management with supplier discounts
4. Promotions/sales with date ranges
5. Hierarchical product categories (3 levels)
6. Inventory reports (weekly or on-demand, filterable by category)
7. Defective/expired item tracking and reporting

---

## 2. Entities to Model

### Core Entities

| Entity | Key Fields | Notes |
|--------|-----------|-------|
| **ProductSpec** (הגדרת פריט) | name, manufacturer, category_id, min_stock_threshold, cost_price, selling_price | Value object — defines WHAT a product IS. cost_price and selling_price reflect current supplier pricing (updated when supplier discounts change). No id, no quantity |
| **Product** (פריט) | id, spec (ProductSpec) | Entity — identified by id, wraps a ProductSpec |
| **StockItem** (יחידת מלאי) | spec (ProductSpec), area (STORE/WAREHOUSE), shelf_number, row_number, quantity, expiry_date | Bulk inventory at one location. expiryDate is per-batch (nullable). References spec directly (not Product) |
| **Category** (קטגוריה) | name, parent_category_id | Recursive tree — 3+ levels deep |
| **Discount/Promotion** (מבצע) | product_id OR category_id, discount_percent, start_date, end_date | Can target specific products or entire categories |
| **DefectiveItemReport** (דוח פגומים) | product_id, quantity, reason (DEFECTIVE/EXPIRED), report_date | Employee-reported per product. Triggers quantity reduction (system distributes across locations) |
| **InventoryReport** (דוח מלאי) | report_date, categories_filter, items[] | Generated on demand |
### Cross-Module Entities (you reference but don't own)

| Entity | Owned By | Your Interface |
|--------|----------|---------------|
| Supplier | Suppliers module | You store current cost/sell price on ProductSpec; supplier-specific pricing managed by Suppliers module |
| Order | Suppliers module | Shortage alerts feed into supplier orders |
| Transport | Transport module | Restocking triggers transport |
| Employee | HR module | Employees report defectives, do stock checks |

---

## 3. Forum Q&A — Clarifications for Inventory

These are from the dedicated "שאלות ללקוח [מחסן/מלאי]" forum:

### Q: How should stock alerts work? Who gets them?
**Context:** "לגבי ההתראות לקראת גמר המלאי - איך ההתראות אמורות להיות מוצגות? ולמי?"

**Your design decision:** Alerts display in the system when an employee or manager queries stock. The system flags products below min_stock_threshold. No push notifications needed — just a queryable alert list or flag on products.

### Q: What is an inventory report? What does it show?
**Context:** "מה הוא דוח מלאי? ומה הוא מציג?"

**Your design decision:** A report listing products and their quantities, filtered by one or more categories. Shows: product name, manufacturer, quantity in store, quantity in warehouse, total, status (OK/LOW/OUT). Can be generated weekly or on-demand.

### Q: Category hierarchy — recursive or fixed 3 levels?
**Context:** "האם המבנה הוא קטגוריה -> תת קטגוריה -> מוצרים? או שכל קטגוריה תוכל להחזיק תת קטגוריה?"

**Your design decision:** The description says "קטגוריה, תת קטגוריה, תת תת קטגוריה" — model as a **recursive tree** (Category has parent_category_id). This is more flexible and matches the UML aggregation pattern from lectures. A product belongs to a leaf category.

Example:
```
Dairy Products (קטגוריה)
  └── Milk (תת קטגוריה)
       └── By Size (תת תת קטגוריה)
            ├── Tnuva 3% 1L
            └── Tnuva 3% 500ml

Toiletries (קטגוריה)
  └── Shampoo (תת קטגוריה)
       └── By Size (תת תת קטגוריה)
            └── Pinuk 250ml
```

### Q: Expiry tracking — per item, batch, or shipment?
**Context:** "כיצד מוגדר ומנוהל תוקף מוצרים — האם ברמת פריט, אצווה או משלוח?"

**Your design decision:** Track expiry at the **StockItem (batch) level** — each StockItem has its own `expiryDate` (nullable). Different batches of the same product type can have different expiry dates. When items expire, the system must **automatically reduce quantity** from the relevant StockItem — expired items don't just get flagged, they get removed from active stock. The `reportDefective(productId, qty)` flow should reduce total quantity by distributing the removal across StockItems (store first, then warehouse).

### Q: How does inventory get updated after sales/deliveries?
**Context:** "כיצד מודול המלאי מקבל עדכונים על שינויי מלאי שנגרמו ממכירה או מקבלת אספקה?"

**Your design decision:** Since modules are developed independently, your module exposes methods like `updateQuantity(productId, delta)` and `receiveShipment(productId, quantity)`. In the final integration, the Suppliers/Transport module calls these. For Assignment 1, preload test data and test these methods via CLI.

### Q: Shelf alerts vs total stock alerts?
**Context:** "האם ההתראות מתייחות רק לכמות הכוללת בסניף (לטובת הזמנה מספקים)?"

**Your design decision:** Track quantities separately (shelf vs warehouse) but alert based on **total** quantity (store + warehouse). Shelf-specific alerts ("go refill from warehouse") are nice-to-have (NTH). Document shelf alerts as a future feature.

### Q: Exact product location (shelf 2, row 3)?
**Context:** "האם המערכת צריכה לספק את המיקום המדויק כמו מדף השני בשורה השלישית?"

**Your design decision:** YES — track exact location as shelf + row for both store and warehouse. The data model uses a **Location** object (shelf_number, row_number, area: STORE/WAREHOUSE). This makes the system more useful without adding much complexity — it's just a nested structure per product instead of flat quantity. The warehouse follows the same shelf+row scheme as the store floor.

### Q: Defective/expired — alerts or manual entry only?
**Context:** "האם המערכת צריכה להתריע על מוצרים שעומדים להיות/נהיו פגי תוקף? או שרק עובדים יכולים להזין?"

**Your design decision:** Both — employees manually report defectives they find, AND the system should flag products approaching expiry (if you track expiry dates). For Assignment 1, focus on manual entry. Proactive expiry alerts can be NTH.

---

## 4. Requirements Table — Inventory Module

### Functional Requirements

| ID | Module | F/NF | Description | Priority | Risk | Status |
|----|--------|------|-------------|----------|------|--------|
| INV-1 | Inventory | F | The system SHALL maintain a product catalog with name, manufacturer, category, cost price, and selling price | MH | Low | Done |
| INV-2 | Inventory | F | The system SHALL track product quantities by exact location (area + shelf + row) for both store and warehouse | MH | Medium | Done |
| INV-3 | Inventory | F | The system SHALL alert when a product's total quantity falls below its minimum stock threshold | MH | Medium | Done |
| INV-4 | Inventory | F | The system SHALL support a hierarchical category structure (category → sub-category → sub-sub-category) | MH | Medium | Done |
| INV-5 | Inventory | F | The system SHALL allow defining promotions with percentage discounts on products or categories within date ranges | MH | Medium | Done |
| INV-6 | Inventory | F | The system SHALL generate inventory reports filterable by one or more categories | MH | Low | Done |
| INV-7 | Inventory | F | The system SHALL allow employees to report defective or expired items | MH | Low | Done |
| INV-8 | Inventory | F | The system SHALL generate periodic defect reports listing all reported defective/expired items | MH | Low | Done |
| INV-9 | Inventory | F | The system SHALL preserve and track the cost price (reflecting supplier discounts) and selling price per product as part of the item's stored information | MH | Low | Done |
| INV-10 | Inventory | F | The system SHALL allow updating product quantities when stock is received, sold, or expired | MH | Low | Done |
| INV-11 | Inventory | F | The system SHALL automatically reduce quantity when items are reported expired/defective | MH | Low | Done |
| INV-12 | Inventory | F | The system CAN proactively alert on products approaching expiry date | NTH | High | Deferred |

### Non-Functional Requirements

| ID | Module | F/NF | Description | Priority | Risk | Status |
|----|--------|------|-------------|----------|------|--------|
| INV-NF1 | Inventory | NF | All data stored in memory, resets each run (no database) | MH | Low | Done |
| INV-NF2 | Inventory | NF | CLI interface for all operations | MH | Low | Done |
| INV-NF3 | Inventory | NF | Preloaded test data available on startup | MH | Low | Done |
| INV-NF4 | Inventory | NF | Service layer shall bridge Presentation to Domain (InventoryMenu → InventoryService → InventoryController) | MH | Low | TODO |
| INV-NF5 | Inventory | NF | Edge case validation tests for all domain classes (null inputs, negative values, empty strings, boundary conditions) | MH | Low | Done |

---

## 5. Open Questions Table

| # | Topic | Issue | Impact | Resolution |
|---|-------|-------|--------|------------|
| 1 | Expiry tracking | Per item, batch, or product level? | <5h | Tracked per StockItem (batch level). Different batches of the same product type can have different expiry dates |
| 2 | Location detail | Exact shelf/row or just shelf vs warehouse? | <5h | Exact location (area + shelf + row). Same structure for store and warehouse |
| 3 | Alert delivery | Push notifications or query-based? | <5h | Assumed query-based (flag on product) |
| 4 | Stock check automation | Automatic or employee-triggered? | <5h | Assumed employee-triggered (Mon/Thu as described) |
| 5 | Category depth | Fixed 3 levels or unlimited recursion? | <5h | Assumed recursive tree (matches UML patterns) |

---

## 6. Class Diagram — Inventory Module

### Presentation Layer

```
InventoryMenu
  - scanner: Scanner
  Fields:
  - inventoryService: InventoryService

  (handles CLI: add product, update stock, search, reports, defectives)
```

### Domain Layer — UML Class Diagram

```
┌─────────────────────────────────┐
│          «value object»         │
│           ProductSpec           │
├─────────────────────────────────┤
│ - name: String                  │
│ - manufacturer: String          │
│ - costPrice: double             │  ← reflects supplier discounts
│ - sellPrice: double             │  ← reflects supplier discounts
│ - minStockThreshold: int        │
├─────────────────────────────────┤
│ + getCostPrice(): double        │
│ + setCostPrice(double): void    │
│ + getSellPrice(): double        │
│ + setSellPrice(double): void    │
│ + getMinStockThreshold(): int   │
└─────────────────────────────────┘
        │ *..1           │ 1..*
        │                │
        ▼                ▼
┌──────────────┐  ┌─────────────────────────┐
│  «entity»    │  │       StockItem          │
│   Product    │  ├─────────────────────────┤
├──────────────┤  │ - area: Area             │
│ - id: int    │  │ - shelfNumber: int       │
├──────────────┤  │ - rowNumber: int         │
│ + getId()    │  │ - quantity: int          │
│ + getSpec()  │  │ - expiryDate: LocalDate  │  ← nullable, per batch
└──────────────┘  ├─────────────────────────┤
                  │ + getQuantity(): int     │
                  │ + setQuantity(int): void │
                  └─────────────────────────┘

┌──────────────────────────────┐
│          Category            │
├──────────────────────────────┤     ◇ parent
│ - name: String               │◄────────────┐
│ - parent: Category           │  0..1    *   │
│ - subCategories: List        │─────────────┘
│ - products: List<ProductSpec>│
├──────────────────────────────┤
│ + getAllProducts(): List      │
│ + addProduct(ProductSpec)    │
└──────────────────────────────┘
        ▲ *..1
        │
        │ category
┌───────┴─────────────────────────┐
│          ProductSpec             │  (same class as above)
└─────────────────────────────────┘

┌──────────────────────────────┐
│         Promotion            │
├──────────────────────────────┤
│ - discountPercent: double    │
│ - startDate: LocalDate       │
│ - endDate: LocalDate         │
│ - targetProduct: ProductSpec │  ← null if targeting category
│ - targetCategory: Category   │  ← null if targeting product
├──────────────────────────────┤
│ + isActive(): boolean        │
│ + getEffectivePrice(): double│
│ + appliesTo(ProductSpec): boolean │
└──────────────────────────────┘

┌──────────────────────────────┐  ┌─────────────────────────────┐
│      DefectiveReport         │  │      InventoryReport        │
├──────────────────────────────┤  ├─────────────────────────────┤
│ - productId: int             │  │ - reportDate: LocalDate     │
│ - quantity: int              │  │ - categoriesFilter: List    │
│ - reason: String             │  │ - items: List<Product>      │
│ - reportDate: LocalDate      │  └─────────────────────────────┘
└──────────────────────────────┘

┌───────────────────────────────────────────┐
│          InventoryController              │
├───────────────────────────────────────────┤
│ - catalog: Map<Integer, Product>          │
│ - stockItems: List<StockItem>             │
│ - rootCategories: List<Category>          │
│ - promotions: List<Promotion>             │
│ - defectiveReports: List<DefectiveReport> │
├───────────────────────────────────────────┤
│ + addProduct(Product): void               │
│ + getProduct(int): Product                │
│ + addStockItem(StockItem): void           │
│ + getTotalQuantity(int): int              │
│ + updateQuantity(...): void               │
│ + getLowStockProducts(): List<Product>    │
│ + addPromotion(Promotion): void           │
│ + getEffectivePrice(int): double          │
│ + reportDefective(...): void              │
│ + generateReport(List<Category>): InventoryReport │
└───────────────────────────────────────────┘
```

### Relationships

```
ProductSpec  1 ──────── *  Product         (each Product references one spec)
ProductSpec  1 ──────── *  StockItem       (many stock entries per spec)
ProductSpec  * ──────── 1  Category        (each spec belongs to one category)
Category     0..1 ◇──── *  Category        (self-aggregation: parent → children)
Category     1 ◇─────── *  ProductSpec     (aggregation: category holds products)
Promotion    * ─ ─ ─ ─  0..1 ProductSpec   (optional target, XOR with category)
Promotion    * ─ ─ ─ ─  0..1 Category      (optional target, XOR with product)
DefectiveReport * ────── 1  Product        (by productId)
```

### UML Notes
- **Aggregation (◇)**: Category aggregates subcategories and products (provides find, remove, count)
- **Association (──)**: Simple reference (Product→ProductSpec, StockItem→ProductSpec)
- **Dashed (─ ─)**: Optional association (Promotion targets one OR the other, never both)
- ProductSpec is a **value object** (no identity) — Product and StockItem share the same spec reference
- Category hierarchy = self-referencing aggregation (recursive tree)

---

## 7. Java Implementation Guide

### Package Structure

```
dev/
└── src/
    ├── Presentation/
    │   ├── Main.java                    // Entry point — loads data, starts menu
    │   ├── Presentation/
    │   │   └── InventoryMenu.java      // CLI menus
    │   ├── Service/
    │   │   └── InventoryService.java   // Bridges presentation ↔ domain (TODO)
    │   ├── Data/
    │   │   └── PreloadData.java        // Seed data for startup/tests
    │   └── Domain/
    │       ├── ProductSpec.java        // Value object — type definition
    │       ├── Product.java            // Entity — id + ProductSpec
    │       ├── StockItem.java          // Bulk inventory at one location
    │       ├── Category.java
    │       ├── Promotion.java
    │       ├── DefectiveReport.java
    │       ├── InventoryReport.java
    │       ├── InventoryController.java // Main business logic
    │       └── Area.java               // Enum: STORE, WAREHOUSE
```

### Key Implementation Patterns

**Category hierarchy (recursive, holds ProductSpec not Product):**
```java
class Category {
    private String name;
    private Category parent;  // null for root categories
    private List<Category> subCategories = new ArrayList<>();
    private List<ProductSpec> products = new ArrayList<>();  // only leaf categories

    public List<ProductSpec> getAllProducts() {
        // Recursive: collect from this + all sub-categories
        List<ProductSpec> all = new ArrayList<>(products);
        for (Category sub : subCategories) {
            all.addAll(sub.getAllProducts());
        }
        return all;
    }
}
```

**ProductSpec — value object (type definition, no id):**
```java
class ProductSpec {
    private String name;
    private String manufacturer;
    private Category category;
    private double costPrice;
    private double sellPrice;
    private int minStockThreshold;
    // Value object — no id, no expiry, no locations
}
```

**Product — entity (id + spec):**
```java
class Product {
    private int id;
    private ProductSpec spec;
    // Entity — just id + spec
}
```

**StockItem — bulk inventory at one location:**
```java
enum Area { STORE, WAREHOUSE }

class StockItem {
    private ProductSpec spec;      // shared reference — same object as product.getSpec()
    private Area area;             // STORE or WAREHOUSE
    private int shelfNumber;
    private int rowNumber;
    private int quantity;
    private LocalDate expiryDate;  // nullable — per batch
    // e.g., STORE shelf=3 row=2 qty=15 expiry=2026-07-01
    //        WAREHOUSE shelf=1 row=5 qty=40 expiry=2026-07-15
}
```

**Promotion with flexible targeting (targets ProductSpec, not Product):**
```java
class Promotion {
    private double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProductSpec targetProduct;    // null if targeting category
    private Category targetCategory;     // null if targeting product

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
}
```

**InventoryController — key methods:**
```java
class InventoryController {
    private Map<Integer, Product> catalog = new HashMap<>();   // id → Product(id, spec)
    private List<StockItem> stockItems = new ArrayList<>();    // all inventory entries
    private List<Category> rootCategories = new ArrayList<>();
    private List<Promotion> promotions = new ArrayList<>();
    private List<DefectiveReport> defectiveReports = new ArrayList<>();

    // Catalog
    public void addProduct(Product p) { ... }
    public Product getProduct(int id) { ... }

    // Stock
    public void addStockItem(StockItem item) { ... }
    public int getTotalQuantity(int productId) { ... }  // aggregates StockItems by shared spec
    public void updateQuantity(int productId, Area area, int shelf, int row, int delta) { ... }

    // Alerts — aggregates across all StockItems for each product
    public List<Product> getLowStockProducts() {
        return catalog.values().stream()
            .filter(p -> getTotalQuantity(p.getId()) < p.getSpec().getMinStockThreshold())
            .collect(Collectors.toList());
    }

    // Reports
    public InventoryReport generateReport(List<Category> categories) { ... }
    public List<DefectiveReport> getDefectiveReport(LocalDate from, LocalDate to) { ... }

    // Promotions
    public void addPromotion(Promotion p) { ... }
    public double getEffectivePrice(int productId) { ... }

    // Defectives — ALSO reduces quantity from StockItems
    public void reportDefective(int productId, int qty, String reason) {
        defectiveReports.add(new DefectiveReport(productId, qty, reason, LocalDate.now()));
        removeStock(productId, qty);  // drain store-first, then warehouse
    }
}
```

---

## 8. Unit Tests — Inventory (10+ required)

Focus on **Domain layer** (InventoryController + Product + Category):

| # | Test | What it verifies |
|---|------|-----------------|
| 1 | `testAddProduct` | Product added to catalog, retrievable by ID |
| 2 | `testUpdateQuantityByLocation` | Quantity updates target specific area+shelf+row |
| 3 | `testLowStockAlert` | Product below min threshold appears in alert list |
| 4 | `testLowStockAlertNotTriggeredAboveThreshold` | Product above threshold doesn't trigger alert |
| 5 | `testCategoryHierarchy` | Sub-categories correctly nest, getAllProducts returns recursively |
| 6 | `testPromotionActiveDiscount` | Active promotion returns discounted price |
| 7 | `testPromotionExpiredNoDiscount` | Expired promotion returns full price |
| 8 | `testPromotionOnCategory` | Category-level promotion applies to all products in category |
| 9 | `testReportDefectiveReducesQuantity` | Defective report recorded AND total quantity reduced across locations |
| 10 | `testInventoryReportFilterByCategory` | Report shows only products in selected categories |
| 11 | `testReceiveShipment` | Warehouse quantity increases after shipment |
| 12 | `testDefectiveReportPeriodFilter` | Defect report filtered by date range |

**Remember:** Tests must fail before implementation and pass after (TDD).

---

## 9. Preloaded Test Data (for instructions.pdf)

Your `instructions.pdf` must specify what data exists on startup:

```
Categories:
  Dairy Products
    └── Milk
         └── By Size
  Toiletries
    └── Shampoo
         └── By Size
  Snacks
    └── Chips
         └── By Brand

Products (Product = id + ProductSpec, StockItems share the same spec):
  1. Tnuva 3% Milk 1L
     Product: id=1, spec=(name="Tnuva 3% Milk 1L", mfg="Tnuva", cat=Dairy/Milk/BySize, cost=4.5, sell=6.9, minStock=15)
     StockItems (same spec):
       STORE     shelf=2 row=1 qty=20 expiry=2026-07-01
       WAREHOUSE shelf=1 row=3 qty=50 expiry=2026-07-15
  2. Tnuva 3% Milk 500ml
     Product: id=2, spec=(name="Tnuva 3% Milk 500ml", mfg="Tnuva", cat=Dairy/Milk/BySize, cost=3.0, sell=4.9, minStock=10)
     StockItems:
       STORE     shelf=2 row=2 qty=15 expiry=2026-07-01
       WAREHOUSE shelf=1 row=3 qty=30 expiry=2026-07-10
  3. Pinuk Shampoo 250ml
     Product: id=3, spec=(name="Pinuk Shampoo 250ml", mfg="Pinuk", cat=Toiletries/Shampoo/BySize, cost=8.0, sell=14.9, minStock=8)
     StockItems:
       STORE     shelf=5 row=1 qty=10 expiry=null
       WAREHOUSE shelf=3 row=1 qty=25 expiry=null
  4. Bamba 80g
     Product: id=4, spec=(name="Bamba 80g", mfg="Osem", cat=Snacks/Chips/ByBrand, cost=2.5, sell=4.5, minStock=20)
     StockItems:
       STORE     shelf=4 row=3 qty=40 expiry=2026-09-01
       WAREHOUSE shelf=2 row=5 qty=100 expiry=2026-10-01
  5. Bissli 70g
     Product: id=5, spec=(name="Bissli 70g", mfg="Osem", cat=Snacks/Chips/ByBrand, cost=2.0, sell=3.9, minStock=12)
     StockItems:
       STORE     shelf=4 row=4 qty=5 expiry=2026-08-01
       WAREHOUSE shelf=2 row=5 qty=3 expiry=2026-08-15  ← LOW STOCK (total=8 < min=12)!

Active Promotion:
  - 10% off all Dairy products, April 1-30

Defective Reports:
  - 3 units of Tnuva 3% Milk 1L (productId=1) reported expired on April 10
```

---

## 10. Cross-Module Integration Points

For the final integration (future assignments), your inventory module will need to interface with:

| Module | They call you | You call them |
|--------|--------------|---------------|
| **Suppliers** | `receiveShipment(productId, qty)` after delivery | Nothing directly (but your shortage alerts feed their order decisions) |
| **Transport** | `receiveShipment(productId, qty)` after transport arrival | Nothing directly |
| **HR** | N/A | N/A (employees use your CLI) |

Design your `InventoryController` methods as clean public interfaces that other modules can call.

---

## 11. Common Mistakes — Inventory Specific

1. **Flat categories** — don't use 3 separate string fields (category, subCategory, subSubCategory). Use a recursive Category class with parent reference. This is what the lectures teach.
2. **Flat quantity fields** — don't use `shelfQty` and `warehouseQty` ints. Use `StockItem` entries with area+shelf+row+qty+expiryDate. Both store and warehouse follow the same shelf+row structure. Expiry is per-batch (per StockItem), not per product type.
2b. **Not reducing quantity on defective/expired** — when an item is reported defective or expired, its quantity must be reduced from the relevant StockItems. `reportDefective` drains store-first, then warehouse.
3. **No min-stock per product** — the spec says "ההתראה לכל מוצר תקבע עבור כמות מינימלית שונה". Each product has its own threshold.
4. **Promotions without dates** — promotions must have start/end dates per the spec.
5. **Category promotions not cascading** — a promotion on "Dairy" should affect all products under Dairy, including sub-categories.
6. **Business logic in Presentation** — keep all filtering, alerting, report generation in the Domain layer. Presentation just collects input and displays output.
7. **No preloaded data** — the JAR must start with test data already loaded. Document it in instructions.pdf.
8. **Testing Presentation instead of Domain** — your 10+ unit tests should test InventoryController methods, not CLI menus.

---

## 12. Instructions Page (for instructions.pdf)

### How to Run

```
java -jar adss2025_v01.jar
```

The system starts with preloaded test data (see below). No database is required — all data is stored in memory and resets on each run.

### Main Menu

After startup, the system prints `Preloaded data ready.` and presents the Inventory Management menu:

```
=== Inventory Management ===
1.  Add product
2.  Add stock to product
3.  View product locations & quantities
4.  Update stock
5.  Low stock alerts
6.  Add category
7.  Add promotion
8.  View active promotions
9.  Check effective price
10. Report defective/expired
11. Locate defective items
12. Defective report by dates
13. Generate inventory report
0.  Exit
Choose:
```

### Menu Operations Guide

| # | Operation | Inputs | What it does |
|---|-----------|--------|-------------|
| 1 | Add product | ID, name, manufacturer, category name, cost price, sell price, min stock threshold | Registers a new product in the catalog |
| 2 | Add stock to product | Product ID, area (STORE/WAREHOUSE), shelf, row, quantity, expiry date (or empty) | Creates a new stock batch at a specific location |
| 3 | View product locations & quantities | Product ID | Shows all stock locations, quantities, and expiry dates for the product, plus store/warehouse/total summary |
| 4 | Update stock | Product ID, area, shelf, row, delta (+/-) | Adjusts quantity at a specific location (positive to add, negative to remove) |
| 5 | Low stock alerts | (none) | Lists all products whose total quantity is below their minimum stock threshold |
| 6 | Add category | Category name, parent category name (or empty for root) | Creates a new category in the hierarchy |
| 7 | Add promotion | Discount %, start date, end date, target type (PRODUCT/CATEGORY), target ID or name | Creates a percentage discount on a product or category for a date range |
| 8 | View active promotions | (none) | Lists all currently active promotions |
| 9 | Check effective price | Product ID | Shows the selling price after applying any active promotions |
| 10 | Report defective/expired | Product ID, quantity, reason (DEFECTIVE/EXPIRED) | Records a defect report AND automatically reduces stock (store-first, then warehouse) |
| 11 | Locate defective items | (none) | Shows stock locations for all products that have been reported as defective |
| 12 | Defective report by dates | From date, To date (YYYY-MM-DD) | Lists all defective reports within the specified period |
| 13 | Generate inventory report | Filter by categories? (y/n), category names if yes | Generates a report of all products (or filtered by categories) with store/warehouse quantities and status (OK/LOW/OUT) |
| 0 | Exit | (none) | Exits the system |

### Date Format

All dates are entered in **YYYY-MM-DD** format (e.g., `2026-04-30`). Expiry dates can be left empty (press Enter) for products without expiry.

### Preloaded Data at Startup

The system starts with the following data already loaded:

#### Categories (3-level hierarchy)

```
Dairy Products
  └── Milk
       └── By Size
Toiletries
  └── Shampoo
       └── By Size
Snacks
  └── Chips
       └── By Brand
```

#### Products

| ID | Name | Manufacturer | Category | Cost | Sell | Min Stock |
|----|------|-------------|----------|------|------|-----------|
| 1 | Tnuva 3% Milk 1L | Tnuva | Dairy Products > Milk > By Size | 4.50 | 6.90 | 15 |
| 2 | Tnuva 3% Milk 500ml | Tnuva | Dairy Products > Milk > By Size | 3.00 | 4.90 | 10 |
| 3 | Pinuk Shampoo 250ml | Pinuk | Toiletries > Shampoo > By Size | 8.00 | 14.90 | 8 |
| 4 | Bamba 80g | Osem | Snacks > Chips > By Brand | 2.50 | 4.50 | 20 |
| 5 | Bissli 70g | Osem | Snacks > Chips > By Brand | 2.00 | 3.90 | 12 |

#### Stock Items (inventory by location)

| Product | Area | Shelf | Row | Qty | Expiry |
|---------|------|-------|-----|-----|--------|
| Tnuva 3% Milk 1L | STORE | 2 | 1 | 20 | 2026-07-01 |
| Tnuva 3% Milk 1L | WAREHOUSE | 1 | 3 | 50 | 2026-07-15 |
| Tnuva 3% Milk 500ml | STORE | 2 | 2 | 15 | 2026-07-01 |
| Tnuva 3% Milk 500ml | WAREHOUSE | 1 | 3 | 30 | 2026-07-10 |
| Pinuk Shampoo 250ml | STORE | 5 | 1 | 10 | — |
| Pinuk Shampoo 250ml | WAREHOUSE | 3 | 1 | 25 | — |
| Bamba 80g | STORE | 4 | 3 | 40 | 2026-09-01 |
| Bamba 80g | WAREHOUSE | 2 | 5 | 100 | 2026-10-01 |
| Bissli 70g | STORE | 4 | 4 | 5 | 2026-08-01 |
| Bissli 70g | WAREHOUSE | 2 | 5 | 3 | 2026-08-15 |

> **Note:** Bissli 70g has total stock = 8, which is below its min threshold of 12. It will appear in low stock alerts.

> **Note:** Tnuva 3% Milk 1L quantities shown are **after** the preloaded defective report reduced 3 units from store stock (20 → 17).

#### Active Promotion

- **10% off all Dairy Products** — April 1 to April 30, 2026
  - Applies to: Tnuva 3% Milk 1L (effective price: 6.21), Tnuva 3% Milk 500ml (effective price: 4.41)

#### Defective Reports

- **3 units** of Tnuva 3% Milk 1L (ID=1) reported as **EXPIRED** on 2026-04-10
  - Stock was automatically reduced by 3 units from store location

### Example: Verifying Preloaded Data

After startup, try these operations to verify the system:

1. **Option 5 — Low stock alerts** → Should show Bissli 70g (total=8, min=12)
2. **Option 3 — View product stock for ID=1** → Should show Tnuva 3% Milk 1L at two locations (store qty=17, warehouse qty=50)
3. **Option 9 — Check effective price for ID=1** → Should show 6.21 (10% off 6.90)
4. **Option 8 — View active promotions** → Should show 10% off Dairy Products (April 1-30)
5. **Option 12 — Defective report from 2026-04-01 to 2026-04-30** → Should show the expired milk report
