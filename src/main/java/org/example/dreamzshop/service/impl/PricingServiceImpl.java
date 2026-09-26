package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.dto.CouponValidationResult;
import org.example.dreamzshop.dto.PricingResult;
import org.example.dreamzshop.entity.Cart;
import org.example.dreamzshop.entity.CartItem;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.service.CouponService;
import org.example.dreamzshop.service.OfferService;
import org.example.dreamzshop.service.PricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class PricingServiceImpl
        implements PricingService {

    private final OfferService offerService;

    private final CouponService couponService;

    public PricingServiceImpl(
            OfferService offerService,
            CouponService couponService
    ) {

        this.offerService =
                offerService;

        this.couponService =
                couponService;
    }

    @Override
    public PricingResult calculateCartPricing(
            Cart cart,
            String couponCode
    ) {

        if (cart == null
                || cart.getItems() == null
                || cart.getItems().isEmpty()) {

            return emptyPricing();
        }

        BigDecimal originalSubtotal =
                BigDecimal.ZERO;

        BigDecimal offerDiscount =
                BigDecimal.ZERO;

        BigDecimal taxableAmount =
                BigDecimal.ZERO;

        /*
         * Use the cart item's price snapshot as the source of truth.
         * Product.sellingPrice may have changed since the item was added.
         */
        for (CartItem item : cart.getItems()) {
            if (item == null
                    || item.getProduct() == null
                    || item.getUnitPrice() == null
                    || item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                continue;
            }

            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            originalSubtotal = originalSubtotal.add(unitPrice.multiply(quantity));
        }

        originalSubtotal = money(originalSubtotal);

        /*
         * Apply product/category/brand/global offers only when the
         * cart subtotal satisfies the offer's minimum order amount.
         */
        for (CartItem item : cart.getItems()) {
            if (item == null
                    || item.getProduct() == null
                    || item.getUnitPrice() == null
                    || item.getQuantity() == null
                    || item.getQuantity() <= 0) {
                continue;
            }

            Product product = item.getProduct();
            BigDecimal itemAmount = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            BigDecimal itemOfferDiscount =
                    calculateProductOfferDiscount(product, itemAmount, originalSubtotal);

            offerDiscount = offerDiscount.add(itemOfferDiscount);

            BigDecimal itemAfterOffer = itemAmount
                    .subtract(itemOfferDiscount)
                    .max(BigDecimal.ZERO);

            if (product.isTaxable()
                    && product.getTaxPercentage() != null
                    && product.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
                taxableAmount = taxableAmount.add(itemAfterOffer);
            }
        }

        originalSubtotal =
                money(originalSubtotal);

        offerDiscount =
                money(offerDiscount);

        BigDecimal subtotalAfterOffers =
                originalSubtotal
                        .subtract(offerDiscount)
                        .max(BigDecimal.ZERO);

        subtotalAfterOffers =
                money(subtotalAfterOffers);

        /*
         * Coupon is applied AFTER product offers.
         *
         * This gives the customer:
         *
         * Product Offer
         *       ↓
         * Coupon
         */
        BigDecimal couponDiscount =
                BigDecimal.ZERO;

        String appliedCouponCode =
                null;

        BigDecimal subtotalAfterCoupon =
                subtotalAfterOffers;

        if (couponCode != null
                && !couponCode.trim().isEmpty()) {

            CouponValidationResult validation =
                    couponService.validateCoupon(
                            cart.getUser().getEmail(),
                            couponCode,
                            subtotalAfterOffers
                    );

            if (!validation.isValid()) {

                throw new IllegalArgumentException(
                        validation.getMessage()
                );
            }

            couponDiscount =
                    validation.getDiscountAmount();

            appliedCouponCode =
                    validation.getCouponCode();

            subtotalAfterCoupon =
                    subtotalAfterOffers
                            .subtract(
                                    couponDiscount
                            )
                            .max(
                                    BigDecimal.ZERO
                            );
        }

        couponDiscount =
                money(couponDiscount);

        subtotalAfterCoupon =
                money(subtotalAfterCoupon);

        /*
         * Recalculate tax based on the amount after
         * product offers and coupon.
         *
         * The previous taxable amount is used as a
         * reference for distributing coupon reduction.
         */
        BigDecimal taxAmount =
                calculateTaxAfterCoupon(
                        cart,
                        couponDiscount,
                        subtotalAfterOffers,
                        originalSubtotal
                );

        /*
         * Currently free delivery.
         *
         * Delivery charges can be added later.
         */
        BigDecimal deliveryCharge =
                BigDecimal.ZERO;

        BigDecimal grandTotal =
                subtotalAfterCoupon
                        .add(taxAmount)
                        .add(deliveryCharge);

        grandTotal =
                money(grandTotal);

        return PricingResult.builder()

                .originalSubtotal(
                        originalSubtotal
                )

                .offerDiscount(
                        offerDiscount
                )

                .subtotalAfterOffers(
                        subtotalAfterOffers
                )

                .couponDiscount(
                        couponDiscount
                )

                .subtotalAfterCoupon(
                        subtotalAfterCoupon
                )

                .taxAmount(
                        taxAmount
                )

                .deliveryCharge(
                        deliveryCharge
                )

                .grandTotal(
                        grandTotal
                )

                .couponCode(
                        appliedCouponCode
                )

                .build();
    }

    private BigDecimal calculateProductOfferDiscount(
            Product product,
            BigDecimal itemAmount,
            BigDecimal orderAmount
    ) {

        if (product == null
                || itemAmount == null
                || itemAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        var offer =
                offerService.getBestOffer(
                        product,
                        orderAmount,
                        itemAmount
                );

        if (offer == null) {
            return BigDecimal.ZERO;
        }

        /*
         * For product offers, minimum order condition
         * is checked against the applicable item amount.
         */
        return offerService.calculateDiscount(
                offer,
                itemAmount
        );
    }

    private BigDecimal calculateTaxableAmount(
            Product product,
            BigDecimal amountAfterOffer
    ) {

        if (product == null
                || !product.isTaxable()
                || product.getTaxPercentage() == null
                || product.getTaxPercentage()
                .compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        return amountAfterOffer;
    }

    private BigDecimal calculateTaxAfterCoupon(
            Cart cart,
            BigDecimal couponDiscount,
            BigDecimal subtotalAfterOffers,
            BigDecimal originalSubtotal
    ) {

        if (cart == null
                || cart.getItems() == null
                || cart.getItems().isEmpty()) {

            return BigDecimal.ZERO;
        }

        if (subtotalAfterOffers == null
                || subtotalAfterOffers.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        /*
         * Calculate tax on the effective discounted
         * item amounts.
         *
         * Coupon discount is distributed proportionally
         * across taxable items.
         */
        BigDecimal totalTax =
                BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            Product product =
                    item.getProduct();

            if (product == null
                    || !product.isTaxable()
                    || product.getTaxPercentage() == null
                    || product.getTaxPercentage()
                    .compareTo(BigDecimal.ZERO) <= 0
                    || item.getQuantity() == null
                    || item.getQuantity() <= 0
                    || item.getUnitPrice() == null) {

                continue;
            }

            BigDecimal itemAmount =
                    item.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

            BigDecimal itemOfferDiscount =
                    calculateProductOfferDiscount(
                            product,
                            itemAmount,
                            originalSubtotal
                    );

            BigDecimal itemAfterOffer =
                    itemAmount
                            .subtract(
                                    itemOfferDiscount
                            )
                            .max(
                                    BigDecimal.ZERO
                            );

            /*
             * Allocate coupon discount proportionally.
             */
            BigDecimal couponShare =
                    BigDecimal.ZERO;

            if (couponDiscount != null
                    && couponDiscount.compareTo(
                    BigDecimal.ZERO
            ) > 0
                    && subtotalAfterOffers.compareTo(
                    BigDecimal.ZERO
            ) > 0) {

                couponShare =
                        couponDiscount
                                .multiply(
                                        itemAfterOffer
                                )
                                .divide(
                                        subtotalAfterOffers,
                                        8,
                                        RoundingMode.HALF_UP
                                );
            }

            BigDecimal taxableItemAmount =
                    itemAfterOffer
                            .subtract(
                                    couponShare
                            )
                            .max(
                                    BigDecimal.ZERO
                            );

            BigDecimal itemTax =
                    taxableItemAmount
                            .multiply(
                                    product.getTaxPercentage()
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalTax =
                    totalTax.add(
                            itemTax
                    );
        }

        return money(totalTax);
    }

    private PricingResult emptyPricing() {

        return PricingResult.builder()

                .originalSubtotal(
                        BigDecimal.ZERO
                )

                .offerDiscount(
                        BigDecimal.ZERO
                )

                .subtotalAfterOffers(
                        BigDecimal.ZERO
                )

                .couponDiscount(
                        BigDecimal.ZERO
                )

                .subtotalAfterCoupon(
                        BigDecimal.ZERO
                )

                .taxAmount(
                        BigDecimal.ZERO
                )

                .deliveryCharge(
                        BigDecimal.ZERO
                )

                .grandTotal(
                        BigDecimal.ZERO
                )

                .build();
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        if (value == null) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}