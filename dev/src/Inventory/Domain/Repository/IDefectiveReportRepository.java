package Inventory.Domain.Repository;

import Inventory.Domain.DefectiveReport;
import java.time.LocalDate;
import java.util.List;

public interface IDefectiveReportRepository {

    // Persists a new defective/expired report.
    void add(DefectiveReport report);

    // Returns all defective reports.
    List<DefectiveReport> findAll();

    // Returns reports whose reportDate falls within [from, to] inclusive.
    List<DefectiveReport> findByDateRange(LocalDate from, LocalDate to);

    // Clears all defective reports.
    void clear();
}
