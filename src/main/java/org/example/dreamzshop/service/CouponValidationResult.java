package org.example.dreamzshop.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResult {

    private boolean valid;

    private String message;

    private String couponCode;

    private BigDecimal discountAmount;

    private BigDecimal amountAfterDiscount;
}