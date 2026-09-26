package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Offer;
import org.example.dreamzshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OfferService {

    Page<Offer> getAllOffers(
            Pageable pageable
    );

    Offer getOffer(
            Long id
    );

    Offer saveOffer(
            Offer offer
    );

    Offer updateOffer(
            Long id,
            Offer offer
    );

    void deleteOffer(
            Long id
    );

    void toggleOffer(
            Long id
    );

    List<Offer> getCurrentlyActiveOffers();

    List<Offer> getEligibleOffers(
            Product product
    );

    Offer getBestOffer(
            Product product
    );

    /**
     * Returns the best eligible offer when the whole cart/order amount
     * satisfies the offer minimum-order requirement.
     */
    Offer getBestOffer(
            Product product,
            BigDecimal orderAmount,
            BigDecimal itemAmount
    );

    BigDecimal calculateDiscount(
            Offer offer,
            BigDecimal amount
    );

    BigDecimal getDiscountedPrice(
            Product product
    );
}