package Inventory.Data.DAO;

import Inventory.Data.DTO.StockItemDTO;
import java.util.List;

public interface IStockItemDAO {
    void insert(StockItemDTO dto);
    List<StockItemDTO> findBySpecId(int specId);
    List<StockItemDTO> findAll();
    void updateQuantity(int specId, String area, int shelf, int row, int newQty);
}
