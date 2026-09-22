package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Offer;
import org.example.dreamzshop.service.OfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;


    // =========================================================
    // OFFERS LIST
    // GET /admin/offers
    // =========================================================

    @GetMapping
    public String offers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 50) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Offer> offers =
                offerService.getAllOffers(pageable);

        model.addAttribute("offers", offers);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/offers";
    }


    // =========================================================
    // ADD OFFER PAGE
    // GET /admin/offers/add
    // =========================================================

    @GetMapping("/add")
    public String addOffer(Model model) {

        model.addAttribute(
                "offer",
                new Offer()
        );

        return "admin/offer-form";
    }


    // =========================================================
    // SAVE OFFER
    // POST /admin/offers/save
    // =========================================================

    @PostMapping("/save")
    public String saveOffer(
            @ModelAttribute("offer") Offer offer,
            RedirectAttributes redirectAttributes
    ) {

        try {

            offerService.saveOffer(offer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Offer created successfully."
            );

            return "redirect:/admin/offers";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return "redirect:/admin/offers/add";
        }
    }


    // =========================================================
    // EDIT OFFER
    // GET /admin/offers/edit/{id}
    // =========================================================

    @GetMapping("/edit/{id}")
    public String editOffer(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Offer offer =
                    offerService.getOffer(id);

            model.addAttribute(
                    "offer",
                    offer
            );

            return "admin/offer-form";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return "redirect:/admin/offers";
        }
    }


    // =========================================================
    // UPDATE OFFER
    // POST /admin/offers/update/{id}
    // =========================================================

    @PostMapping("/update/{id}")
    public String updateOffer(
            @PathVariable Long id,
            @ModelAttribute("offer") Offer offer,
            RedirectAttributes redirectAttributes
    ) {

        try {

            offerService.updateOffer(id, offer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Offer updated successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/offers";
    }


    // =========================================================
    // TOGGLE OFFER
    // POST /admin/offers/toggle/{id}
    // =========================================================

    @PostMapping("/toggle/{id}")
    public String toggleOffer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            offerService.toggleOffer(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Offer status updated successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/offers";
    }


    // =========================================================
    // DELETE OFFER
    // POST /admin/offers/delete/{id}
    // =========================================================

    @PostMapping("/delete/{id}")
    public String deleteOffer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            offerService.deleteOffer(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Offer deleted successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/offers";
    }
}