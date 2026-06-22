package Inventory.Domain.Repository;

import Inventory.Domain.DefectiveReport;
import java.time.LocalDate;
import java.util.List;

public interface IDefectiveReportRepository {

    /**
     * Persists a new defective/expired report.
     * Called by InventoryController.reportDefective() and removeExpiredStock().
     */
    void add(DefectiveReport report);

    /**
     * Returns all defective reports.
     * Called by InventoryController.getDefectiveItemsWithLocations().
     */
    List<DefectiveReport> findAll();

    /**
     * Returns reports whose reportDate falls within [from, to] inclusive.
     * Date filtering happens here - DAO will mirror this with a WHERE clause.
     * Called by InventoryController.getDefectiveReports(from, to).
     */
    List<DefectiveReport> findByDateRange(LocalDate from, LocalDate to);

    /**
     * Clears all defective reports.
     * Called by InventoryController.reset().
     */
    void clear();
}
