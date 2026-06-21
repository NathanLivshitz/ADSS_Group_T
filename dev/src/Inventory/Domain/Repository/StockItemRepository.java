package Inventory.Domain.Repository;

import Inventory.Data.DAO.IStockItemDAO;
import Inventory.Data.DTO.StockItemDTO;
import Inventory.Domain.ProductSpec;
import Inventory.Domain.StockItem;
import java.util.*;

public class StockItemRepository implements IStockItemRepository {

    private final List<StockItem> stockItems = new ArrayList<>();
    private final IStockItemDAO dao;

    public StockItemRepository(IStockItemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void add(StockItem item) {
        stockItems.add(item);
        dao.insert(toDTO(item));
    }

    @Override
    public List<StockItem> findBySpec(ProductSpec spec) {
        List<StockItem> result = new ArrayList<>();
        for (StockItem si : stockItems) {
            if (si.getSpec() == spec) result.add(si);
        }
        return result;
    }

    @Override
    public List<StockItem> findAll() {
        return Collections.unmodifiableList(stockItems);
    }

    @Override
    public void clear() {
        stockItems.clear();
    }

    private StockItemDTO toDTO(StockItem si) {
        String expiry = si.getExpiryDate() != null ? si.getExpiryDate().toString() : null;
        return new StockItemDTO(si.getSpec().getSpecId(), si.getArea().name(),
                si.getShelfNumber(), si.getRowNumber(), si.getQuantity(), expiry);
    }
}
