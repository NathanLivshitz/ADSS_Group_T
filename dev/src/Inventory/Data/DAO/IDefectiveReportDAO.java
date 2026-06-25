package Inventory.Data.DAO;

import Inventory.DTO.DefectiveReportDTO;
import java.util.List;

public interface IDefectiveReportDAO {
    void insert(DefectiveReportDTO dto);
    List<DefectiveReportDTO> findAll();
    void deleteAll();
}
