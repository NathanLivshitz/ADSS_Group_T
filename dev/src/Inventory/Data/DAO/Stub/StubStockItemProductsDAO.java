package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IStockItemProductsDAO;
import java.util.*;

public class StubStockItemProductsDAO implements IStockItemProductsDAO {
    @Override public void insert(int specId, String area, int shelf, int row, int productId) {}
    @Override public List<Integer> findByLocation(int specId, String area, int shelf, int row) { return Collections.emptyList(); }
    @Override public void deleteByLocation(int specId, String area, int shelf, int row) {}
}
