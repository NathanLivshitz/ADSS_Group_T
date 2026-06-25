package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IDefectiveReportDAO;
import Inventory.DTO.DefectiveReportDTO;
import java.util.*;

public class StubDefectiveReportDAO implements IDefectiveReportDAO {
    @Override public void insert(DefectiveReportDTO dto) {}
    @Override public List<DefectiveReportDTO> findAll() { return Collections.emptyList(); }
    @Override public void deleteAll() {}
}
