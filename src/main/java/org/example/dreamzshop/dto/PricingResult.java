package org.example.dreamzshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingResult {

    private BigDecimal originalSubtotal;

    private BigDecimal offerDiscount;

    private BigDecimal subtotalAfterOffers;

    private BigDecimal couponDiscount;

    private BigDecimal subtotalAfterCoupon;

    private BigDecimal taxAmount;

    private BigDecimal deliveryCharge;

    private BigDecimal grandTotal;

    private String couponCode;

    public BigDecimal getTotalDiscount() {

        BigDecimal offer =
                offerDiscount != null
                        ? offerDiscount
                        : BigDecimal.ZERO;

        BigDecimal coupon =
                couponDiscount != null
                        ? couponDiscount
                        : BigDecimal.ZERO;

        return offer.add(coupon);
    }
}