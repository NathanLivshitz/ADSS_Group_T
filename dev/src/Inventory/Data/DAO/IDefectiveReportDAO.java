package Inventory.Data.DAO;

import Inventory.Data.DTO.DefectiveReportDTO;
import java.util.List;

public interface IDefectiveReportDAO {
    void insert(DefectiveReportDTO dto);
    List<DefectiveReportDTO> findAll();
}
