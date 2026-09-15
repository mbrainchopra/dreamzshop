package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.dto.CouponValidationResult;
import org.example.dreamzshop.entity.Coupon;
import org.example.dreamzshop.entity.CouponUsage;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.DiscountType;
import org.example.dreamzshop.repository.CouponRepository;
import org.example.dreamzshop.repository.CouponUsageRepository;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
public class CouponServiceImpl
        implements CouponService {

    private final CouponRepository couponRepository;

    private final CouponUsageRepository couponUsageRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    public CouponServiceImpl(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository,
            UserRepository userRepository,
            OrderRepository orderRepository
    ) {

        this.couponRepository =
                couponRepository;

        this.couponUsageRepository =
                couponUsageRepository;

        this.userRepository =
                userRepository;

        this.orderRepository =
                orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getAllCoupons(
            Pageable pageable
    ) {

        return couponRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCoupon(
            Long id
    ) {

        return couponRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Coupon not found"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponByCode(
            String code
    ) {

        if (code == null
                || code.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Coupon code is required"
            );
        }

        return couponRepository
                .findByCodeIgnoreCase(
                        code.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Coupon not found"
                        )
                );
    }

    @Override
    public Coupon saveCoupon(
            Coupon coupon
    ) {

        validateCouponData(
                coupon,
                null
        );

        normalizeCoupon(
                coupon
        );

        return couponRepository.save(
                coupon
        );
    }

    @Override
    public Coupon updateCoupon(
            Long id,
            Coupon coupon
    ) {

        Coupon existing =
                getCoupon(id);

        validateCouponData(
                coupon,
                id
        );

        normalizeCoupon(
                coupon
        );

        existing.setCode(
                coupon.getCode()
        );

        existing.setDescription(
                coupon.getDescription()
        );

        existing.setDiscountType(
                coupon.getDiscountType()
        );

        existing.setDiscountValue(
                coupon.getDiscountValue()
        );

        existing.setMinimumOrderAmount(
                coupon.getMinimumOrderAmount()
        );

        existing.setMaximumDiscountAmount(
                coupon.getMaximumDiscountAmount()
        );

        existing.setUsageLimit(
                coupon.getUsageLimit()
        );

        existing.setPerCustomerLimit(
                coupon.getPerCustomerLimit()
        );

        existing.setFirstOrderOnly(
                coupon.isFirstOrderOnly()
        );

        existing.setStartDate(
                coupon.getStartDate()
        );

        existing.setEndDate(
                coupon.getEndDate()
        );

        existing.setEnabled(
                coupon.isEnabled()
        );

        return couponRepository.save(
                existing
        );
    }

    @Override
    public void deleteCoupon(
            Long id
    ) {

        Coupon coupon =
                getCoupon(id);

        long usageCount =
                couponUsageRepository
                        .countByCouponId(id);

        if (usageCount > 0) {

            throw new IllegalArgumentException(
                    "This coupon has already been used. Disable it instead."
            );
        }

        couponRepository.delete(
                coupon
        );
    }

    @Override
    public void toggleCoupon(
            Long id
    ) {

        Coupon coupon =
                getCoupon(id);

        coupon.setEnabled(
                !coupon.isEnabled()
        );

        couponRepository.save(
                coupon
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResult validateCoupon(
            String email,
            String code,
            BigDecimal orderAmount
    ) {

        if (email == null
                || email.isBlank()) {

            return CouponValidationResult.failure(
                    "Please login to use a coupon"
            );
        }

        if (code == null
                || code.trim().isEmpty()) {

            return CouponValidationResult.failure(
                    "Please enter a coupon code"
            );
        }

        if (orderAmount == null
                || orderAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return CouponValidationResult.failure(
                    "Invalid order amount"
            );
        }

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                )
                        );

        Coupon coupon =
                couponRepository
                        .findByCodeIgnoreCase(
                                code.trim()
                        )
                        .orElse(null);

        if (coupon == null) {

            return CouponValidationResult.failure(
                    "Invalid coupon code"
            );
        }

        if (!coupon.isEnabled()) {

            return CouponValidationResult.failure(
                    "This coupon is currently disabled"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (coupon.getStartDate() == null
                || coupon.getEndDate() == null) {

            return CouponValidationResult.failure(
                    "This coupon has invalid validity dates"
            );
        }

        if (now.isBefore(
                coupon.getStartDate()
        )) {

            return CouponValidationResult.failure(
                    "This coupon is not active yet"
            );
        }

        if (now.isAfter(
                coupon.getEndDate()
        )) {

            return CouponValidationResult.failure(
                    "This coupon has expired"
            );
        }

        if (coupon.hasReachedUsageLimit()) {

            return CouponValidationResult.failure(
                    "This coupon has reached its usage limit"
            );
        }

        if (coupon.getMinimumOrderAmount() != null
                && orderAmount.compareTo(
                coupon.getMinimumOrderAmount()
        ) < 0) {

            return CouponValidationResult.failure(
                    "Minimum order amount for this coupon is ₹"
                            + coupon.getMinimumOrderAmount()
            );
        }

        if (coupon.getPerCustomerLimit() != null
                && coupon.getPerCustomerLimit() > 0) {

            long customerUsage =
                    couponUsageRepository
                            .countByCouponIdAndUserId(
                                    coupon.getId(),
                                    user.getId()
                            );

            if (customerUsage
                    >= coupon.getPerCustomerLimit()) {

                return CouponValidationResult.failure(
                        "You have already used this coupon the maximum number of times"
                );
            }
        }

        if (coupon.isFirstOrderOnly()) {

            boolean hasPreviousOrder =
                    orderRepository
                            .existsByUserId(
                                    user.getId()
                            );

            if (hasPreviousOrder) {

                return CouponValidationResult.failure(
                        "This coupon is valid only for your first order"
                );
            }
        }

        BigDecimal discount =
                calculateDiscount(
                        coupon,
                        orderAmount
                );

        if (discount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return CouponValidationResult.failure(
                    "This coupon cannot be applied to this order"
            );
        }

        BigDecimal afterDiscount =
                orderAmount
                        .subtract(discount)
                        .max(BigDecimal.ZERO)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return CouponValidationResult.success(
                coupon.getCode(),
                discount,
                afterDiscount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal orderAmount
    ) {

        if (coupon == null
                || orderAmount == null
                || orderAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal discount;

        if (coupon.getDiscountType()
                == DiscountType.PERCENTAGE) {

            discount =
                    orderAmount
                            .multiply(
                                    coupon.getDiscountValue()
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            discount =
                    coupon.getDiscountValue();
        }

        if (coupon.getMaximumDiscountAmount()
                != null
                && discount.compareTo(
                coupon.getMaximumDiscountAmount()
        ) > 0) {

            discount =
                    coupon.getMaximumDiscountAmount();
        }

        if (discount.compareTo(orderAmount) > 0) {

            discount = orderAmount;
        }

        return discount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    @Override
    public void recordCouponUsage(
            Coupon coupon,
            User user,
            Order order,
            BigDecimal discountAmount
    ) {

        if (coupon == null
                || user == null
                || order == null) {

            return;
        }

        if (couponUsageRepository
                .existsByCouponIdAndOrderId(
                        coupon.getId(),
                        order.getId()
                )) {

            return;
        }

        CouponUsage usage =
                CouponUsage.builder()
                        .coupon(coupon)
                        .user(user)
                        .order(order)
                        .discountAmount(
                                discountAmount
                                        .setScale(
                                                2,
                                                RoundingMode.HALF_UP
                                        )
                        )
                        .build();

        couponUsageRepository.save(
                usage
        );

        coupon.setUsedCount(
                coupon.getUsedCount() + 1
        );

        couponRepository.save(
                coupon
        );
    }

    private void validateCouponData(
            Coupon coupon,
            Long existingId
    ) {

        if (coupon == null) {

            throw new IllegalArgumentException(
                    "Coupon data is required"
            );
        }

        if (coupon.getCode() == null
                || coupon.getCode()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Coupon code is required"
            );
        }

        String code =
                coupon.getCode()
                        .trim()
                        .toUpperCase();

        if (existingId == null) {

            if (couponRepository
                    .existsByCodeIgnoreCase(code)) {

                throw new IllegalArgumentException(
                        "Coupon code already exists"
                );
            }

        } else {

            Coupon existing =
                    couponRepository
                            .findByCodeIgnoreCase(code)
                            .orElse(null);

            if (existing != null
                    && !existing.getId()
                    .equals(existingId)) {

                throw new IllegalArgumentException(
                        "Coupon code already exists"
                );
            }
        }

        if (coupon.getDiscountType() == null) {

            throw new IllegalArgumentException(
                    "Discount type is required"
            );
        }

        if (coupon.getDiscountValue() == null
                || coupon.getDiscountValue()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Discount value must be greater than zero"
            );
        }

        if (coupon.getDiscountType()
                == DiscountType.PERCENTAGE
                && coupon.getDiscountValue()
                .compareTo(
                        BigDecimal.valueOf(100)
                ) > 0) {

            throw new IllegalArgumentException(
                    "Percentage discount cannot exceed 100%"
            );
        }

        if (coupon.getMinimumOrderAmount() != null
                && coupon.getMinimumOrderAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum order amount cannot be negative"
            );
        }

        if (coupon.getMaximumDiscountAmount() != null
                && coupon.getMaximumDiscountAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Maximum discount cannot be negative"
            );
        }

        if (coupon.getUsageLimit() != null
                && coupon.getUsageLimit() <= 0) {

            throw new IllegalArgumentException(
                    "Usage limit must be greater than zero"
            );
        }

        if (coupon.getPerCustomerLimit() != null
                && coupon.getPerCustomerLimit() <= 0) {

            throw new IllegalArgumentException(
                    "Per customer limit must be greater than zero"
            );
        }

        if (coupon.getStartDate() == null
                || coupon.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "Coupon start and end dates are required"
            );
        }

        if (!coupon.getEndDate()
                .isAfter(
                        coupon.getStartDate()
                )) {

            throw new IllegalArgumentException(
                    "End date must be after start date"
            );
        }

        if (existingId != null
                && coupon.getUsageLimit() != null) {

            Coupon existing =
                    couponRepository
                            .findById(existingId)
                            .orElse(null);

            if (existing != null
                    && coupon.getUsageLimit()
                    < existing.getUsedCount()) {

                throw new IllegalArgumentException(
                        "Usage limit cannot be lower than current usage count"
                );
            }
        }
    }

    private void normalizeCoupon(
            Coupon coupon
    ) {

        coupon.setCode(
                coupon.getCode()
                        .trim()
                        .toUpperCase()
        );

        if (coupon.getDescription() != null) {

            coupon.setDescription(
                    coupon.getDescription()
                            .trim()
            );
        }

        if (coupon.getMinimumOrderAmount()
                == null) {

            coupon.setMinimumOrderAmount(
                    BigDecimal.ZERO
            );
        }

        coupon.setDiscountValue(
                coupon.getDiscountValue()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        coupon.setMinimumOrderAmount(
                coupon.getMinimumOrderAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        if (coupon.getMaximumDiscountAmount()
                != null) {

            coupon.setMaximumDiscountAmount(
                    coupon.getMaximumDiscountAmount()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );
        }

        if (coupon.getUsedCount() == null) {

            coupon.setUsedCount(0);
        }
    }
}