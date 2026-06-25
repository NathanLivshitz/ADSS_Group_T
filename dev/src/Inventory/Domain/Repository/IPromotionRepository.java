package Inventory.Domain.Repository;

import Inventory.Domain.Promotion;
import java.util.List;

public interface IPromotionRepository {

    // Persists a new promotion.
    void add(Promotion promotion);

    // Returns all promotions (active and inactive).
    // Filtering by isActive() is done in the controller, not here.
    List<Promotion> findAll();

    // Clears all promotions.
    void clear();
}
