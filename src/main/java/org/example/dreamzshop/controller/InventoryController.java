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

    @GetMapping
    public String inventoryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        int pageSize = 10;

        int safePage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(
                safePage,
                pageSize,
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                )
        );

        Page<Product> productPage =
                inventoryService.getAllProducts(
                        keyword,
                        pageable
                );

        if (productPage.getTotalPages() > 0
                && safePage >= productPage.getTotalPages()) {

            pageable = PageRequest.of(
                    productPage.getTotalPages() - 1,
                    pageSize,
                    Sort.by(
                            Sort.Direction.ASC,
                            "name"
                    )
            );

            productPage =
                    inventoryService.getAllProducts(
                            keyword,
                            pageable
                    );
        }

        /*
         * These counts now come from the complete database,
         * not just the currently displayed page.
         */
        long totalProducts =
                inventoryService.getTotalProductCount();

        long lowStockProducts =
                inventoryService.getLowStockProductCount();

        long outOfStockProducts =
                inventoryService.getOutOfStockProductCount();

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
                keyword == null ? "" : keyword
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

    @GetMapping("/product/{productId}")
    public String productInventory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Product product =
                    inventoryService.getProduct(productId);

            Pageable pageable =
                    PageRequest.of(
                            Math.max(page, 0),
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
                    inventoryService.isLowStock(product)
            );

            return "admin/inventory-details";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/inventory";
        }
    }

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
                    "Stock added successfully"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }

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
                    "Stock removed successfully"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }

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
                    "Stock adjusted successfully"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/inventory";
    }
}