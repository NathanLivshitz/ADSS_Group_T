package Inventory.Domain.Repository;

import Inventory.Data.DAO.IStockItemDAO;
import Inventory.DTO.StockItemDTO;
import Inventory.Domain.Area;
import Inventory.Domain.ProductSpec;
import Inventory.Domain.StockItem;
import java.time.LocalDate;
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
    public void updateQuantity(StockItem item) {
        dao.updateQuantity(item.getSpec().getSpecId(), item.getArea().name(),
                item.getShelfNumber(), item.getRowNumber(), item.getQuantity());
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

    // Rebuilds in-memory StockItem objects from the stock_items table.
    // specRepo must be fully hydrated first; same object references are reused
    // so that findBySpec() identity (==) comparisons continue to work.
    // productIds are transient; each StockItem starts with an empty list.
    public void hydrate(IProductSpecRepository specRepo) {
        List<StockItemDTO> dtos = dao.findAll();
        for (StockItemDTO dto : dtos) {
            ProductSpec spec = specRepo.findById(dto.specId());
            if (spec == null)
                throw new IllegalStateException(
                    "Cannot hydrate StockItem: spec " + dto.specId() + " not found");
            Area area = Area.valueOf(dto.area());
            LocalDate expiry = (dto.expiryDate() != null && !dto.expiryDate().isEmpty())
                ? LocalDate.parse(dto.expiryDate()) : null;
            StockItem item = new StockItem(spec, area, dto.shelf(), dto.row(),
                dto.quantity(), expiry);
            stockItems.add(item);
        }
    }

    private StockItemDTO toDTO(StockItem si) {
        String expiry = si.getExpiryDate() != null ? si.getExpiryDate().toString() : null;
        return new StockItemDTO(si.getSpec().getSpecId(), si.getArea().name(),
                si.getShelfNumber(), si.getRowNumber(), si.getQuantity(), expiry);
    }
}
