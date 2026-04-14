# Project State — Inventory Module
> Last updated: 2026-04-14
> Branch: 322919663_209805944

---

## What's Done

### Code — Domain Layer (dev/src/Inventory/Domain/)
- Area.java — enum: STORE, WAREHOUSE
- Category.java — recursive tree (parent/children), holds ProductSpecs
- Product.java — entity (id + ProductSpec)
- ProductSpec.java — value object (name, manufacturer, category, cost, sell, minStock)
- StockItem.java — bulk inventory at one location (spec, area, shelf, row, qty, expiryDate)
- Promotion.java — discount on product or category with date range
- DefectiveReport.java — records defective/expired items
- InventoryReport.java — generated report with date, category filter, product list
- InventoryController.java — all business logic, implements INV-1 through INV-11

### Code — Presentation Layer (dev/src/Inventory/Presentation/)
- InventoryMenu.java — CLI with 15 menu options + exit
  - Supports category path input (c1,c2,c3) with auto-create
  - Accepts shared Scanner from Main

### Code — Data Layer (dev/src/Inventory/Data/)
- PreloadData.java — seeds 5 products, 3 category trees, stock items, 1 promotion, 1 defective report
  - Calls controller.reset() before loading (safe to call multiple times)
  - External to domain layer, calls domain methods only (assignment-compliant)

### Code — Entry Point
- Main.java — startup prompt "Load preloaded test data? (y/n)", passes PreloadData to menu

### Tests (dev/src/tests/) — 132 total, all passing
- DomainEntityTest.java (13 tests) — entity construction and behavior
- InventoryControllerTest.java (30 tests) — all controller operations + reset + empty state
- DomainEdgeCaseTest.java (41 tests) — null, negative, boundary validation
- PreloadDataTest.java (22 tests) — seed data correctness + double-load safety

### Deliverables
- docs/instructions.pdf — Hebrew, run instructions + preloaded data + menu guide
- release/adss2025_v01.jar — working JAR, run with: java -jar adss2025_v01.jar
- docs/spec.md — full spec with requirements, design decisions, class diagram, implementation guide

---

## What's NOT Committed Yet

These files exist locally but are untracked:
- dev/src/Inventory/Main.java
- dev/src/Inventory/Data/PreloadData.java
- dev/src/Inventory/Service/InventoryService.java (empty stub)
- dev/src/tests/DomainEdgeCaseTest.java
- dev/src/tests/PreloadDataTest.java
- docs/instructions.pdf
- docs/spec.md
- docs/domain-uml.drawio
- docs/state.md (this file)
- release/adss2025_v01.jar

Modified (tracked):
- dev/src/Inventory/Domain/InventoryController.java (added reset(), removeExpiredStock())
- dev/src/Inventory/Presentation/InventoryMenu.java (new constructor, menu options, category path)

---

## TODO for Partner

### Code: InventoryService (INV-NF4)
File: dev/src/Inventory/Service/InventoryService.java (currently empty)

The assignment recommends a Service layer bridging Presentation to Domain:
InventoryMenu -> InventoryService -> InventoryController

Steps:
1. Add field: private InventoryController controller
2. Constructor: InventoryService(InventoryController controller)
3. Create wrapper methods for every public InventoryController method
4. Update InventoryMenu to take InventoryService instead of InventoryController
5. Update Main.java to create InventoryService and pass it to InventoryMenu
6. Add unit tests for the Service layer

### Docs: Requirements Table PDF (Table 1)
Content exists in docs/spec.md section 4.
Must be exported as a standalone PDF **in Hebrew**.
Format per assignment: ID, Module, Functional/Non-Functional, Description, Priority, Risk, Status.

### Docs: Open Questions Table PDF (Table 2)
Content exists in docs/spec.md section 5.
Must be exported as a standalone PDF **in Hebrew**.
Format per assignment: #, Topic, Issue, Impact, Resolution.

### Docs: Class Diagram PDF
Drawio source: docs/domain-uml.drawio

Must be updated to include:
- All classes (including InventoryService once implemented)
- Attributes only (no methods) per assignment section 2.2
- Multiplicities on all relationships (1..1, 1..*, 0..1, etc.)
- Roles on relationships
- Domain layer and Data layer classes
- Export as PDF for submission (font size 8+ per assignment)

### Docs: instructions.pdf
May need update if menu changes after InventoryService is wired up.

---

## Git Instructions

### Files to commit (use specific paths, NOT git add .):
```
git add dev/src/Inventory/Main.java
git add dev/src/Inventory/Data/PreloadData.java
git add dev/src/Inventory/Service/InventoryService.java
git add dev/src/Inventory/Domain/InventoryController.java
git add dev/src/Inventory/Presentation/InventoryMenu.java
git add dev/src/tests/DomainEdgeCaseTest.java
git add dev/src/tests/PreloadDataTest.java
git add docs/instructions.pdf
git add docs/spec.md
git add docs/domain-uml.drawio
git add docs/state.md
git add release/adss2025_v01.jar
```

### Files to NEVER commit:
- .classpath, .project (IDE files)
- .claude/ (tool config)
- dev/out/ (already in .gitignore)

### After committing:
```
git push origin 322919663_209805944
```

---

## Architecture Overview

```
Main.java (entry point)
  |
  +-> InventoryController (domain logic)
  +-> PreloadData(controller) (seed data, external to domain)
  +-> InventoryMenu(controller, preloadData, scanner) (CLI)
        |
        +-> controller.addProduct(), getProduct(), ...
        +-> preloadData.load() (menu option 15)
```

Assignment-required flow (after partner adds Service):
```
Main.java
  +-> InventoryController
  +-> InventoryService(controller)
  +-> PreloadData(controller)
  +-> InventoryMenu(service, preloadData, scanner)
        +-> service.addProduct() -> controller.addProduct()
```
