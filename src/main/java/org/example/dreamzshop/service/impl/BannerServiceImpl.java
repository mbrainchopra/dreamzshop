package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Banner;
import org.example.dreamzshop.repository.BannerRepository;
import org.example.dreamzshop.service.BannerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;


    // =========================================================
    // GET ALL BANNERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Banner> getAllBanners(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null
                || keyword.trim().isEmpty()) {

            return bannerRepository
                    .findAllByOrderByDisplayOrderAsc(
                            pageable
                    );
        }

        return bannerRepository
                .findByTitleContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                );
    }


    // =========================================================
    // GET SINGLE BANNER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Banner getBanner(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Banner ID is required."
            );
        }

        return bannerRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Banner not found."
                        )
                );
    }


    // =========================================================
    // SAVE BANNER
    // =========================================================

    @Override
    public Banner saveBanner(Banner banner) {

        if (banner == null) {

            throw new IllegalArgumentException(
                    "Banner is required."
            );
        }

        validateBanner(banner);

        normalizeBanner(banner);

        return bannerRepository.save(banner);
    }


    // =========================================================
    // UPDATE BANNER
    // =========================================================

    @Override
    public Banner updateBanner(
            Long id,
            Banner banner
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Banner ID is required."
            );
        }

        if (banner == null) {

            throw new IllegalArgumentException(
                    "Banner is required."
            );
        }

        Banner existingBanner =
                getBanner(id);

        validateBanner(banner);

        existingBanner.setTitle(
                banner.getTitle()
        );

        existingBanner.setSubtitle(
                banner.getSubtitle()
        );

        existingBanner.setButtonText(
                banner.getButtonText()
        );

        existingBanner.setButtonUrl(
                banner.getButtonUrl()
        );

        existingBanner.setImageUrl(
                banner.getImageUrl()
        );

        existingBanner.setDisplayOrder(
                banner.getDisplayOrder()
        );

        existingBanner.setEnabled(
                banner.getEnabled()
        );

        existingBanner.setStartDate(
                banner.getStartDate()
        );

        existingBanner.setEndDate(
                banner.getEndDate()
        );

        normalizeBanner(existingBanner);

        return bannerRepository.save(
                existingBanner
        );
    }


    // =========================================================
    // TOGGLE BANNER
    // =========================================================

    @Override
    public void toggleBanner(Long id) {

        Banner banner =
                getBanner(id);

        banner.setEnabled(
                !Boolean.TRUE.equals(
                        banner.getEnabled()
                )
        );

        bannerRepository.save(banner);
    }


    // =========================================================
    // DELETE BANNER
    // =========================================================

    @Override
    public void deleteBanner(Long id) {

        Banner banner =
                getBanner(id);

        bannerRepository.delete(banner);
    }


    // =========================================================
    // ACTIVE BANNERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Banner> getActiveBanners() {

        return bannerRepository
                .findActiveBanners(
                        LocalDateTime.now()
                );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateBanner(
            Banner banner
    ) {

        if (banner.getTitle() == null
                || banner.getTitle().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Banner title is required."
            );
        }

        if (banner.getImageUrl() == null
                || banner.getImageUrl().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Banner image is required."
            );
        }

        if (banner.getDisplayOrder() == null) {

            banner.setDisplayOrder(0);
        }

        if (banner.getDisplayOrder() < 0) {

            throw new IllegalArgumentException(
                    "Display order cannot be negative."
            );
        }

        if (banner.getStartDate() != null
                && banner.getEndDate() != null
                && banner.getEndDate()
                .isBefore(banner.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date."
            );
        }
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    private void normalizeBanner(
            Banner banner
    ) {

        banner.setTitle(
                banner.getTitle().trim()
        );

        banner.setImageUrl(
                banner.getImageUrl().trim()
        );

        if (banner.getSubtitle() != null) {

            String value =
                    banner.getSubtitle().trim();

            banner.setSubtitle(
                    value.isEmpty()
                            ? null
                            : value
            );
        }

        if (banner.getButtonText() != null) {

            String value =
                    banner.getButtonText().trim();

            banner.setButtonText(
                    value.isEmpty()
                            ? null
                            : value
            );
        }

        if (banner.getButtonUrl() != null) {

            String value =
                    banner.getButtonUrl().trim();

            banner.setButtonUrl(
                    value.isEmpty()
                            ? null
                            : value
            );
        }

        if (banner.getDisplayOrder() == null) {

            banner.setDisplayOrder(0);
        }

        if (banner.getEnabled() == null) {

            banner.setEnabled(true);
        }
    }
}