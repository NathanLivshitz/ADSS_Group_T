# מטלה 2 - מערכת סופר-לי

**מודולים:** ניהול מלאי + ספקים
**מגישים:** ג'ונתן בר, ניר יטח
**ת"ז:** 322919663, 209805944
**כלי מידול:** draw.io (diagrams.net)

## מבנה ההגשה (תחת docs/)

```
use-cases/
  use-case-diagram.pdf / .png      תרשים Use-Case (סעיף 1.1): אקטורים + תרחישים
  use-case-diagram.xml             מקור התרשים
  use-case-f.pdf                   תיאור טקסטואלי של תרחיש f (סעיף 1.2)
contracts/
  contract-orderShortageFromSupplier.pdf   חוזה 1 (סעיף 2.1)
  contract-selectProduct.pdf                חוזה 2
  contract-confirmOrder.pdf                 חוזה 3
sequence-diagrams/
  seq-diagram-orderShortageFromSupplier.pdf  תרשים רצף לחוזה 1 (סעיף 2.2)
  seq-diagram-selectProduct.pdf              תרשים רצף לחוזה 2
  seq-diagram-confirmOrder.pdf               תרשים רצף לחוזה 3
class-diagram.pdf / .png / .xml    תרשים מחלקות מעודכן (סעיף 3.1)
data-layer-architecture.pdf / .png  פירוט שכבת הנתונים (Repository/DAO/DTO, NF-2)
requirements.pdf                   מסמך דרישות מעודכן (סעיף 3.2)
```

## תאימות בין הסעיפים

המודל אחיד לכל אורך ההגשה. שמות המחלקות והמתודות בתרשים המחלקות תואמים אחד-לאחד
לחוזים (2.1) ולתרשימי הרצף (2.2). תרחיש החוסר (UC f) זורם כך:

1. `InventoryService.orderShortageFromSupplier()` -> `InventoryController.getLowStockProducts()` -> מעבר על `Product`.
2. `InventoryService.selectProduct(productId)` -> `SupplierService.createOrderProposal(...)` -> `SupplierController.findAgreements(...)` -> `SupplyAgreement.priceFor(qty)` -> בחירת הספק הזול -> `OrderProposal.create(...)`.
3. `InventoryService.confirmOrder(orderProposalId)` -> `Order.create(...)` -> `SupplierController.registerAndTransmitOrder(...)` -> `Supplier.receiveOrder(...)` -> `InventoryController.markProductOrdered(...)`.

מערכת הספקים החיצונית ממומשת כ-mock (`SupplierController «mock»`). שכבת הנתונים בתבנית
Repository + DAO + DTO מעל SQLite (לפי מאגר הייחוס שהוצג בתרגול).
