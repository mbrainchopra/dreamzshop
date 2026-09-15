package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.dto.ChangePasswordRequest;
import org.example.dreamzshop.dto.CustomerProfileRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.service.CustomerProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;


    // =========================================================
    // PROFILE PAGE
    // =========================================================

    @GetMapping
    public String profile(
            Authentication authentication,
            Model model
    ) {

        User customer =
                customerProfileService.getCustomerProfile(
                        authentication.getName()
                );


        CustomerProfileRequest profileRequest =
                CustomerProfileRequest.builder()
                        .fullName(customer.getFullName())
                        .email(customer.getEmail())
                        .phone(customer.getPhone())
                        .build();


        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "profileRequest",
                profileRequest
        );

        model.addAttribute(
                "passwordRequest",
                new ChangePasswordRequest()
        );


        return "customer/profile";
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PostMapping("/update")
    public String updateProfile(
            Authentication authentication,
            @Valid
            @ModelAttribute("profileRequest")
            CustomerProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        if (bindingResult.hasErrors()) {

            User customer =
                    customerProfileService
                            .getCustomerProfile(
                                    authentication.getName()
                            );

            model.addAttribute(
                    "customer",
                    customer
            );

            model.addAttribute(
                    "passwordRequest",
                    new ChangePasswordRequest()
            );

            return "customer/profile";
        }


        String currentEmail =
                authentication.getName();


        try {

            customerProfileService.updateProfile(
                    currentEmail,
                    request
            );


            /*
             * Email may have changed.
             *
             * Spring Security's current Authentication
             * still contains the old email for this request.
             *
             * Therefore redirect to login so the user
             * can establish a fresh authenticated session.
             */

            if (!currentEmail.equalsIgnoreCase(
                    request.getEmail().trim()
            )) {

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Profile updated successfully. Please login again using your new email address."
                );

                return "redirect:/login";
            }


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile updated successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }


        return "redirect:/customer/profile";
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @Valid
            @ModelAttribute("passwordRequest")
            ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult.getFieldErrors()
                            .stream()
                            .findFirst()
                            .map(error ->
                                    error.getDefaultMessage()
                            )
                            .orElse(
                                    "Please enter valid password details."
                            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    message
            );

            return "redirect:/customer/profile";
        }


        try {

            customerProfileService.changePassword(
                    authentication.getName(),
                    request
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Password changed successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }


        return "redirect:/customer/profile";
    }
}