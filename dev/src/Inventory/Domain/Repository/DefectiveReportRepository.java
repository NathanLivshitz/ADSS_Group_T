package Inventory.Domain.Repository;

import Inventory.Data.DAO.IDefectiveReportDAO;
import Inventory.DTO.DefectiveReportDTO;
import Inventory.Domain.DefectiveReport;
import java.time.LocalDate;
import java.util.*;

public class DefectiveReportRepository implements IDefectiveReportRepository {

    private final List<DefectiveReport> reports = new ArrayList<>();
    private final IDefectiveReportDAO dao;

    public DefectiveReportRepository(IDefectiveReportDAO dao) {
        this.dao = dao;
    }

    @Override
    public void add(DefectiveReport report) {
        reports.add(report);
        dao.insert(toDTO(report));
    }

    @Override
    public List<DefectiveReport> findAll() {
        return Collections.unmodifiableList(reports);
    }

    @Override
    public List<DefectiveReport> findByDateRange(LocalDate from, LocalDate to) {
        List<DefectiveReport> result = new ArrayList<>();
        for (DefectiveReport r : reports) {
            if (!r.getReportDate().isBefore(from) && !r.getReportDate().isAfter(to))
                result.add(r);
        }
        return result;
    }

    @Override
    public void clear() {
        reports.clear();
    }

    private DefectiveReportDTO toDTO(DefectiveReport r) {
        return new DefectiveReportDTO(r.getProductId(), r.getQuantity(),
                r.getReason(), r.getReportDate().toString());
    }
}
