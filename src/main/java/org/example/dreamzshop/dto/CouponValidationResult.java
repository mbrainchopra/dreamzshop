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
public class CouponValidationResult {

    private boolean valid;

    private String message;

    private String couponCode;

    private BigDecimal discountAmount;

    private BigDecimal amountAfterDiscount;

    public static CouponValidationResult success(
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal amountAfterDiscount
    ) {

        return CouponValidationResult.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .couponCode(couponCode)
                .discountAmount(discountAmount)
                .amountAfterDiscount(amountAfterDiscount)
                .build();
    }

    public static CouponValidationResult failure(
            String message
    ) {

        return CouponValidationResult.builder()
                .valid(false)
                .message(message)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}