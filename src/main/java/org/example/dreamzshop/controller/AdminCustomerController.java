package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.service.OrderService;
import org.example.dreamzshop.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserRepository userRepository;

    private final OrderService orderService;


    // =========================================================
    // CUSTOMER LIST
    // =========================================================

    @GetMapping
    public String customers(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 10;
        }


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        Page<User> customers;


        if (keyword == null
                || keyword.trim().isEmpty()) {

            customers =
                    userRepository.findByRole(
                            Role.CUSTOMER,
                            pageable
                    );

        } else {

            String search =
                    keyword.trim();

            customers =
                    userRepository
                            .findByRoleAndFullNameContainingIgnoreCase(
                                    Role.CUSTOMER,
                                    search,
                                    pageable
                            );
        }


        model.addAttribute(
                "customers",
                customers
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );


        model.addAttribute(
                "totalCustomers",
                userRepository.countByRole(
                        Role.CUSTOMER
                )
        );

        model.addAttribute(
                "activeCustomers",
                userRepository.countByRoleAndEnabledTrue(
                        Role.CUSTOMER
                )
        );

        model.addAttribute(
                "inactiveCustomers",
                userRepository.countByRoleAndEnabledFalse(
                        Role.CUSTOMER
                )
        );


        return "admin/customers";
    }


    // =========================================================
    // CUSTOMER DETAILS
    // =========================================================

    @GetMapping("/{id}")
    public String customerDetails(
            @PathVariable Long id,
            Model model
    ) {

        User customer =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Customer not found."
                                )
                        );


        if (customer.getRole()
                != Role.CUSTOMER) {

            throw new IllegalArgumentException(
                    "Customer not found."
            );
        }


        Pageable orderPageable =
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        Page<org.example.dreamzshop.entity.Order> orders =
                orderService.getCustomerOrders(
                        customer.getEmail(),
                        orderPageable
                );


        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "orders",
                orders
        );


        return "admin/customer-details";
    }


    // =========================================================
    // ENABLE / DISABLE
    // =========================================================

    @PostMapping("/{id}/toggle")
    public String toggleCustomer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            User customer =
                    userRepository.findById(id)
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Customer not found."
                                    )
                            );


            if (customer.getRole()
                    != Role.CUSTOMER) {

                throw new IllegalArgumentException(
                        "Invalid customer account."
                );
            }


            customer.setEnabled(
                    !customer.isEnabled()
            );


            userRepository.save(
                    customer
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    customer.isEnabled()
                            ? "Customer enabled successfully."
                            : "Customer disabled successfully."
            );


        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }


        return "redirect:/admin/customers";
    }
}