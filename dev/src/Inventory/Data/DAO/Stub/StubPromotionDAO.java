package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IPromotionDAO;
import Inventory.DTO.PromotionDTO;
import java.util.*;

public class StubPromotionDAO implements IPromotionDAO {
    @Override public void insert(PromotionDTO dto) {}
    @Override public List<PromotionDTO> findAll() { return Collections.emptyList(); }
}
