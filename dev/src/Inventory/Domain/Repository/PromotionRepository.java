package Inventory.Domain.Repository;

import Inventory.Data.DAO.IPromotionDAO;
import Inventory.DTO.PromotionDTO;
import Inventory.Domain.Category;
import Inventory.Domain.ProductSpec;
import Inventory.Domain.Promotion;
import java.time.LocalDate;
import java.util.*;

public class PromotionRepository implements IPromotionRepository {

    private final List<Promotion> promotions = new ArrayList<>();
    private final IPromotionDAO dao;

    public PromotionRepository(IPromotionDAO dao) {
        this.dao = dao;
    }

    @Override
    public void add(Promotion promotion) {
        promotions.add(promotion);
        dao.insert(toDTO(promotion));
    }

    @Override
    public List<Promotion> findAll() {
        return Collections.unmodifiableList(promotions);
    }

    @Override
    public void clear() {
        promotions.clear();
        dao.deleteAll();
    }

    // specRepo and categoryRepo must be hydrated first
    public void hydrate(IProductSpecRepository specRepo, ICategoryRepository categoryRepo) {
        List<PromotionDTO> dtos = dao.findAll();
        for (PromotionDTO dto : dtos) {
            ProductSpec targetSpec = (dto.targetSpecId() != 0)
                ? specRepo.findById(dto.targetSpecId()) : null;
            Category targetCat = (dto.targetCategoryId() != 0)
                ? categoryRepo.findById(dto.targetCategoryId()) : null;
            Promotion promo = new Promotion(
                dto.discountPercent(),
                LocalDate.parse(dto.startDate()),
                LocalDate.parse(dto.endDate()),
                targetSpec,
                targetCat
            );
            promotions.add(promo);
        }
    }

    private PromotionDTO toDTO(Promotion p) {
        int specId = p.getTargetProduct() != null ? p.getTargetProduct().getSpecId() : 0;
        int catId  = p.getTargetCategory() != null ? p.getTargetCategory().getCategoryId() : 0;
        String prodName = p.getTargetProduct() != null ? p.getTargetProduct().getName() : null;
        String catName  = p.getTargetCategory() != null ? p.getTargetCategory().getName() : null;
        return new PromotionDTO(p.getDiscountPercent(),
                p.getStartDate().toString(), p.getEndDate().toString(),
                specId, catId, prodName, catName);
    }
}
