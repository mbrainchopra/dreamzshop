package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BannerService {

    Page<Banner> getAllBanners(
            String keyword,
            Pageable pageable
    );

    Banner getBanner(Long id);

    Banner saveBanner(Banner banner);

    Banner updateBanner(
            Long id,
            Banner banner
    );

    void toggleBanner(Long id);

    void deleteBanner(Long id);

    List<Banner> getActiveBanners();
}