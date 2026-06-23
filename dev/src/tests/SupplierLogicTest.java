package tests;

import Suppliers.Domain.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Supplier selection logic for the shortage / periodic order flows (UC-f / UC-e).
public class SupplierLogicTest {

    @Test
    void orderProposalPicksCheapestSupplier() {
        SupplierController sc = new SupplierController();
        Supplier expensive = sc.addSupplier(1, "Expensive Co");
        Supplier cheap = sc.addSupplier(2, "Cheap Co");
        sc.addAgreement(new SupplyAgreement(expensive, 1, 5, 10.00));
        sc.addAgreement(new SupplyAgreement(cheap, 1, 5, 6.00));

        OrderProposal p = sc.createOrderProposal(1, 10);
        assertEquals(2, p.getSupplierId());
        assertEquals(6.00, p.getPrice(), 0.001);
    }

    @Test
    void orderProposalKeepsRequestedQuantityAndPrice() {
        SupplierController sc = new SupplierController();
        Supplier s = sc.addSupplier(1, "Only Co");
        sc.addAgreement(new SupplyAgreement(s, 7, 5, 4.00));

        OrderProposal p = sc.createOrderProposal(7, 12);
        assertEquals(1, p.getSupplierId());
        assertEquals(12, p.getRequiredQty());
        assertEquals(4.00, p.getPrice(), 0.001);
    }

    @Test
    void orderProposalNoAgreementThrows() {
        SupplierController sc = new SupplierController();
        sc.addSupplier(1, "Some Co");
        assertThrows(IllegalArgumentException.class, () -> sc.createOrderProposal(99, 5));
    }

    @Test
    void findAgreementsReturnsOnlyThatSpec() {
        SupplierController sc = new SupplierController();
        Supplier a = sc.addSupplier(1, "A");
        Supplier b = sc.addSupplier(2, "B");
        sc.addAgreement(new SupplyAgreement(a, 3, 5, 9.00));
        sc.addAgreement(new SupplyAgreement(b, 3, 5, 8.00));
        sc.addAgreement(new SupplyAgreement(a, 4, 5, 7.00));

        assertEquals(2, sc.findAgreements(3).size());
        assertEquals(1, sc.findAgreements(4).size());
    }
}
