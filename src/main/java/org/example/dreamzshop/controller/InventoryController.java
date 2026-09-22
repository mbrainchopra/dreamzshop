package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;


    // =========================================================
    // INVENTORY LIST
    // URL: /admin/inventory
    // =========================================================

    @GetMapping
    public String inventoryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        int pageSize = 10;

        // ---------------------------------------------
        // SAFE PAGE
        // ---------------------------------------------

        int safePage = Math.max(page, 0);

        // ---------------------------------------------
        // NORMALIZE SEARCH
        // ---------------------------------------------

        String searchKeyword = null;

        if (keyword != null
                && !keyword.trim().isEmpty()) {

            searchKeyword = keyword.trim();
        }

        // ---------------------------------------------
        // PAGE REQUEST
        // ---------------------------------------------

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        pageSize,
                        Sort.by(
                                Sort.Direction.ASC,
                                "name"
                        )
                );

        // ---------------------------------------------
        // GET PRODUCTS
        // ---------------------------------------------

        Page<Product> productPage =
                inventoryService.getAllProducts(
                        searchKeyword,
                        pageable
                );

        // ---------------------------------------------
        // HANDLE INVALID PAGE
        // ---------------------------------------------

        if (productPage.getTotalPages() > 0
                && safePage >= productPage.getTotalPages()) {

            safePage =
                    productPage.getTotalPages() - 1;

            pageable =
                    PageRequest.of(
                            safePage,
                            pageSize,
                            Sort.by(
                                    Sort.Direction.ASC,
                                    "name"
                            )
                    );

            productPage =
                    inventoryService.getAllProducts(
                            searchKeyword,
                            pageable
                    );
        }

        // ---------------------------------------------
        // INVENTORY COUNTS
        // ---------------------------------------------

        long totalProducts =
                inventoryService.getTotalProductCount();

        long lowStockProducts =
                inventoryService.getLowStockProductCount();

        long outOfStockProducts =
                inventoryService.getOutOfStockProductCount();

        // ---------------------------------------------
        // MODEL
        // ---------------------------------------------

        model.addAttribute(
                "productPage",
                productPage
        );

        model.addAttribute(
                "products",
                productPage.getContent()
        );

        model.addAttribute(
                "keyword",
                searchKeyword == null
                        ? ""
                        : searchKeyword
        );

        model.addAttribute(
                "currentPage",
                productPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                productPage.getTotalPages()
        );

        model.addAttribute(
                "totalProducts",
                totalProducts
        );

        model.addAttribute(
                "lowStockProducts",
                lowStockProducts
        );

        model.addAttribute(
                "outOfStockProducts",
                outOfStockProducts
        );

        return "admin/inventory";
    }


    // =========================================================
    // INVENTORY PRODUCT DETAILS
    // URL: /admin/inventory/product/{id}
    // =========================================================

    @GetMapping("/product/{productId}")
    public String productInventory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Product product =
                    inventoryService.getProduct(
                            productId
                    );

            int safePage =
                    Math.max(page, 0);

            Pageable pageable =
                    PageRequest.of(
                            safePage,
                            10,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "createdAt"
                            )
                    );

            Page<InventoryTransaction> transactionPage =
                    inventoryService.getTransactions(
                            productId,
                            pageable
                    );

            // -----------------------------------------
            // HANDLE INVALID TRANSACTION PAGE
            // -----------------------------------------

            if (transactionPage.getTotalPages() > 0
                    && safePage >= transactionPage.getTotalPages()) {

                safePage =
                        transactionPage.getTotalPages() - 1;

                pageable =
                        PageRequest.of(
                                safePage,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        );

                transactionPage =
                        inventoryService.getTransactions(
                                productId,
                                pageable
                        );
            }

            model.addAttribute(
                    "product",
                    product
            );

            model.addAttribute(
                    "transactionPage",
                    transactionPage
            );

            model.addAttribute(
                    "transactions",
                    transactionPage.getContent()
            );

            model.addAttribute(
                    "currentPage",
                    transactionPage.getNumber()
            );

            model.addAttribute(
                    "totalPages",
                    transactionPage.getTotalPages()
            );

            model.addAttribute(
                    "lowStock",
                    inventoryService.isLowStock(
                            product
                    )
            );

            return "admin/inventory-details";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );

            return "redirect:/admin/inventory";
        }
    }


    // =========================================================
    // STOCK IN
    // =========================================================

    @PostMapping("/stock-in")
    public String stockIn(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {

        try {

            inventoryService.addStock(
                    productId,
                    quantity,
                    reason
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Stock added successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }


    // =========================================================
    // STOCK OUT
    // =========================================================

    @PostMapping("/stock-out")
    public String stockOut(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {

        try {

            inventoryService.removeStock(
                    productId,
                    quantity,
                    reason
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Stock removed successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }


    // =========================================================
    // ADJUST STOCK
    // =========================================================

    @PostMapping("/adjust")
    public String adjustStock(
            @RequestParam Long productId,
            @RequestParam Integer newStock,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {

        try {

            inventoryService.adjustStock(
                    productId,
                    newStock,
                    reason
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Stock adjusted successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }
}