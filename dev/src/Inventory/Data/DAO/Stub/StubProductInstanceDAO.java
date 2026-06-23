package Inventory.Data.DAO.Stub;

import Inventory.Data.DAO.IProductInstanceDAO;
import java.util.*;

public class StubProductInstanceDAO implements IProductInstanceDAO {
    @Override public void insert(int productId, int specId) {}
    @Override public List<Integer> findAllBySpecId(int specId) { return Collections.emptyList(); }
    @Override public List<Integer> findAll() { return Collections.emptyList(); }
}
