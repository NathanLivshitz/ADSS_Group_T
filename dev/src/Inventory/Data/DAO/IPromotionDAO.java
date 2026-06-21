package Inventory.Data.DAO;

import Inventory.DTO.PromotionDTO;
import java.util.List;

public interface IPromotionDAO {
    void insert(PromotionDTO dto);
    List<PromotionDTO> findAll();
}
