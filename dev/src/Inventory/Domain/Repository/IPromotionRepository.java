package Inventory.Domain.Repository;

import Inventory.Domain.Promotion;
import java.util.List;

public interface IPromotionRepository {

    /**
     * Persists a new promotion.
     * Called by InventoryController.addPromotion().
     */
    void add(Promotion promotion);

    /**
     * Returns all promotions (active and inactive).
     * Filtering by isActive() is done in the controller, not here.
     * Called by InventoryController.getActivePromotions() and getEffectivePrice().
     */
    List<Promotion> findAll();

    /**
     * Clears all promotions.
     * Called by InventoryController.reset().
     */
    void clear();
}
