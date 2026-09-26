package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Brand;
import org.example.dreamzshop.entity.Category;
import org.example.dreamzshop.entity.Offer;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.enums.DiscountType;
import org.example.dreamzshop.repository.BrandRepository;
import org.example.dreamzshop.repository.CategoryRepository;
import org.example.dreamzshop.repository.OfferRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.service.OfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;


    // =========================================================
    // GET ALL OFFERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Offer> getAllOffers(
            Pageable pageable
    ) {

        return offerRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }


    // =========================================================
    // GET OFFER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Offer getOffer(
            Long id
    ) {

        return offerRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Offer not found"
                        )
                );
    }


    // =========================================================
    // GET ALL CURRENTLY ACTIVE OFFERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Offer> getCurrentlyActiveOffers() {

        return offerRepository
                .findCurrentlyActiveOffers(
                        LocalDateTime.now()
                );
    }


    // =========================================================
    // SAVE OFFER
    // =========================================================

    @Override
    public Offer saveOffer(
            Offer offer
    ) {

        validateOffer(offer);

        normalizeOffer(offer);

        loadTargetEntities(offer);

        return offerRepository.save(offer);
    }


    // =========================================================
    // UPDATE OFFER
    // =========================================================

    @Override
    public Offer updateOffer(
            Long id,
            Offer offer
    ) {

        Offer existing =
                offerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Offer not found"
                                )
                        );

        validateOffer(offer);

        normalizeOffer(offer);

        loadTargetEntities(offer);

        existing.setName(
                offer.getName()
        );

        existing.setDescription(
                offer.getDescription()
        );

        existing.setDiscountType(
                offer.getDiscountType()
        );

        existing.setDiscountValue(
                offer.getDiscountValue()
        );

        existing.setMinimumOrderAmount(
                offer.getMinimumOrderAmount()
        );

        existing.setMaximumDiscountAmount(
                offer.getMaximumDiscountAmount()
        );

        existing.setProduct(
                offer.getProduct()
        );

        existing.setCategory(
                offer.getCategory()
        );

        existing.setBrand(
                offer.getBrand()
        );

        existing.setStartDate(
                offer.getStartDate()
        );

        existing.setEndDate(
                offer.getEndDate()
        );

        existing.setEnabled(
                offer.isEnabled()
        );

        return offerRepository.save(
                existing
        );
    }


    // =========================================================
    // DELETE OFFER
    // =========================================================

    @Override
    public void deleteOffer(
            Long id
    ) {

        if (!offerRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Offer not found"
            );
        }

        offerRepository.deleteById(id);
    }


    // =========================================================
    // TOGGLE OFFER
    // =========================================================

    @Override
    public void toggleOffer(
            Long id
    ) {

        Offer offer =
                offerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Offer not found"
                                )
                        );

        offer.setEnabled(
                !offer.isEnabled()
        );

        offerRepository.save(
                offer
        );
    }


    // =========================================================
    // ELIGIBLE OFFERS FOR PRODUCT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Offer> getEligibleOffers(
            Product product
    ) {

        if (product == null
                || product.getId() == null) {

            return List.of();
        }

        Long productId =
                product.getId();

        Long categoryId =
                product.getCategory() != null
                        ? product.getCategory().getId()
                        : null;

        Long brandId =
                product.getBrand() != null
                        ? product.getBrand().getId()
                        : null;

        return offerRepository
                .findEligibleOffers(
                        productId,
                        categoryId,
                        brandId,
                        LocalDateTime.now()
                );
    }


    // =========================================================
    // BEST OFFER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Offer getBestOffer(
            Product product
    ) {
        if (product == null || product.getSellingPrice() == null) {
            return null;
        }
        return getBestOffer(product, product.getSellingPrice(), product.getSellingPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public Offer getBestOffer(
            Product product,
            BigDecimal orderAmount,
            BigDecimal itemAmount
    ) {

        if (product == null || orderAmount == null || itemAmount == null
                || orderAmount.compareTo(BigDecimal.ZERO) <= 0
                || itemAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        List<Offer> offers = getEligibleOffers(product);
        Offer bestOffer = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (Offer offer : offers) {
            BigDecimal minimum = offer.getMinimumOrderAmount() == null
                    ? BigDecimal.ZERO
                    : offer.getMinimumOrderAmount();

            if (orderAmount.compareTo(minimum) < 0) {
                continue;
            }

            BigDecimal discount = calculateDiscount(
                    offer,
                    itemAmount
            );

            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestOffer = offer;
            }
        }

        return bestOffer;
    }


    // =========================================================
    // CALCULATE DISCOUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(
            Offer offer,
            BigDecimal amount
    ) {

        if (offer == null
                || amount == null
                || amount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        if (!isValidOffer(offer)) {

            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        if (offer.getDiscountType()
                == DiscountType.PERCENTAGE) {

            discount =
                    amount
                            .multiply(
                                    offer.getDiscountValue()
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            discount =
                    offer.getDiscountValue();
        }

        if (offer.getMaximumDiscountAmount()
                != null) {

            discount =
                    discount.min(
                            offer.getMaximumDiscountAmount()
                    );
        }

        discount =
                discount.min(amount);

        return discount.max(
                BigDecimal.ZERO
        );
    }


    // =========================================================
    // GET DISCOUNTED PRODUCT PRICE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDiscountedPrice(
            Product product
    ) {

        if (product == null
                || product.getSellingPrice() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal price =
                product.getSellingPrice();

        Offer offer =
                getBestOffer(product);

        if (offer == null) {

            return price;
        }

        BigDecimal discount =
                calculateDiscount(
                        offer,
                        price
                );

        return price
                .subtract(discount)
                .max(BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // VALIDATE OFFER
    // =========================================================

    private void validateOffer(
            Offer offer
    ) {

        if (offer == null) {

            throw new IllegalArgumentException(
                    "Offer is required"
            );
        }

        if (offer.getStartDate() == null
                || offer.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }

        if (!offer.getEndDate()
                .isAfter(
                        offer.getStartDate()
                )) {

            throw new IllegalArgumentException(
                    "End date must be after start date"
            );
        }

        if (offer.getDiscountValue() == null
                || offer.getDiscountValue()
                .compareTo(BigDecimal.ZERO)
                <= 0) {

            throw new IllegalArgumentException(
                    "Discount value must be greater than zero"
            );
        }

        if (offer.getDiscountType()
                == DiscountType.PERCENTAGE
                && offer.getDiscountValue()
                .compareTo(
                        BigDecimal.valueOf(100)
                ) > 0) {

            throw new IllegalArgumentException(
                    "Percentage discount cannot exceed 100%"
            );
        }

        if (offer.getMinimumOrderAmount() != null
                && offer.getMinimumOrderAmount()
                .compareTo(BigDecimal.ZERO)
                < 0) {

            throw new IllegalArgumentException(
                    "Minimum order amount cannot be negative"
            );
        }

        if (offer.getMaximumDiscountAmount() != null
                && offer.getMaximumDiscountAmount()
                .compareTo(BigDecimal.ZERO)
                < 0) {

            throw new IllegalArgumentException(
                    "Maximum discount cannot be negative"
            );
        }
    }


    // =========================================================
    // LOAD TARGET ENTITIES
    // =========================================================

    private void loadTargetEntities(
            Offer offer
    ) {

        if (offer.getProduct() != null) {

            if (offer.getProduct().getId() == null) {

                throw new IllegalArgumentException(
                        "Invalid product"
                );
            }

            Product product =
                    productRepository
                            .findById(
                                    offer.getProduct().getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected product not found"
                                    )
                            );

            offer.setProduct(
                    product
            );
        }

        if (offer.getCategory() != null) {

            if (offer.getCategory().getId() == null) {

                throw new IllegalArgumentException(
                        "Invalid category"
                );
            }

            Category category =
                    categoryRepository
                            .findById(
                                    offer.getCategory().getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected category not found"
                                    )
                            );

            offer.setCategory(
                    category
            );
        }

        if (offer.getBrand() != null) {

            if (offer.getBrand().getId() == null) {

                throw new IllegalArgumentException(
                        "Invalid brand"
                );
            }

            Brand brand =
                    brandRepository
                            .findById(
                                    offer.getBrand().getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected brand not found"
                                    )
                            );

            offer.setBrand(
                    brand
            );
        }
    }


    // =========================================================
    // NORMALIZE OFFER
    // =========================================================

    private void normalizeOffer(
            Offer offer
    ) {

        if (offer.getName() != null) {

            offer.setName(
                    offer.getName().trim()
            );
        }

        if (offer.getMinimumOrderAmount() == null) {

            offer.setMinimumOrderAmount(
                    BigDecimal.ZERO
            );
        }

        if (offer.getDiscountValue() != null) {

            offer.setDiscountValue(
                    offer.getDiscountValue()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }

        offer.setMinimumOrderAmount(
                offer.getMinimumOrderAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        if (offer.getMaximumDiscountAmount()
                != null) {

            offer.setMaximumDiscountAmount(
                    offer.getMaximumDiscountAmount()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }
    }


    // =========================================================
    // CURRENTLY VALID OFFER
    // =========================================================

    private boolean isValidOffer(
            Offer offer
    ) {

        if (offer == null
                || !offer.isEnabled()
                || offer.getStartDate() == null
                || offer.getEndDate() == null) {

            return false;
        }

        LocalDateTime now =
                LocalDateTime.now();

        return !now.isBefore(
                offer.getStartDate()
        )
                && !now.isAfter(
                offer.getEndDate()
        );
    }
}