package Suppliers.Service;

import Shared.DTO.*;
import Suppliers.Domain.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Suppliers.
 * All public methods accept and return only DTOs.
 * Domain types are converted to DTOs before leaving this class.
 */
public class SupplierService {
    private final SupplierController supplierSystem;

    /** No-arg constructor — creates its own SupplierController. */
    public SupplierService() {
        this.supplierSystem = new SupplierController();
    }

    public SupplierService(SupplierController supplierSystem) {
        if (supplierSystem == null)
            throw new IllegalArgumentException("supplierSystem cannot be null");
        this.supplierSystem = supplierSystem;
    }

    // ── ORDER PROPOSALS ──────────────────────────────────────

    /** Create an order proposal; returns DTO. */
    public OrderProposalDTO createOrderProposal(int productSpecId, int requiredQty) {
        OrderProposal proposal = supplierSystem.createOrderProposal(productSpecId, requiredQty);
        return toOrderProposalDTO(proposal);
    }

    /** Look up a proposal by ID; returns DTO. */
    public OrderProposalDTO getOrderProposal(int id) {
        OrderProposal proposal = supplierSystem.getOrderProposal(id);
        return proposal != null ? toOrderProposalDTO(proposal) : null;
    }

    // ── ORDERS ───────────────────────────────────────────────

    /** Create an order from a proposal; returns DTO. */
    public OrderSummaryDTO createOrder(int proposalId) {
        OrderProposal proposal = supplierSystem.getOrderProposal(proposalId);
        Order order = Order.create(proposal, proposal.getPrice());
        Order registered = supplierSystem.registerAndTransmitOrder(order);
        return toOrderSummaryDTO(registered);
    }

    /** Create a periodic order from DTO items; returns DTO. */
    public OrderSummaryDTO createPeriodicOrder(int supplierId, List<OrderItemDTO> itemDtos, LocalDate deliveryDate) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO dto : itemDtos) {
            items.add(new OrderItem(dto.productSpecId(), dto.quantity(), dto.unitPrice()));
        }
        Order order = Order.createPeriodic(supplierId, items, deliveryDate);
        Order registered = supplierSystem.registerAndTransmitOrder(order);
        return toOrderSummaryDTO(registered);
    }

    // ── SUPPLIERS ────────────────────────────────────────────

    /** Get all suppliers; returns DTOs. */
    public List<SupplierScheduleDTO> getSuppliers() {
        return supplierSystem.getSuppliers().stream()
                .map(this::toSupplierScheduleDTO)
                .collect(Collectors.toList());
    }

    /** Add a supplier; returns supplier ID. */
    public int addSupplier(int id, String name) {
        Supplier s = supplierSystem.addSupplier(id, name);
        return s.getSupplierID();
    }

    // ── AGREEMENTS & SCHEDULES ───────────────────────────────

    public void addAgreement(SupplyAgreement a) { supplierSystem.addAgreement(a); }

    public void addSchedule(DeliverySchedule d) { supplierSystem.addSchedule(d); }

    public List<DayOfWeek> getDeliveryDays(int supplierId) {
        return supplierSystem.getFixedDeliveryDays(supplierId);
    }

    /** Get agreements for a supplier; returns DTOs. */
    public List<SupplyAgreementDTO> getAgreementsForSupplier(int supplierId) {
        return supplierSystem.getAgreementsForSupplier(supplierId).stream()
                .map(this::toSupplyAgreementDTO)
                .collect(Collectors.toList());
    }

    /** Get suppliers that have at least one delivery schedule; returns DTOs. */
    public List<SupplierScheduleDTO> getSuppliersWithSchedules() {
        return supplierSystem.getSuppliersWithSchedules().stream()
                .map(this::toSupplierScheduleDTO)
                .collect(Collectors.toList());
    }

    // ── DTO CONVERSION HELPERS ───────────────────────────────

    private OrderProposalDTO toOrderProposalDTO(OrderProposal p) {
        return new OrderProposalDTO(
            p.getOrderProposalId(),
            p.getSupplierId(),
            p.getProductSpecId(),
            p.getRequiredQty(),
            p.getPrice()
        );
    }

    private OrderSummaryDTO toOrderSummaryDTO(Order o) {
        return new OrderSummaryDTO(
            o.getOrderId(),
            o.getSupplierId(),
            o.getItems().size(),
            o.getTotalPrice(),
            o.getOrderType().name(),
            o.getExpectedDeliveryDate().toString()
        );
    }

    private SupplierScheduleDTO toSupplierScheduleDTO(Supplier s) {
        List<String> days = supplierSystem.getFixedDeliveryDays(s.getSupplierID()).stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return new SupplierScheduleDTO(s.getSupplierID(), s.getSupplierName(), days);
    }

    private SupplyAgreementDTO toSupplyAgreementDTO(SupplyAgreement a) {
        return new SupplyAgreementDTO(
            a.getProductSpecId(),
            a.getSupplier().getSupplierID(),
            a.getMinQuantity(),
            a.getUnitPrice()
        );
    }
}
