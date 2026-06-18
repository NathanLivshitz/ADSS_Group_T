# מטלה 2 — מערכת סופר-לי · מודולים: מלאי + ספקים

זוג: 322919663 / 209805944. כל מסמכי המטלה מרוכזים בתיקייה זו, מסודרים לפי סעיפי ההגשה.

## מבנה התיקייה

| סעיף | קובץ | תוכן |
|---|---|---|
| 2.1 — חוזים | `2.1-contracts.md` | חוזי שלוש פעולות המערכת של UC f (orderShortageFromSupplier / selectProduct / confirmOrder) בפורמט Larman |
| 2.1 — חוזים (מקור) | `2.1-contracts-nir.pdf` | גרסת ה-PDF המקורית |
| 2.2 — תרשימי רצף | `2.2-sequences/` | שלושה תרשימי Sequence, אחד לכל חוזה |
| 3.1 — תרשים מחלקות | `3.1-class-diagram/class_diagram_combined.{drawio,png}` | Design Class Diagram שכבתי מאוחד (מלאי + ספקים) |
| 3.1 — ארכיטקטורת נתונים | `3.1-class-diagram/data_layer_architecture.{drawio,png}` | פירוט שכבת הנתונים: Repository → DAO → DTO → DatabaseManager (NF-2) |
| 3.2 — דרישות | `3.2-requirements.md` | מסמך דרישות מעודכן (פונקציונליות, NF, מושגים, הנחות, עיצוב) |
| Use Case | `use-case/` | תרשים Use-Case, תרשים Activity ותיאור טקסטואלי של UC f |

## תאימות בין הסעיפים (single source of truth)

המודל אחיד בכל המסמכים. שמות המחלקות והמתודות בתרשים המחלקות (`class_diagram_combined`) תואמים
אחד-לאחד לחוזים (2.1) ולתרשימי הרצף (2.2):

- **UC f, שלב 1** — `InventoryService.orderShortageFromSupplier()` → `InventoryController.getLowStockProducts()` → לולאה על `Product.getTotalQuantity()` / `getMinStockThreshold()`.
- **UC f, שלב 3** — `InventoryService.selectProduct(productId)` → `SupplierService.createOrderProposal(...)` → `SupplierController.findAgreements(...)` → `SupplyAgreement.priceFor(qty)` → בחירת הספק הזול → `OrderProposal.create(...)`.
- **UC f, שלב 9** — `InventoryService.confirmOrder(orderProposalId)` → `Order.create(...)` → `SupplierController.registerAndTransmitOrder(...)` → `Supplier.receiveOrder(...)` → `InventoryController.markProductOrdered(...)` → `updateShortageReport(...)`.

מערכת הספקים החיצונית ממומשת כ-mock (`SupplierController «mock»`); שכבת הנתונים בתבנית
Repository + DAO + DTO מעל SQLite (לפי מאגר הייחוס של התרגול).
