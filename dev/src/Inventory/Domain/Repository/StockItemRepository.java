package Inventory.Domain.Repository;

import Inventory.Data.DAO.IProductInstanceDAO;
import Inventory.Data.DAO.IStockItemDAO;
import Inventory.Data.DAO.IStockItemProductsDAO;
import Inventory.DTO.StockItemDTO;
import Inventory.Domain.Area;
import Inventory.Domain.ProductSpec;
import Inventory.Domain.StockItem;
import java.time.LocalDate;
import java.util.*;

public class StockItemRepository implements IStockItemRepository {

    private final List<StockItem> stockItems = new ArrayList<>();
    private final IStockItemDAO dao;
    private final IStockItemProductsDAO productsDao;

    public StockItemRepository(IStockItemDAO dao, IStockItemProductsDAO productsDao) {
        this.dao = dao;
        this.productsDao = productsDao;
    }

    @Override
    public void add(StockItem item) {
        stockItems.add(item);
        dao.insert(toDTO(item));
        // persist product IDs
        String area = item.getArea().name();
        int specId = item.getSpec().getSpecId();
        for (Integer productId : item.getProductIds()) {
            productsDao.insert(specId, area, item.getShelfNumber(), item.getRowNumber(), productId);
        }
    }

    @Override
    public void updateQuantity(StockItem item) {
        dao.updateQuantity(item.getSpec().getSpecId(), item.getArea().name(),
                item.getShelfNumber(), item.getRowNumber(), item.getQuantity());
        // sync product IDs in mapping table
        String area = item.getArea().name();
        int specId = item.getSpec().getSpecId();
        productsDao.deleteByLocation(specId, area, item.getShelfNumber(), item.getRowNumber());
        for (Integer productId : item.getProductIds()) {
            productsDao.insert(specId, area, item.getShelfNumber(), item.getRowNumber(), productId);
        }
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

    // specRepo must be hydrated first; reuses same spec references so findBySpec() (==) works.
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
            // load product IDs from mapping table
            List<Integer> productIds = productsDao.findByLocation(
                dto.specId(), dto.area(), dto.shelf(), dto.row());
            StockItem item = new StockItem(spec, area, dto.shelf(), dto.row(),
                dto.quantity(), expiry, productIds);
            stockItems.add(item);
        }
    }

    private StockItemDTO toDTO(StockItem si) {
        String expiry = si.getExpiryDate() != null ? si.getExpiryDate().toString() : null;
        return new StockItemDTO(si.getSpec().getSpecId(), si.getArea().name(),
                si.getShelfNumber(), si.getRowNumber(), si.getQuantity(), expiry);
    }
}
