package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IStockItemDAO;
import Inventory.DTO.StockItemDTO;
import java.util.*;

public class StubStockItemDAO implements IStockItemDAO {
    @Override public void insert(StockItemDTO dto) {}
    @Override public List<StockItemDTO> findBySpecId(int specId) { return Collections.emptyList(); }
    @Override public List<StockItemDTO> findAll() { return Collections.emptyList(); }
    @Override public void updateQuantity(int specId, String area, int shelf, int row, int newQty) {}
}
